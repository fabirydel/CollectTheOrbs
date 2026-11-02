"""Neon sign sounds for the splash: a looping mains hum, a crackle for glitches, a power-up strike and a power dip."""
import numpy as np, os, wave
SR = 44100
OUT = os.path.join(os.path.dirname(__file__), '..', '..', 'android', 'assets', 'audio')
r = np.random.default_rng(5)

def lowpass(x, k):
    return np.convolve(x, np.ones(k) / k, mode='same')

def save(name, x, peak=0.8):
    # plain 16-bit wav: tiny files, and the hum loops without the padding gap an ogg encoder leaves
    x = x / (np.abs(x).max() + 1e-9) * peak
    with wave.open(os.path.join(OUT, name + '.wav'), 'wb') as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes((x * 32767).astype(np.int16).tobytes())

def env(n, a, d):
    t = np.arange(n) / SR
    return np.minimum(t / a, 1) * np.exp(-t / d)

# hum: 2 s loop, 100 Hz (mains x2) and harmonics, a band of buzz noise gated at the mains rate; whole cycles so it loops cleanly
n = 2 * SR; t = np.arange(n) / SR
hum = sum(a * np.sin(2 * np.pi * 100 * k * t + k) for k, a in ((1, 1.0), (2, 0.5), (3, 0.6), (4, 0.25), (6, 0.15)))
buzz = r.normal(0, 1, n); buzz = buzz - lowpass(buzz, 12)
buzz = lowpass(buzz, 3) * (0.5 + 0.5 * np.abs(np.sin(2 * np.pi * 100 * t / 2)))
slow = lowpass(r.normal(0, 1, n), 4000)
hum = hum / np.abs(hum).max() + 0.25 * buzz / np.abs(buzz).max() + 0.4 * slow / np.abs(slow).max() * np.sin(2 * np.pi * 0.5 * t) ** 2
f = int(0.01 * SR); hum[:f] *= np.linspace(0, 1, f); hum[-f:] *= np.linspace(1, 0, f)   # click-free edges
save('neon_hum', hum, 0.5)

# crackle: a few sharp ticks with a short buzz tail, like a tube misfiring
n = int(0.22 * SR); z = np.zeros(n)
for at in (0, 0.035, 0.08, 0.12):
    i = int(at * SR); m = min(n - i, int(0.03 * SR))
    z[i:i + m] += r.normal(0, 1, m) * np.exp(-np.arange(m) / (0.004 * SR)) * r.uniform(0.5, 1)
tt = np.arange(n) / SR
z += 0.4 * np.sin(2 * np.pi * 120 * tt) * env(n, 0.002, 0.05) + 0.3 * np.sin(2 * np.pi * 360 * tt) * env(n, 0.002, 0.04)
save('neon_zap', z, 0.7)

# strike: relay-like tick, then a buzz that rises and settles into the hum
n = int(0.7 * SR); tt = np.arange(n) / SR
ping = r.normal(0, 1, n) * np.exp(-tt / 0.006)
sweep = np.sin(2 * np.pi * (60 * tt + 80 * (1 - np.exp(-tt * 8)) * tt)) * (1 - np.exp(-tt * 30)) * np.exp(-tt / 0.35)
st = ping * 0.8 + 0.7 * sweep + 0.25 * lowpass(r.normal(0, 1, n), 3) * env(n, 0.01, 0.25)
save('neon_strike', st, 0.8)

# dip: the tube sags (descending buzz) with a soft thud
n = int(0.5 * SR); tt = np.arange(n) / SR
d = np.sin(2 * np.pi * np.cumsum(140 * np.exp(-tt * 5) + 40) / SR) * env(n, 0.003, 0.18)
d += 0.5 * lowpass(r.normal(0, 1, n), 6) * env(n, 0.002, 0.08)
save('neon_dip', d, 0.7)
