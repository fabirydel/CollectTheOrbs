"""Energy bar art: a glass tube (front frame + dark back) and a tileable gold liquid, for the play-screen bar.

Run from the repo root:  python3 tools/art/energy.py
- res/hud/lifeBar{3,4,5}Lifes.png   tube frame drawn over the liquid (opaque ring, glass highlight, ticks)
- res/fx/tubeback{3,4,5}.png        dark glass interior, drawn behind the liquid
- res/fx/liquid.png                 64x16 horizontally tileable gold liquid (scrolled by EnergyBar)
- res/fx/liquid_tip.png             soft glow at the liquid's leading edge
Geometry (logical px, bar height 36): outer capsule x 2..W-2, y 5..31; liquid interior x 8..W-8, y 10..26.
"""
import os, sys
import numpy as np
from PIL import Image, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
from neon import NEUTRAL

OUT_X0, OUT_Y0, OUT_Y1 = 2, 5, 31
IN_X0, IN_Y0, IN_Y1 = 8, 10, 26
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


def tube_front(W, K):
    H = 36
    outer = capsule_sdf(W, H, OUT_X0, OUT_Y0, W - OUT_X0, OUT_Y1, K)
    inner = capsule_sdf(W, H, IN_X0, IN_Y0, W - IN_X0, IN_Y1, K)
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    ring = smooth(0.4, -0.4, outer) * smooth(-0.4, 0.4, inner)          # opaque frame between the two capsules
    rgb = np.zeros((H * K, W * K, 3), dtype=np.float32)
    base = np.array([34, 35, 38], dtype=np.float32)
    shade = 0.75 + 0.5 * smooth(OUT_Y0, OUT_Y1, y)[..., None] * 0.0
    rgb[:] = base
    # brushed highlight on the ring top, darker toward the bottom
    top = 1 - smooth(OUT_Y0, (IN_Y0 + OUT_Y0) / 2 + 1, y)
    rgb += (np.array([120, 122, 128]) * top[..., None])
    alpha = ring
    # neon outlines: outer edge (bright neutral) and inner edge (softer)
    line_o = smooth(0.9, 0.0, np.abs(outer + 0.45)) * 1.0
    line_i = smooth(0.8, 0.0, np.abs(inner - 0.4)) * 0.7
    nc = np.array(NEUTRAL, dtype=np.float32) / 255
    rgb = rgb / 255
    rgb = rgb * (1 - line_o[..., None]) + nc * line_o[..., None] * 1.15
    rgb = rgb * (1 - line_i[..., None]) + nc * line_i[..., None] * 0.9
    alpha = np.clip(np.maximum(alpha, np.maximum(line_o, line_i)), 0, 1)
    # glass over the liquid: top specular band + faint tick marks per energy cell
    glass_in = smooth(0.2, -0.2, inner)
    spec = np.exp(-((y - (IN_Y0 + 2.2)) / 1.6) ** 2) * glass_in
    cells = None
    glass = np.zeros_like(alpha)
    glass_rgb = np.ones_like(rgb)
    glass_a = spec * 0.42
    out = np.dstack([rgb, np.ones_like(alpha)])
    base_img = out.copy()
    # composite: ring/lines first, then glass specular on the interior
    a_total = np.clip(alpha + glass_a * (1 - alpha), 0, 1)
    rgb_total = (rgb * alpha[..., None] + glass_rgb * (glass_a * (1 - alpha))[..., None]) / np.maximum(a_total, 1e-4)[..., None]
    # outer glow
    img = Image.fromarray(np.dstack([rgb_total * 255, a_total * 255]).astype(np.uint8))
    halo = N.glow(img, 1.6, NEUTRAL, 0.55)
    return Image.alpha_composite(halo, img)


def tick_overlay(W, K, cells):
    """Thin dividers between energy cells, drawn on the glass."""
    H = 36
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    x = (xx + 0.5) / K
    y = (yy + 0.5) / K
    inner_w = (W - IN_X0) - IN_X0
    a = np.zeros((H * K, W * K), dtype=np.float32)
    for c in range(1, cells):
        cx = IN_X0 + inner_w * c / cells
        a = np.maximum(a, np.exp(-((x - cx) / 0.35) ** 2) * ((y > IN_Y0) & (y < IN_Y1)))
    rgb = np.ones((H * K, W * K, 3), dtype=np.float32)
    return Image.fromarray(np.dstack([rgb * 255, a * 0.35 * 255]).astype(np.uint8))


def tube_back(W, K):
    H = 36
    inner = capsule_sdf(W, H, IN_X0 - 0.5, IN_Y0 - 0.5, W - IN_X0 + 0.5, IN_Y1 + 0.5, K)
    yy, xx = np.mgrid[0:H * K, 0:W * K].astype(np.float32)
    y = (yy + 0.5) / K
    inside = smooth(0.3, -0.3, inner)
    shade = 0.05 + 0.1 * smooth(IN_Y0, IN_Y1, y)       # slightly lighter toward the bottom
    rgb = np.dstack([shade * 255, shade * 255, (shade + 0.01) * 255])
    return Image.fromarray(np.dstack([rgb, inside * 235]).astype(np.uint8))


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
        front = Image.alpha_composite(tube_front(W, K), tick_overlay(W, K, cells))
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


if __name__ == '__main__':
    N.set_scale(1)
    build()
    N.write_manifest()
    print('done')
