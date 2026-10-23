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


def tint_red(sheet):
    """The red orb frames, pulled from pink toward pure red."""
    a = np.asarray(sheet, dtype=np.float32).copy()
    a[..., 1] *= 0.35
    a[..., 2] *= 0.42
    return Image.fromarray(a.clip(0, 255).astype(np.uint8))


def background(w, h, seed=3, stars=1.0):
    """Near-black space with white star dots of a few sizes (like the original icon)."""
    arr = np.zeros((h, w, 4), np.float32)
    arr[..., :3] = [3, 3, 5]
    arr[..., 3] = 255
    rng = random.Random(seed)
    area = w * h / 512.0 / 512.0
    for _ in range(int(900 * area * stars)):
        x, y = rng.randrange(w), rng.randrange(h)
        v = rng.uniform(70, 230)
        arr[y, x, :3] += v
    for _ in range(int(160 * area * stars)):
        x, y = rng.randrange(1, w - 1), rng.randrange(1, h - 1)
        v = rng.uniform(150, 255)
        arr[y, x, :3] += v
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            arr[y + dy, x + dx, :3] += v * 0.35
    for _ in range(int(18 * area * stars)):
        x, y = rng.randrange(2, w - 2), rng.randrange(2, h - 2)
        for dx in range(-1, 2):
            for dy in range(-1, 2):
                arr[y + dy, x + dx, :3] += 120 if (dx or dy) else 255
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


def flame(base, root, direction, length, width):
    """Engine plume like the game's: cyan with a white-hot core, tapering away from the nozzle at `root`."""
    import math
    dx, dy = direction
    n = 60
    for k in range(n):
        f = k / n
        px, py = root[0] + dx * length * f, root[1] + dy * length * f
        sz = max(2, int(width * (1 - f * 0.75) * 2))
        yy, xx = np.mgrid[0:sz, 0:sz].astype(np.float32)
        d = np.hypot(xx - sz / 2, yy - sz / 2) / (sz / 2)
        al = np.clip(1 - d, 0, 1) ** 1.3 * (1 - f) ** 1.4 * 255
        t = min(1.0, f * 2.2)
        col = (int(235 - 195 * t), int(250 - 80 * t), 255)
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


SHIP_NOZZLES = [(395, 62), (52, 238), (52, 422)]   # engine rears in the 859x668 ship art
SHIP_DIR = (-0.90, -0.43)                           # exhaust direction (away from the front)


def scene(w, h, orbs, ship_xy, ship_scale, scale=1.0, seed=3, tail_mul=2.3, plume=210):
    """The original blue fighter (tools/art/src/original_ship.png) with game-style plumes and neon orbs in the original positions."""
    import math
    k = scale
    im = background(w, h, seed=seed, stars=1.3)
    sheets = {'b': ((40, 190, 255), img('fx/orb_blue.png'), 4), 'r': ((255, 20, 36), tint_red(img('fx/orb_red.png')), 9)}
    for (hx, hy), (tx, ty), dia, c in orbs:
        col, sheet, fi = sheets[c]
        big = dia > 60
        length = math.hypot(hx - tx, hy - ty) * k * (1.6 if big else tail_mul)
        ang = math.degrees(math.atan2(hy - ty, hx - tx))
        im = comet(im, (hx * k, hy * k), col, length, ang, dia * k * (0.45 if big else 0.30), sheet, fi, int(dia * k * 1.78))
    ship = Image.open(os.path.join(os.path.dirname(__file__), 'src', 'original_ship.png')).convert('RGBA')
    sc = ship_scale * k
    ship = ship.resize((int(ship.width * sc), int(ship.height * sc)), Image.LANCZOS)
    ox, oy = ship_xy[0] * k, ship_xy[1] * k
    aura = N.radial(int(ship.width * 1.4), [(0, (40, 120, 255, 28)), (0.6, (30, 90, 230, 12)), (1.0, (20, 60, 200, 0))])
    im = add(im, aura, (ox + ship.width / 2, oy + ship.height / 2))
    for nx, ny in SHIP_NOZZLES:
        im = flame(im, (ox + nx * sc, oy + ny * sc), SHIP_DIR, plume * sc, 26 * sc)
    layer = Image.new('RGBA', im.size, (0, 0, 0, 0))
    layer.alpha_composite(ship, (0, 0)) if False else None
    big_layer = Image.new('RGBA', (im.width + 2 * ship.width, im.height + 2 * ship.height), (0, 0, 0, 0))
    big_layer.alpha_composite(ship, (int(ox) + ship.width, int(oy) + ship.height))
    layer = big_layer.crop((ship.width, ship.height, ship.width + im.width, ship.height + im.height))
    return Image.alpha_composite(im, layer)


def pad(mask, m=40):
    out = Image.new('L', (mask.width + 2 * m, mask.height + 2 * m), 0)
    out.paste(mask, (m, m))
    return out


def neon_border(size, inset=0.03, radius=0.17, stroke=0.012):
    """Gray-white neon rounded-rectangle outline, like the game's UI cards."""
    from PIL import ImageDraw
    N.set_scale(1)
    m = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(m)
    i = int(size * inset)
    d.rounded_rectangle((i, i, size - 1 - i, size - 1 - i), radius=int(size * radius), outline=255, width=max(2, int(size * stroke)))
    layer = UI.neon_text(m, UI.NEUTRAL, size / 170.0)
    return layer


def composition(size=512):
    im = scene_icon(size)
    return add(im, neon_border(size), (size / 2, size / 2))


def scene_icon(size=512):
    return scene(size, size, ICON_ORBS, (-82, 130), 0.69, size / 512.0, plume=210)


def feature(w=1024, h=500):
    im = scene(w, h, BANNER_ORBS, (29, 23), 0.65, 1.0, seed=9)
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
