"""Adds the Spanish accented letters (a e i o u with an acute accent, n with a tilde) and the inverted ! and ? to the grey
neon bitmap fonts, built from the existing neon glyphs plus a tube-lit accent stroke. Appends to the atlas and .fnt in
place; running it twice does nothing the second time."""
import math, os, re, sys
import numpy as np
from PIL import Image, ImageDraw
import neon_ui as U
from neon_tube import tube_rgba
from neon import TUBE_WHITE

FONTS = os.path.join(U.ASSETS, 'fonts')
S = 4


def parse(path):
    txt = open(path, encoding='latin-1').read()
    chars = {}
    for line in txt.splitlines():
        if line.startswith('char '):
            c = {k: int(v) for k, v in re.findall(r'(\w+)=(-?\d+)', line)}
            chars[c['id']] = c
    return txt, chars


def stroke_mask(W, H, pts, sw):
    big = Image.new('L', (W * S, H * S), 0)
    d = ImageDraw.Draw(big)
    p = [(x * S, y * S) for x, y in pts]
    d.line(p, fill=255, width=int(sw * S), joint='curve')
    r = sw * S / 2
    for x, y in (p[0], p[-1]):
        d.ellipse((x - r, y - r, x + r, y + r), fill=255)
    return big.resize((W, H), Image.LANCZOS)


def run(fnt, png):
    path = os.path.join(FONTS, fnt)
    txt, chars = parse(path)
    if 225 in chars:
        print(fnt, 'already has accents')
        return
    atlas = Image.open(os.path.join(FONTS, png)).convert('RGBA')
    size = int(re.search(r'size=(\d+)', txt).group(1))
    P = int(re.search(r'padding=(\d+)', txt).group(1))
    sw = 0.055 * size
    cap = chars[ord('i')]                       # tall cell: cap/ascender top
    CH, cyoff = cap['height'], cap['yoffset']
    maxy = max(c['y'] + c['height'] for c in chars.values())
    new = []

    def crop(c):
        return atlas.crop((c['x'], c['y'], c['x'] + c['width'], c['y'] + c['height']))

    def lit(mask):
        return tube_rgba(mask, TUBE_WHITE, sw, sig=(0.3, 0.9, 2.2), fade=P)

    for ch, base, kind in [('á', 'a', 'acute'), ('é', 'e', 'acute'), ('í', 'i', 'acute'),
                           ('ó', 'o', 'acute'), ('ú', 'u', 'acute'), ('ñ', 'n', 'tilde')]:
        b = chars[ord(base)]
        W = b['width']
        img = Image.new('RGBA', (W, CH), (0, 0, 0, 0))
        g = crop(b)
        top = b['yoffset'] - cyoff
        if base == 'i':                          # keep the stem, drop the dot (the accent replaces it)
            g = g.copy()
            arr = np.asarray(g).copy()
            arr[:int(P + 0.20 * size)] = 0
            g = Image.fromarray(arr)
            top = b['yoffset'] - cyoff
        img.alpha_composite(g, (0, top))
        cx = W / 2.0
        inkTop = top + P                          # top of the lowercase ink (for i: stem top)
        y0 = P + 0.06 * size if base != 'i' else P + 0.06 * size
        if kind == 'acute':
            pts = [(cx - 0.06 * size, P + 0.115 * size), (cx + 0.06 * size, P + 0.015 * size)]
        else:
            ty = P + 0.07 * size
            pts = [(cx - 0.15 * size + t * 0.30 * size / 12, ty - math.sin(t / 12 * 2 * math.pi) * 0.035 * size) for t in range(13)]
        img.alpha_composite(lit(stroke_mask(W, CH, pts, sw)))
        c = dict(b)
        c.update(id=ord(ch), height=CH, yoffset=cyoff)
        new.append((c, img))

    for ch, base in (('¡', '!'), ('¿', '?')):
        b = chars[ord(base)]
        img = crop(b).transpose(Image.FLIP_TOP_BOTTOM)
        c = dict(b)
        c['id'] = ord(ch)
        new.append((c, img))

    x, y, rowh = 0, maxy + 2, 0
    lines = []
    for c, img in new:
        if x + img.width > atlas.width:
            x, y, rowh = 0, y + rowh + 2, 0
        atlas.paste(img, (x, y))
        lines.append('char id=%d x=%d y=%d width=%d height=%d xoffset=%d yoffset=%d xadvance=%d page=0 chnl=0' %
                     (c['id'], x, y, img.width, img.height, c['xoffset'], c['yoffset'], c['xadvance']))
        x += img.width + 2
        rowh = max(rowh, img.height)
    assert y + rowh <= atlas.height, 'atlas full'
    atlas.save(os.path.join(FONTS, png))
    count = int(re.search(r'chars count=(\d+)', txt).group(1)) + len(lines)
    txt = re.sub(r'chars count=\d+', 'chars count=%d' % count, txt)
    i = txt.rindex('\nchar ')
    j = txt.index('\n', i + 1)
    txt = txt[:j] + '\n' + '\n'.join(lines) + txt[j:]
    open(path, 'w', encoding='latin-1').write(txt)
    print(fnt, 'added', len(lines))


if __name__ == '__main__':
    run('prototype96grey.fnt', 'neon_prototype96grey.png')
    # prototype96grey2.fnt shares neon_prototype96grey.png (the two pages were identical copies): after running this,
    # copy the new 'char' lines from prototype96grey.fnt into prototype96grey2.fnt
