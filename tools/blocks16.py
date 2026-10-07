#!/usr/bin/env python3
"""
Redraws DeathBound's 32px block textures at 16px (vanilla resolution), in the same style.

Colours are taken from each original (tools/original_32px/), layouts follow how vanilla draws the same kind
of block at 16px (stone bricks, tiles, polished stone, logs, planks, shelves...). Deterministic: re-run any time.

    python3 tools/blocks16.py            # writes into src/main/resources/assets/deathbound/textures/block
    python3 tools/blocks16.py --preview  # also writes /tmp/db_tex/blocks16_compare.png
"""
import math
import os
import random
import sys

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "tools/original_32px")
OUT = os.path.join(ROOT, "src/main/resources/assets/deathbound/textures/block")
S = 16
T = (0, 0, 0, 0)


# ------------------------------------------------------------------ palette helpers
def orig(name):
    return Image.open(os.path.join(SRC, name + ".png")).convert("RGBA")


def lum(c):
    return 0.299 * c[0] + 0.587 * c[1] + 0.114 * c[2]


def sat(c):
    return max(c[:3]) - min(c[:3])


def ramp_from(img, n=6, region=None, max_sat=None, min_sat=None):
    """n colours from dark to light, sampled from the original's opaque pixels (quantiles of luminance)."""
    if region:
        img = img.crop(region)
    px = [p for p in img.getdata() if p[3] > 128]
    if max_sat is not None:
        px = [p for p in px if sat(p) <= max_sat] or px
    if min_sat is not None:
        px = [p for p in px if sat(p) >= min_sat] or px
    px.sort(key=lum)
    out = []
    for i in range(n):
        lo = int(len(px) * (i / n))
        hi = max(lo + 1, int(len(px) * ((i + 1) / n)))
        chunk = px[lo:hi]
        out.append(tuple(int(sum(c[k] for c in chunk) / len(chunk)) for k in range(3)))
    return out


def accent_from(img, min_sat=60):
    px = [p for p in img.getdata() if p[3] > 128 and sat(p) >= min_sat]
    if not px:
        return [(150, 80, 220), (190, 120, 250)]
    px.sort(key=lum)
    return [px[len(px) // 4][:3], px[(3 * len(px)) // 4][:3]]


def new(w=S, h=S):
    return Image.new("RGBA", (w, h), T)


def put(im, x, y, c, a=255):
    if 0 <= x < im.width and 0 <= y < im.height:
        im.putpixel((x, y), (c[0], c[1], c[2], a))


def noise(seed, w=S, h=S, cell=2):
    rng = random.Random(seed)
    gw, gh = w // cell + 2, h // cell + 2
    g = [[rng.random() - 0.5 for _ in range(gw)] for _ in range(gh)]

    def f(x, y):
        fx, fy = x / cell, y / cell
        ix, iy = int(fx), int(fy)
        tx, ty = fx - ix, fy - iy
        a = g[iy][ix] * (1 - tx) + g[iy][ix + 1] * tx
        b = g[iy + 1][ix] * (1 - tx) + g[iy + 1][ix + 1] * tx
        return a * (1 - ty) + b * ty
    return f


def pick(rmp, v):
    v = max(0.0, min(0.999, v))
    return rmp[int(v * len(rmp))]


# ------------------------------------------------------------------ families
def bricks(rmp, seed, cracked=False, vein=None):
    """Vanilla stone-brick layout: 4 courses of 3px bricks + 1px mortar, alternate courses offset by 4."""
    im = new()
    n = noise(seed, cell=2)
    rng = random.Random(seed)
    mortar = rmp[0]
    for course in range(4):
        y0 = course * 4
        off = 0 if course % 2 == 0 else 4
        joints = [(off + k * 8) % 16 for k in range(2)]
        shade = [rng.uniform(-0.08, 0.08) for _ in range(3)]
        for y in range(y0, y0 + 4):
            for x in range(S):
                if y == y0 + 3 or (x in joints):
                    put(im, x, y, mortar)
                    continue
                bi = sum(1 for j in joints if x > j) % 2
                ly = y - y0
                xl = (x - off) % 8
                v = 0.55 + shade[bi] + n(x, y) * 0.35
                if ly == 0:
                    v += 0.22            # lit top edge
                elif ly == 2:
                    v -= 0.14            # shaded bottom row
                if xl == 1:
                    v += 0.08
                if xl == 0 and x not in joints:
                    v += 0.1
                put(im, x, y, pick(rmp[1:], v))
    if cracked:
        for _ in range(3):
            x, y = rng.randrange(2, 14), rng.randrange(0, 4)
            for _ in range(rng.randrange(4, 8)):
                put(im, x, y, mortar)
                x += rng.choice((-1, 0, 1))
                y += 1
                if y >= S:
                    break
        for _ in range(4):                          # chipped brick corners
            cx, cy = rng.randrange(0, 16), rng.randrange(0, 4) * 4
            put(im, cx, cy, rmp[1])
    if vein:
        x = 12
        pts = []
        for y in range(S):
            pts.append((x, y))
            if y % 3 == 2:
                x += rng.choice((-1, 1)) if 10 < x < 14 else (1 if x <= 10 else -1)
        for (x, y) in pts:
            put(im, x, y, vein[0])
        for (x, y) in pts[::4]:
            put(im, x + 1, y, vein[1])
        return im, pts
    return im


def flagstone(rmp, seed, accent=None, specks=3):
    """Irregular fitted stones (soulstone): Voronoi cells, dark seams, each stone lit from the top-left."""
    rng = random.Random(seed)
    pts = []
    for gy in range(3):
        for gx in range(3):
            pts.append((gx * 5.3 + rng.uniform(0.5, 4.5), gy * 5.3 + rng.uniform(0.5, 4.5)))
    n = noise(seed + 1, cell=2)
    im = new()
    tone = [rng.uniform(-0.1, 0.1) for _ in pts]
    for y in range(S):
        for x in range(S):
            best = []
            for i, (px, py) in enumerate(pts):
                for ox in (-16, 0, 16):          # tile seamlessly
                    for oy in (-16, 0, 16):
                        d = math.hypot(x + 0.5 - px - ox, y + 0.5 - py - oy)
                        best.append((d, i, px + ox, py + oy))
            best.sort()
            d1, i1, cx, cy = best[0]
            d2 = next(b[0] for b in best if b[1] != i1)
            if d2 - d1 < 0.75:
                put(im, x, y, rmp[0])
                continue
            rel = ((x + 0.5 - cx) + (y + 0.5 - cy)) / 6.0
            v = 0.6 + tone[i1] - rel * 0.35 + n(x, y) * 0.3
            if d2 - d1 < 1.9:
                v -= 0.1                          # stones curve down into the seam
            put(im, x, y, pick(rmp[1:], v))
    if accent:
        for _ in range(specks):
            put(im, rng.randrange(16), rng.randrange(16), accent[rng.randrange(2)])
    return im


def tiles(rmp, seed):
    """2x2 large tiles with a bevel and an engraved ring in each (soulstone tiles)."""
    im = new()
    n = noise(seed, cell=2)
    rng = random.Random(seed)
    for ty in range(2):
        for tx in range(2):
            t = rng.uniform(-0.06, 0.06)
            for y in range(8):
                for x in range(8):
                    X, Y = tx * 8 + x, ty * 8 + y
                    if x == 7 or y == 7:
                        put(im, X, Y, rmp[0])
                        continue
                    v = 0.52 + t + n(X, Y) * 0.25
                    if x == 0 or y == 0:
                        v += 0.22
                    if x == 6 or y == 6:
                        v -= 0.16
                    d = math.hypot(x - 3.0, y - 3.0)
                    if 1.6 <= d < 2.5:            # engraved ring: dark, lit lip below-right
                        v = 0.05 if (x + y) < 6 else 0.18
                    put(im, X, Y, pick(rmp, v))
    return im


def polished(rmp, seed):
    im = new()
    n = noise(seed, cell=4)
    for y in range(S):
        for x in range(S):
            e = min(x, y, 15 - x, 15 - y)
            if e == 0:
                c = rmp[0]
            elif e == 1:
                c = rmp[4] if (x == 1 or y == 1) and x < 15 - 1 and y < 15 - 1 else rmp[1]
            else:
                c = pick(rmp[2:5], 0.5 + n(x, y) * 0.4 + (1 - (x + y) / 30) * 0.2 - 0.1)
            put(im, x, y, c)
    return im


def chiseled(rmp, seed):
    im = polished(rmp, seed)
    for y in range(S):
        for x in range(S):
            e = min(x, y, 15 - x, 15 - y)
            if e == 3:                            # inner frame
                put(im, x, y, rmp[1] if (x > 7 or y > 7) else rmp[4])
            d = max(abs(x - 7.5), abs(y - 7.5)) * 0.62 + min(abs(x - 7.5), abs(y - 7.5)) * 0.62
            if 3.3 <= d < 4.2:                    # octagon ring
                put(im, x, y, rmp[0])
            if (x in (7, 8) and 5 <= y <= 10) or (y in (7, 8) and 5 <= x <= 10):
                put(im, x, y, rmp[1])             # cross in the middle
    return im


def chiseled_glow(base_glow_color):
    im = new()
    for y in range(S):
        for x in range(S):
            d = max(abs(x - 7.5), abs(y - 7.5)) * 0.62 + min(abs(x - 7.5), abs(y - 7.5)) * 0.62
            if 3.3 <= d < 4.2 and (x + y) % 3 == 0:
                put(im, x, y, base_glow_color)
    return im


def pillar_side(rmp, seed):
    im = new()
    n = noise(seed, cell=2)
    flute = [0.15, 0.75, 0.55, 0.45]              # groove, ridge, face, face
    for y in range(S):
        for x in range(S):
            if y in (0, 15):
                c = rmp[0] if y == 15 else rmp[4]
            elif y in (1, 14):
                c = rmp[3] if y == 1 else rmp[1]
            else:
                c = pick(rmp, flute[(x + 1) % 4] + n(x, y) * 0.15)
            put(im, x, y, c)
    return im


def pillar_top(rmp, seed):
    im = new()
    for y in range(S):
        for x in range(S):
            e = min(x, y, 15 - x, 15 - y)
            light = (x + y) < 15
            if e % 3 == 0:
                c = rmp[0] if not light else rmp[1]
            elif e % 3 == 1:
                c = rmp[4] if light else rmp[2]
            else:
                c = rmp[3]
            put(im, x, y, c)
    for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
        put(im, x, y, rmp[0])
    return im


def soil(rmp, seed, accent=None, specks=5):
    im = new()
    n = noise(seed, cell=2)
    n2 = noise(seed + 7, cell=4)
    rng = random.Random(seed)
    for y in range(S):
        for x in range(S):
            put(im, x, y, pick(rmp, 0.5 + n(x, y) * 0.6 + n2(x, y) * 0.4))
    if accent:
        for _ in range(specks):
            put(im, rng.randrange(16), rng.randrange(16), accent[rng.randrange(2)])
    return im


def soil_side(rmp, crust, seed, accent=None):
    im = soil(rmp, seed, accent, specks=3)
    rng = random.Random(seed + 3)
    for x in range(S):
        depth = 2 + rng.choice((0, 0, 1, 1, 2))
        for y in range(depth):
            put(im, x, y, crust[3] if y == 0 else crust[2] if y < depth - 1 else crust[1])
    return im


def bark(rmp, seed):
    """Log side: vertical bark with thin wavy furrows and lit ridges (vanilla-log style)."""
    im = new()
    n = noise(seed, w=S, h=S * 4, cell=2)
    rng = random.Random(seed)
    furrows = [1, 5, 9, 13]
    phase = [rng.uniform(0, 6.28) for _ in furrows]
    for y in range(S):
        for x in range(S):
            v = 0.55 + n(x, y * 0.25) * 0.5
            for f, ph in zip(furrows, phase):
                fx = (f + int(round(math.sin(y * 0.55 + ph) * 1.0))) % 16
                if x == fx:
                    v = 0.04
                elif x == (fx - 1) % 16:
                    v += 0.2
            put(im, x, y, pick(rmp, v))
    return im


def rings(rmp, seed, rim, center=None, cracks=False):
    """Log top: concentric growth rings, bark rim, optional dark core and radial cracks."""
    im = new()
    n = noise(seed, cell=2)
    for y in range(S):
        for x in range(S):
            e = min(x, y, 15 - x, 15 - y)
            if e == 0:
                put(im, x, y, rim[0] if (x + y) % 3 else rim[1])
                continue
            d = math.hypot(x - 7.5, y - 7.5) + n(x, y) * 0.5
            band = int(d * 0.9) % 3
            put(im, x, y, rmp[[1, 4, 3][band]] if e > 1 else rmp[2])
    if center:
        for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8)):
            put(im, x, y, center)
    if cracks:
        for ang in (math.pi * 0.5, math.pi * (0.5 + 2 / 3), math.pi * (0.5 + 4 / 3)):
            for r in range(1, 7):
                put(im, int(round(7.5 + math.cos(ang) * r)), int(round(7.5 + math.sin(ang) * r)), rmp[0])
    return im


def planks(rmp, seed, nails=True):
    """Four horizontal boards (3px + 1px gap), offset end seams, grain streaks, nails at the ends."""
    im = new()
    n = noise(seed, w=32, h=S, cell=4)
    rng = random.Random(seed)
    seams = [rng.choice((3, 7, 11, 12)) for _ in range(4)]
    for b in range(4):
        y0 = b * 4
        t = rng.uniform(-0.07, 0.07)
        for y in range(y0, y0 + 4):
            for x in range(S):
                if y == y0 + 3:
                    put(im, x, y, rmp[0])
                    continue
                v = 0.55 + t + n(x * 2, y) * 0.35
                if y == y0:
                    v += 0.15
                if y == y0 + 2:
                    v -= 0.1
                if x == seams[b]:
                    v = 0.08
                put(im, x, y, pick(rmp, v))
        if nails:
            for nx in (1, 14):
                put(im, nx, y0 + 1, rmp[0])
                put(im, nx, y0, rmp[5] if len(rmp) > 5 else rmp[-1])
    return im


def grain(rmp, seed):
    """Dark vertical wood grain (ferry_dark)."""
    im = new()
    n = noise(seed, cell=2)
    for y in range(S):
        for x in range(S):
            v = 0.5 + math.sin(x * 1.3 + math.sin(y * 0.4 + x) * 0.9) * 0.25 + n(x, y) * 0.3
            put(im, x, y, pick(rmp, v))
    return im


def plates(rmp, seed):
    """Ossified log side: cracked bone plates."""
    return flagstone(rmp, seed, accent=None, specks=0)


def shelf(frame, inside, items, seed):
    """Two-compartment shelf block like a vanilla bookshelf: frame rows 0-1, 7-8, 14-15; items in each row."""
    im = new()
    for y in range(S):
        for x in range(S):
            if y in (0, 7, 8, 15) or x in (0, 15):
                light = y in (0, 8) or x == 0
                c = frame[3] if light else frame[1]
                if y in (7, 15):
                    c = frame[0] if x not in (0,) else frame[1]
                put(im, x, y, c)
            elif y == 1 or y == 9:
                put(im, x, y, frame[2])            # shelf lip
            else:
                put(im, x, y, inside[0] if (x + y) % 5 else inside[1])
    glow = new()
    for row, y_bottom in enumerate((6, 13)):
        for (x, sprite, glows) in items[row]:
            for (dx, dy, c) in sprite:
                put(im, x + dx, y_bottom - dy, c)
                if glows and c in glows:
                    put(glow, x + dx, y_bottom - dy, c, 220)
    return im, glow


def bottle(liquid, cork=(172, 140, 96), glass=(190, 200, 214)):
    hi = tuple(min(255, int(v * 1.35) + 20) for v in liquid)
    return [(0, 0, liquid), (1, 0, liquid), (0, 1, hi), (1, 1, liquid), (0, 2, glass), (1, 2, glass), (0, 3, cork), (1, 3, cork)]


def skull(bone, dark):
    """5x5 skull: dome, eye sockets, teeth. Lit from the top-left."""
    rows = [".bbb.", "bbbbb", "bdbdb", "bbbbb", ".b.b."]
    out = []
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch == "b":
                lit = dy <= 1 and dx <= 2
                shade = dy >= 3 or dx == 4
                out.append((dx, 4 - dy, bone[-1] if lit else bone[0] if shade else bone[len(bone) // 2]))
            elif ch == "d":
                out.append((dx, 4 - dy, dark))
    return out


def banner(cloth, bone, eye, seed):
    """12x24 banner art in a 16x32 texture (model maps uv [0,0,12,12] -> exactly 16px density)."""
    im = Image.new("RGBA", (16, 32), T)
    n = noise(seed, w=12, h=24, cell=2)
    rng = random.Random(seed)
    tatter = [rng.choice((0, 1, 2, 3, 4)) for _ in range(12)]
    for y in range(24):
        for x in range(12):
            if y >= 24 - tatter[x]:
                continue
            fold = 0.12 if x % 4 == 1 else -0.12 if x % 4 == 3 else 0.0
            put(im, x, y, pick(cloth, 0.5 + fold + n(x, y) * 0.3 - (0.15 if y < 1 else 0)))
    rows = [".bbbb.", "bbbbbb", "beebeb", "bbbbbb", ".b.b.."]
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch == "b":
                put(im, 3 + dx, 5 + dy, bone[2] if dy < 2 else bone[1])
            elif ch == "e":
                put(im, 3 + dx, 5 + dy, eye)
    return im


def tombstone(stone, seed):
    """10x11 tombstone face in a 16x16 texture (model maps north uv [0,0,10,11]): bevelled slab, engraved skull, worn lines."""
    im = new()
    n = noise(seed, w=10, h=11, cell=2)
    for y in range(11):
        for x in range(10):
            e = min(x, y, 9 - x, 10 - y)
            v = 0.58 + n(x, y) * 0.3
            if e == 0:
                v = 0.32 if (x == 9 or y == 10) else 0.85
            put(im, x, y, pick(stone, v))
    for dy, row in enumerate([".1111.", "101101", "111111", ".1..1."]):
        for dx, ch in enumerate(row):
            if ch == "1":
                put(im, 2 + dx, 2 + dy, stone[1])  # engraved (recessed) skull
            elif ch == "0":
                put(im, 2 + dx, 2 + dy, stone[0])
    for (x, y) in ((3, 3), (6, 3)):
        put(im, x, y, stone[0])                    # eye sockets
    for y, (a, b) in ((7, (2, 8)), (9, (3, 7))):
        for x in range(a, b):
            if (x * 3 + y) % 5:
                put(im, x, y, stone[1])            # worn inscription
    return im


def downscale_frames(img):
    """For the animated soul seal: palette-mapped 2x2 averaging per frame (keeps the energy look)."""
    w, h = img.size
    frames = h // w
    out = Image.new("RGBA", (w // 2, h // 2), T)
    pal = ramp_from(img, 10)
    for f in range(frames):
        fr = img.crop((0, f * w, w, (f + 1) * w))
        px = fr.load()
        for y in range(0, w, 2):
            for x in range(0, w, 2):
                cell = [px[x, y], px[x + 1, y], px[x, y + 1], px[x + 1, y + 1]]
                op = [c for c in cell if c[3] > 64]
                if len(op) < 2:
                    continue
                if any(sat(c) > 60 for c in op):          # keep the bright energy threads
                    c = max(op, key=lambda c: sat(c) + lum(c) * 0.3)[:3]
                else:
                    avg = [sum(c[i] for c in op) / len(op) for i in range(3)]
                    c = min(pal, key=lambda p: sum((p[i] - avg[i]) ** 2 for i in range(3)))
                a = int(sum(c2[3] for c2 in op) / len(op))
                put(out, x // 2, f * (w // 2) + y // 2, c, a)
    return out


# ------------------------------------------------------------------ build all
def build():
    out = {}
    stone_r = ramp_from(orig("soulstone"), 6, max_sat=40)
    stone_acc = accent_from(orig("soulstone"))
    for name, seed in (("soulstone", 11), ("soulstone_1", 12), ("soulstone_2", 13)):
        out[name] = flagstone(ramp_from(orig(name), 6, max_sat=40), seed, accent_from(orig(name)))
    for name, seed in (("soulstone_bricks", 21), ("soulstone_bricks_1", 22), ("soulstone_bricks_2", 23)):
        out[name] = bricks(ramp_from(orig(name), 6, max_sat=40), seed)
    for name, seed in (("dark_soulstone_bricks", 31), ("dark_soulstone_bricks_1", 32)):
        out[name] = bricks(ramp_from(orig(name), 6, max_sat=40), seed)
    for name, seed in (("cracked_soulstone_bricks", 41), ("cracked_soulstone_bricks_1", 42)):
        out[name] = bricks(ramp_from(orig(name), 6, max_sat=40), seed, cracked=True)
    vein_cols = accent_from(orig("soul_veined_bricks_glow"), 40)
    veined, path = bricks(ramp_from(orig("soul_veined_bricks"), 6, max_sat=40), 51, vein=vein_cols)
    out["soul_veined_bricks"] = veined
    g = new()
    for i, (x, y) in enumerate(path):
        put(g, x, y, vein_cols[1] if i % 4 else vein_cols[0])
    out["soul_veined_bricks_glow"] = g
    for name, seed in (("soulstone_tiles", 61), ("soulstone_tiles_1", 62)):
        out[name] = tiles(ramp_from(orig(name), 6, max_sat=40), seed)
    out["polished_soulstone"] = polished(ramp_from(orig("polished_soulstone"), 6, max_sat=40), 71)
    out["chiseled_soulstone"] = chiseled(ramp_from(orig("chiseled_soulstone"), 6, max_sat=40), 72)
    out["chiseled_soulstone_glow"] = new()          # the original overlay is empty
    out["soulstone_pillar"] = pillar_side(ramp_from(orig("soulstone_pillar"), 6, max_sat=40), 81)
    out["soulstone_pillar_top"] = pillar_top(ramp_from(orig("soulstone_pillar_top"), 6, max_sat=40), 82)
    soil_r = ramp_from(orig("ashen_soil"), 6, max_sat=50)
    out["ashen_soil"] = soil(soil_r, 91, accent_from(orig("ashen_soil")))
    out["ashen_soil_side"] = soil_side(ramp_from(orig("ashen_soil_side"), 6, region=(0, 8, 32, 32), max_sat=50),
                                       ramp_from(orig("ashen_soil_side"), 6, region=(0, 0, 32, 6), max_sat=50), 92,
                                       accent_from(orig("ashen_soil_side")))
    gw = ramp_from(orig("ghostwood_log"), 6)
    out["ghostwood_log"] = bark(gw, 101)
    out["ghostwood_log_top"] = rings(ramp_from(orig("ghostwood_log_top"), 6), 102, rim=gw[:2], cracks=True)
    out["ghostwood_planks"] = planks(ramp_from(orig("ghostwood_planks"), 6), 103)
    out["ferry_planks"] = planks(ramp_from(orig("ferry_planks"), 6), 104)
    out["ferry_dark"] = grain(ramp_from(orig("ferry_dark"), 5), 105)
    bone_r = ramp_from(orig("ossified_log"), 6)
    out["ossified_log"] = plates(bone_r, 111)
    out["ossified_log_top"] = rings(ramp_from(orig("ossified_log_top"), 6), 112, rim=bone_r[:2],
                                    center=ramp_from(orig("ossified_log_top"), 6)[0])

    frame_c = ramp_from(orig("curio_shelf"), 5, max_sat=30)
    inside = [(20, 16, 28), (26, 22, 34)]
    liquids = [(170, 40, 50), (130, 60, 200), (60, 160, 80), (190, 130, 50), (200, 160, 240)]
    row1 = [(2, bottle(liquids[0]), None), (5, bottle(liquids[1]), {liquids[1]}), (8, bottle(liquids[2]), None), (11, bottle(liquids[3]), None)]
    row2 = [(2, bottle(liquids[4]), {liquids[4]}), (6, bottle(liquids[2]), {liquids[2]}), (10, bottle(liquids[1]), None), (13, bottle(liquids[0]), None)]
    out["curio_shelf"], out["curio_shelf_glow"] = shelf(frame_c, inside, [row1, row2], 121)
    bone2 = ramp_from(orig("ossuary_shelf"), 5, region=(4, 4, 28, 14), min_sat=8)
    sk = skull(bone2, (30, 24, 26))
    out["ossuary_shelf"], _ = shelf(ramp_from(orig("ossuary_shelf"), 5, max_sat=30), inside,
                                    [[(2, sk, None), (9, sk, None)], [(2, sk, None), (9, sk, None)]], 122)
    out["soul_seal"] = downscale_frames(orig("soul_seal"))
    out["tattered_banner"] = banner(ramp_from(orig("tattered_banner"), 5), [(96, 80, 106), (122, 104, 132), (152, 134, 162)],
                                    accent_from(orig("tattered_banner"))[1], 131)
    out["tombstone_front"] = tombstone(ramp_from(orig("tombstone_front"), 6, max_sat=40), 141)
    return out


def main():
    out = build()
    for name, im in out.items():
        im.save(os.path.join(OUT, name + ".png"))
    print(f"wrote {len(out)} textures at 16px")
    if "--preview" in sys.argv:
        os.makedirs("/tmp/db_tex", exist_ok=True)
        names = sorted(out)
        cell, cols = 112, 6
        rows = (len(names) + cols - 1) // cols
        sheet = Image.new("RGBA", (cols * (2 * cell + 18) + 10, rows * (cell + 12) + 10), (54, 56, 64, 255))
        for i, n in enumerate(names):
            o = orig(n)
            o = o.crop((0, 0, o.width, min(o.height, o.width * (2 if n == "tattered_banner" else 1))))
            nw = out[n].crop((0, 0, 16, 32 if n == "tattered_banner" else 16))
            x = 10 + (i % cols) * (2 * cell + 18)
            y = 10 + (i // cols) * (cell + 12)
            sz = (cell // 2, cell) if n == "tattered_banner" else (cell, cell)
            sheet.alpha_composite(o.resize(sz, Image.NEAREST), (x, y))
            sheet.alpha_composite(nw.resize(sz, Image.NEAREST), (x + cell + 4, y))
        sheet.save("/tmp/db_tex/blocks16_compare.png")


if __name__ == "__main__":
    main()
