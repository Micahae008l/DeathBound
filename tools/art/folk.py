"""The small folk of the Underworld: Mira (Aldous's daughter, a ghost in the line at the Door), the Lamplighter of
Lantern's End, the Sentry still at his post on the Western Watch, the skeleton children who play in the streets, and
the Lantern Wisp that follows you home."""
import math
from core import Model, Ramp, BONE, DARK_BONE, RAG, SOUL, STEEL, WOOD, LEATHER, CLEAR
from materials import by_face, shaded, cloth, grain, smooth, bevel, vgrad, tatter, trim, outline_edges, phys, steel, hstripes
import minor

PALE = Ramp('#141c2c', '#223048', '#34486a', '#4d668f', '#6f8ab4', '#9ab2d6', '#c9d9f2', '#e8f0ff')
IRON = Ramp('#0b0a0d', '#151419', '#201e25', '#2c2a32', '#3a3740', '#4a4651', '#5d5965', '#76727e')
COAT = Ramp('#0d0b12', '#16131d', '#201b29', '#2b2436', '#372e44', '#443953')
SCARVES = [Ramp('#1b0f22', '#2c1838', '#40224f', '#552d68', '#6c3a84'),        # violet
           Ramp('#16161a', '#24242a', '#34343c', '#46464f', '#5a5a64'),        # ash grey
           Ramp('#1f140d', '#2f1f14', '#43301f', '#58402b', '#6d5139')]        # old brown


def ghost(base=4.2, alpha=215, fade=False, ramp=None):
    """Ghost flesh: like the lost souls', but they're more themselves, so more solid."""
    def paint(ctx):
        r = ramp or PALE
        for y in range(ctx.h):
            for x in range(ctx.w):
                L = base + ctx.light * 0.8 + ctx.noise.smooth(phys(ctx, x), y, 3.0, 2) * 0.5
                col = r.at(L, x, y, 0.25)
                wisp = 0.75 + 0.25 * ctx.noise.smooth(phys(ctx, x) * 2.5, y * 0.6, 2.0, 7)
                a = alpha * wisp
                if fade and ctx.side:
                    a *= 1 - 0.8 * y / max(1, ctx.h - 1)
                ctx.set(x, y, (col[0], col[1], col[2], max(10, int(a))))
    return paint


# ------------------------------------------------------------------ Mira
def mira_face(ctx):
    ghost(5.0, 235)(ctx)
    L = {'h': PALE.cols[6], 'H': PALE.cols[7], 'k': (30, 26, 52, 255), 'E': (235, 228, 255, 255), 'm': (40, 30, 64, 255), 't': (240, 240, 250, 255),
         'c': (150, 160, 205, 255)}
    G = {'E': (205, 215, 255, 255)}
    ctx.stamp(["HhHHHhH",
               "h.....h",
               ".EE.EE.",
               ".Ek.Ek.",
               ".c...c.",
               "...tm..",
               "......."], L, glow_legend=G)


def mira_hair(ctx):
    ghost(5.6, 225, ramp=PALE)(ctx)
    if ctx.face == 'front':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y >= 1 else None)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)


def ribbon(ctx):
    for y in range(ctx.h):
        for x in range(ctx.w):
            col = minor.GHOSTS['dusk'].at(5.0 + ctx.light, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, (col[0], col[1], col[2], 120))


def nightdress(ctx):
    ghost(4.6, 215, fade=True)(ctx)
    if ctx.face == 'front':   # lace at the collar
        for x in range(ctx.w):
            if x % 2 == 0:
                ctx.set(x, 0, PALE.cols[7])


def build_mira():
    m = Model('mira', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('skirt', 'root', (0, -6, 0))
    m.cube('skirt', (-4, 0, -3), (8, 6, 6), nightdress)
    m.bone('body', 'skirt', (0, 0, 0))
    m.cube('body', (-3, -6, -2), (6, 6, 4), nightdress)
    m.bone('head', 'body', (0, -6, 0))
    m.cube('head', (-3.5, -7, -3.5), (7, 7, 7), by_face(ghost(5.0, 235), front=mira_face))
    m.cube('head', (-4, -7.5, -4), (8, 5, 8), mira_hair, inflate=0.1)
    m.bone('braid', 'head', (0, -3, 3.8))
    m.cube('braid', (-1, 0, 0), (2, 7, 1), ghost(5.4, 220))
    m.cube('head', (-2.5, -9, -1), (5, 2, 1), ribbon)
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_arm', 'body', (3.8 * sx, -5.5, 0), (-10, 0, 0))
        m.cube(f'{side}_arm', (-1, 0, -1), (2, 6, 2), ghost(4.8, 215))
    m.paint()
    return m


# ------------------------------------------------------------------ the Lamplighter
def lamp_face(ctx):
    ghost(4.4, 235)(ctx)
    L = {'k': (26, 24, 46, 255), 'E': (225, 230, 255, 255), 'b': PALE.cols[6], 'B': PALE.cols[7], 'w': PALE.cols[5]}
    ctx.stamp(["w.....w",
               "kE...Ek",
               "kk...kk",
               "...w...",
               ".bbbbb.",
               "bBbBbBb",
               "BbBbBbB"], L, glow_legend={'E': (210, 220, 255, 255)})


def hat(ctx):
    cloth(COAT, 2.6, fold=0.4)(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, ctx.h - 2, RAG.cols[5])   # a band round the crown


def long_coat(ctx):
    cloth(COAT, 3.0, fold=1.1, period=3.0)(ctx)
    tatter(3, 0.7)(ctx)
    outline_edges(COAT)(ctx)
    if ctx.face == 'front':   # brass buttons long gone green
        for y in range(1, ctx.h - 3, 3):
            ctx.set(ctx.w // 2, y, (120, 140, 110, 255))


def lamp_pole(ctx):
    shaded(WOOD, 2.8, grain(0.3, 2), bevel(0.3, -0.3, -0.4))(ctx)


def cage(ctx):
    steel(IRON, 3.2, rivets=False, scratches=0)(ctx)
    if ctx.side:
        for y in range(1, ctx.h - 1):
            for x in range(1, ctx.w - 1):
                ctx.set(x, y, CLEAR)


def flame(ctx):
    for y in range(ctx.h):
        for x in range(ctx.w):
            hot = 1 - y / max(1, ctx.h - 1)
            col = SOUL.at(2.2 + (1 - hot) * 3.0 + ctx.noise.white(x, y, 4) * 0.4, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, col)


def build_lamplighter():
    m = Model('lamplighter', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -14, 0), (6, 0, 0))
    m.cube('body', (-3.5, -12, -2), (7, 12, 4), long_coat)
    m.bone('coat', 'body', (0, 0, 0))
    m.cube('coat', (-4, 0, -2.5), (8, 12, 5), by_face(long_coat, top=lambda c: None), inflate=0.1)
    m.bone('head', 'body', (0, -12, -0.5), (12, 0, 0))
    m.cube('head', (-3.5, -7, -3.5), (7, 7, 7), by_face(ghost(4.4, 230), front=lamp_face))
    m.cube('head', (-5, -7.6, -5), (10, 1, 10), hat)
    m.cube('head', (-3.5, -13, -3.5), (7, 6, 7), hat)
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_arm', 'body', (4.5 * sx, -11, 0), (-15 if sx < 0 else -45, 0, 0))
        m.cube(f'{side}_arm', (-1.5, -1, -1.5), (3, 11, 3), long_coat)
        m.cube(f'{side}_arm', (-1, 9.5, -1), (2, 2, 2), ghost(4.6, 230))
    m.bone('pole', 'left_arm', (0, 10.5, 0), (40, 0, 0))
    m.cube('pole', (-0.5, -18, -0.5), (1, 26, 1), lamp_pole)
    m.cube('pole', (-0.5, -18, -0.5), (1, 1, 5), lamp_pole)
    m.bone('lamp', 'pole', (0, -17.5, 4.5))
    m.cube('lamp', (-1.5, 0, -1.5), (3, 4, 3), cage)
    m.cube('lamp', (-1, 0.5, -1), (2, 3, 2), flame)
    m.paint()
    return m


# ------------------------------------------------------------------ the Sentry
def sentry_face(ctx):
    ghost(4.2, 235)(ctx)
    L = {'k': (22, 22, 40, 255), 'E': (220, 230, 255, 255), 'm': (40, 40, 66, 255)}
    ctx.stamp([".......",
               "kE...Ek",
               "kk...kk",
               ".......",
               "..mmm..",
               ".......",
               "......."], L, glow_legend={'E': (210, 220, 255, 255)})


def helm(ctx):
    steel(STEEL, 3.6, rivets=True, scratches=3)(ctx)
    if ctx.face == 'front':   # the face is open; a dent over the left eye
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y >= 3 else None)
        if ctx.w > 3:
            ctx.set(ctx.w - 3, 2, STEEL.cols[1])
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)


def breastplate(ctx):
    steel(STEEL, 3.4, rivets=True, scratches=2)(ctx)


def build_sentry():
    m = Model('sentry', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -14, 0))
    m.cube('body', (-4, -12, -2), (8, 12, 4), ghost(4.0, 200, fade=True))
    m.cube('body', (-4.5, -12.5, -2.5), (9, 7, 5), breastplate, inflate=0.1)
    m.bone('tail', 'body', (0, 0, 0))
    m.cube('tail', (-3, 0, -2), (6, 8, 4), ghost(3.6, 150, fade=True))
    m.bone('head', 'body', (0, -12.5, 0))
    m.cube('head', (-3.5, -7, -3.5), (7, 7, 7), by_face(ghost(4.2, 230), front=sentry_face))
    m.cube('head', (-4, -8, -4), (8, 6, 8), helm, inflate=0.15)
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_arm', 'body', (5 * sx, -11.5, 0), (-30 if sx < 0 else 0, 0, 0))
        m.cube(f'{side}_arm', (-1.5, -1, -1.5), (3, 11, 3), ghost(4.0, 210))
        m.cube(f'{side}_arm', (-2, -1.5, -2), (4, 3, 4), breastplate)
    m.bone('spear', 'right_arm', (0, 9.5, 0), (60, 0, 0))
    m.cube('spear', (-0.5, -22, -0.5), (1, 30, 1), lamp_pole)
    m.cube('spear', (-1, -26, -0.5), (2, 4, 1), breastplate)
    m.paint()
    return m


# ------------------------------------------------------------------ the skeleton children
def kid_skull(ctx):
    shaded(BONE, 4.6, grain(0.25, 2), smooth(0.3), bevel(0.6, -0.7, -0.35), vgrad(0.2, -0.5))(ctx)
    if ctx.face == 'front':
        L = {'k': (14, 12, 18, 255), 'K': (30, 26, 34, 255), 'E': SOUL.cols[4], 't': BONE.cols[6], 'n': (24, 20, 28, 255)}
        ctx.stamp([".......",
                   ".......",
                   ".kk.kk.",
                   ".kE.Ek.",
                   "...n...",
                   ".ttttt.",
                   "..ttt.."], L, glow_legend={'E': SOUL.cols[5]})


def kid_ribs(ctx):
    shaded(BONE, 3.6, hstripes(1.2, 2), grain(0.3))(ctx)


def build_skelkid(variant=0):
    scarf = SCARVES[variant % len(SCARVES)]
    m = Model('skeleton_kid', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -6, 0))
    m.cube('body', (-2.5, -5, -1.5), (5, 5, 3), kid_ribs)
    m.cube('body', (-3, -5.5, -2), (6, 2, 4), lambda c: cloth(scarf, 3.0, fold=0.6)(c))   # a scarf, or what's left of one
    m.bone('scarf_tail', 'body', (1.5, -4.5, 2))
    m.cube('scarf_tail', (-1, 0, 0), (2, 4, 1), lambda c: cloth(scarf, 2.6, fold=0.6)(c))
    m.bone('head', 'body', (0, -5.5, 0))
    m.cube('head', (-3.5, -7, -3.5), (7, 7, 7), by_face(lambda c: shaded(BONE, 4.4, grain(0.25, 2), bevel(0.6, -0.7, -0.35))(c), front=kid_skull))
    m.bone('cap', 'head', (0, 0, 0))   # a cap pulled down over one socket (the model shows it on some of them)
    m.cube('cap', (-4, -7.8, -4), (8, 2, 8), lambda c: cloth(scarf, 2.4, fold=0.3)(c), inflate=0.1)
    m.cube('cap', (-3.5, -6.6, -5.5), (7, 1, 2), lambda c: cloth(scarf, 2.2, fold=0.3)(c))
    m.bone('bow', 'head', (0, 0, 0))   # or a bow of old ribbon
    m.cube('bow', (-2, -9, -0.5), (4, 2, 1), lambda c: cloth(scarf, 3.6, fold=0.3)(c))
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_arm', 'body', (3 * sx, -4.5, 0))
        m.cube(f'{side}_arm', (-0.5, 0, -0.5), (1, 5, 1), lambda c: shaded(BONE, 4.0, grain(0.2, 2))(c))
        m.bone(f'{side}_leg', 'root', (1.3 * sx, -6, 0))
        m.cube(f'{side}_leg', (-0.5, 0, -0.5), (1, 6, 1), lambda c: shaded(BONE, 4.0, grain(0.2, 2))(c))
    m.paint()
    return m


# ------------------------------------------------------------------ the Lantern Wisp
def wisp_flame(ctx):
    flame(ctx)
    if ctx.face == 'front':   # two little eyes in the fire
        for x, y in ((1, 1), (ctx.w - 2, 1)):
            if 0 <= x < ctx.w and y < ctx.h:
                ctx.set(x, y, (250, 245, 255, 255))
                ctx.emit(x, y, (255, 255, 255, 255))


def build_lantern_wisp():
    m = Model('lantern_wisp', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('lantern', 'root', (0, -10, 0))
    m.cube('lantern', (-3, -6, -3), (6, 6, 6), cage)
    m.cube('lantern', (-3.5, -7, -3.5), (7, 1, 7), lambda c: steel(IRON, 3.4, rivets=False, scratches=0)(c))
    m.cube('lantern', (-3.5, 0, -3.5), (7, 1, 7), lambda c: steel(IRON, 3.0, rivets=False, scratches=0)(c))
    m.cube('lantern', (-1, -9, -0.5), (2, 2, 1), lambda c: steel(IRON, 3.6, rivets=False, scratches=0)(c))
    m.bone('flame', 'lantern', (0, -3, 0))
    m.cube('flame', (-2, -2.5, -2), (4, 5, 4), wisp_flame)
    m.paint()
    return m
