"""Neon-tube loading bar for the splash: 21 frames, 286x72 each (2x of the 143x36 logical size), laid out 7 columns x 3 rows.
Rendered with the same glass-tube neon as the splash sign (cyberpunk lib): a blue rim tube and a yellow tube that lights up
from the left as loading progresses, the unlit part staying dead glass. Writes android/assets/res/hud/progress_bar.png."""
import os, sys
import numpy as np
from PIL import Image, ImageDraw
from scipy import ndimage as ndi
sys.path.insert(0, os.path.expanduser('~/.claude/skills/claude-drawing/lib'))
from cyberpunk import Cyberpunk

FW, FH, N, COLS = 286, 72, 21, 7
BLUE, YELLOW = '#22d3f5', '#ffc21f'
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
OUT = os.path.join(ROOT, 'android', 'assets', 'res', 'hud', 'progress_bar.png')
Z = 12.0
SS = 4
PAD = 60
RIM_R, FILL_R = 2.6, 3.6
M = 22        # rim inset from the frame edge; the glow is faded out over this margin


def rr(w, h, box, r):
    im = Image.new('L', (w * SS, h * SS), 0)
    ImageDraw.Draw(im).rounded_rectangle([v * SS for v in box], r * SS, fill=255)
    return np.asarray(im.resize((w, h), Image.BOX), np.float32) / 255


def rim_path(box, r, n=24):
    x0, y0, x1, y1 = box
    pts = []
    for (cx, cy, a0) in ((x1 - r, y0 + r, -90), (x1 - r, y1 - r, 0), (x0 + r, y1 - r, 90), (x0 + r, y0 + r, 180)):
        for a in np.linspace(a0, a0 + 90, n):
            pts.append((cx + r * np.cos(np.radians(a)), cy + r * np.sin(np.radians(a))))
    return np.array(pts, np.float32)


def render(i, tubes=True):
    """light-only render of one frame (the wall is black, so the image is the neon's own light)"""
    # padded canvas so the glow falls off naturally at the frame edge, cropped back afterwards
    W, H = FW + 2 * PAD, FH + 2 * PAD
    c = Cyberpunk(W, H, seed=3, record=False, fog=400.0)
    sl = c._box(0, 0, W - 1, H - 1)
    c._put(sl, np.ones((H, W), np.float32), np.zeros((H, W, 3), np.float32), 0.0, Z + 1.5, None)
    f = i / (N - 1)
    if not tubes:
        return np.asarray(c.composite().convert('RGB'), np.float32)[PAD:PAD + FH, PAD:PAD + FW] / 255
    x0, x1 = 42 + PAD, FW - 42 + PAD
    xe = x0 + (x1 - x0) * f
    mid = H / 2
    c.neon_path(rim_path((M + PAD, M + PAD, FW - M + PAD, FH - M + PAD), (FH - 2 * M) / 2), BLUE, Z, r=RIM_R, level=0.4, closed=True)
    # the yellow tube: lit up to xe, dead glass beyond
    if f > 0:
        c.neon_path(np.array([(x0, mid), (xe, mid)], np.float32), YELLOW, Z, r=FILL_R)
    if f < 1:
        c.neon_path(np.array([(max(xe, x0) + (0 if f > 0 else 0), mid), (x1, mid)], np.float32), YELLOW, Z, r=FILL_R, state='dead')
    return np.asarray(c.composite().convert('RGB'), np.float32)[PAD:PAD + FH, PAD:PAD + FW] / 255


BASE = None


def frame(i):
    global BASE
    if BASE is None: BASE = render(0, tubes=False)
    # subtract the empty wall's own haze and grain, so only the tubes' light remains
    L = np.clip(render(i) - BASE, 0, 1)
    # fade the light to nothing at the frame edge (a cut-off halo would show as a rectangle)
    ex = np.clip(np.minimum(np.arange(FW), FW - 1 - np.arange(FW)) / M, 0, 1)
    ey = np.clip(np.minimum(np.arange(FH), FH - 1 - np.arange(FH)) / M, 0, 1)
    fade = (ey[:, None] * ex[None, :]) ** 1.5
    L = L * (fade * fade * (3 - 2 * fade))[..., None]
    body = rr(FW, FH, (M, M, FW - M, FH - M), (FH - 2 * M) / 2)
    glass_a = body * 0.62
    dark = np.array([0.03, 0.04, 0.07], np.float32)
    rgb = dark * glass_a[..., None] + L
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
    if len(sys.argv) > 1:   # preview: a few frames over the splash wall colour
        outp = sys.argv[1]
        prev = Image.new('RGBA', (FW * 2, FH * 4), (14, 16, 24, 255))
        for k, i in enumerate((0, 5, 10, 20)):
            f = frame(i); prev.alpha_composite(f, (0, k * FH)); prev.alpha_composite(frame(min(i + 3, 20)), (FW, k * FH))
        prev = prev.resize((prev.width * 3, prev.height * 3), Image.LANCZOS); prev.save(outp)
    else:
        main()
