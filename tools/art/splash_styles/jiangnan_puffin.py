"""Puffin Games splash, Jiangnan garden: a puffin on a garden stone by a spring pond."""
from common import *
from jiangnan import JiangnanGarden

g = JiangnanGarden(W, H, seed=8, keep_stages=False)
HZ, BANK = 600, 730
g.camera(horizon=HZ, eye=3.0, focal=820, cx=W / 2)
r = np.random.default_rng(5)

# sky and hills
g.sky(top='#7ea4c0', bottom='#cfdcd6', y1=560, clouds=0.8, seed=1)
g.hills([(-40, 330), (140, 300), (320, 262), (500, 306), (720, 280), (780, 300)], base=BANK, colour='#86a6a6', far=0.66, seed=2)
g.haze(280, 480, 620, strength=0.4, seed=3)
g.hills([(-40, 420), (200, 388), (420, 410), (620, 372), (780, 400)], base=BANK, colour='#7a9a90', far=0.48, seed=4)
g.haze(380, 520, 640, strength=0.45, seed=5)
# distant trees
for i, x in enumerate(np.arange(-30, 780, 70)):
    g.foliage(g.crown(x, 540 + r.uniform(-14, 8), r.uniform(100, 150), r.uniform(50, 80), n=8, seed=100 + i), 'far', leaf=(2.5, 4.5),
              density=0.9, far=0.6, highlights=0.4, edge=0.6, seed=200 + i)
g.haze(470, 580, 650, strength=0.3, seed=6)
for i, (x, y, w, h, rp) in enumerate(((120, 470, 220, 150, 'jade'), (360, 440, 260, 180, 'spring'), (600, 470, 240, 160, 'deep'))):
    g.foliage(g.crown(x, y, w, h, n=20, size=(0.15, 0.34), seed=300 + i), rp, leaf=(3.0, 5.4), density=1.1, far=0.3, seed=400 + i)
g.haze(500, 620, 690, strength=0.2, seed=7)

# whitewashed garden wall with a moon gate onto a bright inner garden, a leak window on the right
g.wall(0, 560, 720, 690, 'plaster', seed=60)
g.stonework(0, 680, 720, 700, course=(12, 12), block=(40, 80), damp=0.2, seed=61)
g.round_window(92, 628, 52, ring=7, inside=None, ground=684, reveal=5)
g.round_window(655, 600, 24, ring=6, pattern='ice', cell=10, back='#dfe4dc')
g.dapple(0, 560, 720, 690, amount=0.3, scale=22, cover=0.55, falloff=(100, 560, 400), seed=65)
g.coping(-6, 726, 560, h=22, seed=62)
g.foliage(np.array([[40, 700, 60, 34], [200, 706, 70, 30], [520, 706, 80, 34], [660, 702, 70, 30]], np.float32), 'deep', leaf=(4, 7), far=0.08, seed=91)
g.blossom_tree([], np.array([[60, 696, 34, 16], [620, 700, 36, 16]], np.float32), 'azalea', petal=(2.4, 4.0), leaves=0.3, seed=92)
# willow on the left bank
trunk = ([(26, 730), (34, 640), (50, 540), (64, 430)], 30, 14)
boughs = [([(62, 440), (20, 380), (-30, 370)], 9, 3), ([(62, 430), (120, 372), (190, 360), (250, 390)], 9, 3), ([(60, 450), (110, 420), (170, 430)], 7, 3)]
g.willow(trunk, boughs, strands=600, length=(100, 260), ramp_='willow', leaf=(3.0, 5.2), far=0.1, sway=0.3, tips=720,
         crown=g.crown(70, 380, 300, 120, n=12, seed=80), gap=2.3, splay=(0.06, 0.3), crown_anchor=0.3, seed=81)

# water and reflections
b = np.full(W, BANK, np.float32)
src = g.C.copy()
g.lake(BANK - 2, H, bank=b, seed=110)
g.reflect(b, strength=0.8, blur=(2.0, 8.0), ripple=2.4, src=src, seed=111)
g.ripples((0, BANK, W, H), n=700, light='#dcebe6', dark='#2f6358', alpha=0.28, seed=112)

# lily pads and lotus around the lower corners; centre stays calm for the bar and name
pads = []
for cx_, cz, n in ((-2.2, 7.4, 14), (2.6, 7.6, 12), (-1.8, 10.0, 9), (2.3, 10.4, 8)):
    for _ in range(n):
        pads.append((cx_ + r.normal(0, 0.5), cz + r.normal(0, 0.7), r.uniform(0.12, 0.26)))
flowers = [(-2.4, 7.2, 0.24), (2.4, 7.8, 0.26), (-1.5, 9.6, 0.2), (2.9, 10.0, 0.2)]
g.lily_pads(pads, flowers=flowers, seed=131)

# rock and the puffin
g.rock(375, 760, 230, 54, seed=140)
g.rock(560, 770, 90, 30, seed=141)
P = puffin(95, 190, 540)
g.fill(P['body'], '#232830'); g.fill(P['body'] * np.clip((np.mgrid[0:H, 0:W][1] - 380) / 260, 0, 1), '#0f1218', 0.55)
rim = np.clip(P['body'] - np.roll(np.roll(P['body'], 8, 1), 6, 0), 0, 1)       # lit from the upper left
g.fill(ndi.gaussian_filter(rim, 1.2) * 1.6, '#aebed0', 0.55)
g.fill(P['breast'], '#f4f1e6'); g.fill(P['breast'] * np.clip((np.mgrid[0:H, 0:W][1] - 340) / 70, 0, 1), '#b9c0c9', 0.5)
g.fill(P['face'], '#dfe3e6'); g.fill(P['face'] * np.clip((np.mgrid[0:H, 0:W][0] - 410) / 50, 0, 1), '#aab4bc', 0.5)
g.fill(P['tip'], '#d93a28'); g.fill(P['white'], '#f4ead6'); g.fill(P['orange'], '#ee8a28'); g.fill(P['yellow'], '#f1c443')
g.fill(P['rosette'], '#e88a2a'); g.fill(P['eye'], '#d93a28'); g.fill(P['pupil'], '#17120f'); g.fill(P['cheek'], '#2b2622', 0.8)
g.fill(edge(P['beak'], 2, inner=True) * 0.8, '#3a2a1e', 0.7)
ft = Image.new('L', (W, H), 0); dd = ImageDraw.Draw(ft)
for fx in (290, 390):
    dd.polygon([(fx, 727), (fx + 52, 727), (fx + 60, 762), (fx + 36, 750), (fx + 14, 762), (fx - 6, 754)], fill=255)
g.fill(np.asarray(ft, np.float32) / 255, '#e8652a')
g.ripples((60, 780, 680, 840), n=120, light='#e2efe8', dark='#2f6358', alpha=0.35, seed=150)

# foreground: blossom branch from upper right, willow strands from upper left, reeds and leaves in the low corners
boughs = [([(-60, -30), (60, -14), (180, -18), (300, -40)], 9, 4)]
g.willow(None, boughs, strands=20, length=(110, 250), ramp_='willow', leaf=(10, 16), far=0.0, sway=0.2, light=-0.2, gap=6.5, stem=1.3,
         bundle=(1, 3), spread=14, splay=(0.18, 0.5), aspect=0.22, seed=160)
g.blossom_branch([(780, 130), (660, 96), (560, 120), (470, 84)], 18, 4, 'pink', flowers=90, spread=30, size=(13, 21), twigs=8, seed=161)
g.blossom_branch([(760, -10), (690, 40), (640, 28)], 9, 3, 'pink', flowers=20, spread=22, size=(12, 17), seed=162)
K = np.array([[30, 1330, 110, 100], [170, 1370, 120, 90], [-10, 1230, 90, 120], [90, 1250, 80, 70]], np.float32)
g.foliage(K, 'deep', leaf=(10, 16), density=1.0, droop=0.3, veins=True, seed=170)
K2 = np.array([[690, 1330, 110, 100], [560, 1380, 120, 90], [730, 1240, 90, 120], [640, 1260, 80, 70]], np.float32)
g.foliage(K2, 'deep', leaf=(10, 16), density=1.0, droop=0.3, veins=True, seed=171)
g.blossom_tree([], np.array([[60, 1260, 60, 40], [670, 1270, 60, 40]], np.float32), 'pink', petal=(5, 8), density=0.8, leaves=0.2, seed=172)
g.reeds(180, 330, lambda x: 1420.0 + 0 * x, n=22, height=(70, 150), ramp_='deep', lean=0.3, width=0.07, seed=173)
g.petals((0, 0, 720, 900), n=60, kind='pink', size=(4, 7), seed=174)
g.petals((0, 800, 720, 1400), n=70, kind='pink', size=(3, 5.5), water=True, seed=175)

# name board and seal
g.plaque(W / 2, 1180, 500, 118, 'PUFFIN GAMES', board='#2a3a36', letters='#e8c56a', frame='#6b4a2f', size=56, font=SUP + 'Copperplate.ttc')
g.seal(620, 1300, 28, 50, '海鹦')
g.grain(1.0)
im = g.composite().convert('RGBA')
save(im, 'jiangnan')
