"""Sound effect options for Collect The Orbs: a small additive/FM synth that renders three families of each sound.

  A "Glass"  : FM bells and glassy sines with a long shimmering reverb (soft, spacey)
  B "Synth"  : detuned saw pads and plucks with a ping-pong echo (synthwave)
  C "Pulse"  : tight pulse/triangle blips, almost dry (arcade-clean)

All sounds are written in A minor pentatonic (A C D E G) so they sit together, like the speed charge-up sounds.

Run from the repo root:  python3 tools/sfx/synth.py [outdir]    -> outdir/<sound>_<A|B|C>.wav
"""
import os, sys
import numpy as np
from scipy.signal import butter, sosfilt, fftconvolve

SR = 44100
RNG = np.random.RandomState(11)

# A minor pentatonic across a few octaves
A3, C4, D4, E4, G4 = 220.0, 261.63, 293.66, 329.63, 392.0
A4, C5, D5, E5, G5 = 440.0, 523.25, 587.33, 659.25, 783.99
A5, C6, D6, E6, G6, A6 = 880.0, 1046.5, 1174.7, 1318.5, 1568.0, 1760.0
A2, E3 = 110.0, 164.81


def tt(d):
    return np.arange(int(d * SR)) / SR


def fgrid(f0, f1, n, curve=1.0):
    """Frequency per sample, gliding f0 -> f1 (curve > 1 lingers on f0)."""
    x = np.linspace(0, 1, n) ** curve
    return f0 * (f1 / f0) ** x


def phase(f):
    return 2 * np.pi * np.cumsum(f) / SR


def lowpass(x, fc, order=2):
    return sosfilt(butter(order, min(fc, SR / 2 - 100), 'low', fs=SR, output='sos'), x)


def bandpass(x, lo, hi):
    return sosfilt(butter(2, [lo, min(hi, SR / 2 - 100)], 'band', fs=SR, output='sos'), x)


def decay(n, a=0.004, tau=0.12, rel=0.01):
    t = np.arange(n) / SR
    e = np.minimum(t / max(a, 1e-4), 1.0) * np.exp(-t / tau)
    r = int(rel * SR)
    if r > 0 and n > r:
        e[-r:] *= np.linspace(1, 0, r)
    return e


def harmonic_wave(ph, f, kind, brightness=1.0):
    """Band-limited saw / square / pulse built from harmonics (f may be an array)."""
    out = np.zeros_like(ph)
    fm = float(np.max(f))
    K = int(min(24, (SR / 2 - 500) / max(fm, 30)))
    for k in range(1, max(K, 2) + 1):
        if kind == 'saw':
            g = 1.0 / k
        elif kind == 'square':
            g = (1.0 / k) if k % 2 else 0.0
        else:  # 25% pulse
            g = np.sin(np.pi * k * 0.25) / k
        g = g * brightness ** (k - 1)
        if g:
            out += g * np.sin(k * ph)
    return out


def voice(kind, f0, f1, dur, tau=None, curve=1.0):
    """One note (mono) in the timbre of the family, gliding from f0 to f1."""
    n = int(dur * SR)
    f = fgrid(f0, f1, n, curve)
    ph = phase(f)
    if kind == 'A':   # glass: inharmonic FM bell + octave sine
        tau = tau or dur * 0.45
        idx = 2.2 * np.exp(-np.arange(n) / SR / (tau * 0.7))
        x = np.sin(ph + idx * np.sin(phase(f * 3.5)))
        x += 0.35 * np.sin(2 * ph) * np.exp(-np.arange(n) / SR / (tau * 0.5))
        return x * decay(n, 0.003, tau)
    if kind == 'B':   # synth: two detuned saws, brightness decays
        tau = tau or dur * 0.6
        a = harmonic_wave(ph, f, 'saw', 0.78)
        b = harmonic_wave(phase(f * 1.006), f, 'saw', 0.78)
        x = lowpass((a + b) * 0.5, 3500)
        return x * decay(n, 0.006, tau)
    tau = tau or dur * 0.35   # pulse: 25% pulse + triangle sub
    tri = np.arcsin(np.sin(phase(f * 0.5))) * 2 / np.pi
    x = 0.8 * harmonic_wave(ph, f, 'pulse', 0.9) + 0.4 * tri
    return lowpass(x, 5200) * decay(n, 0.002, tau)


def whoosh(dur, f0, f1, bw=220, curve=1.0, gain=1.0):
    """Swept band of noise: slow noise ring-modulated up to a moving center frequency."""
    n = int(dur * SR)
    base = lowpass(RNG.randn(n), bw)
    base /= np.max(np.abs(base)) + 1e-9
    return base * np.cos(phase(fgrid(f0, f1, n, curve))) * gain


def click(dur=0.012, f=3000, gain=1.0):
    n = int(dur * SR)
    return bandpass(RNG.randn(n), f * 0.5, f * 1.6) * np.exp(-np.arange(n) / SR / (dur / 3)) * gain


def servo(dur, f0, f1, am=70.0, curve=1.0):
    """A small motor/actuator: buzzing saw with gear-tooth amplitude modulation, gliding in pitch."""
    n = int(dur * SR)
    f = fgrid(f0, f1, n, curve)
    x = harmonic_wave(phase(f), f, 'saw', 0.8) * (0.55 + 0.45 * np.sin(phase(f * am / max(f0, 1.0) * 0.5)))
    x = bandpass(x, 120, 2600)
    t = np.arange(n) / SR
    e = np.minimum(t / 0.02, 1.0) * np.minimum((dur - t) / 0.04, 1.0).clip(0, 1)
    return x * e


def latch(gain=1.0):
    """A latch/relay: sharp click plus a short low thunk."""
    n = int(0.09 * SR)
    t = np.arange(n) / SR
    thunk = np.sin(2 * np.pi * (95 + 60 * np.exp(-t / 0.01)) * t) * np.exp(-t / 0.022)
    c = np.pad(click(0.014, 3400, 1.0), (0, n - int(0.014 * SR)))
    return (c * 0.9 + thunk * 0.8) * gain


def clank(gain=1.0, f=820.0):
    """A metallic clank: inharmonic FM hit."""
    n = int(0.22 * SR)
    t = np.arange(n) / SR
    ph = 2 * np.pi * f * t
    x = np.sin(ph + 3.2 * np.sin(2.76 * ph) * np.exp(-t / 0.04)) * np.exp(-t / 0.055)
    return (x + 0.5 * np.pad(click(0.01, 4200, 1.0), (0, n - int(0.01 * SR)))) * gain


def hiss(dur=0.25, gain=1.0):
    """Pneumatic release: bright noise that dies away."""
    n = int(dur * SR)
    return bandpass(RNG.randn(n), 2800, 9500) * np.exp(-np.arange(n) / SR / (dur / 3.0)) * gain


def ratchet(count, gap, gain=1.0):
    buf = np.zeros(int((count * gap + 0.1) * SR))
    for i in range(count):
        place(buf, click(0.012, 2600 + 160 * i, 1.0), i * gap, gain * (0.8 + 0.05 * i))
    return buf


MECH = {  # sound -> list of (time, element, args...) ; element in latch servo clank hiss ratchet
    'menu_tap': [(0, 'latch', 0.8)],
    'menu_big': [(0, 'ratchet', 2, 0.045, 0.9), (0.09, 'latch', 1.0)],
    'cant': [(0, 'servo', 0.16, 240, 85, 60), (0.15, 'clank', 0.7, 520)],
    'bought': [(0, 'ratchet', 4, 0.065, 0.8), (0.27, 'latch', 1.0), (0.28, 'hiss', 0.22, 0.4)],
    'launch': [(0, 'servo', 0.5, 70, 560, 60, 1.8), (0.1, 'ratchet', 3, 0.1, 0.6), (0.4, 'clank', 1.0, 700), (0.42, 'hiss', 0.4, 0.5)],
    'pre': [(0, 'servo', 0.3, 160, 520, 70, 1.4), (0.3, 'latch', 0.8)],
    'absorb': [(0, 'latch', 0.8), (0.03, 'servo', 0.12, 420, 900, 90)],
    'wrong': [(0, 'clank', 1.0, 300), (0.02, 'servo', 0.22, 280, 70, 60)],
    'shield_hit': [(0, 'clank', 1.0, 1100), (0.01, 'hiss', 0.12, 0.35)],
    'life': [(0, 'ratchet', 3, 0.08, 0.8), (0.16, 'servo', 0.2, 300, 700, 80), (0.2, 'hiss', 0.2, 0.3)],
    'dead': [(0, 'servo', 1.0, 420, 38, 55, 1.4), (0.0, 'clank', 1.0, 260), (0.35, 'clank', 0.7, 200), (0.7, 'clank', 0.5, 160), (0.05, 'hiss', 0.6, 0.6)],
    'item_pickup': [(0, 'latch', 0.9), (0.04, 'servo', 0.2, 300, 800, 80), (0.14, 'latch', 0.6)],
    'item_tick': [(0, 'latch', 0.7)],
    'swap': [(0, 'latch', 0.8), (0.02, 'servo', 0.22, 260, 840, 70), (0.22, 'latch', 0.8)],
    'mult': [(0, 'ratchet', 2, 0.07, 0.8), (0.1, 'servo', 0.26, 350, 1100, 80), (0.3, 'latch', 0.6)],
    'slow': [(0, 'servo', 0.6, 520, 55, 70, 0.7), (0.0, 'hiss', 0.45, 0.3), (0.55, 'clank', 0.5, 240)],
    'shield_item': [(0, 'servo', 0.45, 120, 520, 60, 1.5), (0.18, 'latch', 0.9), (0.34, 'latch', 0.9), (0.4, 'hiss', 0.45, 0.45)],
    'switch': [(0, 'latch', 0.8), (0.01, 'servo', 0.09, 480, 900, 90)],
    'count_tick': [(0, 'latch', 0.6)],
}
_name = [None]


def mech_layer(name, scale, total):
    buf = np.zeros(int(total * SR) + 1)
    for ev in MECH.get(name, []):
        t0, el, *a = ev
        if el == 'latch':
            x = latch(a[0])
        elif el == 'clank':
            x = clank(a[0], a[1])
        elif el == 'hiss':
            x = hiss(a[0], a[1])
        elif el == 'ratchet':
            x = ratchet(a[0], a[1], a[2])
        else:
            x = servo(a[0], a[1], a[2], a[3], a[4] if len(a) > 4 else 1.0) * 0.5
        place(buf, x, t0, scale)
    return buf


def place(buf, x, start, gain=1.0):
    i = int(start * SR)
    end = min(len(buf), i + len(x))
    if i < len(buf) and end > i:
        buf[i:end] += x[:end - i] * gain


def reverb(x, tau, wet, seed=0):
    """Stereo reverb: independent exponentially-decaying noise tails for left and right."""
    r = np.random.RandomState(seed)
    n = int(tau * 3 * SR)
    t = np.arange(n) / SR
    out = []
    for ch in (0, 1):
        ir = lowpass(r.randn(n), 5000) * np.exp(-t / tau)
        ir[: int(0.004 * SR)] *= np.linspace(0, 1, int(0.004 * SR))
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        out.append(fftconvolve(x, ir)[: len(x) + n // 2])
    L = np.pad(x, (0, len(out[0]) - len(x)))
    return np.stack([L * (1 - wet) + out[0] * wet * 2.2, L * (1 - wet) + out[1] * wet * 2.2])


def pingpong(x, delay, fb, wet):
    n = len(x) + int(delay * SR * 5)
    L = np.zeros(n)
    R = np.zeros(n)
    L[: len(x)] += x
    R[: len(x)] += x
    d = int(delay * SR)
    echo = x.copy()
    for k in range(1, 5):
        echo = echo * fb
        tgt = L if k % 2 else R
        tgt[k * d: k * d + len(x)] += echo * wet
    return np.stack([L, R])


def finish(stereo, peak=0.7, fade=0.01):
    s = stereo
    m = np.max(np.abs(s)) + 1e-9
    s = s * (peak / m)
    f = int(fade * SR)
    # trim trailing silence
    amp = np.max(np.abs(s), axis=0)
    idx = np.where(amp > peak * 0.02)[0]
    s = s[:, : (idx[-1] + 1 if len(idx) else s.shape[1])]
    f = min(f, s.shape[1] // 2)
    s[:, -f:] *= np.linspace(1, 0, f)
    return s


def mix_family(kind, events, noise=(), total=1.0, room=1.0):
    """events: (start, dur, f0, f1, gain[, tau, curve]); noise: (start, whoosh-args..., gain) -> stereo buffer with the family's space."""
    buf = np.zeros(int(total * SR) + 1)
    mech = kind in ('D', 'E')
    tkind = 'B' if mech else kind
    for ev in events:
        s, d, f0, f1, g = ev[:5]
        tau = ev[5] if len(ev) > 5 else None
        cv = ev[6] if len(ev) > 6 else 1.0
        place(buf, voice(tkind, f0, f1, d, tau, cv), s, g * (0.8 if mech else 1.0))
    for nz in noise:
        s, d, a, b, bw, g = nz
        place(buf, whoosh(d, a, b, bw), s, g * (0.7 if kind == 'C' else 1.0))
    if kind == 'A':
        st = reverb(buf, 0.55 * room, 0.38 * min(1.0, room + 0.25), 3)
    elif kind in ('B', 'D', 'E'):
        if mech:
            buf = buf + mech_layer(_name[0], 0.9 if kind == 'D' else 1.5, total)
        st = pingpong(buf, 0.14 * max(room, 0.35), 0.45, (0.55 if kind == 'B' else 0.3) * min(1.0, room + 0.2))
    else:
        st = np.stack([buf, buf * 0.98])
    return st


# ---- the sounds: (name, when it plays, builder(kind) -> stereo) --------------------------------------------------

def S(name, desc):
    def reg(fn):
        SOUNDS.append((name, desc, fn))
        return fn
    return reg


SOUNDS = []


@S('menu_tap', 'Small menu buttons (settings, back, items)')
def _(k):
    return mix_family(k, [(0, 0.09, A5, A5 * 1.06, 1.0, 0.05)], total=0.5, room=0.2)


@S('menu_big', 'Big buttons: home, retry, pause screen')
def _(k):
    return mix_family(k, [(0, 0.12, E5, E5, 0.8, 0.08), (0.07, 0.16, A5, A5, 1.0, 0.12)], total=0.6, room=0.3)


@S('cant', "Can't afford it / locked")
def _(k):
    return mix_family(k, [(0, 0.12, E4, E4 * 0.94, 1.0, 0.09), (0.11, 0.18, C4, C4 * 0.9, 1.0, 0.14)], total=0.6, room=0.3)


@S('bought', 'Buying a ship, galaxy or upgrade')
def _(k):
    ev = [(i * 0.065, 0.3, f, f, 0.8 + 0.07 * i, 0.22) for i, f in enumerate([A5, C6, E6, A6])]
    return mix_family(k, ev, total=1.2, room=0.55)


@S('launch', 'The START press: the game launches')
def _(k):
    ev = [(0, 0.5, A3, A4, 0.6, 0.5, 2.0), (0.38, 0.5, A4, A4, 0.8, 0.35), (0.38, 0.5, E5, E5, 0.5, 0.35), (0.38, 0.5, A5, A5, 0.4, 0.35)]
    return mix_family(k, ev, noise=[(0, 0.45, 400, 3200, 260, 0.35)], total=1.4, room=0.7)


@S('pre', 'Short cue just before the run begins')
def _(k):
    return mix_family(k, [(0, 0.35, D5, A5, 0.7, 0.22, 1.6)], noise=[(0, 0.3, 600, 2500, 200, 0.18)], total=0.9, room=0.55)


@S('absorb', 'Catching an orb with the matching shield')
def _(k):
    return mix_family(k, [(0, 0.11, E5, A5, 1.0, 0.07), (0.05, 0.2, A5, A6 * 0.5 * 2 / 2, 0.8, 0.13)], total=0.8, room=0.5)


@S('wrong', 'Hitting an orb with the wrong shield: energy lost')
def _(k):
    ev = [(0, 0.28, A3, A3 * 0.5, 1.0, 0.18), (0, 0.2, A3 * 1.01, A3 * 0.55, 0.7, 0.14)]
    st = mix_family(k, ev, noise=[(0, 0.18, 900, 160, 400, 0.6)], total=0.8, room=0.5)
    return st


@S('shield_hit', 'A hit absorbed by a shield item')
def _(k):
    ev = [(0, 0.3, D5, D5 * 0.99, 0.9, 0.18), (0, 0.3, D5 * 1.5, D5 * 1.49, 0.6, 0.12), (0, 0.3, D5 * 2.7, D5 * 2.7, 0.35, 0.07)]
    return mix_family(k, ev, noise=[(0, 0.06, 2000, 2500, 900, 0.4)], total=0.9, room=0.45)


@S('life', 'Catching a gold orb: energy restored')
def _(k):
    ev = [(0, 0.3, A4, A4, 0.8, 0.2), (0.08, 0.3, C5, C5, 0.8, 0.2), (0.16, 0.5, E5, E5, 0.9, 0.3), (0.16, 0.5, A5, A5, 0.4, 0.3)]
    return mix_family(k, ev, total=1.2, room=0.55)


@S('dead', 'Game over: the shield and ship give out')
def _(k):
    ev = [(0, 1.1, A4, A2, 0.9, 0.7, 1.6), (0, 1.1, A4 * 1.5, A2 * 1.5, 0.45, 0.5, 1.6), (0.02, 0.6, A2, 45.0, 0.9, 0.5)]
    return mix_family(k, ev, noise=[(0, 0.7, 2600, 120, 600, 0.55)], total=1.9)


@S('item_pickup', 'Picking up an item')
def _(k):
    ev = [(0, 0.25, E5, E5, 0.8, 0.13), (0.06, 0.25, A5, A5, 0.85, 0.15), (0.12, 0.45, D6, D6, 0.95, 0.26)]
    return mix_family(k, ev, total=1.0, room=0.45)


@S('item_tick', 'Item about to run out: warning tick')
def _(k):
    st = mix_family(k, [(0, 0.05, A5 * 1.5, A5 * 1.5, 1.0, 0.03)], total=0.3, room=0.15)
    return st


@S('swap', 'Color swap item activates (and its reverse when it ends)')
def _(k):
    ev = [(0, 0.16, A5, E5 * 0.75, 0.8, 0.1, 1.0), (0.1, 0.22, E5, A5 * 1.5, 0.95, 0.16)]
    return mix_family(k, ev, noise=[(0, 0.26, 800, 2800, 300, 0.25)], total=0.8, room=0.45)


@S('mult', 'x2 / x3 item activates (and its reverse when it ends)')
def _(k):
    ev = [(0, 0.2, A5, A5, 0.85, 0.12), (0.09, 0.4, A6, A6, 0.95, 0.24), (0.09, 0.4, E6, E6, 0.3, 0.2)]
    return mix_family(k, ev, total=0.9, room=0.45)


@S('slow', 'Slow item: time stretches (and its reverse when it ends)')
def _(k):
    ev = [(0, 0.6, A5, A3, 0.8, 0.45, 0.7), (0, 0.6, E5, E3, 0.4, 0.4, 0.7)]
    return mix_family(k, ev, noise=[(0, 0.55, 3000, 220, 500, 0.35)], total=1.3, room=0.7)


@S('shield_item', 'Shield item raised (and its reverse when it ends)')
def _(k):
    ev = [(0.0, 0.7, A4, A4 * 1.5, 0.55, 0.5, 0.6), (0.25, 0.5, E5, E5, 0.7, 0.4), (0.3, 0.55, A5, A5, 0.7, 0.45), (0.38, 0.6, C6, C6, 0.5, 0.45)]
    return mix_family(k, ev, noise=[(0, 0.5, 500, 4200, 350, 0.3)], total=1.5, room=0.7)


@S('switch', 'Switching shield color (blue <-> red)')
def _(k):
    return mix_family(k, [(0, 0.1, D5, G5, 0.9, 0.06)], noise=[(0, 0.1, 1500, 3500, 600, 0.2)], total=0.5, room=0.3)


@S('count_tick', 'Counting up your orbs on the game-over screen')
def _(k):
    return mix_family(k, [(0, 0.06, A5, A5 * 1.02, 0.9, 0.035)], total=0.4, room=0.15)


REVERSED = {'swap': 'swap_end', 'mult': 'mult_end', 'slow': 'slow_end', 'shield_item': 'shield_item_end'}


def render_all(outdir):
    import wave
    os.makedirs(outdir, exist_ok=True)
    names = []
    for name, desc, fn in SOUNDS:
        for kind in 'ABCDE':
            _name[0] = name
            st = finish(fn(kind))
            outs = [(name, st)]
            if name in REVERSED:
                outs.append((REVERSED[name], finish(st[:, ::-1].copy(), fade=0.004)))
            for nm, s in outs:
                path = os.path.join(outdir, '%s_%s.wav' % (nm, kind))
                data = (np.clip(s.T, -1, 1) * 32767).astype('<i2')
                with wave.open(path, 'wb') as w:
                    w.setnchannels(2)
                    w.setsampwidth(2)
                    w.setframerate(SR)
                    w.writeframes(data.tobytes())
        names.append(name)
        if name in REVERSED:
            names.append(REVERSED[name])
    return names


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else '/tmp/cto_sfx'
    print(len(render_all(out)), 'sounds x 3 families ->', out)
