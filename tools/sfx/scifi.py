"""Original sci-fi sound effects in the spirit of classic space-opera (set A) and starship-bridge (set B) sounds.

These are synthesised from scratch (oscillators and envelopes): blaster zaps, droid chirps, deflector shimmer, engine whines,
console beeps, transporter-like twinkles, warp-style hums. They are NOT recordings or copies of any film or TV sound.

Run from the repo root:  python3 tools/sfx/scifi.py   -> ~/Downloads/CollectTheOrbs-sound-options.html
"""
import os, re, sys
import numpy as np
from scipy.signal import butter, sosfilt

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import treat

SR = 44100


def t_(d):
    return np.arange(int(d * SR)) / SR


def glide(f0, f1, n, curve=1.0):
    return f0 * (f1 / f0) ** (np.linspace(0, 1, n) ** curve)


def ph(f):
    return 2 * np.pi * np.cumsum(f) / SR


def lowpass(x, fc):
    return sosfilt(butter(2, min(fc, SR / 2 - 200), 'low', fs=SR, output='sos'), x)


def saw(f, k=14):
    p = ph(f)
    fm = float(np.max(f))
    return sum(np.sin(h * p) / h for h in range(1, k + 1) if h * fm < SR / 2 - 600)


def square(f, k=13):
    p = ph(f)
    fm = float(np.max(f))
    return sum(np.sin(h * p) / h for h in range(1, k + 1, 2) if h * fm < SR / 2 - 600)


def sine(f):
    return np.sin(ph(f))


def env(n, a=0.008, tau=0.15, rel=0.02):
    t = np.arange(n) / SR
    e = np.minimum(t / a, 1.0) * np.exp(-t / tau)
    r = min(int(rel * SR), n // 2)
    e[-r:] *= np.linspace(1, 0, r)
    return e


def swell(n, a, r):
    t = np.arange(n) / SR
    return np.clip(t / a, 0, 1) ** 1.3 * np.clip((n / SR - t) / r, 0, 1) ** 1.3


def mix(total, parts):
    buf = np.zeros(int(total * SR) + 1)
    for start, x, g in parts:
        i = int(start * SR)
        e = min(len(buf), i + len(x))
        if i < len(buf):
            buf[i:e] += x[: e - i] * g
    return buf


def pew(f0, f1, dur, curve=0.3, tau=None):
    """A blaster-style zap: a fast falling saw pair with a metallic ring on top."""
    n = int(dur * SR)
    f = glide(f0, f1, n, curve)
    x = saw(f) + 0.7 * saw(f * 1.012) + 0.35 * sine(f * 2.02)
    return lowpass(x, 7000) * env(n, 0.004, tau or dur * 0.45)


def chirp(f0, f1, dur, trill=0.0, tau=None, wave='sine'):
    n = int(dur * SR)
    f = glide(f0, f1, n, 0.8) * (1 + trill * np.sin(2 * np.pi * 32 * t_(dur)[:n]))
    x = sine(f) if wave == 'sine' else 0.5 * saw(f, 8) + 0.5 * sine(f)
    return x * env(n, 0.006, tau or dur * 0.5)


def beep(f, dur, tau=None, bright=0.25):
    n = int(dur * SR)
    return (sine(np.full(n, f)) + bright * sine(np.full(n, f * 2))) * env(n, 0.006, tau or dur * 0.5)


def chime(f, dur, tau=0.2):
    n = int(dur * SR)
    return (sine(np.full(n, f)) + 0.4 * sine(np.full(n, f * 2.0)) + 0.15 * sine(np.full(n, f * 3.01))) * env(n, 0.005, tau)


def hum(f0, dur, f1=None, harm=(1.0, 0.7, 0.5, 0.35, 0.25), wob=0.0, wobhz=3.0):
    n = int(dur * SR)
    f = glide(f0, f1 or f0, n)
    if wob:
        f = f * (1 + wob * np.sin(2 * np.pi * wobhz * t_(dur)[:n]))
    p = ph(f)
    return sum(a * np.sin((k + 1) * p) for k, a in enumerate(harm))


def shimmer(freqs, dur, tremolo=11.0, depth=0.5, tau=None):
    """Stacked sines each with its own fast tremolo, like a twinkling transporter-style swirl."""
    n = int(dur * SR)
    t = t_(dur)[:n]
    out = np.zeros(n)
    for i, f in enumerate(freqs):
        out += np.sin(2 * np.pi * f * t) * (1 - depth + depth * np.sin(2 * np.pi * (tremolo + i * 1.3) * t + i))
    return out * swell(n, dur * 0.25, dur * 0.35)


def boom(dur, f0=130.0, f1=32.0, tau=0.25):
    n = int(dur * SR)
    return sine(glide(f0, f1, n, 0.6)) * env(n, 0.004, tau)


def crash(dur, f0, f1):
    """A dense falling cluster of detuned saws: a burst of debris without any noise source."""
    n = int(dur * SR)
    f = glide(f0, f1, n, 0.7)
    x = sum(saw(f * (1 + 0.011 * k), 10) * (1 - 0.1 * abs(k)) for k in range(-3, 4))
    return lowpass(x, 3500) * env(n, 0.004, dur * 0.35)


# ---- set A: space-opera: blasters, droids, ion engines ---------------------------------------------------------------

def A(name):
    d = {}
    d['menu_tap'] = (0.2, [(0, chirp(1500, 2100, 0.07, 0.01), 0.9), (0.07, chirp(2100, 2800, 0.08, 0.01), 0.8)])
    d['menu_big'] = (0.3, [(0, boom(0.14, 160, 70, 0.07), 0.9), (0.05, chirp(900, 1700, 0.12, 0.02, wave='saw'), 0.7)])
    d['cant'] = (0.5, [(0, pew(330, 200, 0.22, 0.6, 0.18), 0.8), (0.2, pew(250, 120, 0.26, 0.6, 0.2), 0.8)])
    d['bought'] = (0.6, [(0, chirp(1200, 2400, 0.12, 0.04), 0.7), (0.1, chirp(1600, 3200, 0.14, 0.05), 0.7), (0.24, chime(2400, 0.4, 0.22), 0.6)])
    d['launch'] = (1.4, [(0, hum(70, 1.2, 420, harm=(1, 0.8, 0.6, 0.5, 0.4, 0.3)) * swell(int(1.2 * SR), 0.7, 0.3), 0.6),
                         (0.1, hum(400, 1.1, 2200, harm=(1, 0.4, 0.2)) * swell(int(1.1 * SR), 0.7, 0.3), 0.4), (0, boom(0.5, 90, 36, 0.2), 0.8)])
    d['pre'] = (0.6, [(0, hum(220, 0.45, 900, harm=(1, 0.5, 0.3)) * swell(int(0.45 * SR), 0.35, 0.08), 0.6), (0.38, chirp(900, 1800, 0.1, 0.02), 0.5)])
    d['absorb'] = (0.45, [(0, chirp(300, 1700, 0.22, 0.015, 0.2, 'saw'), 0.7), (0.12, chime(1700, 0.3, 0.14), 0.5), (0.2, boom(0.2, 160, 70, 0.07), 0.45)])
    d['wrong'] = (0.5, [(0, pew(2200, 140, 0.3, 0.28), 0.9), (0, boom(0.25, 140, 50, 0.1), 0.6)])
    d['shield_hit'] = (0.5, [(0, pew(3200, 900, 0.14, 0.4, 0.08), 0.7), (0.03, chime(2600, 0.35, 0.12), 0.6), (0.03, chime(3700, 0.3, 0.09), 0.3)])
    d['life'] = (0.5, [(0, chirp(700, 3000, 0.16, 0.02, 0.12), 0.7), (0.12, chime(2000, 0.35, 0.16), 0.5), (0.16, chime(3000, 0.3, 0.12), 0.35)])
    d['dead'] = (1.0, [(0, boom(0.8, 110, 28, 0.3), 1.0), (0, crash(0.7, 1600, 90), 0.8), (0.1, pew(1500, 70, 0.5, 0.4, 0.2), 0.5)])
    d['item_pickup'] = (0.3, [(0, chirp(1800, 2600, 0.05, 0.02), 0.8), (0.055, chirp(2600, 1500, 0.07, 0.03), 0.8), (0.12, chirp(1500, 2900, 0.08, 0.03), 0.7)])
    d['item_tick'] = (0.2, [(0, beep(1400, 0.07), 0.8), (0.08, beep(1050, 0.08), 0.7)])
    d['swap'] = (0.9, [(0, hum(110, 0.8, 130, harm=(1, 0.9, 0.7, 0.5, 0.35, 0.2), wob=0.06, wobhz=2.6) * env(int(0.8 * SR), 0.03, 0.55), 0.6),
                       (0, hum(112.5, 0.8, 133, harm=(1, 0.8, 0.6, 0.4, 0.25, 0.15)) * env(int(0.8 * SR), 0.03, 0.55), 0.5)])
    d['mult'] = (0.5, [(0, hum(500, 0.2, 2600, harm=(1, 0.4, 0.15)) * swell(int(0.2 * SR), 0.17, 0.01), 0.55), (0.2, chime(2600, 0.3, 0.1), 0.6), (0.2, boom(0.14, 200, 90, 0.05), 0.4)])
    d['slow'] = (1.1, [(0, hum(1100, 1.0, 140, harm=(1, 0.5, 0.25, 0.12), wob=0.01) * env(int(1.0 * SR), 0.02, 0.55), 0.6), (0, boom(0.7, 90, 36, 0.3), 0.4)])
    d['shield_item'] = (0.7, [(0, hum(105, 0.6, 130, harm=(1, 0.9, 0.8, 0.7, 0.6, 0.5, 0.4), wob=0.015, wobhz=5) * swell(int(0.6 * SR), 0.18, 0.2), 0.6),
                              (0.04, hum(420, 0.55, 2400, harm=(1, 0.3)) * swell(int(0.55 * SR), 0.3, 0.2), 0.3)])
    d['switch'] = (0.3, [(0, pew(1800, 700, 0.07, 0.5, 0.05), 0.6), (0.08, chirp(900, 1500, 0.1, 0.01), 0.6)])
    return d[name]


# ---- set B: starship bridge: console beeps, deflector shimmer, warp hum ----------------------------------------------

def B(name):
    d = {}
    d['menu_tap'] = (0.2, [(0, beep(1850, 0.06, 0.04), 0.9), (0.065, beep(2450, 0.08, 0.06), 0.8)])
    d['menu_big'] = (0.3, [(0, beep(1250, 0.07, 0.05), 0.8), (0.07, beep(1650, 0.07, 0.05), 0.8), (0.14, beep(2200, 0.1, 0.07), 0.9)])
    d['cant'] = (0.5, [(0, beep(310, 0.17, 0.12, 0.6), 0.9), (0.21, beep(310, 0.2, 0.14, 0.6), 0.9)])
    d['bought'] = (0.8, [(i * 0.075, chime(f, 0.5, 0.22), 0.75) for i, f in enumerate([880, 1320, 1760, 2640])])
    d['launch'] = (1.5, [(0, hum(46, 1.3, 220, harm=(1, 0.6, 0.45, 0.3)) * swell(int(1.3 * SR), 0.9, 0.35), 0.7), (0.15, hum(230, 1.2, 1100, harm=(1, 0.5, 0.25), wob=0.012, wobhz=6) * swell(int(1.2 * SR), 0.85, 0.35), 0.4),
                         (0.7, shimmer([1100, 1650, 2200], 0.7, 9, 0.5), 0.2)])
    d['pre'] = (0.6, [(0, hum(60, 0.5, 190, harm=(1, 0.7, 0.5, 0.3)) * swell(int(0.5 * SR), 0.4, 0.08), 0.7), (0.3, beep(1400, 0.15, 0.1), 0.4)])
    d['absorb'] = (0.5, [(0, shimmer([440, 660, 880, 1320], 0.42, 14, 0.6), 0.6), (0, hum(180, 0.4, 340, harm=(1, 0.5, 0.2)) * swell(int(0.4 * SR), 0.12, 0.2), 0.4)])
    d['wrong'] = (0.5, [(0, hum(900, 0.32, 150, harm=(1, 0.8, 0.6, 0.5, 0.4), wob=0.08, wobhz=22) * env(int(0.32 * SR), 0.004, 0.14), 0.8), (0, boom(0.25, 120, 45, 0.1), 0.5)])
    d['shield_hit'] = (0.5, [(0, boom(0.2, 220, 80, 0.07), 0.7), (0.0, shimmer([1500, 2250, 3000, 4500], 0.35, 18, 0.7), 0.5), (0.02, chime(3000, 0.3, 0.1), 0.3)])
    d['life'] = (0.7, [(0, shimmer([523, 784, 1046, 1568, 2093], 0.6, 10, 0.5), 0.6), (0.2, chime(2093, 0.4, 0.2), 0.4)])
    d['dead'] = (1.1, [(0, hum(200, 0.5, 1500, harm=(1, 0.4, 0.2)) * swell(int(0.5 * SR), 0.45, 0.03), 0.5), (0.5, boom(0.6, 120, 28, 0.3), 1.0), (0.5, crash(0.6, 1500, 80), 0.7)])
    d['item_pickup'] = (0.3, [(0, chirp(2000, 3000, 0.1, 0.05), 0.8), (0.11, chirp(2600, 3400, 0.11, 0.05), 0.7)])
    d['item_tick'] = (0.25, [(0, chime(1900, 0.18, 0.07), 0.8)])
    d['swap'] = (0.9, [(0, hum(150, 0.8, 150, harm=(1, 0.7, 0.4, 0.25), wob=0.05, wobhz=7) * env(int(0.8 * SR), 0.05, 0.5), 0.6), (0, shimmer([600, 900], 0.8, 7, 0.6), 0.25)])
    d['mult'] = (0.5, [(0, shimmer([800, 1200, 1600], 0.3, 16, 0.5) * 1.0, 0.5), (0.25, chime(2400, 0.3, 0.1), 0.6)])
    d['slow'] = (1.1, [(0, hum(240, 1.0, 50, harm=(1, 0.6, 0.45, 0.3), wob=0.01) * env(int(1.0 * SR), 0.03, 0.6), 0.7), (0, shimmer([900, 1350], 0.9, 6, 0.5), 0.15)])
    d['shield_item'] = (0.8, [(0, shimmer([330, 495, 660, 990, 1320], 0.7, 9, 0.55), 0.55), (0, hum(80, 0.7, 160, harm=(1, 0.6, 0.4)) * swell(int(0.7 * SR), 0.3, 0.25), 0.4)])
    d['switch'] = (0.3, [(0, beep(1500, 0.06, 0.04), 0.8), (0.07, beep(1100, 0.08, 0.06), 0.8)])
    return d[name]


def render(fn, name):
    total, parts = fn(name)
    y = mix(total, parts)
    n = len(y)
    d = int(0.0009 * SR)
    st = np.stack([y, np.pad(y, (d, 0))[:n] * 0.97])
    st = treat.reverb(st, 0.07, 0.10)
    return treat.trim(st, 0.03)


def main():
    rows = []
    for name, file, desc, end in treat.SOUNDS:
        ref = treat.trim(treat.load(file))
        opts = {'Current': {'start': treat.mp3(treat.load(file))}}
        refend = treat.trim(treat.load(end)) if end else None
        if end:
            opts['Current']['end'] = treat.mp3(treat.load(end))
        for label, fn in (('A Space opera', A), ('B Starship', B)):
            st = treat.match_level(render(fn, name), ref)
            clip = {'start': treat.mp3(st)}
            if end:
                clip['end'] = treat.mp3(treat.match_level(st[:, ::-1].copy(), refend))
            opts[label] = clip
        rows.append({'name': name, 'desc': desc, 'opts': opts})
    tpl = treat.TEMPLATE
    tpl = tpl.replace("const COL={'Current':'var(--cur)','Polished':'var(--a)','Neon':'var(--b)','Deeper':'var(--c)','Crisp':'var(--d)'};",
                      "const COL={'Current':'var(--cur)','A Space opera':'var(--a)','B Starship':'var(--b)'};")
    tpl = re.sub(r'<p class="lead">.*?</p>', '<p class="lead">Original sci-fi sounds in two moods, synthesised from scratch (not recordings of anything): <b style="color:var(--a)">A Space opera</b> has blaster zaps, droid chirps, ion-engine whines and a swinging saber-like hum; <b style="color:var(--b)">B Starship</b> has bridge-console beeps, a shimmering deflector, a transporter-style twinkle and a low warp hum. Tap ▶ to hear one, then tap a card to pick it. Volumes are matched to the current sounds. Items with a start and an end sound have two buttons.</p>', tpl, flags=re.S)
    import json
    path = os.path.expanduser('~/Downloads/CollectTheOrbs-sound-options.html')
    open(path, 'w').write(tpl.replace('__DATA__', json.dumps(rows)))
    print(path)


if __name__ == '__main__':
    main()
