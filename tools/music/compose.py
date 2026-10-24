"""Background music loops for Collect The Orbs: bass-heavy, atmospheric electronic tracks that loop seamlessly.

Each track is synthesised (sub bass, soft kick, hats, wide pads, sparse plucks) with sidechain pumping, then rendered three loops long
through the reverb/echo and the MIDDLE loop is kept, so the tails of the previous pass flow into the start: the end joins the beginning
with no gap, click or tail cut-off.

Run from the repo root:  python3 tools/music/compose.py [outdir]   -> outdir/<track>.wav  (default /tmp/cto_music)
"""
import os, sys, wave
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, fftconvolve

SR = 44100


def mtof(m):
    return 440.0 * 2 ** ((m - 69) / 12)


def lowpass(x, fc, order=2):
    return sosfilt(butter(order, min(fc, SR / 2 - 200), 'low', fs=SR, output='sos'), x)


def highpass(x, fc, order=2):
    return sosfilt(butter(order, fc, 'high', fs=SR, output='sos'), x)


def bandpass(x, lo, hi):
    return sosfilt(butter(2, [lo, min(hi, SR / 2 - 200)], 'band', fs=SR, output='sos'), x)


def env_ar(n, a, r, curve=1.0):
    e = np.ones(n)
    a = max(int(a * SR), 1)
    r = min(max(int(r * SR), 1), n)
    e[:a] = np.linspace(0, 1, a) ** curve
    e[-r:] *= np.linspace(1, 0, r) ** curve
    return e


class Track:
    def __init__(self, bpm, bars, loops=3):
        self.bpm, self.bars, self.loops = bpm, bars, loops
        self.beat = 60.0 / bpm
        self.loop_s = bars * 4 * self.beat
        self.L = int(round(self.loop_s * SR))
        n = self.L * loops + SR * 2
        self.stems = {k: np.zeros((2, n)) for k in ('bass', 'kick', 'hat', 'snare', 'pad', 'pluck', 'air')}
        self.kick_times = []

    def put(self, stem, x, t, pan=0.0, gain=1.0):
        i = int(round(t * SR))
        buf = self.stems[stem]
        e = min(buf.shape[1], i + len(x))
        if i < buf.shape[1] and e > i:
            l, r = gain * np.sqrt((1 - pan) / 2), gain * np.sqrt((1 + pan) / 2)
            buf[0, i:e] += x[: e - i] * l
            buf[1, i:e] += x[: e - i] * r

    def every_loop(self):
        for k in range(self.loops):
            yield k * self.L   # an exact whole number of samples, so every pass is sample-identical


# ---- instruments --------------------------------------------------------------------------------------------------

def bass_note(m, dur, vel=1.0, cutoff=260):
    n = int(dur * SR)
    f = mtof(m)
    t = np.arange(n) / SR
    f_t = f * (1 + 0.5 * np.exp(-t / 0.012))   # a tiny pitch drop at the onset gives the note some weight
    p = 2 * np.pi * np.cumsum(f_t) / SR
    sub = np.sin(p)
    body = sum(np.sin(h * p * d) / h for h in (1, 2, 3, 4, 5, 6) for d in (0.997, 1.003))
    x = sub * 0.9 + lowpass(body, cutoff) * 0.45 + lowpass(body, cutoff * 3.5) * 0.25 * np.exp(-t / 0.07)
    return np.tanh(x * 1.4) / 1.2 * vel * env_ar(n, 0.006, min(0.06, dur / 2))


def kick(vel=1.0, f0=130.0, f1=46.0):
    n = int(0.42 * SR)
    t = np.arange(n) / SR
    f = f1 + (f0 - f1) * np.exp(-t / 0.035)
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t / 0.16)
    return x * vel * env_ar(n, 0.002, 0.05)


def hat(vel=1.0, open_=False):
    n = int((0.16 if open_ else 0.05) * SR)
    t = np.arange(n) / SR
    x = highpass(np.random.randn(n), 7500) * np.exp(-t / (0.05 if open_ else 0.012))
    return x * vel * 0.35


def snare(vel=1.0):
    n = int(0.22 * SR)
    t = np.arange(n) / SR
    tone = np.sin(2 * np.pi * 185 * t * (1 + 0.1 * np.exp(-t / 0.02))) * np.exp(-t / 0.07)
    nz = bandpass(np.random.randn(n), 1200, 7000) * np.exp(-t / 0.075)
    return (0.5 * tone + 0.5 * nz) * vel * env_ar(n, 0.002, 0.04)


def pad_chord(midis, dur, cutoff=1100, lfo_hz=0.12, phase0=0.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    L = np.zeros(n)
    R = np.zeros(n)
    for j, m in enumerate(midis):
        for d, side in ((-0.012, 0), (0.0, 0), (0.012, 1), (0.007, 1), (-0.006, 0)):
            f = mtof(m) * (1 + d)
            p = 2 * np.pi * f * t + np.random.rand() * 6.28
            w = sum(np.sin(h * p) / h for h in range(1, 7) if h * f < 9000)
            (L if side == 0 else R)[:] += w
    move = 0.78 + 0.22 * np.sin(2 * np.pi * lfo_hz * t + phase0)   # slow swell for movement
    out = [lowpass(ch, cutoff * 0.85, 2) * move for ch in (L, R)]
    a = min(1.2, dur * 0.4)
    e = env_ar(n, a, min(1.4, dur * 0.45), 1.5)
    return np.stack(out) * e * 0.06


def pluck(m, dur=0.5, bright=1.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m)
    x = np.sin(2 * np.pi * f * t) + 0.5 * np.sin(2 * np.pi * 2 * f * t) * np.exp(-t / 0.12) + 0.25 * np.sin(2 * np.pi * 3.01 * f * t) * np.exp(-t / 0.06) * bright
    return x * np.exp(-t / 0.18) * env_ar(n, 0.004, 0.05)


def air(freqs, dur):
    n = int(dur * SR)
    t = np.arange(n) / SR
    x = sum(np.sin(2 * np.pi * f * t) * (0.6 + 0.4 * np.sin(2 * np.pi * (0.2 + 0.07 * i) * t + i)) for i, f in enumerate(freqs))
    return x * env_ar(n, dur * 0.4, dur * 0.4, 1.5) * 0.02


# ---- effects ------------------------------------------------------------------------------------------------------

def reverb_stereo(x, tau=1.8, wet=0.3, seed=1):
    r = np.random.RandomState(seed)
    n = int(tau * 3 * SR)
    t = np.arange(n) / SR
    out = []
    for ch in range(2):
        ir = lowpass(r.randn(n), 5000) * np.exp(-t / tau)
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        out.append(fftconvolve(x[ch], ir)[: x.shape[1]])
    return x * (1 - wet) + np.stack(out) * wet * 2.2


def echo(x, delay, fb, wet, taps=5):
    d = int(delay * SR)
    out = np.zeros_like(x)
    cur = x.copy()
    for k in range(1, taps + 1):
        cur = lowpass(cur, 3500) * fb
        shift = k * d
        tgt = 1 - (k % 2)   # ping-pong
        out[tgt, shift:] += cur[0, : x.shape[1] - shift] * 0.6 + cur[1, : x.shape[1] - shift] * 0.4
    return x + out * wet


def sidechain(kicks, n, depth=0.55, release=0.22):
    g = np.ones(n)
    shape = 1 - depth * np.exp(-np.arange(int(release * 4 * SR)) / SR / (release / 2.2))
    for t in kicks:
        i = int(round(t * SR))
        e = min(n, i + len(shape))
        if i < n:
            g[i:e] = np.minimum(g[i:e], shape[: e - i])
    return g


def master(x, loudness=0.2):
    x = highpass(x, 28, 2) if False else np.stack([highpass(c, 28) for c in x])
    # low shelf warmth for a bass-heavy feel
    w = 2 * np.pi * 90 / SR
    A = 10 ** (3.0 / 40)
    c, s = np.cos(w), np.sin(w)
    al = s / (2 * 0.707)
    b = np.array([A * ((A + 1) - (A - 1) * c + 2 * np.sqrt(A) * al), 2 * A * ((A - 1) - (A + 1) * c), A * ((A + 1) - (A - 1) * c - 2 * np.sqrt(A) * al)])
    a = np.array([(A + 1) + (A - 1) * c + 2 * np.sqrt(A) * al, -2 * ((A - 1) + (A + 1) * c), (A + 1) + (A - 1) * c - 2 * np.sqrt(A) * al])
    x = np.stack([lfilter(b / a[0], a / a[0], ch) for ch in x])
    x = np.tanh(x * 1.1)
    rms = np.sqrt(np.mean(x ** 2)) + 1e-9
    x = x * (loudness / rms)
    peak = np.max(np.abs(x))
    return x * (0.92 / peak) if peak > 0.92 else x


def finish_loop(t: Track, mixbus):
    """Keep the middle loop of the 3-loop render so the tails of the previous pass wrap into the start."""
    return mixbus[:, t.L: 2 * t.L]


# ---- tracks -------------------------------------------------------------------------------------------------------

def sequence(t, chords, bass_pat, bass_len, kick_steps, hat_pat, snare_steps, pluck_steps, root_oct=2, open_hat_steps=(), pluck_oct=5, seed=3):
    """chords: list (per 4 bars) of (root midi, triad offsets). Patterns are lists over 16 steps of a bar."""
    step = t.beat / 4
    for k0 in t.every_loop():
        def q(local, k0=k0):   # place on a whole sample that is the same in every pass
            return (k0 + int(round(local * SR))) / SR
        for bar in range(t.bars):
            ch = chords[(bar // 4) % len(chords)]
            root, third, fifth = ch
            t0 = q(bar * 4 * t.beat)
            if bar % 4 == 0:   # pad chord (fills the 4 bars) + airy top
                voicing = [root + 12, third + 12, fifth + 12, root + 24]
                np.random.seed(1000 + bar)   # same pad phases every pass, so the loop is exactly periodic
                p = pad_chord(voicing, 4 * t.beat + 1.2, 1200, 0.1 + 0.03 * (bar // 4), phase0=bar)
                t.put('pad', p[0], t0)
                t.stems['pad'][1, int(round(t0 * SR)): int(round(t0 * SR)) + p.shape[1]] += p[1][: t.stems['pad'].shape[1] - int(round(t0 * SR))]
                t.put('air', air([mtof(root + 36), mtof(fifth + 36)], 4 * t.beat + 0.5), t0, 0.0)
            for s in range(16):
                ts = q(bar * 4 * t.beat + s * step)
                if bass_pat[s]:
                    m = root + 12 * (root_oct - 2) + (0 if bass_pat[s] == 1 else 12 if bass_pat[s] == 2 else 7)
                    t.put('bass', bass_note(m, bass_len[s] * step, 0.9 if bass_pat[s] == 1 else 0.7), ts)
                if s in kick_steps:
                    t.put('kick', kick(1.0 if s % 4 == 0 else 0.8), ts)
                    t.kick_times.append(ts)
                np.random.seed(bar * 16 + s)
                rng = np.random.RandomState(seed * 7919 + bar * 16 + s)
                if hat_pat[s]:
                    t.put('hat', hat(hat_pat[s] * 0.8, s in open_hat_steps), ts + (0.004 * (s % 2) // (1 / SR)) / SR, 0.25 * (-1) ** s)
                if s in snare_steps:
                    t.put('snare', snare(0.8), ts)
                if s in pluck_steps and rng.rand() < 0.8:
                    note = [root, third, fifth, root + 12, third + 12, fifth + 12][rng.randint(0, 6)] + 12 * (pluck_oct - 3)
                    t.put('pluck', pluck(note, 0.5), ts, rng.uniform(-0.5, 0.5), 0.45)


def render_track(spec):
    np.random.seed(spec['seed'])
    t = Track(spec['bpm'], spec['bars'])
    sequence(t, **spec['seq'])
    n = t.stems['bass'].shape[1]
    g = sidechain(t.kick_times, n, spec.get('duck', 0.55), spec.get('release', 0.22))
    pump = np.stack([g, g])
    bus = (t.stems['bass'] * spec['lvl']['bass'] * (0.65 + 0.35 * pump) + t.stems['kick'] * spec['lvl']['kick'] +
           t.stems['hat'] * spec['lvl']['hat'] + t.stems['snare'] * spec['lvl']['snare'] +
           (t.stems['pad'] * spec['lvl']['pad'] + t.stems['air'] * 1.0) * pump + echo(t.stems['pluck'], t.beat * 0.75, 0.55, 0.7) * spec['lvl']['pluck'] * pump)
    bus = reverb_stereo(bus, spec.get('tau', 1.6), spec.get('wet', 0.22))
    bus = master(bus, spec.get('loud', 0.2))
    render_track.seam = float(np.max(np.abs(bus[:, t.L: 2 * t.L] - bus[:, 2 * t.L: 3 * t.L])))   # 0 = the loop repeats exactly
    return finish_loop(t, bus)


# F minor, A minor, D minor progressions (root, third, fifth as midi)
FM = [(41, 44, 48), (37, 41, 44), (44, 48, 51), (39, 43, 46)]    # Fm Db Ab Eb
AM = [(45, 48, 52), (41, 45, 48), (48, 52, 55), (43, 47, 50)]    # Am F C G
DM = [(38, 41, 45), (34, 38, 41), (41, 45, 48), (36, 40, 43)]    # Dm Bb F C

TRACKS = {
    'deep_sub': dict(seed=1, bpm=96, bars=16, duck=0.5, release=0.3, tau=2.0, wet=0.26, loud=0.19,
                     lvl=dict(bass=1.0, kick=0.8, hat=0.35, snare=0.0, pad=1.0, pluck=0.5),
                     seq=dict(chords=FM, bass_pat=[1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 3, 0], bass_len=[5, 1, 1, 1, 1, 1, 3, 1, 1, 1, 5, 1, 1, 1, 2, 1],
                              kick_steps=(0, 8), hat_pat=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1], snare_steps=(), pluck_steps=(6, 11, 14), open_hat_steps=(14,), seed=4)),
    'night_drive': dict(seed=2, bpm=112, bars=16, duck=0.6, release=0.22, tau=1.4, wet=0.2, loud=0.2,
                        lvl=dict(bass=1.0, kick=0.75, hat=0.5, snare=0.35, pad=0.9, pluck=0.6),
                        seq=dict(chords=AM, bass_pat=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 2, 0], bass_len=[1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 2, 1],
                                 kick_steps=(0, 4, 8, 12), hat_pat=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0], snare_steps=(4, 12), pluck_steps=(2, 5, 9, 10, 13, 15), open_hat_steps=(2, 6, 10, 14), seed=7)),
    'float': dict(seed=3, bpm=84, bars=16, duck=0.35, release=0.35, tau=2.6, wet=0.34, loud=0.17,
                  lvl=dict(bass=1.0, kick=0.45, hat=0.18, snare=0.0, pad=1.2, pluck=0.7),
                  seq=dict(chords=DM, bass_pat=[1, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0], bass_len=[8, 1, 1, 1, 1, 1, 1, 1, 7, 1, 1, 1, 1, 1, 1, 1],
                           kick_steps=(0,), hat_pat=[0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0], snare_steps=(), pluck_steps=(3, 6, 10, 13), open_hat_steps=(), seed=11)),
}


def write_wav(path, x):
    with wave.open(path, 'wb') as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((np.clip(x.T, -1, 1) * 32767).astype('<i2').tobytes())


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else '/tmp/cto_music'
    os.makedirs(out, exist_ok=True)
    for name, spec in TRACKS.items():
        x = render_track(spec)
        write_wav(os.path.join(out, name + '.wav'), x)
        print(name, round(x.shape[1] / SR, 1), 's  loop-repeat error', round(render_track.seam, 6), ' peak', round(float(np.max(np.abs(x))), 2), ' rms', round(float(np.sqrt(np.mean(x ** 2))), 3))
