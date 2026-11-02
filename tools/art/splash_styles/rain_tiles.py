"""Seamless rain tiles for the animated splash: three depths, scrolled along the slant by Splash.java and drawn additively."""
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
import os
W, H, SLANT = 720, 1440, 0.08
OUT = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'android', 'assets', 'res', 'splash')
TINT = np.array([0.72, 0.84, 1.0])
# name, count, length, width, alpha, blur, gain
DEPTHS = [('rain_far', 1800, (8, 20), 1, (0.2, 0.55), 0, 0.30),
          ('rain_mid', 500, (24, 55), 1, (0.3, 0.8), 0.35, 0.50),
          ('rain_near', 30, (150, 330), 3, (0.25, 0.55), 2.6, 0.70)]
for seed, (name, n, L, wd, a, bl, gain) in enumerate(DEPTHS):
    r = np.random.default_rng(seed + 11)
    im = Image.new('L', (W, H), 0); d = ImageDraw.Draw(im)
    for x, y, l, v in zip(r.uniform(0, W, n), r.uniform(0, H, n), r.uniform(*L, n), r.uniform(*a, n)):
        for dx in (-W, 0, W):
            for dy in (-H, 0, H):
                d.line([(x + dx, y + dy), (x + dx + SLANT * l, y + dy + l)], fill=int(v * 255), width=wd)
    if bl: im = im.filter(ImageFilter.GaussianBlur(bl))
    m = np.asarray(im, np.float32) / 255 * gain
    rgb = np.clip(m[..., None] * TINT * 255, 0, 255).astype(np.uint8)
    Image.fromarray(rgb).save(os.path.join(OUT, name + '.png'), optimize=True)
