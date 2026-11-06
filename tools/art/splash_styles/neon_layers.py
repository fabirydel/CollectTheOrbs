"""Layered neon splash for animation: base image (wall + every tube unlit) and one additive glow layer per neon group.
Writes android/assets/res/splash.png (base), android/assets/res/splash/<group>.png and splash_layers.txt."""
from puffin_paths import *
from cyberpunk import Cyberpunk
from core import noise2d
from PIL import ImageFilter, ImageDraw as ID

RES = os.path.abspath(os.path.join(ART, '..', '..', 'android', 'assets', 'res'))
PINK, TEAL, ORANGE, YEL, BLUE2 = '#ff2a4e', '#22d3f5', '#ff8a1c', '#ffc21f', '#22d3f5'
Z = 12.0
GROUPS = ['ring', 'puffin', 'beak', 'name_a', 'name_f', 'games_a', 'games_m', 'frame']


def build(lit):
    c = Cyberpunk(W, H, seed=7, record=False, fog=400.0)
    sl = c._box(0, 0, W - 1, H - 1)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    base = 0.055 + 0.02 * noise2d(H, W, 90, 3, 5)
    streak = np.asarray(Image.fromarray(noise2d(H // 24 + 2, W, 5, 2, 11).astype(np.float32)).resize((W, H), Image.BILINEAR))
    alb = (base + 0.025 * streak)[..., None] * np.array([0.8, 0.9, 1.2], np.float32)
    seam = ((yy % 240) < 3) | ((xx % 360) < 3)
    alb = alb * np.where(seam, 0.45, 1.0)[..., None]
    c._put(sl, np.ones((H, W), np.float32), alb, 0.0, Z + 1.5, None)
    for box in ((cx - ER - 36, cy - ER - 36, cx + ER + 36, cy + ER + 36),):
        pm = Image.new('L', (W, H), 0); ID.Draw(pm).ellipse(box, fill=255)
        c._put(sl, np.asarray(pm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255, np.array([0.012, 0.012, 0.018], np.float32), 0.0, Z, None)
    bm = Image.new('L', (W, H), 0); ID.Draw(bm).rounded_rectangle((60, 1080, 660, 1300), 34, fill=255)
    c._put(sl, np.asarray(bm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255, np.array([0.012, 0.012, 0.018], np.float32), 0.0, Z, None)

    def tube(g, pts, col, r=3.4, closed=False, level=1.0):
        c.neon_path(np.asarray(pts, np.float32), col, Z, r=r, level=level, closed=closed, state='on' if g in lit else 'dead')
    outer = np.stack([cx + (ER + 22) * np.cos(np.radians(np.linspace(0, 360, 400))), cy + (ER + 22) * np.sin(np.radians(np.linspace(0, 360, 400)))], 1)[:-1]
    tube('ring', RING[:-1], PINK, r=5.2, closed=True)
    tube('ring', outer, BLUE2, r=2.2, closed=True, level=0.7)
    for pts, r in ((HEAD, 4.2), (FRONT, 4.2), (FACEP[(np.hypot(FACEP[:, 0] - cx, FACEP[:, 1] - cy) < ER - 4)], 3.2), (BREAST, 3.2)):
        tube('puffin', pts, TEAL, r=r)
    tube('puffin', EYE, PINK, r=2.6)
    tube('puffin', CHEEK, TEAL, r=2.6)
    tube('beak', np.vstack([TE, BASE, LE]), ORANGE, r=4.2, closed=True, level=0.9)
    for tp, bt in stripe:
        tube('beak', [tp, bt], YEL, r=3.4)
    ndead = lambda keep, n: tuple(i for i in range(n) if i not in keep)
    # lettering: a group lights its own characters, the rest stay dead glass
    for word, y, col, parts_ in (('FLUTTERING', 1149, PINK, (('name_a', (0, 1, 2, 4, 5, 6, 7, 8, 9)), ('name_f', (3,)))),
                                 ('PUFFINS', 1229, TEAL, (('games_a', (0, 1, 3, 4, 5, 6)), ('games_m', (2,))))):
        dead = tuple(i for i in range(len(word)) if not any(g in lit and i in idx for g, idx in parts_))
        c.neon_text(word, W / 2, y, 72, col, Z, spacing=0.06, dead=dead)
    tube('frame', np.array([(70, 1090), (650, 1090), (650, 1290), (70, 1290)]), YEL, r=2.4, closed=True, level=0.6)
    c.rain(far=0, mid=0, near=0, slant=0.08, rings=0)  # rain is animated in Splash.java (see rain_tiles.py)
    c.mist(y=H * 0.8, h=200, density=0.1)
    return np.asarray(c.composite().convert('RGB'), np.float32) / 255


def main():
    base = build(set())
    os.makedirs(os.path.join(RES, 'splash'), exist_ok=True)
    Image.fromarray((base * 255 + 0.5).astype(np.uint8)).save(os.path.join(RES, 'splash.png'))
    full = build(set(GROUPS))
    D = {g: np.clip(build({g}) - base, 0, 1) for g in GROUPS}
    tot = sum(D.values())
    want = np.clip(full - base, 0, 1)
    S = np.clip(want / np.maximum(tot, 1e-3), 0, 1)          # tone mapping is not additive: scale the layers so that all-on == the full render
    S = np.stack([ndi.gaussian_filter(S[..., k], 1.0) for k in range(3)], -1)
    lines = []
    total = base.copy()
    for g in GROUPS:
        d = np.clip(D[g] * S, 0, 1)
        total += d
        m = d.max(-1) > 2 / 255
        ys, xs = np.nonzero(m)
        x0, x1, y0, y1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
        Image.fromarray((d[y0:y1, x0:x1] * 255 + 0.5).astype(np.uint8)).save(os.path.join(RES, 'splash', g + '.png'))
        lines.append(f'{g} {x0} {y0} {x1 - x0} {y1 - y0}')
        print(g, x0, y0, x1 - x0, y1 - y0)
    open(os.path.join(RES, 'splash_layers.txt'), 'w').write('\n'.join(lines) + '\n')
    Image.fromarray((np.clip(total, 0, 1) * 255 + 0.5).astype(np.uint8)).save(os.path.join(ART, 'splash_options3', 'neon_layers_sum.png'))
    Image.fromarray((np.clip(full, 0, 1) * 255 + 0.5).astype(np.uint8)).save(os.path.join(ART, 'splash_options3', 'neon_layers_full.png'))


if __name__ == '__main__':
    main()
