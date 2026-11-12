"""Store the Help screen's pictures at 75% of their drawn resolution.

They are shown about three quarters of the screen wide, so their 1344 px width was far more than any phone
displays. The game keeps treating them at their original ("logical") size, listed in
android/assets/res/logical-sizes.txt, so nothing in the code changes.

Run once from the repo root:  python3 tools/art/shrink_help.py   (then oxipng the results)
It refuses to shrink a picture that is already below the logical size, so running it twice is harmless.
"""
import os
import numpy as np
from PIL import Image

RES = os.path.join(os.path.dirname(__file__), '..', '..', 'android', 'assets', 'res')
NAMES = ['inst', 'instEsp', 'items', 'itemsEsp', 'orbs', 'orbsEsp']
SCALE = 0.75

logical = {}
for line in open(os.path.join(RES, 'logical-sizes.txt')):
    p = line.split()
    if len(p) == 3:
        logical[p[0]] = (int(p[1]), int(p[2]))

for n in NAMES:
    path = os.path.join(RES, 'objects', n + '.png')
    im = Image.open(path).convert('RGBA')
    lw, lh = logical['res/objects/%s.png' % n]
    if im.width < lw:
        print(n, 'already shrunk, skipped')
        continue
    size = (round(lw * SCALE), round(lh * SCALE))
    # resize with premultiplied alpha, so transparent pixels do not darken the glowing edges
    a = np.asarray(im, np.float32) / 255
    a[..., :3] *= a[..., 3:4]
    pm = Image.fromarray((a * 255 + 0.5).astype(np.uint8), 'RGBA').resize(size, Image.LANCZOS)
    b = np.asarray(pm, np.float32) / 255
    alpha = np.maximum(b[..., 3:4], 1e-6)
    b[..., :3] = np.clip(b[..., :3] / alpha, 0, 1)
    before = os.path.getsize(path)
    Image.fromarray((b * 255 + 0.5).astype(np.uint8), 'RGBA').save(path, optimize=True)
    print(n, im.size, '->', size, '%.2f -> %.2f MB' % (before / 1e6, os.path.getsize(path) / 1e6))
