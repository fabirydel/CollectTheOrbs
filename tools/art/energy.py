"""Energy bar art: a glass tube (front frame + dark back) and a tileable gold liquid, for the play-screen bar.

Run from the repo root:  python3 tools/art/energy.py
- res/hud/lifeBar{3,4,5}Lifes.png   tube frame drawn over the liquid (opaque ring, glass highlight, ticks)
- res/fx/tubeback{3,4,5}.png        dark glass interior, drawn behind the liquid
- res/hud/upgradeBarFull|Empty.png   40x24 upgrade 'battery' cells (same tube design)
- res/fx/liquid.png                 64x16 horizontally tileable gold liquid (scrolled by EnergyBar)
- res/fx/liquid_tip.png             soft glow at the liquid's leading edge
Geometry (logical px, bar height 36): end caps x 1.5..12 and W-12..W-1.5 (y 3..33), liquid interior x 13..W-13, y 10..26.
"""
import os, sys
import numpy as np
from PIL import Image, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
from neon import NEUTRAL

OUT_X0, OUT_Y0, OUT_Y1 = 2, 5, 31
IN_X0, IN_Y0, IN_Y1 = 13, 10, 26
GOLD = (255, 190, 46)


def smooth(e0, e1, x):
    t = np.clip((x - e0) / (e1 - e0), 0, 1)
    return t * t * (3 - 2 * t)


def capsule_sdf(w, h, x0, y0, x1, y1, K):
    """Signed distance (in logical px, negative inside) to a capsule x0..x1 / y0..y1, sampled on a w*K x h*K grid."""
    yy, xx = np.mgrid[0:h * K, 0:w * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    r = (y1 - y0) / 2
    cy = (y0 + y1) / 2
    cx = np.clip(x, x0 + r, x1 - r)
    return np.hypot(x - cx, y - cy) - r


def lerp(c0, c1, t):
    return tuple(int(round(c0[i] + (c1[i] - c0[i]) * t)) for i in range(3))


def tube_front(W, K):
    """Mechanical tube: metal rails above and below the glass, collars, and grooved end caps with bolts."""
    from PIL import ImageDraw
    H = 36
    S = K * 3
    img = Image.new('RGBA', (W * S, H * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    nc = NEUTRAL

    def R(x0, y0, x1, y1, fill):
        d.rectangle((x0 * S, y0 * S, x1 * S - 1, y1 * S - 1), fill=fill)

    def vgrad(x0, y0, x1, y1, top, bot):
        n = max(1, int(round((y1 - y0) * S)))
        for i in range(n):
            c = lerp(top, bot, i / max(1, n - 1))
            d.rectangle((x0 * S, y0 * S + i, x1 * S - 1, y0 * S + i), fill=c + (255,))

    X0, X1 = 13, W - 13           # glass interior (liquid) x range
    # rails above and below the glass
    vgrad(X0 - 1, 6.5, X1 + 1, 10, (150, 152, 158), (62, 64, 70))
    vgrad(X0 - 1, 26, X1 + 1, 29.5, (96, 98, 104), (44, 45, 50))
    R(X0 - 1, 6.5, X1 + 1, 7.0, nc + (255,))               # bright outer edge of the top rail
    R(X0 - 1, 29.0, X1 + 1, 29.5, tuple(int(c * 0.8) for c in nc) + (255,))
    R(X0 - 1, 9.6, X1 + 1, 10.0, (20, 20, 22, 255))         # shadow where the rail meets the glass
    R(X0 - 1, 26.0, X1 + 1, 26.4, (20, 20, 22, 255))

    def cap(x0, x1, left):
        y0, y1, c = 3.0, 33.0, 2.6
        pts = [(x0 + c, y0), (x1 - c, y0), (x1, y0 + c), (x1, y1 - c), (x1 - c, y1), (x0 + c, y1), (x0, y1 - c), (x0, y0 + c)]
        mask = Image.new('L', img.size, 0)
        ImageDraw.Draw(mask).polygon([(px * S, py * S) for px, py in pts], fill=255)
        body = Image.new('RGBA', img.size, (0, 0, 0, 0))
        bd = ImageDraw.Draw(body)
        n = int((y1 - y0) * S)
        for i in range(n):
            t = i / n
            c0 = lerp((128, 130, 136), (46, 47, 52), min(1, t * 1.15))
            bd.rectangle((x0 * S, y0 * S + i, x1 * S, y0 * S + i), fill=c0 + (255,))
        # vertical grooves (brushed / ribbed metal)
        gx = x0 + 1.2
        k = 0
        while gx < x1 - 0.6:
            bd.rectangle((gx * S, (y0 + 2) * S, (gx + 0.35) * S, (y1 - 2) * S), fill=(0, 0, 0, 90))
            bd.rectangle(((gx + 0.4) * S, (y0 + 2) * S, (gx + 0.7) * S, (y1 - 2) * S), fill=(255, 255, 255, 40))
            gx += 1.5
            k += 1
        # bevel: light along the outer edge, dark along the inner edge
        edge_x = x0 if left else x1
        bd.rectangle(((edge_x - (0 if left else 0.9)) * S, (y0 + 1.5) * S, (edge_x + (0.9 if left else 0)) * S, (y1 - 1.5) * S), fill=(255, 255, 255, 60))
        # bolt
        bx, by = (x0 + x1) / 2 + (0.4 if left else -0.4), 18.0
        bd.ellipse(((bx - 2.1) * S, (by - 2.1) * S, (bx + 2.1) * S, (by + 2.1) * S), fill=(24, 24, 26, 255))
        bd.ellipse(((bx - 1.6) * S, (by - 1.6) * S, (bx + 1.6) * S, (by + 1.6) * S), fill=(104, 106, 112, 255))
        bd.ellipse(((bx - 1.0) * S, (by - 1.3) * S, (bx + 0.4) * S, (by - 0.2) * S), fill=(210, 212, 218, 255))
        bd.line(((bx - 1.5) * S, (by + 0.1) * S, (bx + 1.5) * S, (by + 0.1) * S), fill=(30, 30, 32, 255), width=max(1, int(0.35 * S)))
        img.paste(body, (0, 0), mask)
        d2 = ImageDraw.Draw(img)
        d2.line([(px * S, py * S) for px, py in pts + [pts[0]]], fill=nc + (255,), width=max(1, int(0.8 * S)), joint='curve')

    cap(1.5, 12.0, True)
    cap(W - 12.0, W - 1.5, False)
    # collars where the caps meet the glass
    for cx0 in (12.0, W - 15.0):
        vgrad(cx0, 5.5, cx0 + 3.0, 30.5, (96, 98, 104), (36, 37, 41))
        R(cx0 + (0 if cx0 < W / 2 else 2.6), 5.5, cx0 + (0.4 if cx0 < W / 2 else 3.0), 30.5, (14, 14, 16, 255))
        R(cx0, 5.5, cx0 + 3.0, 6.0, nc + (255,))
        R(cx0, 30.0, cx0 + 3.0, 30.5, tuple(int(c * 0.7) for c in nc) + (255,))

    front = img.resize((W * K, H * K), Image.LANCZOS)
    # glass over the liquid: top specular, soft shade at the bottom
    arr = np.zeros((H * K, W * K, 4), dtype=np.float32)
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    inside = ((x >= 15) & (x <= W - 15) & (y >= 10) & (y <= 26)).astype(np.float32)
    spec = np.exp(-((y - 11.8) / 1.1) ** 2) * inside
    shade = smooth(21, 26, y) * inside
    arr[..., :3] = 255 * spec[..., None] + 0 * (1 - spec[..., None])
    arr[..., 3] = (spec * 0.4 + shade * 0.22 * (1 - spec)) * 255
    arr[..., :3] = np.where(shade[..., None] > spec[..., None], 0, 255)
    glass = Image.fromarray(arr.astype(np.uint8))
    out = Image.alpha_composite(glass, front)
    halo = N.glow(out, 1.6, NEUTRAL, 0.5)
    return Image.alpha_composite(halo, out)


def tube_back(W, K):
    H = 36
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    inside = ((x >= 12) & (x <= W - 12) & (y >= 10) & (y <= 26)).astype(np.float32)
    shade = 0.05 + 0.10 * smooth(10, 26, y)
    rgb = np.dstack([shade * 255, shade * 255, (shade + 0.01) * 255])
    return Image.fromarray(np.dstack([rgb, inside * 240]).astype(np.uint8))


def liquid_tile(K):
    """64x16 logical gold liquid, tileable horizontally."""
    w, h = 64, 16
    yy, xx = np.mgrid[0:h * K, 0:w * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K / h          # 0 top .. 1 bottom
    TAU = 2 * np.pi
    # vertical gradient: bright top -> rich gold -> deep amber bottom
    top = np.array([255, 236, 150], dtype=np.float32)
    mid = np.array([255, 190, 46], dtype=np.float32)
    bot = np.array([214, 112, 4], dtype=np.float32)
    t1 = smooth(0.0, 0.45, y)[..., None]
    t2 = smooth(0.35, 1.0, y)[..., None]
    rgb = top * (1 - t1) + mid * t1
    rgb = rgb * (1 - t2) + bot * t2
    # flowing bands (periodic in x so the tile repeats)
    band = 0.5 + 0.5 * np.sin(TAU * x / 64 * 2 + 0.9 * y)
    rgb = rgb * (0.9 + 0.14 * band[..., None])
    # bright energy streaks
    streak = np.exp(-((((x / 64 * 2 + 0.22 * y) % 1.0) - 0.5) / 0.07) ** 2) * 0.42
    rgb = rgb + streak[..., None] * np.array([95, 75, 25])
    # bubbles
    rng = np.random.RandomState(7)
    for _ in range(9):
        bx, by, br = rng.uniform(0, 64), rng.uniform(0.2, 0.85) * 16, rng.uniform(0.7, 1.4)
        for ox in (-64, 0, 64):
            d = np.hypot(x - (bx + ox), (y * 16) - by)
            ring = np.exp(-((d - br) / 0.22) ** 2) * 0.55
            rgb += ring[..., None] * np.array([255, 245, 200])
    return Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8)).convert('RGBA')


def liquid_tip(K):
    w, h = 12, 16
    yy, xx = np.mgrid[0:h * K, 0:w * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    along = np.exp(-((w - x) / 3.2) ** 2)
    across = np.exp(-(((y - h / 2) / (h / 2)) ** 2) * 1.2)
    a = along * across
    rgb = np.dstack([np.full_like(a, 255), np.full_like(a, 226), np.full_like(a, 130)])
    return Image.fromarray(np.dstack([rgb, a * 235]).astype(np.uint8))


def build():
    K = 4
    for cells, W in ((3, 102), (4, 122), (5, 143)):
        front = tube_front(W, K)
        front.save(os.path.join(N.OUT, 'hud', 'lifeBar%dLifes.png' % cells))
        N.logical_sizes['res/hud/lifeBar%dLifes.png' % cells] = (W, 36)
        back = tube_back(W, K)
        os.makedirs(os.path.join(N.OUT, 'fx'), exist_ok=True)
        back.save(os.path.join(N.OUT, 'fx', 'tubeback%d.png' % cells))
        N.logical_sizes['res/fx/tubeback%d.png' % cells] = (W, 36)
    liquid_tile(K).save(os.path.join(N.OUT, 'fx', 'liquid.png'))
    N.logical_sizes['res/fx/liquid.png'] = (64, 16)
    liquid_tip(K).save(os.path.join(N.OUT, 'fx', 'liquid_tip.png'))
    N.logical_sizes['res/fx/liquid_tip.png'] = (12, 16)


def cell(full, K=8):
    """One 40x24 upgrade 'battery': a short mechanical tube (metal end caps, rails, glass), gold liquid if full."""
    from PIL import ImageDraw
    W, H = 40, 24
    S = K * 2
    img = Image.new('RGBA', (W * S, H * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    nc = NEUTRAL

    def R(x0, y0, x1, y1, fill):
        d.rectangle((x0 * S, y0 * S, x1 * S - 1, y1 * S - 1), fill=fill)

    def vgrad(x0, y0, x1, y1, top, bot):
        n = max(1, int(round((y1 - y0) * S)))
        for i in range(n):
            c = lerp(top, bot, i / max(1, n - 1))
            d.rectangle((x0 * S, y0 * S + i, x1 * S - 1, y0 * S + i), fill=c + (255,))

    GX0, GX1, GY0, GY1 = 8, W - 8, 6.5, 17.5          # glass interior
    # rails
    vgrad(GX0 - 0.5, 4.0, GX1 + 0.5, GY0, (150, 152, 158), (62, 64, 70))
    vgrad(GX0 - 0.5, GY1, GX1 + 0.5, 20.0, (96, 98, 104), (44, 45, 50))
    R(GX0 - 0.5, 4.0, GX1 + 0.5, 4.4, nc + (255,))
    R(GX0 - 0.5, 19.6, GX1 + 0.5, 20.0, tuple(int(c * 0.8) for c in nc) + (255,))
    # liquid or empty glass
    liq = None
    if full:
        tile = Image.open(os.path.join(N.OUT, 'fx', 'liquid.png')).convert('RGBA')
        gw, gh = int((GX1 - GX0) * S), int((GY1 - GY0) * S)
        t2 = tile.resize((int(64 * S / 4 * 1.0), gh), Image.LANCZOS)
        liq = Image.new('RGBA', (gw, gh))
        for x in range(0, gw, t2.width):
            liq.paste(t2, (x, 0))
        img.paste(liq, (int(GX0 * S), int(GY0 * S)))
    else:
        R(GX0, GY0, GX1, GY1, (13, 13, 15, 255))
        vgrad(GX0, GY0, GX1, GY1, (22, 22, 25), (36, 36, 40))

    def cap(x0, x1, left):
        y0, y1, c = 1.5, 22.5, 2.0
        pts = [(x0 + c, y0), (x1 - c, y0), (x1, y0 + c), (x1, y1 - c), (x1 - c, y1), (x0 + c, y1), (x0, y1 - c), (x0, y0 + c)]
        mask = Image.new('L', img.size, 0)
        ImageDraw.Draw(mask).polygon([(px * S, py * S) for px, py in pts], fill=255)
        body = Image.new('RGBA', img.size, (0, 0, 0, 0))
        bd = ImageDraw.Draw(body)
        n = int((y1 - y0) * S)
        for i in range(n):
            c0 = lerp((128, 130, 136), (46, 47, 52), min(1, i / n * 1.15))
            bd.rectangle((x0 * S, y0 * S + i, x1 * S, y0 * S + i), fill=c0 + (255,))
        gx = x0 + 1.0
        while gx < x1 - 0.5:
            bd.rectangle((gx * S, (y0 + 1.5) * S, (gx + 0.35) * S, (y1 - 1.5) * S), fill=(0, 0, 0, 90))
            bd.rectangle(((gx + 0.4) * S, (y0 + 1.5) * S, (gx + 0.7) * S, (y1 - 1.5) * S), fill=(255, 255, 255, 40))
            gx += 1.5
        bx, by = (x0 + x1) / 2 + (0.3 if left else -0.3), 12.0
        bd.ellipse(((bx - 1.6) * S, (by - 1.6) * S, (bx + 1.6) * S, (by + 1.6) * S), fill=(24, 24, 26, 255))
        bd.ellipse(((bx - 1.2) * S, (by - 1.2) * S, (bx + 1.2) * S, (by + 1.2) * S), fill=(104, 106, 112, 255))
        bd.ellipse(((bx - 0.8) * S, (by - 1.0) * S, (bx + 0.3) * S, (by - 0.1) * S), fill=(210, 212, 218, 255))
        img.paste(body, (0, 0), mask)
        ImageDraw.Draw(img).line([(px * S, py * S) for px, py in pts + [pts[0]]], fill=nc + (255,), width=max(1, int(0.7 * S)), joint='curve')

    cap(0.8, 8.0, True)
    cap(W - 8.0, W - 0.8, False)
    for cx0 in (7.2, W - 9.6):
        vgrad(cx0, 3.2, cx0 + 2.4, 20.8, (96, 98, 104), (36, 37, 41))
        R(cx0, 3.2, cx0 + 2.4, 3.6, nc + (255,))
    front = img.resize((W * K, H * K), Image.LANCZOS)
    # glass specular band over the liquid / empty tube
    arr = np.zeros((H * K, W * K, 4), dtype=np.float32)
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x, y = (xx + 0.5) / K, (yy + 0.5) / K
    inside = ((x >= GX0) & (x <= GX1) & (y >= GY0) & (y <= GY1)).astype(np.float32)
    spec = np.exp(-((y - (GY0 + 1.7)) / 0.9) ** 2) * inside
    arr[..., :3] = 255
    arr[..., 3] = spec * (0.42 if full else 0.28) * 255
    out = Image.alpha_composite(Image.fromarray(arr.astype(np.uint8)), front)
    halo = N.glow(out, 1.2, NEUTRAL, 0.4)
    return Image.alpha_composite(halo, out)


def clean_cell(full, K=8, shift=0.0):
    """One 40x24 upgrade 'battery': a clean glass capsule with a neutral neon outline, gold liquid if full."""
    W, H = 40, 24
    ox0, oy0, ox1, oy1 = 1.5, 3.5, W - 1.5, H - 3.5      # outer capsule
    ix0, iy0, ix1, iy1 = 3.6, 5.6, W - 3.6, H - 5.6      # inner (liquid) capsule
    outer = capsule_sdf(W, H, ox0, oy0, ox1, oy1, K)
    inner = capsule_sdf(W, H, ix0, iy0, ix1, iy1, K)
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    nc = np.array(NEUTRAL, dtype=np.float32) / 255
    inside = smooth(0.3, -0.3, inner)
    rgb = np.zeros((H * K, W * K, 3), dtype=np.float32)
    alpha = np.zeros((H * K, W * K), dtype=np.float32)
    if full:
        tile = Image.open(os.path.join(N.OUT, 'fx', 'liquid.png')).convert('RGB').resize((64 * K // 2, 16 * K // 2 * 2), Image.LANCZOS)
        t = np.asarray(tile, dtype=np.float32) / 255
        th, tw, _ = t.shape
        v = np.clip(((y - iy0) / (iy1 - iy0) * th).astype(int), 0, th - 1)
        u = (xx.astype(int) + 7 * K + int(round(shift * tw))) % tw
        liq = t[v, u]
        rgb = liq
        alpha = inside
    else:
        shade = (13 + 24 * smooth(iy0, iy1, y)) / 255
        rgb = np.dstack([shade, shade, shade * 1.03])
        alpha = inside * 0.96
    # glass: dark body of the tube between the capsules, outline, specular band
    body = smooth(0.3, -0.3, outer) * (1 - inside)
    body_rgb = np.dstack([np.full_like(y, 0.1), np.full_like(y, 0.1), np.full_like(y, 0.115)])
    line_o = smooth(0.85, 0.0, np.abs(outer + 0.5))
    line_i = smooth(0.6, 0.0, np.abs(inner - 0.2)) * 0.55
    out_rgb = rgb * alpha[..., None] + body_rgb * body[..., None] * (1 - alpha[..., None])
    out_a = np.clip(alpha + body, 0, 1)
    out_rgb = out_rgb / np.maximum(out_a, 1e-4)[..., None]
    for ln in (line_o[..., None] * 1.1, line_i[..., None]):
        out_rgb = out_rgb * (1 - np.clip(ln, 0, 1)) + nc * np.clip(ln, 0, 1)
    out_a = np.clip(np.maximum(out_a, np.maximum(line_o, line_i)), 0, 1)
    spec = np.exp(-((y - (iy0 + 1.6)) / 0.9) ** 2) * inside * (0.4 if full else 0.28)
    out_rgb = out_rgb * (1 - spec[..., None]) + spec[..., None]
    out_a = np.clip(out_a + spec * (1 - out_a), 0, 1)
    img = Image.fromarray(np.dstack([np.clip(out_rgb, 0, 1) * 255, out_a * 255]).astype(np.uint8))
    halo = N.glow(img, 1.0, NEUTRAL, 0.5)
    return Image.alpha_composite(halo, img)


def build_cells():
    for name, full in (('upgradeBarFull', True), ('upgradeBarEmpty', False)):
        clean_cell(full).save(os.path.join(N.OUT, 'hud', name + '.png'))
        N.logical_sizes['res/hud/%s.png' % name] = (40, 24)


if __name__ == '__main__':
    N.set_scale(1)
    build()
    build_cells()
    N.write_manifest()
    print('done')
