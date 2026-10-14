"""Small plant and cloth sprites for the decoration pass: dead grass, ghost blooms, ashen lilies, the tattered banner."""
import math
from core import Ramp, Noise, CLEAR
import numpy as np

DRY = Ramp('#17130f', '#251f18', '#342b21', '#463a2c', '#584a38', '#6d5c46', '#857157')
PALE = Ramp('#2a2433', '#4a4058', '#6f6382', '#988aab', '#c4b8d4', '#ece4f6')
LILY = Ramp('#0f0d14', '#1a1622', '#262031', '#332b40', '#423750', '#524562')
BANNER = Ramp('#0d0812', '#160d1f', '#21132e', '#2d1a3f', '#3a2150', '#4a2a63')


def _canvas(w=16, h=16):
    return np.zeros((h, w, 4), dtype=np.uint8)


def _put(img, x, y, col):
    if 0 <= x < img.shape[1] and 0 <= y < img.shape[0]:
        img[y, x] = col


def dead_grass(tall=False):
    """Dry, broken blades, bent over: grass that died standing up."""
    img = _canvas()
    blades = [(2, 8), (4, 11), (6, 13), (7, 9), (9, 12), (11, 10), (13, 7), (5, 6), (10, 14)] if tall else \
             [(3, 6), (5, 8), (7, 9), (8, 6), (10, 8), (12, 5), (6, 4)]
    for i, (bx, h) in enumerate(blades):
        lean = (-1) ** i * (0.12 + 0.08 * (i % 3))
        snap = h - 2 - (i % 3)   # where the blade broke and flopped over
        for k in range(h):
            x = bx + lean * k
            y = 15 - k
            if k > snap:   # the broken tip hangs sideways
                x += (k - snap) * (1 if i % 2 else -1)
                y = 15 - snap + (k - snap) // 2
            _put(img, int(round(x)), y, DRY.at(1.0 + k / h * 4.6 + (i % 2) * 0.4, int(x), y, 0.2))
    return img


def ghost_bloom():
    """A pale flower that grows where someone was buried: three drooping petals, faintly lit."""
    img = _canvas()
    stem = [(8, 15), (8, 14), (8, 13), (7, 12), (7, 11), (7, 10), (8, 9), (8, 8)]
    for x, y in stem:
        _put(img, x, y, DRY.at(2.2, x, y, 0))
    for (x, y) in [(10, 13), (11, 12), (5, 14), (4, 13)]:   # two leaves
        _put(img, x, y, DRY.at(2.8, x, y, 0))
    petals = [(6, 6), (7, 5), (8, 5), (9, 5), (10, 6), (6, 7), (10, 7), (5, 8), (11, 8), (7, 6), (8, 6), (9, 6), (8, 7)]
    for x, y in petals:
        _put(img, x, y, PALE.at(3.6 + (0.8 if y < 6 else 0), x, y, 0))
    _put(img, 8, 6, (220, 190, 255, 255))
    return img


MUD = Ramp('#120e0b', '#1d1712', '#2a2119', '#382c22', '#46382b', '#564536', '#685442')


def mired_hand():
    """A skeletal hand clawing up out of the dried riverbed: where one of the Mired lies (fingers hooked, mud to the wrist)."""
    from core import BONE
    img = _canvas()
    fingers = [[(4, 9), (3, 7), (2, 5), (2, 4), (3, 3)],      # knuckle first, tip last; the tips hook over like a claw
               [(6, 9), (5, 7), (5, 4), (5, 3), (6, 2)],
               [(8, 9), (8, 6), (8, 3), (8, 2), (9, 1)],
               [(10, 9), (11, 7), (12, 5), (12, 4), (11, 3)],
               [(11, 11), (13, 10), (14, 9), (14, 8), (13, 7)]]   # the thumb
    for f in fingers:
        pts = []
        for (ax, ay), (bx, by) in zip(f, f[1:]):
            n = max(abs(bx - ax), abs(by - ay))
            pts += [(round(ax + (bx - ax) * k / n), round(ay + (by - ay) * k / n)) for k in range(n)]
        pts.append(f[-1])
        for i, (x, y) in enumerate(pts):
            joint = i in (len(pts) // 3, 2 * len(pts) // 3)
            _put(img, x, y, BONE.cols[2] if joint else (BONE.cols[3] if i == len(pts) - 1 else BONE.cols[4]))
    for x, c in zip(range(4, 11), (3, None, 4, None, 4, None, 3)):   # the back of the hand: bones with mud between
        _put(img, x, 10, BONE.cols[c] if c else MUD.cols[2])
    mud = ["....mMMbMMmMm...",
           ".....mMMLMMm....",
           "......mLMMm.....",
           "....mmMLMMMmm...",
           "..mmMMLMMMMMMMm."]
    for y, row in enumerate(mud, 11):
        for x, ch in enumerate(row):
            if ch != '.':
                _put(img, x, y, {'M': MUD.cols[3], 'm': MUD.cols[1], 'L': MUD.cols[5], 'b': BONE.cols[3]}[ch])
    for x, y in ((5, 7), (8, 8), (11, 7), (3, 8)):   # dried mud still clinging to the fingers
        _put(img, x, y, MUD.cols[4])
    return img


def ashen_lily():
    """A lily pad gone grey, with one pale bloom; seen from above."""
    img = _canvas()
    n = Noise(31)
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            d = math.hypot(dx, dy)
            ang = math.atan2(dy, dx)
            notch = abs(ang - 0.6) < 0.35 and d > 2
            if d < 7.2 and not notch:
                vein = 0.6 if abs(math.sin(ang * 4)) < 0.15 else 0
                img[y, x] = LILY.at(2.6 + n.white(x, y) * 0.6 + vein - d * 0.12, x, y, 0.2)
    for x, y in [(6, 6), (7, 5), (8, 6), (7, 7), (6, 8), (8, 8)]:
        img[y, x] = PALE.at(4.4, x, y, 0)
    img[6, 7] = (225, 200, 255, 255)
    return img


def tattered_banner():
    """32x64: dark violet cloth, torn ragged at the foot, a hooded skull stitched in old thread."""
    w, h = 32, 64
    img = np.zeros((h, w, 4), dtype=np.uint8)
    n = Noise(41)
    tear = [h - 1 - int(6 + 9 * n.white(x // 2, 0) + 4 * math.sin(x * 0.7)) for x in range(w)]
    for y in range(h):
        for x in range(w):
            if y > tear[x] or (n.white(x // 2, y // 3, 5) > 0.93 and 8 < y < h - 12):
                continue
            fold = math.sin(x * 0.55) * 0.5
            img[y, x] = BANNER.at(2.8 + fold + n.white(x, y) * 0.5 - y * 0.012, x, y, 0.25)
    for x in range(w):   # a hem along the top where it hangs from the pole
        for y in (0, 1):
            img[y, x] = BANNER.at(1.4, x, y, 0)
    sig = ["....kkkkkk....", "...kddddddk...", "..kddddddddk..", "..kdeeddeedk..", "..kdeeddeedk..", "..kddddddddk..",
           "...kddkkddk...", "...kdkddkdk...", "....kddddk....", ".....kkkk....."]
    cols = {'k': (24, 14, 32, 255), 'd': (122, 104, 132, 255), 'e': (160, 90, 230, 255)}
    for j, row in enumerate(sig):
        for i, c in enumerate(row):
            if c in cols:
                for sx in (0, 1):
                    for sy in (0, 1):
                        x, y = 2 + i * 2 + sx, 16 + j * 2 + sy
                        if 0 <= x < w and img[y, x, 3]:
                            img[y, x] = cols[c]
    return img
