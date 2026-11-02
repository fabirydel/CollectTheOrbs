"""Puffin Games splash, ukiyo-e woodblock style."""
import sys
from common import *
from ukiyoe import Ukiyoe
from core import spline

u = Ukiyoe(W, H, seed=1831)
HOR = 820
# dawn sky: indigo wiped down from the top, sun-red rising from the horizon
sky = u.rect(0, 0, W, HOR + 2)
u.bokashi(sky, '#27487f', 0, 620)
u.bokashi(sky, '#e0723b', HOR, 330, strength=0.9)
# big sun disc behind the bird
u.block(u.circle(360, 470, 250), '#f0b24a', 0.9)
u.block(u.circle(360, 470, 250) * (u.YY > 560), '#e8803a', 0.5)
for (x0, x1, y, h) in [(20, 420, 250, 40), (0, 300, 700, 34)]:
    u.cloud_band(x0, x1, y, h, glow='#f0a27a' if y > 400 else None)
# distant island + sea
for (x0, x1, hh) in [(380, 720, 40), (0, 220, 24)]:
    isl = [(x0, HOR + 4)] + [(x, HOR - hh * np.sin(np.pi * (x - x0) / (x1 - x0)) ** 0.6) for x in np.linspace(x0, x1, 30)] + [(x1, HOR + 4)]
    u.block(u.poly(isl), '#56786a'); u.key(isl[1:-1], 2.2)
sea = u.rect(0, HOR, W, H)
u.block(sea, '#2c5d9c')
u.bokashi(sea, '#15356a', H, 960)
u.seigaiha(sea * (u.YY > HOR + 40), '#8db3de', r=34, width=2.4)

# the puffin, perched on a rock: carved colour blocks then key outline
P = puffin(95, 250, 540)
rock = [(150, HOR + 30), (230, 700), (330, 690), (470, 705), (560, HOR + 20), (600, HOR + 60), (110, HOR + 70)]
u.block(u.poly(rock), '#80603e'); u.bokashi(u.poly(rock), '#4f3a26', HOR + 70, 690); u.key(rock, 3.4, closed=True)
u.block(P['body'], '#23242c', 1.2); u.block(P['body'] * (u.YY < 440) * (u.XX > 220), '#171820', 0.7)
u.block(P['breast'], '#f6f1e3', 1.2)
u.block(P['face'], '#d9dde0', 1.2)
u.block(P['tip'], '#f0453a'); u.block(P['white'], '#f6f1e3'); u.block(P['orange'], '#f58a24'); u.block(P['yellow'], '#f6c53a')
u.block(P['rosette'], '#f58a24')
u.block(P['eye'], '#f0453a'); u.block(P['pupil'], '#1a1816')
u.block(np.maximum(edge(P['all'], 3), edge(P['beak'], 3, inner=True) * 0.9), '#2a2320', 0.97, key='__key__')
u.block(P['cheek'], '#2a2320', 0.9)
# feet
for fx in (300, 380):
    u.block(u.poly([(fx, 712), (fx + 46, 712), (fx + 52, 742), (fx + 30, 732), (fx + 10, 742), (fx - 8, 738)]), '#f0602a')

# swallows-ish birds
for (bx, by, s) in [(150, 110, 1.0), (205, 140, 0.7), (100, 150, 0.6)]:
    u.key(spline([(bx - 18 * s, by), (bx - 7 * s, by - 8 * s), (bx, by)], 6), 2.4)
    u.key(spline([(bx, by), (bx + 7 * s, by - 8 * s), (bx + 18 * s, by)], 6), 2.4)

# title: vertical-feel cartouche + horizontal wordmark
u.cartouche(618, 90, 76, 340, ' ')
u.seal(618, 1230, 62, '羽')
im = u.composite().convert('RGBA')
d = ImageDraw.Draw(im)
f = font('Copperplate.ttc', 78, 1)
for t, y in (('PUFFIN', 1090), ('GAMES', 1170)):
    w = d.textlength(t, font=f)
    d.text((W / 2 - w / 2, y), t, font=f, fill=(246, 231, 195, 255))
f2 = font('Copperplate.ttc', 30, 1)
for i, ch in enumerate('PUFFIN'):
    d.text((634, 108 + i * 52), ch, font=f2, fill=(42, 35, 32, 255))
save(im, 'ukiyoe')
