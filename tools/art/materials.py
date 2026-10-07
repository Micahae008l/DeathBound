"""Reusable face painters. A painter is fn(ctx) that fills ctx.px (and optionally ctx.glow)."""
import math
from core import CLEAR, SOUL


def phys(ctx, x):
    """Horizontal coordinate that is identical for opposite faces (keeps thin planes consistent)."""
    w = ctx.w
    if ctx.face == 'back':
        return w - 1 - x
    if ctx.face == 'right':
        return w - 1 - x
    return x


def by_face(default=None, **faces):
    def paint(ctx):
        fn = faces.get(ctx.face, default)
        if fn is not None:
            fn(ctx)
    return paint


def chain(*painters):
    def paint(ctx):
        for p in painters:
            p(ctx)
    return paint


def solid(col):
    return lambda ctx: ctx.fill(lambda x, y: col)


def shaded(ramp, base, *mods, light=1.0, dither=0.35):
    """Generic level painter: base level + face light + modulators, quantised onto the ramp."""
    def paint(ctx):
        for y in range(ctx.h):
            for x in range(ctx.w):
                L = base + ctx.light * light
                for m in mods:
                    L += m(ctx, x, y)
                ctx.set(x, y, ramp.at(L, x, y, dither))
    return paint


# ---------------------------------------------------------------- modulators: fn(ctx, x, y) -> level delta
def grain(amount=0.5, size=2, s=0):
    return lambda ctx, x, y: ctx.noise.cluster(x, y, size, s) * amount


def speckle(amount=0.35, s=1):
    return lambda ctx, x, y: ctx.noise.white(x, y, s) * amount


def smooth(amount=0.6, scale=3.0, s=2):
    return lambda ctx, x, y: ctx.noise.smooth(x, y, scale, s) * amount


def bevel(top=0.6, bottom=-0.55, sides=-0.2):
    """Rim light on the upper edge of side faces, occlusion on the lower edge."""
    def m(ctx, x, y):
        if not ctx.side or ctx.h < 2:
            return 0.0
        d = 0.0
        if y == 0:
            d += top
        if y == ctx.h - 1:
            d += bottom
        if ctx.w > 2 and (x == 0 or x == ctx.w - 1):
            d += sides
        return d
    return m


def vgrad(top=0.3, bottom=-0.6):
    """Vertical gradient down a side face (light from above, occlusion toward the ground)."""
    def m(ctx, x, y):
        if not ctx.side or ctx.h < 2:
            return 0.0
        t = y / (ctx.h - 1)
        return top + (bottom - top) * t
    return m


def ao_top(rows=2, amount=-0.8):
    """Shadow just under a joint (e.g. under a shoulder pad or skull)."""
    def m(ctx, x, y):
        if ctx.side and y < rows:
            return amount * (1 - y / rows)
        return 0.0
    return m


def folds(amount=0.9, period=3.0, s=3):
    """Vertical cloth folds: alternating ridges/valleys that drift a little down the cloth."""
    def m(ctx, x, y):
        if not ctx.side:
            return 0.0
        px = phys(ctx, x)
        wob = ctx.noise.smooth(px * 3, y, 5.0, s) * 0.8
        v = math.sin((px + wob + ctx.seed % 7) * 2 * math.pi / period)
        return v * amount * (0.55 + 0.45 * y / max(1, ctx.h - 1))
    return m


def hstripes(amount=0.7, period=3, offset=0):
    """Horizontal bands (ribs, bracers, wraps)."""
    return lambda ctx, x, y: (amount if (y + offset) % period == 0 else 0.0) if ctx.side else 0.0


# ---------------------------------------------------------------- post passes (operate on painted px)
def tatter(depth=3, density=0.6, s=11, faces=('right', 'front', 'left', 'back')):
    """Ragged transparent bottom edge."""
    def paint(ctx):
        if ctx.face not in faces:
            return
        for x in range(ctx.w):
            px = phys(ctx, x)
            n = ctx.noise._h(px, 0, s + (0 if ctx.face in ('front', 'back') else 50))
            n2 = ctx.noise._h(px // 2, 1, s + 7 + (0 if ctx.face in ('front', 'back') else 50))
            cut = int(round(depth * (0.35 * n + 0.65 * n2))) if n2 < density else 0
            for y in range(ctx.h - cut, ctx.h):
                ctx.set(x, y, CLEAR)
            # a darker frayed lip above the cut
            yy = ctx.h - cut - 1
            if 0 <= yy < ctx.h and ctx.px[yy][x] != CLEAR:
                r, g, b, a = ctx.px[yy][x]
                ctx.set(x, yy, (int(r * 0.7), int(g * 0.7), int(b * 0.75), a))
    return paint


def holes(threshold=0.72, size=2, s=21, rows=None, faces=('front', 'back', 'right', 'left')):
    """Ragged holes through cloth (overlay layers only)."""
    def paint(ctx):
        if ctx.face not in faces:
            return
        for y in range(ctx.h):
            if rows and not (rows[0] <= y < rows[1]):
                continue
            for x in range(ctx.w):
                px = phys(ctx, x)
                v = ctx.noise._h(px // size, y // size, s) * 0.7 + ctx.noise._h(px, y, s + 1) * 0.3
                if v > threshold:
                    ctx.set(x, y, CLEAR)
    return paint


def trim(ramp, rows, level=3.0, edge='bottom', faces=('right', 'front', 'left', 'back')):
    """A band of trim color on the bottom (or top) rows of side faces; respects existing transparency."""
    def paint(ctx):
        if ctx.face not in faces:
            return
        for y in range(ctx.h):
            k = (ctx.h - 1 - y) if edge == 'bottom' else y
            if k >= rows:
                continue
            for x in range(ctx.w):
                if ctx.px[y][x] == CLEAR:
                    continue
                L = level + ctx.light * 0.7 + ctx.noise.cluster(x, y, 2, 31) * 0.4 + (0.5 if k == rows - 1 else 0)
                ctx.set(x, y, ramp.at(L, x, y))
    return paint


def cracks(ramp, count=2, length=5, level=0.6, s=41, faces=None):
    """Thin dark crack lines wandering downward."""
    def paint(ctx):
        if faces and ctx.face not in faces:
            return
        n = ctx.noise
        for i in range(count):
            x = int(n._h(i, 0, s) * ctx.w)
            y = int(n._h(i, 1, s) * max(1, ctx.h // 2))
            for k in range(length):
                if 0 <= x < ctx.w and 0 <= y < ctx.h and ctx.px[y][x] != CLEAR:
                    ctx.set(x, y, ramp.at(level, x, y, 0))
                    # highlight lip beside the crack sells the depth
                    if x + 1 < ctx.w and ctx.px[y][x + 1] != CLEAR:
                        r, g, b, a = ctx.px[y][x + 1]
                        ctx.set(x + 1, y, (min(255, r + 14), min(255, g + 13), min(255, b + 12), a))
                y += 1
                x += int(round(n.white(i, k, s + 2)))
    return paint


def soul_glow_spots(count=2, s=51, faces=None, ramp=SOUL):
    """Emissive soul-energy specks."""
    def paint(ctx):
        if faces and ctx.face not in faces:
            return
        for i in range(count):
            x = int(ctx.noise._h(i, 3, s) * ctx.w)
            y = int(ctx.noise._h(i, 4, s) * ctx.h)
            if 0 <= x < ctx.w and 0 <= y < ctx.h and ctx.px[y][x] != CLEAR:
                ctx.emit(x, y, ramp.cols[3])
    return paint


def outline_edges(ramp, level=0.5, faces=('front', 'back', 'right', 'left')):
    """Darken pixels that border transparency (gives cutouts a clean dark rim)."""
    def paint(ctx):
        if ctx.face not in faces:
            return
        src = [row[:] for row in ctx.px]
        for y in range(ctx.h):
            for x in range(ctx.w):
                if src[y][x] == CLEAR:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    xx, yy = x + dx, y + dy
                    if 0 <= xx < ctx.w and 0 <= yy < ctx.h and src[yy][xx] == CLEAR:
                        r, g, b, a = src[y][x]
                        ctx.set(x, y, (int(r * 0.72), int(g * 0.72), int(b * 0.78), a))
                        break
    return paint


# ---------------------------------------------------------------- composite materials
def stain(ramp, amount=1.0, start=0.55, s=7):
    """Soul-stained fold valleys toward the hem: recolours dark pixels in the lower part of a face."""
    def paint(ctx):
        if not ctx.side:
            return
        for y in range(int(ctx.h * start), ctx.h):
            t = (y - ctx.h * start) / max(1, ctx.h * (1 - start))
            for x in range(ctx.w):
                c = ctx.px[y][x]
                if c == CLEAR:
                    continue
                v = math.sin((phys(ctx, x) + ctx.seed % 7) * 2 * math.pi / 3.0) + ctx.noise.smooth(phys(ctx, x), y, 2.5, s) * 0.8
                if v < -0.35 - (1 - t) * 0.9 * amount:
                    ctx.set(x, y, ramp.at(1.2 + t * 1.6 + ctx.light, x, y, 0.2))
    return paint


def cloth(ramp, base=2.6, fold=0.8, period=3.0, tat=0, hole=None, trim_ramp=None, trim_rows=0, light=1.0, stain_ramp=None):
    p = [shaded(ramp, base, folds(fold, period), grain(0.18, 2), smooth(0.25, 4.0), vgrad(0.4, -0.6), bevel(0.5, -0.3, 0),
                light=light, dither=0.2)]
    if stain_ramp is not None:
        p.append(stain(stain_ramp))
    if trim_ramp is not None and trim_rows:
        p.append(trim(trim_ramp, trim_rows))
    if hole:
        p.append(holes(hole))
    if tat:
        p.append(tatter(tat))
    return chain(*p)


def bone(ramp, base=4.0, light=1.0, crack=0, knobs=True):
    mods = [grain(0.3, 2), smooth(0.35, 3.0), bevel(0.55, -0.6, -0.35)]
    if knobs:
        mods.append(lambda ctx, x, y: (0.45 if y == 1 else -0.35 if y == 2 else 0.0) if ctx.side and ctx.h > 6 else 0.0)
    p = [shaded(ramp, base, *mods, light=light)]
    if crack:
        p.append(cracks(ramp, crack, 4, base - 2.6))
    return chain(*p)


def steel(ramp, base=3.6, rivets=True, scratches=2, light=1.0):
    def paint(ctx):
        shaded(ramp, base, grain(0.25, 2), smooth(0.4, 4.0), bevel(1.2, -1.0, -0.4), light=light)(ctx)
        n = ctx.noise
        if ctx.side and scratches:
            for i in range(scratches):
                x = int(n._h(i, 7, 61) * ctx.w)
                y = int(n._h(i, 8, 61) * ctx.h)
                for k in range(3):
                    if 0 <= x + k < ctx.w and 0 <= y + k < ctx.h and ctx.px[y + k][x + k] != CLEAR:
                        ctx.set(x + k, y + k, ramp.at(base + 1.6, x, y, 0))
        if rivets and ctx.side and ctx.w >= 4 and ctx.h >= 4:
            for rx in (1, ctx.w - 2):
                ctx.set(rx, 1, ramp.at(base + 2.4, 0, 0, 0))
                ctx.set(rx, 2, ramp.at(base - 1.6, 0, 0, 0))
    return paint
