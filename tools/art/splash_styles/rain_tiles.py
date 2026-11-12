"""Seamless rain tiles for the animated splash: three depths, scrolled along the slant by Splash.java and drawn as grey masks: multiplied by the neon glow
(so the streaks are lit by the tubes) and added faintly as ambient."""
import numpy as np
from PIL import Image, ImageDraw, ImageFilter
import os
W, H, SLANT = 720, 1440, 0.08
OUT = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'android', 'assets', 'res', 'splash')
TINT = np.array([0.55, 0.65, 0.85])
# name, count, length, width, alpha, blur, gain
# (a heavier downpour than the first version: more streaks at every depth, and thicker, brighter ones in the middle and near layers)
DEPTHS = [('rain_far', 2600, (10, 24), 1, (0.25, 0.6), 0, 0.30),
          ('rain_mid', 800, (28, 62), 2, (0.3, 0.8), 0.5, 0.50),
          ('rain_near', 60, (160, 340), 5, (0.3, 0.6), 2.8, 0.62)]
for seed, (name, n, L, wd, a, bl, gain) in enumerate(DEPTHS):
    r = np.random.default_rng(seed + 11)
    im = Image.new('L', (W, H), 0); d = ImageDraw.Draw(im)
    for x, y, l, v in zip(r.uniform(0, W, n), r.uniform(0, H, n), r.uniform(*L, n), r.uniform(*a, n)):
        for dx in (-W, 0, W):
            for dy in (-H, 0, H):
                d.line([(x + dx, y + dy), (x + dx + SLANT * l, y + dy + l)], fill=int(v * 255), width=wd)
    if bl: im = im.filter(ImageFilter.GaussianBlur(bl))
    m = np.asarray(im, np.float32) / 255 * gain
    g = np.clip(m * 255, 0, 255).astype(np.uint8)
    Image.fromarray(np.stack([g, g, g], axis=2)).save(os.path.join(OUT, name + '.png'), optimize=True)
