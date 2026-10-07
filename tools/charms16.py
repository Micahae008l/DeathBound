"""Draws the 16px textures for the Collector's and Phantom charms
in the palette of the existing charms (dark outline, light from the top left, soul-purple accents)."""
import sys
from PIL import Image

PAL = {
    '.': None,
    'O': '120e18',  # outline
    'v': '1c1026',  # void / dark glass
    'D': '2a2232',  # wick, deep shade
    'm': '433d4c',  # cloth / dark iron
    'n': '5d5568',  # cloth highlight
    'i': '55536a',  # iron shade
    'h': 'c9c7d6',  # iron / glass highlight
    'W': 'efe8d6',  # bone / wax light
    'B': 'e2d9c3',  # bone / wax
    'b': 'b9ad96',  # bone shade
    'c': '857a6a',  # bone deep shade
    'w': '7d5233',  # wood light
    'd': '5e3a24',  # wood
    'e': '3a2418',  # wood dark
    'R': 'e3ccff',  # soul brightest
    'Q': 'c79bff',
    'P': 'a868f0',
    'q': '7a3cc2',
    'p': '4c1d7a',
    'x': '2e0f4f',  # soul glow on dark
    'F': 'fff6d8',  # flame core
    'f': 'f0d68a',  # flame
    'y': 'd8b257',  # flame edge
    'g': 'a9792e',  # flame deep
    'G': 'd9d0ee',  # ghost light
    'H': 'b8aed6',  # ghost
    'J': '8a7fae',  # ghost shade
    'K': '5a5078',  # ghost deep shade
    'j': '8a7fae99',  # fading tail
    'k': '5a507899',
    'z': '120e1899',
}

ART = {
    'collectors_charm': [
        "................",
        "......OOOO......",
        ".....OwwwdO.....",
        ".....OddeeO.....",
        "....OOOOOOOO....",
        "....OhhnnnmO....",
        "...OOOOOOOOOO...",
        "..OhvvvvvvvvmO..",
        "..Ohvvvxxvvvmo..",
        "..Ohvvxqqxvvmo..",
        "..OvvxPQRQPxmO..",
        "..OvvxPRRRPxmO..",
        "..OvvxqPQPqxmO..",
        "..Ovvvxqqqxvmo..",
        "...OmmmmmmmmO...",
        "....OOOOOOOO....",
    ],
    'phantom_charm': [
        "................",
        ".....OOOOOO.....",
        "....OGGGGHHO....",
        "...OGGGGHHHJO...",
        "...OGvvGHvvJO...",
        "..OGGvvGHvvJJO..",
        "..OGGPvHHvPJJO..",
        ".OGOHHHvvHHJOJO.",
        ".OGOHHvvvvHJOJO.",
        "..OOHHvvvvJJOO..",
        "...OHHHvvJJJO...",
        "...OHHJJJJJKO...",
        "....OHJJJKKKO...",
        "....OJJOKKOKO...",
        "....OjO.OkOzO...",
        ".....z...z..z...",
    ],
}


def draw(rows):
    assert len(rows) == 16, len(rows)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row, len(row))
        for x, ch in enumerate(row):
            ch = 'O' if ch == 'o' else ch
            c = PAL[ch]
            if c:
                alpha = int(c[6:8], 16) if len(c) == 8 else 255
                im.putpixel((x, y), tuple(int(c[i:i + 2], 16) for i in (0, 2, 4)) + (alpha,))
    return im


if __name__ == '__main__':
    out = sys.argv[1] if len(sys.argv) > 1 else 'src/main/resources/assets/deathbound/textures/item'
    for name, rows in ART.items():
        draw(rows).save(f'{out}/{name}.png')
        print('wrote', name)
