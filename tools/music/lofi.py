"""Lo-fi background music loops for Collect The Orbs: dark, slow and laid-back, with the bassline in front.

Warm electric-piano chords, a deep melodic sub bass, soft boom-bap drums with swing, a sparse echoing melody, dusty vinyl crackle and a
slight tape wobble. Moody rather than upbeat. Seamless loop: every hit lands on the same whole sample each pass, the crackle and the
tape wobble repeat exactly every loop, the track is rendered two passes long and the SECOND pass is kept.

Run from the repo root:  python3 tools/music/lofi.py [outdir] [--verify]   -> outdir/<track>.wav  (default /tmp/cto_music)
"""
import os, sys
import numpy as np
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import compose as C
from compose import SR, F, mtof, lowpass, highpass, bandpass, env_ar, reverb_stereo, echo, write_wav


class Song:
    def __init__(self, bpm, bars=16, passes=2, swing=0.3):
        self.beat = 60.0 / bpm
        self.step = self.beat / 4
        self.bars, self.passes, self.swing = bars, passes, swing
        self.L = int(round(bars * 4 * self.beat * SR))
        n = self.L * passes + SR * 3
        self.buses = {k: np.zeros((2, n), F) for k in ('drums', 'bass', 'keys', 'lead')}
        self.kicks = []

    def put(self, bus, x, i, pan=0.0, gain=1.0):
        buf = self.buses[bus]
        e = min(buf.shape[1], i + len(x))
        if 0 <= i < buf.shape[1] and e > i:
            l, r = gain * np.sqrt((1 - pan) / 2), gain * np.sqrt((1 + pan) / 2)
            buf[0, i:e] += (x[: e - i] * l).astype(F)
            buf[1, i:e] += (x[: e - i] * r).astype(F)


# ---- instruments --------------------------------------------------------------------------------------------------

def ep(m, dur, vel=1.0):
    """A warm electric piano: two-operator FM tine with a slow tremolo, softened by a low-pass."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m)
    idx = 1.4 * np.exp(-t / 0.25) + 0.25
    p = 2 * np.pi * f * t
    x = np.sin(p + idx * np.sin(p)) + 0.18 * np.sin(2 * np.pi * f * 7 * t) * np.exp(-t / 0.05) + 0.12 * np.sin(2 * p) * np.exp(-t / 0.5)
    x *= 1 + 0.12 * np.sin(2 * np.pi * 4.2 * t)
    x = lowpass(x, 2300) * 0.2 * vel
    return x * np.exp(-t / max(dur * 0.55, 0.3)) * env_ar(n, 0.004, min(0.25, dur * 0.4))


def sub(m, dur, vel=1.0, glide_from=None):
    """The star of the track: a deep, warm bass note (sine + soft low saw, lightly saturated), with an optional slide in."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    f = mtof(m) * np.ones(n)
    if glide_from is not None:
        f = mtof(m) * (mtof(glide_from) / mtof(m)) ** np.exp(-t / 0.06)
    p = 2 * np.pi * np.cumsum(f) / SR
    # the 2nd/3rd harmonics keep the line audible on phone speakers, where the deep fundamental barely sounds
    x = np.sin(p) + 0.75 * np.sin(2 * p) + 0.45 * np.sin(3 * p) + 0.2 * np.sin(4 * p) + lowpass(sum(np.sin(h * p) / h for h in range(1, 9)), 700) * 0.3
    x = np.tanh(x * 1.5) / 1.3
    return x * vel * env_ar(n, 0.012, min(0.12, dur / 2), 1.3) * np.exp(-t / max(dur * 1.4, 0.4))


def kick():
    n = int(0.35 * SR)
    t = np.arange(n) / SR
    x = np.sin(2 * np.pi * np.cumsum(48 + 70 * np.exp(-t / 0.035)) / SR) * np.exp(-t / 0.13)
    return lowpass(x, 900) * env_ar(n, 0.003, 0.05)


def snare(vel=1.0):
    n = int(0.25 * SR)
    t = np.arange(n) / SR
    tone = np.sin(2 * np.pi * 190 * t) * np.exp(-t / 0.05)
    nz = lowpass(bandpass(np.random.randn(n), 900, 5000), 4500) * np.exp(-t / 0.09)
    return (0.45 * tone + 0.6 * nz) * vel * 0.8 * env_ar(n, 0.002, 0.06)


def hat(vel=1.0):
    n = int(0.06 * SR)
    t = np.arange(n) / SR
    return lowpass(highpass(np.random.randn(n), 6500), 9000) * np.exp(-t / 0.014) * vel * 0.26


def crush(x, factor=3, bits=10):
    y = np.repeat(x[:, ::factor], factor, axis=1)[:, : x.shape[1]]
    q = 2 ** bits
    return (np.round(y * q) / q).astype(F)


def tape(x, wow_cycles, flutter_cycles, passes_len, depth_ms=0.45, flutter_ms=0.07):
    """Tape wobble: a slowly varying delay. Cycle counts are whole numbers per loop, so the wobble repeats exactly."""
    n = x.shape[1]
    t = np.arange(n) / SR
    L = passes_len / SR
    d = (0.0015 + depth_ms * 1e-3 * np.sin(2 * np.pi * wow_cycles / L * t) + flutter_ms * 1e-3 * np.sin(2 * np.pi * flutter_cycles / L * t + 1.3)) * SR
    pos = np.arange(n) - d
    i = np.clip(np.floor(pos).astype(int), 0, n - 2)
    fr = (pos - np.floor(pos)).astype(F)
    return np.stack([ch[i] * (1 - fr) + ch[i + 1] * fr for ch in x]).astype(F)


def vinyl(L, total, seed=9):
    """Dusty crackle: sparse little pops and a faint hiss. One loop long and tiled, so it repeats exactly."""
    r = np.random.RandomState(seed)
    base = np.zeros((2, L), F)
    n_pops = int(L / SR * 7)
    for _ in range(n_pops):
        i = r.randint(0, L - 400)
        m = r.randint(40, 260)
        pop = highpass(r.randn(m), 1800) * np.exp(-np.arange(m) / (m / 4.0)) * r.uniform(0.15, 1.0) * (1.0 if r.rand() < 0.85 else 2.2)
        c = r.randint(0, 2)
        base[c, i:i + m] += pop[: L - i].astype(F)
        base[1 - c, i:i + m] += pop[: L - i].astype(F) * 0.4
    hiss = lowpass(r.randn(2, L), 5000) * 0.012
    base = (base * 0.05 + hiss).astype(F)
    reps = total // L + 2
    return np.tile(base, (1, reps))[:, :total]


# ---- song ---------------------------------------------------------------------------------------------------------

def compose(sg, spec):
    st = sg.step
    for p in range(sg.passes):
        base = p * sg.L

        def idx(local):
            return base + int(round(local * SR))
        for bar in range(sg.bars):
            second_half = bar >= 8
            chord = spec['chords'][(bar // 2) % len(spec['chords'])]
            voicing, root = chord
            b0 = bar * 4 * sg.beat
            np.random.seed(7000 + bar)
            # keys: a long chord each 2 bars, a soft re-strum on the "and" of 3
            if bar % 2 == 0:
                for k, m in enumerate(voicing):
                    sg.put('keys', ep(m, 2 * 4 * sg.beat * 0.95, 0.85), idx(b0 + k * 0.012), (-0.35 + 0.23 * k) * 0.8, 1.0)
            if bar % 2 == 1:
                for k, m in enumerate(voicing):
                    sg.put('keys', ep(m, sg.beat * 1.6, 0.5), idx(b0 + 2.5 * sg.beat + k * 0.015), (-0.3 + 0.2 * k) * 0.8, 1.0)
            # bass: the pattern for this chord, played with the pattern's own rhythm
            pat = spec['bass_pat'][bar % 2]
            for (s, iv, ln) in pat:
                note = root + iv
                gl = (root + iv - 5) if (s == 0 and spec.get('slide') and bar % 2 == 0) else None
                sg.put('bass', sub(note, ln * st, 1.0 if s % 8 == 0 else 0.85, gl), idx(b0 + s * st))
            # drums with swing
            for s in range(16):
                sw = sg.swing * st if s % 2 == 1 else 0.0
                i = idx(b0 + s * st + sw)
                np.random.seed(bar * 16 + s)
                if s in spec['kicks'] or (second_half and s in spec.get('kicks2', ())):
                    sg.put('drums', kick(), i, 0.0, 1.0 if s == 0 else 0.8)
                    sg.kicks.append(i)
                if s in (4, 12):
                    sg.put('drums', snare(1.0), i)
                if s in spec.get('ghosts', ()) and second_half:
                    sg.put('drums', snare(0.35), i)
                if s % 2 == 0 and spec['hats_on']:
                    sg.put('drums', hat(1.0 if s % 4 == 0 else 0.6), i, 0.25 * (-1) ** (s // 2))
                elif s % 2 == 1 and spec['hats_on'] == 2:
                    sg.put('drums', hat(0.35), i, 0.25 * (-1) ** s)
            # sparse melody: two or three notes every other bar, from the minor pentatonic, lots of space
            rs = np.random.RandomState(spec['seed'] * 131 + bar)
            if bar % 2 == 0 and (bar % 4 == 0 or second_half):
                pent = spec['pent']
                for k in range(rs.randint(2, 4)):
                    s = [2, 6, 9, 12][k + (0 if k < 3 else 0)] + rs.randint(0, 2)
                    sg.put('lead', ep(pent[rs.randint(0, len(pent))] + 12 * spec['lead_oct'], st * 5, 0.9), idx(b0 + s * st + (sg.swing * st if s % 2 else 0)), rs.uniform(-0.4, 0.4), 1.0)


def render_track(spec, verify=False):
    np.random.seed(spec['seed'])
    sg = Song(spec['bpm'], 16, 3 if verify else 2, spec['swing'])
    compose(sg, spec)
    n = sg.buses['keys'].shape[1]
    g = C.sidechain(sg.kicks, n, 0.3, 0.25)
    lv = spec['lvl']
    drums = crush(sg.buses['drums'], 2, 11)
    drums = np.stack([lowpass(c, 7500) for c in drums]).astype(F)
    keys = (sg.buses['keys'] * g).astype(F)
    lead = echo(sg.buses['lead'], sg.beat * 0.75, 0.55, 0.8)
    wob = tape((keys * lv['keys'] + lead * lv['lead']).astype(F), spec['wow'], spec['flutter'], sg.L)
    bass = sg.buses['bass'] * lv['bass']
    mix = (drums * lv['drums'] + bass + wob).astype(F)
    mix = reverb_stereo(mix, spec.get('tau', 1.2), spec.get('wet', 0.14))
    mix = (mix + vinyl(sg.L, n) * lv['vinyl']).astype(F)
    mix = np.stack([lowpass(highpass(c, 28), 9500) for c in mix]).astype(F)
    mix = C.master(mix, 0.2)
    if verify:
        render_track.seam = float(np.max(np.abs(mix[:, sg.L: 2 * sg.L] - mix[:, 2 * sg.L: 3 * sg.L])))
    return mix[:, sg.L: 2 * sg.L]


# chords: (EP voicing midis, bass root midi)
CM = [((51, 55, 58, 62), 36), ((56, 60, 63, 67), 32), ((53, 56, 60, 63), 29), ((55, 58, 62, 65), 31)]   # Cm9 Abmaj7 Fm7 Gm7
AM = [((57, 60, 64, 67), 33), ((53, 57, 60, 64), 38), ((53, 57, 60, 64), 29), ((55, 59, 62, 67), 40)]   # Am9 Dm9 Fmaj7 Em7
DM = [((53, 57, 60, 64), 38), ((58, 62, 65, 69), 34), ((55, 58, 62, 65), 31), ((57, 60, 64, 67), 33)]   # Dm9 Bbmaj7 Gm9 Am7

TRACKS = {
    'dusty_sub': dict(seed=1, bpm=72, swing=0.32, chords=CM, pent=[60, 63, 65, 67, 70], lead_oct=0, tau=1.3, wet=0.14, wow=3, flutter=29,
                      lvl=dict(drums=1.0, bass=0.62, keys=1.0, lead=0.8, vinyl=1.0), hats_on=1, kicks=(0, 10), kicks2=(7, 15), ghosts=(7, 15), slide=True,
                      bass_pat=[[(0, 0, 6), (6, 0, 2), (8, 7, 4), (12, 5, 3), (14, 3, 2)], [(0, 0, 5), (4, 7, 3), (8, 5, 4), (13, 3, 3)]]),
    'midnight_tape': dict(seed=2, bpm=80, swing=0.36, chords=AM, pent=[57, 60, 62, 64, 67], lead_oct=1, tau=1.0, wet=0.12, wow=4, flutter=37,
                          lvl=dict(drums=1.05, bass=0.62, keys=1.0, lead=0.7, vinyl=1.2), hats_on=2, kicks=(0, 7, 10), kicks2=(14,), ghosts=(15,), slide=False,
                          bass_pat=[[(0, 0, 3), (3, 0, 2), (6, 7, 2), (8, 0, 3), (11, 3, 2), (14, 5, 2)], [(0, 0, 4), (4, 12, 2), (7, 7, 3), (10, 0, 3), (14, 10, 2)]]),
    'rainy_drift': dict(seed=3, bpm=66, swing=0.28, chords=DM, pent=[62, 65, 67, 69, 72], lead_oct=0, tau=1.8, wet=0.2, wow=2, flutter=23,
                        lvl=dict(drums=0.8, bass=0.66, keys=1.05, lead=0.85, vinyl=1.1), hats_on=1, kicks=(0,), kicks2=(10,), ghosts=(), slide=True,
                        bass_pat=[[(0, 0, 10), (10, 7, 6)], [(0, 0, 8), (8, 3, 4), (12, 5, 4)]]),
}

if __name__ == '__main__':
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    verify = '--verify' in sys.argv
    out = args[0] if args else '/tmp/cto_music'
    os.makedirs(out, exist_ok=True)
    for name, spec in TRACKS.items():
        x = render_track(spec, verify)
        write_wav(os.path.join(out, name + '.wav'), x)
        print(name, round(x.shape[1] / SR, 1), 's', ('loop-repeat error %.2g' % render_track.seam) if verify else '', 'peak', round(float(np.max(np.abs(x))), 2))
