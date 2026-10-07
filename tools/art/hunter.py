"""The Hollow Hunter: the first living soul ever to come down, who refused the throne, and has been hunted ever since.
A tall shape in a deep hood and a cloak gone to rags, an antler crown through the cloth, nothing in the hood but dark and
one lit eye. No quiver: every arrow he shoots is one somebody put in his back."""
from core import Model, Ramp, BONE, DARK_BONE, SOUL, CLEAR
from materials import shaded, cloth, grain, smooth, bevel, vgrad, tatter, outline_edges, holes, hstripes

CLOAK = Ramp('#08070b', '#0f0e14', '#17151d', '#201d27', '#2a2632', '#35303e', '#413b4b')
FEATHER = Ramp('#060509', '#0d0b12', '#16131c', '#201c28', '#2c2735', '#3a3445', '#4b4458')
WRAP = Ramp('#0e0c10', '#18151a', '#231f25', '#2f2a30', '#3c363b', '#4a4348')
HIDE = Ramp('#0c0a0b', '#151113', '#1f191b', '#2a2224', '#362c2d')
VOID = Ramp('#020103', '#050408', '#09070d', '#0e0b13')
ANTLER = Ramp('#120f0d', '#1d1814', '#2a231c', '#3a3026', '#4d4032', '#625240', '#78664f')
SINEW = Ramp('#2a2420', '#3d352d', '#54493c', '#6e604d', '#8a7a62')
GLOW_HI, GLOW_LO = (238, 222, 255, 255), (150, 84, 222, 255)
FLETCH = Ramp('#3b1466', '#561e8f', '#7330b8', '#9150d4', '#b27ce8', '#d5b2f6')
SHAFT = Ramp('#120d10', '#1d1519', '#2a2024', '#382b2f')
IRON = Ramp('#0f0e12', '#1d1b22', '#2e2b34', '#46424d', '#625d69')


def cloak(tat=0, hole=None, base=2.5):
    def paint(ctx):
        cloth(CLOAK, base, fold=1.1, period=2.6, stain_ramp=FEATHER)(ctx)
        if hole:
            holes(hole)(ctx)
        if tat:
            tatter(tat, 0.85)(ctx)
            outline_edges(CLOAK)(ctx)
    return paint


def open_ends(painter, faces=('top', 'bottom')):
    def paint(ctx):
        painter(ctx)
        if ctx.face in faces:
            ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)
    return paint


def hood(ctx):
    """Deep, ragged, and empty-looking: the opening shows only the dark inside."""
    cloak(base=2.7)(ctx)
    if ctx.face == 'front':
        for y in range(ctx.h):
            for x in range(ctx.w):
                if 2 <= x < ctx.w - 2 and 2 <= y:
                    ctx.set(x, y, CLEAR)
                elif 1 <= x < ctx.w - 1 and 1 <= y and (x in (1, ctx.w - 2) or y == 1):
                    ctx.set(x, y, CLOAK.at(0.8, x, y, 0))   # the rim curls in, darker
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y < ctx.h - 1 else None)
    tatter(2, 0.7, faces=('right', 'left', 'back'))(ctx)


def void_head(ctx):
    """What's under the hood. Black, a ridge of brow, and one eye still lit."""
    ctx.fill(lambda x, y: VOID.at(1.2 + ctx.noise.white(x, y, 3) * 0.8, x, y))
    if ctx.face == 'front':
        L = {'v': VOID.cols[1], 'V': VOID.cols[0], 'b': DARK_BONE.cols[2], 'B': DARK_BONE.cols[3], 'E': GLOW_HI, 'e': GLOW_LO,
             'k': DARK_BONE.cols[1]}
        ctx.stamp(["vvvvvv",
                   "vvvvvv",
                   "vBbvbv",
                   "veEVkv",
                   "vVeVVv",
                   "vvbbvv",
                   "vvvvvv"], L, glow_legend={'E': SOUL.cols[6], 'e': SOUL.cols[3]})


def jaw(ctx):
    ctx.fill(lambda x, y: VOID.cols[1])
    if ctx.face == 'front':
        ctx.stamp(["tgtg"], {'t': DARK_BONE.cols[4], 'g': VOID.cols[0]})


def mantle(ctx):
    """Crow feathers over the shoulders: layered tips, a ragged hem."""
    for y in range(ctx.h):
        for x in range(ctx.w):
            fx = (x + y % 2) % 3
            lvl = 2.6 + (0.9 if fx == 0 else -0.5 if fx == 2 else 0.2) - y * 0.15 + ctx.noise.white(x, y, 9) * 0.6
            ctx.set(x, y, FEATHER.at(lvl, x, y))
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 3 <= x < ctx.w - 3 and 1 <= y < ctx.h - 1 else None)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)
    tatter(2, 0.9, s=17)(ctx)


def jerkin(ctx):
    shaded(HIDE, 2.2, grain(0.3, 2), vgrad(0.3, -0.5))(ctx)
    if ctx.face == 'front':   # a strap across the chest, a bone toggle
        for y in range(ctx.h):
            x = ctx.w - 1 - (y * ctx.w) // max(1, ctx.h)
            for dx in (0, 1):
                if 0 <= x - dx < ctx.w:
                    ctx.set(x - dx, y, WRAP.at(1.6 + dx * 0.6, x, y))
        ctx.set(ctx.w // 2, ctx.h // 2, DARK_BONE.cols[5])


def limb(ctx):
    shaded(DARK_BONE, 4.2, smooth(0.25, 4.0), bevel(0.6, -0.7, -0.4), vgrad(0.25, -0.5))(ctx)


def wrap(ctx):
    """Dark strips wound round the bone, fraying."""
    shaded(WRAP, 2.6, hstripes(1.1, 2), grain(0.3, 2))(ctx)
    tatter(1, 0.4)(ctx)


def claw(ctx):
    shaded(BONE, 3.4, grain(0.25, 2))(ctx)
    if ctx.side:   # long fingers, dark between
        for y in range(1, ctx.h):
            ctx.set(ctx.w // 2, y, DARK_BONE.cols[1])


def antler(ctx):
    shaded(ANTLER, 4.2, grain(0.25, 2), bevel(0.8, -0.6, -0.3))(ctx)


def bow_limb(ctx):
    shaded(DARK_BONE, 5.0, grain(0.25, 2), bevel(0.6, -0.5, -0.4), lambda c, x, y: 0.6 if y % 5 == 0 else 0.0)(ctx)


def grip(ctx):
    shaded(WRAP, 2.2, hstripes(0.9, 1))(ctx)


def string(ctx):
    ctx.fill(lambda x, y: SINEW.at(2.6 + (y % 2) * 0.5, x, y))


# ---------------------------------------------------------------- arrows: thin dark shafts, crossed glowing vanes, a nock
VANE = ["fFf",
        "FFF",
        "FFF",
        "fFf",
        ".F.",
        ".f."]


def vane(ctx):
    cut = ctx.seed % 4   # every feather torn a little differently
    for y, row in enumerate(VANE):
        for x, ch in enumerate(row):
            if ch == '.' or (y == 2 and x == (0 if cut < 2 else 2) and cut % 2 == 0):
                continue
            lvl = (3.4 if ch == 'F' else 2.2) + (0.5 if x == 1 else -0.5) - y * 0.2
            c = FLETCH.at(lvl, x, y, 0.2)
            ctx.set(x, y, c)
            ctx.emit(x, y, c)


def shaft(ctx):
    ctx.fill(lambda x, y: SHAFT.at(1.8 + (0.8 if ctx.face in ('top', 'left') else 0), x, y))


def nock(ctx):
    ctx.fill(lambda x, y: DARK_BONE.cols[5])


def head(ctx):
    for y, row in enumerate(["HHH", "HHH", ".H."]):
        for x, ch in enumerate(row):
            if ch == 'H':
                ctx.set(x, y, IRON.at(3.2 - y * 0.6 + (0.6 if x == 1 else 0), x, y))


def arrow(m, name, parent, pos, rot, back, front=2, tip=False):
    """An arrow along its bone's y: fletching `back` pixels out on -y, `front` pixels of shaft on +y (buried, or ending in a head)."""
    m.bone(name, parent, pos, rot)
    m.cube(name, (-0.5, -back, -0.5), (1, back + front, 1), shaft, inflate=-0.3)
    m.cube(name, (-1.5, -back + 0.3, 0), (3, 6, 0), vane)
    m.cube(name, (0, -back + 0.3, -1.5), (0, 6, 3), vane)
    m.cube(name, (-0.5, -back - 0.4, -0.5), (1, 1, 1), nock, inflate=-0.25)
    if tip:
        m.cube(name, (-1.5, front - 0.5, 0), (3, 3, 0), head)
        m.cube(name, (0, front - 0.5, -1.5), (0, 3, 3), head)


def build():
    m = Model('hollow_hunter', 128, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('hips', 'root', (0, -20, 0))
    m.cube('hips', (-3, -2, -2), (6, 3, 4), wrap)
    for side, sx in (('right', -1), ('left', 1)):
        thigh, shin = f'{side}_thigh', f'{side}_shin'
        m.bone(thigh, 'hips', (2 * sx, 0, 0))
        m.cube(thigh, (-1, 0, -1), (2, 10, 2), limb)
        m.cube(thigh, (-3, -1, -3), (6, 13, 6), open_ends(cloak(tat=4)), inflate=0.15)   # the coat's long skirts
        m.bone(shin, thigh, (0, 10, 0))
        m.cube(shin, (-1, 0, -1), (2, 10, 2), limb)
        m.cube(shin, (-1.5, 1, -1.5), (3, 8, 3), wrap)
        m.cube(shin, (-1.5, 9, -3), (3, 1, 4), limb)
    m.bone('spine', 'hips', (0, -2, 0))
    m.cube('spine', (-1, -6, -1), (2, 6, 2), limb)
    m.cube('spine', (-3.5, -6, -2.5), (7, 7, 5), jerkin)
    m.bone('chest', 'spine', (0, -6, 0), (14, 0, 0))
    m.cube('chest', (-4, -9, -2.5), (8, 9, 5), jerkin)
    m.cube('chest', (-5.5, -10.5, -3.5), (11, 5, 7), mantle, inflate=0.2)
    m.bone('cape', 'chest', (0, -9.5, 3.5), (6, 0, 0))
    m.cube('cape', (-5.5, 0, 0), (11, 32, 0), cloak(tat=6, hole=0.8, base=2.2))
    m.bone('head', 'chest', (0, -9.5, -0.5), (-12, 0, 0))
    m.cube('head', (-3, -7.5, -3), (6, 7, 6), void_head)
    m.bone('jaw', 'head', (0, -1, -1))
    m.cube('jaw', (-2, 0, -2), (4, 1, 3), jaw)
    m.cube('head', (-4, -9, -5), (8, 10, 8), hood)
    m.cube('head', (-3, -10.5, -4), (6, 2, 6), cloak(base=2.9))   # the crown of the hood, rounded off
    m.bone('hood_tail', 'head', (0, -9.5, 2.5), (-50, 0, 0))
    m.cube('hood_tail', (-1.5, -1, 0), (3, 2, 6), cloak(tat=2))
    for side, sx in (('right', -1), ('left', 1)):   # the antler crown, through the hood
        a = f'{side}_antler'
        m.bone(a, 'head', (2.2 * sx, -10, 0), (-10, 0, 26 * sx))
        m.cube(a, (-0.5, -8, -0.5), (1, 8, 1), antler)
        m.cube(a, (-0.5 + 1.5 * sx, -5, -0.5), (1, 1, 1), antler)
        m.cube(a, (-0.5 + 2.5 * sx, -7, -0.5), (1, 3, 1), antler)
        m.cube(a, (-0.5, -6, -2), (1, 1, 2), antler)
        m.cube(a, (-0.5, -3, 0.5), (1, 1, 2), antler)
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        m.bone(arm, 'chest', (5 * sx, -8.5, 0))
        m.cube(arm, (-1, -1, -1), (2, 10, 2), limb)
        m.cube(arm, (-2, -1.5, -2), (4, 8, 4), open_ends(cloak(tat=3)))
        m.bone(fore, arm, (0, 9, 0))
        m.cube(fore, (-1, 0, -1), (2, 9, 2), limb)
        m.cube(fore, (-1.5, 0.5, -1.5), (3, 7, 3), wrap)
        m.cube(fore, (-1, 9, -1), (2, 4, 2), claw)
    # the bow, held low in the left hand
    m.bone('bow', 'left_forearm', (0, 10.5, 0))
    m.cube('bow', (-0.5, -2, -0.5), (1, 4, 1), grip)
    m.bone('bow_upper', 'bow', (0, -2, 0), (-12, 0, 0))
    m.cube('bow_upper', (-0.5, -11, -0.5), (1, 11, 1), bow_limb)
    m.cube('bow_upper', (-0.5, -13, -1.5), (1, 2, 1), bow_limb)
    m.bone('bow_lower', 'bow', (0, 2, 0), (12, 0, 0))
    m.cube('bow_lower', (-0.5, 0, -0.5), (1, 11, 1), bow_limb)
    m.cube('bow_lower', (-0.5, 11, -1.5), (1, 2, 1), bow_limb)
    m.cube('bow', (-0.5, -12, 2.2), (1, 24, 0), string)
    # every arrow he has is one somebody put in him
    for name, parent, pos, rot, back in (('stuck_0', 'chest', (1.5, -6, 2.8), (-58, 0, 16), 10),
                                         ('stuck_1', 'chest', (2.8, -3, 2.8), (-74, 14, 12), 9),
                                         ('stuck_2', 'chest', (-0.5, -8, 2.8), (-46, -6, -8), 11),
                                         ('stuck_3', 'chest', (0.6, -1.5, 2.8), (-80, 0, 4), 8),
                                         ('pull_arrow', 'chest', (-3, -8, 2.8), (-40, 0, -22), 11),
                                         ('stuck_4', 'left_arm', (0.5, 1.5, 1.6), (-68, 0, 28), 8)):
        arrow(m, name, parent, pos, rot, back)
    arrow(m, 'nocked_arrow', 'bow', (0.9, 0, 0), (-90, 0, 0), 5, 9, tip=True)    # on the string, head forward
    arrow(m, 'hand_arrow', 'right_forearm', (0, 11, -1.3), (0, 0, 0), 5, 7, tip=True)   # just pulled from his back
    m.paint()
    return m


PREVIEW_POSES = {'rest': {}}
