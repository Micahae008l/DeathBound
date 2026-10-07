"""Soulwater: the Mere's water. Vanilla water's own ripples (so it moves like water), run through a palette of black-violet
ink with lilac on the crests, and now and then a soul-light glinting up through it and going out again. Glints are
placed per loop so the animation still loops (the flow's ride down with the current, a pixel a frame like vanilla's)."""
import io
import math
import zipfile
import colorsys
import numpy as np
from PIL import Image

FRAMES = 32
# luminance of vanilla's grey water -> ours: the troughs nearly black, the crests a cold lilac
STOPS = [(0.00, (16, 10, 36)), (0.60, (27, 17, 58)), (0.90, (39, 25, 82)), (1.00, (64, 45, 120))]


def _palette(lum):
    out = np.zeros(lum.shape + (3,))
    for (a, ca), (b, cb) in zip(STOPS, STOPS[1:]):
        m = (lum >= a) & (lum <= b)
        k = ((lum - a) / (b - a))[m][:, None]
        out[m] = np.array(ca) * (1 - k) + np.array(cb) * k
    return out


def sheet(jar_path, flow=False):
    name = 'water_flow' if flow else 'water_still'
    with zipfile.ZipFile(jar_path) as z:
        src = np.array(Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/block/{name}.png'))).convert('RGBA')).astype(float)
    n = src.shape[1]
    lum = src[..., :3].mean(-1)
    lum = (lum - lum.min()) / (lum.max() - lum.min())
    rgb = _palette(lum)
    alpha = 204 + lum * 32
    img = np.concatenate([rgb, alpha[..., None]], -1)
    # soul-lights: a few per tile, each rising out of the dark and fading over six frames
    rng = np.random.default_rng(11 if flow else 5)
    for _ in range(6 if flow else 4):
        x0, y0, phase = rng.integers(0, n), rng.integers(0, n), rng.integers(0, FRAMES)
        for k in range(6):
            f = (phase + k) % FRAMES
            glow = math.sin(math.pi * (k + 0.5) / 6)
            y = (y0 + f) % n if flow else y0
            for dx, dy, w in ((0, 0, 1.0), (1, 0, 0.4), (-1, 0, 0.4), (0, 1, 0.4), (0, -1, 0.4)):
                xx, yy = (x0 + dx) % n, f * n + (y + dy) % n
                c = glow * w
                img[yy, xx] = img[yy, xx] * (1 - c) + np.array([216, 186, 255, 250]) * c
    return np.clip(img, 0, 255).astype(np.uint8)


def bucket(jar_path):
    with zipfile.ZipFile(jar_path) as z:
        im = Image.open(io.BytesIO(z.read('assets/minecraft/textures/item/water_bucket.png'))).convert('RGBA')
    px = im.load()
    for yy in range(im.size[1]):
        for xx in range(im.size[0]):
            r, g, b, a = px[xx, yy]
            if a and b > r + 25:   # the water in it
                h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
                rr, gg, bb = colorsys.hsv_to_rgb(0.76, min(1, s * 1.1), v * 0.62)
                px[xx, yy] = (int(rr * 255), int(gg * 255), int(bb * 255), a)
    return np.array(im)


if __name__ == '__main__':   # a look at it: still and flow tiled, a few frames side by side
    import os
    import sys
    jar = os.path.expanduser('~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar')
    s, fl = sheet(jar), sheet(jar, True)
    assert s.shape == (16 * FRAMES, 16, 4) and fl.shape == (32 * FRAMES, 32, 4)
    tiles = []
    for f in (0, 8, 16, 24):
        fr = s[f * 16:(f + 1) * 16]
        tiles.append(np.tile(fr, (4, 4, 1)))
    out = Image.fromarray(np.concatenate(tiles, 1)).resize((64 * 4 * 6, 64 * 6), Image.NEAREST)
    bg = Image.new('RGBA', out.size, (40, 40, 40, 255))
    bg.alpha_composite(out)
    bg.save(sys.argv[1] if len(sys.argv) > 1 else 'soulwater_preview.png')
