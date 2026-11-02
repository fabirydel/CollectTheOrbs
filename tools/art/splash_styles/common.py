"""Shared helpers for the Puffin Games splash style renders (720x1440, central 720x1280 is the safe zone)."""
import os, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from scipy import ndimage as ndi

HERE = os.path.dirname(os.path.abspath(__file__))
ART = os.path.abspath(os.path.join(HERE, '..'))
sys.path.insert(0, ART)
sys.path.insert(0, os.path.expanduser('~/.claude/skills/claude-drawing/lib'))
import splash as S0
from splash import BODY, BREAST, FACE, BEAK, text_layer

W, H = 720, 1440
OUT = os.path.join(ART, 'splash_options2')
ASSETS = os.path.join(ART, '..', '..', 'android', 'assets', 'res')
SUP = '/System/Library/Fonts/Supplemental/'
BAR_Y = int(H * 2 / 3)  # the loading bar is centred here (2/3 down), keep it clear

_CLIP = f'<defs><clipPath id="b"><path d="{BEAK}"/></clipPath></defs>'
PARTS = {
    'body': f'<path d="{BODY}"/>',
    'breast': f'<path d="{BREAST}"/>',
    'face': f'<path d="{FACE}"/>',
    'beak': f'<path d="{BEAK}"/>',
    'tip': _CLIP + '<g clip-path="url(#b)"><rect x="0" y="40" width="58" height="130"/></g>',
    'white': _CLIP + '<g clip-path="url(#b)"><path d="M 58 60 L 54 170 L 66 170 L 72 60 Z"/></g>',
    'orange': _CLIP + '<g clip-path="url(#b)"><path d="M 72 60 L 66 170 L 86 170 L 92 60 Z"/></g>',
    'yellow': _CLIP + '<g clip-path="url(#b)"><path d="M 92 60 L 86 170 L 110 170 L 110 60 Z"/></g>',
    'rosette': '<circle cx="108" cy="122" r="9"/>',
    'eye': '<circle cx="128" cy="92" r="8.5"/>',
    'pupil': '<circle cx="128" cy="92" r="4.6"/>',
    'cheek': '<path d="M 128 98 C 150 98 172 104 186 112" fill="none" stroke="#000" stroke-width="3" stroke-linecap="round"/>',
}


def puffin(px, py, size, flip=False):
    """Float masks (H, W) of each puffin part, bust placed with its top-left at (px, py)."""
    out = {}
    for k, inner in PARTS.items():
        svg = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 300">{inner}</svg>'
        a = np.asarray(S0.render_svg(svg, size).split()[3], dtype=np.float32) / 255
        if flip:
            a = a[:, ::-1]
        m = np.zeros((H, W), np.float32)
        y1, x1 = min(H, py + size), min(W, px + size)
        m[max(py, 0):y1, max(px, 0):x1] = a[max(-py, 0):y1 - py, max(-px, 0):x1 - px]
        out[k] = m
    out['all'] = np.maximum.reduce([out['body'], out['face'], out['beak'], out['breast']])
    return out


def edge(mask, w=3, inner=False):
    """Ring around a mask (outside by default)."""
    b = mask > 0.5
    d = ndi.binary_dilation(b, iterations=w)
    if inner:
        return (b & ~ndi.binary_erosion(b, iterations=w)).astype(np.float32)
    return (d & ~b).astype(np.float32)


def font(name, size, index=0):
    return ImageFont.truetype(SUP + name, size, index=index)


def paste_c(base, layer, cx, top):
    base.alpha_composite(layer, (int(cx - layer.width / 2), int(top)))


def progress_mock(im):
    sheet = Image.open(os.path.join(ASSETS, 'hud', 'progress_bar.png')).convert('RGBA')
    fw = sheet.width // 21
    fr = sheet.crop((fw * 10, 0, fw * 11, sheet.height)).resize((int(143 * 1.75), int(36 * 1.75)), Image.LANCZOS)
    im.alpha_composite(fr, (W // 2 - fr.width // 2, BAR_Y - fr.height // 2))


def save(im, name, mock=True):
    os.makedirs(OUT, exist_ok=True)
    im = im.convert('RGBA')
    clean = im.copy()
    if mock:
        progress_mock(im)
    clean.convert('RGB').save(os.path.join(OUT, f'{name}.png'))
    im.convert('RGB').save(os.path.join(OUT, f'{name}_preview.png'))
