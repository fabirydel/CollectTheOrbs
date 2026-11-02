"""Puffin Games splash, retro synthwave."""
from common import *
from synthwave import Synthwave, MAGENTA, CYAN, PINK

HOR = 930
s = Synthwave(W, H, seed=1987, horizon=HOR, vp_x=W / 2, focal=640, eye=3.0, record=False)
s.sky()
s.stars(n=900, bright=16, y_max=HOR - 40)
s.sun(360, HOR - 20, 250, bands=8, band_top=0.36)
s.mountains(-1800, -420, 950, 1500, 90, cell=(60, 100), level=0.7) if False else None
s.mountains(-60, -9, 80, 130, 80, cell=(52, 85))
s.mountains(9, 60, 80, 130, 70, cell=(52, 85))
s.floor(MAGENTA, spacing=(2.2, 2.2), width=0.07)
s.chrome_text('PUFFIN', W / 2, 1115, 120, depth=14)
s.chrome_text('GAMES', W / 2, 1245, 120, depth=14)
base = s.composite(final=False).convert('RGBA')

P = puffin(95, 190, 540)
A = np.asarray(base, np.float32) / 255
rgb = A[..., :3]

def over(mask, col, a=1.0):
    global rgb
    m = (np.clip(mask * a, 0, 1))[..., None]
    rgb = rgb * (1 - m) + np.array(col, np.float32) / 255 * m

# neon halo behind the bird
halo = ndi.gaussian_filter(P['all'], 26)
rgb = 1 - (1 - rgb) * (1 - np.clip(halo * 1.1, 0, 1)[..., None] * np.array([1.0, 0.18, 0.62], np.float32) * 0.65)
body = P['body']
yy = np.mgrid[0:H, 0:W][0].astype(np.float32)
g = np.clip((yy - 330) / 450, 0, 1)[..., None]
over(body, (0, 0, 0)); 
rgb = rgb * (1 - body[..., None]) + (np.array([22, 10, 48], np.float32) * (1 - g) + np.array([60, 14, 84], np.float32) * g) / 255 * body[..., None]
over(P['breast'], (236, 226, 255)); over(P['breast'] * (np.mgrid[0:H, 0:W][1] > 340), (190, 150, 240), 0.8)
over(P['face'], (200, 210, 235)); over(P['face'] * (yy > 440), (150, 130, 220), 0.9)
over(P['tip'], (255, 70, 70)); over(P['white'], (255, 245, 235)); over(P['orange'], (255, 140, 30)); over(P['yellow'], (255, 215, 60))
over(P['rosette'], (255, 160, 50)); over(P['eye'], (255, 60, 110)); over(P['pupil'], (10, 5, 20))
# magenta/cyan rim light
ed = edge(P['all'], 4, inner=True)
xx = np.mgrid[0:H, 0:W][1].astype(np.float32)
mix = np.clip((xx - 120) / 520, 0, 1)[..., None]
rim = np.array([255, 60, 190], np.float32) * (1 - mix) + np.array([60, 230, 255], np.float32) * mix
rgb = np.clip(rgb * (1 - ed[..., None]) + rim / 255 * ed[..., None], 0, 1)
rgb = 1 - (1 - rgb) * (1 - ndi.gaussian_filter(ed, 5)[..., None] * rim / 255 * 0.6)
over(P['cheek'], (30, 10, 50), 0.8)
# feet
from PIL import ImageDraw as ID
f = Image.new('L', (W, H), 0); dd = ID.Draw(f)
for fx in (290, 390):
    dd.polygon([(fx, 730), (fx + 52, 730), (fx + 60, 764), (fx + 36, 752), (fx + 14, 764), (fx - 6, 756)], fill=255)
over(np.asarray(f, np.float32) / 255, (255, 120, 40))

# scanlines + slight vignette
sl = 1 - 0.07 * (np.mgrid[0:H, 0:W][0] % 3 == 0)[..., None]
rgb = np.clip(rgb * sl, 0, 1)
im = Image.fromarray((rgb * 255).astype(np.uint8)).convert('RGBA')
save(im, 'synthwave')
