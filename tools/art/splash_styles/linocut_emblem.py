"""Puffin Games splash, linocut emblem: the upper puffin in a carved white sun, beak breaking out of the circle."""
from emblem import *
from linocut import Linocut
from core import text_mask

L = Linocut(W, H, block=(26, 26, 694, 1372), seed=33, paper='#eee7d4')
BX0, BY0, BX1, BY1 = L.box
P = parts()
red = L.colour_block('#e0522b', offset=(3, -2))
yel = L.colour_block('#eab935', offset=(-2, 3))
cx, cy = EC

# ground: tangential wind cuts and broken rings round the emblem
ring = lambda x, y: np.degrees(np.arctan2(y - cy, x - cx)) + 90
frame = [(BX0, BY0), (BX1, BY0), (BX1, BY1), (BX0, BY1)]
L.flow(frame, ring, n=420, length=(40, 170), width=(2, 5),
       density=lambda x, y: float(np.clip(1.1 - abs(np.hypot(x - cx, y - cy) - ER * 1.35) / 420, 0.05, 1)))
L.rings(cx, cy, [ER * 1.1, ER * 1.2], width=3.2, arc=(0.5, 1.6), gap=(0.1, 0.5))

# the emblem disc: cleared, black ring kept inside the rim
D = disc(ER)
L.clear(D, chatter=0.15, angle=-30)
L.leave(np.clip(disc(ER) - disc(ER - 9), 0, 1))
L.outline(disc(ER), 7)
# sky hatching in the disc, sparse, thicker toward the top
L.hatch(D * (1 - P['all']), angle=-8, spacing=15, width=3, dash=(30, 90), gap=(12, 40), cut=False,
        bend=lambda x, y: 6 * np.sin(x / 60), density=lambda x, y: float(np.clip((520 - y) / 420, 0, 0.6)))

# puffin (inside the disc: black body, white face and breast, ringed in black where it meets the white)
L.leave(edge(P['all'] * disc(ER - 8), 5))
L.leave(P['body'] * disc(ER - 8))
bodycut = (P['body'] * (1 - P['face']) * (1 - P['breast']) * disc(ER - 12)).astype(np.float32)
L.hatch(bodycut, angle=62, spacing=11, width=3.2, dash=(20, 60), gap=(6, 28), bend=lambda x, y: 8 * np.sin(y / 38),
        density=lambda x, y: float(np.clip(0.15 + (x - 300) / 420 * 0.55 + (y - 420) / 600 * 0.3, 0.1, 0.9)))
L.clear(P['face'], chatter=0.1)
L.hatch(P['face'], angle=10, spacing=11, width=3, dash=(20, 60), gap=(10, 30), cut=False,
        density=lambda x, y: float(np.clip((y - 500) / 90, 0, 0.7)))
L.clear(P['breast'], chatter=0.1)
L.hatch(P['breast'], angle=80, spacing=10, width=3, dash=(20, 50), gap=(10, 24), cut=False,
        density=lambda x, y: float(np.clip((x - 330) / 100, 0, 0.9)))
# eye and cheek line
L.clear(P['eye']); L.leave(P['eye'], on=red); L.leave(P['pupil']); L.leave(P['cheek'])
# beak: cut white in the key (even beyond the circle), colour in the spot blocks, black ridges and rim left standing
L.clear(P['beak'] , chatter=0.0)
L.leave(edge(P['beak'], 5) * (1 - disc(ER + 4)))  # keeps black ground tight outside; inside the disc the outline is the next line
L.leave(edge(P['beak'], 4) * disc(ER))
L.leave(np.maximum(P['tip'], P['orange']), on=red)
L.leave(P['yellow'], on=yel)
L.leave(P['rosette'], on=red)
for (u0, v0, u1, v1) in ((58, 150, 52, 62), (73, 150, 66, 62), (92, 150, 87, 62)):
    a, b = uv(u0, v0), uv(u1, v1)
    L.gouge([a, b], 5, 'brush', cut=False, end=4, smooth=False, region=P['beak'])
L.leave(edge(P['beak'], 3, inner=True))

# wordmark: white letters in a black bar
bar = L.rect(BX0 + 30, 1085, BX1 - 30, 1318)
L.clear(L.rect(BX0 + 20, 1075, BX1 - 20, 1328), chatter=0.0)
L.leave(L.rect(BX0 + 26, 1081, BX1 - 26, 1322))
L.clear(L.rect(BX0 + 40, 1095, BX1 - 40, 1308) * 0 + L.rect(BX0 + 38, 1093, BX1 - 38, 1310) - L.rect(BX0 + 44, 1099, BX1 - 44, 1304), chatter=0.0)
f = ImageFont.truetype(SUP + 'Impact.ttf', 104)
tm = np.maximum(text_mask(H, W, 'PUFFIN', f, (W / 2, 1160), 'mm', spacing=6), text_mask(H, W, 'GAMES', f, (W / 2, 1255), 'mm', spacing=6))
L.clear(tm, chatter=0.0)
L.speckle(None, n=450)
L.sign('1/1', 'Puffin Games', 'Fabi', y=1404, size=24)
L.chop(654, 1398, 30)
save3(L.pull().convert('RGBA'), 'linocut')
