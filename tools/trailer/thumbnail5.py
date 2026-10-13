"""Three thumbnails to A/B test with YouTube's Test & Compare: each one big subject, a 2-3 word hook that adds to the title
("I Made My Dream Minecraft Mod (It's Terrifying): DeathBound"), the DEATHBOUND logo small, nothing in the bottom-right
corner (YouTube puts the video length there).

    python tools/trailer/thumbnail5.py   -> build/trailer5/thumb_{sees,behind,kneel}.png (1280x720)

Stills: shots.py t9x (the Hunter's face) and t9y (behind you, the King), filmed with Mellow.
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import text  # noqa: E402
import thumbnail4  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'build' / 'trailer5'
SHOTS = ROOT / 'run' / 'screenshots'


def hook(font, lines, scale, fill=(240, 234, 248), shade=(196, 176, 236)):
    """Big pixel-font words: bone white fading to lavender, a thick black outline, a drop shadow and a purple glow."""
    masks = [np.pad(font.mask(l), 2) for l in lines]
    w = max(m.shape[1] for m in masks)
    rows = []
    for m in masks:   # centre each line
        pad = w - m.shape[1]
        rows.append(np.pad(m, ((0, 0), (pad // 2, pad - pad // 2))))
    m = np.vstack(rows)
    rim = np.zeros_like(m)
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            rim |= np.roll(np.roll(m, dy, 0), dx, 1)
    h = m.shape[0]
    img = np.zeros((*m.shape, 4), np.uint8)
    img[rim] = (10, 6, 16, 255)
    for y in range(h):
        line_y = y % masks[0].shape[0]
        t = min(1.0, max(0.0, (line_y - 2) / 7))
        c = tuple(int(a + (b - a) * t) for a, b in zip(fill, shade))
        img[y][m[y]] = (*c, 255)
    big = Image.fromarray(img).resize((m.shape[1] * scale, h * scale), Image.NEAREST)
    pad = 40
    layer = Image.new('RGBA', (big.width + 2 * pad, big.height + 2 * pad), (0, 0, 0, 0))
    shadow = Image.new('RGBA', big.size, (0, 0, 0, 0))
    shadow.putalpha(big.split()[3].point(lambda v: int(v * 0.8)))
    layer.alpha_composite(shadow, (pad + scale, pad + scale))
    layer.alpha_composite(big, (pad, pad))
    out = Image.new('RGBA', layer.size, (0, 0, 0, 0))
    out.alpha_composite(thumbnail4._glow(layer, 16, (140, 70, 255, 0), 0.9) if hasattr(thumbnail4, '_glow') else text._glow(layer, 16, (140, 70, 255, 0), 0.9))
    out.alpha_composite(layer)
    return out


def still(name, box, lift, glow=0.0, side='right'):
    img = Image.open(SHOTS / f'{name}.png').convert('RGB').crop(box).resize((1280, 720), Image.LANCZOS)
    img = thumbnail4.grade(img, lift, glow)
    return thumbnail4.shade_side(img, side).convert('RGBA') if side else img.convert('RGBA')


def logo(font, width):
    art = thumbnail4.logo_art(font, 15)
    return art.resize((width, round(art.height * width / art.width)), Image.LANCZOS)


def build():
    font = text.PixelFont(edit.font_sheet())
    OUT.mkdir(parents=True, exist_ok=True)

    # A: the Hunter's face, straight on; the words on the dark side
    img = still('t9x_hunter_d19', (391, 60, 1920, 920), 1.5)
    words = hook(font, ['IT SEES', 'YOU'], 13)
    img.alpha_composite(words, (1280 - words.width + 10, 150))
    lg = logo(font, 330)
    img.alpha_composite(lg, (1280 - lg.width - 60, 60))
    img.convert('RGB').save(OUT / 'thumb_sees.png')

    # B: behind you: the player facing us, his eye over their shoulder
    img = still('t9y_behind_d', (380, 250, 1513, 887), 1.7, side=None)   # his antlers in, the player to the knees
    words = hook(font, ['BEHIND', 'YOU'], 12)
    img.alpha_composite(words, (1280 - words.width - 10, 110))
    lg = logo(font, 300)
    img.alpha_composite(lg, (40, 36))
    img.convert('RGB').save(OUT / 'thumb_behind.png')

    # C: the Death King on his throne, close
    img = still('t9y_king_level', (380, 150, 1700, 893), 1.15, side=None)   # hood, ribs and the throne's arms; not blown out
    words = hook(font, ['KNEEL.'], 14)
    img.alpha_composite(words, (-20, 720 - words.height + 10))
    lg = logo(font, 330)
    img.alpha_composite(lg, (1280 - lg.width - 40, 36))
    img.convert('RGB').save(OUT / 'thumb_kneel.png')
    print('wrote', [str(OUT / f'thumb_{n}.png') for n in ('sees', 'behind', 'kneel')])


if __name__ == '__main__':
    build()
