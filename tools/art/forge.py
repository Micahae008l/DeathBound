"""Soulforging art: the Grave Rune item, the Soul trim palette, and the 'soulforged' trim pattern drawn over any armor.

Trim patterns are greyscale in vanilla's 8-step trim_base key (224 brightest ... 0 darkest); the game recolours them
with a material palette (8 colours, brightest first). Layout is the standard 64x32 humanoid armor map.
"""
import math
import numpy as np
from core import Noise, hx
import sprites as sp

KEY = [224, 192, 160, 128, 96, 64, 32, 0]
SOUL_PALETTE = ['#f4e9ff', '#d5aeff', '#ad70f4', '#8443d8', '#6328ae', '#46187f', '#2c0d55', '#170630']


def palette():
    img = np.zeros((1, 8, 4), dtype=np.uint8)
    for i, h in enumerate(SOUL_PALETTE):
        img[0, i] = hx(h)
    return img


def rune():
    return sp.stamp(sp.canvas(), ["................",
                                  "......KKKK......",
                                  ".....KqXXXK.....",
                                  "....KqXXXXxK....",
                                  "...KqXXwXXXxK...",
                                  "...KXXwVwXXxK...",
                                  "..KqXXXVXXXxxK..",
                                  "..KXwXXVXXwXxK..",
                                  "..KXXVVVVVXXxK..",
                                  "..KqXXXVXXXxK...",
                                  "...KXXwVwXxxK...",
                                  "...KxXXwXXxK....",
                                  "....KxxXxxK.....",
                                  ".....KKxxK......",
                                  "......KKK.......",
                                  "................"], sp.L)


# face rects (x0, y0, w, h) of the 64x32 humanoid armor map
HEAD = {'front': (8, 8, 8, 8), 'right': (0, 8, 8, 8), 'left': (16, 8, 8, 8), 'back': (24, 8, 8, 8), 'top': (8, 0, 8, 8)}
BODY = {'front': (20, 20, 8, 12), 'back': (32, 20, 8, 12), 'right': (16, 20, 4, 12), 'left': (28, 20, 4, 12)}
ARM = {'front': (44, 20, 4, 12), 'back': (52, 20, 4, 12), 'right': (40, 20, 4, 12), 'left': (48, 20, 4, 12)}
LEG = {'front': (4, 20, 4, 12), 'back': (12, 20, 4, 12), 'right': (0, 20, 4, 12), 'left': (8, 20, 4, 12)}


def _put(img, x, y, level):
    if 0 <= x < img.shape[1] and 0 <= y < img.shape[0]:
        v = KEY[level]
        img[y, x] = (v, v, v, 255)


def _channel(img, x0, y0, w, h, seed, rows=None, bright=1):
    """A vertical rune-channel winding down a face, with short side glyph strokes and a dark lip."""
    n = Noise(seed)
    x = x0 + w // 2
    for y in range(y0, y0 + h):
        if rows and not (rows[0] <= y - y0 < rows[1]):
            continue
        if n._h(0, y, 3) > 0.72:
            x = max(x0, min(x0 + w - 1, x + (1 if n._h(1, y, 3) > 0.5 else -1)))
        _put(img, x, y, bright)
        if x + 1 < x0 + w:
            _put(img, x + 1, y, 5)
        if (y - y0) % 4 == 1 and w > 3:
            side = -1 if n._h(2, y, 3) > 0.5 else 1
            _put(img, x + side, y, bright + 1)
            _put(img, x + 2 * side, y - 1, bright + 2)


def _band(img, x0, y0, w, y, seed):
    """A horizontal band of rune marks (helmet brow, belt, boot cuffs)."""
    n = Noise(seed)
    for x in range(x0, x0 + w):
        _put(img, x, y, 4)
        if n._h(x, 0, 5) > 0.45:
            _put(img, x, y - 1, 1 if n._h(x, 1, 5) > 0.5 else 2)
        if (x - x0) % 3 == 1:
            _put(img, x, y + 1, 5)


def _sigil(img, x0, y0):
    """The Steward's eye, burned into the breastplate."""
    rows = ["..1111..",
            ".1.22.1.",
            "1.2002.1",
            ".1.22.1.",
            "..1111..",
            "...33...",
            "...3....",
            "..3.3..."]
    for r, row in enumerate(rows):
        for c, ch in enumerate(row):
            if ch != '.':
                _put(img, x0 + c, y0 + r, int(ch))


def pattern(leggings=False):
    img = np.zeros((32, 64, 4), dtype=np.uint8)
    if leggings:
        for k, (x, y, w, h) in enumerate(LEG.values()):
            _channel(img, x, y, w, h, 400 + k, rows=(0, 9))
        for k, (x, y, w, h) in enumerate(BODY.values()):
            _band(img, x, y, w, y + 9, 450 + k)  # belt line on the waist
        return img
    # helmet: a rune circlet at the brow, a seam down the crown
    for k, face in enumerate(('front', 'right', 'left', 'back')):
        x, y, w, h = HEAD[face]
        _band(img, x, y, w, y + 2, 100 + k)
    x, y, w, h = HEAD['top']
    for yy in range(y, y + h):
        _put(img, x + w // 2, yy, 2 if yy % 2 else 1)
    # breastplate: the eye sigil, channels running from it
    x, y, w, h = BODY['front']
    _sigil(img, x, y + 1)
    _channel(img, x, y + 9, w, 3, 201)
    for k, face in enumerate(('back', 'right', 'left')):
        _channel(img, *BODY[face], 210 + k)
    # arms: a spiral of marks winding toward the wrist
    for k, (x, y, w, h) in enumerate(ARM.values()):
        for yy in range(y + 1, y + h, 3):
            _put(img, x + (yy // 3 + k) % w, yy, 1)
            _put(img, x + (yy // 3 + k + 1) % w, yy, 4)
    # boots: a cuff band
    for k, (x, y, w, h) in enumerate(LEG.values()):
        _band(img, x, y, w, y + h - 3, 300 + k)
    return img


def sweep_frames(n=8, size=32):
    """A purple crescent slash, its bright edge running along the arc and the tail burning out (the scythe's swing)."""
    frames = []
    cx, cy, r0 = size / 2, size * 0.7, size * 0.36
    for f in range(n):
        img = np.zeros((size, size, 4), dtype=np.uint8)
        head = -105 + f * 22.0          # degrees, sweeping left to right across the top of the circle
        tail = head - 130
        for y in range(size):
            for x in range(size):
                dx, dy = x + 0.5 - cx, y + 0.5 - cy
                ang = math.degrees(math.atan2(dy, dx))
                if not (tail <= ang <= head):
                    continue
                t = (ang - tail) / (head - tail)            # 0 at the tail, 1 at the leading edge
                width = 1.2 + 5.6 * math.sin(math.pi * min(1.0, t * 1.1)) * (1 - f / (n * 1.8))
                d = abs(math.hypot(dx, dy) - r0)
                if d > width:
                    continue
                core = 1 - d / width
                heat = core * (0.35 + 0.65 * t)
                col = hx('#f4e9ff') if heat > 0.75 else hx('#c597ff') if heat > 0.5 else hx('#8a3cf0') if heat > 0.28 else hx('#4f17a0')
                a = int(200 * min(1.0, 0.2 + heat * 1.1) * (1 - f / (n + 2)))
                img[y, x] = (col[0], col[1], col[2], a)
        frames.append(img)
    return frames


def lantern_icon():
    """Inventory icon for the Wraith Lantern: a dark iron cage around a soul flame."""
    return sp.stamp(sp.canvas(), ["................",
                                  ".......KK.......",
                                  "......KzzK......",
                                  ".......KK.......",
                                  ".....KKKKKK.....",
                                  "....KsSSSSsK....",
                                  "....KzKKKKzK....",
                                  "....Kz.vV.zK....",
                                  "....KzvwVvzK....",
                                  "....KzVwwVzK....",
                                  "....KzvVVvzK....",
                                  "....KzKKKKzK....",
                                  "....KsSSSSsK....",
                                  ".....KKKKKK.....",
                                  "................",
                                  "................"], sp.L)



def open_door_charm():
    """The Charm of the Open Door: a little stone arch on a cord, soul light pouring through it."""
    return sp.stamp(sp.canvas(), ["......kRRk......",
                                  ".....k....k.....",
                                  ".....R....R.....",
                                  "......KKKK......",
                                  "....KKqqqqKK....",
                                  "...KqXXXXXXqK...",
                                  "..KqXKKKKKKXqK..",
                                  "..KXKgvVVvgKXK..",
                                  "..KXKvVwwVvKXK..",
                                  "..KXKVwwwwVKXK..",
                                  "..KXKvVwwVvKXK..",
                                  "..KXKgvVVvgKXK..",
                                  "..KqKggvvggKqK..",
                                  "..KKKKKKKKKKKK..",
                                  "................",
                                  "................"], sp.L)


def _carve(img, mask, depth=0.55):
    """Cut a shape into stone: the groove darkens, its lower-right lip catches light."""
    out = img.copy()
    h, w = mask.shape
    for y in range(h):
        for x in range(w):
            if mask[y, x]:
                out[y, x, :3] = (img[y, x, :3] * depth).astype(np.uint8)
            elif (y > 0 and mask[y - 1, x]) or (x > 0 and mask[y, x - 1]):
                out[y, x, :3] = np.minimum(255, img[y, x, :3].astype(int) * 1.18).astype(np.uint8)
    return out


def tombstone_front():
    import hd
    base = hd.rock_tex(611)
    mask = np.zeros((32, 32), dtype=bool)
    skull = ["..XXXXXX..",
             ".XXXXXXXX.",
             "XX..XX..XX",
             "XX..XX..XX",
             "XXXX..XXXX",
             ".XXXXXXXX.",
             "..X.XX.X..",
             "..XXXXXX.."]
    for r, row in enumerate(skull):
        for c, ch in enumerate(row):
            if ch == 'X':
                mask[4 + r, 11 + c] = True
    for i, (x0, x1) in enumerate(((8, 24), (10, 22), (9, 23))):   # an epitaph worn past reading
        y = 16 + i * 4
        for x in range(x0, x1):
            if (x * 7 + i * 3) % 9 != 0:
                mask[y, x] = True
    return _carve(base, mask)


def ossuary_shelf():
    """Shelves of skulls and bones in a ghostwood frame."""
    import hd
    wood = hd.planks_tex(83)
    img = wood.copy()
    dark = np.array(hx('#0b090d'))
    bone = ['#3b342e', '#5c5248', '#827564', '#a89a83', '#c9bca2', '#e2d8c2']
    skull = ["..aabb..",
             ".abccdb.",
             "abcddeeb",
             "bKKcdKKc",
             "bKKdeKKd",
             "abcnndcb",
             ".bdTdTd.",
             "..bTbT.."]
    pal = {'a': hx(bone[0]), 'b': hx(bone[1]), 'c': hx(bone[2]), 'd': hx(bone[3]), 'e': hx(bone[4]),
           'K': hx('#120e10'), 'n': hx('#1d1715'), 'T': hx(bone[5])}
    for top in (3, 18):
        img[top:top + 11, 2:30] = dark
        for k, sx in enumerate((3, 12, 21)):
            for r, row in enumerate(skull):
                for c, ch in enumerate(row):
                    if ch in pal:
                        img[top + 3 + r, sx + c] = pal[ch]
            # a long bone across the front of the shelf
        for x in range(4, 28):
            if (x + top) % 11 not in (0, 1):
                img[top + 10, x] = hx(bone[3] if x % 3 else bone[4])
    return img


def jars():
    """Glass jars, 16x16 faces whose top-left corner the jar model samples (body 6x8, contents 5x6, cork 3x2).

    The glass is see-through with a hard highlight down one side; the contents are opaque behind it.
    """
    def rgba(h, a=255):
        r, g, b = hx(h)[:3]
        return np.array([r, g, b, a], dtype=np.uint8)

    out = {}
    glass = np.zeros((16, 16, 4), dtype=np.uint8)
    for y in range(16):
        for x in range(16):
            glass[y, x] = rgba('#c8d2e0', 46)
    for y in range(16):
        glass[y, 0] = rgba('#8090a8', 150)          # the curve of the glass darkens at the edges
        glass[y, 5] = rgba('#8090a8', 150)
        glass[y, 1] = rgba('#f4f8ff', 190) if 1 <= y <= 6 else rgba('#c8d2e0', 80)   # the highlight
        glass[y, 4] = rgba('#a8b4c8', 90)
    glass[0, 0:6] = [rgba('#d8e0ec', 170)] * 6      # rim
    glass[7, 0:6] = [rgba('#6c7890', 170)] * 6      # base
    out['jar_glass'] = glass

    cork = np.zeros((16, 16, 4), dtype=np.uint8)
    for y in range(16):
        for x in range(16):
            v = ['#5a3f22', '#7a5532', '#8f6a40', '#6b4a2a'][(x * 3 + y * 5 + (x * y) % 3) % 4]
            cork[y, x] = rgba(v)
    cork[0, :] = [rgba('#a07a4c')] * 16
    out['jar_cork'] = cork

    def contents(rows, legend, back):
        img = np.zeros((16, 16, 4), dtype=np.uint8)
        img[:, :] = rgba(back)
        for r, row in enumerate(rows):
            for c, ch in enumerate(row):
                if ch in legend:
                    img[r, c] = rgba(legend[ch])
                    img[r + 8 if r + 8 < 16 else r, c + 8 if c + 8 < 16 else c] = rgba(legend[ch])
        return img

    out['jar_soul'] = contents(["..v..",
                                ".vVv.",
                                "vVwVv",
                                ".VwV.",
                                "..V.v",
                                ".v..."], {'v': '#7a3cc2', 'V': '#b07cf4', 'w': '#f0e2ff'}, '#2c0d55')
    out['jar_bones'] = contents(["b.dB.",
                                 "BTd.b",
                                 ".bBTd",
                                 "dT.bB",
                                 "Bbd.T",
                                 "bTBdb"], {'b': '#a89a83', 'B': '#c9bca2', 'd': '#6e6252', 'T': '#e2d8c2'}, '#2a221c')
    out['jar_eye'] = contents([".....",
                               ".WWW.",
                               "WWRWW",
                               "WRKRW",
                               ".WRW.",
                               "....."], {'W': '#e8e2d0', 'R': '#b0402c', 'K': '#0c0808'}, '#3d4a2e')
    murk = out['jar_eye']
    for y in range(16):
        for x in range(16):
            if tuple(murk[y, x][:3]) == tuple(rgba('#3d4a2e')[:3]) and (x * 7 + y * 3) % 5 == 0:
                murk[y, x] = rgba('#56663e')
    return out


def artifacts():
    """The eight forgotten things the Collector wants."""
    art = {
        'ferry_coin': ["................",
                       "................",
                       "......nnnn......",
                       "....nnoyyonn....",
                       "...noyYYYYyon...",
                       "...oyYyKKyYyo...",
                       "..noYyKttKyYon..",
                       "..oyYKtKKtKYyo..",
                       "..oyYKttttKYyo..",
                       "..noYyKtKKyYon..",
                       "...oyYyKKyYyo...",
                       "...noyYYYYyon...",
                       "....nnoyyonn....",
                       "......nnnn......",
                       "................",
                       "................"],
        'wooden_horse': ["................",
                         "................",
                         "..........RR....",
                         ".........RhhR...",
                         ".........RhKhR..",
                         "........RhhhhR..",
                         "...RRRRRhhhR....",
                         "..RhhhhhhhhR....",
                         ".RrRhhhhhhhR....",
                         ".R..RhhhhhR.....",
                         "....RrR.RrR.....",
                         "....R.R.R.R.....",
                         "....R.R.R.R.....",
                         "...rr..rr.......",
                         "................",
                         "................"],
        'kings_quill': ["................",
                        "............kK..",
                        "...........kXK..",
                        "..........kXqk..",
                        ".........kXqXk..",
                        "........kXqXk...",
                        ".......kXqXk....",
                        "......kXqXk.....",
                        ".....kXqXk......",
                        "....kXqXk.......",
                        "....kXXk........",
                        "...KkkK.........",
                        "..yY............",
                        ".oy.............",
                        ".n..............",
                        "................"],
        'warden_seal': ["................",
                        "......KKKK......",
                        ".....KzSSzK.....",
                        ".....KzSSzK.....",
                        "......KzzK......",
                        "......KzzK......",
                        "......KzzK......",
                        "....KKKzzKKK....",
                        "...KzSSSSSSzK...",
                        "...KsgvVVvgsK...",
                        "...KsvwVVwvsK...",
                        "...KsgvVVvgsK...",
                        "...KKKKKKKKKK...",
                        "................",
                        "................",
                        "................"],
        'melted_chains': ["................",
                          "................",
                          "......KK........",
                          ".....KsSK.KK....",
                          "....KszzSKsSK...",
                          "...KszKzsSzzK...",
                          "...KsSvzzKzsK...",
                          "..KszzzSszzK....",
                          "..KsKVzszsSzK...",
                          "...KzszzvzszK...",
                          "...KsSzsKzSK....",
                          "....KKzszzK.....",
                          ".....KKKKK......",
                          "................",
                          "................",
                          "................"],
        'kings_ring': ["................",
                       "................",
                       "......KGGK......",
                       ".....KGwVGK.....",
                       "......KGGK......",
                       ".....noyyon.....",
                       "....noK..Kon....",
                       "...noK....Kon...",
                       "...oy......yo...",
                       "...oy......yo...",
                       "...noK....Kon...",
                       "....noK..Kon....",
                       ".....noyyon.....",
                       "......nnnn......",
                       "................",
                       "................"],
        'hunters_arrowhead': ["................",
                              "................",
                              ".......Kt.......",
                              "......KtB.......",
                              "......KBbK......",
                              ".....KtBbdK.....",
                              ".....KBbbdK.....",
                              "....KtBbbbdK....",
                              "....KBbbbbdK....",
                              "....KKKbdKKK....",
                              "......KdDK......",
                              "......KrRK......",
                              "......KrRK......",
                              "......KrRK......",
                              ".......KK.......",
                              "................"],
        'last_drop': ["................",
                      "......KKKK......",
                      "......KRRK......",
                      ".......KK.......",
                      "......KqqK......",
                      ".....Kq..qK.....",
                      "....Kq....qK....",
                      "....Kq.vV.qK....",
                      "....KqvVwVqK....",
                      "....KqVwwVqK....",
                      "....KqvVVvqK....",
                      ".....KqqqqK.....",
                      "......KKKK......",
                      "................",
                      "................",
                      "................"],
    }
    return {k: sp.stamp(sp.canvas(), rows, sp.L) for k, rows in art.items()}


def hunters_charm():
    """The Hunter's Charm: a bone arrowhead with his one eye set in it."""
    return sp.stamp(sp.canvas(), ["................",
                                  ".......Kt.......",
                                  "......KtBK......",
                                  ".....KtBBdK.....",
                                  "....KtBKKbdK....",
                                  "...KtBKvVKbdK...",
                                  "...KBbKVwKbdK...",
                                  "..KtBbKKKKbddK..",
                                  "..KBbbbbbbbddK..",
                                  "..KKKKKdbKKKKK..",
                                  "......KdDK......",
                                  "......KrRK......",
                                  ".....kKrRKk.....",
                                  "....k..KK..k....",
                                  "...k........k...",
                                  "................"], sp.L)
