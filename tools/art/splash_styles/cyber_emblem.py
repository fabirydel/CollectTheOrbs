"""Puffin Games splash, cyberpunk: the emblem as a neon sign bolted to a rain-streaked wall."""
from puffin_paths import *
from cyberpunk import Cyberpunk, MAGENTA, CYAN, AMBER
from core import noise2d
from PIL import ImageFilter

c = Cyberpunk(W, H, seed=7, record=False, fog=400.0)
Z = 12.0
# the wall: dark wet concrete, panel seams, rain streaks
sl = c._box(0, 0, W - 1, H - 1)
rng = np.random.default_rng(3)
yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
base = 0.055 + 0.02 * noise2d(H, W, 90, 3, 5)
streak = noise2d(H, W, 6, 2, 9)[:, :]
streak = np.asarray(Image.fromarray(noise2d(H // 24 + 2, W, 5, 2, 11).astype(np.float32)).resize((W, H), Image.BILINEAR))
alb = (base + 0.025 * streak)[..., None] * np.array([0.8, 0.9, 1.2], np.float32)
seam = ((yy % 240) < 3) | ((xx % 360) < 3)
alb = alb * np.where(seam, 0.45, 1.0)[..., None]
c._put(sl, np.ones((H, W), np.float32), alb, 0.0, Z + 1.5, None)

# dark backing plate for the emblem, edged with a steel frame
plate = np.zeros((H, W), np.float32)
from PIL import ImageDraw as ID
pm = Image.new('L', (W, H), 0); ID.Draw(pm).ellipse((cx - ER - 36, cy - ER - 36, cx + ER + 36, cy + ER + 36), fill=255)
pa = np.asarray(pm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255
c._put(sl, pa, np.array([0.012, 0.012, 0.018], np.float32), 0.0, Z, None)
bm = Image.new('L', (W, H), 0); ID.Draw(bm).rounded_rectangle((60, 1060, 660, 1320), 34, fill=255)
ba = np.asarray(bm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255
c._put(sl, ba, np.array([0.012, 0.012, 0.018], np.float32), 0.0, Z, None)

PINK, TEAL, ORANGE, YEL, VIOLET = '#ff2a4e', '#22d3f5', '#ff8a1c', '#ffc21f', '#22d3f5'   # the red and blue of the game orbs
def tube(pts, col, r=3.4, closed=False, level=1.0, state='on'):
    c.neon_path(np.asarray(pts, np.float32), col, Z, r=r, level=level, closed=closed, state=state)

# ring: pink, with a second thin violet ring
tube(RING[:-1], PINK, r=5.2, closed=True)
tube(ring(0, 360, 400)[:-1] * 0 + np.stack([cx + (ER + 22) * np.cos(np.radians(np.linspace(0, 360, 400))), cy + (ER + 22) * np.sin(np.radians(np.linspace(0, 360, 400)))], 1)[:-1], VIOLET, r=2.2, closed=True, level=0.7)
# puffin body: cyan
tube(HEAD, TEAL, r=4.2)
tube(FRONT, TEAL, r=4.2)
tube(FACEP[(np.hypot(FACEP[:, 0] - cx, FACEP[:, 1] - cy) < ER - 4)], TEAL, r=3.2)
tube(BREAST, TEAL, r=3.2)
# beak: orange outline, yellow stripes
beak_outline = np.vstack([TE, BASE, LE])
tube(beak_outline, ORANGE, r=4.2, closed=True, level=0.9)
for tp, bt in stripe:
    tube([tp, bt], YEL, r=3.4)
# eye and cheek
tube(EYE, PINK, r=2.6)
tube(CHEEK, TEAL, r=2.6)
# lettering in neon tubes, PUFFIN in pink, GAMES in cyan, a flickering letter
c.neon_text('PUFFIN', W / 2, 1140, 112, PINK, Z, spacing=0.06)
c.neon_text('GAMES', W / 2, 1255, 112, TEAL, Z, spacing=0.06)
# plate borders
tube(np.array([(70, 1070), (650, 1070), (650, 1310), (70, 1310)]), VIOLET, r=2.4, closed=True, level=0.6)
c.rain(far=1800, mid=500, near=30, slant=0.08, rings=0)
c.mist(y=H * 0.8, h=200, density=0.1)
im = c.composite().convert('RGBA')
save3(im, 'cyber')
