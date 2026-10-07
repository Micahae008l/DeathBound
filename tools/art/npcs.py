"""Underworld NPCs: the Ferryman (arrival), the Gravedigger (village, the only living man below), the Chained Prophet (crypt)."""
import math
from core import Model, Ramp, BONE, DARK_BONE, RAG, ROBE, PURPLE, SOUL, STEEL, WOOD, LEATHER, RUST, CLEAR
from materials import (by_face, chain, shaded, cloth, grain, smooth, bevel, vgrad, ao_top, tatter, trim, holes,
                       outline_edges, phys, hstripes, cracks, folds, speckle, steel)

ASH_SKIN = Ramp('#18161a', '#29252b', '#3d383e', '#544e54', '#6d666b', '#888084', '#a39b9c', '#bcb4b2')
LIVE_SKIN = Ramp('#231a1a', '#3a2b29', '#54403c', '#705852', '#8c7168', '#a68a7f', '#bfa498', '#d6bdb1')
PALE_SKIN = Ramp('#1d1b22', '#2f2c35', '#45414c', '#5d5866', '#77717f', '#928c99', '#aea8b4', '#c9c4ce')
GOLD = Ramp('#1e1407', '#3a2810', '#5c4218', '#7f5e22', '#a07a2e', '#c39a43', '#dfbe6a', '#f3dc9a')
WHITE_HAIR = Ramp('#2a2830', '#433f48', '#5d5862', '#78737c', '#958f97', '#b3adb3', '#cfcace', '#e6e2e4')
GREY_HAIR = Ramp('#17161a', '#24222a', '#34313b', '#46424e', '#5a5562', '#6f6a77')
COAT = Ramp('#120e0c', '#1d1714', '#29201b', '#362a23', '#44352c', '#534136', '#634e41')
CANVAS = Ramp('#14130f', '#201e19', '#2d2a23', '#3b372e', '#4a453a', '#5a5446', '#6b6553')
DENIM = Ramp('#0f1014', '#181a20', '#22252d', '#2d313b', '#393e49', '#464c58')
IRON = Ramp('#0b0a0d', '#151419', '#201e25', '#2c2a32', '#3a3740', '#4a4651', '#5d5965', '#76727e')
CANDLE = Ramp('#3a1404', '#6e2a06', '#a84a0c', '#de7a1c', '#ffae45', '#ffd98f', '#fff3d6')


def skin(ramp, base=4.2, wrinkles=0.0):
    mods = [grain(0.1, 2), smooth(0.25, 3.0), bevel(0.55, -0.6, -0.25), vgrad(0.25, -0.35)]
    if wrinkles:
        mods.append(lambda ctx, x, y: (math.sin(y * 2.4 + ctx.noise.smooth(x, y, 3.0, 9) * 2) * wrinkles) if ctx.side else 0.0)
    return shaded(ramp, base, *mods)


def ragged(ramp, base=3.0, tat=0, hole=None, fold=1.0, period=3.0, lining=None, lining_rows=0, stain_ramp=None):
    def paint(ctx):
        cloth(ramp, base, fold=fold, period=period, stain_ramp=stain_ramp)(ctx)
        if lining is not None and lining_rows:
            trim(lining, lining_rows, level=2.6)(ctx)
        if hole:
            holes(hole)(ctx)
        if tat:
            tatter(tat, 0.8)(ctx)
            outline_edges(ramp)(ctx)
    return paint


def open_ends(painter, faces=('top', 'bottom')):
    """Tubes of cloth (sleeves, skirts) are hollow at their open ends."""
    def paint(ctx):
        painter(ctx)
        if ctx.face in faces:
            ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)
    return paint


def chain_links(ctx):
    """Iron chain: alternating flat and edge-on links along the long axis."""
    long = ctx.h >= ctx.w
    n = ctx.h if long else ctx.w
    for y in range(ctx.h):
        for x in range(ctx.w):
            k = y if long else x
            phase = k % 4
            if phase == 3:
                ctx.set(x, y, IRON.at(1.0, x, y, 0))
            elif phase == 0:
                ctx.set(x, y, IRON.at(5.6 + ctx.light, x, y, 0))
            else:
                ctx.set(x, y, IRON.at(3.6 + ctx.light + ctx.noise.white(x, k, 3) * 0.4, x, y, 0))


# ================================================================== the Ferryman
def ferry_face(ctx):
    L = {'k': ASH_SKIN.cols[0], 'd': ASH_SKIN.cols[2], 'm': ASH_SKIN.cols[4], 's': ASH_SKIN.cols[5], 'S': ASH_SKIN.cols[6],
         'b': ASH_SKIN.cols[1], 'G': GOLD.cols[6], 'g': GOLD.cols[4], 'o': GOLD.cols[2], 'n': ASH_SKIN.cols[3],
         'N': ASH_SKIN.cols[6], 'W': WHITE_HAIR.cols[6], 'w': WHITE_HAIR.cols[4], 'v': WHITE_HAIR.cols[2]}
    G = {'G': (220, 180, 90, 150)}
    ctx.stamp(["kddddddk",
               "dbbmmbbd",
               "mGgmnGgm",
               "mgoNngom",
               "dsmNNmsd",
               "mWWwwWWm",
               "WwvkkvwW",
               "wWwWWwWw"], L, glow_legend=G)


def ferry_head_side(ctx):
    skin(ASH_SKIN, 3.6, wrinkles=0.35)(ctx)


def ferry_hood(ctx):
    ragged(RAG, 2.6, hole=0.86)(ctx)
    if ctx.face == 'front':
        for y in range(ctx.h):
            for x in range(ctx.w):
                if 2 <= x < ctx.w - 2 and 2 <= y:
                    ctx.set(x, y, CLEAR)
                elif 1 <= x < ctx.w - 1 and 1 <= y and (x in (1, ctx.w - 2) or y == 1):
                    ctx.set(x, y, RAG.at(1.2, x, y, 0))
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and y < ctx.h - 1 else None)
    tatter(3, 0.75, faces=('right', 'left', 'back'))(ctx)


def beard(ctx, base=4.2, amp=0.9):
    for y in range(ctx.h):
        for x in range(ctx.w):
            p = phys(ctx, x)
            strand = math.sin(p * 2.3 + ctx.noise.smooth(p, y, 4.0, 5) * 1.5)
            L = base - y * 0.25 + strand * amp + ctx.noise.white(p, y, 6) * 0.3
            ctx.set(x, y, WHITE_HAIR.at(L, x, y, 0.25))
    # a beard thins to a few long strands at the end
    for x in range(ctx.w):
        p = phys(ctx, x)
        cut = int(ctx.noise._h(p, 0, 19) * 5)
        for y in range(ctx.h - cut, ctx.h):
            ctx.set(x, y, CLEAR)


def coin_robe(ctx):
    ragged(ROBE, 3.1, fold=1.2)(ctx)
    if ctx.face == 'front':
        # a strand of grave-coins across the chest, one for every soul he was never paid for
        for i, x in enumerate(range(1, ctx.w - 1)):
            y = 2 + int(round(2.2 * math.sin(math.pi * (x - 0.5) / (ctx.w - 1))))
            ctx.set(x, y - 1, IRON.cols[2])
            if i % 2 == 0:
                ctx.set(x, y, GOLD.cols[5])
                ctx.set(x, y + 1, GOLD.cols[3])
                ctx.emit(x, y, (190, 150, 70, 90))
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 2 <= x < ctx.w - 2 and 1 <= y < ctx.h - 1 else None)


def ferry_mantle(ctx):
    ragged(RAG, 2.9, tat=3, fold=0.9)(ctx)
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CLEAR if 3 <= x < ctx.w - 3 and 1 <= y < ctx.h - 1 else None)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)


def bony_hand(ctx):
    shaded(ASH_SKIN, 3.4, grain(0.3, 2), bevel(0.5, -0.5, -0.3), vgrad(0.2, -0.5))(ctx)
    if ctx.side:
        for x in range(ctx.w):
            if x % 2 == 1:
                ctx.set(x, ctx.h - 1, CLEAR)
            ctx.set(x, 0, ASH_SKIN.cols[5])


def pole(ctx):
    shaded(WOOD, 3.2, lambda c, x, y: c.noise.smooth(x * 4, y * 0.25, 3.0, 2) * 0.8, grain(0.25, 1), bevel(0.3, -0.3, -0.4))(ctx)
    if ctx.side:
        for y in range(4, ctx.h, 13):  # iron bands
            for x in range(ctx.w):
                ctx.set(x, y, IRON.cols[5])
                ctx.set(x, y + 1, IRON.cols[2])


def lantern_cage(ctx):
    steel(IRON, 3.2, rivets=False, scratches=0)(ctx)
    if ctx.side:
        for y in range(1, ctx.h - 1):
            for x in range(1, ctx.w - 1):
                ctx.set(x, y, CLEAR)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: IRON.cols[1])


def soul_flame(ctx):
    for y in range(ctx.h):
        for x in range(ctx.w):
            hot = 1 - y / max(1, ctx.h - 1)
            col = SOUL.at(2.0 + (1 - hot) * 3.2 + ctx.noise.white(x, y, 4) * 0.4, x, y, 0)
            ctx.set(x, y, col)
            ctx.emit(x, y, col)


def build_ferryman():
    m = Model('ferryman', 128, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('hips', 'root', (0, -20, 0))
    m.cube('hips', (-5, 0, -4), (10, 11, 8), open_ends(ragged(ROBE, 3.0, fold=1.2, stain_ramp=PURPLE)))
    m.bone('skirt', 'hips', (0, 10, 0))
    m.cube('skirt', (-6, 0, -5), (12, 10, 10), open_ends(ragged(ROBE, 2.8, tat=4, fold=1.3, stain_ramp=PURPLE)))
    m.bone('chest', 'hips', (0, 0, 0), (16, 0, 0))
    m.cube('chest', (-4.5, -13, -3), (9, 13, 6), coin_robe)
    m.cube('chest', (-6, -14, -4), (12, 5, 8), ferry_mantle, inflate=0.2)
    m.bone('head', 'chest', (0, -13, -1), (-14, 0, 0))
    m.cube('head', (-4, -8, -4), (8, 8, 8), by_face(ferry_head_side, front=ferry_face, bottom=shaded(ASH_SKIN, 1.6)), mat='skin')
    m.cube('head', (-5, -10, -5.5), (10, 11, 10), ferry_hood)
    m.bone('hood_tip', 'head', (0, -9.5, 4), (-38, 0, 0))
    m.cube('hood_tip', (-2, -1, 0), (4, 3, 6), ragged(RAG, 2.6, tat=2))
    m.bone('beard', 'head', (0, -2, -4.1))
    m.cube('beard', (-2.5, 0, 0), (5, 9, 0), beard)
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore, hand = f'{side}_arm', f'{side}_forearm', f'{side}_hand'
        m.bone(arm, 'chest', (6 * sx, -12, 0), (-38, 0, 8) if sx < 0 else (-10, 0, -6))
        m.cube(arm, (-2, -1, -2), (4, 11, 4), open_ends(ragged(RAG, 2.8, fold=0.9, period=2.5)))
        m.bone(fore, arm, (0, 10, 0), (-40, 0, 0) if sx < 0 else (-25, 0, 0))
        m.cube(fore, (-2.5, 0, -2.5), (5, 7, 5), open_ends(ragged(RAG, 2.6, tat=2, period=2.5)))
        m.cube(fore, (-1, 0, -1), (2, 7, 2), bony_hand, mat='skin')
        m.bone(hand, fore, (0, 7, 0))
        m.cube(hand, (-1.5, 0, -1.5), (3, 3, 3), bony_hand, mat='skin')
    # the punt-pole he no longer has a river for, a soul lantern swinging from its crook
    m.bone('pole', 'right_hand', (0, 1.5, 0), (66, 0, -8))
    m.cube('pole', (-1, -30, -1), (2, 50, 2), pole)
    m.cube('pole', (-1, -31, -1), (6, 1, 2), pole)
    m.bone('cloak', 'hips', (0, -12, 4.2), (5, 0, 0))
    m.cube('cloak', (-6, 0, 0), (12, 32, 0), ragged(RAG, 2.6, tat=6, hole=0.84, stain_ramp=PURPLE))
    m.bone('lantern', 'pole', (4, -30, 0))
    m.cube('lantern', (-0.5, 0, -0.5), (1, 3, 1), chain_links)
    m.cube('lantern', (-2, 3, -2), (4, 5, 4), lantern_cage)
    m.cube('lantern', (-1.5, 4.5, -1.5), (3, 3, 3), soul_flame)
    m.cube('lantern', (-2.5, 2.5, -2.5), (5, 1, 5), shaded(IRON, 3.6, bevel(0.8, -0.6, 0)))
    m.paint()
    return m


FERRY_POSE = {}


# ================================================================== the Gravedigger
def digger_face(ctx):
    L = {'h': GREY_HAIR.cols[2], 'H': GREY_HAIR.cols[4], 's': LIVE_SKIN.cols[4], 'S': LIVE_SKIN.cols[5],
         'b': GREY_HAIR.cols[0], 'r': LIVE_SKIN.cols[2], 'w': (196, 188, 172, 255), 'p': (30, 38, 46, 255),
         'n': LIVE_SKIN.cols[2], 'N': LIVE_SKIN.cols[6], 't': LIVE_SKIN.cols[3], 'T': GREY_HAIR.cols[1],
         'm': LIVE_SKIN.cols[0]}
    ctx.stamp(["hhHhhHhh",
               "sSsSSsSs",
               "bbbssbbb",
               "rwpsSpwr",
               "srrsNrrs",
               "sssnnsss",
               "TtmmmmtT",
               "tTtTTtTt"], L)


def digger_head_side(ctx):
    skin(LIVE_SKIN, 4.1)(ctx)
    if ctx.side:  # grey hair at the temples and nape
        for x in range(ctx.w):
            ctx.set(x, 0, GREY_HAIR.at(3.2, x, 0))
            if ctx.face == 'back' or (ctx.face in ('right', 'left') and phys(ctx, x) > ctx.w // 2):
                for y in range(1, 5):
                    ctx.set(x, y, GREY_HAIR.at(2.6 + ctx.noise.white(x, y) * 0.5, x, y))


def hat_crown(ctx):
    shaded(CANVAS, 2.4, grain(0.3, 2), smooth(0.4), bevel(0.6, -0.4, -0.2))(ctx)
    if ctx.side:  # sweat-stained band
        for x in range(ctx.w):
            ctx.set(x, ctx.h - 2, LEATHER.at(2.0, x, 0))
            ctx.set(x, ctx.h - 1, LEATHER.at(1.2, x, 0))


def hat_brim(ctx):
    shaded(CANVAS, 2.2 if ctx.face == 'top' else 1.4, grain(0.3, 2), smooth(0.5, 3.0))(ctx)
    if ctx.face in ('top', 'bottom'):
        cx, cy = (ctx.w - 1) / 2, (ctx.h - 1) / 2
        for y in range(ctx.h):
            for x in range(ctx.w):
                d = max(abs(x - cx) / cx, abs(y - cy) / cy)
                if d > 0.93 and ctx.noise._h(x, y, 8) > 0.5:
                    ctx.set(x, y, CLEAR)   # frayed, chewed edge


def coat(tat=0):
    def paint(ctx):
        cloth(COAT, 3.2, fold=0.7, period=4.0, stain_ramp=CANVAS)(ctx)
        if tat:
            tatter(tat, 0.7)(ctx)
            outline_edges(COAT)(ctx)
    return paint


def coat_torso(ctx):
    coat()(ctx)
    if ctx.face == 'front':
        c = ctx.w // 2
        for y in range(ctx.h):
            ctx.set(c - 1, y, COAT.cols[0])  # the coat's opening
            if y % 3 == 1:
                ctx.set(c, y, IRON.cols[5])  # buttons
        for x in range(ctx.w):  # belt
            ctx.set(x, ctx.h - 3, LEATHER.at(2.2, x, 0))
            ctx.set(x, ctx.h - 2, LEATHER.at(1.4, x, 0))
        ctx.set(c - 1, ctx.h - 3, RUST.cols[4]); ctx.set(c, ctx.h - 3, RUST.cols[5])
    if ctx.face == 'top':
        ctx.fill(lambda x, y: CANVAS.at(2.0, x, y) if 2 <= x < ctx.w - 2 else None)


def scarf(ctx):
    cloth(Ramp('#1a0f0f', '#2a1616', '#3c1e1d', '#4f2826', '#633330'), 2.4, fold=0.8, period=2.0)(ctx)
    tatter(1, 0.5)(ctx)


def hand_skin(ctx):
    skin(LIVE_SKIN, 3.8)(ctx)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: LIVE_SKIN.cols[2])


def trousers(ctx):
    cloth(DENIM, 2.8, fold=0.5, period=3.0)(ctx)
    if ctx.side:  # caked grave-dirt toward the knees and cuffs
        for y in range(ctx.h):
            for x in range(ctx.w):
                if ctx.noise.smooth(x, y, 2.0, 12) + y / ctx.h * 1.2 > 1.2:
                    ctx.set(x, y, COAT.at(2.0 + ctx.noise.white(x, y, 13) * 0.6, x, y))


def boot(ctx):
    shaded(LEATHER, 2.4, grain(0.35, 2), bevel(0.8, -0.8, -0.3))(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, ctx.h - 1, COAT.cols[0])


def shovel_handle(ctx):
    shaded(WOOD, 3.6, lambda c, x, y: c.noise.smooth(x * 3, y * 0.3, 3.0, 2) * 0.7, bevel(0.3, -0.3, -0.3))(ctx)


def shovel_blade(ctx):
    steel(STEEL, 3.4, rivets=False, scratches=3)(ctx)
    for y in range(ctx.h):
        for x in range(ctx.w):
            if ctx.noise.smooth(x, y, 1.6, 7) > 0.15:
                ctx.set(x, y, RUST.at(2.6 + ctx.noise.white(x, y, 8) * 0.8, x, y))
            if y >= ctx.h - 2 and ctx.side:
                ctx.set(x, y, COAT.at(2.2, x, y))   # dirt on the edge


def candle_lantern(ctx):
    steel(IRON, 3.0, rivets=False, scratches=0)(ctx)
    if ctx.side:
        for y in range(1, ctx.h - 1):
            for x in range(1, ctx.w - 1):
                hot = 1 - abs(x - (ctx.w - 1) / 2) / max(1, ctx.w / 2)
                col = CANDLE.at(2.2 + hot * 2.6 - y * 0.2, x, y, 0)
                ctx.set(x, y, col)
                ctx.emit(x, y, col)


def build_gravedigger():
    m = Model('gravedigger', 128, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('body', 'root', (0, -12, 0), (8, 0, 0))
    m.cube('body', (-4, -12, -2), (8, 12, 4), coat_torso, inflate=0.1)
    m.cube('body', (-4.5, -12.5, -2.5), (9, 3, 5), scarf, inflate=0.1)
    m.bone('coat_tail', 'body', (0, 0, 0))
    m.cube('coat_tail', (-4.5, -0.5, -2.5), (9, 8, 5), open_ends(coat(tat=2)), inflate=0.15)
    m.bone('head', 'body', (0, -12, 0), (-8, 0, 0))
    m.cube('head', (-4, -8, -4), (8, 8, 8), by_face(digger_head_side, front=digger_face, bottom=shaded(LIVE_SKIN, 2.0)), mat='skin')
    m.bone('hat', 'head', (0, -7, 0), (-4, 0, 3))
    m.cube('hat', (-4.5, -4, -4.5), (9, 4, 9), hat_crown)
    m.cube('hat', (-8, 0, -8), (16, 1, 16), hat_brim)
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        m.bone(arm, 'body', (6 * sx, -10, 0))
        m.cube(arm, (-2, -2, -2), (4, 7, 4), coat(), inflate=0.15)
        m.bone(fore, arm, (0, 5, 0))
        m.cube(fore, (-2, 0, -2), (4, 5, 4), coat(), inflate=0.1)
        m.cube(fore, (-1.5, 5, -1.5), (3, 2, 3), hand_skin, mat='skin')
    # the shovel rides on his back now; at the forge his hands are for the hammer
    m.bone('shovel', 'body', (0, -7, 2.8), (0, 0, 38))
    m.cube('shovel', (-0.5, -12, -0.5), (1, 22, 1), shovel_handle)
    m.cube('shovel', (-1.5, -13, -0.5), (3, 1, 1), shovel_handle)
    m.cube('shovel', (-2, 8, -0.5), (4, 6, 1), shovel_blade)
    m.bone('hammer', 'right_forearm', (0, 6.5, 0), (0, 0, 0))
    m.cube('hammer', (-0.5, -0.5, -7), (1, 1, 8), shovel_handle)
    m.cube('hammer', (-1, -2, -9), (2, 4, 2), shovel_blade)
    m.bone('lantern', 'body', (3.5, -2, -2.5))
    m.cube('lantern', (-0.5, 0, -0.5), (1, 1, 1), chain_links)
    m.cube('lantern', (-1.5, 1, -1.5), (3, 4, 3), candle_lantern)
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_leg', 'root', (2 * sx, -12, 0))
        m.cube(f'{side}_leg', (-2, 0, -2), (4, 10, 4), trousers)
        m.cube(f'{side}_leg', (-2, 9, -2.5), (4, 3, 5), boot, inflate=0.15)
    m.paint()
    return m


DIGGER_POSE = {'right_arm': (-25, 0, 6, 0, 0, 0), 'right_forearm': (-50, 0, 0, 0, 0, 0),
               'left_arm': (-35, 0, -12, 0, 0, 0), 'left_forearm': (-45, 0, 0, 0, 0, 0)}


# ================================================================== the Chained Prophet
def prophet_face(ctx):
    L = {'H': WHITE_HAIR.cols[6], 'h': WHITE_HAIR.cols[4], 's': PALE_SKIN.cols[4], 'S': PALE_SKIN.cols[5],
         'B': RAG.cols[2], 'b': RAG.cols[4], 'T': (60, 20, 110, 255), 'n': PALE_SKIN.cols[3], 'N': PALE_SKIN.cols[6],
         'm': PALE_SKIN.cols[1], 'k': PALE_SKIN.cols[0]}
    G = {'T': SOUL.cols[3], 'E': SOUL.cols[4]}
    ctx.stamp(["HhHHhHHh",
               "hsSssSsh",
               "BBBBBBBB",
               "BbBBBBbB",
               "sTssNsTs",
               "hTsnnsTh",
               "hsmkkmsh",
               "HhmkkmhH"], L, glow_legend=G)


def prophet_head_side(ctx):
    skin(PALE_SKIN, 3.8, wrinkles=0.25)(ctx)
    if ctx.side:
        for y in range(ctx.h):
            for x in range(ctx.w):
                p = phys(ctx, x)
                back = ctx.face == 'back' or (ctx.face in ('right', 'left') and p > 2)
                if y < 2 or back:
                    ctx.set(x, y, WHITE_HAIR.at(4.6 + math.sin(p * 2.1) * 0.8 - y * 0.08, x, y))
        if ctx.face in ('right', 'left'):  # the blindfold wraps round
            for x in range(ctx.w):
                ctx.set(x, 2, RAG.at(2.0, x, 2)); ctx.set(x, 3, RAG.at(3.4, x, 3))


def hair_curtain(ctx):
    beard(ctx, 5.0, 0.5)


def hair_top(ctx):
    for y in range(ctx.h):
        for x in range(ctx.w):
            part = abs(x - (ctx.w - 1) / 2)
            L = 3.1 + math.sin(y * 1.7 + x * 0.4) * 0.6 - (0.9 if part < 0.6 else 0) + ctx.noise.white(x, y, 3) * 0.3
            ctx.set(x, y, WHITE_HAIR.at(L, x, y, 0.25))


def ribbed_skin(ctx):
    skin(PALE_SKIN, 3.6)(ctx)
    if ctx.face in ('front', 'back'):
        for y in range(2, ctx.h - 3, 2):
            for x in range(ctx.w):
                if abs(x - (ctx.w - 1) / 2) > 0.8:
                    ctx.set(x, y, PALE_SKIN.at(2.2, x, y))
                    if y + 1 < ctx.h:
                        ctx.set(x, y + 1, PALE_SKIN.at(4.6, x, y + 1))


def prophet_rags(ctx):
    ragged(RAG, 2.8, tat=4, hole=0.8, stain_ramp=PURPLE)(ctx)
    if ctx.face in ('top', 'bottom'):
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)


def shackle(ctx):
    steel(IRON, 3.0, rivets=True, scratches=1)(ctx)


# ------------------------------------------------------------------ the old King's regalia (shown once he's freed)
ROYAL = Ramp('#0a0610', '#130b1c', '#1d1129', '#291838', '#36204a', '#44295d', '#543272', '#663d88')


def royal_robe(ctx):
    cloth(ROYAL, 3.1, fold=1.0, period=3.5)(ctx)
    if ctx.face == 'front':   # a band of tarnished gold down the front, a soul-stone clasp at the throat
        mid = (ctx.w - 1) / 2
        for y in range(ctx.h):
            for x in range(ctx.w):
                if abs(x - mid) < 1.0:
                    ctx.set(x, y, GOLD.at(3.6 + ctx.noise.white(x, y, 5) * 0.7 - y * 0.06, x, y))
        c = int(mid)
        ctx.set(c, 1, SOUL.cols[4])
        ctx.emit(c, 1, SOUL.cols[4])
    trim(GOLD, 1, level=3.0)(ctx)
    if ctx.face in ('top', 'bottom'):
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)


def royal_sleeve(ctx):
    cloth(ROYAL, 3.0, fold=0.9, period=2.5)(ctx)
    trim(GOLD, 1, level=3.0)(ctx)
    if ctx.face in ('top', 'bottom'):
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)


def royal_mantle(ctx):
    """Velvet over the shoulders with a collar of grey fur."""
    cloth(ROYAL, 3.4, fold=0.7, period=4.0)(ctx)
    if ctx.side:
        for x in range(ctx.w):
            for y in range(min(2, ctx.h)):
                ctx.set(x, y, WHITE_HAIR.at(4.6 - y * 1.4 + ctx.noise.white(x, y, 8) * 0.9, x, y))
    if ctx.face == 'top':
        def fur(x, y):
            inner = 2 <= x < ctx.w - 2 and 2 <= y < ctx.h - 2
            return CLEAR if inner else WHITE_HAIR.at(4.2 + ctx.noise.white(x, y, 9) * 1.0, x, y)
        ctx.fill(fur)
    if ctx.face == 'bottom':
        ctx.fill(lambda x, y: CLEAR)


def crown_band(ctx):
    steel(GOLD, 2.9, rivets=False, scratches=1)(ctx)
    if ctx.side:
        for x in range(ctx.w):   # a darker rim top and bottom, and a soul-stone set in every face
            ctx.set(x, ctx.h - 1, GOLD.cols[2])
        c = ctx.w // 2
        ctx.set(c, 1, SOUL.cols[3])
        ctx.emit(c, 1, SOUL.cols[3])
        if ctx.face == 'front':
            for x in (c - 1, c + 1):
                if 0 <= x < ctx.w:
                    ctx.set(x, 1, SOUL.cols[5])
                    ctx.emit(x, 1, SOUL.cols[2])
    if ctx.face in ('top', 'bottom'):
        ctx.fill(lambda x, y: CLEAR if 1 <= x < ctx.w - 1 and 1 <= y < ctx.h - 1 else None)


def crown_spike(ctx):
    for y in range(ctx.h):
        for x in range(ctx.w):
            ctx.set(x, y, GOLD.at(4.0 - y * 0.9 + ctx.light + ctx.noise.white(x, y, 3) * 0.4, x, y, 0))
    if ctx.face == 'top':
        ctx.set(0, 0, GOLD.cols[7])


def regalia(m):
    """Crown, mantle, robe and sleeves: their own bones, so the model can hide them while he hangs in chains."""
    m.bone('crown', 'head', (0, -8, 0))
    m.cube('crown', (-4, -2.5, -4), (8, 3, 8), crown_band, inflate=0.05)
    for i, (x, z) in enumerate(((-4, -4), (3, -4), (-4, 3), (3, 3), (-0.5, -4), (-0.5, 3), (-4, -0.5), (3, -0.5))):
        tall = 3 if i == 4 else 2
        m.cube('crown', (x, -2.5 - tall, z), (1, tall, 1), crown_spike)
    m.bone('mantle', 'body', (0, 0, 0))
    m.cube('mantle', (-5, -10.5, -3), (10, 4, 6), royal_mantle, inflate=0.25)
    m.bone('robe', 'body', (0, 0, 0))
    m.cube('robe', (-4, -10, -2.5), (8, 10, 5), royal_robe, inflate=0.35)
    m.bone('robe_hips', 'hips', (0, 0, 0))
    m.cube('robe_hips', (-4.5, -2.5, -2.8), (9, 5, 6), royal_robe, inflate=0.3)
    for side in ('right', 'left'):
        m.bone(f'{side}_sleeve', f'{side}_arm', (0, 0, 0))
        m.cube(f'{side}_sleeve', (-1.5, -1, -1.5), (3, 8, 3), royal_sleeve, inflate=0.35)
        m.bone(f'{side}_cuff', f'{side}_forearm', (0, 0, 0))
        m.cube(f'{side}_cuff', (-2, 0, -2), (4, 4, 4), royal_sleeve, inflate=0.25)
        m.bone(f'{side}_thigh_robe', f'{side}_thigh', (0, 0, 0))
        m.cube(f'{side}_thigh_robe', (-1.5, 0, -1.5), (3, 7, 3), royal_sleeve, inflate=0.4)
        m.bone(f'{side}_shin_robe', f'{side}_shin', (0, 0, 0))
        m.cube(f'{side}_shin_robe', (-1.5, 0, -1.5), (3, 6, 3), royal_sleeve, inflate=0.4)


def corruption(m, seed=7):
    """The hint at the end of the King's ending: veins of the Death King's light creeping over his skin (an emissive
    overlay, faded in by the renderer). Drawn over every skin cube at the texture's full resolution."""
    import random
    import numpy as np
    from core import face_rects
    rnd, s = random.Random(seed), m.scale
    out = np.zeros_like(m.img)
    for c in m.cubes:
        if c.mat != 'skin' or c.share is not None:
            continue
        for face, (u, v, fw, fh) in face_rects(c).items():
            if fw <= 0 or fh <= 0 or face in ('top', 'bottom'):
                continue
            x0, y0, x1, y1 = u * s, v * s, (u + fw) * s, (v + fh) * s
            for _ in range(max(1, fw * fh // 10)):
                x, y = rnd.uniform(x0, x1), rnd.uniform(y0 + (y1 - y0) * 0.3, y1)
                for step in range(rnd.randint(8, 22)):
                    px, py = int(x), int(y)
                    if x0 <= px < x1 and y0 <= py < y1:
                        a = max(60, 240 - step * 8)
                        out[py, px] = (190, 110, 255, a)
                    x += rnd.uniform(-0.9, 0.9)
                    y -= rnd.uniform(0.2, 1.1)   # upward, towards the face
    return out


def build_prophet():
    m = Model('prophet', 128, 128)
    m.bone('root', None, (0, 24, 0))
    m.bone('hips', 'root', (0, -10, 0))
    m.cube('hips', (-4, -2, -2), (8, 4, 4), ribbed_skin, mat='skin')
    m.cube('hips', (-4.5, -2.5, -2.5), (9, 9, 5), prophet_rags, inflate=0.1)
    m.bone('body', 'hips', (0, -2, 0), (10, 0, 0))
    m.cube('body', (-3.5, -10, -2), (7, 10, 4), ribbed_skin, mat='skin')
    m.bone('head', 'body', (0, -10, -0.5), (20, 0, 0))
    m.cube('head', (-3.5, -8, -3.5), (7, 8, 7), by_face(prophet_head_side, front=prophet_face, bottom=shaded(PALE_SKIN, 1.6),
                                                       top=hair_top), mat='skin')
    for sx in (-1, 1):
        m.bone(f'lock_{sx + 1}', 'head', (3.9 * sx, -6, -0.5), (0, 0, 6 * sx))
        m.cube(f'lock_{sx + 1}', (0, 0, -2), (0, 11, 4), hair_curtain)
    m.cube('head', (-3.5, -6, -3.5), (7, 2, 7), by_face(shaded(RAG, 2.6, grain(0.3)), front=lambda c: None,
                                                       top=lambda c: None, bottom=lambda c: None), inflate=0.15)
    m.bone('hair', 'head', (0, -6, 3.7))
    m.cube('hair', (-3.5, 0, 0), (7, 13, 0), hair_curtain)
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore, links = f'{side}_arm', f'{side}_forearm', f'{side}_chain'
        m.bone(arm, 'body', (4.5 * sx, -9, 0))
        m.cube(arm, (-1.5, -1, -1.5), (3, 8, 3), skin(PALE_SKIN, 3.6), mat='skin')
        m.bone(fore, arm, (0, 7, 0))
        m.cube(fore, (-1.5, 0, -1.5), (3, 7, 3), skin(PALE_SKIN, 3.4), mat='skin')
        m.cube(fore, (-2, 4, -2), (4, 3, 4), shackle)
        m.cube(fore, (-1, 7, -1), (2, 2, 2), bony_hand)
        m.bone(links, fore, (0, 6, 0))
        m.cube(links, (-0.5, 0, -0.5), (1, 26, 1), chain_links)
        m.bone(f'{side}_thigh', 'hips', (2 * sx, 1, 0), (-80, 0, 0))
        m.cube(f'{side}_thigh', (-1.5, 0, -1.5), (3, 7, 3), skin(PALE_SKIN, 3.4), mat='skin')
        m.bone(f'{side}_shin', f'{side}_thigh', (0, 7, 0), (100, 0, 0))
        m.cube(f'{side}_shin', (-1.5, 0, -1.5), (3, 7, 3), skin(PALE_SKIN, 3.2), mat='skin')
    regalia(m)
    m.paint()
    return m


# arms hang up and out in their chains
PROPHET_POSE = {'right_arm': (0, 0, 135, 0, 0, 0), 'right_forearm': (0, 0, 15, 0, 0, 0), 'right_chain': (0, 0, 10, 0, 0, 0),
                'left_arm': (0, 0, -135, 0, 0, 0), 'left_forearm': (0, 0, -15, 0, 0, 0), 'left_chain': (0, 0, -10, 0, 0, 0)}

# ================================================================== the Collector
MOSS = Ramp('#0f110c', '#181b13', '#22261b', '#2d3223', '#3a402c', '#474e36', '#565e41')
LENS = Ramp('#0b2412', '#164a24', '#24733a', '#3fa552', '#7fd27a', '#c4f5b4')
BRASS = Ramp('#1c1306', '#36260c', '#584015', '#7a5a1e', '#9c7a2c', '#c3a14a', '#e6cd82')
SACK = Ramp('#17130e', '#241e16', '#332a1f', '#433829', '#544735', '#665742')


def collector_face(ctx):
    """Sunken and thin-lipped, white stubble; the left eye bright and greedy (the right one is behind the lens)."""
    L = {'c': MOSS.cols[2], 'C': MOSS.cols[3], 's': ASH_SKIN.cols[4], 'S': ASH_SKIN.cols[5], 'h': ASH_SKIN.cols[6],
         'k': ASH_SKIN.cols[2], 'K': ASH_SKIN.cols[1], 'w': (214, 206, 170, 255), 'p': (24, 18, 14, 255), 'n': ASH_SKIN.cols[3],
         'N': ASH_SKIN.cols[6], 'm': ASH_SKIN.cols[1], 't': WHITE_HAIR.cols[4], 'T': WHITE_HAIR.cols[6]}
    ctx.stamp(["cCcCcCc",
               "SshhhsS",
               "kKkskwp",
               "sSsNsSk",
               "KnsNsnK",
               "tsmmmst",
               "TtTtTtT"], L)


def collector_head_side(ctx):
    skin(ASH_SKIN, 4.0, wrinkles=0.25)(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, 0, MOSS.at(2.6, x, 0))
            if ctx.face == 'back':
                for y in range(1, 4):
                    ctx.set(x, y, WHITE_HAIR.at(3.0 + ctx.noise.white(x, y) * 0.8, x, y))


def cap(ctx):
    cloth(MOSS, 2.6, fold=0.4, period=2.0)(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, ctx.h - 1, LEATHER.at(1.8, x, 0))


def moss_coat(tat=0):
    def paint(ctx):
        cloth(MOSS, 3.2, fold=0.8, period=3.0, stain_ramp=SACK)(ctx)
        if ctx.face == 'front' and ctx.h >= 8:   # pockets, everywhere
            for py in (2, 6):
                for px in (1, ctx.w - 3):
                    for y in range(py, min(ctx.h, py + 2)):
                        for x in range(px, min(ctx.w, px + 2)):
                            ctx.set(x, y, MOSS.at(1.6, x, y))
                    ctx.set(px, py, BRASS.cols[5])
        if tat:
            tatter(tat, 0.7)(ctx)
            outline_edges(MOSS)(ctx)
    return paint


def lens(ctx):
    """A brass-rimmed monocle: the glass glows the green of old soul-light."""
    for y in range(ctx.h):
        for x in range(ctx.w):
            rim = x in (0, ctx.w - 1) or y in (0, ctx.h - 1)
            if ctx.face in ('front', 'back') and not rim:
                col = LENS.at(4.2, x, y, 0)
                ctx.set(x, y, col)
                ctx.emit(x, y, col)
            else:
                ctx.set(x, y, BRASS.at(4.6 + (0.8 if (x + y) % 2 else 0), x, y))


def pack_frame(ctx):
    shaded(WOOD, 2.8, lambda c, x, y: c.noise.smooth(x * 3, y * 0.4, 3.0, 4) * 0.6, bevel(0.3, -0.3, -0.3))(ctx)


def crate(ctx):
    shaded(WOOD, 3.2, grain(0.3, 2), bevel(0.6, -0.5, -0.3))(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, 0, WOOD.at(1.6, x, 0))
            ctx.set(x, ctx.h - 1, WOOD.at(1.6, x, 0))
        for y in range(ctx.h):
            ctx.set(0, y, WOOD.at(1.8, 0, y))
            ctx.set(ctx.w - 1, y, WOOD.at(1.8, 0, y))
            if ctx.w > 4:
                ctx.set((y * 7) % (ctx.w - 2) + 1, y, IRON.cols[4])   # a diagonal brace


def sack(ctx):
    cloth(SACK, 3.0, fold=1.2, period=2.5)(ctx)
    if ctx.side:
        for x in range(ctx.w):
            ctx.set(x, 1, LEATHER.at(2.0, x, 0))   # the strap


def scroll(ctx):
    shaded(Ramp('#3b3226', '#5c4f3c', '#7d6d53', '#9e8c6c', '#bfae8a', '#ddd0ad'), 3.6, grain(0.3, 2))(ctx)
    if ctx.face in ('right', 'left'):
        ctx.fill(lambda x, y: (60, 44, 30, 255) if (x + y) % 2 else None)


def jar_on_pack(glow):
    def paint(ctx):
        for y in range(ctx.h):
            for x in range(ctx.w):
                if y == 0:
                    ctx.set(x, y, LEATHER.at(2.6, x, y))
                else:
                    col = glow.at(2.4 + (1.6 if x == 0 else 0) + (y % 2) * 0.4, x, y, 0)
                    ctx.set(x, y, col)
                    ctx.emit(x, y, col)
    return paint


def trinket(ctx):
    shaded(GOLD, 4.6, grain(0.2, 2), bevel(0.8, -0.6, -0.4))(ctx)


def build_collector():
    m = Model('collector', 128, 128)
    m.bone('root', None, (0, 24, 0))
    for side, sx in (('right', -1), ('left', 1)):
        m.bone(f'{side}_leg', 'root', (1.8 * sx, -11, 0))
        m.cube(f'{side}_leg', (-1.5, 0, -1.5), (3, 9, 3), trousers)
        m.cube(f'{side}_leg', (-1.5, 8, -2.5), (3, 3, 4), boot, inflate=0.15)
    m.bone('body', 'root', (0, -11, 0), (24, 0, 0))   # bent nearly double under the pack
    m.cube('body', (-3.5, -11, -2), (7, 11, 4), moss_coat(), inflate=0.1)
    m.bone('coat_tail', 'body', (0, 0, 0), (-18, 0, 0))
    m.cube('coat_tail', (-4, -0.5, -2.5), (8, 8, 5), open_ends(moss_coat(tat=2)), inflate=0.15)
    m.bone('head', 'body', (0, -11, -1), (-30, 0, 0))
    m.cube('head', (-3.5, -7, -4), (7, 7, 7), by_face(collector_head_side, front=collector_face, bottom=shaded(ASH_SKIN, 2.0)), mat='skin')
    m.cube('head', (-0.5, -4, -5.5), (1, 2, 2), shaded(ASH_SKIN, 4.4, bevel(0.5, -0.5, -0.3)), mat='skin')   # a long nose
    m.cube('head', (-4, -7.5, -4.5), (8, 2, 8), cap)
    m.bone('lens', 'head', (-1.5, -4.5, -4.4))
    m.cube('lens', (-1.5, -1.5, -0.6), (3, 3, 1), lens)
    for side, sx in (('right', -1), ('left', 1)):
        arm, fore = f'{side}_arm', f'{side}_forearm'
        m.bone(arm, 'body', (4.8 * sx, -9.5, 0), (-30, 0, 0))
        m.cube(arm, (-1.5, -1.5, -1.5), (3, 7, 3), moss_coat(), inflate=0.12)
        m.bone(fore, arm, (0, 5, 0), (-35, 0, 0))
        m.cube(fore, (-1.5, 0, -1.5), (3, 5, 3), moss_coat(), inflate=0.08)
        m.cube(fore, (-1.5, 5, -1.5), (3, 2, 3), shaded(ASH_SKIN, 3.6), mat='skin')
    m.bone('trinket', 'right_forearm', (0, 7, -0.5))
    m.cube('trinket', (-1, 0, -1), (2, 2, 2), trinket)
    # the pack: a frame taller than he is, loaded with everything the Underworld forgot
    m.bone('pack', 'body', (0, -9, 2.5))
    for px in (-3.5, 2.5):
        m.cube('pack', (px, -14, 0), (1, 23, 1), pack_frame)
    m.cube('pack', (-4.5, 1, -0.5), (9, 7, 6), crate)
    m.cube('pack', (-4, -6, 0), (8, 7, 5), sack, inflate=0.2)
    m.cube('pack', (-5.5, -9.5, 1), (11, 2, 2), scroll)
    m.cube('pack', (-6.5, -3, 1.5), (2, 3, 2), jar_on_pack(PURPLE))
    m.cube('pack', (4.5, -1, 1.5), (2, 3, 2), jar_on_pack(LENS))
    m.cube('pack', (-3, -14.5, -0.5), (6, 1, 2), pack_frame)
    m.bone('pack_lantern', 'pack', (2.5, -13.5, 0.5))
    m.cube('pack_lantern', (-0.5, 0, -0.5), (1, 2, 1), chain_links)
    m.cube('pack_lantern', (-1.5, 2, -1.5), (3, 4, 3), candle_lantern)
    m.paint()
    return m


COLLECTOR_POSE = {}


PREVIEW_POSES = {'rest': {}}


# ================================================================== Clatter, the Bonesmith (a little golem of bones and grave-stone)
GRAVESTONE = Ramp('#121117', '#1c1a22', '#27242e', '#332f3b', '#403b49', '#4e4858', '#5e5768', '#706979')


def stone(base=3.6, cracked=1):
    def paint(ctx):
        shaded(GRAVESTONE, base, grain(0.3, 2), smooth(0.5, 3.5), bevel(1.0, -0.9, -0.35), vgrad(0.25, -0.4))(ctx)
        if cracked and ctx.side:
            cracks(GRAVESTONE, cracked, 4, base - 2.4)(ctx)
    return paint


def golem_bone(ctx):
    shaded(BONE, 3.9, grain(0.3, 2), smooth(0.35, 3.0), bevel(0.6, -0.7, -0.35), vgrad(0.25, -0.5))(ctx)


def golem_face(ctx):
    """A head like a box of skulls: one wide brow, two square sockets lit from inside, a broken nose."""
    L = {'B': BONE.cols[5], 'b': BONE.cols[4], 'd': BONE.cols[3], 'D': BONE.cols[2], 'k': DARK_BONE.cols[0], 'K': DARK_BONE.cols[1],
         'E': SOUL.cols[5], 'e': SOUL.cols[3], 'n': DARK_BONE.cols[0], 'c': BONE.cols[2]}
    G = {'E': SOUL.cols[6], 'e': SOUL.cols[3]}
    ctx.stamp(["dbbBBBBbbd",
               "bdDDbbDDdb",
               "bKkKbbKkKb",
               "bkEebbeEkb",
               "bkeEbceEkb",
               "bKkKbbKkKb",
               "bbBbnnbBbb",
               "dbBbnnbBbd"], L, glow_legend=G)


def golem_head_side(ctx):
    golem_bone(ctx)
    if ctx.side:   # stitched together from more than one skull: seams down the sides
        for y in range(ctx.h):
            x = ctx.w // 2 + (1 if y % 3 == 0 else 0)
            if x < ctx.w:
                ctx.set(x, y, BONE.cols[2])


def golem_jaw(ctx):
    shaded(DARK_BONE, 4.6, grain(0.3, 2), bevel(0.5, -0.7, 0))(ctx)
    if ctx.face == 'front':
        for x in range(ctx.w):   # a row of big square teeth
            ctx.set(x, 0, BONE.cols[5] if x % 2 else BONE.cols[4])
            if ctx.h > 1:
                ctx.set(x, 1, BONE.cols[3] if x % 2 else DARK_BONE.cols[1])


def golem_ribs(ctx):
    L = {'B': BONE.cols[5], 'b': BONE.cols[4], 'd': BONE.cols[2], 'k': DARK_BONE.cols[0], 'K': DARK_BONE.cols[1], 'E': SOUL.cols[2]}
    if ctx.face in ('front', 'back'):
        rows = ["dbBBbbbbBBbd", "kKkkkBbkkkKk", "bBbbkBbkbbBb", "kkkkkBbkkkkk", "dbbbkBbkbbbd", "kkkkkBbkkkkk", "kkdbkbdkbdkk", "kkkkkBbkkkkk"]
        ctx.stamp([r[:ctx.w].center(ctx.w, 'k') for r in rows], L)
        if ctx.face == 'front':   # the soul that keeps him going, behind his ribs
            c = ctx.w // 2
            for y in (3, 4):
                ctx.set(c - 1, y, SOUL.cols[3]); ctx.emit(c - 1, y, SOUL.cols[4])
    else:
        shaded(BONE, 2.8, hstripes(1.4, 2), grain(0.3))(ctx)


def tongs(ctx):
    steel(IRON, 3.0, rivets=False, scratches=0)(ctx)


def quench_blade(ctx):
    """The blade he's working: still lit from the Mere."""
    for y in range(ctx.h):
        for x in range(ctx.w):
            col = STEEL.at(5.6 + ctx.light + ctx.noise.white(x, y, 5) * 0.5, x, y, 0)
            ctx.set(x, y, col)
            if (x + y) % 3 == 0:
                ctx.emit(x, y, SOUL.cols[2])


def build_bonesmith():
    m = Model('bonesmith', 128, 128)
    m.bone('root', None, (0, 24, 0))
    for side, sx in (('right', -1), ('left', 1)):   # stumps of legs under a lot of body
        m.bone(f'{side}_leg', 'root', (2.6 * sx, -7, 0))
        m.cube(f'{side}_leg', (-1.5, 0, -1.5), (3, 4, 3), golem_bone)
        m.cube(f'{side}_leg', (-2, 4, -2.8), (4, 3, 5), stone(3.4))
    m.bone('hips', 'root', (0, -7, 0))
    m.cube('hips', (-4, -3, -3), (8, 3, 6), stone(3.2))
    m.bone('body', 'hips', (0, -3, 0), (4, 0, 0))
    m.cube('body', (-5, -8, -3), (10, 8, 6), golem_ribs)
    m.cube('body', (-6, -9, -3.5), (12, 3, 7), stone(3.8), inflate=0.1)
    m.bone('head', 'body', (0, -8.5, -0.5))
    m.cube('head', (-5, -8, -4.5), (10, 8, 9), by_face(golem_head_side, front=golem_face, top=stone(3.6, 2), bottom=shaded(DARK_BONE, 1.6)))
    m.cube('head', (-5.5, -9, -5), (11, 2, 10), stone(4.0, 2), inflate=0.05)   # a slab of grave-stone for a brow
    m.bone('jaw', 'head', (0, -1, -1))
    m.cube('jaw', (-4, 0, -3.8), (8, 2, 7), golem_jaw)
    for side, sx in (('right', -1), ('left', 1)):   # long arms, big fists: he walks on his knuckles when he walks at all
        arm, fore, hand = f'{side}_arm', f'{side}_forearm', f'{side}_hand'
        m.bone(arm, 'body', (6.5 * sx, -7, 0))
        m.cube(arm, (-1, -1.5, -1), (2, 7, 2), golem_bone)
        m.cube(arm, (-2, -2.5, -2), (4, 3, 4), stone(3.8))
        m.bone(fore, arm, (0, 5.5, 0))
        m.cube(fore, (-2, 0, -2), (4, 6, 4), stone(3.4))
        m.bone(hand, fore, (0, 6, 0))
        m.cube(hand, (-2.5, 0, -2.5), (5, 3, 5), by_face(stone(3.2), bottom=golem_bone))
    m.bone('tongs', 'right_hand', (0, 2.5, -1))
    m.cube('tongs', (-0.5, -0.5, -9), (1, 1, 9), tongs)
    m.cube('tongs', (-0.5, -0.8, -16), (1, 2, 7), quench_blade)
    m.paint()
    return m


# arms forward and down, the tongs held out over the water
BONESMITH_POSE = {'right_arm': (-25, 0, 6, 0, 0, 0), 'right_forearm': (-30, 0, 0, 0, 0, 0), 'tongs': (85, 0, 0, 0, 0, 0), 'left_arm': (-8, 0, -8, 0, 0, 0),
                  'left_forearm': (-10, 0, 0, 0, 0, 0)}


def bake(m, pose):
    """Fold a pose into the bones' rest rotations, so the in-game mesh stands that way without animation."""
    for name, (rx, ry, rz, *_) in pose.items():
        b = m.by_name[name]
        b.rot = (b.rot[0] + rx, b.rot[1] + ry, b.rot[2] + rz)
    return m


def build(v):
    builder, pose = {'ferryman': (build_ferryman, FERRY_POSE), 'gravedigger': (build_gravedigger, DIGGER_POSE),
                     'prophet': (build_prophet, PROPHET_POSE), 'collector': (build_collector, COLLECTOR_POSE),
                     'bonesmith': (build_bonesmith, BONESMITH_POSE)}[v]
    return bake(builder(), pose)
