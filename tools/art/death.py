"""Death — phase 1/2 'Reaper' (hooded, robed) and phase 3 'Beast' (towering skeletal monster)."""
import math
from core import Model, BONE, DARK_BONE, ROBE, RAG, PURPLE, SOUL, CLEAR
from materials import (by_face, chain, shaded, cloth, grain, smooth, bevel, vgrad, tatter, trim, holes,
                       outline_edges, phys, hstripes, cracks, stain)


# ================================================================== shared
def pale_skull_front(ctx):
    L = {'c': BONE.cols[2], 'H': BONE.cols[6], 'h': BONE.cols[5], 'b': BONE.cols[4], 'm': BONE.cols[3], 's': BONE.cols[2],
         'S': BONE.cols[6], 'k': BONE.cols[1], 'K': BONE.cols[0], 'E': (200, 150, 255, 255), 'n': BONE.cols[0],
         'T': BONE.cols[5], 'g': BONE.cols[0]}
    G = {'E': SOUL.cols[6], 'K': SOUL.cols[0]}
    ctx.stamp(["cbhHHhbc",
               "bHhbbhHb",
               "hhbbbbhh",
               "bkKbbKkb",
               "mKEbbEKm",
               "bkKnnKkb",
               "sSmnnmSs",
               "mTgTTgTm"], L, glow_legend=G)


def robe(tat=0, hole=None, lining_rows=0, base=3.2, stain_amt=False):
    def paint(ctx):
        cloth(ROBE, base, fold=1.1, period=3.0, stain_ramp=PURPLE if stain_amt else None)(ctx)
        if lining_rows:
            trim(PURPLE, lining_rows, level=3.6)(ctx)
        if hole:
            holes(hole)(ctx)
        if tat:
            tatter(tat, 0.8)(ctx)
            outline_edges(ROBE)(ctx)
    return paint


# ================================================================== Reaper (phase 1 + 2)
def ribcage_reaper(ctx):
    L = {'B': BONE.cols[5], 'b': BONE.cols[4], 'd': BONE.cols[2], 'k': ROBE.cols[0], 'K': ROBE.cols[1],
         'h': (60, 20, 100, 255)}
    G = {'h': SOUL.cols[2]}
    if ctx.face == 'front':
        ctx.stamp(["kdBBBBdk",
                   "kkkBbkkk",
                   "dBbBbbBd",
                   "kkkBbkkk",
                   "kBbBbbBk",
                   "kkkBhkkk",
                   "kdbBbbdk",
                   "kkkBbkkk",
                   "kkdBbdkk",
                   "kkkBbkkk",
                   "kkkbdkkk",
                   "kkkBbkkk",
                   "kdBBBBdk",
                   "kkdbbdkk"], L, glow_legend=G)
    else:
        shaded(ROBE, 1.2, grain(0.3))(ctx)


def robe_torso(ctx):
    robe(base=2.9)(ctx)
    if ctx.face == 'front':
        # robe hangs open: a V that widens down the chest, lined in purple
        for y in range(ctx.h):
            half = 1 + (y * 3) // ctx.h
            c = ctx.w / 2
            for x in range(ctx.w):
                d = abs(x + 0.5 - c)
                if d < half:
                    ctx.set(x, y, CLEAR)
                elif d < half + 1:
                    ctx.set(x, y, PURPLE.at(4.0 - y * 0.1, x, y, 0.2))
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 2 <= x < ctx.w - 2 and 1 <= y < ctx.h - 1 else None)


def hood(ctx):
    robe(base=3.0)(ctx)
    if ctx.face == 'front':
        # face opening; purple-lined rim
        for y in range(ctx.h):
            for x in range(ctx.w):
                inside = 2 <= x < ctx.w - 2 and 2 <= y < ctx.h - 1
                if inside:
                    ctx.set(x, y, CLEAR)
                elif (1 <= x < ctx.w - 1 and 1 <= y) and (x in (1, ctx.w - 2) or y == 1):
                    ctx.set(x, y, PURPLE.at(3.3, x, y, 0.2))
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y < ctx.h - 1 else None)
    if ctx.face == 'top':
        # pointed crown seam
        for y in range(ctx.h):
            ctx.set(ctx.w // 2, y, ROBE.cols[1])


def mantle(ctx):
    robe(tat=3, base=3.2)(ctx)
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 3 <= x < ctx.w - 3 and 1 <= y < ctx.h - 1 else None)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)


def sleeve(lining=False, tat=0):
    def paint(ctx):
        robe(tat=tat, lining_rows=2 if lining else 0, base=2.9)(ctx)
        if ctx.face in ('top', 'bottom'):
            ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)
    return paint


def hand_bone(ctx):
    shaded(BONE, 3.6, grain(0.3, 2), bevel(0.5, -0.5, -0.3))(ctx)


def fingers(ctx):
    shaded(BONE, 3.8, grain(0.3, 2), vgrad(0.3, -0.5))(ctx)
    if ctx.side:
        for y in range(ctx.h):
            for x in range(ctx.w):
                if x % 2 == 1 and y > 0:
                    ctx.set(x, y, CLEAR)
                elif y == ctx.h - 1:
                    ctx.set(x, y, BONE.cols[2])


def orb(ctx):
    cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
    for y in range(ctx.h):
        for x in range(ctx.w):
            d = math.hypot(x - cx, y - cy) / max(cx, 0.5)
            col = SOUL.at(5.4 - d * 2.6, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, col)


def robe_skirt(tat):
    def paint(ctx):
        robe(tat=tat, lining_rows=0, base=3.0, stain_amt=True)(ctx)
        if ctx.face in ('top', 'bottom'):
            ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)
    return paint


def cape(ctx):
    robe(tat=6, base=3.0)(ctx)
    if ctx.face == 'front':  # inner side (facing the body) is lined purple
        cloth(PURPLE, 2.2, fold=1.0)(ctx)
        tatter(6, 0.8)(ctx)
        outline_edges(PURPLE)(ctx)


def build_reaper():
    m = Model('death_reaper', 128, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('hips', 'root', (0, -22, 0))
    m.bone('chest', 'hips', (0, 0, 0))
    m.cube('chest', (-4, -14, -2.5), (8, 14, 5), ribcage_reaper)
    m.cube('chest', (-5, -15, -3), (10, 15, 6), robe_torso, inflate=0.25)
    m.cube('chest', (-6.5, -15.5, -3.5), (13, 5, 7), mantle, inflate=0.2)
    m.bone('cape', 'chest', (0, -14.5, 3.9))
    m.cube('cape', (-6, 0, 0), (12, 37, 0), cape)
    m.bone('head', 'chest', (0, -15, -0.5))
    m.cube('head', (-4, -8, -4), (8, 8, 8), by_face(shaded(BONE, 4.2, grain(0.3, 2), smooth(0.3), bevel(0.8, -0.8, -0.2)),
                                                   front=pale_skull_front, bottom=shaded(BONE, 1.8)))
    m.cube('head', (-5, -10, -5.6), (10, 11, 10), hood)
    m.cube('head', (-2, -11, -1), (4, 1, 5), robe(base=2.8))
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore, hand = f'{side}_arm', f'{side}_forearm', f'{side}_hand'
        m.bone(arm, 'chest', (6.5 * sx, -13, 0))
        m.cube(arm, (-2.5, -2, -2.5), (5, 11, 5), sleeve())
        m.bone(fore, arm, (0, 9, 0))
        m.cube(fore, (-3, 0, -3), (6, 7, 6), sleeve(lining=True, tat=2))
        m.cube(fore, (-1, 0, -1), (2, 7, 2), hand_bone)
        m.bone(hand, fore, (0, 6.5, 0))
        m.cube(hand, (-1.5, 0, -1.5), (3, 2, 3), hand_bone)
        m.cube(hand, (-1.5, 2, -1.5), (3, 3, 3), fingers)
        m.bone(f'{side}_orb', hand, (0, 4, -2.5))
        m.cube(f'{side}_orb', (-1.5, -1.5, -1.5), (3, 3, 3), orb)
    for side, sx in (('right', -1), ('left', 1)):
        leg, shin = f'{side}_leg', f'{side}_shin'
        m.bone(leg, 'hips', (2.5 * sx, 0, 0))
        m.cube(leg, (-1, 0, -1), (2, 11, 2), hand_bone)
        m.cube(leg, (-3, -1, -3), (6, 12, 6), robe_skirt(0), inflate=0.2)
        m.bone(shin, leg, (0, 11, 0))
        m.cube(shin, (-3.5, 0, -3.5), (7, 11, 7), robe_skirt(3))
        m.cube(shin, (-1, 10, -3), (2, 1, 3), hand_bone)
    m.paint()
    return m


# ================================================================== Beast (phase 3)
def beast_bone(base=4.2, crack=1):
    def paint(ctx):
        shaded(DARK_BONE, base, grain(0.3, 2), smooth(0.4, 3.0), bevel(0.8, -0.8, -0.35), vgrad(0.3, -0.5))(ctx)
        if crack:
            # glowing soul fissures
            n = ctx.noise
            for i in range(crack):
                x, y = int(n._h(i, 0, 71) * ctx.w), int(n._h(i, 1, 71) * max(1, ctx.h // 3))
                for k in range(max(3, ctx.h // 3)):
                    if 0 <= x < ctx.w and 0 <= y < ctx.h:
                        ctx.set(x, y, (64, 20, 110, 255))
                        ctx.emit(x, y, SOUL.cols[2 + (k % 2)])
                    y += 1
                    x += int(round(n.white(i, k, 72)))
    return paint


def beast_skull_front(ctx):
    L = {'c': DARK_BONE.cols[3], 'H': DARK_BONE.cols[7], 'h': DARK_BONE.cols[6], 'b': DARK_BONE.cols[5], 'm': DARK_BONE.cols[4],
         's': DARK_BONE.cols[3], 'S': DARK_BONE.cols[7], 'k': DARK_BONE.cols[1], 'K': DARK_BONE.cols[0], 'E': (190, 130, 255, 255),
         'e': (80, 30, 140, 255), 'n': DARK_BONE.cols[0], 't': BONE.cols[4], 'T': BONE.cols[5], 'g': DARK_BONE.cols[0]}
    G = {'E': SOUL.cols[6], 'e': SOUL.cols[3], 'K': SOUL.cols[0]}
    ctx.stamp(["cbhHHHHHHhbc",
               "bHHhbbbbhHHb",
               "mhbsbbbbsbhm",
               "skKKKbbKKKks",
               "sKKeEbbEeKKs",
               "skKKKmmKKKks",
               "bsSmmmmmmSsb",
               "bbsmnnnnmsbb",
               "mbsmnKKnmsbm",
               "tTgTtTTtTgTt"], L, glow_legend=G)


def beast_jaw_front(ctx):
    L = {'t': BONE.cols[4], 'T': BONE.cols[5], 'g': DARK_BONE.cols[0], 's': DARK_BONE.cols[3],
         'm': DARK_BONE.cols[4], 'b': DARK_BONE.cols[5]}
    ctx.stamp(["TgTtgTTgtTgT"[:ctx.w],
               "tggTggggTggt"[:ctx.w],
               "smbbbbbbbbms"[:ctx.w],
               "ssmmbbbbmmss"[:ctx.w]], L)


def beast_ribcage(ctx):
    """Ribs with see-through gaps; the core glow inside shows between them."""
    shaded(DARK_BONE, 4.0, grain(0.3, 2), smooth(0.3), hstripes(0.6, 3))(ctx)
    if ctx.face in ('front', 'right', 'left'):
        for y in range(ctx.h):
            gap = (y % 3 == 2) and 2 < y < ctx.h - 3
            for x in range(ctx.w):
                if gap and not (ctx.face == 'front' and abs(x - (ctx.w - 1) / 2) < 1.6):
                    ctx.set(x, y, CLEAR)
            if y % 3 == 0:
                for x in range(ctx.w):
                    if ctx.px[y][x] != CLEAR:
                        ctx.set(x, y, DARK_BONE.at(5.6 + ctx.light, x, y, 0))
        outline_edges(DARK_BONE, faces=('front', 'right', 'left'))(ctx)
    if ctx.face == 'front':
        c = ctx.w // 2
        for y in range(ctx.h):
            ctx.set(c - 1, y, DARK_BONE.at(6.0, 0, y, 0))
            ctx.set(c, y, DARK_BONE.at(4.6, 0, y, 0))


def soul_core(ctx):
    cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
    for y in range(ctx.h):
        for x in range(ctx.w):
            d = math.hypot(x - cx, y - cy) / max(cx, cy, 0.5)
            col = SOUL.at(5.6 - d * 3.0 + ctx.noise.white(x, y, 3) * 0.3, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, col)


def claws(ctx):
    shaded(DARK_BONE, 5.0, vgrad(0.6, -1.2), grain(0.2))(ctx)
    if ctx.side:
        ctx.set(0, ctx.h - 1, DARK_BONE.cols[7])


def tatters(tat=6, base=2.4):
    def paint(ctx):
        cloth(ROBE, base, fold=1.0, period=3.0, stain_ramp=PURPLE)(ctx)
        holes(0.8, 2, 33)(ctx)
        tatter(tat, 0.85, faces=('front', 'back', 'right', 'left'))(ctx)
        outline_edges(ROBE)(ctx)
    return paint


def build_beast():
    m = Model('death_beast', 256, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('pelvis', 'root', (0, -32, 6))
    m.cube('pelvis', (-7, -3, -4), (14, 6, 8), beast_bone(4.0, 1))
    m.bone('loin_front', 'pelvis', (0, 2, -4.4))
    m.cube('loin_front', (-6, 0, 0), (12, 16, 0), tatters(5))
    m.bone('torso', 'pelvis', (0, -2, 0), (48, 0, 0))
    m.cube('torso', (-1.5, -24, 2.5), (3, 24, 3), beast_bone(5.0, 0))
    m.cube('torso', (-4, -19, -4), (8, 9, 6), soul_core)
    m.cube('torso', (-8, -24, -6), (16, 18, 11), beast_ribcage)
    m.cube('torso', (-11, -27, -5), (22, 5, 10), beast_bone(4.4, 2))
    m.bone('cloak', 'torso', (0, -26, 5.5), (-40, 0, 0))
    m.cube('cloak', (-11, 0, 0), (22, 30, 0), tatters(8, 2.2))
    m.bone('neck', 'torso', (0, -25, -3), (22, 0, 0))
    m.cube('neck', (-2, -7, -2), (4, 8, 4), beast_bone(4.6, 0))
    m.bone('head', 'neck', (0, -6, -1), (-80, 0, 0))
    m.cube('head', (-6, -10, -9), (12, 10, 12), by_face(beast_bone(4.6, 2), front=beast_skull_front,
                                                      bottom=shaded(DARK_BONE, 1.4)))
    m.bone('jaw', 'head', (0, 0, 2))
    m.cube('jaw', (-5.5, 0, -11.5), (11, 4, 11), by_face(beast_bone(4.0, 0), front=beast_jaw_front,
                                                       top=shaded(DARK_BONE, 0.8)))
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore, hand = f'{side}_arm', f'{side}_forearm', f'{side}_hand'
        m.bone(arm, 'torso', (12 * sx, -24, 0), (-48, 0, 0))
        m.cube(arm, (-3, -3, -3), (6, 6, 6), beast_bone(4.8, 1))
        m.cube(arm, (-2, 0, -2), (4, 22, 4), beast_bone(4.2, 1))
        m.cube(arm, (-3.5, 2, -3.5), (7, 9, 7), tatters(4, 2.6))
        m.bone(fore, arm, (0, 21, 0), (-20, 0, 0))
        m.cube(fore, (-1.5, 0, -1.5), (3, 22, 3), beast_bone(4.0, 1))
        m.cube(fore, (-2.5, -1, -2.5), (5, 3, 5), beast_bone(4.8, 0))
        m.bone(hand, fore, (0, 22, 0), (10, 0, 0))
        m.cube(hand, (-3, 0, -3), (6, 3, 6), beast_bone(4.4, 0))
        for i, cx in enumerate((-2.5, -1, 0.5, 2)):
            m.cube(hand, (cx, 2.5, -3.5 + (0.5 if i in (0, 3) else 0)), (1, 10 if i in (1, 2) else 9, 1), claws)
        m.cube(hand, (-3.5 if sx < 0 else 2.5, 1, 0), (1, 6, 1), claws)
    for side, sx in (('right', -1), ('left', 1)):
        thigh, shin, foot = f'{side}_thigh', f'{side}_shin', f'{side}_foot'
        m.bone(thigh, 'pelvis', (5.5 * sx, 0, 0), (-35, 0, 0))
        m.cube(thigh, (-2, -1, -2), (4, 17, 4), beast_bone(4.2, 1))
        m.bone(shin, thigh, (0, 16, 0), (55, 0, 0))
        m.cube(shin, (-1.5, 0, -1.5), (3, 17, 3), beast_bone(4.0, 0))
        m.cube(shin, (-2.5, -1, -2.5), (5, 3, 5), beast_bone(4.8, 0))
        m.bone(foot, shin, (0, 17, 0), (-20, 0, 0))
        m.cube(foot, (-2.5, 0, -6), (5, 2, 8), beast_bone(4.0, 0))
        for cx in (-2.5, -0.5, 1.5):
            m.cube(foot, (cx, 0, -8), (1, 2, 2), claws)
    m.paint()
    return m


def build(v=None):
    return build_beast() if v == 'beast' else build_reaper()


SEATED = {'hips': (0, 0, 0, 0, 11, 0), 'right_leg': (-90, 8, 0, 0, 0, 0), 'left_leg': (-90, -8, 0, 0, 0, 0),
          'right_shin': (90, 0, 0, 0, 0, 0), 'left_shin': (90, 0, 0, 0, 0, 0),
          'right_arm': (-40, 0, 0, 0, 0, 0), 'right_forearm': (-35, 0, 0, 0, 0, 0),
          'left_arm': (-40, 0, 0, 0, 0, 0), 'left_forearm': (-35, 0, 0, 0, 0, 0),
          'cape': (20, 0, 0, 0, 0, 0), 'right_orb': (0, 0, 0, 0, 0, 100), 'left_orb': (0, 0, 0, 0, 0, 100)}
CASTING = {'right_arm': (-20, 0, 45, 0, 0, 0), 'right_forearm': (-60, 0, 0, 0, 0, 0),
           'left_arm': (-20, 0, -45, 0, 0, 0), 'left_forearm': (-60, 0, 0, 0, 0, 0)}
PREVIEW_POSES = {'seated': SEATED, 'casting': CASTING}
