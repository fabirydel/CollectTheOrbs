"""Neon-tube loading bar for the splash: 21 frames, 286x72 each (2x of the 143x36 logical size), laid out 7 columns x 3 rows.
Blue glass tube, yellow neon fill with a white-hot core, in the orbs' colours. Writes android/assets/res/hud/progress_bar.png."""
import os
import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage as ndi

FW, FH, N, COLS = 286, 72, 21, 7
RED, BLUE = np.array([1.0, 0.76, 0.12]), np.array([0.13, 0.83, 0.96])
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
OUT = os.path.join(ROOT, 'android', 'assets', 'res', 'hud', 'progress_bar.png')
SS = 4


def rr(w, h, box, r):
    im = Image.new('L', (w * SS, h * SS), 0)
    ImageDraw.Draw(im).rounded_rectangle([v * SS for v in box], r * SS, fill=255)
    return np.asarray(im.resize((w, h), Image.BOX), np.float32) / 255


def ring(w, h, box, r, t):
    a = rr(w, h, box, r)
    b = rr(w, h, (box[0] + t, box[1] + t, box[2] - t, box[3] - t), max(r - t, 1))
    return np.clip(a - b, 0, 1)


def blur(a, s): return ndi.gaussian_filter(a, s)


def frame(i):
    w, h = FW, FH
    f = i / (N - 1)
    L = np.zeros((h, w, 3), np.float32)
    glass_a = np.zeros((h, w), np.float32)
    # glass tube body: dark, slightly translucent, so the bar reads on any splash
    body = rr(w, h, (14, 14, w - 14, h - 14), 22)
    glass_a = np.maximum(glass_a, body * 0.62)
    # blue neon rim
    rim = ring(w, h, (14, 14, w - 14, h - 14), 22, 4.5)
    core = ring(w, h, (14, 14, w - 14, h - 14), 22, 2.2)
    L += (rim[..., None] * BLUE * 1.6 + core[..., None] * 0.85) * 0.9
    # red fill: a neon tube growing from the left, with a hot core and a bright end cap
    x0, x1 = 30, w - 30
    xe = x0 + (x1 - x0) * f
    if f > 0:
        fill = rr(w, h, (x0, 24, max(xe, x0 + 6), h - 24), 11)
        hot = rr(w, h, (x0 + 3, 31, max(xe - 3, x0 + 6), h - 31), 4)
        L += fill[..., None] * RED * 1.7 + hot[..., None] * np.array([1.0, 0.93, 0.62]) * 1.2
        cap = np.exp(-(((np.arange(w) - xe) / 7.0) ** 2))[None, :] * body * fill.max(0, keepdims=True).clip(0, 1) * 0
        L += blur(fill, 6)[..., None] * RED * 0.7
    else:
        pass
    # tick marks inside the empty part
    for k in range(1, 20):
        x = x0 + (x1 - x0) * k / 20
        if x > xe + 2:
            L[26:h - 26, int(x):int(x) + 1] += (BLUE * 0.18)
    # glow around the rim
    L += blur(rim, 5)[..., None] * BLUE * 0.9
    # compose to RGBA (light over a translucent dark body)
    dark = np.array([0.03, 0.04, 0.07], np.float32)
    rgb = dark * glass_a[..., None] + L * (1 - 0)
    a = np.clip(np.maximum(glass_a, L.max(-1)), 0, 1)
    col = np.where(a[..., None] > 1e-4, rgb / np.maximum(a[..., None], 1e-4), 0)
    out = np.dstack([np.clip(col, 0, 1), a])
    return Image.fromarray((out * 255 + 0.5).astype(np.uint8))


def main():
    rows = (N + COLS - 1) // COLS
    sheet = Image.new('RGBA', (FW * COLS, FH * rows), (0, 0, 0, 0))
    for i in range(N):
        sheet.paste(frame(i), ((i % COLS) * FW, (i // COLS) * FH))
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    sheet.save(OUT)
    print('wrote', OUT, sheet.size)


if __name__ == '__main__':
    main()
