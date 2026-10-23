"""Play Store art and launcher icons: store/icon-512.png, store/feature-1024x500.png, android/res mipmaps.

Run from the repo root:  python3 tools/art/store.py
"""
import os, sys, random
import numpy as np
from PIL import Image, ImageFilter, ImageChops

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
import neon_ui as UI

ROOT = UI.ROOT
RES = os.path.join(ROOT, 'android', 'assets', 'res')
OUT = os.path.join(ROOT, 'store')
os.makedirs(OUT, exist_ok=True)


def img(path):
    return Image.open(os.path.join(RES, path)).convert('RGBA')


def frame(sheet, i, size):
    f = sheet.height
    return sheet.crop((i * f, 0, i * f + f, f)).resize((size, size), Image.LANCZOS)


def add(base, layer, xy):
    """Additive paste of an RGBA layer (premultiplied by its alpha) with its centre at xy."""
    x, y = int(xy[0] - layer.width / 2), int(xy[1] - layer.height / 2)
    b = np.asarray(base, dtype=np.float32).copy()
    l = np.asarray(layer, dtype=np.float32)
    x0, y0 = max(x, 0), max(y, 0)
    x1, y1 = min(x + layer.width, base.width), min(y + layer.height, base.height)
    if x1 <= x0 or y1 <= y0:
        return base
    sub = l[y0 - y:y1 - y, x0 - x:x1 - x]
    b[y0:y1, x0:x1, :3] += sub[..., :3] * (sub[..., 3:4] / 255.0)
    return Image.fromarray(b.clip(0, 255).astype(np.uint8))


def over(base, layer, xy):
    base = base.copy()
    base.alpha_composite(layer, (int(xy[0] - layer.width / 2), int(xy[1] - layer.height / 2)))
    return base


def background(w, h, seed=3, stars=1.0):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    r = np.hypot((xx - w / 2) / (w * 0.75), (yy - h * 0.55) / (h * 0.8))
    base = np.array([5, 6, 16], np.float32)
    mid = np.array([14, 18, 44], np.float32)
    t = np.clip(1 - r, 0, 1)[..., None] ** 1.5
    rgb = base * (1 - t) + mid * t
    im = Image.fromarray(np.dstack([rgb, np.full((h, w), 255)]).astype(np.uint8))
    rng = random.Random(seed)
    arr = np.asarray(im, dtype=np.float32).copy()
    for _ in range(int(w * h / 1400 * stars)):
        x, y = rng.randrange(w), rng.randrange(h)
        v = rng.uniform(60, 190)
        arr[y, x, :3] += v
    return Image.fromarray(arr.clip(0, 255).astype(np.uint8))


def comet(base, head, color, length, angle_deg, width, orb_frames, frame_i, orb_px):
    """Orb with a tapered additive tail pointing away from the direction of travel (angle in degrees, 0 = right)."""
    import math
    ca, sa = math.cos(math.radians(angle_deg)), math.sin(math.radians(angle_deg))
    n = 60
    for k in range(n, 0, -1):
        f = k / n
        px, py = head[0] - ca * length * f, head[1] - sa * length * f
        s = int(width * (1 - f * 0.85) * 2)
        if s < 2:
            continue
        a = (1 - f) ** 1.7
        blob = N.radial(s, [(0, color + (int(255 * a * 0.5),)), (1, color + (0,))]) if False else None
        g = Image.new('RGBA', (s, s), (0, 0, 0, 0))
        yy, xx = np.mgrid[0:s, 0:s].astype(np.float32)
        d = np.hypot(xx - s / 2, yy - s / 2) / (s / 2)
        al = np.clip(1 - d, 0, 1) ** 1.6 * a * 255
        arr = np.dstack([np.full((s, s), color[0]), np.full((s, s), color[1]), np.full((s, s), color[2]), al]).astype(np.uint8)
        base = add(base, Image.fromarray(arr), (px, py))
    halo = frame(orb_frames, frame_i, orb_px)
    base = add(base, halo, head)
    return base


def neon_shield(px, color=N.CYAN):
    N.set_scale(1)
    return N.shield(px / N.U, color).resize((px, px), Image.LANCZOS)


def composition(size=432, shield_color=0, ship_i=3, safe=True):
    """Ship in its force field with a blue and a red comet coming in (all inside the adaptive-icon safe circle)."""
    k = size / 432.0
    ships = img('ships/ships1.png')
    fields = img('shapes/forceFields.png')
    orb_b, orb_r = img('fx/orb_blue.png'), img('fx/orb_red.png')
    im = background(size, size)
    cx, cy = 216 * k, 238 * k
    # comets first so the shield is in front of their tails
    im = comet(im, (150 * k, 122 * k), (40, 190, 255), 70 * k, 105, 11 * k, orb_b, 4, int(62 * k))
    im = comet(im, (282 * k, 112 * k), (255, 60, 90), 78 * k, 78, 11 * k, orb_r, 9, int(62 * k))
    im = add(im, neon_shield(int(250 * k)), (cx, cy))
    im = over(im, frame(ships, ship_i, int(190 * k)), (cx, cy))
    return im


def pad(mask, m=40):
    out = Image.new('L', (mask.width + 2 * m, mask.height + 2 * m), 0)
    out.paste(mask, (m, m))
    return out


def feature(w=1024, h=500):
    im = background(w, h, seed=9, stars=1.3)
    ships = img('ships/ships1.png')
    fields = img('shapes/forceFields.png')
    orb_b, orb_r, orb_g = img('fx/orb_blue.png'), img('fx/orb_red.png'), img('fx/orb_gold.png')
    # streaming comets across the right side
    spots = [((640, 60), 'b', 100), ((780, 140), 'r', 105), ((905, 60), 'b', 98), ((960, 250), 'r', 100),
             ((700, 280), 'b', 103), ((850, 350), 'g', 97), ((590, 190), 'r', 102)]
    cols = {'b': ((40, 190, 255), orb_b, 4), 'r': ((255, 60, 90), orb_r, 9), 'g': ((255, 190, 40), orb_g, 6)}
    for (x, y), c, ang in spots:
        col, sheet, fi = cols[c]
        im = comet(im, (x, y + 70), col, 120, ang, 10, sheet, fi, 50)
    # ship + shield on the right-centre
    cx, cy = 790, 330
    im = add(im, neon_shield(220), (cx, cy))
    im = over(im, frame(ships, 3, 160), (cx, cy))
    # title
    N.set_scale(2)
    f = UI.BMFont('fonts/prototype128blueWhite')

    def title(text, target_w):
        size = 40
        size = size * target_w / f.width(text, size)
        return UI.neon_text(pad(f.mask(text, size)), UI.NEUTRAL, 2.5)

    t1 = title('Collect The', 430)
    t2 = title('Orbs', 330)
    im = add(im, t1, (270, 160))
    im = add(im, t2, (270, 290))
    return im


def launcher_icons(master):
    """Legacy square launcher PNGs (replace android/res/drawable-*/img.png) from the store composition."""
    sizes = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144}
    for d, s in sizes.items():
        master.convert('RGBA').resize((s, s), Image.LANCZOS).save(os.path.join(ROOT, 'android', 'res', 'drawable-' + d, 'img.png'))


if __name__ == '__main__':
    comp = composition(432)
    # the store icon crops the centre so the art fills the square
    big = composition(432 * 4)
    crop = big.crop((int(big.width * 0.1), int(big.height * 0.1), int(big.width * 0.9), int(big.height * 0.9))).resize((512, 512), Image.LANCZOS)
    crop.convert('RGB').save(os.path.join(OUT, 'icon-512.png'))
    feature().convert('RGB').save(os.path.join(OUT, 'feature-1024x500.png'))
    launcher_icons(crop)
    print('done')
