"""Play Games achievement icons (512x512) and the list to paste into the Play Console.

Run from the repo root:  python3 tools/art/achievements.py   -> store/achievements/*.png and achievements.csv
"""
import csv, os, sys
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, os.path.dirname(__file__))
import neon as N
import neon_ui as UI
import store as S

OUT = os.path.join(UI.ROOT, 'store', 'achievements')
os.makedirs(OUT, exist_ok=True)
CYAN, RED, GOLD, PURPLE, WHITE = (40, 200, 255), (255, 40, 60), (255, 190, 40), (180, 90, 255), (235, 235, 240)
PLATINUM = (170, 205, 255)

# (key, group word, threshold, color, name, description, points)
ACH = [
    ('score_25', 'ORBS', 25, CYAN, 'First Glow', 'Collect 25 orbs of each color in a single game.', 10),
    ('score_50', 'ORBS', 50, CYAN, 'Rising Star', 'Collect 50 orbs of each color in a single game.', 15),
    ('score_75', 'ORBS', 75, CYAN, 'Orb Hunter', 'Collect 75 orbs of each color in a single game.', 20),
    ('score_100', 'ORBS', 100, CYAN, 'Century', 'Collect 100 orbs of each color in a single game.', 30),
    ('score_200', 'ORBS', 200, CYAN, 'Orb Master', 'Collect 200 orbs of each color in a single game.', 45),
    ('score_300', 'ORBS', 300, CYAN, 'Orb Legend', 'Collect 300 orbs of each color in a single game.', 60),
    ('ships_1', 'SHIPS', 1, WHITE, 'Hangar Door', 'Unlock your first extra ship.', 5),
    ('ships_5', 'SHIPS', 5, WHITE, 'Small Squadron', 'Unlock 5 ships.', 10),
    ('ships_10', 'SHIPS', 10, WHITE, 'Fleet Captain', 'Unlock 10 ships.', 15),
    ('ships_25', 'SHIPS', 25, WHITE, 'Admiral', 'Unlock 25 ships.', 25),
    ('ships_50', 'SHIPS', 50, WHITE, 'Armada', 'Unlock 50 ships.', 40),
    ('ships_74', 'SHIPS', 74, WHITE, 'Complete Fleet', 'Unlock 74 ships.', 60),
    ('galaxies_1', 'GALAXIES', 1, PURPLE, 'Stargazer', 'Unlock your first extra galaxy.', 15),
    ('galaxies_5', 'GALAXIES', 5, PURPLE, 'Voyager', 'Unlock 5 galaxies.', 30),
    ('galaxies_9', 'GALAXIES', 9, PURPLE, 'Galaxy Hopper', 'Unlock 9 galaxies.', 50),
    ('games_1', 'GAMES', 1, RED, 'Lift Off', 'Play your first game.', 5),
    ('games_10', 'GAMES', 10, RED, 'Getting Hooked', 'Play 10 games.', 10),
    ('games_25', 'GAMES', 25, RED, 'Regular', 'Play 25 games.', 15),
    ('games_50', 'GAMES', 50, RED, 'Dedicated Pilot', 'Play 50 games.', 25),
    ('games_100', 'GAMES', 100, RED, 'Veteran', 'Play 100 games.', 40),
    ('games_500', 'GAMES', 500, RED, 'Orbital Lifer', 'Play 500 games.', 60),
    ('upgrades_1', 'UPGRADES', 1, GOLD, 'Tinkerer', 'Buy your first upgrade.', 10),
    ('upgrades_5', 'UPGRADES', 5, GOLD, 'Mechanic', 'Buy 5 upgrades.', 20),
    ('upgrades_10', 'UPGRADES', 10, GOLD, 'Engineer', 'Buy 10 upgrades.', 40),
    ('upgrades_18', 'UPGRADES', 18, GOLD, 'Fully Upgraded', 'Buy 18 upgrades.', 60),
    ('platinum', 'PLATINUM', 0, PLATINUM, 'Platinum', 'Unlock all the other achievements.', 100),
]


SINGULAR = {'SHIPS': 'SHIP', 'GALAXIES': 'GALAXY', 'GAMES': 'GAME', 'UPGRADES': 'UPGRADE', 'ORBS': 'ORB'}


def platinum_icon(color):
    size = 512
    im = S.background(size, size, seed=77, stars=0.6)
    for inset, width in ((40, 12), (74, 8)):
        ring = Image.new('L', (size, size), 0)
        ImageDraw.Draw(ring).ellipse((inset, inset, size - inset, size - inset), outline=255, width=width)
        im = S.add(im, UI.neon_text(ring, color, 8.0), (size / 2, size / 2))
    # trophy: cup with two handles, stem and base
    m = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(m)
    w = 11
    cx = size / 2
    d.line([(cx - 64, 128), (cx + 64, 128)], fill=255, width=w, joint='curve')
    d.line([(cx - 64, 128), (cx - 64, 178)], fill=255, width=w)
    d.line([(cx + 64, 128), (cx + 64, 178)], fill=255, width=w)
    d.arc((cx - 64, 118, cx + 64, 238), 0, 180, fill=255, width=w)
    d.arc((cx - 100, 136, cx - 28, 190), 90, 270, fill=255, width=w)
    d.arc((cx + 28, 136, cx + 100, 190), 270, 90, fill=255, width=w)
    d.line([(cx, 238), (cx, 280)], fill=255, width=w)
    d.rounded_rectangle((cx - 48, 280, cx + 48, 306), radius=8, outline=255, width=w)
    im = S.add(im, UI.neon_text(m, color, 5.0), (size / 2, size / 2))
    f = UI.BMFont('fonts/prototype128blueWhite')
    t = UI.neon_text(S.pad(f.mask('PLATINUM', 26), 40), color, 3.0)
    im = S.add(im, t, (size / 2, 352))
    return im.convert('RGB')


def icon(group, number, color):
    if group == 'PLATINUM':
        return platinum_icon(color)
    if number == 1:
        group = SINGULAR[group]
    N.set_scale(1)
    size = 512
    im = S.background(size, size, seed=sum(map(ord, group[:3])) + number, stars=0.6)
    ring = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(ring)
    d.ellipse((46, 46, size - 46, size - 46), outline=255, width=14)
    layer = UI.neon_text(ring, color, 9.0)
    im = S.add(im, layer, (size / 2, size / 2))
    f = UI.BMFont('fonts/prototype128blueWhite')
    num = str(number)
    h = 100 if len(num) < 3 else 78
    t = UI.neon_text(S.pad(f.mask(num, h), 40), color, 3.0)
    im = S.add(im, t, (size / 2, size / 2 - 18))
    lab = UI.neon_text(S.pad(f.mask(group, 22), 30), color, 2.5)
    im = S.add(im, lab, (size / 2, size / 2 + 78))
    return im.convert('RGB')


def build_zip(mapping_name):
    """ZIP for Play Console > Play Games Services > Achievements > Import (CSV files have no header row, no commas in text)."""
    import io, zipfile
    path = os.path.join(OUT, 'import-%s.zip' % ('console-name' if mapping_name == 'AchievementsIconsMappings.csv' else 'docs-name'))
    meta, maps = io.StringIO(), io.StringIO()
    for i, (key, group, n, color, name, desc, pts) in enumerate(ACH, 1):
        assert ',' not in name + desc
        meta.write('%s,%s,False,,Revealed,%d,%d\n' % (name, desc, pts, i))
        maps.write('%s,%s.png\n' % (name, key))
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as z:
        z.writestr('AchievementsMetadata.csv', meta.getvalue())
        z.writestr(mapping_name, maps.getvalue())
        for a in ACH:
            z.write(os.path.join(OUT, a[0] + '.png'), a[0] + '.png')
    return path


if __name__ == '__main__':
    rows = []
    for key, group, n, color, name, desc, pts in ACH:
        icon(group, n, color).save(os.path.join(OUT, key + '.png'))
        rows.append([key, name, desc, pts, key + '.png'])
    with open(os.path.join(OUT, 'achievements.csv'), 'w', newline='') as fh:
        w = csv.writer(fh)
        w.writerow(['key', 'name', 'description', 'points', 'icon'])
        w.writerows(rows)
    build_zip('AchievementsIconsMappings.csv')
    build_zip('AchievementsIconMappings.csv')
    print(len(rows), 'achievements,', sum(r[3] for r in rows), 'points')
