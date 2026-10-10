"""Title art for the trailer: the pixel logo, serif story cards, and boss lines in the game's own chat font."""
import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont

W, H = 1920, 1080
SERIF = 'C:/Windows/Fonts/BASKVILL.TTF'


class PixelFont:
    """Minecraft's ascii.png: 16x16 cells of 8x8, glyph width from its rightmost lit column."""

    def __init__(self, path):
        sheet = Image.open(path).convert('RGBA')
        self.glyphs = {}
        for code in range(32, 127):
            cell = sheet.crop((code % 16 * 8, code // 16 * 8, code % 16 * 8 + 8, code // 16 * 8 + 8))
            alpha = np.array(cell)[:, :, 3] > 0
            cols = np.where(alpha.any(axis=0))[0]
            width = cols.max() + 1 if len(cols) else 3
            self.glyphs[chr(code)] = (alpha[:, :width], width)

    def mask(self, text):
        """1-bit pixel mask of a line, one cell per font pixel."""
        width = sum(self.glyphs[c][1] + 1 for c in text) - 1
        m = np.zeros((8, width), bool)
        x = 0
        for c in text:
            g, w = self.glyphs[c]
            m[:, x:x + w] |= g
            x += w + 1
        return m


def _scaled(mask, s):
    return np.kron(mask, np.ones((s, s), bool))


def _glow(layer, radius, color, strength):
    a = layer.split()[3].filter(ImageFilter.GaussianBlur(radius))
    a = a.point(lambda v: min(255, int(v * strength)))
    g = Image.new('RGBA', layer.size, color)
    g.putalpha(a)
    return g


def logo_box(font, text='DEATHBOUND', scale=15):
    """Where logo() puts its letters: (x0, y0, x1, y1) in the frame."""
    rows, cols = (n + 4 for n in font.mask(text).shape)
    w, h = cols * scale, rows * scale
    x0, y0 = (W - w) // 2, (H - h) // 2 - 40
    return x0, y0, x0 + w, y0 + h


def splash(font, word, scale=5, angle=16, color=(214, 186, 255)):
    """A small word in the logo's pixel font, tipped up to the right like Minecraft's splash text, with a soft glow."""
    m = np.pad(font.mask(word), 2)
    rim = np.zeros_like(m)
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            rim |= np.roll(np.roll(m, dy, 0), dx, 1)
    img = np.zeros((*m.shape, 4), np.uint8)
    img[rim] = (14, 10, 22, 255)
    img[m] = (*color, 255)
    big = Image.fromarray(img).resize((m.shape[1] * scale, m.shape[0] * scale), Image.NEAREST)
    pad = 40
    layer = Image.new('RGBA', (big.width + 2 * pad, big.height + 2 * pad), (0, 0, 0, 0))
    layer.paste(big, (pad, pad))
    out = _glow(layer, 9, (150, 80, 255, 0), 1.0)
    out.alpha_composite(layer)
    return out.rotate(angle, resample=Image.BICUBIC, expand=True)


def logo(font, text='DEATHBOUND', scale=15):
    """Pale stone letters, lit from above, cut out with a dark rim and a soul-purple glow."""
    m = font.mask(text)
    pad = 2
    m = np.pad(m, pad)
    rim = np.zeros_like(m)
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            rim |= np.roll(np.roll(m, dy, 0), dx, 1)
    rows, cols = m.shape
    img = np.zeros((rows, cols, 4), np.uint8)
    # stone ramp, light at the top of each letter to dark at its foot
    ramp = [(236, 228, 246), (214, 203, 232), (190, 177, 214), (166, 151, 196), (142, 126, 176), (120, 103, 156), (100, 84, 138), (84, 68, 120)]
    for y in range(rows):
        for x in range(cols):
            if m[y, x]:
                c = ramp[min(7, max(0, y - pad))]
                if not m[y - 1, x]:  # top edges catch the light
                    c = tuple(min(255, v + 18) for v in c)
                if not m[y + 1, x] or not m[y, x + 1]:  # lower and right edges fall in shadow
                    c = tuple(int(v * 0.78) for v in c)
                img[y, x] = (*c, 255)
            elif rim[y, x]:
                img[y, x] = (14, 10, 22, 255)
    big = Image.fromarray(img).resize((cols * scale, rows * scale), Image.NEAREST)
    canvas = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    x0, y0 = (W - big.width) // 2, (H - big.height) // 2 - 40
    shadow = Image.new('RGBA', big.size, (0, 0, 0, 0))
    shadow.putalpha(big.split()[3].point(lambda v: int(v * 0.7)))
    layer = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    layer.paste(big, (x0, y0))
    canvas.alpha_composite(_glow(layer, 38, (122, 60, 255, 0), 1.1))
    canvas.alpha_composite(_glow(layer, 10, (170, 120, 255, 0), 0.9))
    canvas.alpha_composite(shadow, (x0 + scale, y0 + scale))
    canvas.alpha_composite(big, (x0, y0))
    return canvas, y0 + big.height


def card(lines, size=58, spacing=0.22, color=(232, 226, 240), y=None, glow=True):
    """Spaced-out serif capitals, centred: the story told between the shots."""
    f = ImageFont.truetype(SERIF, size)
    canvas = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(canvas)
    lh = int(size * 1.5)
    top = (H - lh * len(lines)) // 2 if y is None else y
    for i, line in enumerate(lines):
        widths = [d.textlength(c, font=f) for c in line]
        total = sum(widths) + spacing * size * (len(line) - 1)
        x = (W - total) / 2
        for c, w in zip(line, widths):
            d.text((x, top + i * lh), c, font=f, fill=(*color, 255))
            x += w + spacing * size
    if glow:
        out = Image.new('RGBA', (W, H), (0, 0, 0, 0))
        out.alpha_composite(_glow(canvas, 14, (110, 60, 220, 0), 0.6))
        out.alpha_composite(canvas)
        return out
    return canvas


def chat(font, speaker, line, scale=4, y=900, speakerless=False):
    """A boss line the way the game prints it: name in purple, words in white, with the hard drop shadow."""
    parts = [(line, (214, 196, 246))] if speakerless else [(speaker + ': ', (190, 140, 255)), (line, (240, 236, 246))]
    masks = [(font.mask(t), c) for t, c in parts]
    width = sum(m.shape[1] + 1 for m, _ in masks) * scale
    canvas = Image.new('RGBA', (W, H), (0, 0, 0, 0))
    x = (W - width) // 2
    if not speakerless:
        plate = Image.new('RGBA', (width + 12 * scale, 8 * scale + 8 * scale), (0, 0, 0, 120))
        canvas.alpha_composite(plate, (x - 6 * scale, y - 4 * scale))
    for m, color in masks:
        big = _scaled(m, scale)
        for off, col in ((scale, tuple(v // 4 for v in color)), (0, color)):
            rgba = np.zeros((*big.shape, 4), np.uint8)
            rgba[big] = (*col, 255)
            canvas.alpha_composite(Image.fromarray(rgba), (x + off, y + off))
        x += (m.shape[1] + 1) * scale
    return canvas
