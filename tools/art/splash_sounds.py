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

# --- storm bed (appended after the neon sounds so their random draws stay as they were)
def shaped(n, lo, hi, tilt):
    """noise with a band-limited 1/f**tilt spectrum, built in the frequency domain so it is periodic (loops seamlessly)"""
    freqs = np.fft.rfftfreq(n, 1 / SR)
    amp = np.where((freqs > lo) & (freqs < hi), 1 / np.maximum(freqs, 1) ** tilt, 0)
    spec = amp * np.exp(2j * np.pi * r.random(len(freqs)))
    return np.fft.irfft(spec, n)

# rain: 6 s loop, a soft wash of mid/high noise with a slow swell, plus sparse droplet ticks on top
n = 6 * SR; t = np.arange(n) / SR
wash = shaped(n, 400, 9000, 0.35); wash /= np.abs(wash).max()
wash *= 0.85 + 0.15 * np.sin(2 * np.pi * t / 6 * 2 + 1)
drops = np.zeros(n)
for i in np.nonzero(r.random(n) < 260 / SR)[0]:
    m = min(n - i, int(0.006 * SR)); k = np.arange(m)
    drops[i:i + m] += r.uniform(.2, 1) * np.sin(2 * np.pi * r.uniform(1800, 4200) * k / SR) * np.exp(-k / (0.0012 * SR))
drops = np.roll(drops, 0)
rain = wash + 0.35 * drops / np.abs(drops).max()
save('storm_rain', rain, 0.5)

# thunder: a distant crack, then rolls of low rumble that swell and fade; two variants
# (the game ships the two thunders as .ogg: after regenerating them here, convert with
#  ffmpeg -i X.wav -af "pan=stereo|c0=c0|c1=c0" -c:a vorbis -strict -2 -b:a 128k X.ogg  and delete the .wav)
for name, dur, rolls in (('storm_thunder1', 4.2, (0.15, 0.9, 1.9)), ('storm_thunder2', 5.0, (0.2, 1.4, 2.6, 3.4))):
    n = int(dur * SR); t = np.arange(n) / SR
    rumble = shaped(n, 40, 420, 0.6); rumble /= np.abs(rumble).max()
    e = np.zeros(n)
    for k, at in enumerate(rolls):
        e += (0.9 ** k) * np.clip((t - at) / 0.35, 0, 1) * np.exp(-np.maximum(t - at, 0) / (0.9 + 0.2 * k)) * (t >= at)
    crack = lowpass(shaped(n, 300, 3000, 0.2), 4) * np.exp(-np.maximum(t - rolls[0] + 0.05, 0) / 0.12) * (t >= rolls[0] - 0.05)
    th = rumble * e + 0.35 * crack / (np.abs(crack).max() + 1e-9)
    th[:int(0.02 * SR)] *= np.linspace(0, 1, int(0.02 * SR)); th[-int(0.3 * SR):] *= np.linspace(1, 0, int(0.3 * SR))
    save(name, th, 0.9)
