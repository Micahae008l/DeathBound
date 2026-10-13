"""YouTube thumbnail for trailer 4: the Hollow Hunter's face up close (his one glowing eye under the hood, the antlers),
the DEATHBOUND logo with its BETA splash beside it. 'hunter' is the one in use (Michael's pick); the rest are
alternatives.

    python tools/trailer/thumbnail4.py   -> build/trailer4/thumbnail_<variant>.png (1280x720)

Frames come from the trailer's footage (run/trailer/t9_*.mp4, shots.py t9a..t9o) or the stills of shots.py t9t.
"""
import sys
from pathlib import Path

from PIL import Image, ImageEnhance, ImageFilter

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import text  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / 'build' / 'trailer4'
# name: (clip and second, or a still from shots.py t9t/t9u; crop box in the 1920x1080 frame (16:9); logo 'right' or
# 'bottom'; how far the shadows are lifted)
VARIANTS = {
    'hunter': (('still', 't9v_d19'), (391, 60, 1920, 920), 'right', 1.7),   # straight on, level with his eye: a stare
    'hunter_below': (('still', 't9t_hunter_face'), (428, 150, 1868, 960), 'right', 2.0),
    'hunter_eye_level': (('still', 't9u_eye_level'), (360, 150, 1800, 960), 'right', 1.8),
    'king': (('still', 't9t_king_close'), (420, 60, 1860, 870), 'right', 1.4),
    'shade': (('t9_sentry', 5.2), (0, 140, 1600, 1040), 'right', 1.4),
    'soul': (('t9_soul', 2.5), (240, 60, 1680, 870), 'bottom', 1.4),
}


def frame(source):
    clip, at = source
    if clip == 'still':   # a screenshot from the film harness (sharper than a frame of the video)
        return Image.open(ROOT / 'run' / 'screenshots' / f'{at}.png').convert('RGB')
    path = OUT / f'thumb_{clip}_{at}.png'
    edit.ff('-ss', at, '-i', ROOT / 'run' / 'trailer' / f'{clip}.mp4', '-frames:v', 1, path)
    return Image.open(path).convert('RGB')


def grade(img, lift):
    """Brighter and punchier than the film (a thumbnail is seen small, in daylight), still dark around the subject."""
    img = img.point(lambda v: int(255 * (v / 255) ** (1 / lift)))
    img = img.point(lambda v: max(0, int((v - 14) * 255 / 241)))   # crush the haze back to black
    img = ImageEnhance.Contrast(img).enhance(1.4)
    img = ImageEnhance.Color(img).enhance(1.15)   # more and bone turns pink; the purple glow holds up on its own
    glow = img.point(lambda v: max(0, v - 120) * 2).filter(ImageFilter.GaussianBlur(18))
    return Image.blend(img, Image.composite(glow, img, glow.convert('L')), 0.35)


def shade_side(img, side):
    """Fade to black where the logo sits, plus a vignette."""
    w, h = img.size
    mask = Image.new('L', (w, h), 0)
    px = mask.load()
    for y in range(h):
        for x in range(w):
            if side == 'right':
                a = max(0.0, (x / w - 0.42) / 0.38)
            else:
                a = max(0.0, (y / h - 0.55) / 0.35)
            dx, dy = (x / w - 0.5) * 2, (y / h - 0.5) * 2
            a = min(1.0, a * 0.85 + max(0.0, (dx * dx + dy * dy) ** 0.5 - 0.6) * 1.1)
            px[x, y] = int(255 * min(1.0, a))
    return Image.composite(Image.new('RGB', (w, h), (4, 2, 8)), img, mask)


def logo_art(font, scale):
    """The logo (with BETA tipped over its top right) cut out of its full-frame canvas."""
    logo, _ = text.logo(font, scale=scale)
    tag = text.splash(font, 'BETA', scale=max(3, scale // 3))
    x0, y0, x1, y1 = text.logo_box(font, scale=scale)
    logo.alpha_composite(tag, (x1 - tag.width // 2 - scale, y0 - tag.height // 2 - scale))
    return logo.crop(logo.getbbox())


def build(name):
    source, box, side, lift = VARIANTS[name]
    img = frame(source).crop(box).resize((1280, 720), Image.LANCZOS)
    img = shade_side(grade(img, lift), side).convert('RGBA')
    font = text.PixelFont(edit.font_sheet())
    art = logo_art(font, 15)
    if side == 'right':
        art = art.resize((570, round(art.height * 570 / art.width)), Image.LANCZOS)
        img.alpha_composite(art, (1280 - art.width - 24, (720 - art.height) // 2 - 10))
    else:
        art = art.resize((860, round(art.height * 860 / art.width)), Image.LANCZOS)
        img.alpha_composite(art, ((1280 - art.width) // 2, 720 - art.height - 26))
    out = OUT / f'thumbnail_{name}.png'
    img.convert('RGB').save(out)
    print('wrote', out)


if __name__ == '__main__':
    for n in sys.argv[1:] or VARIANTS:
        build(n)
