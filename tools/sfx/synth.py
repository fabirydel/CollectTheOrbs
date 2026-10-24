"""Synth remakes of the game's original sounds, by analysis and resynthesis.

Each original sound (tools/sfx/originals/*.ogg) is analysed for its length, loudness envelope, pitch track (the strongest note over
time), how tonal or noisy it is over time, and where its noise sits in frequency. The sound is then rebuilt from synth voices that
follow those same curves, so the length, pitch and shape stay as close to the original as possible. No clicks or impacts are added.
Three takes of every sound differ only in the voice:

  1 "Retro"  : saw/square voices with the original's own harmonic balance (closest to the originals)
  2 "Smooth" : sine voices with a gentle chorus and soft, filtered noise
  3 "Bright" : pulse voices with an octave shimmer, crisp noise

Run from the repo root:  python3 tools/sfx/synth.py [outdir]    -> outdir/<sound>_<1|2|3>.wav
"""
import os, subprocess, sys, tempfile, wave
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, medfilt, stft

SR = 44100
HOP = 256
HERE = os.path.dirname(os.path.abspath(__file__))
RNG = np.random.RandomState(5)
KINDS = '123'


# ---- helpers ------------------------------------------------------------------------------------------------------

def load_mono(path):
    tmp = tempfile.mktemp(suffix='.wav')
    subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', path, '-ac', '1', '-ar', str(SR), tmp], check=True)
    with wave.open(tmp) as w:
        x = np.frombuffer(w.readframes(w.getnframes()), dtype='<i2').astype(float) / 32768
    os.remove(tmp)
    return x


def lowpass(x, fc, order=2):
    return sosfilt(butter(order, min(fc, SR / 2 - 100), 'low', fs=SR, output='sos'), x)


def sweep_bandpass(x, fc, q, block=128):
    """Resonant band-pass whose centre follows fc (Hz per sample) and whose Q may follow q (per sample)."""
    y = np.zeros_like(x)
    zi = np.zeros(2)
    for i in range(0, len(x), block):
        j = min(i + block // 2, len(fc) - 1)
        w0 = 2 * np.pi * min(max(float(fc[j]), 40.0), SR / 2 - 300) / SR
        al = np.sin(w0) / (2 * max(float(q[j]), 0.5))
        b = np.array([al, 0, -al])
        a = np.array([1 + al, -2 * np.cos(w0), 1 - al])
        seg, zi = lfilter(b / a[0], a / a[0], x[i:i + block], zi=zi)
        y[i:i + block] = seg
    return y


def up(curve, n):
    """Interpolate a per-frame curve to n samples."""
    return np.interp(np.arange(n) / HOP, np.arange(len(curve)), curve)


# ---- analysis -----------------------------------------------------------------------------------------------------

def analyse(path):
    x = load_mono(path)
    pk = np.max(np.abs(x))
    keep = np.where(np.abs(x) > 0.004 * pk)[0]
    x = x[keep[0]: keep[-1] + 1]
    n = len(x)
    f, t, Z = stft(x, SR, nperseg=1024, noverlap=1024 - HOP, nfft=2048, boundary=None, padded=False)
    A = np.abs(Z) + 1e-9
    frames = A.shape[1]
    # loudness per frame
    rms = np.array([np.sqrt(np.mean(x[i * HOP: i * HOP + 1024] ** 2)) for i in range(frames)])
    rms = rms / (rms.max() + 1e-9)
    lo, hi = np.searchsorted(f, 100), np.searchsorted(f, 9000)
    flat = np.exp(np.mean(np.log(A[lo:hi]), 0)) / np.mean(A[lo:hi], 0)
    cent = (f[lo:hi, None] * A[lo:hi]).sum(0) / A[lo:hi].sum(0)
    bw = np.sqrt(((f[lo:hi, None] - cent) ** 2 * A[lo:hi]).sum(0) / A[lo:hi].sum(0))
    # pitch: harmonic product spectrum over 110 .. 4500 Hz
    k0, k1 = np.searchsorted(f, 110), np.searchsorted(f, 4500)
    f0 = np.zeros(frames)
    tonal = np.zeros(frames)
    prof = np.zeros(6)
    cnt = 0
    for i in range(frames):
        col = A[:, i]
        ks = np.arange(k0, k1)
        hps = col[ks] * col[np.minimum(ks * 2, len(col) - 1)] ** 0.7 * col[np.minimum(ks * 3, len(col) - 1)] ** 0.5
        j = ks[np.argmax(hps)]
        if 1 <= j < len(col) - 1:   # parabolic interpolation
            a_, b_, c_ = np.log(col[j - 1]), np.log(col[j]), np.log(col[j + 1])
            j = j + 0.5 * (a_ - c_) / (a_ - 2 * b_ + c_ + 1e-12)
        f0[i] = j * (f[1] - f[0])
        peak = col[int(round(j))] if int(round(j)) < len(col) else 0
        tonal[i] = np.clip(1 - (flat[i] - 0.03) / 0.3, 0, 1)
        if tonal[i] > 0.5 and rms[i] > 0.2:
            for h in range(6):
                kk = int(round(j * (h + 1)))
                if kk < len(col):
                    prof[h] += col[kk] / (peak + 1e-9)
            cnt += 1
    prof = prof / cnt if cnt else np.array([1, 0.4, 0.2, 0.1, 0.05, 0.03])
    f0 = np.pad(f0, 8, mode='edge')
    med = medfilt(f0, 15)
    for i in range(len(f0)):   # fix octave jumps against the local median
        c = [f0[i], f0[i] * 2, f0[i] / 2]
        f0[i] = c[int(np.argmin([abs(np.log2(v / med[i])) for v in c]))]
    f0 = medfilt(f0, 7)[8:-8]
    good = tonal > 0.15
    if good.any():   # hold the pitch through noisy stretches
        idx = np.where(good)[0]
        f0 = np.interp(np.arange(frames), idx, f0[idx])
    f0 = np.convolve(np.pad(f0, 3, mode='edge'), np.ones(7) / 7, mode='valid')
    tonal = np.convolve(tonal, np.ones(5) / 5, 'same') ** 1.5   # favour clearly tonal stretches
    return dict(n=n, rms=rms, f0=f0, tonal=tonal, cent=np.convolve(cent, np.ones(5) / 5, 'same'),
                bw=np.convolve(bw, np.ones(5) / 5, 'same'), prof=np.clip(prof, 0, 1.2), flat=flat)


# ---- resynthesis --------------------------------------------------------------------------------------------------

def voice(kind, f, prof):
    ph = 2 * np.pi * np.cumsum(f) / SR
    fm = float(np.max(f))
    if kind == '1':   # measured harmonic balance, a touch of detune for body
        x = sum(a * np.sin((h + 1) * ph) for h, a in enumerate(prof) if (h + 1) * fm < SR / 2 - 800)
        x += 0.5 * sum(a * np.sin((h + 1) * ph * 1.004) for h, a in enumerate(prof) if (h + 1) * fm < SR / 2 - 800)
        return x
    if kind == '2':   # sine with a soft second partial and a slow chorus
        ch = 1 + 0.003 * np.sin(2 * np.pi * 0.7 * np.arange(len(f)) / SR)
        return np.sin(ph) + 0.25 * np.sin(2 * ph) + 0.8 * np.sin(np.cumsum(f * ch) * 2 * np.pi / SR) + 0.12 * np.sin(3 * ph)
    pulse = sum(np.sin(np.pi * k * 0.25) / k * np.sin(k * ph) for k in range(1, 12) if k * fm < SR / 2 - 800)
    return 0.8 * pulse + 0.35 * np.sin(2 * ph * 1.002)


def resynth(p, kind):
    n = p['n']
    env = up(p['rms'], n) ** 1.0
    f0 = np.maximum(up(p['f0'], n), 60.0)
    tonal = np.clip(up(p['tonal'], n), 0, 1)
    cent = np.maximum(up(p['cent'], n), 150.0)
    bw = np.maximum(up(p['bw'], n), 80.0)
    voiced = voice(kind, f0, p['prof'])
    voiced = voiced / (np.max(np.abs(voiced)) + 1e-9) * tonal * env
    q = np.clip(cent / (bw * 1.6), 0.6, 8.0)
    noise = sweep_bandpass(RNG.randn(n), cent, q)
    if kind == '2':
        noise = lowpass(noise, 7000)
    noise = noise / (np.max(np.abs(noise)) + 1e-9) * (1 - tonal) * env * (1.0 if kind != '2' else 0.8)
    y = voiced + noise
    a = int(0.004 * SR)
    y[:a] *= np.linspace(0, 1, a)   # no clicks: a short fade-in and fade-out
    y[-a:] *= np.linspace(1, 0, a)
    return y


def stereo(y, kind):
    d = {'1': 0.0004, '2': 0.0012, '3': 0.0007}[kind]
    k = int(d * SR)
    return np.stack([y, np.pad(y, (k, 0))[: len(y)] * (0.96 if kind != '2' else 0.9)])


def finish(st, peak=0.7):
    return st * (peak / (np.max(np.abs(st)) + 1e-9))


# sound -> (original file, what it is)
SOUNDS = [
    ('menu_tap', 'buttonSmall', 'Small menu buttons'),
    ('menu_big', 'buttonLarge', 'Big buttons: home, retry'),
    ('cant', 'select', "Can't afford it"),
    ('bought', 'bought', 'Buying something (also the game-over counter)'),
    ('launch', 'launch', 'START: the game launches'),
    ('pre', 'pre', 'Cue just before the run'),
    ('absorb', 'absorb', 'Catching an orb with the right shield'),
    ('wrong', 'loseLife', 'Hitting an orb with the wrong shield'),
    ('shield_hit', 'shield', 'A hit absorbed by a shield item'),
    ('life', 'life', 'Gold orb: energy restored'),
    ('dead', 'dead', 'Game over'),
    ('item_pickup', 'object', 'Picking up an item'),
    ('item_tick', 'objectCount', 'Item about to run out'),
    ('swap', 'swap', 'Color swap item (plays reversed when it ends)'),
    ('mult', 'mult', 'x2 / x3 item (plays reversed when it ends)'),
    ('slow', 'slow', 'Slow item (plays reversed when it ends)'),
    ('shield_item', 'sword', 'Shield item (plays reversed when it ends)'),
    ('switch', 'change2', 'Switching shield color'),
]
REVERSED = {'swap': 'swap_end', 'mult': 'mult_end', 'slow': 'slow_end', 'shield_item': 'shield_item_end'}


def render_all(outdir):
    os.makedirs(outdir, exist_ok=True)

    def write(name, kind, st):
        with wave.open(os.path.join(outdir, '%s_%s.wav' % (name, kind)), 'wb') as w:
            w.setnchannels(2)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes((np.clip(st.T, -1, 1) * 32767).astype('<i2').tobytes())

    cache = {}
    for name, orig, desc in SOUNDS:
        p = cache.setdefault(orig, analyse(os.path.join(HERE, 'originals', orig + '.ogg')))
        for kind in KINDS:
            st = finish(stereo(resynth(p, kind), kind))
            write(name, kind, st)
            if name in REVERSED:
                write(REVERSED[name], kind, st[:, ::-1].copy())
    return [s[0] for s in SOUNDS]


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else '/tmp/cto_sfx'
    print(len(render_all(out)), 'sounds x 3 takes ->', out)
