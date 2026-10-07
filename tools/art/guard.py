"""Death's Guard — armored warden of the Great Door. ~3 blocks tall, wields a lunar scythe-axe."""
from core import Model, BONE, DARK_BONE, ROBE, RAG, PURPLE, SOUL, STEEL, LEATHER, WOOD, CLEAR
from materials import (by_face, chain, shaded, cloth, grain, smooth, bevel, vgrad, ao_top, tatter, trim,
                       outline_edges, phys, steel, hstripes, cracks)


# ------------------------------------------------------------------ armor
def plate(base=3.4, lames=0, ridge=False, rim=True, rivet_rows=()):
    """Dark steel plate: rim light on top edge, lame seams every `lames` rows, optional center ridge."""
    def paint(ctx):
        shaded(STEEL, base, grain(0.22, 2), smooth(0.35, 4.0), bevel(1.3 if rim else 0.5, -1.1, -0.45),
               vgrad(0.35, -0.45), dither=0.25)(ctx)
        if not ctx.side:
            return
        if lames:
            for y in range(lames, ctx.h, lames):
                for x in range(ctx.w):
                    ctx.set(x, y - 1, STEEL.at(base - 1.3, x, y, 0))
                    if (x + y) % 5:
                        ctx.set(x, y, STEEL.at(base + 0.9 + ctx.light, x, y, 0))
        if ridge and ctx.face in ('front', 'back') and ctx.w >= 6:
            c = ctx.w // 2
            for y in range(ctx.h):
                ctx.set(c - 1, y, STEEL.at(base + 2.2, c, y, 0))
                ctx.set(c, y, STEEL.at(base - 0.6, c, y, 0))
        for ry in rivet_rows:
            for rx in (1, ctx.w - 2):
                if 0 <= ry < ctx.h - 1 and ctx.w > 3:
                    ctx.set(rx, ry, STEEL.cols[7])
                    ctx.set(rx, ry + 1, STEEL.cols[1])
    return paint


def purple_cloth(tat=3, base=3.4, trim_dark=True):
    def paint(ctx):
        cloth(PURPLE, base, fold=1.0, period=3.0)(ctx)
        if trim_dark:
            trim(RAG, 1, level=2.0)(ctx)
        if tat:
            tatter(tat, 0.8)(ctx)
            outline_edges(PURPLE)(ctx)
    return paint


def tabard_front(ctx):
    purple_cloth(tat=4)(ctx)
    if ctx.face in ('front', 'back'):
        # Death's sigil: a hooded skull stitched in dark thread
        S = {'k': ROBE.cols[1], 'd': ROBE.cols[3], 'e': SOUL.cols[1]}
        ctx.stamp(["..kkkk..",
                   ".kddddk.",
                   ".kdeedk.",
                   ".kddddk.",
                   "..kddk..",
                   "...kk..."], S, ox=0, oy=2, glow_legend={'e': SOUL.cols[2]})


def scarf(ctx):
    purple_cloth(tat=2, base=3.0, trim_dark=False)(ctx)
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 1 < x < ctx.w - 2 and 1 < y < ctx.h - 2 else None)


def belt(ctx):
    shaded(LEATHER, 3.0, grain(0.3, 2), bevel(0.8, -0.8, 0))(ctx)
    if ctx.face == 'front':
        c = ctx.w // 2
        for y in range(ctx.h):
            ctx.set(c - 2, y, STEEL.cols[6]); ctx.set(c + 1, y, STEEL.cols[6])
            ctx.set(c - 1, y, STEEL.cols[3] if y != 1 else SOUL.cols[1]); ctx.set(c, y, STEEL.cols[3] if y != 1 else SOUL.cols[1])
        ctx.emit(c - 1, 1, SOUL.cols[3]); ctx.emit(c, 1, SOUL.cols[3])


# ------------------------------------------------------------------ bone
def skull_front(ctx):
    L = {'c': DARK_BONE.cols[3], 'H': DARK_BONE.cols[7], 'h': DARK_BONE.cols[6], 'b': DARK_BONE.cols[5], 'm': DARK_BONE.cols[4],
         's': DARK_BONE.cols[3], 'S': DARK_BONE.cols[7], 'k': DARK_BONE.cols[1], 'K': DARK_BONE.cols[0], 'E': (170, 110, 240, 255),
         'e': (70, 25, 120, 255), 'n': DARK_BONE.cols[0], 't': BONE.cols[4], 'T': BONE.cols[5], 'g': DARK_BONE.cols[0]}
    G = {'E': SOUL.cols[6], 'e': SOUL.cols[2]}
    ctx.stamp(["cbhHHHHhbc",
               "bHHhbbhHHb",
               "mhbsbbsbhm",
               "skKKbbKKks",
               "sKeEmmEeKs",
               "bkKKmmKKkb",
               "bSmsnnsmSb",
               "mtTgTTgTtm"], L, glow_legend=G)


def skull_shell(ctx):
    shaded(DARK_BONE, 4.8, grain(0.35, 2), smooth(0.3), bevel(1.0, -0.8, -0.2), vgrad(0.4, -0.5))(ctx)
    cracks(DARK_BONE, 1, 4, 1.5, faces=('top', 'right'))(ctx)


def jaw_front(ctx):
    L = {'t': BONE.cols[4], 'T': BONE.cols[5], 'g': DARK_BONE.cols[0], 's': DARK_BONE.cols[3],
         'm': DARK_BONE.cols[4], 'b': DARK_BONE.cols[5], 'c': DARK_BONE.cols[2]}
    ctx.stamp(["sTgTtTgTs", "csmbbbmsc", "ccsmmmscc"], L)


def ribs(ctx):
    L = {'B': BONE.cols[5], 'b': BONE.cols[4], 'd': BONE.cols[2], 'k': ROBE.cols[0], 'K': ROBE.cols[1]}
    if ctx.face in ('front', 'back'):
        rows = ["dbBBbbBBbd"[:ctx.w],
                "kKkkBbkkKk"[:ctx.w],
                "bBbkBbkbBb"[:ctx.w],
                "kkkkBbkkkk"[:ctx.w],
                "kdbkBbkbdk"[:ctx.w],
                "kkkkBbkkkk"[:ctx.w],
                "kkkkbdkkkk"[:ctx.w],
                "kkkkBbkkkk"[:ctx.w],
                "kkkkbdkkkk"[:ctx.w]]
        ctx.stamp([r.center(ctx.w, 'k')[:ctx.w] for r in rows], L)
    else:
        shaded(BONE, 2.6, hstripes(1.4, 2), grain(0.3))(ctx)


def limb_bone(ctx):
    shaded(BONE, 3.8, grain(0.3, 2), smooth(0.3), bevel(0.5, -0.6, -0.35), vgrad(0.2, -0.5))(ctx)


# ------------------------------------------------------------------ weapon
def blade(ctx):
    if ctx.face not in ('front', 'back'):
        return
    W, H = ctx.w, ctx.h
    for y in range(H):
        for x in range(W):
            px = W - 1 - phys(ctx, x)  # 0 = against the shaft (shaft is on the +x side)
            o = (px + 3) ** 2 / 21 ** 2 + (y - 10) ** 2 / 13 ** 2
            i = (px + 8) ** 2 / 19 ** 2 + (y - 11.5) ** 2 / 10.5 ** 2
            if o > 1 or i <= 1:
                continue
            edge = 1 - o  # 0 at the cutting edge
            if edge < 0.09:
                col = STEEL.at(8.0, px, y, 0)          # honed edge
            elif edge < 0.2:
                col = STEEL.at(6.6, px, y, 0)
            else:
                n = ctx.noise.smooth(px, y, 3, 3) * 0.6 + ctx.noise.white(px, y, 4) * 0.25
                col = STEEL.at(4.6 + n + (0.8 if i < 1.12 else 0), px, y, 0.3)  # brighter spine bevel on the inner arc
            ctx.set(x, y, col)
    # a few nicks in the edge
    for k in range(3):
        y = int(ctx.noise._h(k, 0, 9) * (H - 6)) + 3
        for x in range(W):
            px = W - 1 - phys(ctx, x)
            o = (px + 3) ** 2 / 21 ** 2 + (y - 10) ** 2 / 13 ** 2
            if 0.92 < o <= 1:
                ctx.set(x, y, CLEAR)


def shaft(ctx):
    shaded(WOOD, 2.2, grain(0.4, 1), bevel(0.3, -0.3, -0.4))(ctx)
    if ctx.side:
        for y in range(ctx.h):
            if y % 9 in (0, 1) and 4 < y < ctx.h - 4:
                for x in range(ctx.w):
                    ctx.set(x, y, LEATHER.at(3.2 if (x + y) % 2 else 2.0, x, y, 0))


def housing(ctx):
    plate(4.2, rim=True)(ctx)
    if ctx.side:
        G = {'g': SOUL.cols[3], 'G': SOUL.cols[5], 'd': SOUL.cols[1]}
        L = {'g': (120, 60, 200, 255), 'G': (210, 170, 255, 255), 'd': (70, 25, 120, 255)}
        ctx.stamp(["dgd", "gGg", "dgd"][:ctx.h], L, ox=(ctx.w - 3) // 2, oy=(ctx.h - 3) // 2, glow_legend=G)


def build():
    m = Model('deaths_guard', 128, 128)
    m.bone('root', None, (0, 24, 0))
    # torso
    m.bone('hips', 'root', (0, -20, 0))
    m.cube('hips', (-5, -2, -3), (10, 4, 6), by_face(plate(3.0)))
    m.cube('hips', (-6, -3, -3.5), (12, 3, 7), belt)
    m.bone('chest', 'hips', (0, -2, 0), (4, 0, 0))
    m.cube('chest', (-5, -10, -2.5), (10, 10, 5), ribs)
    m.cube('chest', (-6, -16, -4), (12, 9, 8), by_face(plate(3.5, lames=0, ridge=True, rivet_rows=(1,)),
                                                      top=plate(3.0), bottom=shaded(STEEL, 1.4)))
    m.cube('chest', (-6, -9, -4), (12, 2, 8), plate(3.0, rim=True), inflate=0.3)
    m.cube('chest', (-5.5, -17, -4.5), (11, 3, 9), scarf, inflate=0.2)
    # head
    m.bone('head', 'chest', (0, -16, -0.5))
    m.cube('head', (-5, -10, -5), (10, 8, 10), by_face(skull_shell, front=skull_front,
                                                      bottom=shaded(DARK_BONE, 1.6)))
    m.bone('jaw', 'head', (0, -2, 2.5))
    m.cube('jaw', (-4.5, 0, -7), (9, 3, 9), by_face(shaded(DARK_BONE, 4.0, grain(0.35, 2), bevel(0.6, -0.8, 0)),
                                                    front=jaw_front, top=shaded(DARK_BONE, 0.8)))
    # arms
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        m.bone(arm, 'chest', (8 * sx, -13.5, 0))
        m.cube(arm, (-4.5 if sx < 0 else -2.5, -4.5, -5), (7, 6, 10), plate(3.8, lames=3, rivet_rows=()))
        m.cube(arm, (-5 if sx < 0 else -1, -1.5, -4.5), (6, 3, 9), plate(3.2, rim=True))
        m.cube(arm, (-1.5, 0, -1.5), (3, 9, 3), limb_bone)
        m.cube(arm, (-2, 3, -2), (4, 2, 4), plate(3.0))
        m.bone(fore, arm, (0, 9, 0))
        m.cube(fore, (-2.5, 0, -2.5), (5, 8, 5), plate(3.4, lames=4))
        m.cube(fore, (-3, -0.5, -3), (6, 2, 6), plate(4.0, rim=True))
        m.cube(fore, (-2, 8, -2), (4, 3, 4), plate(2.8, rim=False))
    # weapon in the right hand
    m.bone('weapon', 'right_forearm', (0, 9.5, 0))
    m.cube('weapon', (-1, -34, -1), (2, 50, 2), shaft)
    m.cube('weapon', (-2, -29, -2), (4, 5, 4), housing)
    m.cube('weapon', (-1.5, -40, -1.5), (3, 6, 3), plate(4.6))
    m.cube('weapon', (-0.5, -45, -0.5), (1, 5, 1), plate(5.5, rim=False))
    m.bone('blade', 'weapon', (0, 0, 0), rot=(0, -90, 0))  # blade faces forward, so every overhead swing leads with the edge
    m.cube('blade', (-19, -38, 0), (18, 24, 0), blade)
    m.cube('weapon', (-1.5, 15, -1.5), (3, 2, 3), plate(3.6))
    # tabards
    m.bone('tabard_front', 'hips', (0, 0, -3.8))
    m.cube('tabard_front', (-4, 0, 0), (8, 15, 0), tabard_front)
    m.bone('tabard_back', 'hips', (0, 0, 3.8))
    m.cube('tabard_back', (-5, 0, 0), (10, 17, 0), purple_cloth(tat=5))
    # legs
    for side, sx in (('right', -1), ('left', 1)):
        leg, shin = f'{side}_leg', f'{side}_shin'
        m.bone(leg, 'root', (3 * sx, -20, 0))
        m.cube(leg, (-1.5, 0, -1.5), (3, 10, 3), limb_bone)
        m.cube(leg, (-3, -1, -3.5), (5 if sx < 0 else 5, 7, 6), plate(3.4, lames=2))
        m.cube(leg, (-2.5, 8, -3), (5, 3, 5), plate(4.0, rim=True))
        m.bone(shin, leg, (0, 10, 0))
        m.cube(shin, (-2.5, 0, -2.5), (5, 8, 5), plate(3.3, ridge=True))
        m.cube(shin, (-3, 8, -4.5), (6, 2, 7), plate(3.0, rim=True))
    m.paint()
    return m


# rest pose holds the axe across the body; this is the in-game idle
IDLE = {
    'right_arm': (-8, 0, 6, 0, 0, 0), 'right_forearm': (-45, 0, 0, 0, 0, 0), 'weapon': (53, 0, -6, 0, 0, 0),
    'left_arm': (4, 0, -4, 0, 0, 0), 'left_forearm': (-12, 0, 0, 0, 0, 0),
}
PREVIEW_POSES = {'idle': IDLE}
