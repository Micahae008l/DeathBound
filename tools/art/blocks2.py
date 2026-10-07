"""Atmosphere pass: shaded building blocks, glowing overlays, and the textures for remains / cages / braziers / shards."""
import math
import numpy as np
from core import Ramp, hx, Noise
from sprites import canvas, stamp, CLEAR

# Built stone leans purple; the island rock is a colder grey so buildings stand out from the land.
BRICK = Ramp('#120d19', '#1c1526', '#271d34', '#332642', '#403051', '#4e3b62', '#5f4876', '#72588b', '#86699f')
ROCK = Ramp('#17161b', '#201f26', '#2a2831', '#35323d', '#413d4a', '#4e4957', '#5c5766', '#6c6676')
DARK = Ramp('#0b0a0e', '#121117', '#1a1820', '#23202b', '#2d2936', '#373241')
GLOW = Ramp('#3c1170', '#6020b0', '#8a3cf0', '#b070ff', '#d6aaff', '#f2e6ff')
BONE = Ramp('#3b342e', '#5c5248', '#827564', '#a89a83', '#c9bca2', '#e2d8c2', '#f2ecdc')
IRON = Ramp('#0e0d10', '#19171c', '#25222a', '#322e37', '#423d48', '#56505d', '#6e6876')
CLOTH = Ramp('#0c0a10', '#15121b', '#1f1a27', '#2a2334', '#362d43', '#433853')


def brick_wall(ramp, seed, bw=8, bh=4, mortar=0.35, purple_bias=0.0, chip=0.25):
    """Bricks with a real bevel: lit top-left edge, shadowed bottom-right, per-brick tone, pits and chipped corners."""
    img = canvas()
    n = Noise(seed)
    for y in range(16):
        row = y // bh
        off = 0 if row % 2 == 0 else bw // 2
        for x in range(16):
            bx = (x + off) % bw
            by = y % bh
            brick_id = ((x + off) // bw, row)
            if bx == bw - 1 or by == bh - 1:
                img[y, x] = ramp.at(mortar, x, y, 0)
                continue
            tone = n._h(brick_id[0] + 7, brick_id[1], 1) * 1.1 - 0.4 + purple_bias
            v = 3.6 + tone + n.cluster(x, y, 2, 3) * 0.22
            if by == 0:
                v += 1.1
            elif by == bh - 2:
                v -= 0.9
            if bx == 0:
                v += 0.55
            elif bx == bw - 2:
                v -= 0.6
            if n.white(x, y, 5) > 0.9:
                v -= 1.2   # pit
            img[y, x] = ramp.at(v, x, y, 0.15)
        # chipped corners
    for row in range(16 // bh):
        off = 0 if row % 2 == 0 else bw // 2
        for b in range(-1, 16 // bw + 1):
            if n._h(b, row, 9) < chip:
                cx = (b * bw - off) % 16
                cy = row * bh
                if 0 <= cy < 16:
                    img[cy, cx % 16] = ramp.at(mortar + 0.4, 0, 0, 0)
    return img


def soulstone():
    """Island rock: cold grey strata with faint purple seams."""
    img = canvas()
    n = Noise(101)
    for y in range(16):
        band = n.smooth(0, y, 3.0, 4) * 0.8
        for x in range(16):
            v = 3.1 + band + n.smooth(x, y, 4.0, 1) * 0.5 + n.cluster(x, y, 3, 2) * 0.35 + n.white(x, y, 3) * 0.08
            if n._h(x // 3, y // 2, 6) > 0.9:
                v -= 1.0
            img[y, x] = ROCK.at(v, x, y, 0.12)
    for (x, y) in [(3, 2), (4, 3), (4, 4), (5, 5), (11, 9), (12, 10), (12, 11), (13, 12)]:
        img[y, x] = hx('#4b2e6e')
    return img


def soulstone_bricks():
    return brick_wall(BRICK, 201)


def cracked_bricks():
    img = brick_wall(BRICK, 202, chip=0.45)
    path = [(3, 0), (3, 1), (4, 2), (4, 4), (5, 5), (5, 6), (6, 8), (6, 9), (7, 10), (9, 11), (10, 13), (10, 14), (11, 15)]
    for x, y in path:
        img[y, x] = BRICK.at(0.1, x, y, 0)
        if x + 1 < 16:
            img[y, x + 1] = BRICK.at(5.4, x, y, 0)
    for x, y in [(12, 2), (13, 3), (13, 4), (14, 5), (1, 9), (2, 10)]:
        img[y, x] = BRICK.at(0.1, x, y, 0)
    return img


def dark_bricks():
    return brick_wall(DARK, 203, mortar=0.1)


def veined_bricks():
    """Bricks split by glowing soul veins. Returns (base, emissive overlay)."""
    base = brick_wall(BRICK, 204, purple_bias=0.3)
    glow = canvas()
    path = [(2, 0), (2, 1), (3, 2), (3, 3), (4, 3), (5, 4), (5, 5), (6, 6), (7, 7), (7, 8), (8, 9), (9, 9), (10, 10), (11, 11), (11, 12),
            (12, 13), (13, 14), (13, 15), (8, 3), (9, 2), (10, 1), (10, 0), (4, 10), (3, 11), (3, 12), (2, 13)]
    for i, (x, y) in enumerate(path):
        base[y, x] = hx('#5d24a8')
        c = GLOW.cols[4 if i % 3 == 0 else 3]
        glow[y, x] = c
    return base, glow


def tiles():
    img = canvas()
    n = Noise(301)
    for y in range(16):
        for x in range(16):
            tx, ty = x % 8, y % 8
            tid = (x // 8, y // 8)
            v = 3.4 + (n._h(tid[0], tid[1], 2) - 0.5) * 0.9 + n.cluster(x, y, 2, 1) * 0.2
            if tx == 7 or ty == 7:
                v = 0.4
            elif tx == 0 or ty == 0:
                v += 1.1
            elif tx == 6 or ty == 6:
                v -= 0.9
            if 2 <= tx <= 4 and 2 <= ty <= 4 and (tx + ty) % 2 == 0:
                v -= 0.35  # worn centre
            img[y, x] = BRICK.at(v, x, y, 0.15)
    return img


def polished():
    img = canvas()
    n = Noise(401)
    for y in range(16):
        for x in range(16):
            v = 3.8 + n.smooth(x, y, 6.0, 1) * 0.35 - (x + y) * 0.03
            if x == 0 or y == 0:
                v = 5.6
            elif x == 15 or y == 15:
                v = 1.2
            elif x == 1 or y == 1:
                v += 0.5
            elif x == 14 or y == 14:
                v -= 0.6
            img[y, x] = BRICK.at(v, x, y, 0.12)
    return img


def chiseled():
    """Carved skull face. Returns (base, emissive eyes)."""
    img = polished()
    skull = ["..KKKKKKKKKK..",
             ".KxXXXXXXXXxK.",
             "KxXXXXXXXXXXxK",
             "KxEEXXXXXXEExK",
             "KxEeEXXXXEeExK",
             "KxEEXXxxXXEExK",
             "KxXXXxKKxXXXxK",
             ".KxXXXxxXXXxK.",
             ".KxtKtKtKtKxK.",
             "..KKKKKKKKKK.."]
    leg = {'K': BRICK.cols[0], 'x': BRICK.cols[2], 'X': BRICK.cols[5], 'E': hx('#2a0f45'), 'e': hx('#7a3cc2'), 't': BRICK.cols[6]}
    stamp(img, skull, leg, 1, 3)
    glow = canvas()
    stamp(glow, ["..............", "..............", "..............",
                 "..EE......EE..",
                 "..EeE....EeE..",
                 "..EE......EE.."], {'E': GLOW.cols[2], 'e': GLOW.cols[5]}, 1, 3)
    return img, glow


def pillar_side():
    img = canvas()
    n = Noise(501)
    for y in range(16):
        for x in range(16):
            v = 3.5 + n.smooth(x, y * 0.4, 3.0, 1) * 0.35
            if x in (0, 15):
                v = 0.6
            elif x in (1, 14):
                v += 0.9
            elif x in (5, 10):
                v -= 1.1
            elif x in (6, 11):
                v += 0.7
            if y in (0, 1):
                v = 5.4 if y == 0 else 4.4
            if y in (14, 15):
                v = 1.6 if y == 14 else 0.8
            img[y, x] = BRICK.at(v, x, y, 0.12)
    return img


def pillar_top():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            v = 3.4 + (0.9 if int(d) % 3 == 0 else 0) - (1.6 if d > 7 else 0) + (0.6 if d > 6 and d <= 7 else 0)
            img[y, x] = BRICK.at(v, x, y, 0.12)
    return img


# ------------------------------------------------------------------ decor
def bone():
    img = canvas()
    n = Noise(601)
    for y in range(16):
        for x in range(16):
            v = 4.0 + n.smooth(x, y, 3.0, 2) * 0.5 + n.white(x, y, 3) * 0.15 - (0.8 if y % 8 == 7 else 0) + (0.6 if y % 8 == 0 else 0)
            if n.white(x, y, 7) > 0.88:
                v -= 1.4
            img[y, x] = BONE.at(v, x, y, 0.15)
    return img


def skull_face():
    img = canvas()
    for y in range(16):
        for x in range(16):
            img[y, x] = BONE.at(4.3 - y * 0.05 + (0.4 if y < 3 else 0), x, y, 0.15)
    face = ["................",
            "................",
            "................",
            "................",
            "..KKKK....KKKK..",
            ".KKkkKK..KKkkKK.",
            ".KKkkKK..KKkkKK.",
            "..KKKK....KKKK..",
            "......d..d......",
            ".......KK.......",
            "......dKKd......",
            "................",
            "..tKtKtKtKtKtK..",
            "..KtKtKtKtKtKt..",
            "................",
            "................"]
    stamp(img, face, {'K': hx('#140e10'), 'k': hx('#2c1a3a'), 'd': BONE.cols[2], 't': BONE.cols[6]})
    return img


def rag():
    img = canvas()
    n = Noise(701)
    for y in range(16):
        for x in range(16):
            fold = math.sin((x + n.smooth(x, y, 4, 1) * 2) * 1.9)
            v = 2.6 + fold * 0.7 + n.cluster(x, y, 2, 2) * 0.3
            if n._h(x // 2, y // 2, 5) > 0.82 or (y > 11 and n._h(x, 0, 6) * 5 < y - 11):
                continue  # holes and a ragged hem
            img[y, x] = CLOTH.at(v, x, y, 0.15)
    return img


def iron():
    img = canvas()
    n = Noise(801)
    for y in range(16):
        for x in range(16):
            v = 2.8 + n.smooth(x, y, 3.0, 1) * 0.6 + n.white(x, y, 2) * 0.2
            if (x + y) % 7 == 0:
                v += 0.8
            if n.white(x, y, 9) > 0.86:
                img[y, x] = Ramp('#2a1810', '#4a2a18', '#6a3c22').at(1.2 + n.white(x, y, 10), x, y, 0)  # rust
                continue
            img[y, x] = IRON.at(v, x, y, 0.15)
    return img


def soul_fire(frames=8):
    out = []
    for f in range(frames):
        img = canvas()
        for x in range(16):
            for y in range(16):
                t = 1 - y / 15
                h = 0.55 + 0.45 * math.sin(x * 0.9 + f * 0.8) * 0.5 + 0.25 * math.sin(x * 2.3 - f * 1.3)
                if t > h + 0.25:
                    continue
                heat = (1 - t / (h + 0.25)) * (1 - abs(x - 7.5) / 9)
                if heat <= 0.05:
                    continue
                c = GLOW.at(heat * 6.0, x, y, 0.2)
                img[y, x] = (c[0], c[1], c[2], int(min(255, 90 + heat * 300)))
        out.append(img)
    return np.concatenate(out, axis=0)


def embers():
    img = canvas()
    n = Noise(901)
    for y in range(16):
        for x in range(16):
            v = n.cluster(x, y, 2, 1)
            img[y, x] = GLOW.at(1.2 + v * 1.6, x, y, 0.2) if v > -0.2 else DARK.at(2.0, x, y, 0)
    return img


def shard(size):  # noqa
    """Soul shard buds (cross model): small / medium / large."""
    img = canvas()
    C = Ramp('#2d0f50', '#4a1b80', '#6d2fb8', '#9455e6', '#b98af7', '#dcc4ff', '#f6edff')
    spikes = {0: [(7.5, 8, 1.7, 7), (5, 12, 1.0, 3)], 1: [(7.5, 7, 1.6, 8), (4.5, 11, 1.0, 4), (10.5, 10, 1.1, 5)],
              2: [(7.5, 3, 2.0, 12), (4, 8, 1.4, 7), (11, 7, 1.4, 8)]}[size]
    for cx, top, half, h in spikes:
        for y in range(int(top), 16):
            t = (y - top) / h
            if t > 1:
                break
            w = half * min(1, t * 3.0 + 0.25)
            for x in range(16):
                dx = x - cx
                if abs(dx) <= w:
                    img[y, x] = C.at(3.6 - dx / max(w, 0.6) * 1.5 + (1.5 if t < 0.25 else 0), x, y, 0.2)
    return img
