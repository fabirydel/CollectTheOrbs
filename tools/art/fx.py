"""Textures for the additive trail effects, and per-ship engine (nozzle) positions.

Run from the repo root:  python3 tools/art/fx.py
- res/fx/tail.png       orb comet tail, head at the bottom, white (tinted in code)
- res/fx/core.png       narrower, hotter tail core
- res/fx/plume.png      engine flame, nozzle at the top, white (tinted in code)
- res/fx/plume_core.png narrower flame core
- res/fx/spark_*.png    small glow dots for the collect burst
- res/ships/nozzles.txt "id x y w [x y w ...]" per ship, in the 128px ship frame (y down), read
                        from the original white-pixel trail art (BASE commit)
"""
import io, os, subprocess, sys
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import neon as N

BASE = 'fcb590d'
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
RES = os.path.join(ROOT, 'android', 'assets', 'res')


def save(arr, name):
    im = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), 'RGBA')
    path = os.path.join(RES, 'fx', name)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    im.save(path)
    N.logical_sizes['res/fx/' + name] = (im.width, im.height)


def streak(w, h, profile, falloff, head_bottom, power_core=1.0):
    """White RGBA streak. profile(t): half-width fraction at distance t (0..1) from the head;
    falloff(t): alpha along the length."""
    t = np.linspace(0, 1, h)[:, None]
    if not head_bottom:
        pass
    x = (np.arange(w)[None, :] - (w - 1) / 2) / (w / 2)
    hw = np.maximum(profile(t), 1e-3)
    across = np.exp(-(x / hw) ** 2 * 2.2)
    alpha = across * falloff(t)
    rgba = np.zeros((h, w, 4), dtype=np.float32)
    rgba[..., :3] = 255
    rgba[..., 3] = alpha * 255
    if head_bottom:
        rgba = rgba[::-1]
    return rgba


def spark(color):
    n = 32
    yy, xx = np.mgrid[0:n, 0:n]
    r = np.hypot(xx - (n - 1) / 2, yy - (n - 1) / 2) / (n / 2)
    core = np.clip(1 - r / 0.28, 0, 1) ** 1.2
    halo = np.clip(1 - r, 0, 1) ** 2.2
    rgba = np.zeros((n, n, 4), dtype=np.float32)
    for i, c in enumerate(color):
        rgba[..., i] = c + (255 - c) * core
    rgba[..., 3] = np.clip(halo * 0.9 + core, 0, 1) * 255
    return rgba


def build_textures():
    save(streak(64, 256, lambda t: 0.62 * (1 - t) ** 0.55 + 0.05, lambda t: (1 - t) ** 1.5, True), 'tail.png')
    save(streak(32, 256, lambda t: 0.40 * (1 - t) ** 0.7 + 0.03, lambda t: (1 - t) ** 1.9, True), 'core.png')
    save(streak(64, 256, lambda t: (0.5 + 0.5 * np.minimum(t * 7, 1)) * (1 - t) ** 0.8 * 0.95 + 0.04, lambda t: (1 - t) ** 1.25, False), 'plume.png')
    save(streak(32, 256, lambda t: (0.45 + 0.55 * np.minimum(t * 9, 1)) * (1 - t) ** 0.95 * 0.7 + 0.03, lambda t: (1 - t) ** 1.6, False), 'plume_core.png')
    for name, col in (('blue', N.CYAN), ('red', N.RED), ('gold', N.GOLD), ('purple', N.PURPLE)):
        save(spark(col), 'spark_%s.png' % name)


def build_nozzles():
    lines = []
    for sheet in range(5):
        blob = subprocess.check_output(['git', 'show', '%s:android/assets/res/ships/trace%d.png' % (BASE, sheet + 1)], cwd=ROOT)
        a = np.asarray(Image.open(io.BytesIO(blob)).convert('RGBA'))
        for f in range(15):
            fr = a[:, f * 128:(f + 1) * 128]
            m = (fr[..., 3] > 60) & (fr[..., :3].mean(-1) > 150)
            ys, xs = np.where(m)
            parts = []
            if len(xs):
                order = np.argsort(xs)
                xs_s, ys_s = xs[order], ys[order]
                groups, cur = [], [0]
                for i in range(1, len(xs_s)):
                    if xs_s[i] - xs_s[i - 1] > 7:
                        groups.append(cur)
                        cur = []
                    cur.append(i)
                groups.append(cur)
                for g in groups:
                    gx, gy = xs_s[g], ys_s[g]
                    parts += ['%.1f' % gx.mean(), '%.1f' % gy.max(), '%d' % (gx.max() - gx.min() + 1)]
            lines.append('%d %s' % (sheet * 15 + f, ' '.join(parts)))
    with open(os.path.join(RES, 'ships', 'nozzles.txt'), 'w') as fh:
        fh.write('\n'.join(lines) + '\n')


if __name__ == '__main__':
    N.set_scale(1)
    build_textures()
    build_nozzles()
    N.write_manifest()
    print('done')
