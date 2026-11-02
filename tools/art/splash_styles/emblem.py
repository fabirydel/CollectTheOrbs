"""Shared emblem geometry: the upper puffin inside a round emblem, its beak free of the circle."""
from common import *

EC = (360, 590)         # emblem centre
ER = 255                # emblem radius
PX, PY, PS = 86, 330, 520
OUT = os.path.join(ART, 'splash_options3')


def disc(r=ER, soft=1.0):
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float32)
    return np.clip(r + 0.5 - np.hypot(xx - EC[0], yy - EC[1]), 0, 1)


def parts():
    """puffin masks: everything clipped to the emblem except the beak group (tip/white/orange/yellow/rosette/beak)"""
    P = puffin(PX, PY, PS)
    D = disc(ER - 2)
    free = ('beak', 'tip', 'white', 'orange', 'yellow', 'rosette')
    for k in P:
        if k not in free:
            P[k] = P[k] * D
    P['all'] = np.maximum.reduce([P['body'], P['face'], P['breast'], P['beak']])
    return P


def uv(u, v):
    return PX + u / 300 * PS, PY + v / 300 * PS


def save3(im, name):
    global OUT
    import common
    common.OUT = OUT
    common.save(im, name)
