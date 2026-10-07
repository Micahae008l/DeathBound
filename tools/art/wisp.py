"""Soul Wisp — a small flying skull wreathed in soul fire."""
import math
import os
from core import Model, Ramp, DARK_BONE, BONE, SOUL, CLEAR
from materials import by_face, shaded, grain, smooth, bevel, vgrad, phys


_FIRE = None
_JAR = os.path.expanduser('~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar')


def _fire_frames():
    """Minecraft's soul fire, turned purple, its base rounded off (the same frames the bolts use: see build.py)."""
    global _FIRE
    if _FIRE is None:
        import colorsys
        import sprites as sp
        f = sp.purple_vanilla(_JAR, 'block/soul_fire_0').astype(int)
        for k in range(32):
            for y in range(16):
                for x in range(16):
                    p = f[k * 16 + y, x]
                    if p[3]:
                        h, s_, v = colorsys.rgb_to_hsv(*(p[:3] / 255.0))
                        r, g, b = colorsys.hsv_to_rgb(0.77, max(s_, 0.42), v * 0.96)
                        f[k * 16 + y, x, :3] = (int(r * 255), int(g * 255), int(b * 255))
            f[k * 16 + 15, :, 3] = 0
            for y, keep in ((14, 6), (13, 10), (12, 12), (11, 14)):
                cut = (16 - keep) // 2
                f[k * 16 + y, :cut, 3] = 0
                f[k * 16 + y, 16 - cut:, 3] = 0
        _FIRE = f
    return _FIRE


def flame_tex(ctx, frame=0, shade=1.0):
    """Glow-only fire sheet: one frame of Minecraft's fire, purple, pixel for pixel (16x16 sheets map 1:1; smaller ones take
    every other pixel). shade darkens it for the umbral wisp, lightens it for the pale one."""
    if ctx.face not in ('front', 'back', 'right', 'left'):
        return
    fire = _fire_frames()
    W, H = ctx.w, ctx.h
    for y in range(H):
        for x in range(W):
            px = phys(ctx, x)
            p = fire[frame * 16 + y * 16 // H, px * 16 // W]
            if p[3]:
                r, g, b = (min(255, int(c * shade)) if shade <= 1 else int(c + (255 - c) * (shade - 1)) for c in p[:3])
                ctx.emit(x, y, (r, g, b, 235))


def skull_front(ctx):
    L = {'c': DARK_BONE.cols[2], 'H': DARK_BONE.cols[6], 'h': DARK_BONE.cols[5], 'b': DARK_BONE.cols[4], 'm': DARK_BONE.cols[3],
         's': DARK_BONE.cols[2], 'S': DARK_BONE.cols[5], 'k': DARK_BONE.cols[1], 'K': DARK_BONE.cols[0], 'E': (170, 110, 240, 255),
         'n': DARK_BONE.cols[0], 'T': BONE.cols[4], 'g': DARK_BONE.cols[0], 'f': (60, 20, 110, 255)}
    G = {'E': SOUL.cols[6], 'K': SOUL.cols[0], 'f': SOUL.cols[3]}
    ctx.stamp(["chHHHfhc",
               "bHhbbfHb",
               "hkKbbKkh",
               "bKEmmEKb",
               "mSsnnsSm",
               "sTgTTgTs"], L, glow_legend=G)


def skull_shell(ctx):
    shaded(DARK_BONE, 4.0, grain(0.4, 2), smooth(0.35), bevel(0.9, -0.8, -0.2), vgrad(0.3, -0.5))(ctx)
    # glowing fissures
    if ctx.face in ('top', 'left', 'back'):
        x, y = int(ctx.noise._h(0, 0, 3) * (ctx.w - 2)) + 1, 0
        for k in range(ctx.h):
            if 0 <= x < ctx.w and 0 <= y < ctx.h:
                ctx.set(x, y, (55, 18, 100, 255))
                ctx.emit(x, y, SOUL.cols[3] if k % 2 == 0 else SOUL.cols[2])
            y += 1
            x += int(round(ctx.noise.white(k, 1, 4)))


def jaw_front(ctx):
    L = {'T': BONE.cols[4], 'g': DARK_BONE.cols[0], 's': DARK_BONE.cols[2], 'b': DARK_BONE.cols[4], 'c': DARK_BONE.cols[1]}
    ctx.stamp(["sTgTgTs", "csbbbsc"], L)


def mote(ctx):
    ctx.fill(lambda x, y: CLEAR)
    ctx.emit(0, 0, SOUL.cols[5])


FLAMES = {
    'violet': None,
    'pale': Ramp('#1c1028', '#3a2258', '#6a4498', '#9c74cc', '#c6a6ec', '#e4d2fb', '#f6eeff'),   # lilac, still soul fire
    'umbral': Ramp('#040208', '#0c0616', '#180c2c', '#2c1650', '#53309a', '#9a6ae6', '#e0c8ff'),
}


SHADE = 1.0


def build(variant='violet'):
    """Same wisp, different fire: the module's SOUL ramp (the streamer) and the fire's shade are swapped while it paints."""
    global SOUL, SHADE
    saved = SOUL
    SOUL = FLAMES.get(variant) or saved
    SHADE = {'pale': 1.3, 'umbral': 0.6}.get(variant, 1.0)
    try:
        return _build()
    finally:
        SOUL, SHADE = saved, 1.0


def _build():
    m = Model('soul_wisp', 64, 64)
    m.bone('root', None, (0, 24, 0))
    m.bone('skull', 'root', (0, -5, 0))
    m.cube('skull', (-4, -6, -4), (8, 6, 8), by_face(skull_shell, front=skull_front,
                                                   bottom=shaded(DARK_BONE, 1.5, grain(0.3))))
    m.bone('jaw', 'skull', (0, 0, 2))
    m.cube('jaw', (-3.5, 0, -5.5), (7, 2, 7), by_face(shaded(DARK_BONE, 3.4, grain(0.4, 2), bevel(0.5, -0.8, 0)),
                                                      front=jaw_front, top=shaded(DARK_BONE, 0.8)))
    m.bone('flame_a', 'skull', (0, 4, 0), (0, 45, 0))
    m.cube('flame_a', (-8, -15, 0), (16, 16, 0), lambda c: flame_tex(c, 3, SHADE))
    m.bone('flame_b', 'skull', (0, 4, 0), (0, -45, 0))
    m.cube('flame_b', (-8, -15, 0), (16, 16, 0), lambda c: flame_tex(c, 19, SHADE))
    m.bone('crown', 'skull', (0, -6, 0))
    m.cube('crown', (-4, -8, 0), (8, 8, 0), lambda c: flame_tex(c, 9, SHADE))
    m.cube('crown', (0, -8, -4), (0, 8, 8), lambda c: flame_tex(c, 25, SHADE))
    m.bone('tail', 'skull', (0, -2, 3.5), (8, 0, 0))
    m.cube('tail', (0, -4, 0), (0, 8, 12), tail_tex)
    for i in range(3):
        m.bone(f'mote{i}', 'skull', (0, -3, 0))
        m.cube(f'mote{i}', (-0.5, -0.5, -0.5), (1, 1, 1), mote)
    m.paint()
    return m


def tail_tex(ctx):
    """Vertical streamer trailing behind the skull: tall at the skull, rippling to a point. Flat bands, like the fire."""
    if ctx.face not in ('right', 'left'):
        return
    W, H = ctx.w, ctx.h
    for x in range(W):
        p = x if ctx.face == 'left' else W - 1 - x   # 0 = at the skull
        t = 1 - p / max(1, W - 1)
        mid = (H - 1) / 2 + math.sin(p * 0.9) * 1.2 * (1 - t)
        half = 0.4 + t * (H / 2 - 0.4)
        for y in range(H):
            d = abs(y - mid)
            if d <= half:
                e = d / max(half, 0.5)
                i = (5 if sum(SOUL.cols[5][:3]) < 600 else 4) if e > 0.7 else 3 if e > 0.35 else 1
                r, g, b = SOUL.cols[i][:3]
                ctx.emit(x, y, (r, g, b, 120 + 25 * (i // 2)))


PREVIEW_POSES = {'rest': {}}
