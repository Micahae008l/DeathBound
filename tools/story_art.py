"""16px art for the stories: the paper of a pinned/lying note (block/story_note) and the Lost Journal item (item/story_book)."""
from PIL import Image

R = 'src/main/resources/assets/deathbound/textures'


def hexa(h, a=255):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (a,)


PAPER = {
    '.': None,
    'p': hexa('e6d8b8'),  # paper
    'P': hexa('efe4c8'),  # paper, lit
    's': hexa('cdb98f'),  # paper, shaded edge
    'S': hexa('b39f78'),  # paper, deep edge / fold
    'i': hexa('3d2c1e'),  # ink
    'I': hexa('6b5340'),  # faded ink
    't': hexa('a3804e'),  # tea stain
}
NOTE = [
    "..ssssssssssss..",
    ".sPPPPPPPPPPPPs.",
    ".sPPiiiIPPPPPps.",
    ".sPPPPPPPPPPPps.",
    ".sPiiIiiiIiiPps.",
    ".sPPPPPPPPPPPps.",
    ".sPIiiiiPiiiPps.",
    ".spPPPPPPPPPpps.",
    ".spiiIiiPiiIpps.",
    ".sppPPPPPPPppts.",
    ".sppiiiIiippttS.",
    ".spppppppppptts.",
    ".sppppIiiiippps.",
    ".sppppppppppppS.",
    ".SsssssssssSS...",
    "..SSSSSSSSS.....",
]

BOOK = {
    '.': None,
    'O': hexa('1c120c'),  # outline
    'L': hexa('7d5233'),  # leather lit
    'l': hexa('5e3a24'),  # leather
    'd': hexa('3a2418'),  # leather dark / spine
    'w': hexa('e2d9c3'),  # page edges
    'W': hexa('b9ad96'),  # page edge shade
    'g': hexa('d8b257'),  # brass clasp
    'G': hexa('a9792e'),
    'r': hexa('a868f0'),  # bookmark ribbon
    'R': hexa('7a3cc2'),
}
JOURNAL = [
    "................",
    "...OOOOOOOOOO...",
    "..OdLLLLLLLLwO..",
    "..OdLLLLLLLlwO..",
    "..OdLlLLLLLlwO..",
    "..OdLLLllLLlWO..",
    "..OdLLLLLLLlwO..",
    "..OdLLLLLLllwO..",
    "..OdLlLLLllggOO.",
    "..OdLLLLLLlGgdO.",
    "..OdLLLLLlllwOO.",
    "..OdLLLLLlllwO..",
    "..OdllLLllllWO..",
    "..OddlllllldwO..",
    "...OOOrROOOOO...",
    "......rR........",
]


def draw(rows, pal):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row)
        for x, ch in enumerate(row):
            if pal[ch]:
                im.putpixel((x, y), pal[ch])
    return im


if __name__ == '__main__':
    draw(NOTE, PAPER).save(f'{R}/block/story_note.png')
    draw(JOURNAL, BOOK).save(f'{R}/item/story_book.png')
    print('ok')
