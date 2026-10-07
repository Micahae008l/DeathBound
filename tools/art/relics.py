"""Second pass on the Underworld's set dressing, at the user's request (2026-10-05): no more skull faces carved into
stone, real-looking skulls and bones instead of cartoon ones, the Hollow's trees and arrows in our own palette instead of
vanilla bone blocks and end rods, glowing mushrooms, and jars with something worth keeping inside.

32x32 for full blocks (the HD pass), 16x16 for model parts."""
import io
import math
import os
import zipfile
import numpy as np
import hd
from hd import N, fbm, surface, BRICK, WOOD, GLOW, worley
from core import Ramp, hx, Noise

BONE_R = Ramp('#2a2620', '#3b352d', '#4e473c', '#62594b', '#776d5c', '#8c816d', '#a1957f', '#b5aa93', '#c8bea7', '#d9d0ba')
PURPLE = Ramp('#1c0b2e', '#2e1249', '#451a6c', '#5e2590', '#7a36b4', '#9850cc', '#b57ae0', '#d2a8f0', '#ecd8fb')
SHAFT = Ramp('#120d16', '#1c1522', '#271d2f', '#33273c', '#3f3149', '#4b3b56')


def _rgba(c, a=255):
    c = hx(c) if isinstance(c, str) else c
    return np.array([c[0], c[1], c[2], a], dtype=np.uint8)


def _img(n=16):
    return np.zeros((n, n, 4), dtype=np.uint8)


# ------------------------------------------------------------------ stone: the carving that replaces the skull
def door_relief_tex(seed=41):
    """A small arched door cut into the stone, shut, with a thread of soul-light down its seam. The Great Door, in miniature."""
    rough = fbm(seed, 2, 4)
    h = np.zeros((N, N))
    glow = np.zeros((N, N, 4), dtype=np.uint8)
    cx, top, bottom, r = 15.5, 13.0, 27, 6.0
    for y in range(N):
        for x in range(N):
            e = min(x, N - 1 - x, y, N - 1 - y)
            h[y, x] = 0.3 + min(1.0, (e + 0.5) / 3.0) * 0.4 + rough[y, x] * 0.04
            dx = x - cx
            inner = (abs(dx) <= r and top <= y <= bottom) or (y < top and dx * dx + (y - top) ** 2 <= r * r)
            outer = (abs(dx) <= r + 2 and top <= y <= bottom + 1) or (y < top and dx * dx + (y - top) ** 2 <= (r + 2) ** 2)
            if inner:
                h[y, x] -= 0.32                                   # the recess
                if abs(dx) < 0.6 and y > top - r + 1:
                    h[y, x] -= 0.12                               # the seam between the two leaves
                    glow[y, x] = _rgba(GLOW.cols[3] if y % 3 else GLOW.cols[4][:3])
                elif y in (18, 23) and 1.5 < abs(dx) < 4.5:
                    h[y, x] += 0.1                                # iron bands across each leaf
            elif outer:
                h[y, x] += 0.22                                   # the raised frame
    for y in range(N):   # the light bleeds a pixel either side of the seam
        for x in range(1, N - 1):
            if glow[y, x, 3] == 0 and (glow[y, x - 1, 3] or glow[y, x + 1, 3]):
                glow[y, x] = (*GLOW.cols[1][:3], 110)
    img = surface(h, 5.2 + rough * 0.5, BRICK, relief=3.4, shade=2.8, ao=7.0, dust=4.0)
    img[glow[:, :, 3] > 200] = hx('#4a1a80')
    return img, glow


# ------------------------------------------------------------------ the Hollow's trees: wood that turned to bone
def ossified_side_tex(seed=71):
    rough = fbm(seed, 4, 4)
    h = np.zeros((N, N))
    alb = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            ridge = math.sin(x * 0.9 + math.sin(y * 0.21 + x * 0.05) * 2.2) * 0.5 + 0.5
            h[y, x] = 0.45 + ridge * 0.25 + rough[y, x] * 0.12
            alb[y, x] = 6.0 + ridge * 0.9 + rough[y, x] * 0.7 - (1.6 if rough[y, x] > 0.78 else 0)
    f1, f2, _ = worley(seed + 3, 7)
    cracks = (f2 - f1) < 0.9
    h[cracks] -= 0.25
    alb[cracks] -= 2.2
    return surface(h, alb, BONE_R, relief=3.0, shade=2.2, ao=6.0, dust=2.5)


def ossified_top_tex(seed=72):
    rough = fbm(seed, 4, 4)
    h = np.zeros((N, N))
    alb = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            d = math.hypot(x - 15.5, y - 15.5)
            ring = math.cos(d * 1.3 + rough[y, x] * 2.0) * 0.5 + 0.5
            h[y, x] = 0.5 + ring * 0.15 - (0.3 if d > 14.5 else 0)
            alb[y, x] = 5.4 + ring * 1.2 + rough[y, x] * 0.5 - (2.4 if d < 2.2 else 0) - (1.2 if d > 14.0 else 0)
    return surface(h, alb, BONE_R, relief=2.4, shade=2.0, ao=5.0, dust=2.0)


# ------------------------------------------------------------------ the curio shelf: ghostwood, crowded with glass
def curio_shelf_tex(seed=83):
    img = hd.planks_tex(seed)
    glow = np.zeros((N, N, 4), dtype=np.uint8)
    back = hx('#100c14')
    contents = [('#7a36b4', True), ('#3fa552', True), ('#a07a2e', False), ('#d2a8f0', True), ('#6e2a2a', False), ('#5d5568', False)]
    r = np.random.default_rng(seed)
    for top in (3, 18):
        img[top:top + 11, 2:30] = back
        img[top + 10, 2:30] = hx('#433d4b')       # the shelf board
        x = 3
        k = int(r.integers(0, 6))
        while x < 28:
            w = int(r.integers(3, 6))
            hgt = int(r.integers(5, 9))
            col, lit = contents[k % len(contents)]
            k += 1
            if x + w > 29:
                break
            y0 = top + 10 - hgt
            for yy in range(y0, top + 10):
                for xx in range(x, x + w):
                    edge = xx in (x, x + w - 1)
                    if yy == y0:
                        img[yy, xx] = hx('#8a7a64')   # cork
                    elif yy == y0 + 1 and w > 3 and edge:
                        continue                      # the neck
                    else:
                        fill = yy > y0 + 2
                        c = hx(col) if fill and not edge else hx('#9aa2b4' if edge else '#2a2d38')
                        img[yy, xx] = c
                        if fill and not edge and lit:
                            glow[yy, xx] = _rgba(col, 220)
            img[y0 + 2, x + 1] = hx('#e6ecf6')       # a glint on the glass
            x += w + 1
    return img, glow


# ------------------------------------------------------------------ skulls, the way a skull actually looks
def skull_textures():
    """Aged bone: a pale crown, darker toward the jaw, with sutures. The face has deep sockets, a nasal hole, real teeth."""
    n = Noise(611)
    out = {}

    def bone_field(base, x, y, salt=0):
        v = base + n.smooth(x, y, 3.0, 2 + salt) * 0.7 + n.white(x, y, 3 + salt) * 0.25
        return BONE_R.at(v, x, y, 0.2)

    bone = _img()
    for y in range(16):
        for x in range(16):
            bone[y, x] = bone_field(7.0 - y * 0.08, x, y)
    for x in range(16):   # a suture wandering across
        y = 7 + int(math.sin(x * 1.3) * 1.5 + (1 if x % 3 == 0 else 0))
        bone[y, x] = BONE_R.cols[3]
    out['skull_bone'] = bone

    front = _img()
    for y in range(16):
        for x in range(16):
            front[y, x] = bone_field(7.4 - y * 0.18, x, y, 5)
    deep, dark, rim = hx('#0a0709'), hx('#1c1612'), BONE_R.cols[4]
    for (sx, sy) in ((2, 5), (9, 5)):   # sockets: rounded, deep, shadowed at the top
        for yy in range(sy, sy + 5):
            for xx in range(sx, sx + 5):
                d = ((xx - sx - 2) / 2.6) ** 2 + ((yy - sy - 2) / 2.4) ** 2
                if d <= 1.0:
                    front[yy, xx] = deep if d < 0.55 else dark
                elif d <= 1.45:
                    front[yy, xx] = rim
    for yy, (a, b) in enumerate(((7, 9), (6, 10), (7, 9)), start=10):   # the nasal hole, an upside-down heart
        for xx in range(a, b):
            front[yy, xx] = deep
    for xx in range(4, 12):   # cheekbones catch the light
        front[9, xx] = BONE_R.cols[8] if xx in (4, 5, 10, 11) else front[9, xx]
    for xx in range(3, 13):   # upper teeth
        front[14, xx] = BONE_R.cols[9] if xx % 2 else BONE_R.cols[7]
        front[15, xx] = hx('#140f0c') if xx % 2 == 0 else BONE_R.cols[6]
    out['skull_front'] = front

    teeth = _img()
    for y in range(16):
        for x in range(16):
            teeth[y, x] = bone_field(5.6, x, y, 9)
    for x in range(16):
        teeth[0, x] = BONE_R.cols[9] if x % 2 else BONE_R.cols[7]
        teeth[1, x] = hx('#140f0c') if x % 2 == 0 else BONE_R.cols[6]
    out['skull_jaw'] = teeth
    return out


def long_bone_tex():
    """Limb bones: pale shaft, knobbly darker ends, a little grime."""
    n = Noise(631)
    img = _img()
    for y in range(16):
        for x in range(16):
            end = y in (0, 1, 14, 15)
            v = 7.2 + n.smooth(x, y, 2.5, 3) * 0.6 + n.white(x, y, 5) * 0.25 - (1.0 if end else 0) - (0.5 if x in (0, 15) else 0)
            img[y, x] = BONE_R.at(v, x, y, 0.2)
    return img


# ------------------------------------------------------------------ the Hunter's arrows: bone heads, purple fletching
def arrow_parts():
    out = {}
    shaft = _img()
    for y in range(16):
        for x in range(16):
            shaft[y, x] = SHAFT.at(2.4 + (0.8 if x % 4 == 0 else 0) + (y % 5 == 0) * 0.4, x, y, 0.1)
    out['hunter_arrow_shaft'] = shaft
    fletch = _img()
    for y in range(16):
        for x in range(16):
            v = 5.2 + math.sin(x * 0.8 + y * 0.4) * 0.8 - (1.4 if (x + y) % 5 == 0 else 0)
            fletch[y, x] = PURPLE.at(v, x, y, 0.15)
    out['hunter_arrow_fletch'] = fletch
    head = _img()
    for y in range(16):
        for x in range(16):
            head[y, x] = BONE_R.at(7.6 - abs(x - 7.5) * 0.18, x, y, 0.15)
    out['hunter_arrow_head'] = head
    return out


def projectile_tex():
    """The vanilla arrow sheet, recoloured: dark shaft, bone head, purple feathers."""
    jar = os.path.expanduser('~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar')
    from PIL import Image
    with zipfile.ZipFile(jar) as z:
        src = Image.open(io.BytesIO(z.read('assets/minecraft/textures/entity/projectiles/arrow.png'))).convert('RGBA')
    swap = {(255, 255, 255): '#d2a8f0', (226, 226, 226): '#9850cc', (193, 193, 193): '#e2d9c3', (158, 158, 158): '#b9ad96',
            (180, 144, 90): '#3a2a44', (159, 132, 77): '#2e2236', (115, 94, 57): '#1e1624', (188, 152, 98): '#4a3756'}
    a = np.array(src)
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            k = tuple(int(v) for v in a[y, x, :3])
            if a[y, x, 3] and k in swap:
                a[y, x, :3] = hx(swap[k])[:3]
    return a


# ------------------------------------------------------------------ glowcaps: the Collector's light
def glowcap_textures():
    n = Noise(651)
    out = {}
    cap = _img()
    for y in range(16):
        for x in range(16):
            v = 5.4 + n.smooth(x, y, 2.5, 1) * 0.8
            cap[y, x] = PURPLE.at(v, x, y, 0.15)
            if n.white(x, y, 2) > 0.78:
                cap[y, x] = _rgba('#f2e6ff')
    out['glowcap_cap'] = cap
    stem = _img()
    for y in range(16):
        for x in range(16):
            stem[y, x] = BONE_R.at(7.4 + n.white(x, y, 4) * 0.4 - (0.6 if x % 3 == 0 else 0), x, y, 0.1)
    out['glowcap_stem'] = stem
    gill = _img()
    for y in range(16):
        for x in range(16):
            gill[y, x] = PURPLE.at(3.0 + (1.6 if x % 2 else 0), x, y, 0.1)
    out['glowcap_gill'] = gill
    return out


# ------------------------------------------------------------------ what the Collector keeps in his jars
def curio_textures():
    n = Noise(661)
    out = {}

    def flat(ramp, base, spread=0.6, salt=0):
        img = _img()
        for y in range(16):
            for x in range(16):
                img[y, x] = ramp.at(base + n.smooth(x, y, 2.0, salt) * spread + n.white(x, y, salt + 1) * 0.2, x, y, 0.15)
        return img

    GOLD = Ramp('#2a1b08', '#4a3210', '#6e4c18', '#93681f', '#b8862b', '#d7a73f', '#efcd6e', '#fbe7a6')
    IRON = Ramp('#141317', '#211f26', '#302d36', '#403c47', '#524d59', '#68626f', '#807a87')
    FLESH = Ramp('#1a0609', '#2e0b11', '#45101a', '#5e1623', '#7a1f2e', '#96303e')
    SAIL = Ramp('#3b3542', '#514a59', '#686072', '#81788b', '#9a91a4', '#b3abbc')
    out['curio_gold'] = flat(GOLD, 5.2, 1.0, 3)
    out['curio_iron'] = flat(IRON, 4.2, 0.8, 5)
    out['curio_hull'] = flat(SHAFT, 3.2, 0.6, 7)
    out['curio_sail'] = flat(SAIL, 3.6, 0.8, 9)
    heart = flat(FLESH, 3.6, 1.0, 11)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 5) % 11 == 0:
                heart[y, x] = _rgba('#9850cc')     # veins gone purple
    out['curio_heart'] = heart
    moth = _img()
    for y in range(16):
        for x in range(16):
            d = abs(x - 7.5)
            v = 3.0 + d * 0.35 + math.sin(y * 1.1) * 0.6
            moth[y, x] = PURPLE.at(v, x, y, 0.1)
            if (int(d) == 4 and y % 6 == 3) or (int(d) == 2 and y % 6 == 0):
                moth[y, x] = _rgba('#f2e6ff')       # eye-spots on the wings
    out['curio_moth'] = moth
    return out


# ------------------------------------------------------------------ the Shade: a person-shaped hole in the fog
def shade_tex():
    """64x64 humanoid sheet, black all over with a faint violet bruise; the legs fray into nothing; two pale eyes."""
    rng = np.random.default_rng(66)
    img = _img(64)
    base = np.array(hx('#07060a'), dtype=float)
    for y in range(64):
        for x in range(64):
            v = rng.random()
            c = base + (np.array(hx('#141019'), dtype=float) - base) * (v ** 3)
            img[y, x] = _rgba(tuple(int(t) for t in c))
    # legs (right at 0,16; left at 16,48): the bottom rows thin out to nothing
    for ox, oy in ((0, 16), (16, 48)):
        for y in range(oy + 4, oy + 16):
            fade = (y - (oy + 9)) / 7.0
            for x in range(ox, ox + 16):
                if fade > 0 and rng.random() < fade:
                    img[y, x] = 0
    eyes = _img(64)
    for x, y in ((10, 12), (13, 12)):
        img[y, x] = _rgba('#d8cfe6')
        eyes[y, x] = _rgba('#efe8fb')
    return img, eyes


# ------------------------------------------------------------------ the three keys to the Death King's gate, and its locks
SYMBOLS = {
    'kings': ["#...##...#",
              "##..##..##",
              "###.##.###",
              "##########",
              "#.##..##.#",
              "##########"],
    'watchers': ["...####...",
                 ".##....##.",
                 "#...##...#",
                 "#..####..#",
                 "#...##...#",
                 ".##....##.",
                 "...####..."],
    'bell': ["....##....",
             "...####...",
             "..######..",
             "..######..",
             "..######..",
             ".########.",
             "##########",
             "....##...."],
}


def _symbol_at(rows, size=16):
    h, w = len(rows), len(rows[0])
    oy, ox = (size - h) // 2, (size - w) // 2
    return [(ox + x, oy + y) for y, row in enumerate(rows) for x, ch in enumerate(row) if ch == '#']


def lock_tex(kind, base_path):
    """The gate's lock: a square socket cut into dark brick, its symbol carved in the back. The glow sheet lights the symbol."""
    from PIL import Image
    img = np.array(Image.open(base_path).convert('RGBA'))[:16, :16].copy()
    glow = _img()
    for y in range(2, 14):
        for x in range(2, 14):
            edge = x in (2, 13) or y in (2, 13)
            img[y, x] = _rgba('#3a3346' if (edge and (y == 2 or x == 2)) else '#100d15' if edge else '#08070b')
    for x, y in _symbol_at(SYMBOLS[kind]):
        img[y, x] = _rgba('#2b2433')
        glow[y, x] = _rgba('#d9b8ff' if (x + y) % 3 else '#a466f0')
    return img, glow


def sigil_icon(kind):
    """A stone medallion with a lit symbol."""
    img = _img()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 7.6:
                rim = d > 6.2
                shade = (7.5 - y) * 0.04 + (7.5 - x) * 0.02
                if rim:
                    img[y, x] = _rgba('#6328ae' if shade > 0 else '#3b1466')
                else:
                    v = 0.5 + shade
                    img[y, x] = _rgba('#2a2532' if v > 0.55 else '#1d1a24' if v > 0.45 else '#15121a')
    for x, y in _symbol_at(SYMBOLS[kind]):
        if 1 <= x <= 14 and 1 <= y <= 14:
            img[y, x] = _rgba('#e5cfff' if (x * 7 + y) % 4 else '#b27ce8')
    return img


# ------------------------------------------------------------------ the First Hunter's Bow
def hunters_bow_textures(jar):
    """Vanilla's bow sheets re-cut in bone and sinew, antler spurs at the tips; the nocked arrow is one of his
    (dark shaft, iron head, purple fletching at the string)."""
    from PIL import Image
    wood = {(40, 30, 11): '#3a3340', (73, 54, 21): '#82776c', (104, 78, 30): '#a69a86', (137, 103, 39): '#d6ccb4'}
    shaft = {(40, 30, 11): '#1d1519', (73, 54, 21): '#2a2024', (104, 78, 30): '#2a2024', (137, 103, 39): '#382b2f'}
    head = {(255, 255, 255): '#8f8a99', (216, 216, 216): '#6c6775', (177, 177, 177): '#4a4651'}
    grip = {(107, 107, 107): '#2f2a30', (150, 150, 150): '#4a4348'}
    string = '#8a7a62'
    out = {}
    for n in ('bow', 'bow_pulling_0', 'bow_pulling_1', 'bow_pulling_2'):
        src = Image.open(io.BytesIO(jar.read(f'assets/minecraft/textures/item/{n}.png'))).convert('RGBA')
        px = [[src.getpixel((x, y)) for x in range(16)] for y in range(16)]
        img = _img()
        pulling = n != 'bow'
        band = [(x, y) for y in range(16) for x in range(16) if pulling and 0 <= x - y <= 1 and y >= 2 and px[y][x][:3] in shaft]
        tail = sorted({y for _, y in band})[-3:]   # the three rows nearest the string: fletching
        for y in range(16):
            for x in range(16):
                r, g, b, a = px[y][x]
                c = (r, g, b)
                if a == 0:
                    continue
                if (x, y) in band:
                    img[y, x] = _rgba(('#b27ce8' if (x + y) % 2 else '#7330b8') if y in tail else shaft[c])
                elif c in head:
                    img[y, x] = _rgba(head[c])
                elif c in grip:
                    img[y, x] = _rgba(grip[c])
                elif c == (68, 68, 68):
                    img[y, x] = _rgba(string)
                elif c in wood:
                    img[y, x] = _rgba(wood[c])
                else:
                    img[y, x] = _rgba(c, a)
        for x, y in ((15, 0), (0, 15)):   # antler spurs where the limbs end
            if img[y, x][3] == 0:
                img[y, x] = _rgba('#a69a86')
        out['hunters_' + n] = img
    return out


# ------------------------------------------------------------------ the Death King's bolts and orbs (drawn additive: black is nothing)
# ------------------------------------------------------------------ the King who took the throne (player overlay, 64x64 skin layout)
def corruption_textures():
    """A grey wash over the whole skin, dark veins crawling from the chest and up the neck, cracks on the face, and two
    eyes and the vein-ends lit purple (the glow sheet). Plus the bone crown (64x32)."""
    rng = np.random.default_rng(13)
    skin, glow = _img(64), _img(64)
    for y in range(64):
        for x in range(64):
            skin[y, x] = (92, 86, 98, 120)   # the colour going out of them
    def vein(x, y, steps, dx, dy):
        for _ in range(steps):
            if 0 <= x < 64 and 0 <= y < 64:
                skin[y, x] = (34, 26, 44, 225)
                if rng.random() < 0.18:
                    glow[y, x] = (150, 80, 230, 255)
            x += dx + (1 if rng.random() < 0.3 else -1 if rng.random() < 0.3 else 0)
            y += dy + (1 if rng.random() < 0.25 else 0)
    for _ in range(14):   # body front (20..27, 20..31) and arms (44..47 / 36..39)
        vein(20 + rng.integers(0, 8), 20 + rng.integers(0, 4), 9, 0, 1)
    for ox, oy in ((44, 20), (36, 52)):
        for _ in range(5):
            vein(ox + rng.integers(0, 4), oy, 10, 0, 1)
    for _ in range(4):   # cracks up the face (8..15, 8..15)
        vein(8 + rng.integers(0, 8), 15, 5, 0, -1)
    for x, y in ((9, 12), (10, 12), (13, 12), (14, 12)):   # the eyes
        skin[y, x] = (40, 20, 60, 255)
        glow[y, x] = (220, 180, 255, 255) if x in (10, 13) else (150, 80, 230, 255)
    crown = np.zeros((32, 64, 4), dtype=np.uint8)
    bone = [(0x8a, 0x80, 0x72), (0xa6, 0x9a, 0x86), (0xc8, 0xbd, 0xa5), (0xe3, 0xda, 0xc4)]
    for y in range(32):
        for x in range(64):
            if y < 20:
                c = bone[min(3, int(rng.random() * 3.2 + (0.6 if y % 11 < 2 else 0)))]
                crown[y, x] = (*c, 255)
    for x in range(0, 64, 5):   # dark cracks, and purple stones set in the band
        crown[2 + (x % 3), x] = (40, 34, 44, 255)
    for x in (13, 31, 49):
        crown[10, x] = (150, 80, 230, 255)
    return skin, glow, crown


# ------------------------------------------------------------------ chiseled soulstone, third try: a quiet carved ashlar
def carved_stone_tex(seed=43):
    """A dressed block with a sunken panel and a carved ring in it: reads as decoration on a floor, a cap or a whole
    column, and never as a face or a door. No light in it; purple stays for things that glow."""
    rough = fbm(seed, 2, 4)
    h = np.zeros((N, N))
    c = (N - 1) / 2.0
    for y in range(N):
        for x in range(N):
            e = min(x, N - 1 - x, y, N - 1 - y)
            h[y, x] = 0.34 + min(1.0, (e + 0.5) / 2.5) * 0.34 + rough[y, x] * 0.05     # bevelled edge
            if 4 <= x <= N - 5 and 4 <= y <= N - 5:
                h[y, x] -= 0.2                                                      # the sunken panel
                d = math.hypot(x - c, y - c)
                if 6.2 <= d <= 8.4:
                    h[y, x] += 0.24                                                 # the carved ring
                elif d <= 2.2:
                    h[y, x] += 0.18                                                 # its boss
                elif (abs(x - c) < 1.0 or abs(y - c) < 1.0) and 2.2 < d < 6.2:
                    h[y, x] += 0.08                                                 # four spokes
            if (x in (4, N - 5) and 4 <= y <= N - 5) or (y in (4, N - 5) and 4 <= x <= N - 5):
                h[y, x] -= 0.06                                                     # the panel's cut edge
    img = surface(h, 5.0 + rough * 0.6, BRICK, relief=3.6, shade=2.8, ao=7.0, dust=4.0)
    return img, np.zeros((N, N, 4), dtype=np.uint8)
