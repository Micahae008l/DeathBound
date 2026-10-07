"""Item, block, particle and GUI textures.  Run via build.py."""
import math
import os
import colorsys
import numpy as np
from PIL import Image
from core import Ramp, hx, Noise, BAYER4

CLEAR = (0, 0, 0, 0)


def canvas(w=16, h=16):
    return np.zeros((h, w, 4), dtype=np.uint8)


def stamp(img, rows, legend, ox=0, oy=0):
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch != '.' and ch in legend:
                img[oy + y, ox + x] = legend[ch]
    return img


def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    Image.fromarray(img, 'RGBA').save(path)


L = {k: hx(v) for k, v in {
    'K': '#120e18',   # outline
    'k': '#2a2232',
    's': '#8e8c9c', 'S': '#c9c7d6', 'z': '#55536a',   # silver / steel
    'b': '#b9ad96', 'B': '#e2d9c3', 'd': '#857a6a', 'D': '#5c5348',   # bone
    't': '#efe8d6',
    'e': '#1c1026',
    'g': '#4c1d7a', 'G': '#7a3cc2', 'p': '#2e0f4f', 'w': '#e3ccff', 'v': '#a868f0', 'V': '#c79bff',   # soul purple
    'o': '#6b4a1e', 'O': '#a9792e', 'y': '#d8b257', 'Y': '#f0d68a', 'n': '#3f2a12',   # tarnished gold
    'r': '#3a2418', 'R': '#5e3a24', 'h': '#7d5233',   # dark wood
    'x': '#2b2730', 'X': '#433d4c', 'q': '#5d5568',   # dark stone
}.items()}


# ============================================================== items
def relic():
    """The Deathbound Relic: a skull pendant on a chain."""
    img = canvas()
    # chain: two drapes converging on a ring above the skull
    def link(x, y, i):
        img[y, x] = L['S'] if i % 2 == 0 else L['z']
    left = [(1, 1), (1, 2), (2, 3), (2, 4), (3, 5), (4, 6), (5, 6), (6, 7)]
    right = [(14, 1), (14, 2), (13, 3), (13, 4), (12, 5), (11, 6), (10, 6), (9, 7)]
    for i, (x, y) in enumerate(left):
        link(x, y, i)
    for i, (x, y) in enumerate(right):
        link(x, y, i + 1)
    img[0, 1] = L['s']
    img[0, 14] = L['s']
    stamp(img, ["KSSK",
                "KzzK"], L, 6, 6)
    stamp(img, ["..KKKKKK..",
                ".KBBBBBBK.",
                "KBBBBBBbdK",
                "KBeeBBeedK",
                "KbeVbbeVdK",
                ".KbbddbdK.",
                ".KtKtKtK..",
                "..KKKKK..."], L, 3, 8)
    return img


def soulbound():
    img = canvas()
    return stamp(img, ["................",
                       "................",
                       "..zK........Kz..",
                       "..KSK......KSK..",
                       "...KSK....KSK...",
                       "....KSKKKKSK....",
                       ".....KvVVwK.....",
                       "....KgvVVwVK....",
                       "....KgGvVVvK....",
                       "....KpgGGvGK....",
                       ".....KpgGgK.....",
                       "....KSKpgKSK....",
                       "...KSK.KK.KSK...",
                       "..KSK......KSK..",
                       "..zK........Kz..",
                       "................"], L)


def seer():
    img = canvas()
    return stamp(img, ["................",
                       ".......v........",
                       "...v...V...v....",
                       "....v..v..v.....",
                       ".....KKKKKK.....",
                       "...KKBBBBBBKK...",
                       "..KBBKgGGgKBBK..",
                       ".KBBKgVwVVgKBdK.",
                       "KBBBKGVeeVGKBbdK",
                       ".KbdKgVeeVgKdbK.",
                       "..KbdKgGGgKddK..",
                       "...KKdddddDKK...",
                       ".....KKKKKK.....",
                       "....v..v..v.....",
                       "...v...V...v....",
                       "................"], L)


def wraith():
    img = canvas()
    return stamp(img, ["......v..v......",
                       ".....vV.vVv.....",
                       "..v..VwvVwV..v..",
                       "..Vv.vVwVVv.vV..",
                       "...VvKKKKKKvV...",
                       "...KKXXXXXXKK...",
                       "..KXXqXXXXqXXK..",
                       "..KXeVeXXeVeXK..",
                       "..KXeeeXXeeeXK..",
                       "..KqXXXeeXXXqK..",
                       "...KXtKtKtKtK...",
                       "...KtKtKtKtXK...",
                       "....KXXXXXXK....",
                       ".....KKKKKK.....",
                       "......v..v......",
                       "................"], L)


def ferryman():
    img = canvas()
    return stamp(img, ["......kRRk......",
                       ".....k....k.....",
                       ".....R....R.....",
                       "......KKKK......",
                       "....KKyyYYKK....",
                       "...KyyOOOOYYK...",
                       "..KyOOnnnnOYYK..",
                       "..KyOnOOOOnOYK..",
                       "..KOOnOKKOnOyK..",
                       "..KOOnOKKOnOyK..",
                       "..KoOnOOOOnOyK..",
                       "..KooOnnnnOOOK..",
                       "...KooOOOOOOK...",
                       "....KKooooKK....",
                       "......KKKK......",
                       "................"], L)


def reaper():
    img = canvas()
    return stamp(img, ["................",
                       "...KKKKKKKKKK...",
                       "...KRhhhhhhRK...",
                       "...KrrrrrrrrK...",
                       "....KSzzzzSK....",
                       "....KzppppzK....",
                       ".....KzppzK.....",
                       "......KzvK......",
                       "......KzvK......",
                       ".....KzzvzK.....",
                       "....KzzzvzzK....",
                       "....KzgvVvgK....",
                       "...KKRhhhhRKK...",
                       "...KrrrrrrrrK...",
                       "...KKKKKKKKKK...",
                       "................"], L)


def soul():
    img = canvas()
    return stamp(img, ["................",
                       ".......v........",
                       "......vV.v......",
                       ".....vVVvV......",
                       ".....VVwVVv.....",
                       "....vVwwwVV.....",
                       "....VwwwwwVv....",
                       "...vVwwBwwwV....",
                       "...VwwBBBwwVv...",
                       "...VwwBBBwwVV...",
                       "...vVwwBwwwVv...",
                       "....VVwwwwVV....",
                       ".....vVVVVv.....",
                       "......vvvv......",
                       "................",
                       "................"], L)


def heart():
    img = canvas()
    return stamp(img, ["................",
                       "....KK....KK....",
                       "...KXqK..KqXK...",
                       "..KXXXqKKqXXXK..",
                       "..KXXvXXXXXqXK..",
                       ".KXXXXvXXvXXXXK.",
                       ".KXqXXXvVvXXXXK.",
                       ".KXXXXXXwXXXqxK.",
                       ".KxXXXvVvXXXXxK.",
                       "..KxXvXXXvXXxK..",
                       "..KxXXXXXXvxxK..",
                       "...KxxXXvXxxK...",
                       "....KxxxvxxK....",
                       ".....KxxxxK.....",
                       "......KxxK......",
                       ".......KK......."], L)


def scythe16():
    img = canvas()
    return stamp(img, ["...KKKKKKK......",
                       ".KKSSSSSSzKK....",
                       "KSSSzzzzzzzzK...",
                       "KSzzKKKKKKKzzK..",
                       "KzKK......KKzzK.",
                       "KK.........KKzzK",
                       "............KgVK",
                       "...........KrvRK",
                       "..........KrhK..",
                       ".........KrhK...",
                       "........KrhK....",
                       ".......KsSK.....",
                       "......KrhK......",
                       ".....KrhK.......",
                       "....KshK........",
                       "....KKK........."], L)


def scythe32():
    """In-hand sprite: twice the detail, rendered large like the vanilla spear."""
    img = canvas(32, 32)
    rows = [
        "........KKKKKKKKKK..............",
        ".....KKKSSSSSSSSzzKKK...........",
        "...KKSSSSzzzzzzzzzzzzKK.........",
        "..KSSSzzzzzzzzzzzzzzzzzK........",
        ".KSSzzzzzKKKKKKKKKzzzzzzK.......",
        "KSSzzzKKK........KKKzzzzzK......",
        "KSzzKK..............KKzzzzK.....",
        "KSzK..................KKzzzK....",
        "KzK.....................KzzzK...",
        "KK.......................KzzzK..",
        "K.........................KzzzK.",
        "...........................KgvK.",
        "..........................KgVvK.",
        ".........................KsSgVK.",
        "........................KrRsK...",
        ".......................KrhRK....",
        "......................KrhRK.....",
        ".....................KrhRK......",
        "....................KrhRK.......",
        "...................KsSzK........",
        "..................KrhRK.........",
        ".................KrhRK..........",
        "................KrhRK...........",
        "...............KrhRK............",
        "..............KrhRK.............",
        ".............KsSzK..............",
        "............KrhRK...............",
        "...........KrhRK................",
        "..........KrhRK.................",
        ".........KSSzK..................",
        "........KzzK....................",
        "........KKK.....................",
    ]
    return stamp(img, rows, L)


def orb(big=False):
    img = canvas()
    cx, cy = 7.5, 7.5
    r = 7.2 if big else 5.0
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy) / r
            if d > 1:
                continue
            core = 1 - d
            if core > 0.62:
                c = L['w']
            elif core > 0.4:
                c = L['V']
            elif core > 0.18:
                c = L['v']
            else:
                c = L['G']
            a = 255 if core > 0.18 else 190
            img[y, x] = (c[0], c[1], c[2], a)
    return img


def egg(base, spot, seed):
    """Vanilla-shaped spawn egg (no tint in 26.x: each egg is its own texture)."""
    img = canvas()
    shape = ["......KKKK......",
             ".....KbbbbK.....",
             "....KbbbbbbK....",
             "...KbbbbbbbbK...",
             "...KbbbbbbbbK...",
             "..KbbbbbbbbbbK..",
             "..KbbbbbbbbbbK..",
             "..KbbbbbbbbbbK..",
             "..KbbbbbbbbbbK..",
             "..KbbbbbbbbbbK..",
             "...KbbbbbbbbK...",
             "...KbbbbbbbbK...",
             "....KKbbbbKK....",
             "......KKKK......"]
    n = Noise(seed)
    b = Ramp(*base)
    s = Ramp(*spot)
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch == 'K':
                img[y + 1, x] = hx(base[0])
            elif ch == 'b':
                shade = 2.2 - (x - 7.5) * 0.12 - (y - 6) * 0.1
                if x < 6 and y < 7:
                    shade += 0.8
                col = b.at(shade, x, y, 0.25)
                if n.cluster(x, y, 2) > 0.55 or n.white(x, y) > 0.86:
                    col = s.at(1.6 + shade * 0.3, x, y, 0)
                img[y + 1, x] = col
    return img


# ============================================================== blocks
SOULSTONE = Ramp('#1b1622', '#241e2c', '#2e2738', '#3a3146', '#473c55', '#564963', '#665873')
PALE_WOOD = Ramp('#3b3640', '#4f4956', '#655e6c', '#7c7484', '#958d9c', '#aaa2b0')
SOIL = Ramp('#211b24', '#2c2430', '#382e3c', '#463a4a', '#544658', '#635468')


def block_noise(seed, ramp, base=3.0, amp=0.9, w=16, h=16, cluster=2):
    n = Noise(seed)
    img = canvas(w, h)
    for y in range(h):
        for x in range(w):
            v = base + n.smooth(x, y, 4.0, 1) * amp * 0.7 + n.cluster(x, y, cluster, 2) * amp * 0.3 + n.white(x, y, 3) * 0.06
            img[y, x] = ramp.at(v, x, y, 0.12)
    return img


def soulstone():
    img = block_noise(11, SOULSTONE, 3.0, 1.0, cluster=3)
    n = Noise(12)
    for i in range(3):  # faint seams of trapped soul
        x, y = int(n._h(i, 0, 5) * 16), int(n._h(i, 1, 5) * 16)
        for k in range(4):
            img[(y + k) % 16, (x + (k // 2)) % 16] = hx('#4a2f6b')
    return img


def bricks(cracked=False, seed=20):
    img = canvas()
    n = Noise(seed)
    for y in range(16):
        for x in range(16):
            row = y // 4
            off = 0 if row % 2 == 0 else 4
            bx = (x + off) % 8
            mortar = y % 4 == 3 or bx == 7
            if mortar:
                img[y, x] = SOULSTONE.at(0.6, x, y, 0)
                continue
            v = 3.1 + n.cluster(x + off, row * 4, 3, 1) * 0.5 + n.white(x, y, 2) * 0.25
            if y % 4 == 0:
                v += 0.9   # top bevel
            if bx == 0:
                v += 0.4
            if y % 4 == 2 or bx == 6:
                v -= 0.5
            img[y, x] = SOULSTONE.at(v, x, y, 0.2)
    if cracked:
        path = [(3, 0), (3, 1), (4, 2), (4, 4), (5, 5), (5, 6), (6, 8), (6, 9), (7, 10), (9, 11), (10, 13), (10, 14), (11, 15)]
        for x, y in path:
            img[y, x] = SOULSTONE.at(0.2, x, y, 0)
            if x + 1 < 16:
                img[y, x + 1] = SOULSTONE.at(4.6, x, y, 0)
        for x, y in [(12, 2), (13, 3), (13, 4), (14, 5)]:
            img[y, x] = SOULSTONE.at(0.2, x, y, 0)
    return img


def tiles():
    img = canvas()
    n = Noise(31)
    for y in range(16):
        for x in range(16):
            tx, ty = x % 8, y % 8
            v = 3.0 + n.cluster(x, y, 2, 1) * 0.35 + n.white(x, y) * 0.15
            if tx == 7 or ty == 7:
                v = 0.7
            elif tx == 0 or ty == 0:
                v += 0.9
            elif tx == 6 or ty == 6:
                v -= 0.6
            img[y, x] = SOULSTONE.at(v, x, y, 0.2)
    return img


def chiseled():
    img = bricks(seed=40)
    skull = ["...KKKKKKKK.....",
             "..KxXXXXXXxK....",
             ".KxXXXXXXXXxK...",
             ".KxPPXXXXPPxK...",
             ".KxPwPXXPwPxK...",
             ".KxPPXXXXPPxK...",
             ".KxXXXxxXXXxK...",
             "..KxXXxxXXxK....",
             "..KxtKtKtKxK....",
             "...KKKKKKKK....."]
    leg = {'K': SOULSTONE.cols[0], 'x': SOULSTONE.cols[2], 'X': SOULSTONE.cols[4], 'P': hx('#8d4fe0'), 'w': hx('#e8d3ff'),
           't': SOULSTONE.cols[5]}
    img[1:15, 1:15] = SOULSTONE.cols[1]
    for y in range(1, 15):
        img[y, 1] = SOULSTONE.cols[0]
        img[y, 14] = SOULSTONE.cols[5]
        img[1, y] = SOULSTONE.cols[0]
        img[14, y] = SOULSTONE.cols[5]
    stamp(img, skull, leg, 2, 3)
    return img


def pillar_side():
    img = canvas()
    n = Noise(50)
    for y in range(16):
        for x in range(16):
            v = 3.0 + n.smooth(x, y * 0.4, 3.0, 1) * 0.4 + n.white(x, y) * 0.15
            if x in (0, 15):
                v -= 1.2
            if x in (1, 14):
                v += 0.6
            if x in (5, 10):
                v -= 0.9
            if x in (6, 11):
                v += 0.5
            if y in (0, 15):
                v = 4.4 if y == 0 else 1.0
            img[y, x] = SOULSTONE.at(v, x, y, 0.2)
    return img


def pillar_top():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            v = 3.0 + (0.8 if int(d) % 3 == 0 else 0) - (1.2 if d > 7 else 0)
            img[y, x] = SOULSTONE.at(v, x, y, 0.2)
    return img


def ashen_top():
    img = block_noise(60, SOIL, 3.2, 1.1)
    n = Noise(61)
    for y in range(16):
        for x in range(16):
            if n.white(x, y, 4) > 0.9:
                img[y, x] = hx('#6f6378')   # ash flecks
            elif n.white(x, y, 5) > 0.95:
                img[y, x] = hx('#5c3a7e')   # soul grit
    return img


def ashen_side():
    img = block_noise(62, SOIL, 2.4, 0.9)
    top = ashen_top()
    n = Noise(63)
    for x in range(16):
        depth = 2 + int(n._h(x, 0, 1) * 3)
        img[0:depth, x] = top[0:depth, x]
    return img


def gloom_grass():
    img = canvas()
    blades = [(3, 9), (5, 12), (7, 14), (8, 11), (10, 13), (12, 10), (6, 8), (11, 7)]
    G = Ramp('#0f0d12', '#18151c', '#221e28', '#2e2835', '#3b3444', '#4a4256')
    for i, (bx, h) in enumerate(blades):
        lean = (-1) ** i * (0.15 + 0.1 * (i % 3))
        for k in range(h):
            x = int(round(bx + lean * k))
            y = 15 - k
            if 0 <= x < 16 and 0 <= y < 16:
                img[y, x] = G.at(0.5 + k / h * 4.2, x, y, 0)
    return img


def log_side():
    img = canvas()
    n = Noise(70)
    for y in range(16):
        for x in range(16):
            v = 2.6 + n.smooth(x * 2.5, y * 0.3, 3, 1) * 0.9 + n.white(x, y) * 0.15
            if x % 4 == 1 and n._h(x, y // 5, 3) > 0.35:
                v -= 1.3  # deep bark furrows
            img[y, x] = PALE_WOOD.at(v, x, y, 0.2)
    for (x, y) in [(10, 5), (11, 5), (10, 6), (11, 6)]:
        img[y, x] = PALE_WOOD.at(0.5, x, y, 0)
    return img


def log_top():
    img = canvas()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d > 7.2:
                img[y, x] = PALE_WOOD.at(1.2, x, y, 0)
            else:
                v = 3.0 + (0.7 if int(d) % 2 == 0 else -0.2)
                img[y, x] = Ramp('#4a4552', '#5f5868', '#787082', '#918a9a', '#a8a1b0').at(v * 0.8, x, y, 0.2)
    return img


def planks():
    img = canvas()
    n = Noise(80)
    for y in range(16):
        for x in range(16):
            row = y // 4
            v = 3.0 + n.smooth(x * 0.5 + row * 7, y * 3, 3, 1) * 0.5 + n.white(x, y) * 0.15
            if y % 4 == 3:
                v = 1.1
            elif y % 4 == 0:
                v += 0.5
            if (x + row * 5) % 16 == 0:
                v = 1.4
            img[y, x] = PALE_WOOD.at(v, x, y, 0.2)
    return img


def crystal():
    img = canvas()
    C = Ramp('#2d0f50', '#4a1b80', '#6d2fb8', '#9455e6', '#b98af7', '#dcc4ff', '#f6edff')
    shards = [(7.5, 1, 2.2, 14), (4, 6, 1.6, 9), (11, 5, 1.7, 10), (2, 10, 1.2, 5), (13, 10, 1.2, 5)]
    for cx, top, half, h in shards:
        for y in range(int(top), 16):
            t = (y - top) / h
            if t > 1:
                break
            w = half * min(1, t * 3.0 + 0.2)
            for x in range(16):
                dx = x - cx
                if abs(dx) <= w:
                    v = 3.4 - dx / max(w, 0.6) * 1.4 + (1.4 if t < 0.25 else 0)
                    img[y, x] = C.at(v, x, y, 0.2)
    return img


def seal_frames(n=8, alpha=170):
    frames = []
    for f in range(n):
        img = canvas()
        for y in range(16):
            for x in range(16):
                a = math.sin((x * 0.6 + y * 0.35) + f * math.pi * 2 / n) + math.sin((y * 0.7 - x * 0.3) - f * math.pi * 2 / n * 2) * 0.6
                v = 2.6 + a * 1.1
                c = Ramp('#2c0b58', '#4f17a0', '#7a2fe6', '#a061ff', '#c597ff', '#e5cfff').at(v, x, y, 0.3)
                img[y, x] = (c[0], c[1], c[2], alpha if v < 4 else 230)
        frames.append(img)
    return np.concatenate(frames, axis=0)


def rift_frames(n=10):
    frames = []
    for f in range(n):
        img = canvas()
        for y in range(16):
            for x in range(16):
                dx, dy = x - 7.5, y - 7.5
                ang = math.atan2(dy, dx)
                d = math.hypot(dx, dy)
                swirl = math.sin(ang * 3 + d * 0.9 - f * math.pi * 2 / n)
                v = 1.5 + swirl * 1.2 + (2.5 - d * 0.25)
                c = Ramp('#05010a', '#1a0632', '#3c1170', '#7a2fe6', '#c597ff', '#ffffff').at(v, x, y, 0.3)
                img[y, x] = (c[0], c[1], c[2], 220)
        frames.append(img)
    return np.concatenate(frames, axis=0)


def lantern_from(vanilla_soul_lantern):
    """Recolour the vanilla soul lantern: cyan soul fire becomes violet, iron darkens."""
    src = np.array(Image.open(vanilla_soul_lantern).convert('RGBA'))
    out = src.copy()
    for y in range(src.shape[0]):
        for x in range(src.shape[1]):
            r, g, b, a = src[y, x]
            if a == 0:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if s > 0.25 and 0.4 < h < 0.6:      # the soul fire
                h = 0.76
                s = min(1, s * 1.1)
            else:                                # the iron cage
                l *= 0.78
                h, s = 0.75, min(0.12, s + 0.06)
            nr, ng, nb = colorsys.hls_to_rgb(h, l, s)
            out[y, x] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return out


# ============================================================== particles
def mote(size):
    img = canvas(8, 8)
    c = 3.5
    for y in range(8):
        for x in range(8):
            d = math.hypot(x - c, y - c)
            if d <= size:
                t = 1 - d / max(size, 0.5)
                col = Ramp('#7a2fe6', '#a061ff', '#c597ff', '#efe2ff').at(t * 3.4, x, y, 0)
                img[y, x] = (col[0], col[1], col[2], int(140 + 115 * t))
    return img


def purple_vanilla(jar_path, name, lift=0.0, sat=0.85):
    """One of Minecraft's own textures, recoloured to soul fire: same pixels, same shading, the hue turned purple.
    lift brightens the coloured pixels only (a fire charge's cracks glow; its charcoal stays dark)."""
    import io
    import zipfile
    from PIL import Image
    with zipfile.ZipFile(jar_path) as z:
        im = Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/{name}.png'))).convert('RGBA')
    px = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a:
                h, sat_, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                hot = sat_ > 0.6 and v > 0.45          # vanilla's orange and yellow cracks
                if lift and hot:                         # glow; the charcoal round them stays charcoal
                    v = lift + (1 - lift) * v
                rr, gg, bb = colorsys.hsv_to_rgb(0.77, min(1.0, sat_ * sat) if hot or not lift else sat_ * 0.35, v)
                px[x, y] = (int(rr * 255), int(gg * 255), int(bb * 255), a)
    return np.array(im)


# ============================================================== GUI
def relic_gui():
    W, H = 256, 256
    img = canvas(W, H)
    P = Ramp('#0e0a14', '#17111f', '#211a2c', '#2c2338', '#3a2f49', '#4a3c5c', '#5d4c72')
    n = Noise(90)
    for y in range(166):
        for x in range(176):
            edge = x < 3 or y < 3 or x > 172 or y > 162
            if (x < 1 or y < 1 or x > 174 or y > 164) and (x + y) in (0,):
                continue
            v = 2.6 + n.cluster(x, y, 3) * 0.2
            if x == 0 or y == 0 or x == 175 or y == 165:
                v = 0
            elif x == 1 or y == 1:
                v = 5.5
            elif x == 174 or y == 164:
                v = 1.0
            img[y, x] = P.at(v, x, y, 0.15)
    # rounded corners
    for (cx, cy) in ((0, 0), (175, 0), (0, 165), (175, 165)):
        img[cy, cx] = CLEAR
    # player inventory slots
    def slot(x, y):
        for yy in range(18):
            for xx in range(18):
                v = 1.4
                if xx == 0 or yy == 0:
                    v = 0.6
                elif xx == 17 or yy == 17:
                    v = 4.2
                img[y + yy, x + xx] = P.at(v, xx, yy, 0)
    for r in range(3):
        for c in range(9):
            slot(7 + c * 18, 83 + r * 18)
    for c in range(9):
        slot(7 + c * 18, 141)
    # engraved band behind the sockets
    for x in range(8, 168):
        img[28, x] = P.at(1.0, x, 0, 0)
        img[57, x] = P.at(4.5, x, 0, 0)
    # socket art at u=176: a carved skull ring with a soul-glow rim
    S = Ramp('#0b0710', '#1b1424', '#2e2338', '#4a3a5c', '#6a4f8c', '#9a6fd6', '#c9a2ff')
    for y in range(26):
        for x in range(26):
            d = math.hypot(x - 12.5, y - 12.5)
            if d > 12.6:
                continue
            if d > 11.4:
                v = 1.0
            elif d > 10.2:
                v = 5.2 - (x + y) * 0.02
            elif d > 9.0:
                v = 2.6
            else:
                v = 1.4
            img[y, 176 + x] = S.at(v, x, y, 0.2)
    return img


def icon(relic_img):
    img = canvas(128, 128)
    for y in range(128):
        for x in range(128):
            d = math.hypot(x - 63.5, y - 63.5) / 90
            c = Ramp('#05030a', '#140b22', '#2a1048', '#4b1d80').at(3.0 - d * 3.2, x, y, 0.3)
            img[y, x] = c
    big = np.array(Image.fromarray(relic_img).resize((112, 112), Image.NEAREST))
    for y in range(112):
        for x in range(112):
            if big[y, x, 3] > 0:
                img[8 + y, 8 + x] = big[y, x]
    return img
