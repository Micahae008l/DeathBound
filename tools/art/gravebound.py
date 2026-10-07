"""Gravebound — common undead. Two looks share one mesh: 'shrouded' (cloaked, cracked skull) and 'bare' (exposed ribs, rusted blade)."""
from core import Model, Ramp, RAG, BONE, DARK_BONE, ROBE, PURPLE, SOUL, RUST, STEEL, WOOD, LEATHER, CLEAR
from materials import (by_face, chain, shaded, cloth, bone, grain, smooth, bevel, vgrad, ao_top, tatter,
                       holes, trim, cracks, outline_edges, phys, steel)


def skull_front(ctx):
    L = {'c': DARK_BONE.cols[2], 'H': DARK_BONE.cols[7], 'h': DARK_BONE.cols[6], 'b': DARK_BONE.cols[5], 'm': DARK_BONE.cols[4],
         's': DARK_BONE.cols[3], 'S': DARK_BONE.cols[6], 'k': DARK_BONE.cols[1], 'K': DARK_BONE.cols[0], 'E': (150, 90, 230, 255),
         'n': DARK_BONE.cols[0], 'T': BONE.cols[5], 'g': DARK_BONE.cols[0]}
    G = {'E': SOUL.cols[5]}
    ctx.stamp(["cbhHHhbc",
               "bHhbbhHb",
               "hkKbbKkh",
               "bKEmmEKb",
               "mSsnnsSm",
               "sTgTTgTs"], L, glow_legend=G)
    if ctx.model.variant == 'shrouded':  # forehead crack
        ctx.set(5, 0, DARK_BONE.cols[1]); ctx.set(5, 1, DARK_BONE.cols[1]); ctx.set(6, 1, DARK_BONE.cols[2])


def skull_dome(ctx):
    def fn(x, y):
        cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
        d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5 / max(cx, 1)
        return DARK_BONE.at(6.3 - d * 1.6 + ctx.noise.cluster(x, y, 2) * 0.45, x, y)
    ctx.fill(fn)
    if ctx.model.variant == 'shrouded':
        for i, (x, y) in enumerate([(5, 7), (5, 6), (4, 5), (4, 4), (5, 3)]):
            ctx.set(x, y, DARK_BONE.cols[1])


def skull_side(ctx):
    shaded(DARK_BONE, 4.6, grain(0.35, 2), smooth(0.3), bevel(0.8, -0.8, 0), vgrad(0.4, -0.4))(ctx)
    # temple hollow near the front edge (right face: high x = front; left face: low x = front)
    fx = ctx.w - 2 if ctx.face == 'right' else 1
    for y in (2, 3):
        ctx.set(fx, y, DARK_BONE.cols[2])
    ctx.set(fx, 4, DARK_BONE.cols[3])


def jaw_front(ctx):
    L = {'T': BONE.cols[5], 't': BONE.cols[4], 'g': DARK_BONE.cols[0], 's': DARK_BONE.cols[3],
         'm': DARK_BONE.cols[4], 'b': DARK_BONE.cols[5], 'c': DARK_BONE.cols[2]}
    ctx.stamp(["sTgTgTs", "csmbmsc"], L)


def ribcage(ctx):
    L = {'B': BONE.cols[5], 'b': BONE.cols[4], 'd': BONE.cols[3], 'S': BONE.cols[5], 'k': ROBE.cols[0],
         'K': ROBE.cols[1]}
    if ctx.face == 'front':
        ctx.stamp(["dbBBBBbd",
                   "kkdSSdkk",
                   "bBbSSbBb",
                   "kKkSSkKk",
                   "dBbSSbBd",
                   "kKkSSkKk",
                   "kdbSSbdk",
                   "kkkbbkkk",
                   "kKkBbkKk",
                   "kkkbdkkk",
                   "dbBBBBbd",
                   "kdbddbdk"], L)
    elif ctx.face == 'back':
        ctx.stamp(["dbBBBBbd",
                   "bBdbBdBb",
                   "BbdbBdbB",
                   "dkkbBkkd",
                   "bBkbBkBb",
                   "kkkbBkkk",
                   "kdkbBkdk",
                   "kkkbBkkk",
                   "kkkbBkkk",
                   "kkkbdkkk",
                   "dbBBBBbd",
                   "kdbddbdk"], L)
    elif ctx.face in ('right', 'left'):
        ctx.stamp(["dbbd",
                   "kkkk",
                   "BbbB",
                   "kkkk",
                   "bBBb",
                   "kkkk",
                   "dbbd",
                   "kkkk",
                   "kkkk",
                   "kkkk",
                   "dbbd",
                   "kddk"], L)
    else:
        shaded(BONE, 3.4, grain(0.3))(ctx)


def shroud(ctx):
    bare = ctx.model.variant == 'bare'
    cloth(RAG, 3.4, fold=1.1, period=3.0, stain_ramp=PURPLE)(ctx)
    if ctx.face == 'front':
        # torn open chest: ribs show through
        rows = range(1, 10) if bare else range(3, 8)
        for y in rows:
            half = (2 if bare else 1) + (1 if y in (4, 5, 6) else 0)
            jag = int(ctx.noise._h(0, y, 5) * 2)
            for x in range(4 - half - jag % 2, 4 + half + jag // 1 % 2):
                ctx.set(x, y, CLEAR)
    if ctx.face == 'back' and bare:
        holes(0.78, 2, 23, rows=(3, 10))(ctx)
    if ctx.face == 'top':
        # neck hole
        for y in range(1, 3):
            for x in range(2, 6):
                ctx.set(x, y, CLEAR)
    tatter(4 if not bare else 3, 0.75)(ctx)
    outline_edges(RAG)(ctx)


def sleeve(ctx):
    cloth(RAG, 3.6, fold=0.9, period=2.5)(ctx)
    if ctx.face in ('top',):
        for y in range(1, ctx.h - 1):
            for x in range(1, ctx.w - 1):
                ctx.set(x, y, CLEAR)
    tatter(3, 0.8)(ctx)
    outline_edges(RAG)(ctx)


def arm_bone(ctx):
    shaded(BONE, 4.2, grain(0.3, 2), smooth(0.3), bevel(0.5, -0.4, -0.35), vgrad(0.2, -0.4))(ctx)
    if ctx.side:
        # wrist knob and darker clawed hand
        for x in range(ctx.w):
            ctx.set(x, ctx.h - 4, BONE.cols[5])
            ctx.set(x, ctx.h - 3, BONE.cols[3])
            ctx.set(x, ctx.h - 2, BONE.cols[3] if x % 2 == 0 else BONE.cols[2])
            ctx.set(x, ctx.h - 1, BONE.cols[1] if x % 2 == 0 else CLEAR)
        # elbow
        ctx.set(0, 5, BONE.cols[5]); ctx.set(ctx.w - 1, 5, BONE.cols[2])


def leg_bone(ctx):
    shaded(BONE, 4.0, grain(0.3, 2), smooth(0.3), bevel(0.4, -0.5, -0.35), vgrad(0.1, -0.6))(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, 5, BONE.cols[5])
            ctx.set(x, 6, BONE.cols[2])


def knee_rag(ctx):
    cloth(RAG, 3.0, fold=0.7, period=2.0)(ctx)
    tatter(2, 0.7)(ctx)
    if ctx.face in ('top', 'bottom'):
        ctx.fill(lambda x, y: CLEAR if 0 < x < ctx.w - 1 and 0 < y < ctx.h - 1 else None)


def foot(ctx):
    shaded(BONE, 3.6, grain(0.3, 2), bevel(0.5, -0.5, -0.3))(ctx)
    if ctx.face == 'front':
        for x in range(ctx.w):
            if x % 2 == 1:
                ctx.set(x, 0, BONE.cols[1])


def rag(ctx):
    cloth(RAG, 3.0, fold=1.0, period=2.5, stain_ramp=PURPLE)(ctx)
    tatter(4, 0.85, faces=('front', 'back'))(ctx)
    outline_edges(RAG, faces=('front', 'back'))(ctx)


def wisps(ctx):
    # base stays transparent; only the glow layer draws the soul wisps curling off the eyes
    if ctx.face not in ('front', 'back'):
        return
    G = {'e': SOUL.cols[2], 'E': SOUL.cols[3], 'f': SOUL.cols[1]}
    rows = ["f..........f",
            "Ee........eE",
            ".E........E.",
            "............"]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in G:
                ctx.emit(x, y, G[ch])


def grip(ctx):
    shaded(LEATHER, 2.4, lambda c, x, y: 0.7 if (x + y) % 2 == 0 else -0.3)(ctx)


def guard(ctx):
    shaded(RUST, 3.0, grain(0.5, 1), bevel(0.9, -0.8, -0.2))(ctx)


def blade(ctx):
    if ctx.face in ('right', 'left'):
        for y in range(ctx.h):
            for x in range(ctx.w):
                p = phys(ctx, x)  # 0 = tip
                if p == 0 and y != 2:
                    continue
                if p == 1 and y == 0:
                    continue
                if y == 0 and ctx.noise._h(p, 0, 91) > 0.62:  # notched spine
                    continue
                rust = ctx.noise.smooth(p, y, 2.5, 93)
                if y == 2:
                    col = STEEL.at(5.4 + rust * 0.8, x, y)  # honed edge
                elif rust > -0.1:
                    col = RUST.at(3.3 + rust * 1.4 + ctx.noise.white(p, y, 94) * 0.5, x, y)
                else:
                    col = STEEL.at(3.6 + ctx.noise.white(p, y, 95) * 0.6, x, y)
                ctx.set(x, y, col)
    else:
        shaded(RUST, 2.4, grain(0.5, 1))(ctx)


# ------------------------------------------------------------------ the Knight: a soldier who died in his armor
CHAR = Ramp('#0b0908', '#151110', '#211a17', '#2e2420', '#3d302a', '#4f3e35', '#62503f')


def rusted(base=3.4, lames=0, crest=False):
    def paint(ctx):
        steel(STEEL, base, rivets=ctx.side, scratches=1)(ctx)
        for y in range(ctx.h):
            for x in range(ctx.w):
                if ctx.px[y][x] == CLEAR:
                    continue
                if ctx.noise.smooth(phys(ctx, x), y, 2.2, 31) > 0.38:
                    ctx.set(x, y, RUST.at(1.7 + ctx.light * 0.5 + ctx.noise.white(x, y, 32) * 0.5, x, y))
        if lames and ctx.side:
            for y in range(lames, ctx.h, lames):
                for x in range(ctx.w):
                    ctx.set(x, y - 1, STEEL.at(base - 1.6, x, y, 0))
        if crest and ctx.face == 'top':
            for y in range(ctx.h):
                ctx.set(ctx.w // 2, y, STEEL.at(base + 2.2, 0, y, 0))
                ctx.set(ctx.w // 2 - 1, y, STEEL.at(base - 1.0, 0, y, 0))
    return paint


def helm_front(ctx):
    rusted(3.6)(ctx)
    ctx.stamp(["........",
               "........",
               ".KKKKKK.",
               ".KEKKEK.",
               "...KK...",
               "...KK..."], {'K': (10, 8, 12, 255), 'E': (150, 90, 230, 255)}, glow_legend={'E': SOUL.cols[5]})


def knight_torso(ctx):
    # a tattered surcoat hangs below a dented breastplate
    cloth(RAG, 3.0, fold=1.0, period=3.0, stain_ramp=PURPLE)(ctx)
    if ctx.side:
        plate = min(9, ctx.h)
        for y in range(plate):
            for x in range(ctx.w):
                p = phys(ctx, x)
                L = 3.5 + ctx.light * 0.6 + ctx.noise.cluster(p, y, 2) * 0.3 - y * 0.09 + (1.3 if y == 0 else 0)
                if ctx.face in ('front', 'back') and ctx.w >= 6:
                    L += 1.4 if p == ctx.w // 2 - 1 else -0.8 if p == ctx.w // 2 else 0  # the centre ridge
                col = STEEL.at(L, x, y, 0.25)
                if ctx.noise.smooth(p, y, 2.0, 41) > 0.4:
                    col = RUST.at(1.8 + ctx.noise.white(p, y, 42) * 0.5, x, y)
                ctx.set(x, y, col)
            if y == plate - 1:
                for x in range(ctx.w):
                    ctx.set(x, y, STEEL.at(1.4, x, y, 0))
    if ctx.face == 'top':
        for y in range(1, 3):
            for x in range(2, 6):
                ctx.set(x, y, CLEAR)
    tatter(3, 0.7)(ctx)
    outline_edges(RAG)(ctx)


def charred(painter):
    """Burnt to the bone: everything goes to warm black char, and soul-fire glows through the cracks."""
    def paint(ctx):
        painter(ctx)
        for y in range(ctx.h):
            for x in range(ctx.w):
                r, g, b, a = ctx.px[y][x]
                if a == 0:
                    continue
                lum = (0.3 * r + 0.55 * g + 0.15 * b) / 255.0
                cr, cg, cb, _ = CHAR.at(lum * 9.0, x, y, 0.2)
                ctx.px[y][x] = (cr, cg, cb, a)
        if ctx.side and ctx.w >= 2 and ctx.h >= 3:
            n = ctx.noise
            for i in range((ctx.w * ctx.h) // 55):
                x, y = int(n._h(i, 11, 77) * ctx.w), int(n._h(i, 12, 77) * ctx.h)
                for k in range(2 + int(n._h(i, 13, 77) * 3)):
                    if 0 <= x < ctx.w and 0 <= y < ctx.h and ctx.px[y][x][3] > 0:
                        ctx.px[y][x] = (70, 24, 120, 255)
                        ctx.emit(x, y, SOUL.cols[4] if k % 2 else SOUL.cols[3])
                    y += 1
                    x += int(round(n.white(i, k, 78)))
    return paint


def build(variant):
    m = Model('gravebound', 64, 64)
    m.variant = variant
    knight = variant == 'knight'
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -12, 0), (6, 0, 0))
    m.cube('body', (-4, -12, -2), (8, 12, 4), ribcage)
    m.cube('body', (-4, -12, -2), (8, 14, 4), knight_torso if knight else by_face(shroud), inflate=0.45)
    m.bone('head', 'body', (0, -12, 0), (-6, 0, 0))
    m.cube('head', (-4, -8, -4), (8, 6, 8), by_face(rusted(3.4), front=helm_front, top=rusted(3.6, crest=True),
                                                     bottom=shaded(STEEL, 1.6)) if knight else
           by_face(skull_side, front=skull_front, top=skull_dome, bottom=shaded(DARK_BONE, 2.2, grain(0.4)),
                   back=shaded(DARK_BONE, 4.0, grain(0.4, 2), bevel(0.8, -0.8, 0))))
    m.cube('head', (-6, -8, -4.4), (12, 4, 0), wisps)
    m.bone('jaw', 'head', (0, -2, 2))
    m.cube('jaw', (-3.5, 0, -5.5), (7, 2, 7), by_face(shaded(DARK_BONE, 4.0, grain(0.4, 2), bevel(0.5, -0.8, 0)),
                                                      front=jaw_front, top=shaded(DARK_BONE, 1.0)))
    m.bone('right_arm', 'body', (-5, -10, 0), (-6, 0, 0))
    m.cube('right_arm', (-1, -2, -1), (2, 12, 2), arm_bone)
    m.cube('right_arm', (-2, -3, -2), (4, 6, 4), rusted(3.5, lames=2) if knight else sleeve)
    m.bone('left_arm', 'body', (5, -10, 0), (-6, 0, 0))
    m.cube('left_arm', (-1, -2, -1), (2, 12, 2), arm_bone)
    m.cube('left_arm', (-2, -3, -2), (4, 6, 4), rusted(3.5, lames=2) if knight else sleeve)
    m.bone('weapon', 'right_arm', (0, 9, -0.5), (60, 0, 0))
    m.cube('weapon', (-0.5, -0.5, -3), (1, 1, 5), grip)
    m.cube('weapon', (-1, -1.5, -4), (2, 3, 1), guard)
    m.cube('weapon', (-0.5, -1.5, -15), (1, 3, 11), blade)
    m.bone('rag_front', 'body', (0, -0.5, -2.5))
    m.cube('rag_front', (-3.5, 0, 0), (7, 7, 0), rag)
    m.bone('rag_back', 'body', (0, -0.5, 2.5))
    m.cube('rag_back', (-4, 0, 0), (8, 8, 0), rag)
    m.bone('right_leg', 'root', (-2, -12, 0))
    m.cube('right_leg', (-1, 0, -1), (2, 12, 2), leg_bone)
    m.cube('right_leg', (-1.5, 3.5, -1.5), (3, 3, 3), rusted(3.6) if knight else knee_rag)
    m.cube('right_leg', (-1.5, 11, -2.5), (3, 1, 3), foot)
    m.bone('left_leg', 'root', (2, -12, 0))
    m.cube('left_leg', (-1, 0, -1), (2, 12, 2), leg_bone)
    m.cube('left_leg', (-1.5, 3.5, -1.5), (3, 3, 3), rusted(3.6) if knight else knee_rag)
    m.cube('left_leg', (-1.5, 11, -2.5), (3, 1, 3), foot)
    if variant == 'charred':
        for c in m.cubes:
            c.paint = charred(c.paint)
    m.paint()
    return m
