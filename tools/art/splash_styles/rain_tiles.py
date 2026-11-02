"""Seamless rain tiles for the animated splash: three depths, scrolled along the slant by Splash.java and drawn with normal alpha blending
(a flat tint, so the neon behind never makes the rain look lit)."""
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
import os
W, H, SLANT = 720, 1440, 0.08
OUT = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'android', 'assets', 'res', 'splash')
TINT = np.array([0.55, 0.65, 0.85])
# name, count, length, width, alpha, blur, gain
DEPTHS = [('rain_far', 1800, (8, 20), 1, (0.2, 0.55), 0, 0.14),
          ('rain_mid', 500, (24, 55), 1, (0.3, 0.8), 0.35, 0.24),
          ('rain_near', 30, (150, 330), 3, (0.25, 0.55), 2.6, 0.32)]
for seed, (name, n, L, wd, a, bl, gain) in enumerate(DEPTHS):
    r = np.random.default_rng(seed + 11)
    im = Image.new('L', (W, H), 0); d = ImageDraw.Draw(im)
    for x, y, l, v in zip(r.uniform(0, W, n), r.uniform(0, H, n), r.uniform(*L, n), r.uniform(*a, n)):
        for dx in (-W, 0, W):
            for dy in (-H, 0, H):
                d.line([(x + dx, y + dy), (x + dx + SLANT * l, y + dy + l)], fill=int(v * 255), width=wd)
    if bl: im = im.filter(ImageFilter.GaussianBlur(bl))
    m = np.asarray(im, np.float32) / 255 * gain
    rgba = np.concatenate([np.broadcast_to(TINT * 255, (H, W, 3)), m[..., None] * 255], axis=2).astype(np.uint8)
    Image.fromarray(rgba, 'RGBA').save(os.path.join(OUT, name + '.png'), optimize=True)
