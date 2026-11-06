"""Puffin emblem (ring + puffin, no lettering) as a square 512 px icon: store/puffin-emblem-512.png"""
from neon_layers import *

EMBLEM = ['ring', 'puffin', 'beak']
HALF = 320


def main():
    full = build(set(EMBLEM))
    x0, y0 = int(cx - HALF), int(cy - HALF)
    crop = full[y0:y0 + 2 * HALF, x0:x0 + 2 * HALF]
    im = Image.fromarray((np.clip(crop, 0, 1) * 255 + 0.5).astype(np.uint8)).resize((512, 512), Image.LANCZOS)
    out = os.path.abspath(os.path.join(RES, '..', '..', '..', 'store', 'puffin-emblem-512.png'))
    im.save(out)
    print(out, im.size)


main()
