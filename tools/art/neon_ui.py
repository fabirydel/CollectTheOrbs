"""Neon restyle of cards, touch pads, HUD pieces and fonts for Collect The Orbs.

Run from the repo root:  python3 tools/art/neon_ui.py [fonts|hud|cards|pads]
Inputs that are restyled in place (fonts, HUD, ship and galaxy art) are always read from the last
commit before the neon work (BASE), so the script can be re-run without compounding changes.
"""
import io, math, os, re, subprocess, sys
import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
from neon import CYAN, RED, GOLD, PURPLE, WHITE, NEUTRAL, tint, glow, radial

BASE = 'fcb590d'
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
ASSETS = os.path.join(ROOT, 'android', 'assets')
NAVY = (13, 13, 15)


def orig_bytes(path):
    return subprocess.check_output(['git', 'show', '%s:android/assets/%s' % (BASE, path)], cwd=ROOT)


def orig(path):
    return Image.open(io.BytesIO(orig_bytes(path))).convert('RGBA')


def save_same_size(im, path):
    im.save(os.path.join(ASSETS, path))


def ramp(lum, stops):
    xs = [p for p, _ in stops]
    return np.stack([np.interp(lum, xs, [c[i] for _, c in stops]) for i in range(3)], -1)


def recolor(im, stops, hot=None, all_hot=False):
    """Map luminance through `stops`. Saturated pixels (or all, if all_hot) go through the `hot` ramp."""
    arr = np.asarray(im, dtype=np.float32)
    rgb, a = arr[..., :3] / 255.0, arr[..., 3]
    lum = 0.299 * rgb[..., 0] + 0.587 * rgb[..., 1] + 0.114 * rgb[..., 2]
    out = ramp(lum, stops)
    if hot:
        mx, mn = rgb.max(-1), rgb.min(-1)
        w = np.clip(((mx - mn) / (mx + 1e-6) - 0.2) / 0.2, 0, 1)[..., None]
        if all_hot:
            w = np.ones_like(w)
        out = out * (1 - w) + ramp(lum, hot) * w
    out = out * (a[..., None] > 0)
    return Image.fromarray(np.dstack([out, a]).clip(0, 255).astype(np.uint8))


GREY = [(0, (36, 36, 40)), (0.45, (150, 152, 158)), (0.75, (222, 223, 226)), (1, (252, 252, 252))]
BLUE = [(0, (0, 70, 110)), (0.5, (20, 190, 225)), (0.8, (120, 240, 255)), (1, (235, 252, 255))]
REDR = [(0, (70, 0, 18)), (0.35, (200, 20, 55)), (0.7, (255, 70, 100)), (1, (255, 200, 210))]
GOLDR = [(0, (110, 50, 0)), (0.5, (255, 160, 20)), (0.8, (255, 210, 90)), (1, (255, 245, 200))]
NEUT = [(0, (30, 30, 33)), (0.5, (150, 152, 158)), (1, (240, 240, 242))]


def build_fonts():
    for name, st in [('prototype128blueWhite', BLUE), ('prototype64blueWhite', BLUE), ('prototype128redBlack', REDR),
                     ('prototype64redBlack', REDR), ('prototype64yellowOrange', GOLDR),
                     ('prototype96grey1', GREY), ('prototype96grey2', GREY)]:
        save_same_size(recolor(orig('fonts/%s.png' % name), st), 'fonts/%s.png' % name)


TUBE = [(0, (16, 16, 18)), (0.5, (66, 68, 72)), (0.85, (128, 130, 135)), (1, (206, 208, 212))]


def build_hud():
    jobs = [('arrowLeft', NEUT, None, False), ('arrowRight', NEUT, None, False),
            ('upgradeBarEmpty', TUBE, None, False), ('upgradeBarFull', TUBE, GOLDR, True), ('progress_bar', TUBE, GOLDR, False)]
    for n in (3, 4, 5):
        jobs.append(('lifeBar%dLifes' % n, TUBE, None, False))
        jobs.append(('lifeEnergy%dLifes' % n, TUBE, GOLDR, True))
    for n, st, hot, all_hot in jobs:
        save_same_size(recolor(orig('res/hud/%s.png' % n), st, hot, all_hot), 'res/hud/%s.png' % n)


class BMFont:
    def __init__(self, name):
        txt = orig_bytes(name + '.fnt').decode('latin-1')
        self.chars, self.pages = {}, {}
        for line in txt.splitlines():
            kv = dict((k, v) for k, v in re.findall(r'(\w+)=("[^"]*"|-?\d+)', line))
            if line.startswith('common'):
                self.line_h, self.base = int(kv['lineHeight']), int(kv['base'])
            elif line.startswith('page'):
                self.pages[int(kv['id'])] = orig('fonts/' + kv['file'].strip('"'))
            elif line.startswith('char '):
                self.chars[int(kv['id'])] = {k: int(v) for k, v in kv.items()}
        self.size = int(re.search(r'size=(\d+)', txt).group(1))

    def width(self, text, size):
        return sum(self.chars[ord(c)]['xadvance'] for c in text if ord(c) in self.chars) * size * N.U / self.size

    def mask(self, text, size, align='center'):
        """L mask of text at nominal font size `size` logical px (drawn at the current art scale)."""
        s = size * N.U / self.size
        lines = text.split('\n')
        widths = [sum(self.chars[ord(c)]['xadvance'] for c in l if ord(c) in self.chars) * s for l in lines]
        W, H = int(max(widths)) + 4, int(self.line_h * s * len(lines)) + 4
        out = Image.new('L', (W, H), 0)
        for li, l in enumerate(lines):
            x = (max(widths) - widths[li]) / 2 if align == 'center' else 0
            for c in l:
                g = self.chars.get(ord(c))
                if not g:
                    continue
                if g['width'] > 0 and g['height'] > 0:
                    a = self.pages[g['page']].crop((g['x'], g['y'], g['x'] + g['width'], g['y'] + g['height'])).split()[3]
                    a = a.resize((max(1, int(g['width'] * s)), max(1, int(g['height'] * s))), Image.LANCZOS)
                    out.paste(a, (int(x + g['xoffset'] * s), int(li * self.line_h * s + g['yoffset'] * s)), a)
                x += g['xadvance'] * s
        return out


def light(color, t=0.55):
    return tuple(int(c + (255 - c) * t) for c in color)


def neon_text(mask, color, glow_r=2.0):
    layer = Image.new('RGBA', mask.size, (0, 0, 0, 0))
    layer.putalpha(mask)
    halo = glow(layer, glow_r, color, 1.0)
    core = Image.new('RGBA', mask.size, light(color, 0.7) + (255,))
    core.putalpha(mask)
    return Image.alpha_composite(halo, core)


def pad_canvas(layer, margin):
    out = Image.new('RGBA', (layer.width + 2 * margin, layer.height + 2 * margin), (0, 0, 0, 0))
    out.paste(layer, (margin, margin))
    return out


def put_text(dst, mask, x, y, color, glow_r=2.0):
    m = int((6 + 2 * glow_r) * N.U)
    t = neon_text(pad_canvas(Image.merge('L', [mask]) if False else mask, m).convert('L'), color, glow_r)
    dst.alpha_composite(t, (int(x - m), int(y - m)))


def center_text(dst, mask, cx, cy, color, glow_r=2.0):
    """Draw text so the center of its actual ink bounds lands on (cx, cy)."""
    box = mask.getbbox()
    mask = mask.crop(box)
    put_text(dst, mask, cx - mask.width / 2, cy - mask.height / 2, color, glow_r)


def rounded_mask(w, h, r, inset=0):
    m = Image.new('L', (int(w * N.U), int(h * N.U)), 0)
    ImageDraw.Draw(m).rounded_rectangle((inset * N.U, inset * N.U, (w - inset) * N.U, (h - inset) * N.U), radius=r * N.U, fill=255)
    return m


def tile(w, h, colors, split=None, radius=14, alpha=255, bw=3.0, glow_r=2.2):
    """Dark glass card with neon border. colors: one color, or two for a split tile.
    split: None, 'diag' (top-left/bottom-right) or 'horiz' (top/bottom)."""
    size = (int(w * N.U), int(h * N.U))
    outer = rounded_mask(w, h, radius)
    inner = rounded_mask(w, h, radius, bw)
    ring = ImageChops.subtract(outer, inner)
    base = Image.new('RGBA', size, NAVY + (alpha,))
    if split is None:
        regions = [Image.new('L', size, 255)]
    else:
        a = Image.new('L', size, 0)
        d = ImageDraw.Draw(a)
        if split == 'diag':
            d.polygon([(0, 0), (size[0], 0), (0, size[1])], fill=255)
        else:
            d.rectangle((0, 0, size[0], size[1] // 2), fill=255)
        regions = [a, ImageChops.invert(a)]
    out = Image.new('RGBA', size, (0, 0, 0, 0))
    out.paste(base, (0, 0), outer)
    for col, reg in zip(colors, regions):
        wash = radial(max(size), [(0, col + (90,)), (0.9, col + (26,)), (1.0, col + (8,))]).crop((0, 0) + size)
        wash.putalpha(ImageChops.multiply(wash.split()[3], ImageChops.multiply(reg, inner)))
        out = Image.alpha_composite(out, wash)
        border = Image.new('RGBA', size, light(col, 0.15) + (255,))
        border.putalpha(ImageChops.multiply(ring, reg))
        out = Image.alpha_composite(out, border)
        out = Image.alpha_composite(glow(border, glow_r, col, 0.8), out)
    if split is not None:
        line = Image.new('RGBA', size, (0, 0, 0, 0))
        d = ImageDraw.Draw(line)
        if split == 'diag':
            d.line((size[0], 0, 0, size[1]), fill=WHITE + (230,), width=int(bw * 0.55 * N.U))
        else:
            d.line((0, size[1] // 2, size[0], size[1] // 2), fill=WHITE + (230,), width=int(bw * 0.55 * N.U))
        line.putalpha(ImageChops.multiply(line.split()[3], inner))
        out = Image.alpha_composite(out, Image.alpha_composite(glow(line, 1.6, WHITE, 0.6), line))
    return out


def stamp(dst, tile_img, i, tw):
    dst.alpha_composite(tile_img, (int(i * tw * N.U), 0))


def price_tile(font, w, h, red, blue, flip=False, radius=14, horiz=False, size=44):
    if red and blue:
        cols = (RED, CYAN) if not flip else (CYAN, RED)
        amts = (red, blue) if not flip else (blue, red)
        t = tile(w, h, cols, 'horiz' if horiz else 'diag', radius)
        sz = size * (0.8 if not horiz else 1.0)
        m1, m2 = font.mask(str(amts[0]), sz), font.mask(str(amts[1]), sz)
        pad = 9 * N.U
        if horiz:
            put_text(t, m1, w * N.U - pad - m1.width, h * N.U / 2 - pad - m1.height, cols[0])
            put_text(t, m2, pad, h * N.U - pad - m2.height, cols[1])
        else:
            put_text(t, m1, pad, pad * 0.8, cols[0])
            put_text(t, m2, w * N.U - pad - m2.width, h * N.U - pad * 0.8 - m2.height, cols[1])
        return t
    col = RED if red else CYAN
    t = tile(w, h, (col,), None, radius)
    m = font.mask(str(red or blue), size)
    pad = 10 * N.U
    put_text(t, m, w * N.U - pad - m.width, h * N.U - pad - m.height, col)
    return t


def ship_sprite(i):
    sheet = orig('res/ships/ships%d.png' % (i // 15 + 1))
    f = i % 15
    return sheet.crop((f * 128, 0, f * 128 + 128, 128))


def bought_ship_tile(i, size=149):
    t = tile(size, size, (NEUTRAL,), None, 18)
    n = int(size * N.U)
    back = radial(n, [(0, NEUTRAL + (80,)), (0.55, NEUTRAL + (24,)), (0.8, NEUTRAL + (0,)), (1, NEUTRAL + (0,))])
    t = Image.alpha_composite(t, back)
    sp = ship_sprite(i).resize((int(120 * N.U), int(120 * N.U)), Image.LANCZOS)
    t.alpha_composite(sp, ((n - sp.width) // 2, (n - sp.height) // 2))
    return t


def parse_prices():
    src = open(os.path.join(ROOT, 'core/src/com/spaceship/main/Main.java'), encoding='latin-1').read()
    ships = {int(i): (int(r), int(b)) for i, r, b in re.findall(r'INSERT INTO ships \(id, bought, reds, blues\) VALUES \((\d+), \d+, (\d+), (\d+)\)', src)}
    gal = {int(i): (int(r), int(b)) for i, r, b in re.findall(r'INSERT INTO galaxies \(id, bought, reds, blues\) VALUES \((\d+), \d+, (\d+), (\d+)\)', src)}
    ups = [int(x) for x in re.findall(r'INSERT INTO upgradesPrice \(id, bought, lifeBalls\) VALUES \(\d+, \d+, (\d+)\)', src)]
    return ships, gal, ups


def galaxy_thumb(sheet_name, k):
    sheet = orig('res/bgs/%s.png' % sheet_name)
    t = sheet.crop((k * 220, 0, k * 220 + 220, 298))
    arr = np.asarray(t.convert('L'), dtype=np.float32)
    ys, xs = np.where(arr < 205)
    x0, x1, y0, y1 = xs.min(), xs.max(), ys.min(), ys.max()
    img = t.crop((x0, y0, x1 + 1, y1 + 1)).convert('RGB')
    return img


def bought_galaxy_tile(sheet_name, k):
    t = tile(220, 298, (NEUTRAL,), None, 22)
    th = galaxy_thumb(sheet_name, k)
    w, h = int(184 * N.U), int(250 * N.U)
    th = th.resize((w, h), Image.LANCZOS).convert('RGBA')
    m = Image.new('L', (w, h), 0)
    ImageDraw.Draw(m).ellipse((0, 0, w - 1, h - 1), fill=255)
    th.putalpha(m)
    ring = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(ring).ellipse((0, 0, w - 1, h - 1), outline=light(NEUTRAL, 0.2) + (255,), width=int(2.2 * N.U))
    th = Image.alpha_composite(Image.alpha_composite(glow(ring, 3, NEUTRAL, 0.9), th), ring)
    t.alpha_composite(th, (int((220 * N.U - w) / 2), int((298 * N.U - h) / 2)))
    return t


def build_cards():
    N.set_scale(2)
    font = BMFont('fonts/prototype128blueWhite')
    ships, gal, ups = parse_prices()
    for s in range(8):
        n = 5 if s == 7 else 10
        name = 'shipsPrice%d' % (75 if s == 7 else s + 1)
        bname = name.replace('Price', 'Bought')
        W = 149 * n
        price = N.canvas(W, 149)
        bought = N.canvas(W, 149)
        for j in range(n):
            i = s * 10 + j
            r, b = ships[i]
            bt = bought_ship_tile(i)
            stamp(bought, bt, j, 149)
            stamp(price, bt if i == 0 else price_tile(font, 149, 149, r, b, flip=(i % 2 == 0)), j, 149)
        N.finish(price, W, 149, 'ships/%s.png' % name)
        N.finish(bought, W, 149, 'ships/%s.png' % bname)
    for s in (1, 2):
        price = N.canvas(1100, 298)
        bought = N.canvas(1100, 298)
        for j in range(5):
            i = (s - 1) * 5 + j
            bt = bought_galaxy_tile('bgsBought%d' % s, j)
            stamp(bought, bt, j, 220)
            r, b = gal[i] if i in gal else (0, 0)
            stamp(price, bt if i == 0 else price_tile(font, 220, 298, r, b, flip=(i % 2 == 0), radius=22, horiz=True, size=52), j, 220)
        N.finish(price, 1100, 298, 'bgs/bgsPrice%d.png' % s)
        N.finish(bought, 1100, 298, 'bgs/bgsBought%d.png' % s)
    sheet = N.canvas(745, 149)
    for j, v in enumerate([50, 100, 250, 500, 1000]):
        t = tile(149, 149, (GOLD,), None, 14)
        m = font.mask(str(v), 44)
        put_text(t, m, 149 * N.U - 10 * N.U - m.width, 149 * N.U - 10 * N.U - m.height, GOLD)
        stamp(sheet, t, j, 149)
    N.finish(sheet, 745, 149, 'upgrades/upgradesPrice.png')


def build_play():
    N.set_scale(4)
    font = BMFont('fonts/prototype128blueWhite')
    w, h = 196, 98
    # inset the button so its outer neon glow fits inside the image
    mg = 12
    inner = tile(w - 2 * mg, h - 2 * mg, (NEUTRAL,), None, 20, alpha=230, bw=5.0, glow_r=2.0)
    t = N.canvas(w, h)
    t.alpha_composite(inner, (mg * N.U, mg * N.U))
    halo = glow(t, 6.5, NEUTRAL, 1.0)
    t = Image.alpha_composite(halo, t)
    m = font.mask('START', 44)
    center_text(t, m, t.width / 2, t.height / 2, NEUTRAL, 4.0)
    N.finish(t, w, h, 'gui/bGo.png')
    for name, w, h, text in [('bQuestionEN', 311, 80, 'Are you sure you\nwant to leave?'),
                             ('bQuestionES', 320, 80, 'Estas seguro de\nque quieres irte?')]:
        im = N.canvas(w, h)
        m = font.mask(text, 29)
        center_text(im, m, im.width / 2, im.height / 2, WHITE)
        N.finish(im, w, h, 'gui/%s.png' % name)


def glass_pad(w, h):
    return tile(w, h, (NEUTRAL,), None, 40, alpha=150)


def chevron(t, w, h, direction):
    cx, cy = w * N.U / 2, h * N.U / 2
    s = 22 * N.U * 1.6
    pts = [(cx - direction * s * 0.45, cy - s), (cx + direction * s * 0.45, cy), (cx - direction * s * 0.45, cy + s)]
    layer = Image.new('RGBA', t.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.line(pts, fill=WHITE + (255,), width=int(9 * N.U), joint='curve')
    for x, y in pts:
        r = 4.5 * N.U
        d.ellipse((x - r, y - r, x + r, y + r), fill=WHITE + (255,))
    t.alpha_composite(Image.alpha_composite(glow(layer, 5, NEUTRAL, 1.0), layer))


def build_pads():
    N.set_scale(2)
    font = BMFont('fonts/prototype128blueWhite')
    for name, w, h, d in [('padsLeft', 160, 576, -1), ('padsIzq', 160, 576, -1), ('padsRight', 160, 576, 1), ('padsDer', 160, 576, 1)]:
        t = glass_pad(w, h)
        chevron(t, w, h, d)
        N.finish(t, w, h, 'hud/%s.png' % name)
    for name, w, h in [('padVertical', 160, 576), ('padHorizontal', 392, 284)]:
        N.finish(glass_pad(w, h), w, h, 'hud/%s.png' % name)
    for name, text in [('padsShield', 'Switch\nShield'), ('padsItem', 'Use Item'), ('padsShieldEsp', 'Cambiar\nEscudo'), ('padsItemEsp', 'Usar Objeto')]:
        t = glass_pad(392, 284)
        m = font.mask(text, 40)
        center_text(t, m, t.width / 2, t.height / 2, NEUTRAL)
        N.finish(t, 392, 284, 'hud/%s.png' % name)


def build_lightning():
    """Brighter, shinier versions of the original menu-shield bolt sprites (same shapes)."""
    from PIL import ImageFilter
    N.set_scale(2)
    for name, col in (('lightningBlue', CYAN), ('lightningRed', RED)):
        src = orig('res/shapes/%s.png' % name)
        big = src.resize((src.width * N.U, src.height * N.U), Image.LANCZOS)
        a = np.asarray(big.split()[3], dtype=np.float32) / 255.0
        hard = Image.fromarray(((a > 0.55) * 255).astype(np.uint8))
        thin = hard.filter(ImageFilter.MinFilter(5)).filter(ImageFilter.GaussianBlur(0.7 * N.U / 2))
        mid = hard.filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.8 * N.U / 2))
        core = np.asarray(thin, dtype=np.float32) / 255.0
        body = np.asarray(mid, dtype=np.float32) / 255.0
        halo1 = np.asarray(hard.filter(ImageFilter.GaussianBlur(1.6 * N.U)), dtype=np.float32) / 255.0
        halo2 = np.asarray(hard.filter(ImageFilter.GaussianBlur(4.2 * N.U)), dtype=np.float32) / 255.0
        glow_a = np.clip(halo1 * 0.8 + halo2 * 1.1, 0, 1)
        rgb = np.zeros(a.shape + (3,), dtype=np.float32)
        for i in range(3):
            tint = col[i] / 255.0
            lite = tint + (1 - tint) * 0.55
            white = 1.0
            c = tint * glow_a * (1 - body) + lite * body * (1 - core) + white * core
            w = glow_a * (1 - body) + body * (1 - core) + core
            rgb[..., i] = np.clip(c / np.maximum(w, 1e-3), 0, 1)
        alpha = np.clip(glow_a * 0.85 + body, 0, 1)
        alpha = np.maximum(alpha, core)
        im = Image.fromarray(np.dstack([rgb * 255, alpha * 255]).astype(np.uint8))
        im.save(os.path.join(ASSETS, 'res/shapes/%s.png' % name))
        N.logical_sizes['res/shapes/%s.png' % name] = (src.width, src.height)


FONT_COLORS = {'grey': NEUTRAL, 'blueWhite': CYAN, 'redBlack': RED, 'yellowOrange': GOLD}


def build_neon_fonts():
    """Re-renders every bitmap font as neon (white-hot core, tinted body, glow halo).

    Glyph shapes and metrics come from the original atlases (BASE commit). Each glyph gets a transparent
    margin for its glow, so the new atlases are re-packed and the .fnt files rewritten with offsets and
    sizes adjusted (xadvance is unchanged, so text layout is identical).
    """
    from PIL import ImageFilter
    names = [n[:-4] for n in subprocess.check_output(['git', 'ls-tree', '--name-only', BASE, 'android/assets/fonts/'], cwd=ROOT).decode().split()
             if n.endswith('.fnt')]
    for path in names:
        fnt = os.path.basename(path)
        txt = orig_bytes('fonts/' + fnt + '.fnt').decode('latin-1')
        color = next(c for k, c in FONT_COLORS.items() if k in fnt)
        size = int(re.search(r'size=(\d+)', txt).group(1))
        pages = {}
        header, chars, tail = [], [], []
        for line in txt.splitlines():
            if line.startswith('page'):
                kv = dict(re.findall(r'(\w+)=("[^"]*"|-?\d+)', line))
                pages[int(kv['id'])] = orig('fonts/' + kv['file'].strip('"'))
            elif line.startswith('char '):
                chars.append({k: int(v) for k, v in re.findall(r'(\w+)=(-?\d+)', line)})
            elif line.startswith('kerning') or line.startswith('chars'):
                if line.startswith('kerning'):
                    tail.append(line)
            else:
                header.append(line)
        P = int(size * 0.16) + 2
        k = size / 64.0
        glyphs = []
        for c in chars:
            w, h = c['width'], c['height']
            W, H = w + 2 * P, h + 2 * P
            canvas = Image.new('L', (W, H), 0)
            if w > 0 and h > 0:
                m = pages[c['page']].crop((c['x'], c['y'], c['x'] + w, c['y'] + h)).split()[3]
                canvas.paste(m, (P, P))
            hard = canvas.point(lambda v: 255 if v > 110 else 0)
            core = hard.filter(ImageFilter.MinFilter(max(3, int(round(4 * k)) | 1))).filter(ImageFilter.GaussianBlur(0.6 * k))
            body = canvas
            h1 = canvas.filter(ImageFilter.GaussianBlur(2.2 * k))
            h2 = canvas.filter(ImageFilter.GaussianBlur(5.5 * k))
            body_a = np.asarray(body, dtype=np.float32) / 255
            core_a = np.asarray(core, dtype=np.float32) / 255
            glow_a = np.clip(np.asarray(h1, dtype=np.float32) / 255 * 1.3 + np.asarray(h2, dtype=np.float32) / 255 * 1.0, 0, 1) * (1 - body_a)
            rgb = np.zeros(body_a.shape + (3,), dtype=np.float32)
            for i in range(3):
                t = color[i] / 255.0
                lite = t + (1 - t) * 0.55
                num = t * glow_a * 0.9 + lite * body_a * (1 - core_a) + 1.0 * core_a
                den = glow_a * 0.9 + body_a * (1 - core_a) + core_a
                rgb[..., i] = np.clip(num / np.maximum(den, 1e-3), 0, 1)
            alpha = np.clip(glow_a * 0.8 + body_a, 0, 1)
            alpha = np.maximum(alpha, core_a)
            glyphs.append((c, Image.fromarray(np.dstack([rgb * 255, alpha * 255]).astype(np.uint8)), W, H))
        # shelf-pack into one page
        AW = 1024
        x = y = rowh = 0
        placed = []
        for c, im, W, H in glyphs:
            if x + W > AW:
                x, y, rowh = 0, y + rowh + 2, 0
            placed.append((c, im, x, y, W, H))
            x += W + 2
            rowh = max(rowh, H)
        AH = 256
        while AH < y + rowh + 2:
            AH *= 2
        atlas = Image.new('RGBA', (AW, AH), (0, 0, 0, 0))
        out = []
        for c, im, px, py, W, H in placed:
            atlas.paste(im, (px, py))
            out.append(('char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d xadvance=%d page=0 chnl=0' %
                        (c['id'], px, py, W, H, c['xoffset'] - P, c['yoffset'] - P, c['xadvance'])))
        png = 'neon_%s.png' % fnt
        atlas.save(os.path.join(ASSETS, 'fonts', png))
        lines = []
        for h in header:
            if h.startswith('common'):
                h = re.sub(r'scaleW=\d+', 'scaleW=%d' % AW, h)
                h = re.sub(r'scaleH=\d+', 'scaleH=%d' % AH, h)
                h = re.sub(r'pages=\d+', 'pages=1', h)
            lines.append(h)
        lines.append('page id=0 file="%s"' % png)
        lines.append('chars count=%d' % len(out))
        lines += out
        if tail:
            lines.append('kernings count=%d' % len(tail))
            lines += tail
        with open(os.path.join(ASSETS, 'fonts', fnt + '.fnt'), 'w', encoding='latin-1') as fh:
            fh.write('\n'.join(lines) + '\n')


if __name__ == '__main__':
    parts = sys.argv[1:] or ['fonts', 'hud', 'cards', 'pads', 'play', 'lightning']
    if 'fonts' in parts:
        build_fonts()
    if 'hud' in parts:
        build_hud()
    if 'cards' in parts:
        build_cards()
    if 'pads' in parts:
        build_pads()
    if 'play' in parts:
        build_play()
    if 'lightning' in parts:
        build_lightning()
    if 'neonfonts' in parts:
        build_neon_fonts()
    N.write_manifest()
    print('done', parts)
