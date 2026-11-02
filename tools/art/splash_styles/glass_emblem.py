"""Puffin Games splash, stained glass: a round window with the puffin in glass, its beak breaking out of the circle."""
from emblem import *
from stainedglass import StainedGlass, Light
from core import text_mask

class MaskLight(Light):
    """a glass opening of any shape (signed distance from a mask) in place of a pointed-arch light"""
    def __init__(self, g, mask):
        m = mask > 0.5
        s = ndi.distance_transform_edt(m) - ndi.distance_transform_edt(~m)
        self._sdf = (s - 0.5 * np.where(m, 1, -1) * 0 ).astype(np.float32)
        ys, xs = np.nonzero(m)
        self.g = g
        self.top, self.bottom = float(ys.min()), float(ys.max())
        self.height = self.bottom - self.top
        self.cx = float((xs.min() + xs.max()) / 2)
        self.a = (xs.max() - xs.min()) / 2.0
        self.R = self.a; self.ys = self.top; self.splay = 0; self.sill = 0; self.width = 2 * self.a
        self.mask = m.astype(np.float32)
    def sdf(self): return self._sdf
    def band(self, b0, b1):
        return np.clip(self._sdf - b0 + 0.5, 0, 1) * np.clip(b1 - self._sdf + 0.5, 0, 1)
    def inner(self, b=0.0): return np.clip(self._sdf - b + 0.5, 0, 1)

g = StainedGlass(W, H, seed=9, record=False, ambient=0.16)
g.wall('#8e8474', '#6b6358', course=(60, 82), block=(110, 240))
P = parts()
cx, cy = EC
Y, X = np.mgrid[0:H, 0:W].astype(np.float32)

# openings: the round window plus the beak notch, and a banner light below for the name
opening = np.maximum(disc(ER), P['beak'])
from PIL import ImageDraw as ID
bm = Image.new('L', (W, H), 0); ID.Draw(bm).rounded_rectangle((50, 1070, 670, 1330), 38, fill=255)
banner = np.asarray(bm, np.float32) / 255
L1, L2 = MaskLight(g, opening), MaskLight(g, banner)
for Lx in (L1, L2):
    g.win = np.maximum(g.win, np.clip(Lx._sdf + 0.5, 0, 1)); g.lights.append(Lx)
# stone rim around each opening: a slightly lighter dressed ring
for Lx, w in ((L1, 26), (L2, 18)):
    ring = (Lx._sdf < 0) & (Lx._sdf > -w)
    g.alb[ring] = (np.array([0.62, 0.56, 0.49], np.float32) ** 2.2) * (0.9 + 0.2 * g.stone_tex[ring])[:, None]
    g.relief[ring] = 1.08; g.kind[ring] = 3

# ---- sky: blue above, amber dusk below, a low sun
field = L1.mask
g.glass(disc(ER) * (Y < cy + 40), ['#4f86cf', '#3c72bf', '#5a92d6'], cell=40, streak=0.6)
g.glass(disc(ER) * (Y >= cy + 40), ['#f0b04a', '#e8963a', '#f4c45a', '#e07c3a'], cell=40, streak=0.6)
g.glass(g.circle(cx + 140, cy + 75, 48) * field, '#f6d24a', streak=0.3, seeds=0.6)
# ---- puffin
g.glass(P['body'], ['#1b2550', '#222d5e', '#16204a'], cell=34, streak=0.5, vary=1.2)
g.glass(P['face'], ['#e8eef0', '#dfe8ee'], cell=26, streak=0.3)
g.glass(P['breast'], ['#f1ecdc', '#e8e2d0'], cell=28, streak=0.3)
g.glass(P['eye'] * field, '#b3182c', streak=0.6, flashed=True)
g.glass(P['pupil'] * field, '#14100f', streak=0.2)
# ring border (rubies and golds in turn)
sites = np.array([(cx + (ER - 14) * np.cos(a), cy + (ER - 14) * np.sin(a)) for a in np.linspace(0, 2 * np.pi, 34, endpoint=False)], np.float32)
g.glass(L1.band(-2, 26) * disc(ER), ['#b3182c', '#f2c230', '#2456b0'], sites=sites, streak=0.5)
# beak, set over the border and out through the stone
g.glass(P['tip'], '#d9402a', streak=0.5, flashed=True)
g.glass(P['white'], '#f1ecdc', streak=0.3)
g.glass(P['orange'], '#e8821c', streak=0.4)
g.glass(P['yellow'], '#f2c230', streak=0.4)
g.glass(P['rosette'], '#e8821c', streak=0.3, seeds=0.4)
# ---- name banner
g.glass(banner, ['#173c8f', '#1d4aa0', '#14337f'], cell=44, streak=0.6)
ft = ImageFont.truetype(SUP + 'Georgia Bold.ttf', 92)
tm = np.maximum(text_mask(H, W, 'PUFFIN', ft, (W / 2, 1148), 'mm', spacing=6), text_mask(H, W, 'GAMES', ft, (W / 2, 1252), 'mm', spacing=6))
g.glass((tm > 0.5).astype(np.float32) * banner, '#f6e6b0', streak=0.2, seeds=0.3)

# ---- grisaille: cheek line, feather curves, rim details
t = np.linspace(0, 1, 30)
(x0, y0), (x1, y1), (x2, y2), (x3, y3) = uv(128, 98), uv(150, 98), uv(172, 104), uv(186, 112)
bz = np.stack([(1 - t) ** 3 * x0 + 3 * (1 - t) ** 2 * t * x1 + 3 * (1 - t) * t * t * x2 + t ** 3 * x3,
               (1 - t) ** 3 * y0 + 3 * (1 - t) ** 2 * t * y1 + 3 * (1 - t) * t * t * y2 + t ** 3 * y3], 1)
g.trace(bz, 4.0, taper=(0.1, 0.5))
for k in range(7):
    a0 = (3.9 + 0.12 * k)
    pts = np.array([(cx + 40 + 60 * k * 0.4, cy + 60 + 28 * k), (cx + 70 + 60 * k * 0.4, cy + 96 + 28 * k)], np.float32)
g.scratch_mask(P['body'] * 0)
g.shade(P['body'], offset=(9, 9), strength=0.35)
g.lead(width=7.0, rim=11.0)
g.weather(grime=0.5, pits=0.4)
g.daylight(strength=1.0, spill=1.0, halation=1.0)
im = g.composite().convert('RGBA')
save3(im, 'glass')
