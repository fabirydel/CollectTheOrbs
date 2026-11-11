"""Purchase sound: a sci-fi door, cut from a real recording and timed to the shutter animation (MechDoor.java).

Source: tools/sfx/sources/airlock_freesound_607265.mp3 (RyanKingArt, CC0, https://freesound.org/s/607265/).
The shutter's timeline: plates slam shut at 0.13 s, the lock releases with a jolt at 0.46 s, the plates slide open
from 0.62 s and are fully open at 1.12 s.

Pieces taken from the recording (times in the source):
  4.31-5.00  a heavy clunk             -> the slam, peaking at about 0.10 s (a little early: phones play sounds late)
  6.40-7.90  hiss building to a burst  -> the pneumatic release, its peak (6.80) just before the jolt at 0.46 s
  8.30-8.62  a low thud                -> the plates hitting their stops, at 1.08 s

Run from the repo root:  python3 tools/sfx/door_sound.py [out.ogg]
Needs ffmpeg (its built-in Vorbis encoder) and numpy. The previous sound is kept in tools/sfx/originals/bought.ogg.
"""
import os, subprocess, sys, tempfile, wave
import numpy as np

HERE = os.path.dirname(__file__)
SRC = os.path.join(HERE, 'sources', 'airlock_freesound_607265.mp3')
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(HERE, '..', '..', 'android', 'assets', 'audio', 'bought.ogg')
SR = 44100
LENGTH = 1.55


def load():
    tmp = tempfile.mktemp(suffix='.wav')
    subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-i', SRC, '-ac', '1', '-ar', str(SR), tmp], check=True)
    w = wave.open(tmp)
    x = np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float64) / 32768
    os.remove(tmp)
    return x


def piece(x, t0, t1, fade_in=0.008, fade_out=0.05):
    p = x[int(t0 * SR):int(t1 * SR)].copy()
    a, b = int(fade_in * SR), int(fade_out * SR)
    p[:a] *= np.linspace(0, 1, a)
    p[-b:] *= np.linspace(1, 0, b) ** 2
    return p


def place(mix, p, at, gain):
    i = int(at * SR)
    n = min(len(p), len(mix) - i)
    mix[i:i + n] += p[:n] * gain


def main():
    x = load()
    mix = np.zeros(int(LENGTH * SR))
    place(mix, piece(x, 4.31, 5.00, fade_in=0.004, fade_out=0.25), 0.0, 0.9)   # slam
    place(mix, piece(x, 6.40, 7.90, fade_in=0.05, fade_out=0.45), 0.0, 1.0)   # hiss -> release -> slide
    place(mix, piece(x, 8.30, 8.62, fade_in=0.02, fade_out=0.12), 1.02, 1.1)  # stops
    n = int(0.12 * SR)
    mix[-n:] *= np.linspace(1, 0, n)
    mix *= 0.89 / np.abs(mix).max()
    tmp = tempfile.mktemp(suffix='.wav')
    with wave.open(tmp, 'wb') as w:
        w.setnchannels(2); w.setsampwidth(2); w.setframerate(SR)
        s = (mix * 32767).astype(np.int16)
        w.writeframes(np.column_stack([s, s]).tobytes())
    subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-i', tmp, '-c:a', 'vorbis', '-strict', '-2', '-b:a', '160k', OUT], check=True)
    os.remove(tmp)
    print('wrote', OUT, '%.2f s' % LENGTH)


if __name__ == '__main__':
    main()
