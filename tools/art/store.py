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


def ship_layer(px, angle, ship_i=5):
    ships = img('ships/ships1.png')
    sh = frame(ships, ship_i, px)
    return sh.rotate(-angle, Image.BICUBIC, expand=True)


def flame(base, tip, angle, length, width):
    """Engine flame behind a ship pointing at `angle` (degrees clockwise from up): tapered additive glow, orange to white."""
    import math
    dx, dy = math.sin(math.radians(angle)), -math.cos(math.radians(angle))
    n = 40
    for k in range(n):
        f = k / n
        px, py = tip[0] - dx * length * f, tip[1] - dy * length * f
        sz = max(2, int(width * (1 - f * 0.8) * 2))
        yy, xx = np.mgrid[0:sz, 0:sz].astype(np.float32)
        d = np.hypot(xx - sz / 2, yy - sz / 2) / (sz / 2)
        al = np.clip(1 - d, 0, 1) ** 1.4 * (1 - f) * 255
        col = (255, int(190 - 80 * f), int(90 - 60 * f))
        arr = np.dstack([np.full((sz, sz), col[0]), np.full((sz, sz), col[1]), np.full((sz, sz), col[2]), al]).astype(np.uint8)
        base = add(base, Image.fromarray(arr), (px, py))
    return base


# Orb placements copied from the original icon (512 px) and banner (1024x500): head centre, end of the tail, orb diameter.
BANNER_ORBS = [
    ((754, 239), (607, 0), 118, 'r'), ((889, 421), (1024, 164), 122, 'b'),
    ((44, 84), (60, 50), 12, 'r'), ((146, 68), (193, 29), 18, 'r'), ((125, 118), (96, 89), 12, 'b'),
    ((225, 123), (193, 68), 18, 'b'), ((114, 143), (61, 118), 16, 'r'), ((504, 211), (514, 136), 14, 'r'),
    ((29, 256), (32, 193), 14, 'b'), ((256, 418), (268, 336), 26, 'r'), ((427, 451), (389, 371), 30, 'b'),
    ((25, 457), (121, 368), 28, 'b'),
]
ICON_ORBS = [
    ((367, 115), (290, 10), 104, 'r'), ((420, 272), (500, 95), 100, 'b'),
    ((27, 74), (47, 35), 14, 'r'), ((115, 49), (87, 22), 14, 'b'), ((241, 72), (212, 32), 18, 'b'),
    ((171, 93), (185, 55), 16, 'b'), ((125, 116), (150, 77), 18, 'r'), ((40, 142), (77, 117), 16, 'b'),
    ((120, 220), (137, 145), 26, 'r'), ((36, 244), (46, 190), 20, 'r'),
]


def scene(w, h, orbs, ship_xy, scale=1.0, seed=3):
    """The original blue fighter (tools/art/src/original_ship.png) with the neon orbs in the original positions."""
    import math
    k = scale
    im = background(w, h, seed=seed, stars=1.3)
    sheets = {'b': ((40, 190, 255), img('fx/orb_blue.png'), 4), 'r': ((255, 60, 90), img('fx/orb_red.png'), 9)}
    for (hx, hy), (tx, ty), dia, c in orbs:
        col, sheet, fi = sheets[c]
        length = math.hypot(hx - tx, hy - ty) * k
        ang = math.degrees(math.atan2(hy - ty, hx - tx))
        big = dia > 60
        im = comet(im, (hx * k, hy * k), col, length, ang, dia * k * (0.55 if big else 0.42), sheet, fi, int(dia * k * 1.78))
    ship = Image.open(os.path.join(os.path.dirname(__file__), 'src', 'original_ship.png')).convert('RGBA')
    ship = ship.resize((int(ship.width * k), int(ship.height * k)), Image.LANCZOS)
    aura = N.radial(int(ship.width * 1.5), [(0, (40, 120, 255, 70)), (0.6, (30, 90, 230, 30)), (1.0, (20, 60, 200, 0))])
    cx, cy = ship_xy[0] * k + ship.width / 2, ship_xy[1] * k + ship.height / 2
    im = add(im, aura, (cx, cy))
    layer = Image.new('RGBA', im.size, (0, 0, 0, 0))
    layer.alpha_composite(ship, (int(ship_xy[0] * k), int(ship_xy[1] * k))) if ship_xy[0] >= 0 and ship_xy[1] >= 0 else layer.paste(
        ship.crop((max(0, -int(ship_xy[0] * k)), max(0, -int(ship_xy[1] * k)), ship.width, ship.height)), (max(0, int(ship_xy[0] * k)), max(0, int(ship_xy[1] * k))))
    im = Image.alpha_composite(im, layer)
    return im


def pad(mask, m=40):
    out = Image.new('L', (mask.width + 2 * m, mask.height + 2 * m), 0)
    out.paste(mask, (m, m))
    return out


def composition(size=512):
    return scene(size, size, ICON_ORBS, (-45, 137), size / 512.0)


def feature(w=1024, h=500):
    im = scene(w, h, BANNER_ORBS, (35, 40), 1.0, seed=9)
    N.set_scale(2)
    f = UI.BMFont('fonts/prototype128blueWhite')
    size = 40 * 610 / f.width('Collect The Orbs', 40)
    t = UI.neon_text(pad(f.mask('Collect The Orbs', size)), UI.NEUTRAL, 2.5)
    return add(im, t, (705, 48))


def launcher_icons(master):
    """Legacy square launcher PNGs (replace android/res/drawable-*/img.png) from the store composition."""
    sizes = {'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144}
    for d, s in sizes.items():
        master.convert('RGBA').resize((s, s), Image.LANCZOS).save(os.path.join(ROOT, 'android', 'res', 'drawable-' + d, 'img.png'))


if __name__ == '__main__':
    crop = composition(1024).resize((512, 512), Image.LANCZOS)
    crop.convert('RGB').save(os.path.join(OUT, 'icon-512.png'))
    feature().convert('RGB').save(os.path.join(OUT, 'feature-1024x500.png'))
    launcher_icons(crop)
    print('done')
