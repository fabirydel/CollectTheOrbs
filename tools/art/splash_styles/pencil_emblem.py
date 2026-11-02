"""Puffin Games splash, warm pencil storybook emblem."""
from emblem import *
from pencilbook import PencilStorybook
from core import text_mask, blur as cblur

p = PencilStorybook(W, H, seed=14, keep_stages=False)
p.paper()
P = parts()
cx, cy = EC
Y, X = np.mgrid[0:H, 0:W].astype(np.float32)
D = disc(ER)

# warm paper glow behind the emblem
p.lift(np.clip(cblur(disc(ER + 70), 40), 0, 1), 'apricot', 0.28)
# dusk sky inside the disc
sk = D * (1 - P['all'])
p.hatch(sk, p.flow(-6, 6), ('dusk_sky', p.value(0, 1, cy - ER, cy + ER)), length=(60, 130), density=1.6, seed=1)
p.hatch(sk * 0.6, p.flow(-6, 6), ('dusk_sky', np.clip(p.value(0, 1, cy - ER, cy + ER) + 0.12, 0, 1)), length=(40, 90), density=1.0, seed=2)
p.glow(cx + 120, cy + 95, 150, strength=0.9, clip=sk, seed=3)
# distant hills at the foot of the disc
hill = D * (Y > cy + 150) * (1 - P['all'])
p.form(hill, ('lichen', 'sage', 'leaf', 'forest'), p.flow(-8, 12), warm='butter', cool='periwinkle', seed=4)

# puffin
lit = p.form(P['body'], ('slate', 'indigo', 'indigo', 'ultramarine'), p.contour(P['body'], 4), length=(14, 30), density=1.5, cool='periwinkle',
             edge='ultramarine', seed=5)
p.fur(P['body'] * 0.5, p.contour(P['body'], 4), 'periwinkle', length=(8, 16), density=0.5, clip=P['body'], seed=6)
p.form(P['face'], ('white', 'powder', 'periwinkle'), 10, length=(10, 22), density=1.2, warm='cream', edge='slate', seed=7)
p.form(P['breast'], ('white', 'cream', 'powder'), 80, length=(10, 22), density=1.2, warm='cream', edge='slate', seed=8)
for k, col in (('tip', ('coral', 'vermilion', 'red')), ('orange', ('apricot', 'orange', 'tangerine')), ('yellow', ('sun', 'butter', 'gold')),
               ('white', ('white', 'cream', 'powder'))):
    p.form(P[k], col, -8, length=(8, 18), density=1.4, seed=10 + len(k))
p.form(P['rosette'], ('apricot', 'orange', 'tangerine'), 0, length=(4, 8), density=1.6, seed=20)
p.edge(P['beak'], 'sienna', density=1.2, width=(1.6, 2.2), seed=21)
p.form(P['eye'], ('rose', 'coral', 'vermilion'), 0, length=(4, 8), density=2.0, seed=22)
p.form(P['pupil'], ('umber', 'sepia', 'sepia'), 0, length=(3, 6), density=2.0, seed=23)
p.hatch(P['cheek'], 0.0, 'umber', length=(8, 16), width=(1.4, 2.0), density=2.0, pressure=0.85, seed=24)
for fx in (290, 380):
    pass

# pencil ring around the emblem, double
ang = np.linspace(0, 2 * np.pi, 160)
for r_, w_ in ((ER + 2, 3.0), (ER + 14, 1.8)):
    pts = np.stack([cx + np.cos(ang) * r_, cy + np.sin(ang) * r_], 1)
    p.line(pts, 'umber', width=w_, pressure=0.7, alpha=0.8, gaps=0.05, seed=int(r_))
# beak redrawn over the ring where it breaks out
p.edge(P['beak'], 'sienna', density=1.4, width=(1.8, 2.4), seed=30)

# meadow at the foot, flowers, grass, small stars of light
p.grass(p.below(None, 1060) * (Y < 1075) * (1 - blur_ if False else 1), ('grass', 'leaf', 'yellowgreen'), height=(26, 70), seed=31) if False else None
meadow = ((Y > 880) & (Y < 1060)).astype(np.float32) * np.clip(1 - D, 0, 1)
meadow = meadow * np.clip((Y - 880) / 90, 0, 1)
p.grass(meadow, ('grass', 'leaf', 'yellowgreen'), height=(30, 80), seed=31)
p.flowers(meadow * 0.5, n=26, size=(5, 9), seed=32)

# wordmark in pencil
ft = ImageFont.truetype(SUP + 'Georgia Bold.ttf', 116)
tm = np.maximum(text_mask(H, W, 'PUFFIN', ft, (W / 2, 1150), 'mm', spacing=8), text_mask(H, W, 'GAMES', ft, (W / 2, 1275), 'mm', spacing=8))
p.hatch(tm, p.flow(-30, 3), ('brick', 'sienna'), length=(10, 18), width=(2.0, 3.0), density=3.4, pressure=0.9, opacity=0.5, alpha=0.9, passes=3, seed=40) if False else None
p.hatch(tm, p.flow(-30, 3), ['sienna', 'brown', 'umber'], length=(10, 18), width=(2.0, 3.0), density=3.4, pressure=0.9, opacity=0.5, alpha=0.9, passes=3, seed=40)
p.hatch(tm * 0.5, p.flow(60, 3), 'amber', length=(8, 14), width=(1.6, 2.2), density=1.6, pressure=0.8, opacity=0.5, seed=41)
p.finish()
im = p.composite().convert('RGBA')
save3(im, 'pencil')
