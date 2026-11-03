"""Lays text out with a built neon font (.fnt + atlas) on a dark background, to check the alphabet.
Usage: python3 tools/art/neon_alphabet_preview.py out.png [font-name]"""
import os, re, sys
from PIL import Image
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
FD = os.path.join(ROOT, 'android', 'assets', 'fonts')
out = sys.argv[1]
name = sys.argv[2] if len(sys.argv) > 2 else 'prototype96grey'
txt = open(os.path.join(FD, name + '.fnt'), encoding='latin-1').read()
chars = {}
for line in txt.splitlines():
    if line.startswith('char '):
        kv = {k: int(v) for k, v in re.findall(r'(\w+)=(-?\d+)', line)}
        chars[kv['id']] = kv
atlas = Image.open(os.path.join(FD, re.search(r'file="([^"]+)"', txt).group(1))).convert('RGBA')
pad = int(re.search(r'padding=(\d+)', txt).group(1))
base = int(re.search(r'base=(\d+)', txt).group(1))
lh = int(re.search(r'lineHeight=(\d+)', txt).group(1)) - 2 * pad
LINES = ['ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz', '0123456789 .,:;!?-+/()%&\'"', 'Collect The Orbs', 'Best Score: 105   Settings']
W = 2200
img = Image.new('RGBA', (W, (lh + 2 * pad) * len(LINES) + 40), (6, 8, 14, 255))
y = 20
for L in LINES:
    x = 30
    for ch in L:
        c = chars.get(ord(ch))
        if not c: continue
        if c['width'] and c['height']:
            g = atlas.crop((c['x'], c['y'], c['x'] + c['width'], c['y'] + c['height']))
            img.alpha_composite(g, (int(x + c['xoffset']), int(y + c['yoffset'])))
        x += c['xadvance']
    y += lh + 2 * pad
img.save(out)
