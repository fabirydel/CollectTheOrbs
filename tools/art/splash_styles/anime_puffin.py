"""Puffin Games splash, anime key-art style (cel shaded puffin on a summer sea cliff)."""
from common import *
from anime import Anime
from core import blur

a = Anime(W, H, seed=77)
HOR = 900
a.sky('#1f63c6', '#5aa9ec', '#d6f0ff', horizon=HOR)
a.sun_flare(560, 190, strength=0.9)
a.cumulus(110, HOR, 620, 420, puffs=60)
a.cumulus(640, HOR, 560, 300, puffs=50)
# sea: flat bands + sparkle
sea = (a.YY >= HOR).astype(np.float32)
a.gradient(sea, '#37b4e8', '#145fb0', HOR, H)
for i in range(26):
    y = HOR + 12 + i ** 1.5 * 4.2
    x0 = a.rng.uniform(0, W); L = 30 + (y - HOR) * 0.5
    a.lines([(x0, y), (x0 + L, y + 1)], 2 + (y - HOR) / 150, '#bfeaff', 0.65)
a.lines([(0, HOR), (W, HOR)], 3, '#e8f8ff', 0.8)

# cliff / rock the bird stands on
rock = [(40, 1010), (90, 900), (150, 800), (250, 772), (470, 772), (600, 810), (650, 900), (700, 1010)]
rk = a.poly(rock)
a.fill(rk, '#4e5b6f')
a.fill(rk * (a.YY < 836), '#6f7f95')
a.fill(a.poly([(150, 800), (250, 772), (470, 772), (600, 810), (590, 828), (470, 800), (250, 800), (160, 830)]), '#9fb1c6')
a.lines(rock[1:-1], 3.2, '#222a3a')
for (x, y, L) in ((150, 900, 100), (360, 880, 130), (480, 940, 90)):
    a.lines([(x, y), (x + L * 0.5, y + 12), (x + L, y + 4)], 2.2, '#2e3850', 0.7)

# the puffin: cel shaded
P = puffin(105, 205, 540)
def lit(m, dx, dy):
    sh = np.roll(np.roll(m, dx, 1), dy, 0)
    return np.clip(m - sh, 0, 1)           # sliver on the lit side
body = P['body']
a.fill(body, '#1a1c27')
a.fill(np.clip(body - np.roll(np.roll(body, 14, 1), 18, 0), 0, 1), '#3c4766')     # rim light, upper-left
a.fill(P['breast'], '#f5f7fb'); a.fill(P['breast'] * (a.XX > 340), '#c9d3e6', 0.9)
a.fill(P['face'], '#d6dce6'); a.fill(P['face'] * (a.YY > 440), '#aab6cf', 0.9)
beak = P['beak']
a.fill(P['tip'], '#ff3d2b'); a.fill(P['white'], '#fff4e8'); a.fill(P['orange'], '#ff8f1f'); a.fill(P['yellow'], '#ffcf2e')
a.fill(beak * (a.YY > 470), '#000000', 0.14)
a.fill(P['rosette'], '#ff9a2a')
a.fill(P['eye'], '#ff3d2b')
# big shiny anime eye
ex, ey = 105 + 128 / 300 * 540, 205 + 92 / 300 * 540
a.fill(a.circle(ex, ey, 18), '#2a0c10')
a.fill(a.circle(ex, ey, 9), '#0b0608')
a.fill(a.circle(ex - 6, ey - 7, 5.5), '#ffffff')
a.fill(a.circle(ex + 6, ey + 6, 2.4), '#ffffff', 0.9)
a.fill(a.circle(ex, ey, 18) * (1 - a.circle(ex, ey, 12)) * (a.YY > ey), '#ff7a3a', 0.8)
# outlines
a.fill(np.maximum(edge(P['all'], 3), edge(P['beak'], 2, inner=True) * 0.8, edge(P['face'], 2, inner=True) * 0.5), '#12131c')
a.fill(P['cheek'], '#12131c', 0.8)
# feet
for fx in (300, 395):
    a.fill(a.poly([(fx, 735), (fx + 52, 735), (fx + 60, 768), (fx + 36, 756), (fx + 14, 768), (fx - 6, 760)]), '#ff6a2a')
    a.lines([(fx, 735), (fx + 52, 735), (fx + 60, 768), (fx + 36, 756), (fx + 14, 768), (fx - 6, 760)], 2.2, '#12131c', closed=True)

a.sparkles(14, (0, 0, W, 700), (1.5, 3.2)); a.sparkles(30, (0, HOR + 20, W, H), (1.2, 2.6))
a.bloom(0.86, 20, 0.3)
a.vignette(0.14)

im = a.composite().convert('RGBA')
# title: anime logo style, chunky italic with white stroke and a coloured shadow
d = ImageDraw.Draw(im)
f = font('Impact.ttf', 110)
def logo(t, y, fill, shadow='#ff3d6e'):
    w = d.textlength(t, font=f)
    lay = Image.new('RGBA', (W, 220), (0, 0, 0, 0)); ld = ImageDraw.Draw(lay)
    x = (W - w) / 2
    for o in range(10, 0, -1):
        ld.text((x + o * 0.8, 40 + o), t, font=f, fill=shadow, stroke_width=8, stroke_fill=shadow)
    ld.text((x, 40), t, font=f, fill=fill, stroke_width=8, stroke_fill='#ffffff')
    ld.text((x, 40), t, font=f, fill=fill)
    lay = lay.rotate(4, resample=Image.BICUBIC, center=(W / 2, 110))
    im.alpha_composite(lay, (0, y))
logo('PUFFIN', 1040, '#16224f'); logo('GAMES', 1170, '#16224f')
save(im, 'anime')
