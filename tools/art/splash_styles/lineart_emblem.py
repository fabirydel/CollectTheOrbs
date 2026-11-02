"""Puffin Games splash, continuous line art: one unbroken golden line draws the emblem ring, the puffin and its beak."""
from emblem import *
from lineart import LineArt, AMBER, GOLD

la = LineArt(W, H, seed=5)
la.backdrop('night')
P = parts()
cx, cy = EC


def bez(p0, c1, c2, p3, n=40):
    t = np.linspace(0, 1, n)[:, None]
    p0, c1, c2, p3 = (np.array(v, np.float64) for v in (p0, c1, c2, p3))
    return (1 - t) ** 3 * p0 + 3 * (1 - t) ** 2 * t * c1 + 3 * (1 - t) * t * t * c2 + t ** 3 * p3


def px(a):
    a = np.asarray(a, np.float64)
    return np.stack([PX + a[:, 0] / 300 * PS, PY + a[:, 1] / 300 * PS], 1)


def inside(pts, m=4):
    return np.hypot(pts[:, 0] - cx, pts[:, 1] - cy) < ER - m


# --- geometry in the 300-unit puffin box
TOP_EDGE = bez((18, 128), (40, 100), (70, 78), (104, 68), 80)         # beak, tip -> top-right
LOW_EDGE = bez((108, 152), (74, 148), (40, 140), (18, 128), 80)       # beak, base -> tip
H1 = bez((92, 88), (108, 58), (160, 40), (200, 60))
H2 = bez((200, 60), (224, 72), (232, 102), (236, 132))
H3 = bez((236, 132), (244, 172), (270, 196), (286, 230))
H4 = bez((286, 230), (298, 258), (278, 292), (228, 297))
F1 = bez((132, 297), (122, 270), (118, 240), (118, 214))               # neck front, bottom -> up
F2 = bez((118, 214), (120, 184), (116, 158), (98, 142))
B1 = bez((118, 214), (140, 204), (168, 230), (180, 262))
B2 = bez((180, 262), (186, 278), (180, 290), (168, 297))
FACE = np.vstack([bez((106, 142), (94, 126), (92, 106), (98, 90)), bez((98, 90), (116, 66), (160, 58), (190, 74)),
                  bez((190, 74), (208, 86), (208, 110), (196, 124)), bez((196, 124), (180, 144), (138, 152), (106, 142))])
# face outline starting at the chin (106,142), going round and returning there; we go the long way up the back and down the front
FACE_UP = np.vstack([bez((106, 142), (138, 152), (180, 144), (196, 124))[::1][:0], ])

# --- ring with a gap where the beak crosses it
ang = np.degrees(np.arctan2(*(np.array([[y - cy, x - cx] for x, y in []]).T if False else (0, 0)))) if False else None
ring = lambda a0, a1, n=500: np.stack([cx + ER * np.cos(np.radians(np.linspace(a0, a1, n))), cy + ER * np.sin(np.radians(np.linspace(a0, a1, n)))], 1)
tip = uv(18, 128)
th0 = np.degrees(np.arctan2(tip[1] - cy, tip[0] - cx))
RING = ring(th0, th0 + 360, 1200)

# --- beak points in px
TE, LE = px(TOP_EDGE), px(LOW_EDGE)
BEAK_A = TE
BASE = px(np.array([(104, 68), (108, 152)], np.float64))
BASE = np.vstack([BASE[0], BASE[1]])
stripe = []
def edge_x(E, u):
    k = int(np.argmin(np.abs(E[:, 0] - (PX + u / 300 * PS)))); return E[k]
for u_top, u_bot in ((88, 92), (68, 73), (54, 58)):
    stripe.append((edge_x(TE, u_top), edge_x(LE, u_bot)))
# head and back
HEAD = px(np.vstack([H1[4:], H2, H3, H4]))
inr = inside(HEAD, 3)
k0 = int(np.argmax(inr))
rest = inr[k0:]
k_out = k0 + (int(np.argmin(rest)) if not rest.all() else len(rest))
HEAD = HEAD[k0:k_out]
# neck front
FRONT = px(np.vstack([F1, F2]))
inr = inside(FRONT, 3)
k_in = int(np.argmax(inr))
FRONT = FRONT[k_in:]
BREAST = px(np.vstack([B1, B2]))
inr = inside(BREAST, 3)
k = np.argmin(inr) if not inr.all() else len(BREAST)
BREAST = BREAST[:k]
ib = int(np.argmin(np.hypot(*(FRONT - px(np.array([[118, 214.0]]))[0]).T)))
FACEP = px(FACE)
# eye spiral then cheek
ex, ey = uv(128, 92)
t = np.linspace(0, 1, 90)
r_ = (PS / 300) * (9.5 - 5.5 * t)
a_ = np.pi + t * 2 * np.pi * 1.6
EYE = np.stack([ex + r_ * np.cos(a_), ey + r_ * np.sin(a_)], 1)
CHEEK = px(bez((128, 98), (150, 98), (172, 104), (186, 112)))

# angles of ring points nearest to Qb / Qf for the travel between
def ring_between(pa, pb):
    a0 = np.degrees(np.arctan2(pa[1] - cy, pa[0] - cx)); a1 = np.degrees(np.arctan2(pb[1] - cy, pb[0] - cx))
    d = (a1 - a0 + 540) % 360 - 180
    return ring(a0, a0 + d, 200)

parts_ = []
parts_.append(('swoop', LineArt.smooth([(24, 760), (50, 690), (90, 640), (RING[0][0] - 12, RING[0][1] + 6), tuple(RING[0])], 12)))
parts_.append(('ring', RING))
parts_.append(('beak_top', BEAK_A))
parts_.append(('base', BASE))
sz = []
for tp, bt in stripe:
    sz.append(tuple(bt)); sz.append(tuple(tp))
# zigzag stripes: base-bottom -> first stripe bottom, up, along top, down, ...
zig = []
cur_bottom = True
pts = [BASE[1]]
for i, (tp, bt) in enumerate(stripe):
    pts += [bt, tp] if i % 2 == 0 else [tp, bt]
parts_.append(('stripes', LineArt.smooth(pts, 6) if False else np.array(pts)))
parts_.append(('beak_low', np.vstack([LE[int(np.argmin(np.hypot(*(LE - pts[-1]).T))):]])))
parts_.append(('beak_up', TE[:]))
parts_.append(('head', HEAD))
parts_.append(('to_front', ring_between(HEAD[-1], FRONT[0])))
parts_.append(('front_a', FRONT[:ib + 1]))
parts_.append(('breast_out', BREAST))
parts_.append(('breast_back', BREAST[::-1]))
parts_.append(('front_b', FRONT[ib:]))
parts_.append(('face', FACEP))
parts_.append(('eye', np.vstack([FACEP[-1:], EYE]) if False else EYE))
parts_.append(('cheek', CHEEK))
Pth, marks = la.chain(parts_)
la.line(Pth, width=3.4, pressure=[(0, marks['swoop'], 1.0)])
la.draw_to(None)
la.accent(P['beak'] * 0.9, AMBER)
la.text('PUFFIN', W / 2, 1160, 104, spacing=22, font='display')
la.text('GAMES', W / 2, 1275, 104, spacing=22, font='display')
im = la.composite(tip=False).convert('RGBA')
save3(im, 'lineart')
