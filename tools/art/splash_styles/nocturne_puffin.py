"""Puffin Games splash, hatched nocturne: a puffin on a moonlit rock, everything laid in short strokes."""
from common import *
from nocturne import Nocturne
from core import blur, smoothstep, text_mask, noise2d

BOX = (26, 26, 694, 1372)
HZ = 880
MOON = (545, 205)
P = puffin(95, 190, 540)
n = Nocturne(W, H, seed=12, keep_stages=False)
n.sheet(BOX)
sky = n.below(None, HZ + 3, invert=True)
sea = n.below(None, HZ)
Y = n.YY

# ---- sky
n.light(MOON[0], MOON[1], 330, 'starlight', 0.55)
f = n.flow(22, 8, 300, seed=1)
v = n.value(0.04, 0.6, 40, HZ, 0.08, 220, seed=2)
n.hatch(sky, f, ('sky', np.clip(v - 0.1, 0, 1)), (20, 40), (2.2, 3.2), 1.5, value=0.1, hue=0.05, pressure=0.97, dry=0.25, seed=3)
n.hatch(sky, f, ('sky', v), (16, 34), (1.8, 2.6), 1.6, value=0.11, hue=0.06, pressure=0.95, dry=0.35, seed=4)
dm = np.clip(1 - (np.hypot(n.XX - MOON[0], Y - MOON[1]) < 200), 0, 1).astype(np.float32)
n.stars(sky * dm, 130, (0.55, 1.3), bright=0.07, seed=8)
# moon
moon = np.clip(130 - np.hypot(n.XX - MOON[0], Y - MOON[1]) + 0.5, 0, 1).astype(np.float32)
mf = n.radial(MOON[0], MOON[1], offset=0.0, turb=5, seed=9)
n.hatch(moon, mf, '#d9deea', (10, 22), (2.0, 3.0), 3.0, value=0.08, hue=0.02, pressure=0.98, dry=0.2, seed=10)
n.hatch(moon * 0.55, mf, '#fff3d8', (8, 16), (1.6, 2.4), 1.2, value=0.1, pressure=0.9, dry=0.4, seed=11)
n.glow(sky, f, (10, 26), (1.8, 2.8), 1.1, power=0.7, reach=1.6, seed=12)

# ---- sea in long horizontals
sf = 0.0
sv = n.value(0.15, 0.55, HZ, H, 0.1, 90, seed=14)
n.hatch(sea, sf, ('sea', np.clip(sv * 0.8, 0, 1)), (30, 80), (2.2, 3.4), 1.6, jitter=0.03, bend=0.02, value=0.1, pressure=0.95, dry=0.4, seed=15)
n.hatch(sea * 0.55, sf, ('sea', np.clip(sv + 0.25, 0, 1)), (22, 60), (1.6, 2.4), 0.7, jitter=0.03, bend=0.02, value=0.1, pressure=0.85, dry=0.5, seed=16)
n.glitter(MOON[0], HZ + 2, 1220, w0=16, w1=120, power=0.0, density=0.0, seed=17) if False else None
# moon path on the water: pale, broken strokes
col = np.exp(-((n.XX - MOON[0] + 30 * np.sin(Y / 35.0)) / (24 + (Y - HZ) * 0.14)) ** 2) * sea * (1 - 0.5 * np.clip((Y - HZ) / 480, 0, 1)) * (1 - np.clip((Y - 1040) / 80, 0, 1))
brk = noise2d(H, W, 7, 2, 18) if False else None
n.hatch(col * 0.85, 0.0, '#c6d0e8', (18, 56), (1.8, 3.0), 1.4, jitter=0.03, bend=0.01, value=0.15, pressure=0.9, dry=0.4, seed=19)

# ---- rock
rock = n.poly([(120, HZ + 4), (190, 738), (300, 726), (470, 730), (560, 772), (625, HZ + 4)], smooth=False)
n.silhouette(rock, 'land', n.flow(-12, 6, 80, seed=20), val=n.value(0.3, 0.5, 726, HZ, 0.1, 50, seed=21), density=2.6, length=(10, 22),
             width=(1.8, 2.8), rim=0.8, rim_from=MOON, rim_colour='starlight', seed=22)

# ---- the puffin: dark mass with a moonlit rim, pale face and breast, warm beak
body = np.clip(P['body'] - P['face'] - P['breast'], 0, 1)
bf = n.flow(70, 10, 90, seed=23)
n.silhouette(P['body'], 'land', bf, val=n.value(0.25, 0.55, 190, 740, 0.12, 60, seed=24), density=2.8, length=(10, 24), width=(2.0, 3.0),
             rim=0.0, seed=25)
# moonlight rim on the upper-right of the body
sh = np.roll(np.roll(P['body'], -9, 1), -6, 0)
rim = np.clip(P['body'] - sh, 0, 1)
n.hatch(np.clip(blur(rim, 1.0) * 3, 0, 1), bf, '#9fb0d8', (6, 14), (1.4, 2.2), 1.6, value=0.12, pressure=0.9, dry=0.4, mode='light', seed=26)
ff = n.flow(10, 4, 60, seed=27)
n.hatch(P['face'], ff, '#c3cde6', (8, 16), (1.8, 2.6), 2.6, value=0.08, hue=0.03, pressure=0.97, dry=0.25, seed=28)
n.hatch(P['face'] * np.clip((Y - 405) / 70, 0, 1), ff, '#8e9cc6', (6, 12), (1.4, 2.2), 1.4, value=0.1, pressure=0.9, seed=29)
n.hatch(P['breast'], n.flow(80, 4, 60, seed=30), '#dfe5f3', (8, 16), (1.8, 2.6), 2.8, value=0.07, hue=0.02, pressure=0.97, dry=0.25, seed=31)
n.hatch(P['breast'] * np.clip((n.XX - 345) / 80, 0, 1), n.flow(80, 4, 60, seed=32), '#9aa6cc', (6, 12), (1.4, 2.2), 1.2, value=0.1, pressure=0.9, seed=33)
bkf = n.flow(-8, 3, 40, seed=34)
n.hatch(P['tip'], bkf, '#ff5a1a', (8, 16), (1.8, 2.8), 3.0, value=0.1, hue=0.03, pressure=0.98, dry=0.2, seed=35)
n.hatch(P['white'], bkf, '#ece0d0', (8, 16), (1.8, 2.8), 3.0, value=0.06, pressure=0.98, dry=0.2, seed=36)
n.hatch(P['orange'], bkf, '#ff8a2a', (8, 16), (1.8, 2.8), 3.0, value=0.1, pressure=0.98, dry=0.2, seed=37)
n.hatch(P['yellow'], bkf, '#ffc65c', (8, 16), (1.8, 2.8), 3.0, value=0.1, pressure=0.98, dry=0.2, seed=38)
n.hatch(P['rosette'], 0.0, '#ff9a3c', (5, 9), (1.6, 2.4), 3.0, pressure=0.98, seed=39)
n.hatch(P['eye'], n.radial(128 / 300 * 540 + 95, 92 / 300 * 540 + 190, offset=1.57, turb=1, seed=40), '#e8502a', (4, 8), (1.4, 2.0), 4.0, pressure=0.98, seed=41)
n.hatch(P['pupil'], 0.0, '#0a0c18', (4, 7), (1.6, 2.2), 5.0, pressure=0.99, seed=42)
n.hatch(P['cheek'], 0.0, '#0a0c18', (6, 12), (1.6, 2.4), 3.0, pressure=0.98, seed=43)
for fx in (290, 390):
    ft = n.poly([(fx, 730), (fx + 52, 730), (fx + 60, 764), (fx + 36, 752), (fx + 14, 764), (fx - 6, 756)])
    n.hatch(ft, 0.0, '#e0541a', (5, 10), (1.6, 2.4), 3.0, pressure=0.97, seed=44)

# ---- the wordmark in light strokes
ft = ImageFont.truetype(SUP + 'Impact.ttf', 112)
tm = np.maximum(text_mask(H, W, 'PUFFIN', ft, (W / 2, 1120), 'mm', spacing=8), text_mask(H, W, 'GAMES', ft, (W / 2, 1245), 'mm', spacing=8))
n.hatch(tm, n.flow(0, 3, 60, seed=45), '#e9e1c8', (8, 16), (1.8, 2.8), 4.5, value=0.08, hue=0.02, pressure=0.99, dry=0.2, jitter=0.05, bend=0.02, seed=46)

n.mount(plate=14, depth=0.06)
n.caption('Puffin Games', BOX[0] + 2, BOX[3] + 36, 24, anchor='ls', weight='light', seed=101)
n.bloom(1.0); n.grain(1.0)
im = n.composite().convert('RGBA') if hasattr(n, 'composite') else None
n.render() if False else None
save(im, 'nocturne')
