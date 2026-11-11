"""Mechanical shutter art for the purchase animation (see core/.../handlers/MechDoor.java).

Three small pieces, drawn at their own aspect by the code so the shutter fits any card shape:
  res/fx/door_metal.png  brushed gunmetal plate (stretched to the plate; only horizontal detail, so stretching is safe)
  res/fx/door_grip.png   the grooved edge that leads the plate (same grooves as the energy bar's end caps)
  res/fx/door_screw.png  a slotted screw head

Run from the repo root:  python3 tools/art/door.py
"""
import math, os, random
from PIL import Image, ImageDraw, ImageFilter

OUT = os.path.join(os.path.dirname(__file__), '..', '..', 'android', 'assets', 'res', 'fx')
K = 4  # supersampling


def lerp(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(len(a)))


def metal():
    W, H = 64, 128
    random.seed(7)
    im = Image.new('RGBA', (W, H))
    px = im.load()
    top, mid, bot = (92, 96, 106), (58, 61, 70), (34, 36, 43)
    for y in range(H):
        t = y / (H - 1)
        c = lerp(top, mid, t / 0.45) if t < 0.45 else lerp(mid, bot, (t - 0.45) / 0.55)
        # brushed: faint horizontal streaks
        n = random.randint(-5, 5)
        for x in range(W):
            px[x, y] = (max(0, min(255, c[0] + n)), max(0, min(255, c[1] + n)), max(0, min(255, c[2] + n + 1)), 255)
    d = ImageDraw.Draw(im)
    # bevel: light along the top, shadow along the bottom
    d.rectangle([0, 0, W, 2], fill=(150, 155, 166, 255))
    d.rectangle([0, 3, W, 4], fill=(112, 116, 128, 255))
    d.rectangle([0, H - 3, W, H], fill=(16, 17, 21, 255))
    # two recessed rails across the plate
    for y0 in (int(H * 0.22), int(H * 0.74)):
        d.rectangle([0, y0, W, y0 + 1], fill=(18, 19, 24, 255))
        d.rectangle([0, y0 + 2, W, y0 + 3], fill=(118, 122, 134, 255))
    im.save(os.path.join(OUT, 'door_metal.png'))


def grip():
    W, H = 40, 128
    big = Image.new('RGBA', (W * K, H * K), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    for y in range(H * K):
        t = y / (H * K - 1)
        c = lerp((120, 124, 136), (44, 46, 54), t)
        d.line([(0, y), (W * K, y)], fill=c + (255,))
    # vertical grooves, like the energy bar's end caps
    n = 6
    gw = W * K / (n + 0.5)
    for i in range(n):
        x0 = int((i + 0.35) * gw)
        d.rectangle([x0, 4 * K, x0 + int(gw * 0.34), H * K - 4 * K], fill=(20, 21, 26, 255))
        d.rectangle([x0 + int(gw * 0.34), 4 * K, x0 + int(gw * 0.50), H * K - 4 * K], fill=(176, 181, 192, 255))
    d.rectangle([0, 0, W * K, 2 * K], fill=(168, 173, 184, 255))
    d.rectangle([0, H * K - 3 * K, W * K, H * K], fill=(14, 15, 18, 255))
    # the leading edge (right side): a dark lip
    d.rectangle([W * K - 3 * K, 0, W * K, H * K], fill=(12, 13, 16, 255))
    big.resize((W, H), Image.LANCZOS).save(os.path.join(OUT, 'door_grip.png'))


def screw():
    S = 32
    big = Image.new('RGBA', (S * K, S * K), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    c = S * K / 2
    r = S * K * 0.42
    d.ellipse([c - r - K, c - r - K, c + r + K, c + r + K], fill=(12, 13, 16, 255))
    for i in range(int(r), 0, -1):
        t = i / r
        col = lerp((176, 181, 192), (70, 73, 82), t)
        d.ellipse([c - i, c - i - (1 - t) * r * 0.25, c + i, c + i - (1 - t) * r * 0.25], fill=col + (255,))
    d.ellipse([c - r, c - r, c + r, c + r], outline=(24, 25, 30, 255), width=K)
    # slot
    d.rounded_rectangle([c - r * 0.72, c - r * 0.16, c + r * 0.72, c + r * 0.16], radius=K, fill=(14, 15, 18, 255))
    d.line([(c - r * 0.7, c + r * 0.2), (c + r * 0.7, c + r * 0.2)], fill=(150, 154, 166, 255), width=K // 2)
    big.resize((S, S), Image.LANCZOS).save(os.path.join(OUT, 'door_screw.png'))


if __name__ == '__main__':
    metal()
    grip()
    screw()
    print('wrote door_metal.png, door_grip.png, door_screw.png')
