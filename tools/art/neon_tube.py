"""Glass-tube neon shading for masks (used by the fonts and the buttons), matching the splash sign: a white-hot core
down the middle of each stroke, a saturated body toward the edges, and a soft coloured bloom around it.
tube_rgba(mask, color, sw) -> RGBA whose colour is un-premultiplied light, so normal alpha blending over a dark scene
reproduces the glow."""
import numpy as np
from PIL import Image
from scipy import ndimage as ndi


def smooth(x, a, b):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def tube_rgba(mask, color, sw, sig=(0.3, 0.9, 2.2), fade=0, strength=1.0):
    """mask: PIL 'L' (the strokes); color: 0-255 rgb; sw: stroke width in px; sig: bloom sigmas in stroke widths;
    fade: px over which the image edge fades the light out (keeps the bloom from showing a cut-off rectangle)"""
    a = np.asarray(mask, np.float32) / 255
    hard = a > 0.43
    d = ndi.distance_transform_edt(hard)
    hw = ndi.maximum_filter(d, size=int(max(3, sw * 2.2)) | 1)
    t = np.clip(d / np.maximum(hw, 1), 0, 1)
    core = smooth(t, 0.3, 0.85) * a
    core = ndi.gaussian_filter(core, max(0.4, sw * 0.06))
    c = np.array(color, np.float32) / 255
    strength *= 1 - 0.6 * float(c.min())    # white light blooms far brighter than coloured light, so tame it
    sat = c * 0.92
    lite = c + (1 - c) * 0.5
    k = smooth(t, 0.0, 0.75)[..., None]
    body = (sat * (1 - k) + lite * k) * a[..., None]
    halo = sum(w * ndi.gaussian_filter(a, s * sw) for s, w in zip(sig, (0.8, 1.0, 0.9)))
    P = halo[..., None] * c * 1.9 * strength + body * (1 - core[..., None]) + core[..., None]
    P = (1 - np.exp(-P * 1.3)) / (1 - np.exp(-1.3))
    if fade:
        h, w = a.shape
        ex = np.clip(np.minimum(np.arange(w), w - 1 - np.arange(w)) / fade, 0, 1)
        ey = np.clip(np.minimum(np.arange(h), h - 1 - np.arange(h)) / fade, 0, 1)
        f = ey[:, None] * ex[None, :]
        P = P * (f * f * (3 - 2 * f))[..., None]
    al = np.clip(P.max(-1), 0, 1)
    rgb = np.where(al[..., None] > 1e-4, P / np.maximum(al[..., None], 1e-4), 0)
    out = np.dstack([np.clip(rgb, 0, 1), al])
    return Image.fromarray((out * 255 + 0.5).astype(np.uint8))


def thin(hard):
    """Zhang-Suen thinning of a boolean image to a one-pixel skeleton"""
    im = np.pad(hard.astype(np.uint8), 1)
    while True:
        changed = False
        for step in (0, 1):
            p2, p3, p4, p5 = im[:-2, 1:-1], im[:-2, 2:], im[1:-1, 2:], im[2:, 2:]
            p6, p7, p8, p9 = im[2:, 1:-1], im[2:, :-2], im[1:-1, :-2], im[:-2, :-2]
            c = im[1:-1, 1:-1]
            nb = p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9
            seq = [p2, p3, p4, p5, p6, p7, p8, p9, p2]
            tr = sum(((seq[i] == 0) & (seq[i + 1] == 1)).astype(np.uint8) for i in range(8))
            if step == 0:
                cond = (p2 * p4 * p6 == 0) & (p4 * p6 * p8 == 0)
            else:
                cond = (p2 * p4 * p8 == 0) & (p2 * p6 * p8 == 0)
            kill = (c == 1) & (nb >= 2) & (nb <= 6) & (tr == 1) & cond
            if kill.any():
                im[1:-1, 1:-1][kill] = 0
                changed = True
        if not changed:
            return im[1:-1, 1:-1].astype(bool)


def centre_line(mask, width):
    """the strokes of `mask` reduced to a single-line tube `width` px wide (what a neon bender would bend)"""
    a = np.asarray(mask, np.float32) / 255
    big = Image.fromarray((a * 255).astype(np.uint8)).resize((mask.width * 2, mask.height * 2), Image.LANCZOS)
    b = ndi.gaussian_filter(np.asarray(big, np.float32) / 255, 1.2) > 0.5
    sk = thin(b)
    # width is the stroke width in output px; the skeleton lives at 2x, so the radius there equals it
    line = (ndi.distance_transform_edt(~sk) <= width * 0.99).astype(np.float32)
    line = ndi.gaussian_filter(line, 0.8)
    out = Image.fromarray((line * 255).astype(np.uint8)).resize(mask.size, Image.LANCZOS)
    return out
