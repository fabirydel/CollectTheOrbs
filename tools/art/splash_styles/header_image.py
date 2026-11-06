"""Play developer-page header: emblem + FLUTTERING PUFFINS sign on the neon wall, 4096x2304 JPEG (under 1 MB): store/puffin-header-4096x2304.jpg"""
from neon_layers import *

W2, H2 = 2048, 1152
S = 1.12                    # emblem scale
EMB = (600, 576)            # where the emblem centre goes
SIGN = (1415, 576)          # sign centre
K = 1.28                    # sign scale


def main():
    c = Cyberpunk(W2, H2, seed=7, record=False, fog=400.0)
    sl = c._box(0, 0, W2 - 1, H2 - 1)
    yy, xx = np.mgrid[0:H2, 0:W2].astype(np.float32)
    base = 0.055 + 0.02 * noise2d(H2, W2, 90, 3, 5)
    streak = np.asarray(Image.fromarray(noise2d(H2 // 24 + 2, W2, 5, 2, 11).astype(np.float32)).resize((W2, H2), Image.BILINEAR))
    alb = (base + 0.025 * streak)[..., None] * np.array([0.8, 0.9, 1.2], np.float32)
    seam = ((yy % 240) < 3) | ((xx % 360) < 3)
    alb = alb * np.where(seam, 0.45, 1.0)[..., None]
    c._put(sl, np.ones((H2, W2), np.float32), alb, 0.0, Z + 1.5, None)

    def E(p):                      # emblem coordinates -> header
        p = np.asarray(p, np.float32)
        return (p - np.array([cx, cy], np.float32)) * S + np.array(EMB, np.float32)

    def Sg(p):                     # sign coordinates (720 wide layout, centre (360, 1190)) -> header
        p = np.asarray(p, np.float32)
        return (p - np.array([360, 1190], np.float32)) * K + np.array(SIGN, np.float32)

    dark = np.array([0.012, 0.012, 0.018], np.float32)
    pm = Image.new('L', (W2, H2), 0)
    ex, ey = EMB
    ID.Draw(pm).ellipse((ex - (ER + 36) * S, ey - (ER + 36) * S, ex + (ER + 36) * S, ey + (ER + 36) * S), fill=255)
    c._put(sl, np.asarray(pm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255, dark, 0.0, Z, None)
    bm = Image.new('L', (W2, H2), 0)
    (x0, y0), (x1, y1) = Sg((60, 1080)), Sg((660, 1300))
    ID.Draw(bm).rounded_rectangle((x0, y0, x1, y1), 34 * K, fill=255)
    c._put(sl, np.asarray(bm.filter(ImageFilter.GaussianBlur(1.2)), np.float32) / 255, dark, 0.0, Z, None)

    def tube(pts, col, r=3.4, closed=False, level=1.0, sc=S, tf=E):
        c.neon_path(tf(pts), col, Z, r=r * sc, level=level, closed=closed, state='on')

    ang = np.radians(np.linspace(0, 360, 400))
    outer = np.stack([cx + (ER + 22) * np.cos(ang), cy + (ER + 22) * np.sin(ang)], 1)[:-1]
    tube(RING[:-1], PINK, r=5.2, closed=True)
    tube(outer, BLUE2, r=2.2, closed=True, level=0.7)
    for pts, r in ((HEAD, 4.2), (FRONT, 4.2), (FACEP[(np.hypot(FACEP[:, 0] - cx, FACEP[:, 1] - cy) < ER - 4)], 3.2), (BREAST, 3.2)):
        tube(pts, TEAL, r=r)
    tube(EYE, PINK, r=2.6)
    tube(CHEEK, TEAL, r=2.6)
    tube(np.vstack([TE, BASE, LE]), ORANGE, r=4.2, closed=True, level=0.9)
    for tp, bt in stripe:
        tube(np.array([tp, bt]), YEL, r=3.4)
    for word, y, col in (('FLUTTERING', 1149, PINK), ('PUFFINS', 1229, TEAL)):
        px, py = Sg((360, y))
        c.neon_text(word, px, py, 72 * K, col, Z, spacing=0.06, r=max(0.9, 72 * 0.018) * K)
    c.neon_path(Sg(np.array([(70, 1090), (650, 1090), (650, 1290), (70, 1290)])), BLUE2, Z, r=2.4 * K, level=0.6, closed=True, state='on')
    c.mist(y=H2 * 0.8, h=200, density=0.1)
    img = c.composite().convert('RGB').resize((4096, 2304), Image.LANCZOS)
    out = os.path.abspath(os.path.join(RES, '..', '..', '..', 'store', 'puffin-header-4096x2304.jpg'))
    for q in (92, 88, 84, 80, 75, 70):
        img.save(out, quality=q, optimize=True, subsampling=0 if q > 85 else 2)
        if os.path.getsize(out) < 1_000_000:
            break
    print(out, img.size, q, os.path.getsize(out))
    img.resize((1024, 576), Image.LANCZOS).save('/tmp/claude-501/prev/header.png')


main()
