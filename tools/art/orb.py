"""Energy orbs: a looping plasma-swirl animation for the falling orbs, and matching static counter icons.

Run from the repo root:  python3 tools/art/orb.py
- res/fx/orb_blue|red|gold|purple.png   16-frame loop, each frame 32x32 logical px (orb body 18 px + glow and corona), in a row
- res/shapes/blueCounter|redCounter|lifeCounter.png   static frame of the same orb (64x64 logical)
Every animated parameter turns a whole number of cycles per loop, so the loop is seamless.
"""
import os, sys
import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(__file__))
import neon as N

FRAMES = 16
SOFTNESS = 0.6  # 0 = the original hard bright rim, 1 = fully soft edge
CORE = 0.9  # orb radius as a fraction of the frame half-size (the rest is glow)


def smooth(e0, e1, x):
    t = np.clip((x - e0) / (e1 - e0), 0, 1)
    return t * t * (3 - 2 * t)


def orb_frame(n, col, p, body=0.5625, corona=True, softness=None):
    sf = SOFTNESS if softness is None else softness
    """One frame. `body` = orb radius as a fraction of the half-frame; the rest of the canvas holds the glow
    and the corona wisps, which wrap around the orb and flow with whole-number cycles per loop."""
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float32)
    x = (xx - (n - 1) / 2) / (n / 2)
    y = (yy - (n - 1) / 2) / (n / 2)
    rf = np.hypot(x, y)            # radius in half-frame units
    r = rf / body                  # radius in orb radii
    th = np.arctan2(y, x)
    tint = np.array(col, dtype=np.float32) / 255.0
    light = tint + (1 - tint) * 0.72
    TAU = 2 * np.pi

    # plasma inside the body (same flow as before)
    a1 = 0.5 + 0.5 * np.sin(2 * th - 6.0 * r + TAU * p)
    a2 = 0.5 + 0.5 * np.sin(3 * th + 5.0 * r - TAU * 2 * p + 0.8)
    a3 = 0.5 + 0.5 * np.sin(5 * th - 9.0 * r + TAU * 3 * p + 1.7)
    plasma = 0.55 * a1 ** 2 + 0.45 * a2 ** 2 + 0.35 * a3 ** 3
    plasma = 0.30 + 0.85 * plasma
    plasma *= 1.0 - 0.25 * smooth(0.2, 1.0, r)
    pl = np.clip(plasma, 0, 1.2)[..., None]
    hot = np.clip((plasma - 0.85) / 0.45, 0, 1)[..., None]
    body_rgb = tint[None, None, :] * 0.10 + (tint[None, None, :] * 1.15) * pl * 0.85 * (1 - hot) + (light[None, None, :] * 0.95) * hot
    core = np.exp(-(r / 0.25) ** 2) * (0.88 + 0.12 * np.sin(TAU * 2 * p))
    core_halo = np.exp(-(r / 0.5) ** 2) * 0.22
    rgb_in = np.clip(body_rgb + core_halo[..., None] * light[None, None, :] + core[..., None], 0, 1)

    # edge: sf = 0 is the original crisp rim, sf = 1 fades the plasma out toward the edge
    e0 = 0.985 + (0.80 - 0.985) * sf
    e1 = 1.03 + (1.10 - 1.03) * sf
    body_a = 1 - smooth(e0, e1, r)
    hard = 1.0 - sf
    rim_ring = smooth(0.93, 0.985, r) * (1 - smooth(0.985, 1.02, r)) * 0.7 * hard
    rim_fill = smooth(0.7, 1.0, r) ** 1.6 * 0.5 * hard

    rgb_in = np.clip(rgb_in + rim_fill[..., None] * tint[None, None, :] + rim_ring[..., None] * light[None, None, :], 0, 1)

    # glow that hugs the orb and a wider breathing aura
    breathe = 0.9 + 0.1 * np.sin(TAU * 2 * p)
    g0 = 1.0 + (0.8 - 1.0) * sf
    tau = 0.20 + (0.24 - 0.20) * sf
    glow_near = np.exp(-np.maximum(r - g0, 0) / tau) * (0.72 + (0.7 - 0.72) * sf)
    glow_far = np.exp(-np.maximum(r - g0, 0) / 0.5) * 0.2
    glow = (glow_near + glow_far) * breathe

    # corona: tongues of energy that travel around the orb and lick outward
    tongues = np.zeros_like(r)
    if corona:
        for m, spd, ph0, w, curl in ((3, 1, 0.3, 1.0, 0.6), (5, -1, 1.9, 0.7, -0.5)):
            # the angle is twisted by the radius, so each wisp curls around the orb as it reaches out
            twist = th - curl * np.maximum(r - 1.0, 0)
            T = np.clip(np.sin(m * twist + TAU * spd * p + ph0), 0, 1) ** 2
            reach = 0.14 + 0.2 * T
            radial = np.exp(-((r - 1.0) / np.maximum(reach, 0.05)) ** 2 * 1.6) * (r > 0.85)
            tongues += w * T * radial
        tongues = np.clip(tongues * 0.2, 0, 0.5)

    energy = np.clip(glow * 0.8 + tongues * 0.95, 0, 1.4)
    out_rgb = np.zeros(r.shape + (3,), dtype=np.float32)
    hotline = np.clip(tongues - 0.55, 0, 1)
    for i in range(3):
        t = tint[i]
        l = light[i]
        out_rgb[..., i] = np.clip(t * (0.55 + 0.45 * np.clip(energy, 0, 1)) + (l - t) * 0.9 * hotline + 0.15 * hotline, 0, 1)

    a_out = np.clip(energy, 0, 1)
    # composite body over the outer energy
    ba = body_a[..., None]
    rgb = rgb_in * ba + out_rgb * (1 - ba)
    alpha = np.clip(body_a + a_out * (1 - body_a), 0, 1)
    # everything reaches exactly zero at the frame edge
    alpha = alpha * (1 - smooth(0.82, 1.0, rf))
    return Image.fromarray(np.dstack([rgb * 255, alpha * 255]).astype(np.uint8))


def render(n_out, col, p, ss=3, **kw):
    big = orb_frame(n_out * ss, col, p, **kw)
    return big.resize((n_out, n_out), Image.LANCZOS)


FRAME = 32  # logical px per frame; the visible orb body is 18 px of it (see Fx.ORB_DRAW_SCALE)


def build():
    K = 4
    n = FRAME * K
    for name, col in (('blue', N.CYAN), ('red', N.RED), ('gold', N.GOLD), ('purple', N.PURPLE)):
        sheet = Image.new('RGBA', (n * FRAMES, n), (0, 0, 0, 0))
        for f in range(FRAMES):
            sheet.paste(render(n, col, f / FRAMES), (f * n, 0))
        path = os.path.join(N.OUT, 'fx', 'orb_%s.png' % name)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        sheet.save(path)
        N.logical_sizes['res/fx/orb_%s.png' % name] = (FRAME * FRAMES, FRAME)
    # static fallbacks for the counter sprites (the menu draws the animated frames instead)
    for name, col in (('blueCounter', N.CYAN), ('redCounter', N.RED), ('lifeCounter', N.GOLD)):
        im = render(64 * K, col, 0.0, body=0.9, corona=False)
        im.save(os.path.join(N.OUT, 'shapes', name + '.png'))
        N.logical_sizes['res/shapes/%s.png' % name] = (64, 64)


if __name__ == '__main__':
    build()
    N.write_manifest()
    print('done')
