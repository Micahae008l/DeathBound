"""Builds Aldous's Lantern from Minecraft's own lantern: the vanilla model and texture, with a softer,
golden-white light instead of the orange one, in four flame states: lit (0 hits), dim (1), low (2), out (3).
Reads the vanilla texture from the Loom cache: run after a Gradle build."""
import colorsys
import glob
import io
import json
import os
import zipfile
from PIL import Image

R = 'src/main/resources/assets/deathbound'
JAR = sorted(glob.glob(os.path.expanduser('~/.gradle/caches/fabric-loom/*/minecraft-client.jar')))[-1]


def vanilla(path):
    with zipfile.ZipFile(JAR) as z:
        return z.read(path)


def is_light(r, g, b):
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return (h * 360 < 70 or h * 360 > 340) and s > 0.12 and v > 0.45


def recolor(src, state):
    im = src.copy()
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a == 0 or not is_light(r, g, b):
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if state == 'out':
                # cold glass: the metal's blue-grey, dark
                nr, ng, nb = colorsys.hsv_to_rgb(222 / 360, 0.25, 0.16 + v * 0.14)
            else:
                # softer and warmer-white than vanilla: pull orange toward gold, less saturated, a touch brighter
                h = min(h * 360 + 14, 52) / 360
                s *= 0.62
                v = min(1.0, v * 1.06)
                v *= {'lit': 1.0, 'dim': 0.78, 'low': 0.55}[state]
                if state == 'low':
                    h = max(h * 360 - 12, 18) / 360
                    s = min(1.0, s * 1.4)
                nr, ng, nb = colorsys.hsv_to_rgb(h, s, v)
            px[x, y] = (round(nr * 255), round(ng * 255), round(nb * 255), a)
    return im


def model(state):
    template = json.loads(vanilla('assets/minecraft/models/block/template_lantern.json'))
    elements = template['elements']
    if state != 'out':
        elements[0]['light_emission'] = 15
    return {
        'textures': {'lantern': f'deathbound:item/aldous_lantern_{state}', 'particle': f'deathbound:item/aldous_lantern_{state}'},
        'elements': elements,
        'display': {
            'gui': {'rotation': [30, 225, 0], 'translation': [0, 2, 0], 'scale': [0.9, 0.9, 0.9]},
            'ground': {'rotation': [0, 0, 0], 'translation': [0, 3, 0], 'scale': [0.5, 0.5, 0.5]},
            'fixed': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.8, 0.8, 0.8]},
            'head': {'rotation': [0, 0, 0], 'translation': [0, 13, 0], 'scale': [0.8, 0.8, 0.8]},
            # in the hand frame +y is forward and +z runs up the arm: tip it 90 degrees so it hangs from the fist
            'thirdperson_righthand': {'rotation': [90, 45, 0], 'translation': [0, 0.5, -2], 'scale': [0.6, 0.6, 0.6]},
            'thirdperson_lefthand': {'rotation': [90, 45, 0], 'translation': [0, 0.5, -2], 'scale': [0.6, 0.6, 0.6]},
            'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [1.5, 4, -1], 'scale': [0.5, 0.5, 0.5]},
            'firstperson_lefthand': {'rotation': [0, 45, 0], 'translation': [1.5, 4, -1], 'scale': [0.5, 0.5, 0.5]},
        },
    }


if __name__ == '__main__':
    src = Image.open(io.BytesIO(vanilla('assets/minecraft/textures/block/lantern.png'))).convert('RGBA')
    meta = vanilla('assets/minecraft/textures/block/lantern.png.mcmeta')
    names = {'lit': 'aldous_lantern', 'dim': 'aldous_lantern_dim', 'low': 'aldous_lantern_low', 'out': 'aldous_lantern_out'}
    for state, name in names.items():
        out = f'{R}/textures/item/aldous_lantern_{state}.png'
        recolor(src, state).save(out)
        with open(out + '.mcmeta', 'wb') as f:
            f.write(meta)   # keeps the vanilla flicker
        with open(f'{R}/models/item/{name}.json', 'w') as f:
            json.dump(model(state), f, indent=2)
            f.write('\n')
    ref = lambda n: {'type': 'minecraft:model', 'model': f'deathbound:item/{n}'}
    items = {'model': {
        'type': 'minecraft:range_dispatch', 'property': 'minecraft:custom_model_data', 'index': 0,
        'fallback': ref('aldous_lantern'),
        'entries': [{'threshold': 1, 'model': ref('aldous_lantern_dim')},
                    {'threshold': 2, 'model': ref('aldous_lantern_low')},
                    {'threshold': 3, 'model': ref('aldous_lantern_out')}]}}
    with open(f'{R}/items/aldous_lantern.json', 'w') as f:
        json.dump(items, f, indent=2)
        f.write('\n')
    print('ok')
