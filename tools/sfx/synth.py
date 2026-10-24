"""Physically inspired sound design for Collect The Orbs: the sounds a real ship and energy shield might make.

No instruments: everything is built from the things real machines and plasma do (turbulent airflow, a turbine spooling, mains hum
in a transformer, arcing and crackle, ringing metal and glass, relays clicking, pressure released). Each sound comes in two takes:

  A "Grounded"  : close and dry, like a recording from inside the machinery
  B "Cinematic" : the same sources, bigger, with more layers and a roomy tail

Run from the repo root:  python3 tools/sfx/synth.py [outdir]    -> outdir/<sound>_<A|B>.wav
"""
import os, sys
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, fftconvolve

SR = 44100
KINDS = 'AB'
RNG = np.random.RandomState(21)


def tt(d):
    return np.arange(int(d * SR)) / SR


def glide(f0, f1, n, curve=1.0):
    return f0 * (f1 / f0) ** (np.linspace(0, 1, n) ** curve)


def phase(f):
    return 2 * np.pi * np.cumsum(f) / SR


def lowpass(x, fc, order=2):
    return sosfilt(butter(order, min(fc, SR / 2 - 100), 'low', fs=SR, output='sos'), x)


def highpass(x, fc, order=2):
    return sosfilt(butter(order, fc, 'high', fs=SR, output='sos'), x)


def bandpass(x, lo, hi):
    return sosfilt(butter(2, [lo, min(hi, SR / 2 - 100)], 'band', fs=SR, output='sos'), x)


def env(n, a=0.004, tau=0.1, rel=0.012):
    t = np.arange(n) / SR
    e = np.minimum(t / max(a, 1e-4), 1.0) * np.exp(-t / tau)
    r = min(int(rel * SR), n // 2)
    if r > 0:
        e[-r:] *= np.linspace(1, 0, r)
    return e


def swell(n, a, hold, r):
    """Amplitude shape: rise over a seconds, hold, fall over r seconds (all as fractions handled in seconds)."""
    t = np.arange(n) / SR
    up = np.clip(t / max(a, 1e-4), 0, 1) ** 1.5
    dn = np.clip((n / SR - t) / max(r, 1e-4), 0, 1) ** 1.5
    return up * dn


def brown(n):
    x = np.cumsum(RNG.randn(n))
    x = highpass(x, 25)
    return x / (np.max(np.abs(x)) + 1e-9)


def white(n):
    return RNG.randn(n) / 3.0


def sweep_filter(x, fc, q=1.0, mode='lp', block=128):
    """Resonant biquad whose cutoff follows the array fc (Hz), processed in blocks (RBJ cookbook)."""
    y = np.zeros_like(x)
    zi = np.zeros(2)
    for i in range(0, len(x), block):
        f = float(fc[min(i + block // 2, len(fc) - 1)])
        w0 = 2 * np.pi * min(max(f, 20.0), SR / 2 - 200) / SR
        al = np.sin(w0) / (2 * q)
        c = np.cos(w0)
        if mode == 'lp':
            b = [(1 - c) / 2, 1 - c, (1 - c) / 2]
        elif mode == 'bp':
            b = [al, 0, -al]
        else:
            b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
        a = [1 + al, -2 * c, 1 - al]
        seg, zi = lfilter(np.array(b) / a[0], np.array(a) / a[0], x[i:i + block], zi=zi)
        y[i:i + block] = seg
    return y


def modal(freqs, decays, amps, dur, jitter=0.0):
    """Ringing object: a sum of damped sinusoids (metal, glass, a struck panel)."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    out = np.zeros(n)
    for f, d, a in zip(freqs, decays, amps):
        f = f * (1 + jitter * RNG.randn())
        out += a * np.sin(2 * np.pi * f * t + RNG.rand() * 6.28) * np.exp(-t / d)
    return out * np.minimum(t / 0.0008, 1.0)


def metal(f0, dur, decay=0.12, bright=1.0):
    ratios = [1.0, 2.76, 5.40, 8.93, 13.34]
    return modal([f0 * r for r in ratios], [decay / (1 + 0.8 * i) for i in range(5)], [1.0, 0.7 * bright, 0.5 * bright, 0.35 * bright, 0.2 * bright], dur)


def glass(f0, dur, decay=0.2):
    ratios = [1.0, 2.32, 4.25, 6.63, 9.38]
    return modal([f0 * r for r in ratios], [decay / (1 + 0.6 * i) for i in range(5)], [1.0, 0.6, 0.45, 0.3, 0.2], dur)


def crackle(dur, rate0, rate1, tone=(2000, 9000), tau=0.003):
    """Plasma / fire crackle: random tiny bursts whose rate changes over the sound."""
    n = int(dur * SR)
    out = np.zeros(n)
    t = 0.0
    while t < dur:
        r = rate0 + (rate1 - rate0) * (t / dur)
        t += RNG.exponential(1.0 / max(r, 1.0))
        i = int(t * SR)
        if i >= n - 200:
            break
        m = int(0.012 * SR)
        burst = bandpass(RNG.randn(m), tone[0], tone[1]) * np.exp(-np.arange(m) / SR / tau) * (0.3 + RNG.rand())
        out[i:i + m] += burst[: n - i]
    return out / (np.max(np.abs(out)) + 1e-9)


def hum(f0, dur, harm=(1.0, 0.6, 0.45, 0.3, 0.2, 0.12, 0.08), drift=0.003, f1=None):
    """Transformer / energy-field hum: a stack of harmonics with slow drift."""
    n = int(dur * SR)
    f = glide(f0, f1 or f0, n)
    f = f * (1 + drift * lowpass(RNG.randn(n), 8) * 6)
    ph = phase(f)
    return sum(a * np.sin((k + 1) * ph) for k, a in enumerate(harm))


def turbine(f0, f1, dur, curve=1.0):
    """A spooling turbine: a whine and its harmonics rising with the rotor speed, plus blade-pass noise."""
    n = int(dur * SR)
    f = glide(f0, f1, n, curve)
    ph = phase(f)
    x = np.sin(ph) + 0.5 * np.sin(2 * ph + 0.3) + 0.35 * np.sin(3.5 * ph) + 0.2 * np.sin(5 * ph)
    return x * (1 + 0.15 * np.sin(phase(f * 0.5)))


def boom(dur, f0=120.0, f1=34.0, tau=0.2):
    n = int(dur * SR)
    return np.sin(phase(glide(f0, f1, n, 0.6))) * env(n, 0.002, tau)


def relay(gain=1.0, f=900.0):
    """A relay/latch: two quick contact clicks and the small housing ringing."""
    n = int(0.09 * SR)
    out = np.zeros(n)
    for t0, g in ((0.0, 1.0), (0.011, 0.6)):
        i = int(t0 * SR)
        m = int(0.006 * SR)
        out[i:i + m] += highpass(RNG.randn(m), 1800) * np.exp(-np.arange(m) / SR / 0.0012) * g * 1.4
    ring = modal([f, f * 2.7, f * 5.1], [0.016, 0.01, 0.006], [0.6, 0.35, 0.2], 0.09)
    return (out + ring[:n]) * gain


def place(buf, x, start, gain=1.0):
    i = int(start * SR)
    e = min(len(buf), i + len(x))
    if 0 <= i < len(buf) and e > i:
        buf[i:e] += x[: e - i] * gain


def space(kind, x, size=1.0):
    """A: a small steel room; B: a big hall with a long tail."""
    r = np.random.RandomState(9)
    tau, wet = (0.10 * size, 0.12) if kind == 'A' else (0.55 * size, 0.30)
    n = int(tau * 3 * SR) + 64
    t = np.arange(n) / SR
    ch = []
    for _ in (0, 1):
        ir = lowpass(r.randn(n), 6500 if kind == 'A' else 4500) * np.exp(-t / tau)
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        ch.append(fftconvolve(x, ir)[: len(x) + n // 2])
    L = np.pad(x, (0, len(ch[0]) - len(x)))
    return np.stack([L * (1 - wet) + ch[0] * wet * 2.0, L * (1 - wet) + ch[1] * wet * 2.0])


def finish(st, peak=0.7):
    st = st * (peak / (np.max(np.abs(st)) + 1e-9))
    amp = np.max(np.abs(st), axis=0)
    idx = np.where(amp > peak * 0.02)[0]
    st = st[:, : (idx[-1] + 1 if len(idx) else st.shape[1])].copy()
    f = min(int(0.012 * SR), st.shape[1] // 2)
    st[:, -f:] *= np.linspace(1, 0, f)
    return st


SOUNDS = []


def S(name, desc):
    def reg(fn):
        SOUNDS.append((name, desc, fn))
        return fn
    return reg


def build(kind, total, parts, size=1.0):
    buf = np.zeros(int(total * SR) + 1)
    for start, x, g in parts:
        place(buf, x, start, g)
    return space(kind, buf, size)


def rush(dur, f0, f1, level=1.0, q=0.9, a=0.05, r=0.15, src=None):
    """Turbulent airflow: brown/white noise through a resonant filter sweeping f0 -> f1."""
    n = int(dur * SR)
    x = (src if src is not None else (brown(n) * 0.7 + white(n) * 0.5))[:n]
    y = sweep_filter(x, glide(f0, f1, n), q, 'lp')
    return y / (np.max(np.abs(y)) + 1e-9) * swell(n, a, 0, r) * level


# ---- the sounds ---------------------------------------------------------------------------------------------------

@S('menu_tap', 'Small buttons: a light relay click with a tiny ring')
def _(k):
    big = k == 'B'
    return build(k, 0.2, [(0, relay(1.0, 1400), 1.0), (0.004, metal(2600, 0.09, 0.03), 0.18 if not big else 0.3)], 0.3)


@S('menu_big', 'Big buttons: a heavy hatch latch')
def _(k):
    parts = [(0, relay(1.0, 520), 1.0), (0.012, modal([95, 190, 410], [0.05, 0.035, 0.02], [1, 0.5, 0.3], 0.14), 0.9), (0.02, metal(1500, 0.12, 0.05), 0.15)]
    return build(k, 0.3, parts, 0.35)


@S('cant', "Can't afford it: a power relay that fails to engage")
def _(k):
    n = int(0.26 * SR)
    buzz = lowpass(np.sign(np.sin(phase(glide(120, 62, n)))) * 0.6 + white(n) * 0.2, 1400) * env(n, 0.003, 0.12)
    return build(k, 0.4, [(0, relay(0.8, 600), 0.7), (0.02, buzz, 0.9), (0.02, crackle(0.22, 120, 20, (1500, 6000)), 0.35)], 0.35)


@S('bought', 'Buying: a vault tumbler unlocking')
def _(k):
    parts = [(i * 0.07, relay(0.8 + 0.08 * i, 1100 + 150 * i), 0.8) for i in range(3)]
    parts += [(0.23, modal([180, 360, 740], [0.1, 0.07, 0.04], [1, 0.6, 0.3], 0.25), 0.9), (0.24, metal(880, 0.5, 0.25), 0.5), (0.24, glass(1760, 0.45, 0.25), 0.25)]
    return build(k, 0.8, parts, 0.7)


@S('launch', 'START: a ship lighting its engines and taking off')
def _(k):
    big = k == 'B'
    d = 1.2
    parts = [(0, boom(0.45, 70, 32, 0.25), 0.9), (0, crackle(0.25, 400, 60, (800, 6000)), 0.35),
             (0.05, rush(d - 0.05, 220, 4800, 1.0, 1.0, 0.45, 0.35), 0.95), (0.1, rush(d - 0.1, 120, 1600, 1.0, 0.8, 0.5, 0.35, brown(int(d * SR))), 0.7),
             (0.1, turbine(180, 1500, d - 0.1, 1.5) * swell(int((d - 0.1) * SR), 0.5, 0, 0.35), 0.28 if not big else 0.4)]
    return build(k, d + 0.1, parts, 0.8)


@S('pre', 'The cue before the run: engines pressurising')
def _(k):
    parts = [(0, relay(0.9, 700), 0.7), (0.02, rush(0.4, 200, 2200, 1.0, 0.8, 0.12, 0.15), 0.7), (0.02, turbine(160, 520, 0.38, 1.3) * 0.25, 1.0), (0.03, boom(0.25, 90, 55, 0.1), 0.5)]
    return build(k, 0.5, parts, 0.5)


@S('absorb', 'Catching an orb: the shield soaking up energy')
def _(k):
    n = int(0.45 * SR)
    f = glide(260, 1600, n, 0.7)
    charge = sweep_filter(brown(n) * 0.5 + white(n), f, 6.0, 'bp') * swell(n, 0.05, 0, 0.3)
    charge /= np.max(np.abs(charge)) + 1e-9
    parts = [(0, charge, 1.0), (0, hum(110, 0.4, f1=220) * swell(int(0.4 * SR), 0.04, 0, 0.3), 0.3),
             (0.0, crackle(0.35, 260, 40, (2500, 9000)), 0.35), (0.25, boom(0.2, 150, 70, 0.07), 0.6)]
    return build(k, 0.55, parts, 0.45)


@S('wrong', 'Wrong-color hit: the shield overloads and arcs')
def _(k):
    n = int(0.4 * SR)
    saw = ((np.cumsum(glide(190, 60, n)) / SR) % 1.0) * 2 - 1
    buzz = np.tanh(3 * saw) * env(n, 0.002, 0.14)
    parts = [(0, buzz, 0.8), (0, crackle(0.38, 450, 60, (1800, 9000)), 0.9), (0, boom(0.2, 140, 50, 0.07), 0.6), (0, highpass(white(int(0.03 * SR)), 2500) * env(int(0.03 * SR), 0.0005, 0.006), 1.0)]
    return build(k, 0.5, parts, 0.4)


@S('shield_hit', 'A hit absorbed by a shield item: a flash of ringing energy')
def _(k):
    parts = [(0, highpass(white(int(0.05 * SR)), 3000) * env(int(0.05 * SR), 0.0004, 0.01), 1.0), (0, glass(2100, 0.3, 0.1), 0.6),
             (0, metal(740, 0.3, 0.07), 0.45), (0.0, crackle(0.25, 220, 30, (3000, 10000)), 0.45), (0.0, hum(120, 0.25, f1=70) * env(int(0.25 * SR), 0.003, 0.09), 0.4)]
    return build(k, 0.4, parts, 0.45)


@S('life', 'Gold orb: the shield recharging')
def _(k):
    n = int(0.34 * SR)
    h = hum(140, 0.34, harm=(1.0, 0.8, 0.6, 0.4, 0.3), f1=520) * swell(n, 0.04, 0, 0.18)
    parts = [(0, h, 0.8), (0, rush(0.34, 400, 5200, 0.8, 2.5, 0.03, 0.2, white(n)), 0.5), (0.05, crackle(0.28, 60, 320, (4000, 11000)), 0.5), (0.22, glass(2400, 0.2, 0.12), 0.35)]
    return build(k, 0.45, parts, 0.5)


@S('dead', 'Game over: the ship breaking apart')
def _(k):
    d = 0.9
    parts = [(0, boom(0.7, 110, 28, 0.3), 1.0), (0, rush(d, 5200, 160, 1.0, 0.8, 0.003, 0.5), 0.85), (0, crackle(0.6, 500, 40, (700, 8000), 0.006), 0.7),
             (0.05, metal(310, 0.5, 0.25, 0.8), 0.45), (0.14, metal(560, 0.4, 0.18), 0.3), (0.3, metal(220, 0.5, 0.3), 0.3)]
    return build(k, d + 0.1, parts, 0.9)


@S('item_pickup', 'Item pickup: a quick magnetic grab')
def _(k):
    n = int(0.16 * SR)
    zip_ = sweep_filter(white(n), glide(300, 6000, n, 1.6), 4.0, 'bp') * env(n, 0.003, 0.07)
    return build(k, 0.22, [(0, zip_ / (np.max(np.abs(zip_)) + 1e-9), 0.9), (0.09, relay(0.9, 1600), 0.7), (0.09, glass(3100, 0.1, 0.06), 0.25)], 0.3)


@S('item_tick', 'Item about to run out: a sonar-like blip')
def _(k):
    return build(k, 0.2, [(0, relay(0.7, 1300), 0.6), (0, glass(1600, 0.15, 0.06), 0.5)], 0.3)


@S('swap', 'Color swap item: the field retuning (reversed when it ends)')
def _(k):
    n = int(0.8 * SR)
    h = hum(210, 0.8, harm=(1.0, 0.7, 0.5, 0.35, 0.25, 0.15), f1=190) * (1 + 0.35 * np.sin(2 * np.pi * 5 * tt(0.8)[:n]))
    h = sweep_filter(h, glide(500, 2600, n, 0.5), 2.0, 'lp') * env(n, 0.01, 0.45)
    return build(k, 0.9, [(0, h, 0.9), (0, rush(0.5, 600, 3000, 0.6, 1.5, 0.05, 0.3), 0.35), (0, crackle(0.6, 90, 20, (3000, 9000)), 0.25)], 0.6)


@S('mult', 'x2 / x3 item: a capacitor charging and discharging (reversed when it ends)')
def _(k):
    n = int(0.22 * SR)
    whine = turbine(700, 2600, 0.22, 1.2) * swell(n, 0.18, 0, 0.01)
    pop = int(0.2 * SR)
    parts = [(0, whine, 0.5), (0.21, boom(0.12, 220, 90, 0.04), 0.5), (0.21, crackle(0.14, 500, 80, (2500, 10000)), 0.8), (0.21, glass(2200, 0.2, 0.1), 0.45)]
    return build(k, 0.4, parts, 0.5)


@S('slow', 'Slow item: a turbine spooling down (reversed when it ends)')
def _(k):
    d = 1.0
    n = int(d * SR)
    parts = [(0, turbine(1100, 120, d, 0.7) * env(n, 0.005, 0.5), 0.45), (0, rush(d, 4200, 140, 1.0, 1.2, 0.01, 0.4), 0.8), (0, boom(0.6, 80, 38, 0.3), 0.35)]
    return build(k, d + 0.1, parts, 0.8)


@S('shield_item', 'Shield item: a plasma field forming (reversed when it ends)')
def _(k):
    d = 0.6
    n = int(d * SR)
    parts = [(0, hum(130, d, harm=(1, 0.8, 0.7, 0.5, 0.4, 0.3, 0.2), f1=260) * swell(n, 0.35, 0, 0.1), 0.45),
             (0, rush(d, 800, 7500, 0.9, 1.5, 0.3, 0.12, white(n)), 0.6), (0.1, crackle(0.5, 40, 420, (3000, 11000)), 0.6), (0.4, glass(3300, 0.2, 0.1), 0.3)]
    return build(k, d + 0.1, parts, 0.7)


@S('switch', 'Switching shield color: a valve and a spark')
def _(k):
    n = int(0.26 * SR)
    puff = rush(0.25, 500, 3200, 1.0, 1.2, 0.01, 0.12, white(n))
    return build(k, 0.35, [(0, puff, 0.7), (0.01, relay(1.0, 800), 0.8), (0.15, crackle(0.1, 400, 100, (3000, 9000)), 0.35)], 0.35)


@S('count_tick', 'Counting orbs on the game-over screen: a tiny relay tick')
def _(k):
    return build(k, 0.15, [(0, relay(0.9, 1500), 0.9), (0.002, glass(2400, 0.1, 0.04), 0.2)], 0.25)


REVERSED = {'swap': 'swap_end', 'mult': 'mult_end', 'slow': 'slow_end', 'shield_item': 'shield_item_end'}


def render_all(outdir):
    import wave
    os.makedirs(outdir, exist_ok=True)
    names = []
    for name, desc, fn in SOUNDS:
        for kind in KINDS:
            st = finish(fn(kind))
            outs = [(name, st)]
            if name in REVERSED:
                outs.append((REVERSED[name], finish(st[:, ::-1].copy())))
            for nm, s in outs:
                with wave.open(os.path.join(outdir, '%s_%s.wav' % (nm, kind)), 'wb') as w:
                    w.setnchannels(2)
                    w.setsampwidth(2)
                    w.setframerate(SR)
                    w.writeframes((np.clip(s.T, -1, 1) * 32767).astype('<i2').tobytes())
        names.append(name)
    return names


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else '/tmp/cto_sfx'
    print(len(render_all(out)), 'sounds x 2 takes ->', out)
