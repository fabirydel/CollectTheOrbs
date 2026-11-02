"""Puffin Games splash, linocut print (black key block + red-orange and yellow spot blocks)."""
from common import *
from linocut import Linocut
from core import text_mask, load_font

L = Linocut(W, H, block=(26, 26, 694, 1372), seed=21, paper='#eee7d4')
BX0, BY0, BX1, BY1 = L.box
SEA = 835
P = puffin(95, 190, 540)

red = L.colour_block('#e0522b', offset=(3, -2))
yel = L.colour_block('#eab935', offset=(-2, 3))

# ---- sky: white wind streaks radiating round a big carved sun behind the bird
sun = (400, 420)
L.moon(sun[0], sun[1], 215, rings=(1.3, 1.6, 1.95, 2.3), shade=False)
sky = [(BX0, BY0), (BX1, BY0), (BX1, SEA), (BX0, SEA)]
ang = lambda x, y: np.degrees(np.arctan2(y - sun[1], x - sun[0])) + 90
L.flow(sky, ang, n=260, length=(40, 160), width=(2, 5),
       density=lambda x, y: float(np.clip(1.15 - np.hypot(x - sun[0], y - sun[1]) / 700, 0.05, 1)))

# ---- sea: wave lines, thin around the loading bar
sea = [(BX0, SEA), (BX1, SEA), (BX1, BY1), (BX0, BY1)]
L.clear(L.poly([(BX0, SEA - 2), (BX1, SEA - 2), (BX1, SEA + 5), (BX0, SEA + 5)]), chatter=0.2)
L.hatch(sea, angle=0, spacing=13, width=3.4, dash=(40, 140), gap=(8, 40), bend=lambda x, y: 5 * np.sin(x / 55 + y / 23),
        density=lambda x, y: float(np.clip(abs(y - 960) / 160 - 0.1, 0.0, 0.95)) * (0.5 if y < 960 else 1.0))
# rock the bird stands on (stays black, rimmed white)
rock = [(130, SEA + 5), (190, 735), (300, 722), (470, 726), (560, 770), (610, SEA + 5)]
L.outline(L.poly(rock), 5)
L.leave(L.poly(rock))
L.hatch(rock, angle=-15, spacing=12, width=3, dash=(30, 70), gap=(8, 24), density=lambda x, y: 0.55)

# ---- the puffin: black body with a white halo, feather cuts, white face and breast
L.outline(P['all'], 11)
L.leave(edge(P['all'], 5))
L.leave(P['body'])
bodycut = (P['body'] * (1 - P['face']) * (1 - P['breast'])).astype(np.float32)
L.hatch(bodycut, angle=62, spacing=11, width=3.2, dash=(20, 60), gap=(6, 28), bend=lambda x, y: 8 * np.sin(y / 38),
        density=lambda x, y: float(np.clip(0.18 + (x - 330) / 420 * 0.55 + (y - 400) / 700 * 0.25, 0.1, 0.9)))
L.clear(P['face'], chatter=0.1)
L.hatch(P['face'], angle=10, spacing=11, width=3, dash=(20, 60), gap=(10, 30), cut=False,
        density=lambda x, y: float(np.clip((y - 400) / 90, 0, 0.7) * 0.9))
L.clear(P['breast'], chatter=0.1)
L.hatch(P['breast'], angle=80, spacing=10, width=3, dash=(20, 50), gap=(10, 24), cut=False,
        density=lambda x, y: float(np.clip((x - 330) / 100, 0, 0.9)))
# eye: red ring, black pupil
L.leave(P['eye'] * 0, on=red)
L.clear(P['eye'])
L.leave(P['eye'], on=red)
L.leave(P['pupil'])
L.gouge([(210, 420), (270, 435), (330, 450)], 3.2, 'v', cut=False, taper=(0.1, 0.5)) if False else None
L.leave(P['cheek'] > 0.2 if False else P['cheek'])
# beak: cleared in the key, colour bands in the spot blocks, black ridge lines left standing
L.clear(P['beak'], chatter=0.0)
L.leave(np.maximum(P['tip'], P['orange']), on=red)
L.leave(P['yellow'], on=yel)
L.leave(P['rosette'], on=red)
bx = lambda u: 95 + u / 300 * 540
by = lambda v: 190 + v / 300 * 540
for (u0, v0, u1, v1) in ((58, 150, 52, 62), (73, 150, 66, 62), (92, 150, 87, 62)):
    L.gouge([(bx(u0), by(v0)), (bx(u1), by(v1))], 4, 'brush', cut=False, end=3, smooth=False, region=P['beak'])
L.outline_beak = None
ed = edge(P['beak'], 3, inner=True)
L.leave(ed)
# feet
for fx in (290, 390):
    L.leave(L.poly([(fx, 722), (fx + 52, 722), (fx + 60, 758), (fx + 36, 746), (fx + 14, 758), (fx - 6, 750)]), on=red)
    L.clear(L.poly([(fx - 4, 720), (fx + 56, 720), (fx + 64, 762), (fx - 10, 762)]) * 0)

# ---- lettering: white letters cut out of a black bar, cut with the knife
bar = L.rect(BX0 + 30, 1085, BX1 - 30, 1318)
L.leave(bar)
f = load_font(os.path.join(SUP, 'Impact.ttf'), 104) if False else ImageFont.truetype(SUP + 'Impact.ttf', 104)
tm = np.maximum(text_mask(H, W, 'PUFFIN', f, (W / 2, 1155), 'mm', spacing=6),
                text_mask(H, W, 'GAMES', f, (W / 2, 1255), 'mm', spacing=6))
L.clear(tm, chatter=0.0)
L.outline(bar, 3, cut=True)
L.speckle(None, n=500)
L.sign('1/1', 'Puffin Games', 'Fabi', y=1404, size=24)
L.chop(654, 1398, 30)
im = L.pull().convert('RGBA')
save(im, 'linocut')
