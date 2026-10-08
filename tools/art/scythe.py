"""The Reaper's Scythe sprites: the original 16px pixel design (the same sprite is held in hand), now shaded by hand-picked tones.

Every blade pixel is toned by where it sits in the band: the pixel on the inner (cutting) edge is the brightest, the one
behind it a step down, the outer spine dark with a rim of light where it faces up-left, the body a mid steel.
"""
import numpy as np
import sprites as sp
from core import hx

STEEL = {'a': '#211f29', 'b': '#363542', 'c': '#504f5f', 'd': '#757485', 'e': '#a6a5b4', 'f': '#dddce6'}
BLADE = set('Sz')

ICON = ["...KKKKKKK......",
        ".KKSSSSSSzKK....",
        "KSSSzzzzzzzzK...",
        "KSzzKKKKKKKzzK..",
        "KzKK......KKzzK.",
        "KK.........KKzzK",
        "............KgVK",
        "...........KrvRK",
        "..........KrhK..",
        ".........KrhK...",
        "........KrhK....",
        ".......KsSK.....",
        "......KrhK......",
        ".....KrhK.......",
        "....KshK........",
        "....KKK........."]


def _shade(rows, center):
    """Re-tone the blade pixels of a sprite map; everything else keeps its letter."""
    h, w = len(rows), len(rows[0])
    grid = [list(r) for r in rows]
    blade = lambda x, y: 0 <= x < w and 0 <= y < h and rows[y][x] in BLADE
    cx, cy = center
    for y in range(h):
        for x in range(w):
            if rows[y][x] not in BLADE:
                continue
            vx, vy = cx - x, cy - y                     # toward the hollow of the curve
            n = max(abs(vx), abs(vy)) or 1
            ix, iy = round(vx / n), round(vy / n)       # one pixel inward
            if not blade(x + ix, y + iy):
                tone = 'f' if y < cy - 2 else 'e'       # the honed edge, brightest where it catches the light
            elif not blade(x + 2 * ix, y + 2 * iy):
                tone = 'd'
            elif not blade(x - ix, y - iy):
                tone = 'c' if (-ix) + (-iy) < 0 else 'b'  # spine: lit where it faces up-left, dark elsewhere
            else:
                tone = 'c' if y < cy - 4 else 'b'       # the body, darker toward the root
            grid[y][x] = tone
    return [''.join(r) for r in grid]


def _legend():
    leg = dict(sp.L)
    leg.update({k: hx(v) for k, v in STEEL.items()})
    return leg


def icon():
    return sp.stamp(sp.canvas(), _shade(ICON, (5, 9)), _legend())
