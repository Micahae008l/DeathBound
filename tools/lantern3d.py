"""Builds the 3D Aldous's Lantern: one 16px texture sheet per flame state and the matching item models.
States: lit (0 hits), dim (1), low (2), out (3). The glass glows (light_emission) while any flame is left."""
import json
from PIL import Image

R = 'src/main/resources/assets/deathbound'
BRASS = {'H': 'f0d68a', 'B': 'd8b257', 'b': 'a9792e', 'd': '6b4a1e', 'O': '3f2a12'}
IRON = {'n': '5d5568', 'm': '433d4c', 'D': '2a2232'}
# glass colours per state: (edge, inner, flame edge, flame core)
GLASS = {
    'lit': ('e9a43c', 'f6c25a', 'fff0b8', 'fffbea'),
    'dim': ('c98a32', 'e0a646', 'f8dc8c', 'fff3c8'),
    'low': ('8a5a26', 'a8702e', 'e8b85a', 'f8dc8c'),
    'out': ('2a2232', '3a3040', None, None),
}
# 4x6 glass: '.' inner, 'e' edge, 'f' flame edge, 'F' flame core
FLAME = {
    'lit': ["e..e", "..f.", ".fF.", ".FF.", ".fFf", "e..e"],
    'dim': ["e..e", "....", "..f.", ".fF.", ".fF.", "e..e"],
    'low': ["e..e", "....", "....", "..f.", ".fF.", "e..e"],
    'out': ["e..e", "....", "....", "....", "....", "e.he"],
}


def hexrgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def sheet(state):
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    put = lambda x, y, c: im.putpixel((x, y), hexrgb(c))
    edge, inner, fedge, fcore = GLASS[state]
    # side face (0,0)-(6,8): brass frame round the glass
    for x in range(6):
        put(x, 0, BRASS['B'] if x < 5 else BRASS['b'])
        put(x, 7, BRASS['d'] if x > 0 else BRASS['b'])
    for y in range(1, 7):
        put(0, y, BRASS['b'])
        put(5, y, BRASS['d'])
        for x in range(4):
            ch = FLAME[state][y - 1][x]
            c = {'.': inner, 'e': edge, 'f': fedge or inner, 'F': fcore or inner, 'h': '5d5568'}[ch]
            put(1 + x, y, c)
    # brass plate (6,0)-(12,6) with a dark vent in the middle
    for y in range(6):
        for x in range(6):
            c = BRASS['d'] if x in (0, 5) or y in (0, 5) else BRASS['B']
            if (x, y) in ((1, 1), (2, 1), (1, 2)):
                c = BRASS['H']
            if 2 <= x <= 3 and 2 <= y <= 3:
                c = BRASS['O']
            put(6 + x, y, c)
    # iron handle (12,0)-(15,2)
    for x in range(3):
        put(12 + x, 0, IRON['n'])
    put(12, 1, IRON['m'])
    put(14, 1, IRON['m'])
    return im


def face(uv, tex='#lantern'):
    return {'uv': uv, 'texture': tex}


def model(state):
    brass_side = face([6, 0, 12, 1])
    brass_top = face([6, 0, 12, 6])
    glow = 15 if state != 'out' else 0
    body = {'from': [5, 1, 5], 'to': [11, 9, 11],
            'faces': {d: face([0, 0, 6, 8]) for d in ('north', 'south', 'east', 'west')}}
    body['faces']['up'] = brass_top
    body['faces']['down'] = brass_top
    if glow:
        body['light_emission'] = glow
    plate = lambda f, t: {'from': f, 'to': t, 'faces': {
        **{d: brass_side for d in ('north', 'south', 'east', 'west')}, 'up': brass_top, 'down': brass_top}}
    handle = lambda angle: {'from': [6.5, 12, 8], 'to': [9.5, 14, 8],
                            'rotation': {'origin': [8, 12, 8], 'axis': 'y', 'angle': angle},
                            'faces': {'north': face([12, 0, 15, 2]), 'south': face([12, 0, 15, 2])}}
    return {
        'textures': {'lantern': f'deathbound:item/aldous_lantern_{state}', 'particle': f'deathbound:item/aldous_lantern_{state}'},
        'elements': [
            plate([4.5, 0, 4.5], [11.5, 1, 11.5]),
            body,
            plate([4.5, 9, 4.5], [11.5, 10, 11.5]),
            plate([5.5, 10, 5.5], [10.5, 11, 10.5]),
            plate([7, 11, 7], [9, 12, 9]),
            handle(45),
            handle(-45),
        ],
        'display': {
            'gui': {'rotation': [30, 225, 0], 'translation': [0, -1, 0], 'scale': [0.9, 0.9, 0.9]},
            'ground': {'rotation': [0, 0, 0], 'translation': [0, 2, 0], 'scale': [0.5, 0.5, 0.5]},
            'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.75, 0.75, 0.75]},
            'head': {'rotation': [0, 0, 0], 'translation': [0, 13, 0], 'scale': [0.8, 0.8, 0.8]},
            'thirdperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, -2.5, 1.5], 'scale': [0.55, 0.55, 0.55]},
            'thirdperson_lefthand': {'rotation': [0, 45, 0], 'translation': [0, -2.5, 1.5], 'scale': [0.55, 0.55, 0.55]},
            'firstperson_righthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [0.6, 0.6, 0.6]},
            'firstperson_lefthand': {'rotation': [0, 45, 0], 'translation': [0, 0, 0], 'scale': [0.6, 0.6, 0.6]},
        },
    }


if __name__ == '__main__':
    names = {'lit': 'aldous_lantern', 'dim': 'aldous_lantern_dim', 'low': 'aldous_lantern_low', 'out': 'aldous_lantern_out'}
    for state, name in names.items():
        sheet(state).save(f'{R}/textures/item/aldous_lantern_{state}.png')
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
