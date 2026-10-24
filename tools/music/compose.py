"""Background music loops for Collect The Orbs: driving electronic tracks (synthwave / techno / neon house) that loop seamlessly.

Each track has real song structure over 32 bars (intro, groove, breakdown with a riser, second groove, back to the start): punchy
kick, offbeat hats, claps, a rolling bassline, saw-chord stabs, a gated arpeggio and a hook melody, with sidechain pumping.
Everything is synthesised. To loop without a seam, every instrument hit lands on the same whole sample each pass, the track is
rendered two passes long through its reverb and echoes, and the SECOND pass is kept, so the tails of the previous pass flow into
the start: the end joins the beginning exactly.

Run from the repo root:  python3 tools/music/compose.py [outdir] [--verify]   -> outdir/<track>.wav  (default /tmp/cto_music)
"""
import os, sys, wave
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, fftconvolve

SR = 44100
F = np.float32


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


def res_lowpass(x, fc, q=6.0, block=64):
    """Resonant low-pass; fc is an array (Hz per sample) so the cutoff can sweep (the classic acid squelch)."""
    y = np.zeros_like(x)
    zi = np.zeros(2)
    for i in range(0, len(x), block):
        w0 = 2 * np.pi * min(max(float(fc[min(i + block // 2, len(fc) - 1)]), 40.0), SR / 2 - 400) / SR
        al = np.sin(w0) / (2 * q)
        c = np.cos(w0)
        b = np.array([(1 - c) / 2, 1 - c, (1 - c) / 2])
        a = np.array([1 + al, -2 * c, 1 - al])
        seg, zi = lfilter(b / a[0], a / a[0], x[i:i + block], zi=zi)
        y[i:i + block] = seg
    return y


def saw(f, n, k=14, detune=1.0, phase0=0.0):
    t = np.arange(n) / SR
    out = np.zeros(n)
    for h in range(1, k + 1):
        if h * f * detune > SR / 2 - 800:
            break
        out += np.sin(2 * np.pi * f * detune * h * t + phase0 * h) / h
    return out


class Track:
    def __init__(self, bpm, bars, passes=2):
        self.bpm, self.bars, self.passes = bpm, bars, passes
        self.beat = 60.0 / bpm
        self.L = int(round(bars * 4 * self.beat * SR))
        n = self.L * passes + SR * 3
        self.buses = {k: np.zeros((2, n), F) for k in ('drums', 'bass', 'harm', 'arp', 'lead', 'riser')}
        self.kicks = []

    def put(self, bus, x, i, pan=0.0, gain=1.0):
        buf = self.buses[bus]
        e = min(buf.shape[1], i + len(x))
        if 0 <= i < buf.shape[1] and e > i:
            l, r = gain * np.sqrt((1 - pan) / 2), gain * np.sqrt((1 + pan) / 2)
            buf[0, i:e] += (x[: e - i] * l).astype(F)
            buf[1, i:e] += (x[: e - i] * r).astype(F)


# ---- instruments --------------------------------------------------------------------------------------------------

def kick(vel=1.0):
    n = int(0.4 * SR)
    t = np.arange(n) / SR
    f = 46 + 110 * np.exp(-t / 0.03)
    x = np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-t / 0.15)
    x += 0.25 * np.sin(2 * np.pi * np.cumsum(180 * np.exp(-t / 0.008)) / SR) * np.exp(-t / 0.01)   # the beater, a short pitched thud
    return x * vel * env_ar(n, 0.0015, 0.05)


def hat(vel=1.0, open_=False):
    n = int((0.2 if open_ else 0.055) * SR)
    t = np.arange(n) / SR
    return highpass(np.random.randn(n), 7000) * np.exp(-t / (0.06 if open_ else 0.014)) * vel * 0.32


def clap(vel=1.0):
    n = int(0.25 * SR)
    t = np.arange(n) / SR
    x = np.zeros(n)
    for d in (0.0, 0.011, 0.023):
        i = int(d * SR)
        m = n - i
        x[i:] += bandpass(np.random.randn(m), 900, 3500) * np.exp(-np.arange(m) / SR / 0.012)
    x += bandpass(np.random.randn(n), 1000, 3000) * np.exp(-t / 0.09) * 0.7
    return x * vel * 0.9 * env_ar(n, 0.001, 0.04)


def sub_bass(m, dur, vel=1.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m)
    p = 2 * np.pi * np.cumsum(f * (1 + 0.35 * np.exp(-t / 0.01))) / SR
    x = np.sin(p) * 0.9 + lowpass(saw(f, n, 8) + 0.6 * saw(f, n, 8, 1.004), 400) * 0.4
    return np.tanh(x * 1.5) / 1.2 * vel * env_ar(n, 0.005, min(0.05, dur / 2))


def acid_note(m, dur, accent=1.0, cutoff=2200, q=7.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m)
    x = saw(f, n, 16)
    fc = 260 + cutoff * accent * np.exp(-t / 0.11)
    y = res_lowpass(x, fc, q)
    return np.tanh(y * 1.6) * 0.55 * (0.7 + 0.3 * accent) * env_ar(n, 0.003, min(0.04, dur / 2))


def stab(notes, dur=0.22, cutoff=3200):
    n = int(dur * SR)
    t = np.arange(n) / SR
    x = sum(saw(mtof(m), n, 12, d) for m in notes for d in (0.994, 1.0, 1.006))
    x = lowpass(x, cutoff * 0.7 + cutoff * 0.6 * np.exp(-t[-1])) * 0.06
    return x * np.exp(-t / (dur * 0.5)) * env_ar(n, 0.004, 0.05)


def pad(notes, dur, cutoff=1500):
    n = int(dur * SR)
    t = np.arange(n) / SR
    L, R = np.zeros(n), np.zeros(n)
    for m in notes:
        for d, side in ((-0.011, 0), (0.0, 0), (0.011, 1), (0.007, 1)):
            f = mtof(m) * (1 + d)
            w = saw(f, n, 8, 1.0, np.random.rand() * 6.28)
            (L if side == 0 else R)[:] += w
    out = [lowpass(c, cutoff, 2) * (0.8 + 0.2 * np.sin(2 * np.pi * 0.15 * t)) for c in (L, R)]
    return np.stack(out) * env_ar(n, min(1.0, dur * 0.3), min(1.2, dur * 0.4), 1.4) * 0.05


def arp_note(m, dur=0.14, bright=1.0):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m)
    x = saw(f, n, 10) + 0.8 * saw(f, n, 10, 1.005)
    x = lowpass(x, 1800 + 3500 * bright * np.exp(-t[-1] / 0.1)) * 0.22
    return x * np.exp(-t / (dur * 0.55)) * env_ar(n, 0.002, 0.03)


def lead_note(m, dur, vib=0.004):
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m) * (1 + vib * np.sin(2 * np.pi * 5.5 * t) * np.minimum(t / 0.2, 1))
    p = 2 * np.pi * np.cumsum(f) / SR
    x = np.sin(p) + 0.5 * np.sin(2 * p) + 0.33 * np.sin(3 * p) + 0.25 * np.sin(4 * p) + 0.55 * np.sin(np.cumsum(f * 1.006) * 2 * np.pi / SR)
    x = lowpass(x, 2600) * 0.16
    return x * env_ar(n, 0.01, min(0.12, dur * 0.5))


def riser(dur):
    n = int(dur * SR)
    t = np.arange(n) / SR
    k = t / dur
    nz = np.random.randn(n)
    fc = 300 + 9000 * k ** 2
    y = res_lowpass(nz, fc, 2.5) * (k ** 1.6)
    tone = np.sin(2 * np.pi * np.cumsum(200 * 2 ** (3 * k)) / SR) * k ** 2 * 0.5
    return (y * 0.5 + tone) * 0.35


# ---- effects ------------------------------------------------------------------------------------------------------

def reverb_stereo(x, tau, wet, seed=1):
    r = np.random.RandomState(seed)
    n = int(tau * 3 * SR)
    t = np.arange(n) / SR
    out = []
    for ch in range(2):
        ir = lowpass(r.randn(n), 5500) * np.exp(-t / tau)
        ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
        out.append(fftconvolve(x[ch], ir)[: x.shape[1]])
    return (x * (1 - wet) + np.stack(out) * wet * 2.2).astype(F)


def echo(x, delay, fb, wet, taps=5):
    d = int(delay * SR)
    out = np.zeros_like(x)
    cur = x.copy()
    for k in range(1, taps + 1):
        cur = (lowpass(cur, 3800) * fb).astype(F)
        sh = k * d
        out[k % 2, sh:] += cur[0, : x.shape[1] - sh] * 0.6 + cur[1, : x.shape[1] - sh] * 0.4
    return (x + out * wet).astype(F)


def sidechain(kicks, n, depth, release):
    g = np.ones(n, F)
    m = int(release * 4 * SR)
    shape = (1 - depth * np.exp(-np.arange(m) / SR / (release / 2.2))).astype(F)
    for i in kicks:
        e = min(n, i + m)
        if i < n:
            g[i:e] = np.minimum(g[i:e], shape[: e - i])
    return g


def master(x, loud):
    x = np.stack([highpass(c, 30) for c in x])
    w = 2 * np.pi * 95 / SR
    A = 10 ** (3.5 / 40)
    c, s = np.cos(w), np.sin(w)
    al = s / (2 * 0.707)
    b = np.array([A * ((A + 1) - (A - 1) * c + 2 * np.sqrt(A) * al), 2 * A * ((A - 1) - (A + 1) * c), A * ((A + 1) - (A - 1) * c - 2 * np.sqrt(A) * al)])
    a = np.array([(A + 1) + (A - 1) * c + 2 * np.sqrt(A) * al, -2 * ((A - 1) + (A + 1) * c), (A + 1) + (A - 1) * c - 2 * np.sqrt(A) * al])
    x = np.stack([lfilter(b / a[0], a / a[0], ch) for ch in x])
    x = np.tanh(x * 1.25)
    x = x * (loud / (np.sqrt(np.mean(x ** 2)) + 1e-9))
    pk = np.max(np.abs(x))
    return (x * (0.92 / pk) if pk > 0.92 else x).astype(F)


# ---- song structure -----------------------------------------------------------------------------------------------

# sections over 32 bars: which layers play
SECTIONS = {
    'intro': {'pad', 'arp', 'hat_soft'},
    'groove': {'kick', 'hat', 'clap', 'bass', 'stab', 'arp', 'pad'},
    'break': {'pad', 'lead', 'arp', 'bass_long', 'riser'},
    'groove2': {'kick', 'hat', 'clap', 'bass', 'stab', 'arp', 'pad', 'lead'},
    'outro': {'kick', 'hat', 'bass', 'pad', 'arp', 'riser'},
}
BAR_SECTION = ['intro'] * 4 + ['groove'] * 8 + ['break'] * 4 + ['groove2'] * 12 + ['outro'] * 2 + ['break'] * 2


def compose(t, spec):
    chords = spec['chords']           # four chords, each (root midi, [triad midis]), each lasting 2 bars
    rng_seed = spec['seed']
    step = t.beat / 4
    for p in range(t.passes):
        base = p * t.L

        def idx(local):
            return base + int(round(local * SR))
        for bar in range(t.bars):
            layers = SECTIONS[BAR_SECTION[bar]]
            root, triad = chords[(bar // 2) % len(chords)]
            b0 = bar * 4 * t.beat
            np.random.seed(5000 + bar)
            if bar % 2 == 0 and ('pad' in layers):
                pd = pad([n + 12 for n in triad] + [root + 24], 2 * 4 * t.beat * (1 if bar % 8 else 1) + 1.0, spec.get('pad_cut', 1400))
                t.buses['harm'][0, idx(b0): idx(b0) + pd.shape[1]] += pd[0][: t.buses['harm'].shape[1] - idx(b0)]
                t.buses['harm'][1, idx(b0): idx(b0) + pd.shape[1]] += pd[1][: t.buses['harm'].shape[1] - idx(b0)]
            if 'riser' in layers and bar in (14, 15, 30, 31):
                t.put('riser', riser(t.beat * 4), idx(b0), 0.0, 0.7)
            for s in range(16):
                i = idx(b0 + s * step)
                np.random.seed(bar * 16 + s)
                rs = np.random.RandomState(rng_seed * 7919 + bar * 16 + s)
                if 'kick' in layers and s % 4 == 0:
                    t.put('drums', kick(1.0 if s % 8 == 0 else 0.9), i)
                    t.kicks.append(i)
                if ('hat' in layers or 'hat_soft' in layers) and spec['hats'][s]:
                    v = spec['hats'][s] * (0.55 if 'hat_soft' in layers and 'hat' not in layers else 1.0)
                    t.put('drums', hat(v, s in spec.get('open_hats', ())), i + int(0.003 * SR) * (s % 2), 0.3 * (-1) ** s)
                if 'clap' in layers and s in (4, 12):
                    t.put('drums', clap(0.9), i)
                if 'bass' in layers and spec['bass'][s]:
                    n_ = spec['bass'][s]
                    off = {1: 0, 2: 12, 3: 7, 4: 10}[n_ if n_ in (1, 2, 3, 4) else 1]
                    if spec['bass_kind'] == 'acid':
                        t.put('bass', acid_note(root + 12 + off - (12 if off >= 12 else 0), step * spec['bass_len'][s], 1.0 if s % 4 == 2 else 0.6, spec.get('acid_cut', 2200)), i, 0.0, 0.9)
                        t.put('bass', sub_bass(root + off % 12 - 12 if root > 40 else root, step * 1.6, 0.8), i)
                    else:
                        t.put('bass', sub_bass(root + off % 12 + (0 if off < 12 else 0), step * spec['bass_len'][s], 1.0), i)
                if 'bass_long' in layers and s == 0 and bar % 2 == 0:
                    t.put('bass', sub_bass(root, 2 * 4 * t.beat, 0.38), i)
                if 'stab' in layers and s in spec['stabs']:
                    t.put('harm', stab([n + 12 for n in triad], step * 2.2, spec.get('stab_cut', 3200)), i, rs.uniform(-0.3, 0.3), 1.0)
                if 'arp' in layers and spec['arp'][s]:
                    note = triad[(spec['arp'][s] - 1) % 3] + 24 + (12 if spec['arp'][s] > 3 else 0)
                    t.put('arp', arp_note(note, step * 1.6), i, 0.35 * (-1) ** (s // 2), 1.0)
                if 'lead' in layers and bar % 2 == 0 and s in spec['lead_steps']:
                    k = spec['lead_steps'].index(s)
                    deg = spec['lead_notes'][(((bar // 2) % 4) * len(spec['lead_steps']) + k) % len(spec['lead_notes'])]
                    t.put('lead', lead_note(deg, step * spec['lead_len'][k]), i, 0.0, 1.0)


def render_track(spec, verify=False):
    np.random.seed(spec['seed'])
    t = Track(spec['bpm'], 32, passes=3 if verify else 2)
    compose(t, spec)
    n = t.buses['harm'].shape[1]
    g = sidechain(t.kicks, n, spec.get('duck', 0.7), spec.get('release', 0.2))
    lv = spec['lvl']
    mix = (t.buses['drums'] * lv['drums'] + t.buses['bass'] * lv['bass'] * (0.55 + 0.45 * g) +
           t.buses['harm'] * lv['harm'] * g + echo(t.buses['arp'], t.beat * 0.75, 0.5, 0.65) * lv['arp'] * (0.4 + 0.6 * g) +
           echo(t.buses['lead'], t.beat * 0.75, 0.5, 0.75) * lv['lead'] + t.buses['riser'] * lv['riser'])
    mix = reverb_stereo(mix, spec.get('tau', 1.4), spec.get('wet', 0.2))
    mix = master(mix, 0.2)
    if verify:
        render_track.seam = float(np.max(np.abs(mix[:, t.L: 2 * t.L] - mix[:, 2 * t.L: 3 * t.L])))
    return mix[:, t.L: 2 * t.L]


# notes: midi
A_MIN = [(45, [57, 60, 64]), (41, [53, 57, 60]), (48, [55, 60, 64]), (43, [55, 59, 62])]     # Am F C G
C_MIN = [(36, [60, 63, 67]), (44, [56, 60, 63]), (39, [55, 58, 63]), (46, [58, 62, 65])]     # Cm Ab Eb Bb
D_MIN = [(38, [57, 62, 65]), (34, [58, 62, 65]), (41, [57, 60, 65]), (36, [55, 60, 64])]     # Dm Bb F C

TRACKS = {
    'synthwave': dict(seed=1, bpm=116, chords=A_MIN, duck=0.7, release=0.2, tau=1.3, wet=0.2,
                      lvl=dict(drums=0.85, bass=1.0, harm=0.9, arp=0.8, lead=0.7, riser=0.6),
                      hats=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1], open_hats=(2, 6, 10, 14),
                      bass=[1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 3, 0, 0, 1, 0], bass_len=[2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1], bass_kind='sub',
                      stabs=(2, 6, 10, 14), arp=[1, 3, 2, 3, 1, 3, 2, 3, 1, 3, 2, 3, 1, 3, 2, 3], stab_cut=2800,
                      lead_steps=[0, 3, 6, 10], lead_len=[3, 3, 4, 6], lead_notes=[76, 72, 74, 71, 76, 79, 77, 74, 72, 71, 69, 72, 74, 76, 79, 76]),
    'techno_pulse': dict(seed=2, bpm=128, chords=D_MIN, duck=0.75, release=0.18, tau=1.0, wet=0.16, acid_cut=2600, pad_cut=1100,
                         lvl=dict(drums=0.95, bass=1.0, harm=0.7, arp=0.7, lead=0.55, riser=0.6),
                         hats=[0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 1, 1, 1], open_hats=(2, 10),
                         bass=[1, 1, 0, 1, 1, 0, 1, 1, 1, 1, 0, 4, 1, 0, 1, 1], bass_len=[1] * 16, bass_kind='acid',
                         stabs=(3, 7, 11), arp=[1, 0, 2, 0, 3, 0, 2, 0, 1, 0, 2, 0, 3, 0, 4, 0], stab_cut=2400,
                         lead_steps=[0, 6, 10], lead_len=[5, 3, 5], lead_notes=[69, 72, 74, 72, 69, 67, 65, 69, 72, 74, 77, 76]),
    'neon_house': dict(seed=3, bpm=124, chords=C_MIN, duck=0.8, release=0.22, tau=1.5, wet=0.24,
                       lvl=dict(drums=0.8, bass=1.0, harm=1.0, arp=0.9, lead=0.75, riser=0.6),
                       hats=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0], open_hats=(2, 6, 10, 14),
                       bass=[0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0], bass_len=[1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 2, 2, 1], bass_kind='sub',
                       stabs=(2, 5, 8, 11, 14), arp=[1, 2, 3, 4, 3, 2, 1, 2, 3, 4, 3, 2, 1, 2, 3, 4], stab_cut=3600,
                       lead_steps=[0, 2, 4, 8, 12], lead_len=[2, 2, 4, 4, 4], lead_notes=[79, 75, 77, 74, 75, 79, 82, 77, 75, 74, 72, 74, 75, 79, 77, 75, 74, 70, 72, 75]),
}


def write_wav(path, x):
    with wave.open(path, 'wb') as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes((np.clip(x.T, -1, 1) * 32767).astype('<i2').tobytes())


if __name__ == '__main__':
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    verify = '--verify' in sys.argv
    out = args[0] if args else '/tmp/cto_music'
    os.makedirs(out, exist_ok=True)
    for name, spec in TRACKS.items():
        x = render_track(spec, verify)
        write_wav(os.path.join(out, name + '.wav'), x)
        print(name, round(x.shape[1] / SR, 1), 's', ('loop-repeat error %.2g' % render_track.seam) if verify else '', 'peak', round(float(np.max(np.abs(x))), 2))
