"""Lost Soul (procession ghost) and Soul Anchor (phase-1 binding cage)."""
import math
from core import Model, Ramp, BONE, DARK_BONE, STEEL, SOUL, CLEAR
from materials import by_face, shaded, grain, smooth, bevel, vgrad, phys, tatter, steel

GHOST = Ramp('#1a1233', '#2b1f52', '#3f2f75', '#56439a', '#7360bd', '#9a88dc', '#c6b9f4')


def ghostly(base=4.0, alpha=150, fade_bottom=False):
    def paint(ctx):
        for y in range(ctx.h):
            for x in range(ctx.w):
                L = base + ctx.light * 0.8 + ctx.noise.smooth(phys(ctx, x), y, 3.0, 2) * 0.6
                if ctx.side:
                    L += math.sin((phys(ctx, x) + ctx.seed % 5) * 2.1) * 0.35
                r, g, b, _ = GHOST.at(L, x, y, 0.3)
                # wispy: streaks of thicker ectoplasm drifting down through thin haze
                wisp = 0.55 + 0.45 * ctx.noise.smooth(phys(ctx, x) * 2.5, y * 0.6, 2.0, 7)
                a = alpha * wisp
                if fade_bottom and ctx.side:
                    a *= 1 - 0.75 * y / max(1, ctx.h - 1)
                ctx.set(x, y, (r, g, b, max(8, int(a))))
    return paint


def ghost_face(ctx):
    ghostly(4.2, 125)(ctx)
    hollow = (40, 22, 70, 230)
    for x, y in ((1, 3), (2, 3), (5, 3), (6, 3), (1, 4), (2, 4), (5, 4), (6, 4), (3, 6), (4, 6), (3, 5), (4, 5)):
        ctx.set(x, y, hollow)
    for x, y in ((2, 4), (5, 4)):
        ctx.emit(x, y, (225, 205, 255, 255))
    for x, y in ((1, 4), (6, 4), (2, 3), (5, 3)):
        ctx.emit(x, y, (150, 110, 240, 150))


def soul_core(ctx):
    ghostly(3.6, 85)(ctx)
    if ctx.face in ('front', 'back'):  # the faint ember of what they were
        for x, y in ((3, 3), (4, 3), (3, 4), (4, 4)):
            ctx.emit(x, y, (200, 170, 255, 210))
        for x, y in ((3, 2), (4, 2), (2, 3), (5, 3), (2, 4), (5, 4), (3, 5), (4, 5)):
            ctx.emit(x, y, (130, 90, 230, 120))


def hood_ghost(ctx):
    ghostly(3.2, 80)(ctx)
    if ctx.face == 'front':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y >= 2 else None)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)


GHOSTS = {
    'grey': None,
    'pale': Ramp('#141c2c', '#223048', '#34486a', '#4d668f', '#6f8ab4', '#9ab2d6', '#c9d9f2'),
    'dusk': Ramp('#1e1020', '#321a36', '#4a2750', '#65366c', '#84498a', '#a768a9', '#cfa0cc'),
    'drowned': Ramp('#08161a', '#0f2a30', '#18414a', '#245c66', '#367a84', '#5aa0a6', '#9fd0cf'),   # pulled out of the river
    'ashen': Ramp('#140f0e', '#241a18', '#372824', '#4c3832', '#654c43', '#86685c', '#b49a8c'),    # what burned
    'veiled': Ramp('#050407', '#0b0a10', '#13111b', '#1d1a28', '#2a2638', '#3b354d', '#5a5470'),   # almost nothing left
}


def build_soul(tint='grey'):
    global GHOST
    saved = GHOST
    GHOST = GHOSTS.get(tint) or saved
    try:
        return _build_soul()
    finally:
        GHOST = saved


def _build_soul():
    m = Model('lost_soul', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -16, 0))
    m.cube('body', (-4, -12, -2), (8, 12, 4), soul_core)
    m.bone('head', 'body', (0, -12, 0))
    m.cube('head', (-4, -8, -4), (8, 8, 8), by_face(ghostly(4.0, 105), front=ghost_face))
    m.cube('head', (-4.5, -8.5, -4.5), (9, 9, 9), hood_ghost, inflate=0.1)
    m.bone('right_arm', 'body', (-5, -11, 0), (-20, 0, 0))
    m.cube('right_arm', (-1.5, -1, -1.5), (3, 11, 3), ghostly(3.4, 80, True))
    m.bone('left_arm', 'body', (5, -11, 0), (-20, 0, 0))
    m.cube('left_arm', (-1.5, -1, -1.5), (3, 11, 3), ghostly(3.4, 80, True))
    m.bone('tail', 'body', (0, 0, 0))
    m.cube('tail', (-3, 0, -2), (6, 6, 4), ghostly(3.2, 70, True))
    m.bone('tail2', 'tail', (0, 6, 0))
    m.cube('tail2', (-2, 0, -1.5), (4, 5, 3), ghostly(2.8, 50, True))
    m.bone('tail3', 'tail2', (0, 5, 0))
    m.cube('tail3', (-1, 0, -1), (2, 4, 2), ghostly(2.4, 32, True))
    m.paint()
    return m


# ------------------------------------------------------------------ Soul Anchor
def core(ctx):
    cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
    for y in range(ctx.h):
        for x in range(ctx.w):
            d = math.hypot(x - cx, y - cy) / max(cx, 0.5)
            col = SOUL.at(5.5 - d * 2.8 + ctx.noise.white(x, y, 2) * 0.4, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, col)


def bar(ctx):
    shaded(BONE, 3.4, grain(0.3, 2), vgrad(0.4, -0.4), bevel(0.4, -0.4, 0))(ctx)
    if ctx.side:
        for y in range(1, ctx.h, 4):
            ctx.set(0, y, BONE.cols[5])


def cap(ctx):
    steel(STEEL, 3.2, rivets=True, scratches=1)(ctx)
    if ctx.face == 'top':
        c = ctx.w // 2
        for (x, y) in ((c - 1, c - 1), (c, c - 1), (c - 1, c), (c, c)):
            ctx.set(x, y, (60, 20, 110, 255))
            ctx.emit(x, y, SOUL.cols[3])


def chain_plane(ctx):
    if ctx.face not in ('front', 'back', 'right', 'left'):
        return
    for y in range(ctx.h):
        link = y % 4
        for x in range(ctx.w):
            if link in (0, 3) and x in (0, 1, 2):
                ctx.set(x, y, STEEL.at(5.0 if link == 0 else 3.4, x, y, 0))
            elif link in (1, 2) and x in (0, 2):
                ctx.set(x, y, STEEL.at(4.4, x, y, 0))


def spike(ctx):
    shaded(DARK_BONE, 4.6, vgrad(0.8, -0.8), grain(0.2))(ctx)


def build_anchor():
    m = Model('soul_anchor', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('cage', 'root', (0, -14, 0))
    m.cube('cage', (-3, -3, -3), (6, 6, 6), core)
    m.cube('cage', (-6, -9, -6), (12, 2, 12), cap)
    m.cube('cage', (-6, 7, -6), (12, 2, 12), cap)
    for i, (x, z) in enumerate(((-5, -5), (4, -5), (-5, 4), (4, 4))):
        m.cube('cage', (x, -7, z), (1, 14, 1), bar)
    for x, z in ((-1, -7), (-1, 6), (-7, -1), (6, -1)):
        m.cube('cage', (x, -11, z), (2, 2, 1) if abs(z) > 5 else (1, 2, 2), spike)
    m.cube('cage', (-1, -12, -1), (2, 3, 2), spike)
    m.bone('chains', 'cage', (0, 9, 0))
    m.cube('chains', (-4, 0, -4), (3, 8, 0), chain_plane)
    m.cube('chains', (1, 0, 3), (3, 6, 0), chain_plane)
    m.cube('chains', (4, 0, -2), (0, 7, 3), chain_plane)
    m.paint()
    return m


def build(v=None):
    return build_anchor() if v == 'anchor' else build_soul()


PREVIEW_POSES = {'rest': {}}
