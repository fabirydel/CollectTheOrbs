"""Draws Puffin Games splash-screen options (720x1280, same as android/assets/res/splash.png).

Run from the repo root:  python3 tools/art/splash.py
Writes tools/art/splash_options/option_*.png plus a side-by-side sheet.png. The puffin is a vector
drawing (rendered with rsvg-convert) inspired by the reference photo, facing left.
"""
import math, os, random, subprocess, tempfile
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageChops

HERE = os.path.dirname(__file__)
OUT = os.path.join(HERE, 'splash_options')
ASSETS = os.path.join(HERE, '..', '..', 'android', 'assets', 'res')
W, H = 720, 1280
SUP = '/System/Library/Fonts/Supplemental/'

BLACK = '#14161b'
GRAY = '#c9cdd3'
RED = '#ff4a2b'
ORANGE = '#ff8a1f'
YELLOW = '#ffc933'

# Puffin bust in a 300 x 300 box, facing left.
BODY = 'M 92 88 C 108 58 160 40 200 60 C 224 72 232 102 236 132 C 244 172 270 196 286 230 C 298 258 278 292 228 297 L 132 297 C 122 270 118 240 118 214 C 120 184 116 158 98 142 Z'
BREAST = 'M 118 214 C 140 204 168 230 180 262 C 186 278 180 290 168 297 L 132 297 C 122 270 118 240 118 214 Z'
FACE = 'M 98 90 C 116 66 160 58 190 74 C 208 86 208 110 196 124 C 180 144 138 152 106 142 C 94 126 92 106 98 90 Z'
BEAK = 'M 18 128 C 40 100 70 78 104 68 L 108 152 C 74 148 40 140 18 128 Z'


def render_svg(svg, width):
    with tempfile.TemporaryDirectory() as d:
        p = os.path.join(d, 'a.svg')
        open(p, 'w').write(svg)
        o = os.path.join(d, 'a.png')
        subprocess.run(['rsvg-convert', '-w', str(width), '-o', o, p], check=True)
        return Image.open(o).convert('RGBA')


def puffin_flat(width, body=BLACK, face=GRAY):
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 300">
<defs><clipPath id="b"><path d="{BEAK}"/></clipPath></defs>
<path d="{BODY}" fill="{body}"/>
<path d="{BREAST}" fill="#f4f5f7"/>
<path d="{FACE}" fill="{face}"/>
<g clip-path="url(#b)">
 <rect x="0" y="40" width="110" height="130" fill="{RED}"/>
 <path d="M 60 60 L 54 170 L 66 170 L 72 60 Z" fill="#fff3d6"/>
 <path d="M 72 60 L 66 170 L 86 170 L 92 60 Z" fill="{ORANGE}"/>
 <path d="M 92 60 L 86 170 L 110 170 L 110 60 Z" fill="{YELLOW}"/>
</g>
<path d="M 104 68 L 108 152" stroke="{body}" stroke-width="3"/>
<circle cx="108" cy="122" r="9" fill="{YELLOW}"/><circle cx="108" cy="122" r="9" fill="none" stroke="{ORANGE}" stroke-width="3"/>
<path d="M 128 98 C 150 98 172 104 186 112" stroke="#6b7078" stroke-width="3" fill="none" stroke-linecap="round"/>
<circle cx="128" cy="92" r="8.5" fill="{RED}"/><circle cx="128" cy="92" r="4.6" fill="#0b0c0f"/><circle cx="126.6" cy="90.6" r="1.4" fill="#fff"/>
</svg>'''
    return render_svg(svg, width)


def puffin_lines(width, line, beak):
    """Outline-only puffin for the neon look (colors are boosted into a glow afterwards)."""
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 300" fill="none" stroke-linejoin="round" stroke-linecap="round">
<path d="{BODY}" stroke="{line}" stroke-width="3"/>
<path d="{BREAST}" stroke="{line}" stroke-width="3"/>
<path d="{FACE}" stroke="{line}" stroke-width="3"/>
<path d="{BEAK}" stroke="{beak}" stroke-width="3.5"/>
<path d="M 60 90 L 58 140" stroke="{beak}" stroke-width="3"/><path d="M 78 77 L 76 146" stroke="{beak}" stroke-width="3"/><path d="M 94 70 L 95 150" stroke="{beak}" stroke-width="3"/>
<circle cx="108" cy="122" r="9" stroke="{beak}" stroke-width="3"/>
<path d="M 128 98 C 150 98 172 104 186 112" stroke="{line}" stroke-width="3"/>
<circle cx="128" cy="92" r="7" stroke="{beak}" stroke-width="3"/>
</svg>'''
    return render_svg(svg, width)


def font(name, size, index=0):
    return ImageFont.truetype(SUP + name, size, index=index)


def text_layer(txt, fnt, color, tracking=0):
    """Text on a transparent layer, with optional letter spacing."""
    widths = [fnt.getlength(c) for c in txt]
    total = int(sum(widths) + tracking * (len(txt) - 1)) + 8
    asc, desc = fnt.getmetrics()
    im = Image.new('RGBA', (total, asc + desc + 8), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    x = 4
    for c, w in zip(txt, widths):
        d.text((x, 4), c, font=fnt, fill=color)
        x += w + tracking
    return im.crop(im.getbbox())


def paste_c(base, layer, cx, top):
    base.alpha_composite(layer, (int(cx - layer.width / 2), int(top)))


def halo(layer, radius, color, strength=1.0):
    a = layer.split()[3].filter(ImageFilter.GaussianBlur(radius))
    a = a.point(lambda v: min(255, int(v * strength)))
    out = Image.new('RGBA', layer.size, color + (0,))
    out.putalpha(a)
    return out


def neon(base, layer, color, cx, top, radii=((24, 1.2), (10, 1.6), (4, 1.6))):
    """Composite a line layer with a layered glow, centered at cx."""
    pad = 60
    big = Image.new('RGBA', (layer.width + 2 * pad, layer.height + 2 * pad), (0, 0, 0, 0))
    big.alpha_composite(layer, (pad, pad))
    for r, s in radii:
        big = Image.alpha_composite(halo(big, r, color, s), big) if False else big
    out = Image.new('RGBA', big.size, (0, 0, 0, 0))
    for r, s in radii:
        out = Image.alpha_composite(out, halo(big, r, color, s))
    out = Image.alpha_composite(out, big)
    base.alpha_composite(out, (int(cx - out.width / 2), int(top - pad)))


def stars(im, n, seed=7, ymax=None):
    rnd = random.Random(seed)
    d = ImageDraw.Draw(im)
    for _ in range(n):
        x, y = rnd.randrange(W), rnd.randrange(ymax or H)
        r = rnd.choice([1, 1, 1, 2])
        v = rnd.randrange(120, 255)
        d.ellipse((x - r, y - r, x + r, y + r), fill=(v, v, v, 255))


def progress_mock(im):
    sheet = Image.open(os.path.join(ASSETS, 'hud', 'progress_bar.png')).convert('RGBA')
    fw = sheet.width // 21
    fr = sheet.crop((fw * 10, 0, fw * 11, sheet.height))
    fr = fr.resize((int(fw / 3 * 1.75 * 3), int(sheet.height * 1.75 * 3)), Image.LANCZOS) if False else fr
    sz = (int(143 * 1.75), int(36 * 1.75))
    fr = fr.resize(sz, Image.LANCZOS)
    im.alpha_composite(fr, (W // 2 - sz[0] // 2, int(H * 2 / 3 - sz[1] / 2)))


def vgrad(c1, c2):
    t = np.linspace(0, 1, H)[:, None, None]
    a = np.array(c1)[None, None, :] * (1 - t) + np.array(c2)[None, None, :] * t
    return Image.fromarray(np.repeat(a, W, axis=1).astype('uint8'), 'RGB').convert('RGBA')


# --- A: Sunrise (keeps the old sky-blue identity) ---------------------------------------------
def option_a():
    im = Image.new('RGBA', (W, H), (37, 200, 238, 255))
    # rays + sun behind the puffin
    rays = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(rays)
    cx, cy = 360, 290
    for ang in range(-80, 81, 20):
        a0, a1 = math.radians(ang - 5 - 90), math.radians(ang + 5 - 90)
        d.polygon([(cx, cy), (cx + 900 * math.cos(a0), cy + 900 * math.sin(a0)), (cx + 900 * math.cos(a1), cy + 900 * math.sin(a1))], fill=(0, 176, 255, 120))
    rays = rays.filter(ImageFilter.GaussianBlur(6))
    im.alpha_composite(rays)
    d = ImageDraw.Draw(im)
    for i in range(240):
        t = i / 240
        col = (255, int(205 + 45 * t), int(40 + 120 * t), 255)
        r = 150
        y = cy - 150 + i * 1.25
        half = math.sqrt(max(0, r * r - (y - cy) ** 2)) if y < cy + 0 else 0
    sun = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ds = ImageDraw.Draw(sun)
    ds.ellipse((cx - 150, cy - 150, cx + 150, cy + 150), fill=(255, 214, 51, 255))
    grad = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(grad)
    for y in range(cy - 150, cy + 150):
        t = (y - (cy - 150)) / 300
        gd.line([(0, y), (W, y)], fill=(255, int(190 + 60 * t), int(30 + 140 * t), 255))
    sun = ImageChops.multiply(sun, grad) if False else Image.composite(grad, Image.new('RGBA', (W, H), (0, 0, 0, 0)), sun.split()[3])
    im.alpha_composite(sun)
    p = puffin_flat(350)
    im.alpha_composite(p, (cx - 198, cy - 178))
    # wordmark
    f1, f2 = font('Impact.ttf', 180), font('Impact.ttf', 100)
    paste_c(im, text_layer('PUFFIN', f1, (255, 255, 255, 255), 6), W / 2, 490)
    ImageDraw.Draw(im).line([(150, 670), (570, 670)], fill=(255, 255, 255, 255), width=4)
    paste_c(im, text_layer('GAMES', f2, (255, 255, 255, 255), 22), W / 2, 695)
    progress_mock(im)
    return im


# --- B: Neon (matches the in-game neon look) --------------------------------------------------
def option_b():
    im = vgrad((4, 6, 14), (2, 3, 8))
    stars(im, 120, ymax=H)
    p = puffin_lines(420, '#e6f0ff', '#ffaa3c')
    pb = p.copy()
    neon(im, p, (80, 190, 255), 360, 100, radii=((30, 1.1), (12, 1.5), (4, 1.5)))
    f = font('Avenir Next Condensed.ttc', 130, index=5)
    t = text_layer('PUFFIN', f, (255, 255, 255, 255), 14)
    neon(im, t, (80, 190, 255), 360, 520, radii=((26, 1.0), (9, 1.4), (3, 1.4)))
    f2 = font('Avenir Next Condensed.ttc', 64, index=5)
    t2 = text_layer('GAMES', f2, (255, 210, 130, 255), 40)
    neon(im, t2, (255, 150, 40), 360, 690, radii=((20, 1.0), (7, 1.4), (3, 1.4)))
    progress_mock(im)
    return im


# --- C: Emblem (round badge over a night sea) -------------------------------------------------
def option_c():
    im = vgrad((15, 42, 84), (6, 18, 42))
    stars(im, 70, seed=3, ymax=520)
    # waves
    d = ImageDraw.Draw(im)
    for k, (y0, col) in enumerate([(930, (20, 60, 112, 255)), (985, (14, 44, 90, 255)), (1050, (9, 30, 66, 255))]):
        pts = [(x, y0 + 14 * math.sin(x / 55 + k * 1.7)) for x in range(0, W + 10, 10)]
        d.polygon(pts + [(W, H), (0, H)], fill=col)
    cx, cy, R = 360, 290, 210
    badge = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    bd = ImageDraw.Draw(badge)
    bd.ellipse((cx - R, cy - R, cx + R, cy + R), fill=(247, 244, 236, 255))
    bd.ellipse((cx - R + 14, cy - R + 14, cx + R - 14, cy + R - 14), outline=(255, 138, 31, 255), width=8)
    # clip puffin to the circle
    p = puffin_flat(380, face='#c9cdd3')
    layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    layer.alpha_composite(p, (cx - 215, cy - 170))
    mask = Image.new('L', (W, H), 0)
    ImageDraw.Draw(mask).ellipse((cx - R + 22, cy - R + 22, cx + R - 22, cy + R - 22), fill=255)
    layer.putalpha(ImageChops.multiply(layer.split()[3], mask))
    im.alpha_composite(badge)
    im.alpha_composite(layer)
    f1, f2 = font('Copperplate.ttc', 104, 1), font('Copperplate.ttc', 70)
    paste_c(im, text_layer('PUFFIN', f1, (255, 255, 255, 255), 14), W / 2, 545)
    paste_c(im, text_layer('GAMES', f2, (255, 160, 60, 255), 36), W / 2, 690)
    progress_mock(im)
    return im


# --- D: Minimal (dark, beak is the only color) -----------------------------------------------
def option_d():
    im = vgrad((24, 27, 34), (10, 11, 15))
    p = puffin_flat(440, body='#050608', face='#aeb3bb')
    # soft rim light behind the head
    glow = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse((90, 90, 630, 630), fill=(255, 138, 31, 40))
    im.alpha_composite(glow.filter(ImageFilter.GaussianBlur(70)))
    im.alpha_composite(p, (360 - 230 - 10, 90))
    f1, f2 = font('Futura.ttc', 74, 4), font('Futura.ttc', 40, 4)
    paste_c(im, text_layer('PUFFIN GAMES', f1, (244, 245, 247, 255), 10), W / 2, 600)
    ImageDraw.Draw(im).rectangle((W / 2 - 40, 740, W / 2 + 40, 744), fill=(255, 138, 31, 255))
    paste_c(im, text_layer('SMALL STUDIO, BIG ORBS', f2, (160, 166, 176, 255), 8), W / 2, 870) if False else None
    progress_mock(im)
    return im


def main():
    os.makedirs(OUT, exist_ok=True)
    opts = {'a_sunrise': option_a(), 'b_neon': option_b(), 'c_emblem': option_c(), 'd_minimal': option_d()}
    for name, im in opts.items():
        im.convert('RGB').save(os.path.join(OUT, f'option_{name}.png'))
    sheet = Image.new('RGB', (4 * 360 + 5 * 20, 640 + 40), (30, 30, 34))
    for i, im in enumerate(opts.values()):
        sheet.paste(im.convert('RGB').resize((360, 640), Image.LANCZOS), (20 + i * 380, 20))
    sheet.save(os.path.join(OUT, 'sheet.png'))


if __name__ == '__main__':
    main()
