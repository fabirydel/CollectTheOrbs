"""Generates the neon sci-fi art for Collect The Orbs.

Run from the repo root:  python3 tools/art/neon.py
Each asset is drawn at K x its logical size (the size the game code expects) and written over the
original file. res/logical-sizes.txt tells the game the logical size (see GameAssetManager).
"""
import math, os
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageChops

K = 4
SS = 2
U = K * SS
OUT = os.path.join(os.path.dirname(__file__), '..', '..', 'android', 'assets', 'res')
FONT = '/System/Library/Fonts/Supplemental/Arial Rounded Bold.ttf'

CYAN = (34, 211, 238)
RED = (255, 59, 92)
GOLD = (255, 190, 46)
PURPLE = (168, 85, 247)
WHITE = (232, 248, 255)
logical_sizes = {}


def set_scale(k):
    global K, U
    K = k
    U = K * SS


def canvas(w, h):
    return Image.new('RGBA', (int(w * U), int(h * U)), (0, 0, 0, 0))


def finish(im, w, h, path):
    im = im.resize((int(w * K), int(h * K)), Image.LANCZOS)
    full = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    im.save(full)
    logical_sizes['res/' + path] = (int(w), int(h))


def tint(layer, color, alpha=1.0):
    a = layer.split()[3].point(lambda v: int(v * alpha))
    out = Image.new('RGBA', layer.size, color + (0,))
    out.putalpha(a)
    return out


def glow(layer, radius, color, strength=1.0, passes=2):
    """Soft colored halo of a layer's alpha channel (radius in logical px)."""
    halo = tint(layer, color, 1.0).filter(ImageFilter.GaussianBlur(radius * U))
    out = Image.new('RGBA', layer.size, (0, 0, 0, 0))
    for _ in range(passes):
        out = Image.alpha_composite(out, tint(halo, color, min(1.0, strength / passes + 0.15)))
    return out


def radial(size, stops):
    """size x size RGBA array; stops: list of (r0to1, (r,g,b,a)) sorted by radius."""
    n = size
    yy, xx = np.mgrid[0:n, 0:n]
    r = np.hypot(xx - (n - 1) / 2, yy - (n - 1) / 2) / (n / 2)
    out = np.zeros((n, n, 4), dtype=np.float32)
    rs = [s[0] for s in stops]
    for ch in range(4):
        out[..., ch] = np.interp(r, rs, [s[1][ch] for s in stops])
    return Image.fromarray(out.clip(0, 255).astype(np.uint8))


def disc_mask(size, radius_frac):
    n = size
    yy, xx = np.mgrid[0:n, 0:n]
    r = np.hypot(xx - (n - 1) / 2, yy - (n - 1) / 2) / (n / 2)
    edge = 1.5 / (n / 2)
    m = np.clip((radius_frac - r) / edge + 0.5, 0, 1)
    return Image.fromarray((m * 255).astype(np.uint8))


def orb(w, color, core=0.80, wrong=False):
    """Glossy glowing orb filling a w x w logical square."""
    n = int(w * U)
    lc = tuple(int(c + (255 - c) * 0.75) for c in color)
    dc = tuple(int(c * 0.45) for c in color)
    body = radial(n, [(0, lc + (255,)), (0.35, color + (255,)), (1.0, dc + (255,))])
    if wrong:
        body = radial(n, [(0, (255, 255, 255, 255)), (0.55, (235, 240, 245, 255)), (1.0, tuple(int(c * 0.9) for c in color) + (255,))])
    mask = disc_mask(n, core)
    body.putalpha(ImageChops.multiply(body.split()[3], mask))
    halo = glow(body, w * 0.09, color, 0.95)
    rim = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(rim)
    c = n / 2
    rr = core * n / 2
    d.ellipse((c - rr, c - rr, c + rr, c + rr), outline=color + (255,), width=max(1, int(n * 0.02)))
    hi = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(hi)
    d.ellipse((c - rr * 0.55, c - rr * 0.72, c - rr * 0.05, c - rr * 0.30), fill=(255, 255, 255, 150))
    hi = hi.filter(ImageFilter.GaussianBlur(n * 0.02))
    out = Image.alpha_composite(halo, body)
    out = Image.alpha_composite(out, rim)
    out = Image.alpha_composite(out, hi)
    yy, xx = np.mgrid[0:n, 0:n]
    r = np.hypot(xx - (n - 1) / 2, yy - (n - 1) / 2) / (n / 2)
    fade = np.clip((1.0 - r) / 0.22, 0, 1)
    out.putalpha(Image.fromarray((np.asarray(out.split()[3], dtype=np.float32) * fade).astype(np.uint8)))
    return out


def soft_disc(w, color, core=0.8):
    n = int(w * U)
    im = radial(n, [(0, color + (255,)), (core * 0.85, color + (230,)), (core, color + (0,)), (1.0, color + (0,))])
    return im


def shield(w, color):
    n = int(w * U)
    rim_col = tuple(int(c + (255 - c) * 0.35) for c in color)
    body = radial(n, [(0, color + (10,)), (0.62, color + (50,)), (0.9, color + (190,)), (0.96, rim_col + (255,)), (1.0, rim_col + (0,))])
    mask = disc_mask(n, 0.96)
    body.putalpha(ImageChops.multiply(body.split()[3], mask))
    return body


def ring_glow(w, color):
    n = int(w * U)
    im = radial(n, [(0, color + (0,)), (0.62, color + (0,)), (0.80, color + (230,)), (0.90, color + (120,)), (1.0, color + (0,))])
    return im


class Pen:
    """Draws glyph strokes in a normalized [-1, 1] frame centered on (cx, cy) with scale s (px)."""

    def __init__(self, layer, cx, cy, s, width):
        self.layer = layer
        self.d = ImageDraw.Draw(layer)
        self.cx, self.cy, self.s, self.w = cx, cy, s, width

    def silhouette(self, draw_fn, angle=0, center=(0, 0), outline=None):
        """Fill a shape in a scratch mask (draw_fn(ImageDraw, p)), rotate it, and stroke its outline."""
        m = Image.new('L', self.layer.size, 0)
        draw_fn(ImageDraw.Draw(m), self.p)
        if angle:
            m = m.rotate(angle, center=self.p(*center), resample=Image.BICUBIC)
        w = int(outline or self.w)
        m = m.point(lambda v: 255 if v > 127 else 0)
        edge = ImageChops.subtract(m, m.filter(ImageFilter.MinFilter(2 * (w // 2) + 1)))
        self.layer.paste((255, 255, 255, 255), (0, 0), edge)

    def p(self, x, y):
        return (self.cx + x * self.s, self.cy + y * self.s)

    def line(self, pts, w=None, close=False):
        w = w or self.w
        pp = [self.p(*q) for q in pts] + ([self.p(*pts[0])] if close else [])
        self.d.line(pp, fill=(255, 255, 255, 255), width=int(w), joint='curve')
        r = w / 2
        for x, y in pp:
            self.d.ellipse((x - r, y - r, x + r, y + r), fill=(255, 255, 255, 255))

    def arc(self, x, y, r, a0, a1, w=None):
        w = w or self.w
        cx, cy = self.p(x, y)
        rr = r * self.s
        self.d.arc((cx - rr, cy - rr, cx + rr, cy + rr), a0, a1, fill=(255, 255, 255, 255), width=int(w))
        for a in (a0, a1):
            px, py = cx + rr * math.cos(math.radians(a)), cy + rr * math.sin(math.radians(a))
            e = w / 2
            self.d.ellipse((px - e, py - e, px + e, py + e), fill=(255, 255, 255, 255))

    def circle(self, x, y, r, fill=False, w=None):
        cx, cy = self.p(x, y)
        rr = r * self.s
        if fill:
            self.d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), fill=(255, 255, 255, 255))
        else:
            self.d.ellipse((cx - rr, cy - rr, cx + rr, cy + rr), outline=(255, 255, 255, 255), width=int(w or self.w))

    def poly(self, pts, fill=True):
        pp = [self.p(*q) for q in pts]
        if fill:
            self.d.polygon(pp, fill=(255, 255, 255, 255))
        else:
            self.line(pts, close=True)

    def text(self, x, y, txt, size):
        f = ImageFont.truetype(FONT, int(size * self.s))
        cx, cy = self.p(x, y)
        self.d.text((cx, cy), txt, font=f, fill=(255, 255, 255, 255), anchor='mm')


def spiral(pen, arm):
    pts = []
    for i in range(0, 90):
        t = i / 89
        a = arm * math.pi + t * 3.6 * math.pi
        r = 0.08 + t * 0.85
        pts.append((r * math.cos(a), r * 0.55 * math.sin(a)))
    pen.line([(x * math.cos(0.5) - y * math.sin(0.5), x * math.sin(0.5) + y * math.cos(0.5)) for x, y in pts], w=pen.w * 0.8)


def gear(pen):
    pts = []
    for i in range(8):
        a0 = i * math.pi / 4
        for da, rr in ((-0.20, 0.62), (-0.14, 0.9), (0.14, 0.9), (0.20, 0.62)):
            pts.append((rr * math.cos(a0 + da), rr * math.sin(a0 + da)))
    pen.line(pts, close=True)
    pen.circle(0, 0, 0.28)


GLYPHS = {}


def glyph(name):
    def reg(fn):
        GLYPHS[name] = fn
        return fn
    return reg


@glyph('upgrades')
def _(p):
    def wrench(d, P):
        d.rounded_rectangle((*P(-0.12, -0.15), *P(0.12, 0.92)), radius=p.s * 0.12, fill=255)
        d.ellipse((*P(-0.36, -0.86), *P(0.36, -0.14)), fill=255)
        d.rectangle((*P(-0.12, -1.0), *P(0.12, -0.5)), fill=0)

    def hammer(d, P):
        d.rounded_rectangle((*P(-0.11, -0.45), *P(0.11, 0.92)), radius=p.s * 0.11, fill=255)
        d.rounded_rectangle((*P(-0.5, -0.9), *P(0.5, -0.42)), radius=p.s * 0.08, fill=255)
        d.polygon([P(-0.5, -0.9), P(-0.82, -0.78), P(-0.82, -0.54), P(-0.5, -0.42)], fill=255)

    ox = 0.5
    p.silhouette(lambda d, P: wrench(d, lambda x, y: P(x - ox, y)), angle=18, center=(-ox, 0.0), outline=p.w * 0.85)
    p.silhouette(lambda d, P: hammer(d, lambda x, y: P(x + ox, y)), angle=-18, center=(ox, 0.0), outline=p.w * 0.85)


@glyph('settings')
def _(p):
    gear(p)


@glyph('ships')
def _(p):
    p.line([(0, -0.95), (0.3, -0.35), (0.3, 0.45), (-0.3, 0.45), (-0.3, -0.35)], close=True)
    p.line([(0.3, 0.1), (0.7, 0.6), (0.3, 0.45)])
    p.line([(-0.3, 0.1), (-0.7, 0.6), (-0.3, 0.45)])
    p.circle(0, -0.15, 0.12)
    p.line([(-0.12, 0.7), (0, 0.95), (0.12, 0.7)])


@glyph('stats')
def _(p):
    p.line([(-0.85, -0.7), (-0.85, 0.75), (0.85, 0.75)])
    p.line([(-0.6, 0.45), (-0.2, 0.0), (0.05, 0.2), (0.65, -0.45)])
    p.line([(0.3, -0.5), (0.7, -0.5), (0.7, -0.1)])


@glyph('galaxies')
def _(p):
    spiral(p, 0)
    spiral(p, 1)
    p.circle(0, 0, 0.1, fill=True)


@glyph('help')
def _(p):
    p.arc(0, -0.28, 0.42, 190, 440)
    p.line([(0, 0.15), (0, 0.38)])
    p.circle(0, 0.72, 0.09, fill=True)


@glyph('trophy')
def _(p):
    p.line([(-0.5, -0.8), (0.5, -0.8), (0.4, 0.0), (0.0, 0.3), (-0.4, 0.0)], close=True)
    p.line([(-0.5, -0.65), (-0.8, -0.65), (-0.7, -0.15), (-0.4, -0.05)])
    p.line([(0.5, -0.65), (0.8, -0.65), (0.7, -0.15), (0.4, -0.05)])
    p.line([(0, 0.3), (0, 0.65)])
    p.line([(-0.35, 0.8), (0.35, 0.8)])


@glyph('leaderboard')
def _(p):
    p.line([(-0.8, 0.7), (-0.8, 0.0), (-0.3, 0.0), (-0.3, 0.7)])
    p.line([(-0.3, 0.7), (-0.3, -0.5), (0.25, -0.5), (0.25, 0.7)])
    p.line([(0.25, 0.7), (0.25, 0.25), (0.8, 0.25), (0.8, 0.7)])
    p.line([(-0.9, 0.7), (0.9, 0.7)])


@glyph('exit')
def _(p):
    p.arc(0, 0.1, 0.72, 300, 600)
    p.line([(0, -0.9), (0, 0.05)])


@glyph('back')
def _(p):
    p.arc(0.1, 0.1, 0.55, 270, 450)
    p.line([(0.1, -0.45), (-0.7, -0.45)])
    p.line([(-0.35, -0.8), (-0.75, -0.45), (-0.35, -0.1)])
    p.line([(0.1, 0.65), (-0.2, 0.65)])


@glyph('home')
def _(p):
    p.line([(-0.85, -0.05), (0, -0.8), (0.85, -0.05)])
    p.line([(-0.6, -0.1), (-0.6, 0.8), (0.6, 0.8), (0.6, -0.1)])
    p.line([(-0.18, 0.8), (-0.18, 0.3), (0.18, 0.3), (0.18, 0.8)])


@glyph('share')
def _(p):
    p.line([(-0.55, 0.0), (0.5, -0.55)])
    p.line([(-0.55, 0.0), (0.5, 0.55)])
    for x, y in ((-0.55, 0.0), (0.55, -0.6), (0.55, 0.6)):
        p.circle(x, y, 0.24, fill=True)


@glyph('retry')
def _(p):
    r = 0.7
    start, end = 50, 330
    p.arc(0, 0, r, start, end, w=p.w * 1.15)
    th = math.radians(end)
    x, y = r * math.cos(th), r * math.sin(th)
    tx, ty = -math.sin(th), math.cos(th)
    nx, ny = -ty, tx
    L, Wd = 0.52, 0.34
    tip = (x + tx * L * 0.62, y + ty * L * 0.62)
    base = (x - tx * L * 0.38, y - ty * L * 0.38)
    p.poly([tip, (base[0] + nx * Wd, base[1] + ny * Wd), (base[0] - nx * Wd, base[1] - ny * Wd)])


@glyph('pause')
def _(p):
    p.line([(-0.32, -0.6), (-0.32, 0.6)], w=p.w * 1.6)
    p.line([(0.32, -0.6), (0.32, 0.6)], w=p.w * 1.6)


@glyph('play')
def _(p):
    p.poly([(-0.4, -0.7), (0.75, 0.0), (-0.4, 0.7)], fill=False)


@glyph('no')
def _(p):
    p.line([(-0.6, -0.6), (0.6, 0.6)])
    p.line([(0.6, -0.6), (-0.6, 0.6)])


@glyph('yes')
def _(p):
    p.line([(-0.75, 0.0), (-0.25, 0.55), (0.8, -0.6)])


@glyph('sound')
def _(p):
    p.line([(-0.8, -0.25), (-0.5, -0.25), (-0.1, -0.65), (-0.1, 0.65), (-0.5, 0.25), (-0.8, 0.25)], close=True)
    p.arc(-0.05, 0, 0.35, -50, 50)
    p.arc(-0.05, 0, 0.65, -50, 50)


@glyph('vibration')
def _(p):
    p.line([(-0.28, -0.7), (0.28, -0.7), (0.28, 0.7), (-0.28, 0.7)], close=True)
    p.line([(-0.12, 0.5), (0.12, 0.5)])
    p.arc(0.0, 0, 0.95, -35, 35)
    p.arc(0.0, 0, 0.95, 145, 215)


@glyph('tilt')
def _(p):
    p.line([(-0.28, -0.7), (0.28, -0.7), (0.28, 0.7), (-0.28, 0.7)], close=True)
    p.line([(-0.12, 0.5), (0.12, 0.5)])


@glyph('controls')
def _(p):
    p.line([(-0.55, -0.4), (0.55, -0.4), (0.9, 0.55), (0.55, 0.55), (0.25, 0.15), (-0.25, 0.15), (-0.55, 0.55), (-0.9, 0.55)], close=True)
    p.line([(-0.55, -0.1), (-0.25, -0.1)])
    p.line([(-0.4, -0.25), (-0.4, 0.05)])
    p.circle(0.4, -0.1, 0.07, fill=True)
    p.circle(0.6, 0.05, 0.07, fill=True)


@glyph('en')
def _(p):
    p.text(0, 0.05, 'EN', 0.95)


@glyph('es')
def _(p):
    p.text(0, 0.05, 'ES', 0.95)


SLASH = ('no',)


def button(name, gname, size, slash=False):
    """Circle button: neon ring + glyph + glow."""
    w = size
    base = canvas(w, w)
    d = ImageDraw.Draw(base)
    c = w * U / 2
    R = w * U * 0.45
    ring_w = w * U * 0.040
    d.ellipse((c - R, c - R, c + R, c + R), fill=(8, 14, 36, 140))
    stroke = canvas(w, w)
    d = ImageDraw.Draw(stroke)
    d.ellipse((c - R, c - R, c + R, c + R), outline=(255, 255, 255, 255), width=int(ring_w))
    pen = Pen(stroke, c, c, R * 0.55, w * U * 0.040)
    GLYPHS[gname](pen)
    if slash:
        pen.line([(-0.95, -0.95), (0.95, 0.95)], w=w * U * 0.05)
    halo = glow(stroke, w * 0.045, CYAN, 1.0)
    core = Image.new('RGBA', stroke.size, WHITE + (0,))
    core.putalpha(stroke.split()[3])
    out = Image.alpha_composite(Image.alpha_composite(base, halo), core)
    finish(out, w, w, 'gui/%s.png' % name)


def build_orbs():
    specs = {'blueBall': (20, CYAN, False), 'redBall': (20, RED, False), 'redBallParticle': (20, RED, False),
             'lifeBall': (20, GOLD, False), 'objectBall': (20, PURPLE, False),
             'blueWrong': (20, CYAN, True), 'redWrong': (20, RED, True),
             'blueCounter': (64, CYAN, False), 'redCounter': (64, RED, False), 'lifeCounter': (64, GOLD, False)}
    for name, (w, col, wrong) in specs.items():
        finish(orb(w, col, wrong=wrong), w, w, 'shapes/%s.png' % name)
    for name, col in (('shadowBlue', CYAN), ('shadowRed', RED), ('shadowLife', GOLD), ('shadowObject', PURPLE)):
        finish(soft_disc(20, col), 20, 20, 'shapes/%s.png' % name)


def build_fields():
    cols = [CYAN, RED, CYAN, RED, GOLD]
    sheet = canvas(640, 128)
    for i, c in enumerate(cols):
        sheet.paste(shield(128, c), (int(i * 128 * U), 0))
    finish(sheet, 640, 128, 'shapes/forceFields.png')
    sheet = canvas(800, 160)
    for i, c in enumerate([CYAN, RED, RED, CYAN, GOLD]):
        sheet.paste(ring_glow(160, c), (int(i * 160 * U), 0))
    finish(sheet, 800, 160, 'shapes/glows.png')
    n = int(160 * U)
    r = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(r)
    c, rr = n / 2, n * 0.40
    d.ellipse((c - rr, c - rr, c + rr, c + rr), outline=WHITE + (255,), width=int(n * 0.03))
    halo = glow(r, 160 * 0.035, PURPLE, 1.0)
    finish(Image.alpha_composite(halo, r), 160, 160, 'objects/ring.png')


def build_buttons():
    for name, g, size, slash in [
        ('bBack', 'back', 80, False), ('bExit', 'exit', 80, False), ('bHome', 'home', 80, False),
        ('bGalaxies', 'galaxies', 80, False), ('bLeaderboard', 'leaderboard', 80, False), ('bNo', 'no', 80, False),
        ('bPause', 'pause', 100, False), ('bPauseCant', 'pause', 100, True), ('bPlay', 'play', 100, False),
        ('bRetry', 'retry', 80, False), ('bSettings', 'settings', 80, False), ('bShare', 'share', 80, False),
        ('bShips', 'ships', 80, False), ('bStats', 'stats', 80, False), ('bTrophy', 'trophy', 80, False),
        ('bTutorial', 'help', 80, False), ('bUpgrades', 'upgrades', 80, False), ('bYes', 'yes', 80, False),
        ('bControlsOn', 'controls', 80, False), ('bControlsOff', 'controls', 80, True),
        ('bControlsOn120', 'controls', 120, False), ('bControlsOff120', 'controls', 120, True),
        ('bSoundOn', 'sound', 80, False), ('bSoundOff', 'sound', 80, True),
        ('bSoundOn120', 'sound', 120, False), ('bSoundOff120', 'sound', 120, True),
        ('bVibrationOn', 'vibration', 80, False), ('bVibrationOff', 'tilt', 80, False),
        ('bVibrationOn120', 'vibration', 120, False), ('bVibrationOff120', 'tilt', 120, False),
        ('bEnglish', 'en', 120, False), ('bEspanol', 'es', 120, False)]:
        button(name, g, size, slash)


def write_manifest():
    path = os.path.join(OUT, 'logical-sizes.txt')
    existing = {}
    if os.path.exists(path):
        for line in open(path):
            p = line.split()
            if len(p) == 3:
                existing[p[0]] = (int(p[1]), int(p[2]))
    existing.update(logical_sizes)
    with open(path, 'w') as f:
        for k in sorted(existing):
            f.write('%s %d %d\n' % ((k,) + existing[k]))


if __name__ == '__main__':
    import sys
    parts = sys.argv[1:] or ['orbs', 'fields', 'buttons']
    if 'orbs' in parts:
        build_orbs()
    if 'fields' in parts:
        build_fields()
    if 'buttons' in parts:
        build_buttons()
    write_manifest()
    print('wrote', len(logical_sizes), 'assets')
