"""Sound effect remakes for Collect The Orbs, inspired by the game's original sounds.

Each sound keeps the gesture and length of the original (a falling bloop, a held beep, a rising zip, a burst of noise, ...) but is
rebuilt on a small synth in three flavours, all around C5 (523 Hz, the pitch of the original beeps):

  1 "Remaster": retro saw/square voices and noise, cleaned up (closest to the originals)
  2 "Neon"    : smooth sines with a shimmering octave and a soft stereo glow
  3 "Crisp"   : bright pulse/triangle voices, tight and nearly dry

Run from the repo root:  python3 tools/sfx/synth.py [outdir]    -> outdir/<sound>_<1|2|3>.wav
"""
import os, sys
import numpy as np
from scipy.signal import butter, sosfilt, fftconvolve

SR = 44100
RNG = np.random.RandomState(7)
C5, D5, E5, G5, A5, C6, E6, G6 = 523.25, 587.33, 659.25, 783.99, 880.0, 1046.5, 1318.5, 1568.0
KINDS = '123'


def tt(d):
    return np.arange(int(d * SR)) / SR


def glide(f0, f1, n, curve=1.0):
    return f0 * (f1 / f0) ** (np.linspace(0, 1, n) ** curve)


def phase(f):
    return 2 * np.pi * np.cumsum(f) / SR


def lowpass(x, fc, order=2):
    return sosfilt(butter(order, min(fc, SR / 2 - 100), 'low', fs=SR, output='sos'), x)


def bandpass(x, lo, hi):
    return sosfilt(butter(2, [lo, min(hi, SR / 2 - 100)], 'band', fs=SR, output='sos'), x)


def env(n, a=0.004, tau=0.1, rel=0.012):
    t = np.arange(n) / SR
    e = np.minimum(t / max(a, 1e-4), 1.0) * np.exp(-t / tau)
    r = min(int(rel * SR), n // 2)
    if r > 0:
        e[-r:] *= np.linspace(1, 0, r)
    return e


def harmonics(ph, fmax, kind, k_max=24, bright=1.0):
    out = np.zeros_like(ph)
    K = int(min(k_max, (SR / 2 - 500) / max(fmax, 30)))
    for k in range(1, max(K, 2) + 1):
        g = (1.0 / k if kind == 'saw' else (1.0 / k if k % 2 else 0.0) if kind == 'square' else np.sin(np.pi * k * 0.25) / k)
        if g:
            out += g * bright ** (k - 1) * np.sin(k * ph)
    return out


def tone(kind, f0, f1, dur, tau=None, a=0.004, curve=1.0, vib=0.0):
    """One voice in the flavour `kind`, gliding f0 -> f1, with optional vibrato (fractional depth)."""
    n = int(dur * SR)
    f = glide(f0, f1, n, curve)
    if vib:
        f = f * (1 + vib * np.sin(2 * np.pi * 6.5 * tt(dur)[:n]))
    ph = phase(f)
    tau = tau or dur * 0.5
    fm = float(max(f0, f1))
    if kind == '1':
        x = 0.65 * harmonics(ph, fm, 'saw', bright=0.85) + 0.45 * harmonics(phase(f * 1.004), fm, 'square', bright=0.9)
        x = lowpass(x, 4200)
    elif kind == '2':
        sh = 0.35 * np.sin(phase(f * 3.01) + 1.2 * np.sin(phase(f * 0.5)))
        x = np.sin(ph) + 0.3 * np.sin(2 * ph) + 0.12 * np.sin(3 * ph) + 0.18 * sh + 0.35 * np.sin(phase(f * 1.003))
    else:
        tri = np.arcsin(np.sin(phase(f * 0.5))) * 2 / np.pi
        x = lowpass(0.8 * harmonics(ph, fm, 'pulse', bright=0.9) + 0.4 * tri, 6200)
    return x * env(n, a, tau)


def noise(kind, dur, lo, hi, tau=None, a=0.003, f_end=None):
    """Band of noise, optionally sweeping its centre from (lo+hi)/2 to f_end."""
    n = int(dur * SR)
    tau = tau or dur * 0.4
    if f_end is None:
        x = bandpass(RNG.randn(n), lo, hi)
    else:
        bw = (hi - lo) * 0.5
        base = lowpass(RNG.randn(n), max(bw, 80))
        base /= np.max(np.abs(base)) + 1e-9
        x = base * np.cos(phase(glide((lo + hi) / 2, f_end, n)))
    x = x / (np.max(np.abs(x)) + 1e-9)
    return x * env(n, a, tau) * (0.8 if kind == '3' else 1.0)


def thump(dur=0.2, f0=120.0, f1=40.0, tau=0.07):
    n = int(dur * SR)
    return np.sin(phase(glide(f0, f1, n))) * env(n, 0.002, tau)


def place(buf, x, start, gain=1.0):
    i = int(start * SR)
    e = min(len(buf), i + len(x))
    if 0 <= i < len(buf) and e > i:
        buf[i:e] += x[: e - i] * gain


def space(kind, x, room=1.0):
    """The flavour's stereo space: tight room (1), shimmering echo + reverb (2), dry (3)."""
    if kind == '3':
        d = int(0.006 * SR)
        return np.stack([x, np.pad(x, (d, 0))[: len(x)] * 0.97])
    r = np.random.RandomState(4)
    tau, wet = (0.12 * room, 0.14) if kind == '1' else (0.45 * room, 0.30)
    n = int(tau * 3 * SR) + 64
    t = np.arange(n) / SR
    ch = []
    for _ in (0, 1):
        ir = lowpass(r.randn(n), 5500) * np.exp(-t / tau)
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        ch.append(fftconvolve(x, ir)[: len(x) + n // 2])
    L = np.pad(x, (0, len(ch[0]) - len(x)))
    st = np.stack([L * (1 - wet) + ch[0] * wet * 2.0, L * (1 - wet) + ch[1] * wet * 2.0])
    if kind == '2':   # a faint echo on one side gives the glow
        d = int(0.085 * SR)
        st[1, d:] += st[0, : -d] * 0.22
    return st


def finish(st, peak=0.7):
    st = st * (peak / (np.max(np.abs(st)) + 1e-9))
    amp = np.max(np.abs(st), axis=0)
    idx = np.where(amp > peak * 0.02)[0]
    st = st[:, : (idx[-1] + 1 if len(idx) else st.shape[1])].copy()
    f = min(int(0.012 * SR), st.shape[1] // 2)
    st[:, -f:] *= np.linspace(1, 0, f)
    return st


# ---- the sounds ----------------------------------------------------------------------------------------------

SOUNDS = []


def S(name, desc):
    def reg(fn):
        SOUNDS.append((name, desc, fn))
        return fn
    return reg


def build(kind, total, parts, room=1.0):
    buf = np.zeros(int(total * SR) + 1)
    for start, x, g in parts:
        place(buf, x, start, g)
    return space(kind, buf, room)


@S('menu_tap', 'Small menu buttons: two quick rising beeps (like the original)')
def _(k):
    return build(k, 0.3, [(0, tone(k, C6, C6, 0.07, 0.05), 0.9), (0.075, tone(k, E6, E6, 0.09, 0.07), 1.0)], 0.3)


@S('menu_big', 'Big buttons: a quick upward chirp pop')
def _(k):
    return build(k, 0.3, [(0, tone(k, 380, 2300, 0.085, 0.06, curve=0.8), 1.0), (0.07, noise(k, 0.03, 2500, 8000, 0.012), 0.25)], 0.3)


@S('cant', "Can't afford it: a flat beep that sags")
def _(k):
    return build(k, 0.4, [(0, tone(k, C5, C5 * 0.9, 0.2, 0.16), 1.0), (0.0, tone(k, C5 * 0.5, C5 * 0.45, 0.2, 0.16), 0.35)], 0.35)


@S('bought', 'Buying something: a held, organ-like ding')
def _(k):
    parts = [(0, tone(k, C5, C5, 0.5, 0.3, vib=0.002), 0.9), (0, tone(k, C5 * 2, C5 * 2, 0.4, 0.2), 0.3),
             (0.055, tone(k, G5, G5, 0.45, 0.28), 0.55), (0.0, noise(k, 0.02, 3000, 9000, 0.008), 0.3)]
    return build(k, 0.7, parts, 0.7)


@S('launch', 'START: a rising rush of noise with a thump')
def _(k):
    parts = [(0, noise(k, 1.0, 250, 1500, 0.55, 0.12, f_end=4200), 0.9), (0, thump(0.35, 90, 38, 0.18), 0.9),
             (0.5, tone(k, C5, G5, 0.5, 0.3, curve=1.5), 0.35)]
    return build(k, 1.1, parts, 0.8)


@S('pre', 'The cue before the run: a soft puff of engine')
def _(k):
    parts = [(0, noise(k, 0.4, 150, 2600, 0.2, 0.08, f_end=1800), 0.8), (0, thump(0.3, 140, 60, 0.1), 0.5), (0.18, tone(k, C5, E5, 0.2, 0.12), 0.35)]
    return build(k, 0.5, parts, 0.5)


@S('absorb', 'Catching an orb: a falling bloop')
def _(k):
    parts = [(0, tone(k, 1100, 140, 0.4, 0.14, a=0.002, curve=0.55), 1.0), (0, tone(k, 2200, 280, 0.3, 0.08, a=0.002, curve=0.55), 0.3)]
    return build(k, 0.55, parts, 0.5)


@S('wrong', 'Wrong-color hit: a crackle of static')
def _(k):
    parts = [(0, noise(k, 0.4, 4000, 9500, 0.14, 0.002), 0.9), (0, tone(k, 300, 90, 0.3, 0.1, a=0.002), 0.7)]
    for i in range(1, 5):
        parts.append((i * 0.055, noise(k, 0.03, 3000, 9000, 0.01), 0.5))
    return build(k, 0.55, parts, 0.4)


@S('shield_hit', 'A hit absorbed by a shield item: a glassy shhk')
def _(k):
    parts = [(0, noise(k, 0.28, 3200, 9000, 0.08, 0.001), 1.0), (0, tone(k, 4700, 4650, 0.25, 0.07, a=0.001), 0.35), (0, tone(k, 2300, 2280, 0.25, 0.1), 0.3)]
    return build(k, 0.4, parts, 0.45)


@S('life', 'Gold orb: a rising power-up zip')
def _(k):
    parts = [(0, tone(k, 780, 3200, 0.17, 0.12, a=0.003, curve=1.6), 1.0), (0.1, tone(k, C6, C6, 0.14, 0.08), 0.4)]
    return build(k, 0.35, parts, 0.5)


@S('dead', 'Game over: a noisy burst that falls into a boom')
def _(k):
    parts = [(0, noise(k, 0.5, 600, 7000, 0.14, 0.001, f_end=300), 1.0), (0, thump(0.5, 130, 34, 0.16), 1.0),
             (0, tone(k, 1400, 70, 0.45, 0.14, a=0.001, curve=0.6), 0.55)]
    for i in range(1, 6):
        parts.append((i * 0.045, noise(k, 0.025, 1500, 8000, 0.01), 0.45))
    return build(k, 0.8, parts, 0.7)


@S('item_pickup', 'Picking up an item: a very quick rising zip')
def _(k):
    parts = [(0, tone(k, 170, 1900, 0.075, 0.06, a=0.002, curve=1.3), 1.0), (0.05, tone(k, 1900, 1900, 0.1, 0.07), 0.45)]
    return build(k, 0.25, parts, 0.35)


@S('item_tick', 'Item about to run out: a plain beep')
def _(k):
    return build(k, 0.25, [(0, tone(k, C5, C5, 0.17, 0.11, a=0.002), 1.0)], 0.3)


@S('swap', 'Color swap item: a held beep (plays reversed when it ends)')
def _(k):
    parts = [(0, tone(k, C5, C5, 0.75, 0.5, vib=0.004), 0.9), (0, tone(k, C5 * 2, C5 * 2.005, 0.6, 0.3), 0.22), (0, noise(k, 0.03, 2500, 8000, 0.01), 0.25)]
    return build(k, 0.9, parts, 0.6)


@S('mult', 'x2 / x3 item: a bright ping with a shimmer (reversed when it ends)')
def _(k):
    parts = [(0, tone(k, 1390, 1380, 0.26, 0.2, vib=0.01), 1.0), (0, tone(k, 2780, 2770, 0.2, 0.1), 0.3), (0, tone(k, 695, 690, 0.2, 0.12), 0.3)]
    return build(k, 0.4, parts, 0.5)


@S('slow', 'Slow item: a sweep that sinks (reversed when it ends)')
def _(k):
    parts = [(0, tone(k, 1150, 180, 0.95, 0.5, a=0.003, curve=0.7), 1.0), (0, noise(k, 0.9, 400, 5000, 0.35, 0.003, f_end=300), 0.35)]
    return build(k, 1.05, parts, 0.8)


@S('shield_item', 'Shield item: a high shimmering shing (reversed when it ends)')
def _(k):
    parts = [(0, noise(k, 0.5, 3000, 8500, 0.22, 0.04, f_end=6500), 0.9), (0.03, tone(k, 3500, 5600, 0.45, 0.25, a=0.01), 0.3),
             (0.05, tone(k, 1750, 2800, 0.45, 0.25, a=0.01), 0.25)]
    return build(k, 0.6, parts, 0.7)


@S('switch', 'Switching shield color: a mid swoosh that dips')
def _(k):
    parts = [(0, noise(k, 0.3, 350, 2800, 0.12, 0.01, f_end=500), 0.8), (0, tone(k, 430, 190, 0.28, 0.1, a=0.004), 0.85)]
    return build(k, 0.4, parts, 0.4)


@S('count_tick', 'Counting orbs on the game-over screen: a short ding')
def _(k):
    return build(k, 0.25, [(0, tone(k, C5, C5, 0.13, 0.07, a=0.002), 1.0), (0, tone(k, C5 * 2, C5 * 2, 0.1, 0.05), 0.3)], 0.3)


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
    print(len(render_all(out)), 'sounds x 3 flavours ->', out)
