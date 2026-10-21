"""Neon redraw of the Help page images (res/objects: inst, padsControls, orbs, items; EN + ES).

Run from the repo root:  python3 tools/art/neon_help.py
"""
import math, os, sys
import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
import neon_ui as U
import orb as O
from neon import CYAN, RED, GOLD, PURPLE, WHITE, NEUTRAL, glow, tint

BODY = (214, 215, 218)
W = 1344

TEXT = {
    'en': {
        'inst': [
            ('Objective:', "collect as many red and blue orbs as possible in EQUAL amounts before losing all your energy. "
                           "Collect blue orbs with your ship's blue shield and red orbs with the red one."),
            ('Score:', "it will be determined by the amount of orbs of whichever color you've collected the LEAST."),
            ('Energy:', "your energy bar will DECREASE if and when you collect a blue orb with your ship's red shield and "
                        "vice versa. It will INCREASE by collecting an energy orb."),
            ('Items:', "use them to your advantage, they don't appear very frequently so when you see one, GET IT."),
            ('Upgrades:', "every energy orb you collect can later be traded for upgrades for you and your ship, "
                          "which are vital to improve your performance."),
        ],
        'orbs': ['Blue Orb', 'Red Orb', 'Item Orb', 'Energy Orb'],
        'items': ['Blue orbs converter', 'Red orbs converter', 'Blue orbs have a value of 2', 'Red orbs have a value of 2',
                  "Orb's speed decreases", 'No damage from blue orbs', 'No damage from red orbs',
                  'Blue orbs have a value of 3', 'Red orbs have a value of 3', 'Collects both orbs'],
        'pads': ['Left', 'Right', 'Use Item', 'Switch\nShield'],
    },
    'es': {
        'inst': [
            ('Objetivo:', "recolectar cuantos orbes rojos y azules puedas en IGUALES cantidades antes de perder toda tu "
                          "energia. Recoge orbes azules con el escudo azul de tu nave y orbes rojos con el escudo rojo."),
            ('Marca:', "sera determinado por la cantidad de orbes del color del cual hayas conseguido MENOS."),
            ('Energia:', "tu energia DECREMENTARA cuando intentes recolectar un orbe azul con el escudo rojo de tu nave y "
                         "viceversa. INCREMENTARA al recoger un orbe de energia."),
            ('Objetos:', "usalos a tu favor, no aparecen muy frecuentemente asi que si ves uno, CONSIGUELO."),
            ('Mejoras:', "cada orbe de energia que recojas puede ser canjeado por mejoras para ti y tu nave, las cuales "
                         "son vitales para mejorar tu rendimiento."),
        ],
        'orbs': ['Orbe Azul', 'Orbe Rojo', 'Orbe de Objeto', 'Orbe de Energia'],
        'items': ['Convertidor orbes azules', 'Convertidor orbes rojos', 'Orbes azules valen por 2', 'Orbes rojos valen por 2',
                  'Velocidad de los orbes disminuye', 'Inmune a orbes azules', 'Inmune a orbes rojos',
                  'Orbes azules valen por 3', 'Orbes rojos valen por 3', 'Recoge ambos orbes'],
        'pads': ['Izquierda', 'Derecha', 'Usar Objeto', 'Cambiar\nEscudo'],
    },
}


def eorb(body, col):
    """The animated-orb look (soft edge + glow) as a still, with an orb body `body` logical px across.
    The image is larger than the body (it carries the glow): paste it centered."""
    n = int(round(body * N.U / 0.5625))
    return O.render(n, col, 0.12, ss=2)


def px(v):
    return int(round(v * N.U))


def wrap(font, size, words, max_w, first_indent=0):
    """Greedy word wrap. Returns list of lines (lists of words)."""
    lines, cur, cur_w = [], [], first_indent
    space = font.width(' ', size)
    for w in words:
        ww = font.width(w, size)
        if cur and cur_w + space + ww > max_w:
            lines.append(cur)
            cur, cur_w = [w], ww
        else:
            cur_w = cur_w + (space if cur else 0) + ww
            cur.append(w)
    if cur:
        lines.append(cur)
    return lines


def draw_words(layers, font, size, x, y, words, space):
    for word, kind in words:
        layers[kind].paste(255, (int(x), int(y)), font.mask(word, size))
        x += font.width(word, size) + space
    return x


def new_layers(w, h):
    return {k: Image.new('L', (w, h), 0) for k in ('body', 'head', 'emph')}


def composite_layers(layers, size):
    out = Image.new('RGBA', size, (0, 0, 0, 0))
    for kind, col, st in (('body', BODY, 0.35), ('head', NEUTRAL, 1.0), ('emph', GOLD, 0.9)):
        m = layers[kind]
        halo = glow(Image.merge('RGBA', [Image.new('L', size, 0)] * 3 + [m]), 1.6, col, st)
        core = Image.new('RGBA', size, U.light(col, 0.55 if kind != 'body' else 0.0) + (255,))
        core.putalpha(m)
        out = Image.alpha_composite(out, Image.alpha_composite(halo, core))
    return out


def inst_image(lang, name):
    font = U.BMFont('fonts/prototype128blueWhite')
    size = 60
    s = size * N.U / font.size
    line_h = font.line_h * s * 1.02
    pad = px(10)
    paras = TEXT[lang]['inst']
    layout, y = [], pad
    for head, body in paras:
        indent = font.width(head + ' ', size)
        lines = wrap(font, size, body.split(), px(W) - 2 * pad - indent)
        layout.append((head, indent, lines, y))
        y += line_h * max(1, len(lines)) + line_h * 0.35
    H = int(y + pad)
    layers = new_layers(px(W), H)
    for head, indent, lines, y0 in layout:
        font_mask = font.mask(head, size)
        layers['head'].paste(255, (pad, int(y0)), font_mask)
        for li, line in enumerate(lines):
            x = pad + indent
            for word in line:
                kind = 'emph' if (len(word.strip('.,')) >= 4 and word.strip('.,').isupper()) else 'body'
                layers[kind].paste(255, (int(x), int(y0 + li * line_h)), font.mask(word, size))
                x += font.width(word, size) + font.width(' ', size)
    im = composite_layers(layers, (px(W), H))
    N.finish(im, W, H / N.U, 'objects/%s.png' % name)


def pad_label(font, text, size):
    return font.mask(text, size)


def controls_image(lang, name):
    font = U.BMFont('fonts/prototype128blueWhite')
    w, h = 1280, 980
    im = N.canvas(w, h)
    t = TEXT[lang]['pads']
    pads = [(20, 20, 270, 940, NEUTRAL, t[0], -1), (990, 20, 270, 940, NEUTRAL, t[1], 1),
            (310, 20, 660, 460, PURPLE, t[2], 0), (310, 500, 660, 460, NEUTRAL, t[3], 0)]
    for x, y, pw, ph, col, label, d in pads:
        tl = U.tile(pw, ph, (col,), None, 54, alpha=165, bw=7.0, glow_r=5)
        cx, cy = tl.width / 2, tl.height / 2
        if d:
            lay = Image.new('RGBA', tl.size, (0, 0, 0, 0))
            dr = ImageDraw.Draw(lay)
            s = px(62)
            pts = [(cx - d * s * 0.5, cy - px(230) - s), (cx + d * s * 0.5, cy - px(230)), (cx - d * s * 0.5, cy - px(230) + s)]
            dr.line(pts, fill=WHITE + (255,), width=px(16), joint='curve')
            for (a, b) in pts:
                dr.ellipse((a - px(8), b - px(8), a + px(8), b + px(8)), fill=WHITE + (255,))
            tl.alpha_composite(Image.alpha_composite(glow(lay, 10, col, 1.0), lay))
            m = font.mask(label, 64).rotate(90 if d < 0 else -90, expand=True)
            U.center_text(tl, m, cx, cy + px(90), col)
        else:
            U.center_text(tl, font.mask(label, 84), cx, cy, col)
        im.alpha_composite(tl, (px(x), px(y)))
    N.finish(im, w, h, 'objects/%s.png' % name)


def orbs_image(lang, name):
    font = U.BMFont('fonts/prototype128blueWhite')
    w, h = 1281, 418
    im = N.canvas(w, h)
    labels = TEXT[lang]['orbs']
    cols = [CYAN, RED, PURPLE, GOLD]
    size = 66 if lang == 'en' else 54
    for i, (lab, col) in enumerate(zip(labels, cols)):
        cx = 20 + (i % 2) * 650
        cy = 105 + (i // 2) * 210
        d = 170
        o = eorb(d, col)
        im.alpha_composite(o, (px(cx + d / 2 + 8) - o.width // 2, px(cy) - o.height // 2))
        m = font.mask(lab, size)
        box = m.getbbox()
        m = m.crop(box)
        U.put_text(im, m, px(cx + d + 50), px(cy) - m.height / 2, col)
    N.finish(im, w, h, 'objects/%s.png' % name)


def shield_icon(col, size):
    n = px(size)
    lay = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    c = n / 2
    pts = [(-0.80, -0.70), (0.0, -0.58), (0.80, -0.70)]
    for i in range(1, 13):
        t = i / 12
        pts.append((0.80 * (1 - t ** 2.4) ** 0.7, -0.70 + 1.65 * t))
    pts += [(-x, y) for x, y in reversed(pts[3:-1])]
    k = n / 2 * 0.92
    pts = [(c + x * k, c + y * k) for x, y in pts]
    fill = Image.new('RGBA', (n, n), col + (90,))
    mask = Image.new('L', (n, n), 0)
    ImageDraw.Draw(mask).polygon(pts, fill=255)
    fill.putalpha(ImageChops.multiply(fill.split()[3], mask))
    line = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    dl = ImageDraw.Draw(line)
    dl.line(pts + [pts[0]], fill=U.light(col, 0.35) + (255,), width=px(7), joint='curve')
    dl.line([(c, pts[0][1] + px(6)), (c, pts[3][1] - px(10))], fill=U.light(col, 0.35) + (160,), width=px(4))
    out = Image.alpha_composite(glow(line, 6, col, 1.0), fill)
    return Image.alpha_composite(out, line)


def arrow_icon(c1, c2, w, h):
    n = (px(w), px(h))
    arr = np.zeros((n[1], n[0], 4), dtype=np.float32)
    t = np.linspace(0, 1, n[0])[None, :, None]
    col = np.array(c1)[None, None, :] * (1 - t) + np.array(c2)[None, None, :] * t
    arr[..., :3] = col
    arr[..., 3] = 255
    im = Image.fromarray(arr.astype(np.uint8))
    m = Image.new('L', n, 0)
    d = ImageDraw.Draw(m)
    hh = n[1]
    d.rectangle((0, hh * 0.32, n[0] * 0.62, hh * 0.68), fill=255)
    d.polygon([(n[0] * 0.55, 0), (n[0], hh / 2), (n[0] * 0.55, hh)], fill=255)
    im.putalpha(m)
    return Image.alpha_composite(glow(im, 5, c2, 0.8), im)


def chevrons_icon(size):
    n = px(size)
    lay = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    c = n / 2
    for dx in (-0.2, 0.28):
        pts = [(c + n * (dx + 0.14), c - n * 0.32), (c + n * (dx - 0.14), c), (c + n * (dx + 0.14), c + n * 0.32)]
        d.line(pts, fill=WHITE + (255,), width=px(12), joint='curve')
        for a, b in pts:
            d.ellipse((a - px(6), b - px(6), a + px(6), b + px(6)), fill=WHITE + (255,))
    return Image.alpha_composite(glow(lay, 8, NEUTRAL, 1.0), lay)


def cluster_icon(col, count, tag, font):
    n = px(250)
    im = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    spots = {2: [(0.36, 0.40), (0.62, 0.46)], 3: [(0.5, 0.28), (0.34, 0.5), (0.66, 0.5)]}[count]
    for fx, fy in spots:
        o = eorb(118, col)
        im.alpha_composite(o, (int(n * fx - o.width / 2), int(n * fy - o.height / 2)))
    m = font.mask(tag, 56).crop(font.mask(tag, 56).getbbox())
    U.put_text(im, m, (n - m.width) / 2, n * 0.84 - m.height / 2, col)
    return im


def items_image(lang, name):
    font = U.BMFont('fonts/prototype128blueWhite')
    texts = TEXT[lang]['items']
    row_h, w = 290, 1344
    h = row_h * len(texts)
    im = N.canvas(w, h)
    size = 60
    x_text = 390
    space = font.width(' ', size)
    for i, text in enumerate(texts):
        cy = row_h * i + row_h / 2
        ic = None
        icx = 175
        if i in (0, 1):
            a, b = (CYAN, RED) if i == 0 else (RED, CYAN)
            o1, o2 = eorb(100, a), eorb(100, b)
            ar = arrow_icon(a, b, 70, 44)
            im.alpha_composite(o1, (px(icx - 87.5) - o1.width // 2, px(cy) - o1.height // 2))
            im.alpha_composite(ar, (px(icx - 52), px(cy) - ar.height // 2))
            im.alpha_composite(o2, (px(icx + 90.5) - o2.width // 2, px(cy) - o2.height // 2))
        elif i in (2, 3, 7, 8):
            ic = cluster_icon(CYAN if i in (2, 7) else RED, 2 if i < 4 else 3, 'X2' if i < 4 else 'X3', font)
        elif i == 4:
            ic = chevrons_icon(250)
        elif i in (5, 6, 9):
            ic = shield_icon(CYAN if i == 5 else RED if i == 6 else GOLD, 250)
        if ic is not None:
            im.alpha_composite(ic, (px(icx) - ic.width // 2, px(cy) - ic.height // 2))
        lines = wrap(font, size, text.split(), px(w - x_text - 20))
        lh = font.line_h * size * N.U / font.size * 0.98
        y0 = px(cy) - lh * len(lines) / 2
        layers = new_layers(im.width, im.height)
        for li, line in enumerate(lines):
            x = px(x_text)
            for word in line:
                layers['body'].paste(255, (int(x), int(y0 + li * lh)), font.mask(word, size))
                x += font.width(word, size) + space
        txt = composite_layers(layers, im.size)
        im = Image.alpha_composite(im, txt)
    N.finish(im, w, h, 'objects/%s.png' % name)


def converter_icon(a, b):
    n = px(250)
    im = Image.new('RGBA', (n, n), (0, 0, 0, 0))
    o1, o2 = eorb(84, a), eorb(84, b)
    ar = arrow_icon(a, b, 54, 42)
    im.alpha_composite(o1, (px(46) - o1.width // 2, n // 2 - o1.height // 2))
    im.alpha_composite(ar, (n // 2 - ar.width // 2, n // 2 - ar.height // 2))
    im.alpha_composite(o2, (px(204) - o2.width // 2, n // 2 - o2.height // 2))
    return im


def item_icons_sheet():
    """res/objects/objects.png: the ten in-game item icons, 160x160 frames, in the original order."""
    font = U.BMFont('fonts/prototype128blueWhite')
    icons = [converter_icon(CYAN, RED), converter_icon(RED, CYAN),
             cluster_icon(CYAN, 2, 'X2', font), cluster_icon(RED, 2, 'X2', font),
             chevrons_icon(250),
             shield_icon(CYAN, 250), shield_icon(RED, 250),
             cluster_icon(CYAN, 3, 'X3', font), cluster_icon(RED, 3, 'X3', font),
             shield_icon(GOLD, 250)]
    # The ring (res/objects/ring.png) has an inner diameter of ~118 of the 160px frame. Fit each icon's
    # lit shape inside a ~92px circle and center it on its own bounds.
    sheet = N.canvas(1600, 160)
    for i, ic in enumerate(icons):
        alpha = ic.split()[3].point(lambda v: 255 if v > 170 else 0)
        box = alpha.getbbox()
        pad = px(8)
        box = (max(0, box[0] - pad), max(0, box[1] - pad), min(ic.width, box[2] + pad), min(ic.height, box[3] + pad))
        ic = ic.crop(box)
        w, h = ic.size
        diag = (w * w + h * h) ** 0.5
        k = min(px(92) / max(w, h), px(96) / diag)
        small = ic.resize((max(1, int(w * k)), max(1, int(h * k))), Image.LANCZOS)
        cx, cy = px(160) * i + px(160) // 2, px(160) // 2
        sheet.alpha_composite(small, (cx - small.width // 2, cy - small.height // 2))
    N.finish(sheet, 1600, 160, 'objects/objects.png')


if __name__ == '__main__':
    N.set_scale(1)
    for lang, suffix in (('en', ''), ('es', 'Esp')):
        inst_image(lang, 'inst' + suffix)
        controls_image(lang, 'padsControls' + suffix)
        orbs_image(lang, 'orbs' + suffix)
        items_image(lang, 'items' + suffix)
    N.set_scale(2)
    item_icons_sheet()
    N.write_manifest()
    print('done')
