"""Energy orbs: a looping plasma-swirl animation for the falling orbs, and matching static counter icons.

Run from the repo root:  python3 tools/art/orb.py
- res/fx/orb_blue|red|gold|purple.png   16-frame loop, each frame 20x20 logical px, laid out in a row
- res/shapes/blueCounter|redCounter|lifeCounter.png   static frame of the same orb (64x64 logical)
Every animated parameter turns a whole number of cycles per loop, so the loop is seamless.
"""
import os, sys
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import neon as N

FRAMES = 16
CORE = 0.9  # orb radius as a fraction of the frame half-size (the rest is glow)


def smooth(e0, e1, x):
    t = np.clip((x - e0) / (e1 - e0), 0, 1)
    return t * t * (3 - 2 * t)


def orb_frame(n, col, p):
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float32)
    x = (xx - (n - 1) / 2) / (n / 2)
    y = (yy - (n - 1) / 2) / (n / 2)
    r = np.hypot(x, y) / CORE
    th = np.arctan2(y, x)
    tint = np.array(col, dtype=np.float32) / 255.0
    light = tint + (1 - tint) * 0.72
    TAU = 2 * np.pi

    # flowing plasma: three spiral arm layers turning at different whole-number speeds
    a1 = 0.5 + 0.5 * np.sin(2 * th - 6.0 * r + TAU * p)
    a2 = 0.5 + 0.5 * np.sin(3 * th + 5.0 * r - TAU * 2 * p + 0.8)
    a3 = 0.5 + 0.5 * np.sin(5 * th - 9.0 * r + TAU * 3 * p + 1.7)
    plasma = 0.55 * a1 ** 2 + 0.45 * a2 ** 2 + 0.35 * a3 ** 3
    plasma = 0.30 + 0.85 * plasma  # keep the whole ball lit; the swirl rides on top
    plasma *= 1.0 - 0.25 * smooth(0.2, 1.0, r)

    # deep tinted body, bands that run from the pure color up to a light tint only where they are strongest
    pl = np.clip(plasma, 0, 1.2)[..., None]
    hot = np.clip((plasma - 0.85) / 0.45, 0, 1)[..., None]
    body = tint[None, None, :] * 0.10 + (tint[None, None, :] * 1.15) * pl * 0.85 * (1 - hot) + (light[None, None, :] * 0.95) * hot
    core = np.exp(-(r / 0.25) ** 2) * (0.88 + 0.12 * np.sin(TAU * 2 * p))
    core_halo = np.exp(-(r / 0.5) ** 2) * 0.22
    rim = smooth(0.7, 1.0, r) ** 1.6
    edge = smooth(0.93, 0.985, r) * (1 - smooth(0.985, 1.02, r))
    spec = np.exp(-(((x + 0.28) / 0.2) ** 2 + ((y + 0.34) / 0.12) ** 2) * 1.6) * 0.4 * (r < 1)

    rgb_in = body + core_halo[..., None] * light[None, None, :] + core[..., None] * 1.0 + rim[..., None] * tint[None, None, :] * 0.5 + edge[..., None] * light[None, None, :] * 0.7
    rgb_in = rgb_in + spec[..., None]
    rgb_in = np.clip(rgb_in, 0, 1)

    inside = 1 - smooth(0.985, 1.03, r)
    halo = np.exp(-np.maximum(r - 1, 0) / 0.22) * 0.75 * (1 - inside)
    halo *= np.clip((1.0 - r * CORE) / 0.12 + 0.0, 0, 1) if False else np.clip((1.0 - r * CORE) / 0.14, 0, 1)  # reach exactly zero at the frame edge
    halo *= (0.9 + 0.1 * np.sin(TAU * 2 * p))
    rgb = rgb_in * inside[..., None] + tint[None, None, :] * (1 - inside[..., None])
    a = np.clip(inside + halo, 0, 1)
    out = np.dstack([rgb * 255, a * 255]).astype(np.uint8)
    return Image.fromarray(out)


def render(n_out, col, p, ss=3):
    big = orb_frame(n_out * ss, col, p)
    return big.resize((n_out, n_out), Image.LANCZOS)


def build():
    K = 4
    n = 20 * K
    for name, col in (('blue', N.CYAN), ('red', N.RED), ('gold', N.GOLD), ('purple', N.PURPLE)):
        sheet = Image.new('RGBA', (n * FRAMES, n), (0, 0, 0, 0))
        for f in range(FRAMES):
            sheet.paste(render(n, col, f / FRAMES), (f * n, 0))
        path = os.path.join(N.OUT, 'fx', 'orb_%s.png' % name)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        sheet.save(path)
        N.logical_sizes['res/fx/orb_%s.png' % name] = (20 * FRAMES, 20)
    for name, col in (('blueCounter', N.CYAN), ('redCounter', N.RED), ('lifeCounter', N.GOLD)):
        im = render(64 * K, col, 0.0)
        im.save(os.path.join(N.OUT, 'shapes', name + '.png'))
        N.logical_sizes['res/shapes/%s.png' % name] = (64, 64)


if __name__ == '__main__':
    build()
    N.write_manifest()
    print('done')
