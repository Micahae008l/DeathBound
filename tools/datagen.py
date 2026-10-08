"""Writes every JSON asset and data file for DeathBound.  Run: python tools/datagen.py"""
import json
import os
import zipfile
import lore
import talk  # adds the conversation pages to lore.DIALOGS before they're written
import sys

# ---- guard -------------------------------------------------------------------------------------------------------------
# This writes every file it knows from the definitions in these scripts, as they stood at DeathBound 1.0.0 (Oct 6).
# Everything changed in the repo since (stories, advancements, new charms, Death's Requiem, the 16px textures, quest text)
# would be overwritten by the 1.0.0 version. Port the change into the scripts first, then run with DB_REGEN=1.
if os.environ.get('DB_REGEN') != '1':
    sys.exit(__file__ + ': refusing to run without DB_REGEN=1 (it would overwrite newer resources; see the note above).')


ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
A = os.path.join(ROOT, 'src/main/resources/assets/deathbound')
D = os.path.join(ROOT, 'src/main/resources/data/deathbound')
MC = os.path.join(ROOT, 'src/main/resources/data/minecraft')
NS = 'deathbound'
JAR = zipfile.ZipFile(os.path.expanduser('~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar'))


def modern_loot(o):
    """26.3 loot tables: an entry's 'functions' list became 'modifier' (with 'type' for 'function'), 'conditions'
    became one 'condition' (with 'type' for 'condition'). The old keys are silently ignored, which left every written
    book blank (unreadable). Convert anything still in the old shape."""
    if isinstance(o, list):
        return [modern_loot(x) for x in o]
    if not isinstance(o, dict):
        return o
    out = {}
    for k, v in o.items():
        if k == 'functions':
            fns = [modern_loot({('type' if kk == 'function' else kk): vv for kk, vv in f.items()}) for f in v]
            out['modifier'] = fns[0] if len(fns) == 1 else fns
        elif k == 'conditions':
            cs = [modern_loot({('type' if kk == 'condition' else kk): vv for kk, vv in c.items()}) for c in v]
            out['condition'] = cs[0] if len(cs) == 1 else {'type': 'minecraft:all_of', 'terms': cs}
        else:
            out[k] = modern_loot(v)
    return out


def write(path, obj):
    if 'loot_table' in path:
        obj = modern_loot(obj)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', newline='\n') as f:
        json.dump(obj, f, indent=2)
        f.write('\n')


def vanilla(path):
    return JAR.read(path).decode('utf-8')


def from_vanilla(kind, src, dst, swaps):
    """Copy a vanilla blockstate/model/item json, renaming its references."""
    text = vanilla(f'assets/minecraft/{kind}/{src}.json')
    for a, b in swaps:
        text = text.replace(a, b)
    write(os.path.join(A, kind, dst + '.json'), json.loads(text))


def item_model(name, model=None):
    write(os.path.join(A, 'items', name + '.json'), {'model': {'type': 'minecraft:model', 'model': model or f'{NS}:block/{name}'}})


def generated(name, parent='minecraft:item/generated', texture=None):
    write(os.path.join(A, 'models/item', name + '.json'), {'parent': parent, 'textures': {'layer0': texture or f'{NS}:item/{name}'}})
    item_model(name, f'{NS}:item/{name}')


VARIANTS = {'soulstone': 3, 'soulstone_bricks': 3, 'cracked_soulstone_bricks': 2, 'dark_soulstone_bricks': 2, 'soulstone_tiles': 2}
SPIN = {'soulstone'}   # natural rock may also be rotated


def simple_block(name, texture=None):
    tex = texture or name
    n = VARIANTS.get(tex, 1)
    models = []
    for i in range(n):
        sfx = f'_{i}' if i else ''
        write(os.path.join(A, 'models/block', name + sfx + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{tex}{sfx}'}})
        for rot in ((0, 90, 180, 270) if tex in SPIN else (0,)):
            m = {'model': f'{NS}:block/{name}{sfx}'}
            if rot:
                m['y'] = rot
            models.append(m)
    write(os.path.join(A, 'blockstates', name + '.json'), {'variants': {'': models if len(models) > 1 else models[0]}})
    item_model(name)


def self_drop(name, drop=None):
    write(os.path.join(D, 'loot_table/blocks', name + '.json'), {
        'type': 'minecraft:block',
        'pools': [{'condition': {'type': 'minecraft:survives_explosion'}, 'entries': [{'type': 'minecraft:item', 'name': f'{NS}:{drop or name}'}], 'rolls': 1}],
        'random_sequence': f'{NS}:blocks/{name}'})


# ====================================================================== blocks
for b in ('soulstone', 'soulstone_bricks', 'cracked_soulstone_bricks', 'chiseled_soulstone', 'soulstone_tiles', 'ghostwood_planks'):
    simple_block(b)
    self_drop(b)
simple_block('veiled_soulstone', 'soulstone')
self_drop('veiled_soulstone', 'soulstone')
for b in ('soul_seal', 'soul_rift'):
    write(os.path.join(A, 'blockstates', b + '.json'), {'variants': {'': {'model': f'{NS}:block/{b}'}}})
    write(os.path.join(A, 'models/block', b + '.json'), {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{b}'}})

brick = [('minecraft:block/deepslate_bricks', f'{NS}:block/soulstone_bricks'), ('minecraft:block/deepslate_brick', f'{NS}:block/soulstone_brick')]
for kind in ('stairs', 'slab', 'wall'):
    from_vanilla('blockstates', f'deepslate_brick_{kind}', f'soulstone_brick_{kind}', brick)
    item = json.loads(vanilla(f'assets/minecraft/items/deepslate_brick_{kind}.json').replace('minecraft:block/deepslate_brick', f'{NS}:block/soulstone_brick'))
    write(os.path.join(A, 'items', f'soulstone_brick_{kind}.json'), item)
    self_drop(f'soulstone_brick_{kind}')
for m in ('stairs', 'stairs_inner', 'stairs_outer', 'slab', 'slab_top', 'wall_post', 'wall_side', 'wall_side_tall', 'wall_inventory'):
    from_vanilla('models/block', f'deepslate_brick_{m}', f'soulstone_brick_{m}', brick)
# slabs drop two when double
write(os.path.join(D, 'loot_table/blocks/soulstone_brick_slab.json'), json.loads(
    vanilla('data/minecraft/loot_table/blocks/deepslate_brick_slab.json').replace('minecraft:deepslate_brick_slab', f'{NS}:soulstone_brick_slab').replace('minecraft:blocks/', f'{NS}:blocks/')))

for name, side, end in (('soulstone_pillar', 'soulstone_pillar', 'soulstone_pillar_top'), ('ghostwood_log', 'ghostwood_log', 'ghostwood_log_top')):
    from_vanilla('blockstates', 'basalt', name, [('minecraft:block/basalt', f'{NS}:block/{name}')])
    write(os.path.join(A, 'models/block', name + '.json'), {'parent': 'minecraft:block/cube_column', 'textures': {'end': f'{NS}:block/{end}', 'side': f'{NS}:block/{side}'}})
    write(os.path.join(A, 'models/block', name + '_horizontal.json'), {'parent': 'minecraft:block/cube_column_horizontal', 'textures': {'end': f'{NS}:block/{end}', 'side': f'{NS}:block/{side}'}})
    item_model(name)
    self_drop(name)

write(os.path.join(A, 'blockstates/ashen_soil.json'), {'variants': {'': [{'model': f'{NS}:block/ashen_soil'}, {'model': f'{NS}:block/ashen_soil', 'y': 90},
                                                                       {'model': f'{NS}:block/ashen_soil', 'y': 180}, {'model': f'{NS}:block/ashen_soil', 'y': 270}]}})
write(os.path.join(A, 'models/block/ashen_soil.json'), {'parent': 'minecraft:block/cube_bottom_top',
                                                        'textures': {'top': f'{NS}:block/ashen_soil', 'side': f'{NS}:block/ashen_soil_side', 'bottom': f'{NS}:block/soulstone'}})
item_model('ashen_soil')
self_drop('ashen_soil')

write(os.path.join(A, 'blockstates/gloom_grass.json'), {'variants': {'': {'model': f'{NS}:block/gloom_grass'}}})
write(os.path.join(A, 'models/block/gloom_grass.json'), {'parent': 'minecraft:block/cross', 'textures': {'cross': f'{NS}:block/gloom_grass'}})
generated('gloom_grass', texture=f'{NS}:block/gloom_grass')
write(os.path.join(D, 'loot_table/blocks/gloom_grass.json'), {'type': 'minecraft:block', 'pools': [], 'random_sequence': f'{NS}:blocks/gloom_grass'})

from_vanilla('blockstates', 'amethyst_cluster', 'soul_crystal', [('minecraft:block/amethyst_cluster', f'{NS}:block/soul_crystal')])
write(os.path.join(A, 'models/block/soul_crystal.json'), {'parent': 'minecraft:block/cross', 'textures': {'cross': f'{NS}:block/soul_crystal'}})
generated('soul_crystal', texture=f'{NS}:block/soul_crystal')
self_drop('soul_crystal')

from_vanilla('blockstates', 'soul_lantern', 'wraith_lantern', [('minecraft:block/soul_lantern', f'{NS}:block/wraith_lantern')])
for m in ('', '_hanging'):
    text = vanilla(f'assets/minecraft/models/block/soul_lantern{m}.json').replace('minecraft:block/soul_lantern', f'{NS}:block/wraith_lantern')
    write(os.path.join(A, f'models/block/wraith_lantern{m}.json'), json.loads(text))
generated('wraith_lantern')
self_drop('wraith_lantern')

# ====================================================================== items
for name in ('deathbound_relic', 'soulbound_charm', 'seers_charm', 'wraiths_charm', 'ferrymans_charm', 'reapers_charm', 'open_door_charm', 'hunters_charm', 'soul', 'grave_rune', 'heart_of_death',
             'soul_bolt', 'death_orb', 'ferry_coin', 'wooden_horse', 'kings_quill', 'warden_seal', 'melted_chains', 'kings_ring', 'hunters_arrowhead', 'last_drop', 'gravebound_spawn_egg', 'soul_wisp_spawn_egg', 'deaths_guard_spawn_egg', 'death_spawn_egg', 'hollow_hunter_spawn_egg', 'lost_soul_spawn_egg'):
    generated(name)
# the scythe: flat icon in GUIs, a big two-handed blade in hand (same trick as the vanilla spear)
write(os.path.join(A, 'models/item/reaper_scythe.json'), {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/reaper_scythe'}})
write(os.path.join(A, 'models/item/reaper_scythe_in_hand.json'), {
    'parent': 'minecraft:item/generated', 'gui_light': 'front', 'textures': {'layer0': f'{NS}:item/reaper_scythe_in_hand'},
    'display': {
        # 32px at the vanilla spear's scales, so its pixels are a vanilla held item's. Held upright at the side, the blade
        # over the shoulder turned 70 degrees out and the snath leaned 12 out so it reads from behind and in front (a flat
        # sprite is edge-on from some angle). The swing turns it in the hand (ScytheSwing.thirdPersonItem).
        # Left hand: the same numbers with x scale negated. ItemTransform.apply mirrors a left-hand transform itself, which
        # also flips the sprite; the negative scale undoes that, leaving an exact mirror of the right hand.
        'thirdperson_righthand': {'rotation': [94.33, 19.54, -12.75], 'translation': [5.19, -0.59, 8.65], 'scale': [1.7, 1.7, 0.9]},
        'thirdperson_lefthand': {'rotation': [94.33, 19.54, -12.75], 'translation': [5.19, -0.59, 8.65], 'scale': [-1.7, 1.7, 0.9]},
        # first person: the blade hooks toward the crosshair
        'firstperson_righthand': {'rotation': [0, 90, 5], 'translation': [1.5, 3.33, -5.61], 'scale': [1.36, 1.36, 0.8]},
        'firstperson_lefthand': {'rotation': [0, 90, 5], 'translation': [1.5, 3.33, -5.61], 'scale': [-1.36, 1.36, 0.8]},
    }})
write(os.path.join(A, 'items/reaper_scythe.json'), {
    'model': {'type': 'minecraft:select', 'property': 'minecraft:display_context',
              'cases': [{'when': ['gui', 'ground', 'fixed', 'on_shelf'], 'model': {'type': 'minecraft:model', 'model': f'{NS}:item/reaper_scythe'}}],
              'fallback': {'type': 'minecraft:model', 'model': f'{NS}:item/reaper_scythe_in_hand'}},
    'swap_animation_scale': 1.8})

# ====================================================================== particles
write(os.path.join(A, 'particles/soul_mote.json'), {'textures': [f'{NS}:soul_mote_{i}' for i in range(4)]})
write(os.path.join(A, 'particles/soul_flame.json'), {'textures': [f'{NS}:soul_flame_{i}' for i in range(6)]})
write(os.path.join(A, 'particles/soul_sweep.json'), {'textures': [f'{NS}:soul_sweep_{i}' for i in range(8)]})

# ====================================================================== tags
pick = ['soulstone', 'soulstone_bricks', 'cracked_soulstone_bricks', 'chiseled_soulstone', 'soulstone_tiles', 'soulstone_pillar',
        'soulstone_brick_stairs', 'soulstone_brick_slab', 'soulstone_brick_wall', 'veiled_soulstone', 'soul_crystal', 'wraith_lantern']
tag = lambda vals: {'values': [f'{NS}:{v}' for v in vals]}
write(os.path.join(MC, 'tags/block/mineable/pickaxe.json'), tag(pick))
write(os.path.join(MC, 'tags/block/mineable/axe.json'), tag(['ghostwood_log', 'ghostwood_planks', 'ossified_log', 'curio_shelf']))
write(os.path.join(MC, 'tags/block/mineable/shovel.json'), tag(['ashen_soil']))
write(os.path.join(MC, 'tags/block/walls.json'), tag(['soulstone_brick_wall']))
write(os.path.join(MC, 'tags/block/stairs.json'), tag(['soulstone_brick_stairs']))
write(os.path.join(MC, 'tags/block/slabs.json'), tag(['soulstone_brick_slab']))
write(os.path.join(MC, 'tags/block/supports_vegetation.json'), tag(['ashen_soil']))
write(os.path.join(MC, 'tags/item/walls.json'), tag(['soulstone_brick_wall']))
write(os.path.join(MC, 'tags/item/stairs.json'), tag(['soulstone_brick_stairs']))
write(os.path.join(MC, 'tags/item/slabs.json'), tag(['soulstone_brick_slab']))
write(os.path.join(D, 'tags/item/reaper_scythe_repair.json'), tag(['soul']))
write(os.path.join(MC, 'tags/item/enchantable/sharp_weapon.json'), tag(['reaper_scythe']))
write(os.path.join(MC, 'tags/item/enchantable/melee_weapon.json'), tag(['reaper_scythe']))
write(os.path.join(MC, 'tags/item/enchantable/durability.json'), tag(['reaper_scythe', 'hunters_bow']))
write(os.path.join(MC, 'tags/item/enchantable/bow.json'), tag(['hunters_bow']))
write(os.path.join(MC, 'tags/item/enchantable/vanishing.json'), tag(['hunters_bow']))
write(os.path.join(MC, 'tags/item/enchantable/fire_aspect.json'), tag(['reaper_scythe']))
write(os.path.join(D, 'damage_type/soul_rend.json'), {'exhaustion': 0.0, 'message_id': 'deathbound.soul_rend', 'scaling': 'when_caused_by_living_non_player'})
write(os.path.join(D, 'damage_type/crush.json'), {'exhaustion': 0.1, 'message_id': 'deathbound.crush', 'scaling': 'when_caused_by_living_non_player'})
# a shield stops claws and blades; it does nothing against the ground coming up under you, a body charging through, or your soul being pulled
write(os.path.join(MC, 'tags/damage_type/bypasses_shield.json'), {'values': [f'{NS}:crush', f'{NS}:soul_rend']})

# ====================================================================== recipes
R = os.path.join(D, 'recipe')
shaped = lambda pattern, key, result, count=1, cat='building': {'type': 'minecraft:crafting_shaped', 'category': cat, 'key': key, 'pattern': pattern,
                                                                  'result': {'count': count, 'id': result}}
cut = lambda ing, result, count=1: {'type': 'minecraft:stonecutting', 'ingredient': ing, 'result': {'count': count, 'id': result}}
write(os.path.join(R, 'ghostwood_chair.json'), shaped(['P  ', 'PPP', 'S S'], {'P': f'{NS}:ghostwood_planks', 'S': 'minecraft:stick'}, f'{NS}:ghostwood_chair', 2, 'misc'))
write(os.path.join(R, 'ghostwood_table.json'), shaped(['PPP', 'S S', 'S S'], {'P': f'{NS}:ghostwood_planks', 'S': 'minecraft:stick'}, f'{NS}:ghostwood_table', 1, 'misc'))
write(os.path.join(R, 'bone_candelabra.json'), shaped([' C ', 'BBB', ' B '], {'C': 'minecraft:purple_candle', 'B': 'minecraft:bone'}, f'{NS}:bone_candelabra', 1, 'misc'))
write(os.path.join(R, 'tombstone.json'), shaped([' S ', 'SSS', 'SSS'], {'S': f'{NS}:soulstone'}, f'{NS}:tombstone', 2, 'misc'))
write(os.path.join(R, 'ossuary_shelf.json'), shaped(['PPP', 'BBB', 'PPP'], {'P': f'{NS}:ghostwood_planks', 'B': 'minecraft:bone'}, f'{NS}:ossuary_shelf', 1, 'misc'))
write(os.path.join(R, 'soulstone_bricks.json'), shaped(['##', '##'], {'#': f'{NS}:soulstone'}, f'{NS}:soulstone_bricks', 4))
write(os.path.join(R, 'soulstone_tiles.json'), shaped(['##', '##'], {'#': f'{NS}:soulstone_bricks'}, f'{NS}:soulstone_tiles', 4))
write(os.path.join(R, 'soulstone_pillar.json'), shaped(['#', '#'], {'#': f'{NS}:soulstone'}, f'{NS}:soulstone_pillar', 2))
write(os.path.join(R, 'chiseled_soulstone.json'), shaped(['#', 'S'], {'#': f'{NS}:soulstone_brick_slab', 'S': f'{NS}:soul'}, f'{NS}:chiseled_soulstone', 1))
write(os.path.join(R, 'soulstone_brick_stairs.json'), shaped(['#  ', '## ', '###'], {'#': f'{NS}:soulstone_bricks'}, f'{NS}:soulstone_brick_stairs', 4))
write(os.path.join(R, 'soulstone_brick_slab.json'), shaped(['###'], {'#': f'{NS}:soulstone_bricks'}, f'{NS}:soulstone_brick_slab', 6))
write(os.path.join(R, 'soulstone_brick_wall.json'), shaped(['###', '###'], {'#': f'{NS}:soulstone_bricks'}, f'{NS}:soulstone_brick_wall', 6))
write(os.path.join(R, 'ghostwood_planks.json'), {'type': 'minecraft:crafting_shapeless', 'category': 'building', 'ingredients': [f'{NS}:ghostwood_log'],
                                                 'result': {'count': 4, 'id': f'{NS}:ghostwood_planks'}})
write(os.path.join(R, 'wraith_lantern.json'), shaped(['XXX', 'X#X', 'XXX'], {'X': 'minecraft:iron_nugget', '#': f'{NS}:soul'}, f'{NS}:wraith_lantern', 1, 'misc'))
write(os.path.join(R, 'cracked_soulstone_bricks.json'), {'type': 'minecraft:smelting', 'category': 'blocks', 'cookingtime': 200, 'experience': 0.1,
                                                         'ingredient': f'{NS}:soulstone_bricks', 'result': {'id': f'{NS}:cracked_soulstone_bricks'}})
for out, n in (('soulstone_bricks', 1), ('soulstone_tiles', 1), ('soulstone_pillar', 1), ('chiseled_soulstone', 1),
               ('soulstone_brick_stairs', 1), ('soulstone_brick_slab', 2), ('soulstone_brick_wall', 1)):
    write(os.path.join(R, f'{out}_from_soulstone_stonecutting.json'), cut(f'{NS}:soulstone', f'{NS}:{out}', n))

# ====================================================================== loot: mobs
def drops(name, pools):
    write(os.path.join(D, 'loot_table/entities', name + '.json'), {'type': 'minecraft:entity', 'pools': pools, 'random_sequence': f'{NS}:entities/{name}'})


def entry(item, lo=1, hi=1, weight=None, looting=0.0, extra=None):
    e = {'type': 'minecraft:item', 'name': item}
    mods = []
    if lo != 1 or hi != 1:
        mods.append({'type': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi} if lo != hi else lo})
    if looting:
        mods.append({'type': 'minecraft:enchanted_count_increase', 'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': looting}, 'enchantment': 'minecraft:looting'})
    if extra:
        mods.extend(extra)
    if mods:
        e['modifier'] = mods if len(mods) > 1 else mods[0]
    if weight:
        e['weight'] = weight
    return e


def pool(entries, rolls=1, chance=None, player=False):
    p = {'entries': entries, 'rolls': rolls}
    conds = []
    if player:
        conds.append({'type': 'minecraft:killed_by_player'})
    if chance:
        conds.append({'type': 'minecraft:random_chance', 'chance': chance})
    if conds:
        p['condition'] = conds[0] if len(conds) == 1 else {'type': 'minecraft:all_of', 'terms': conds}
    return p


drops('gravebound', [pool([entry(f'{NS}:soul', 0, 1, looting=0.5)]), pool([entry('minecraft:bone', 0, 2, looting=1.0)]),
                     pool([entry(f'{NS}:grave_rune')], chance=0.02, player=True),
                     pool([entry(f'{NS}:wraiths_charm')], chance=0.008, player=True)])
drops('soul_wisp', [pool([entry(f'{NS}:soul', 0, 1, looting=0.5)]), pool([entry('minecraft:phantom_membrane')], chance=0.15, player=True)])
drops('soul_anchor', [pool([entry(f'{NS}:soul', 1, 3)])])
drops('lost_soul', [])
enchanted = lambda lo, hi: [{'type': 'minecraft:enchant_with_levels', 'levels': {'type': 'minecraft:uniform', 'min': lo, 'max': hi}, 'options': '#minecraft:on_random_loot'}]
drops('deaths_guard', [pool([entry(f'{NS}:warden_seal')]), pool([entry(f'{NS}:soul', 6, 10)]), pool([entry(f'{NS}:grave_rune', 1, 1)]),
                       pool([entry(f'{NS}:reapers_charm'), entry(f'{NS}:wraiths_charm'), entry(f'{NS}:soulbound_charm'), entry(f'{NS}:seers_charm')]),
                       pool([entry('minecraft:netherite_scrap', 1, 2)]), pool([entry(f'{NS}:ferrymans_charm')], chance=0.35)])
drops('hollow_hunter', [pool([entry(f'{NS}:hunters_arrowhead')]), pool([entry(f'{NS}:hunters_charm')]),
                        pool([entry(f'{NS}:soul', 10, 16)]), pool([entry(f'{NS}:grave_rune', 1, 1)]),
                        pool([{'type': 'minecraft:item', 'name': f'{NS}:hunters_bow', 'functions': [
                            {'function': 'minecraft:set_enchantments', 'enchantments': {'minecraft:power': 4, 'minecraft:punch': 1, 'minecraft:infinity': 1}}]}])])
drops('death', [pool([entry(f'{NS}:heart_of_death')]), pool([entry(f'{NS}:kings_ring')]), pool([entry(f'{NS}:open_door_charm')]), pool([entry(f'{NS}:reaper_scythe')]), pool([entry(f'{NS}:soul', 16, 24)]),
                pool([entry('minecraft:enchanted_golden_apple')]), pool([entry('minecraft:nether_star')]),
                pool([entry('minecraft:netherite_ingot', 1, 2)]), pool([entry(f'{NS}:soulbound_charm')], chance=0.5)])

# ====================================================================== loot: chests
def chest(name, pools):
    write(os.path.join(D, 'loot_table/chests', name + '.json'), {'type': 'minecraft:chest', 'pools': pools, 'random_sequence': f'{NS}:chests/{name}'})


rolls = lambda lo, hi: {'type': 'minecraft:uniform', 'min': lo, 'max': hi}
supplies = [entry('minecraft:bread', 2, 5, weight=10), entry('minecraft:golden_carrot', 2, 4, weight=8), entry('minecraft:cooked_beef', 2, 4, weight=8),
            entry('minecraft:arrow', 6, 16, weight=8), entry('minecraft:torch', 4, 10, weight=6), entry(f'{NS}:soul', 1, 3, weight=6),
            entry('minecraft:iron_ingot', 2, 5, weight=8), entry('minecraft:bone', 2, 6, weight=6), entry(f'{NS}:wraith_lantern', 1, 2, weight=4)]
treasure = [entry('minecraft:diamond', 1, 3, weight=10), entry('minecraft:emerald', 2, 6, weight=10), entry('minecraft:gold_ingot', 2, 6, weight=12),
            entry('minecraft:ancient_debris', 1, 2, weight=4), entry('minecraft:netherite_scrap', 1, 1, weight=3), entry('minecraft:echo_shard', 1, 3, weight=6),
            entry('minecraft:amethyst_shard', 3, 8, weight=8), entry('minecraft:ender_pearl', 2, 4, weight=8), entry('minecraft:blaze_rod', 1, 3, weight=6),
            entry('minecraft:ghast_tear', 1, 2, weight=5), entry('minecraft:shulker_shell', 1, 2, weight=3), entry('minecraft:golden_apple', 1, 2, weight=8),
            entry('minecraft:enchanted_golden_apple', weight=1), entry('minecraft:book', extra=enchanted(25, 39), weight=8),
            entry('minecraft:diamond_sword', extra=enchanted(25, 39), weight=3), entry('minecraft:diamond_chestplate', extra=enchanted(25, 39), weight=2),
            entry('minecraft:netherite_boots', extra=enchanted(30, 39), weight=1)]
# whoever comes down with nothing still gets a fighting chance
pilgrim_kit = [pool([entry('minecraft:iron_sword'), entry('minecraft:iron_axe')]), pool([entry('minecraft:shield')]),
               pool([entry('minecraft:chainmail_chestplate')]), pool([entry('minecraft:chainmail_helmet'), entry('minecraft:chainmail_boots')]),
               pool([entry('minecraft:torch', 8, 16)])]
def kings_relic(item, name, lines, extra=None):
    fns = [{'function': 'minecraft:set_name', 'target': 'item_name', 'name': {'text': name, 'color': '#e0c27a'}},
           {'function': 'minecraft:set_lore', 'mode': 'replace_all', 'lore': [{'text': t, 'color': 'gray', 'italic': True} for t in lines]}]
    return {'type': 'minecraft:item', 'name': item, 'functions': fns + ([extra] if extra else [])}


kings_relics = {'rolls': 1, 'entries': [
    kings_relic('minecraft:iron_pickaxe', "Halden's Pickaxe", ["King Halden dug his way down from up top.", "He was the first to take the throne."],
                {'function': 'minecraft:enchant_with_levels', 'levels': 20}),
    kings_relic('minecraft:bow', "Ysolde's Bow", ["Queen Ysolde shot King Halden off his throne.", "She ruled for six hundred years."],
                {'function': 'minecraft:enchant_with_levels', 'levels': 25}),
    kings_relic('minecraft:compass', "The King's Compass", ["It still points up. Home.", "The King stopped looking at it long ago."])]}

chest('arrival', [pool([entry(f'{NS}:ferrymans_charm')]), pool(supplies, rolls(3, 5)), *pilgrim_kit, lore.book_pool('arrival'), lore.book_pool('silas1')])
chest('ruins', [lore.journal_pool(0.5), pool([entry(f'{NS}:grave_rune', 1, 1)], chance=0.35), pool(supplies, rolls(2, 4)), pool(treasure, rolls(0, 1)), pool([entry(f'{NS}:seers_charm')], chance=0.15), lore.book_pool('ruins'), lore.book_pool('silas2', 0.5)])
chest('village', [lore.journal_pool(0.5), pool([entry(f'{NS}:grave_rune', 1, 1)], chance=0.4), pool(supplies, rolls(2, 4)), pool(treasure, 1, chance=0.5), pool([entry(f'{NS}:wraiths_charm'), entry(f'{NS}:seers_charm')], chance=0.2),
                  lore.book_pool('village'), lore.book_pool('silas3'), pool([entry(f'{NS}:wooden_horse')], chance=0.5)])
chest('hollow_root', [lore.journal_pool(0.6), lore.book_pool('hollow_root'), pool([entry(f'{NS}:grave_rune', 1, 1)]), pool([entry(f'{NS}:seers_charm')]), pool(treasure, rolls(1, 2))])
chest('crypt', [lore.journal_pool(0.4), pool([entry(f'{NS}:grave_rune', 1, 1)], chance=0.3), pool(supplies, rolls(1, 3)), pool(treasure, rolls(0, 2)), pool([entry(f'{NS}:soulbound_charm')], chance=0.3), lore.book_pool('crypt'), kings_relics])
chest('crypt_vault', [pool([entry(f'{NS}:grave_rune', 1, 1)]), pool([entry(f'{NS}:soulbound_charm')]), pool(treasure, rolls(2, 3)), pool([entry('minecraft:totem_of_undying')], chance=0.5),
                      lore.book_pool('silas4'), pool([entry(f'{NS}:kings_quill')])])
chest('spire', [lore.journal_pool(0.5), pool([entry(f'{NS}:grave_rune', 1, 1)]), pool([entry(f'{NS}:reapers_charm')]), pool(treasure, rolls(1, 2)), lore.book_pool('spire'), lore.book_pool('silas5')])
chest('tower', [lore.journal_pool(0.5), pool([entry(f'{NS}:soul', 1, 4)], chance=0.6), pool([entry(f'{NS}:grave_rune')], chance=0.15),
                pool(supplies, rolls(1, 3)), pool(treasure, rolls(0, 2)), pool([entry('minecraft:arrow', 6, 16)], chance=0.5)])
chest('watch', [lore.journal_pool(0.6), pool([entry(f'{NS}:grave_rune', 1, 1)], chance=0.6), pool([entry(f'{NS}:wraiths_charm')]), pool(treasure, rolls(1, 2)), pool(supplies, rolls(1, 3)), lore.book_pool('watch'), lore.book_pool('silas6')])
# a dead traveller's pack, and the barrels in the village's houses
chest('death_camp', [lore.journal_pool(), pool(supplies, rolls(1, 3)), pool([entry(f'{NS}:soul', 1, 2)], chance=0.5),
                     pool([entry(f'{NS}:grave_rune')], chance=0.3), pool([entry('minecraft:iron_sword'), entry('minecraft:bow'),
                     entry('minecraft:shield'), entry('minecraft:iron_pickaxe')], chance=0.5)])
chest('house_barrel', [pool(supplies, rolls(0, 2)), lore.journal_pool(0.25), pool([entry('minecraft:bone', 1, 4),
                       entry('minecraft:candle', 1, 3), entry('minecraft:paper', 1, 4), entry('minecraft:string', 1, 3)], rolls(1, 2))])
chest('citadel', [pool([entry(f'{NS}:grave_rune', 2, 2)]), pool(treasure, rolls(2, 4)), pool([entry('minecraft:netherite_ingot')], chance=0.5), pool([entry('minecraft:enchanted_golden_apple')], chance=0.5),
                  lore.book_pool('citadel'), lore.book_pool('silas7'), pool([entry(f'{NS}:melted_chains')])])

# the Ferryman's beached boat, the apothecary's barrel, the Collector's spare curios, the Hunter's blind
potion = lambda pid, weight=1: {'type': 'minecraft:item', 'name': 'minecraft:potion', 'weight': weight,
                                'functions': [{'function': 'minecraft:set_potion', 'id': pid}]}
chest('ferry', [pool([entry(f'{NS}:ferry_coin')]), pool([entry(f'{NS}:soul', 2, 5)]), pool(supplies, rolls(2, 4)), lore.journal_pool(0.6),
                pool([entry('minecraft:lantern'), entry('minecraft:compass'), entry('minecraft:lead', 1, 2)], chance=0.6)])
chest('apothecary', [lore.book_pool('apothecary'), pool([potion(f'{NS}:warding', 3), potion(f'{NS}:grave_sight', 2), potion(f'{NS}:last_breath', 1),
                     potion('minecraft:awkward', 3)], rolls(2, 4)),
                     pool([entry('minecraft:nether_wart', 2, 6), entry('minecraft:blaze_powder', 1, 3), entry('minecraft:glass_bottle', 2, 5),
                           entry(f'{NS}:jar', 1, 3), entry(f'{NS}:eye_jar'), entry('minecraft:spider_eye', 1, 3)], rolls(2, 4))])
chest('last_cup', [pool([entry(f'{NS}:last_drop')]), pool([entry(f'{NS}:soul_jar', 1, 2)])])
chest('curios', [pool([entry('minecraft:clock'), entry('minecraft:spyglass'), entry('minecraft:recovery_compass'), entry('minecraft:goat_horn'),
                       entry('minecraft:music_disc_11'), entry('minecraft:name_tag'), entry('minecraft:totem_of_undying', weight=1)], rolls(2, 3)),
                 pool([entry(f'{NS}:soul_jar', 1, 2), entry(f'{NS}:eye_jar'), entry(f'{NS}:bone_jar')], rolls(1, 2)),
                 pool([entry(f'{NS}:grave_rune')], chance=0.4), pool(treasure, rolls(0, 1))])
chest('hunter', [lore.book_pool('hunted'), pool([entry(f'{NS}:hunters_arrowhead')]), pool([entry('minecraft:arrow', 16, 32)]), pool([entry('minecraft:spectral_arrow', 6, 12)], chance=0.7),
                 pool([entry('minecraft:bow', extra=enchanted(20, 30))]), pool([entry('minecraft:bone', 3, 8), entry(f'{NS}:soul', 1, 3)], rolls(1, 2)),
                 pool([entry(f'{NS}:grave_rune', 1, 2)])])

# ====================================================================== the dimension
write(os.path.join(D, 'dimension_type/underworld.json'), {
    'has_skylight': False, 'has_ceiling': True, 'has_ender_dragon_fight': False, 'has_fixed_time': True,
    'coordinate_scale': 1.0, 'min_y': 0, 'height': 256, 'logical_height': 256,
    'infiniburn': '#minecraft:infiniburn_nether', 'ambient_light': 0.07,
    'monster_spawn_light_level': 7, 'monster_spawn_block_light_limit': 0,
    'skybox': 'none', 'cardinal_light': 'default', 'timelines': '#minecraft:in_nether',
    'attributes': {
        'minecraft:visual/fog_color': '#110f15',
        'minecraft:visual/fog_start_distance': 0.0,
        'minecraft:visual/fog_end_distance': 60.0,
        'minecraft:visual/sky_fog_end_distance': 60.0,
        'minecraft:visual/sky_light_factor': 0.0,
        'minecraft:visual/sky_light_color': '#6a4a9a',
        'minecraft:visual/ambient_light_color': '#1c1923',
        'minecraft:visual/block_light_tint': '#c8a2ff',
        'minecraft:visual/default_dripstone_particle': {'type': 'minecraft:dripping_obsidian_tear'},
        'minecraft:gameplay/sky_light_level': 2.0,
        'minecraft:gameplay/bed_rule': {'can_sleep': 'never', 'can_set_spawn': 'never', 'destroy_on_use': True},
        'minecraft:gameplay/straw_bed_rule': {'can_sleep': 'never', 'can_set_spawn': 'never', 'destroy_on_use': True},
        'minecraft:gameplay/respawn_anchor_works': False,
        'minecraft:gameplay/can_start_raid': False,
    }})
write(os.path.join(D, 'worldgen/noise_settings/underworld.json'), {
    'default_block': f'{NS}:soulstone', 'default_fluid': 'minecraft:air', 'disable_mob_generation': False, 'legacy_random_source': False,
    'material_rule': {'type': 'minecraft:block', 'result_state': f'{NS}:soulstone'},
    'noise': {'min_y': 0, 'height': 256}, 'sea_level': 0, 'spawn_target': [],
    'noise_router': {'chunk_surface_level': 0.0, 'continents': 0.0, 'depth': 0.0, 'erosion': 0.0, 'ridges': 0.0,
                     'temperature': 0.0, 'vegetation': 0.0, 'final_density': -1.0}})
write(os.path.join(D, 'worldgen/biome/underworld.json'), {
    'has_precipitation': False, 'temperature': 0.5, 'downfall': 0.0,
    'effects': {'water_color': '#5a2e9a', 'grass_color': '#6b5a8a', 'foliage_color': '#5a4a78'},
    'attributes': {
        'minecraft:visual/fog_color': '#110f15',
        'minecraft:visual/water_fog_color': '#1a0a30',
        'minecraft:visual/ambient_particles': {'modifier': 'append', 'argument': [
            {'particle': {'type': f'{NS}:soul_mote'}, 'probability': 0.0006},
            {'particle': {'type': 'minecraft:white_ash'}, 'probability': 0.03},
            {'particle': {'type': 'minecraft:ash'}, 'probability': 0.02}]},
        'minecraft:audio/background_music': {'default': {'sound': 'minecraft:music.overworld.deep_dark', 'min_delay': 3000, 'max_delay': 9000}},
        'minecraft:audio/ambient_sounds': {
            'loop': 'minecraft:ambient.soul_sand_valley.loop',
            'mood': {'sound': 'minecraft:ambient.cave', 'tick_delay': 1200, 'block_search_extent': 8, 'offset': 2.0},
            'additions': {'sound': f'{NS}:dread.air', 'tick_chance': 0.008}},
        'minecraft:gameplay/natural_mob_spawns': {'modifier': 'overlay', 'argument': {
            'spawns_by_category': {
                'monster': [{'type': f'{NS}:gravebound', 'count': {'type': 'minecraft:uniform', 'min_inclusive': 1, 'max_inclusive': 2}, 'weight': 100},
                            {'type': f'{NS}:soul_wisp', 'count': {'type': 'minecraft:uniform', 'min_inclusive': 1, 'max_inclusive': 1}, 'weight': 55}],
                'creature': [], 'ambient': []},
            # islands have little ground, so the whole mob cap would pile onto it: spawn costs keep them spread thin
            'spawn_costs': {f'{NS}:gravebound': {'energy_budget': 0.08, 'charge': 0.7}, f'{NS}:soul_wisp': {'energy_budget': 0.07, 'charge': 0.7}}}},   # wisps nearly as common as the gravebound
    },
    'carvers': [],
    'features': [[f'{NS}:underworld_builder']]})
write(os.path.join(D, 'worldgen/feature/underworld_builder.json'), {'type': f'{NS}:underworld_builder'})
write(os.path.join(D, 'worldgen/placed_feature/underworld_builder.json'), {'feature': f'{NS}:underworld_builder', 'placement': []})
write(os.path.join(D, 'dimension/underworld.json'), {
    'type': f'{NS}:underworld',
    'generator': {'type': 'minecraft:noise', 'settings': f'{NS}:underworld', 'biome_source': {'type': 'minecraft:fixed', 'biome': f'{NS}:underworld'}}})
# ====================================================================== atmosphere pass: shaded blocks, glow, remains
DIRS = ('north', 'south', 'east', 'west', 'up', 'down')


def el(fr, to, tex, rot=None, faces=DIRS, emit=None, uv_full=None):
    """A model element with explicit UVs clipped to 16 (so parts outside the block never sample the atlas)."""
    w, h, d = to[0] - fr[0], to[1] - fr[1], to[2] - fr[2]
    dims = {'north': (w, h), 'south': (w, h), 'east': (d, h), 'west': (d, h), 'up': (w, d), 'down': (w, d)}
    fs = {}
    for f in faces:
        fw, fh = dims[f]
        if fw <= 0 and fh <= 0:
            continue
        uv = [0, 0, max(0.5, min(16, fw)), max(0.5, min(16, fh))]
        if uv_full and f in uv_full:
            uv = [0, 0, 16, 16]
        fs[f] = {'uv': uv, 'texture': (uv_full or {}).get(f, tex)}
    e = {'from': list(fr), 'to': list(to), 'faces': fs}
    if rot:
        e['rotation'] = {'origin': rot[0], 'axis': rot[1], 'angle': rot[2]}
    if emit:
        e['light_emission'] = emit
    return e


def decor_model(name, elements, textures, twitch=None):
    """twitch: elements for the second pose of a RemainsBlock (twitch=true)."""
    tex = {'particle': f'{NS}:block/remains_bone'}
    tex.update({k: v if ':' in v else f'{NS}:block/{v}' for k, v in textures.items()})
    write(os.path.join(A, 'models/block', name + '.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False, 'textures': tex, 'elements': elements})
    turns = (('north', 0), ('east', 90), ('south', 180), ('west', 270))
    if twitch is None:
        variants = {f'facing={f}': {'model': f'{NS}:block/{name}', **({'y': r} if r else {})} for f, r in turns}
    else:
        write(os.path.join(A, 'models/block', name + '_twitch.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False, 'textures': tex, 'elements': twitch})
        variants = {f'facing={f},twitch={t}': {'model': f'{NS}:block/{name}' + ('_twitch' if t == 'true' else ''), **({'y': r} if r else {})}
                    for f, r in turns for t in ('false', 'true')}
    write(os.path.join(A, 'blockstates', name + '.json'), {'variants': variants})
    item_model(name)
    self_drop(name)


# ---------------------------------------------------------------------- skulls that look like skulls
SKULL_T = {'particle': 'skull_bone', 'bone': 'skull_bone', 'face': 'skull_front', 'jaw': 'skull_jaw'}


def skull_parts(cx=8.0, y=0.0, fz=5.5, s=1.0, R=None, gape=0):
    # A human skull about seven pixels across: cranium, dome, a face plate (sockets, nose, upper teeth), cheekbones,
    # and the jaw hinged under it. cx/y/fz place its centre line, floor and front; s scales; R rotates it all.
    def box(a, b, tex, uv=None, rot=R):
        fr = [cx + a[0] * s, y + a[1] * s, fz + a[2] * s]
        to = [cx + b[0] * s, y + b[1] * s, fz + b[2] * s]
        return el(fr, to, tex, rot, uv_full=uv)
    parts = [box((-3.5, 1.5, 1.0), (3.5, 7.5, 7.5), '#bone'),
             box((-3.0, 7.5, 1.5), (3.0, 8.5, 7.0), '#bone'),
             box((-3.0, 1.5, 0.0), (3.0, 7.0, 1.0), '#bone', {'north': '#face'}),
             box((-3.8, 3.0, 0.5), (-3.2, 4.0, 3.0), '#bone'), box((3.2, 3.0, 0.5), (3.8, 4.0, 3.0), '#bone')]
    jaw = [box((-2.5, 0.0, -0.3), (2.5, 1.5, 2.0), '#bone', {'north': '#jaw'}),
           box((-2.8, 0.0, 2.0), (-2.0, 2.5, 4.0), '#bone'), box((2.0, 0.0, 2.0), (2.8, 2.5, 4.0), '#bone')]
    if gape and R is None:
        for j in jaw:
            j['rotation'] = {'origin': [cx, y + 1.8 * s, fz + 3.5 * s], 'axis': 'x', 'angle': gape}
    return parts + jaw


def arrow_parts(x, z, axis, angle, length=9.0, y=0.0):
    # A purple-fletched arrow driven in at an angle; the head is buried.
    rot = ([x, y, z], axis, angle)
    return [el([x - 0.3, y, z - 0.3], [x + 0.3, y + length, z + 0.3], '#shaft', rot),
            el([x - 1.1, y + length - 2.8, z], [x + 1.1, y + length, z], '#fletch', rot, faces=('north', 'south'), emit=15),
            el([x, y + length - 2.8, z - 1.1], [x, y + length, z + 1.1], '#fletch', rot, faces=('east', 'west'), emit=15)]


ARROW_T = {'shaft': 'hunter_arrow_shaft', 'fletch': 'hunter_arrow_fletch', 'head': 'hunter_arrow_head'}
BONE_T = {'bone': 'remains_bone', 'skull': 'remains_skull', 'rag': 'remains_rag', 'iron': 'dark_iron',
          'face': 'skull_front', 'jaw': 'skull_jaw'}


def skeleton_lying(twitch=False):
    """On its back, laid along the block diagonal so the whole body fits. Twitching: it jerks off the floor, one arm clawing up."""
    R = ([8, 0, 8], 'y', 45)
    e = skull_parts(8, 0, -3.4, 0.7, R)
    e += [el([6.2, 0, 1.2], [9.8, 1.3, 2.6], '#bone', R),
         el([7.5, 0, 2.6], [8.5, 1, 10.5], '#bone', R)]
    for z in (3.2, 4.6, 6.0, 7.4):
        e.append(el([4.6, 0.3, z], [11.4, 2.6, z + 0.8], '#bone', R))
    e += [el([7.25, 2.5, 3.2], [8.75, 2.9, 8.2], '#bone', R),
          el([5.5, 0, 10.2], [10.5, 1.8, 12], '#bone', R),
          el([5.8, 0, 12], [6.8, 1, 16], '#bone', R), el([9.2, 0, 12], [10.2, 1, 16], '#bone', R),
          el([5.6, 0, 16], [7, 1.4, 17.2], '#bone', R), el([9, 0, 16], [10.4, 1.4, 17.2], '#bone', R),
          el([5.8, 0, 17.2], [6.8, 0.9, 19.2], '#bone', R), el([9.2, 0, 17.2], [10.2, 0.9, 19.2], '#bone', R),
          (el([3.4, 0, 3.4], [4.4, 7, 4.4], '#bone', R) if twitch else el([3.4, 0, 3.4], [4.4, 1, 8.4], '#bone', R)),
          el([11.6, 0, 3.4], [12.6, 1, 8.4], '#bone', R),
          el([2.6, 0, 8], [3.6, 0.9, 12], '#bone', ([3, 0, 8], 'y', 20)), el([12.4, 0, 8], [13.4, 0.9, 12], '#bone', ([13, 0, 8], 'y', -20)),
          el([4.3, 2.75, 4.2], [11.7, 2.95, 12.5], '#rag', R, faces=('up', 'down'))]
    if twitch:  # the whole body jerks a little off the stone
        for x in e[:-1]:
            if x['to'][1] - x['from'][1] < 6:
                x['from'][1] += 1.1; x['to'][1] += 1.1
    return e


def skeleton_slumped(twitch=False):
    """Sitting with its back to a wall (south), head fallen forward. Twitching: the head snaps up to look at you."""
    e = [el([5, 0, 10.5], [11, 2, 14], '#bone'), el([7.5, 2, 13], [8.5, 10, 14], '#bone')]
    for y in (3.6, 5.0, 6.4, 7.8):
        e.append(el([4.6, y, 10.6], [11.4, y + 0.8, 14], '#bone'))
    e += [el([7.2, 3.6, 10.3], [8.8, 8.6, 10.6], '#bone'),
          el([4.4, 9.2, 12], [11.6, 10, 13.6], '#bone'),
          *skull_parts(8, 9.6, 9.4, 0.7, ([8, 10, 13], 'x', -12 if twitch else 27.5)),
          el([5.6, 0.2, 2.5], [6.6, 1.2, 10.5], '#bone'),
          el([5.4, 0, 1.5], [6.8, 1.2, 2.8], '#bone'),
          el([9.4, 1, 6], [10.4, 2, 10.6], '#bone', ([10, 1.5, 10.5], 'x', -80 if twitch else -35)),
          el([9.4, 0.2, 3], [10.4, 1.2, 7.2], '#bone', ([10, 0.7, 7], 'x', 30)),
          el([3.8, 3.4, 11.4], [4.8, 9.6, 12.4], '#bone', ([4.3, 9.5, 12], 'z', -6)),
          el([11.2, 3.4, 11.4], [12.2, 9.6, 12.4], '#bone', ([11.7, 9.5, 12], 'z', 6)),
          el([3.7, 0, 6.2], [4.7, 1, 11.6], '#bone'),
          el([10.8, 2, 6.5], [11.8, 2.9, 10.8], '#bone', ([11.3, 2.4, 10.6], 'y', -25)),
          el([4.2, 3.6, 14.1], [11.8, 10.6, 14.4], '#rag', faces=('north', 'south')),
          el([4.5, 2.05, 4], [11.5, 2.3, 10.6], '#rag', faces=('up', 'down'))]
    return e


def skeleton_hanging():
    e = [el([7.5, 6, 7.5], [8.5, 16, 8.5], '#iron'),
         *skull_parts(8, 0.6, 5.3, 0.7, ([8, 5, 8], 'x', 22.5)),
         el([7.5, -8.2, 8], [8.5, 1.2, 9], '#bone')]
    for y in (-1.2, -2.6, -4.0, -5.4):
        e.append(el([5, y, 5.6], [11, y + 0.8, 9.2], '#bone'))
    e += [el([5.5, -9.6, 6], [10.5, -8, 9], '#bone'),
          el([6, -15.8, 7], [7, -9.6, 8], '#bone', ([6.5, -9.6, 7.5], 'z', 4)),
          el([9, -15.8, 7], [10, -9.6, 8], '#bone', ([9.5, -9.6, 7.5], 'z', -3)),
          el([3.8, -6.5, 7], [4.8, 0.6, 8], '#bone', ([4.3, 0.5, 7.5], 'z', -8)),
          el([11.2, -6.5, 7], [12.2, 0.6, 8], '#bone', ([11.7, 0.5, 7.5], 'z', 8)),
          el([4.4, -11.5, 5.1], [11.6, 0.5, 5.3], '#rag', faces=('north', 'south')),
          el([4.4, -11.5, 9.5], [11.6, 0.5, 9.7], '#rag', faces=('north', 'south'))]
    return e


def gibbet():
    e = [el([7.5, 12, 7.5], [8.5, 16, 8.5], '#iron'), el([3, 11, 3], [13, 12, 13], '#iron'), el([3, -12, 3], [13, -11, 13], '#iron')]
    for x, z in ((3, 3), (12, 3), (3, 12), (12, 12), (7.5, 3), (7.5, 12), (3, 7.5), (12, 7.5)):
        e.append(el([x, -11, z], [x + 1, 11, z + 1], '#iron'))
    e += [el([5.5, -11, 6], [10.5, -9.5, 10], '#bone'), el([7.5, -9.5, 9], [8.5, -2, 10], '#bone')]
    for y in (-8.0, -6.4, -4.8):
        e.append(el([5.6, y, 6.6], [10.4, y + 0.8, 10], '#bone'))
    e += [*skull_parts(8, -2.4, 5.8, 0.6, ([8, -2, 8], 'z', 22.5)),
          el([6, -11, 3.6], [7, -10, 9.6], '#bone'), el([9, -11, 3.6], [10, -10, 9.6], '#bone'),
          el([4.2, -7, 5], [5.2, -1, 6], '#bone', ([4.7, -1, 5.5], 'x', -25)),
          el([10.8, -9, 6], [11.8, -3, 7], '#bone')]
    return e


def bone_pile():
    return [el([2, 0, 4], [14, 1.2, 5.2], '#bone', ([8, 0, 4.6], 'y', 25)),
            el([3, 0, 10], [13, 1.2, 11.2], '#bone', ([8, 0, 10.6], 'y', -30)),
            el([6, 1.1, 2], [7.2, 2.3, 14], '#bone', ([6.6, 1.5, 8], 'y', 15)),
            el([9, 0, 3], [10.2, 1.2, 12], '#bone', ([9.6, 0, 7.5], 'y', -40)),
            *skull_parts(6.2, 0, 5.5, 0.6, ([6.2, 0, 8.2], 'y', 30)),
            *skull_parts(11, 0, 8.4, 0.55, ([11, 0, 11], 'y', -45)),
            el([10, 0, 1.5], [15, 0.8, 2.4], '#bone', ([12.5, 0, 2], 'y', 10)),
            el([1, 0, 12], [6, 0.8, 12.9], '#bone', ([3.5, 0, 12.4], 'y', -15))]


def skull_spike():
    return [el([6, 0, 6], [10, 1.5, 10], '#iron'), el([7.3, 1.5, 7.3], [8.7, 21, 8.7], '#iron'),
            *skull_parts(8, 14.5, 5.2, 0.75),
            el([7.6, 21, 7.6], [8.4, 25, 8.4], '#iron')]


decor_model('remains', skeleton_lying(), BONE_T, twitch=skeleton_lying(True))
decor_model('slumped_remains', skeleton_slumped(), BONE_T, twitch=skeleton_slumped(True))
decor_model('hanging_remains', skeleton_hanging(), BONE_T)
decor_model('gibbet_cage', gibbet(), BONE_T)
decor_model('bone_pile', bone_pile(), BONE_T)
decor_model('skull_spike', skull_spike(), BONE_T)

# soul brazier: iron bowl on legs, glowing embers, crossed sheets of full-bright soul fire
brazier = [el([3, 0, 3], [4.5, 6, 4.5], '#iron'), el([11.5, 0, 3], [13, 6, 4.5], '#iron'), el([3, 0, 11.5], [4.5, 6, 13], '#iron'),
           el([11.5, 0, 11.5], [13, 6, 13], '#iron'), el([2, 6, 2], [14, 7, 14], '#iron'),
           el([2, 7, 2], [14, 10.5, 3.5], '#iron'), el([2, 7, 12.5], [14, 10.5, 14], '#iron'),
           el([2, 7, 3.5], [3.5, 10.5, 12.5], '#iron'), el([12.5, 7, 3.5], [14, 10.5, 12.5], '#iron'),
           el([3.5, 7, 3.5], [12.5, 8.6, 12.5], '#embers', faces=('up',), emit=15)]
for ang in (45, -45):
    brazier.append({'from': [1.5, 8, 8], 'to': [14.5, 24, 8], 'rotation': {'origin': [8, 8, 8], 'axis': 'y', 'angle': ang, 'rescale': True},
                    'light_emission': 15, 'shade_direction_override': 'up',
                    'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#fire'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#fire'}}})
write(os.path.join(A, 'models/block/soul_brazier.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False,
                                                          'textures': {'particle': f'{NS}:block/dark_iron', 'iron': f'{NS}:block/dark_iron',
                                                                       'embers': f'{NS}:block/soul_embers', 'fire': f'{NS}:block/soul_fire'},
                                                          'elements': brazier})
write(os.path.join(A, 'blockstates/soul_brazier.json'), {'variants': {'': {'model': f'{NS}:block/soul_brazier'}}})

# ---------------------------------------------------------------------- themed furniture (models face north: front toward -z)
WOOD_T = {'wood': 'ghostwood_planks', 'bone': 'remains_bone', 'iron': 'dark_iron'}
chair = [el([3, 0, 3], [5, 7, 5], '#wood'), el([11, 0, 3], [13, 7, 5], '#wood'), el([3, 0, 11], [5, 7, 13], '#wood'),
         el([11, 0, 11], [13, 7, 13], '#wood'), el([2, 7, 2], [14, 9, 14], '#wood'),
         el([2, 9, 12], [4, 22, 14], '#wood'), el([12, 9, 12], [14, 22, 14], '#wood'),
         el([4, 12, 12.5], [12, 14, 13.5], '#wood'), el([4, 17, 12.5], [12, 19, 13.5], '#wood'),
         el([2, 22, 12], [14, 23, 14], '#wood'), el([6.5, 23, 12.5], [9.5, 25.5, 13.5], '#bone', uv_full={'north': '#skull'})]
decor_model('ghostwood_chair', chair, {**WOOD_T, 'skull': 'remains_skull'})
table = [el([0, 13, 0], [16, 16, 16], '#wood'), el([1, 0, 1], [3, 13, 3], '#wood'), el([13, 0, 1], [15, 13, 3], '#wood'),
         el([1, 0, 13], [3, 13, 15], '#wood'), el([13, 0, 13], [15, 13, 15], '#wood'),
         el([3, 10, 1.5], [13, 12, 2.5], '#wood'), el([3, 10, 13.5], [13, 12, 14.5], '#wood')]
decor_model('ghostwood_table', table, WOOD_T)
cand = [el([5, 0, 5], [11, 1, 11], '#iron'), el([7, 1, 7], [9, 10, 9], '#bone'), el([2.5, 9, 7], [13.5, 10.5, 9], '#bone')]
for cx in (3.5, 7.5, 11.5):
    tall = 5 if cx == 7.5 else 3.5
    base_y = 10.5 if cx != 7.5 else 10
    cand += [el([cx - 0.5, base_y, 7.5], [cx + 1.5, base_y + tall, 8.5], '#candle'),
             {'from': [cx - 1, base_y + tall, 8], 'to': [cx + 2, base_y + tall + 4, 8], 'light_emission': 15, 'shade_direction_override': 'up',
              'rotation': {'origin': [cx + 0.5, base_y, 8], 'axis': 'y', 'angle': 45},
              'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#fire'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#fire'}}},
             {'from': [cx - 1, base_y + tall, 8], 'to': [cx + 2, base_y + tall + 4, 8], 'light_emission': 15, 'shade_direction_override': 'up',
              'rotation': {'origin': [cx + 0.5, base_y, 8], 'axis': 'y', 'angle': -45},
              'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#fire'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#fire'}}}]
decor_model('bone_candelabra', cand, {'bone': 'remains_bone', 'iron': 'dark_iron', 'candle': 'minecraft:block/purple_candle_lit', 'fire': 'soul_fire'})
tomb = [el([3, 1, 6], [13, 12, 10], '#stone', uv_full={'north': '#front'}), el([4, 12, 6], [12, 14, 10], '#stone'),
        el([6, 14, 6], [10, 15, 10], '#stone'), el([2, 0, 4], [14, 1, 13], '#soil')]
decor_model('tombstone', tomb, {'stone': 'soulstone', 'front': 'tombstone_front', 'soil': 'ashen_soil'})
# ---------------------------------------------------------------------- jars: glass, a cork, and whatever was kept in them
JAR_BODY = [el([5, 0, 5], [11, 8, 11], '#glass'), el([6, 8, 6], [10, 9, 10], '#glass'), el([6.5, 9, 6.5], [9.5, 11, 9.5], '#cork')]
for jar, inner, glow in (('jar', None, None), ('soul_jar', 'jar_soul', 15), ('bone_jar', 'jar_bones', None), ('eye_jar', 'jar_eye', 4)):
    parts = ([el([5.5, 0.5, 5.5], [10.5, 6.5, 10.5], '#inner', emit=glow)] if inner else []) + JAR_BODY
    decor_model(jar, parts, {'particle': 'jar_glass', 'glass': 'jar_glass', 'cork': 'jar_cork', **({'inner': inner} if inner else {})})
shapeless = lambda items, result, count=1: {'type': 'minecraft:crafting_shapeless', 'category': 'misc', 'ingredients': items,
                                             'result': {'count': count, 'id': result}}
write(os.path.join(R, 'jar.json'), shaped([' P ', 'G G', 'GGG'], {'P': '#minecraft:planks', 'G': 'minecraft:glass'}, f'{NS}:jar', 3, 'misc'))
write(os.path.join(R, 'soul_jar.json'), shapeless([f'{NS}:jar', f'{NS}:soul'], f'{NS}:soul_jar'))
write(os.path.join(R, 'bone_jar.json'), shapeless([f'{NS}:jar', 'minecraft:bone', 'minecraft:bone'], f'{NS}:bone_jar'))
write(os.path.join(R, 'eye_jar.json'), shapeless([f'{NS}:jar', 'minecraft:spider_eye', f'{NS}:soul'], f'{NS}:eye_jar'))


# ---------------------------------------------------------------------- Underworld potions: brewed in any stand, from what's found below
def brew(base, reagent, out):
    for c in ('potion', 'splash_potion', 'lingering_potion'):
        b = base if ':' in base else f'{NS}:{base}'
        write(os.path.join(R, f'brewing/{c}_{out}_{reagent.split(":")[1]}.json'), {
            'type': 'minecraft:brewing',
            'input': {'item': f'minecraft:{c}', 'potion_contents': {'potions': b}},
            'reagent': {'item': reagent},
            'output': {'id': f'minecraft:{c}', 'components': {'minecraft:potion_contents': {'potion': f'{NS}:{out}'}}}})


brew('minecraft:awkward', f'{NS}:soul', 'warding')
brew('warding', 'minecraft:redstone', 'long_warding')
brew('minecraft:awkward', f'{NS}:eye_jar', 'grave_sight')
brew('grave_sight', 'minecraft:redstone', 'long_grave_sight')
brew('minecraft:awkward', f'{NS}:grave_rune', 'last_breath')


decor_model('skull', skull_parts(gape=8), SKULL_T)
# a skull with one of the Hunter's arrows clean through it: the sign you're on his ground
pierced = skull_parts()
pierced += [el([-1, 4.6, 8.7], [17, 5.2, 9.3], '#shaft', ([8, 4.9, 9], 'y', 22.5)),
            el([-1.5, 4.0, 9.0], [2.0, 5.8, 9.0], '#fletch', ([8, 4.9, 9], 'y', 22.5), faces=('north', 'south'), emit=15),
            el([-1.5, 4.9, 8.1], [2.0, 4.9, 9.9], '#fletch', ([8, 4.9, 9], 'y', 22.5), faces=('up', 'down'), emit=15),
            el([16.5, 4.5, 8.6], [18.5, 5.3, 9.4], '#head', ([8, 4.9, 9], 'y', 22.5))]
decor_model('pierced_skull', pierced, {**SKULL_T, **ARROW_T})
# ---------------------------------------------------------------------- the Ferryman's boat
def ferry_parts():
    """The whole boat in boat coordinates (L along the keel, 0 at the stern to 64 at the bow; x across, centred on 8;
    y up, sunk 1.5 into the silt), cut into four block models. Bow toward model-north."""
    sink = -1.5

    def w(L):   # half-beam
        if L < 6:
            return 5.8 + L / 6 * 1.7
        if L > 44:
            return max(0.9, 7.5 * (1 - ((L - 44) / 20) ** 1.7))
        return 7.5

    def h(L):   # height of the gunwale
        return 6.5 + max(0.0, (L - 42) / 22) ** 2 * 6.5 + max(0.0, (6 - L) / 6) * 1.8

    parts = []   # (L0, L1, element dict in boat coords with z standing in for L)

    def box(x0, y0, L0, x1, y1, L1, tex, uv=None, rot=None, emit=None, faces=DIRS):
        e = el([x0, y0, L0], [x1, y1, L1], tex, rot, faces=faces, emit=emit)
        if uv:
            for f, v in uv.items():
                if f in e['faces']:
                    e['faces'][f]['uv'] = v
        parts.append(e)

    for L0 in range(0, 64, 2):
        Lm = L0 + 1
        ww, hh = w(Lm), h(Lm)
        u = L0 % 16
        top = 16 - min(16, hh - sink)
        # floor, the two sides (planked, the grain running along the hull), and the gunwale capping them
        if ww > 1.3:
            box(8 - ww + 1, sink, L0, 8 + ww - 1, sink + 1, L0 + 2, '#planks', uv={'up': [u, 0, u + 2, 16]})
        for side in (-1, 1):
            x0 = 8 - ww if side < 0 else 8 + ww - 1
            box(x0, sink, L0, x0 + 1, hh, L0 + 2, '#planks', uv={f: [u, top, u + 2, 16] for f in ('east', 'west')})
            cx0 = 8 - ww - 0.4 if side < 0 else 8 + ww - 0.6
            box(cx0, hh, L0, cx0 + 1.0, hh + 0.9, L0 + 2, '#dark')
    # bone ribs inside, every ten
    for L in (10, 20, 30, 40, 50):
        ww, hh = w(L), h(L)
        box(8 - ww + 1, sink + 1, L, 8 + ww - 1, sink + 1.6, L + 1, '#bone')
        box(8 - ww + 1, sink + 1, L, 8 - ww + 1.8, hh - 0.6, L + 1, '#bone')
        box(8 + ww - 1.8, sink + 1, L, 8 + ww - 1, hh - 0.6, L + 1, '#bone')
    # two thwarts, a rag left on one
    for L in (22, 38):
        box(8 - w(L) + 1, 3.0, L, 8 + w(L) - 1, 4.0, L + 3, '#planks')
    box(4.5, 4.0, 38.4, 9.0, 4.4, 41.6, '#rag')
    box(8.6, 1.2, 39.0, 9.0, 4.0, 41.0, '#rag')
    # the transom, and the long steering oar trailing back into the mud
    box(8 - w(0.5), sink, 0, 8 + w(0.5), h(0.5), 1, '#planks')
    box(11.4, 6.0, -12, 12.4, 7.0, 4, '#dark', rot=([11.9, 6.5, 4], 'x', -22.5))
    box(10.6, 5.6, -13, 13.2, 6.1, -7.5, '#planks', rot=([11.9, 6.5, 4], 'x', -22.5))
    # fares nobody collected, in the bilge; a skull that rode down and never got off
    for x, L in ((6, 14), (9.5, 17), (7.5, 27), (10, 33), (5.8, 45)):
        box(x, sink + 1, L, x + 1.2, sink + 1.3, L + 1.2, '#gold')
    # the stem: up from the keel, curling forward, a skull for a figurehead, and an iron hook with the soul lantern
    box(7.1, sink, 62, 8.9, 12.5, 64, '#dark')
    box(7.1, 12.5, 63, 8.9, 15.5, 65.5, '#dark')
    box(7.5, 12.5, 61.0, 8.5, 22.5, 62.0, '#iron')
    box(7.5, 21.7, 62.0, 8.5, 22.5, 72.0, '#iron')
    box(7.75, 19.6, 71.1, 8.25, 21.7, 71.6, '#iron')
    box(6.6, 15.8, 70.0, 9.4, 16.4, 72.8, '#iron')
    box(6.6, 19.0, 70.0, 9.4, 19.6, 72.8, '#iron')
    for dx, dz in ((6.6, 70.0), (8.8, 70.0), (6.6, 72.2), (8.8, 72.2)):
        box(dx, 16.4, dz, dx + 0.6, 19.0, dz + 0.6, '#iron')
    box(7.2, 16.4, 70.6, 8.8, 18.8, 72.2, '#flame', emit=15)
    return parts


def ferry_models():
    """Cut the boat into its four blocks: z = 16k + 16 - L, so the bow (high L) is model-north of each block."""
    import copy
    segs = [[] for _ in range(4)]
    for e in ferry_parts():
        L0, L1 = e['from'][2], e['to'][2]
        k = max(0, min(3, int(((L0 + L1) / 2) // 16)))
        e = copy.deepcopy(e)
        e['from'][2], e['to'][2] = 16 * k + 16 - L1, 16 * k + 16 - L0
        if 'rotation' in e:
            o = e['rotation']['origin']
            o[2] = 16 * k + 16 - o[2]
            if e['rotation']['axis'] == 'x':
                e['rotation']['angle'] = -e['rotation']['angle']
        # north/south faces swap when the axis is mirrored
        f = e['faces']
        if 'north' in f or 'south' in f:
            f['north'], f['south'] = f.get('south'), f.get('north')
            e['faces'] = {k2: v for k2, v in f.items() if v is not None}
        segs[k].append(e)
    skull = skull_parts(8, 15.6, 16 * 3 + 16 - 67.5, 0.62)   # the figurehead, looking downriver
    segs[3] += skull
    return segs


FERRY_T = {'particle': 'ferry_planks', 'planks': 'ferry_planks', 'dark': 'ferry_dark', 'bone': 'remains_bone', 'rag': 'remains_rag',
           'iron': 'dark_iron', 'gold': 'minecraft:block/gold_block', 'flame': 'minecraft:block/soul_fire_0', **SKULL_T}
for k, elements in enumerate(ferry_models()):
    tex = {key: v if ':' in v else f'{NS}:block/{v}' for key, v in FERRY_T.items()}
    write(os.path.join(A, 'models/block', f'ferry_{k}.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False, 'textures': tex, 'elements': elements})
write(os.path.join(A, 'blockstates/ferry.json'), {'variants': {
    f'facing={f},part={k}': {'model': f'{NS}:block/ferry_{k}', **({'y': r} if r else {})}
    for f, r in (('north', 0), ('east', 90), ('south', 180), ('west', 270)) for k in range(4)}})
item_model('ferry', f'{NS}:block/ferry_3')
self_drop('ferry')

# ---------------------------------------------------------------------- the decoration pass
for _p in ('dead_grass', 'tall_dead_grass', 'ghost_bloom'):
    write(os.path.join(A, f'blockstates/{_p}.json'), {'variants': {'': {'model': f'{NS}:block/{_p}'}}})
    write(os.path.join(A, f'models/block/{_p}.json'), {'parent': 'minecraft:block/cross', 'textures': {'cross': f'{NS}:block/{_p}'}})
    generated(_p, texture=f'{NS}:block/{_p}')
    write(os.path.join(D, f'loot_table/blocks/{_p}.json'), {'type': 'minecraft:block', 'pools': [], 'random_sequence': f'{NS}:blocks/{_p}'})
write(os.path.join(A, 'blockstates/ashen_lily.json'), {'variants': {'': [{'model': f'{NS}:block/ashen_lily', 'y': r} for r in (0, 90, 180, 270)]}})
write(os.path.join(A, 'models/block/ashen_lily.json'), {'ambientocclusion': False, 'textures': {'particle': f'{NS}:block/ashen_lily', 'texture': f'{NS}:block/ashen_lily'},
      'elements': [{'from': [0, 0.25, 0], 'to': [16, 0.25, 16], 'faces': {'down': {'uv': [16, 16, 0, 0], 'texture': '#texture'}, 'up': {'uv': [16, 0, 0, 16], 'texture': '#texture'}}}]})
generated('ashen_lily', texture=f'{NS}:block/ashen_lily')
self_drop('ashen_lily')
WHET = [el([2, 0, 5], [4, 10, 11], '#bone'), el([12, 0, 5], [14, 10, 11], '#bone'), el([2, 0, 5], [14, 1.5, 11], '#wood'),
        el([4, 7, 7.5], [12, 8, 8.5], '#iron'), el([5, 2, 6.5], [11, 14, 9.5], '#stone'), el([4, 3, 6.5], [12, 13, 9.5], '#stone'),
        el([12, 7.2, 7.6], [15.5, 7.8, 8.4], '#iron'), el([15, 7.2, 7.6], [15.6, 10.5, 8.4], '#wood')]
decor_model('whetstone', WHET, {'particle': 'soulstone', 'bone': 'remains_bone', 'wood': 'ferry_dark', 'iron': 'dark_iron', 'stone': 'polished_soulstone'})
BLADE = [el([7.7, 15, 7.7], [8.3, 16, 8.3], '#iron'), el([7.4, 13.5, 7.4], [8.6, 15, 8.6], '#iron'), el([7.5, 10, 7.5], [8.5, 13.5, 8.5], '#wood'),
         el([5, 9, 7.4], [11, 10, 8.6], '#iron'), el([7.3, 0.5, 7.85], [8.7, 9, 8.15], '#blade'), el([7.6, 0, 7.85], [8.4, 0.5, 8.15], '#blade')]
decor_model('hanging_blade', BLADE, {'particle': 'dark_iron', 'iron': 'dark_iron', 'wood': 'ferry_dark', 'blade': 'minecraft:block/iron_block'})
COFFIN = [el([3, 0, 11], [13, 30, 16], '#wood'), el([2, 6, 11], [14, 22, 16], '#wood'),
          el([3.5, 1, 10.2], [12.5, 29, 11], '#lid'), el([2.5, 7, 10.2], [13.5, 21, 11], '#lid'),
          el([7.5, 12, 9.8], [8.5, 22, 10.2], '#iron'), el([5, 18, 9.8], [11, 19, 10.2], '#iron'),
          el([2, 3, 12], [2.6, 5, 15], '#iron'), el([13.4, 3, 12], [14, 5, 15], '#iron')]
decor_model('coffin', COFFIN, {'particle': 'ferry_planks', 'wood': 'ferry_planks', 'lid': 'ferry_dark', 'iron': 'dark_iron'})
URN = [el([4.5, 0, 4.5], [11.5, 1, 11.5], '#stone'), el([4, 1, 4], [12, 8, 12], '#stone'), el([3.8, 4, 3.8], [12.2, 5, 12.2], '#band'),
       el([5, 8, 5], [11, 10, 11], '#stone'), el([4.5, 10, 4.5], [11.5, 11, 11.5], '#iron'), el([7, 11, 7], [9, 12.5, 9], '#iron')]
decor_model('urn', URN, {'particle': 'polished_soulstone', 'stone': 'polished_soulstone', 'band': 'soul_veined_bricks', 'iron': 'dark_iron'})
BANNER = [el([0, 30, 14.2], [16, 31.2, 15.4], '#pole'), el([7.6, 31.2, 14.6], [8.4, 32, 15.2], '#iron'),
          {'from': [2, 6, 15], 'to': [14, 30, 15], 'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#cloth'}, 'south': {'uv': [16, 0, 0, 16], 'texture': '#cloth'}}}]
decor_model('tattered_banner', BANNER, {'particle': 'tattered_banner', 'cloth': 'tattered_banner', 'pole': 'ferry_dark', 'iron': 'dark_iron'})
CHAND = [el([7.7, 12, 7.7], [8.3, 16, 8.3], '#iron'), el([7, 9, 7], [9, 12, 9], '#skull_bone'),
         el([2, 8, 7.4], [14, 9, 8.6], '#bone'), el([7.4, 8, 2], [8.6, 9, 14], '#bone')]
for x, z in ((2.5, 8), (13.5, 8), (8, 2.5), (8, 13.5)):
    CHAND += [el([x - 0.8, 9, z - 0.8], [x + 0.8, 11.5, z + 0.8], '#candle'), el([x - 0.4, 11.5, z - 0.4], [x + 0.4, 12.6, z + 0.4], '#fire', emit=15)]
decor_model('bone_chandelier', CHAND, {'particle': 'remains_bone', 'bone': 'remains_bone', 'skull_bone': 'skull_bone', 'iron': 'dark_iron',
                                        'candle': 'minecraft:block/purple_candle_lit', 'fire': 'soul_fire'})

# a giant's ribs and spine, for arches: straight, bending over, and the spine along the top
RIB_T = {'particle': 'remains_bone', 'bone': 'remains_bone'}
_ribs = {0: [el([6, 0, 6], [10, 16, 10], '#bone'), el([5.5, 11, 5.5], [10.5, 12.5, 10.5], '#bone')],
         1: [el([6, -4, 6], [10, 20, 10], '#bone', ([8, 8, 8], 'x', -45))],
         2: [el([0, 6, 6], [16, 10, 10], '#bone'), el([2, 4.5, 4.5], [5, 11.5, 11.5], '#bone'), el([11, 4.5, 4.5], [14, 11.5, 11.5], '#bone'),
             el([3, 11.5, 7.2], [4, 14.5, 8.8], '#bone'), el([12, 11.5, 7.2], [13, 14.5, 8.8], '#bone')]}
for _k, _els in _ribs.items():
    write(os.path.join(A, 'models/block', f'giant_rib_{_k}.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False,
          'textures': {k: f'{NS}:block/{v}' for k, v in RIB_T.items()}, 'elements': _els})
write(os.path.join(A, 'blockstates/giant_rib.json'), {'variants': {f'facing={f},shape={k}': {'model': f'{NS}:block/giant_rib_{k}', **({'y': r} if r else {})}
      for f, r in (('north', 0), ('east', 90), ('south', 180), ('west', 270)) for k in range(3)}})
item_model('giant_rib', f'{NS}:block/giant_rib_1')
self_drop('giant_rib')

# his trophies: a bone stake driven into the ground, a skull rammed down on it, an arrow through both, a rag tied under
STAKE = [el([7.1, 0, 7.1], [8.9, 19, 8.9], '#stake'), el([7.5, 24.4, 7.6], [8.5, 27.2, 8.6], '#stake'),
         el([6.7, 12.2, 6.7], [9.3, 13.6, 9.3], '#rag'), el([9.3, 8.2, 7.7], [9.5, 13.6, 8.5], '#rag'),
         el([5.2, 0, 8.6], [7.2, 0.7, 9.4], '#stake'), el([9.4, 0, 6.2], [10.6, 0.7, 7.6], '#stake')]
STAKE += skull_parts(8, 17.2, 4.5, 0.95)
STAKE += [el([-2, 21.3, 8.2], [18, 21.9, 8.8], '#shaft', ([8, 21.6, 8.5], 'y', -22.5)),
          el([-2.5, 20.7, 8.5], [1.0, 22.5, 8.5], '#fletch', ([8, 21.6, 8.5], 'y', -22.5), faces=('north', 'south'), emit=15),
          el([-2.5, 21.6, 7.6], [1.0, 21.6, 9.4], '#fletch', ([8, 21.6, 8.5], 'y', -22.5), faces=('up', 'down'), emit=15),
          el([17.5, 21.2, 8.1], [19.5, 22.0, 8.9], '#head', ([8, 21.6, 8.5], 'y', -22.5))]
STAKE += arrow_parts(11.5, 11, 'x', 22, 7.5)
decor_model('hunter_stake', STAKE, {**SKULL_T, **ARROW_T, 'stake': 'remains_bone', 'rag': 'remains_rag'})
decor_model('stuck_arrows', arrow_parts(5, 6, 'x', 25) + arrow_parts(10.5, 9, 'z', -20) + arrow_parts(7, 12.5, 'x', -18, 7.5), ARROW_T)

# bone-wood logs for the Hollow, and the curio shelf
from_vanilla('blockstates', 'basalt', 'ossified_log', [('minecraft:block/basalt', f'{NS}:block/ossified_log')])
write(os.path.join(A, 'models/block/ossified_log.json'), {'parent': 'minecraft:block/cube_column', 'textures': {'end': f'{NS}:block/ossified_log_top', 'side': f'{NS}:block/ossified_log'}})
write(os.path.join(A, 'models/block/ossified_log_horizontal.json'), {'parent': 'minecraft:block/cube_column_horizontal', 'textures': {'end': f'{NS}:block/ossified_log_top', 'side': f'{NS}:block/ossified_log'}})
item_model('ossified_log')
self_drop('ossified_log')

# glowcaps: pale stems, caps that light the Collector's room
CAP_T = {'particle': 'glowcap_cap', 'cap': 'glowcap_cap', 'stem': 'glowcap_stem', 'gill': 'glowcap_gill'}


def cap(fr, to):
    e = el(fr, to, '#cap', emit=15)
    e['faces']['down']['texture'] = '#gill'
    return e


decor_model('glowcap', [el([6.5, 0, 6.5], [7.5, 6, 7.5], '#stem'), cap([4.5, 6, 4.5], [9.5, 7.5, 9.5]),
                        el([10, 0, 9], [11, 3.5, 10], '#stem'), cap([8.5, 3.5, 7.5], [12.5, 4.5, 11.5]),
                        el([4, 0, 10.5], [4.6, 2, 11.1], '#stem'), cap([3, 2, 9.5], [5.6, 2.8, 12.1])], CAP_T)
def bracket(x0, x1, y, depth):
    # shelf fungus: a thick lip with a domed top, grown out of the wall at z=16
    return [cap([x0, y, 16 - depth], [x1, y + 1.5, 16]), cap([x0 + 1, y + 1.5, 16 - depth + 1.2], [x1 - 1, y + 2.4, 16])]


decor_model('wall_glowcap', bracket(3, 13, 5, 5.5) + bracket(5, 11, 9.5, 4) + bracket(8, 13, 1.5, 3.5) + bracket(2, 6, 12, 2.5), CAP_T)

# jars with something worth keeping in them
CURIO_T = {'gold': 'curio_gold', 'iron': 'curio_iron', 'hull': 'curio_hull', 'sail': 'curio_sail', 'heart': 'curio_heart', 'moth': 'curio_moth'}
crown = [el([6.2, 0.5, 6.2], [9.8, 1.5, 9.8], '#gold')] + [el([x, 1.5, z], [x + 0.8, 2.8, z + 0.8], '#gold') for x, z in ((6.2, 6.2), (9.0, 6.2), (6.2, 9.0), (9.0, 9.0), (7.6, 6.2))]
keys = [el([6, 0.5, 7.5], [10, 1, 8], '#iron', ([8, 0.5, 8], 'y', 25)), el([5, 0.5, 7], [6.5, 2, 8.5], '#iron', ([8, 0.5, 8], 'y', 25)),
        el([6.5, 1, 6.5], [10.5, 1.5, 7], '#gold', ([8, 1, 7], 'y', -30)), el([10, 1, 6], [11.2, 2.4, 7.4], '#gold', ([8, 1, 7], 'y', -30))]
heart = [el([6.3, 0.5, 6.8], [9.7, 3.5, 9.2], '#heart', emit=4), el([6.8, 3.5, 7.2], [8, 4.2, 8.8], '#heart'), el([8.4, 3.2, 7.4], [9.2, 4.6, 8.4], '#heart')]
moth = [el([7.8, 0.5, 7.8], [8.2, 4.5, 8.2], '#iron'), el([5.5, 3, 8], [10.5, 6.2, 8], '#moth', faces=('north', 'south'), emit=6)]
for jar, inner in (('jar_crown', crown), ('jar_keys', keys), ('jar_heart', heart), ('jar_moth', moth)):
    decor_model(jar, inner + JAR_BODY, {'particle': 'jar_glass', 'glass': 'jar_glass', 'cork': 'jar_cork', **CURIO_T})
ship = [el([3, 0, 5.5], [13, 5, 10.5], '#glass'), el([13, 1.5, 6.5], [15, 3.5, 9.5], '#glass'), el([15, 1.8, 7], [16, 3.2, 9], '#cork'),
        el([5, 0.4, 7], [11, 1.4, 9], '#hull'), el([4.5, 1.0, 7.4], [5, 1.8, 8.6], '#hull'), el([7.8, 1.4, 7.8], [8.2, 4.4, 8.2], '#hull'),
        el([6.4, 1.9, 8], [9.6, 4.2, 8], '#sail', faces=('north', 'south'))]
decor_model('bottled_ship', ship, {'particle': 'jar_glass', 'glass': 'jar_glass', 'cork': 'jar_cork', **CURIO_T})
write(os.path.join(A, 'models/block/ossuary_shelf.json'), {'parent': 'minecraft:block/cube_column',
       'textures': {'side': f'{NS}:block/ossuary_shelf', 'end': f'{NS}:block/ghostwood_planks'}})
write(os.path.join(A, 'blockstates/ossuary_shelf.json'), {'variants': {'': {'model': f'{NS}:block/ossuary_shelf'}}})
item_model('ossuary_shelf')
self_drop('ossuary_shelf')
item_model('soul_brazier')
self_drop('soul_brazier')

# full-bright crystals and shards (cross_emissive draws the same art again at full light)
for name in ('soul_crystal', 'small_soul_shard', 'medium_soul_shard', 'large_soul_shard'):
    write(os.path.join(A, 'models/block', name + '.json'), {'parent': 'minecraft:block/cross_emissive',
                                                            'textures': {'cross': f'{NS}:block/{name}', 'cross_emissive': f'{NS}:block/{name}'}})
    if name != 'soul_crystal':
        from_vanilla('blockstates', 'amethyst_cluster', name, [('minecraft:block/amethyst_cluster', f'{NS}:block/{name}')])
        generated(name, texture=f'{NS}:block/{name}')
        self_drop(name)


def glowing_cube(name, base, glow):
    cube = {f: {'uv': [0, 0, 16, 16], 'texture': '#all', 'cullface': f} for f in DIRS}
    over = {f: {'uv': [0, 0, 16, 16], 'texture': '#glow', 'cullface': f} for f in DIRS}
    write(os.path.join(A, 'models/block', name + '.json'), {'parent': 'minecraft:block/block', 'textures': {
        'particle': f'{NS}:block/{base}', 'all': f'{NS}:block/{base}', 'glow': f'{NS}:block/{glow}'},
        'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': cube},
                     {'from': [0, 0, 0], 'to': [16, 16, 16], 'light_emission': 15, 'faces': over}]})


# the Collector's curio shelf: ghostwood crowded with glowing glass
glowing_cube('curio_shelf', 'curio_shelf', 'curio_shelf_glow')

# ---------------------------------------------------------------------- the First Hunter's Bow: vanilla's pull states, his sprites
from_vanilla('items', 'bow', 'hunters_bow', [('minecraft:item/bow', f'{NS}:item/hunters_bow')])
write(os.path.join(A, 'models/item/hunters_bow.json'), {'parent': 'minecraft:item/bow', 'textures': {'layer0': f'{NS}:item/hunters_bow'}})
for i in range(3):
    write(os.path.join(A, f'models/item/hunters_bow_pulling_{i}.json'), {'parent': 'minecraft:item/bow', 'textures': {'layer0': f'{NS}:item/hunters_bow_pulling_{i}'}})

# ---------------------------------------------------------------------- the forge: armor on a skeleton, blades on a board, a black anvil, a bedroll
RACK = [el([4, 0, 4], [12, 1, 12], '#wood'), el([7.5, 1, 7.5], [8.5, 22, 8.5], '#bone'), el([2, 19.5, 7.5], [14, 20.5, 8.5], '#bone'),
        el([4, 11, 6], [12, 19.5, 10], '#iron'), el([2.2, 17.5, 5.6], [5.2, 21, 10.4], '#iron'), el([10.8, 17.5, 5.6], [13.8, 21, 10.4], '#iron'),
        el([4.5, 7.5, 6.5], [11.5, 11, 9.5], '#rag'), el([5.6, 21, 5.6], [10.4, 25.5, 10.4], '#skull', uv_full={'north': '#face'}),
        el([5, 24.5, 5], [11, 28, 11], '#iron'), el([7.5, 22.5, 4.6], [8.5, 25, 5.0], '#iron'), el([4.6, 22, 7], [5, 25.5, 9], '#iron'),
        el([11, 22, 7], [11.4, 25.5, 9], '#iron')]
decor_model('armor_rack', RACK, {'particle': 'dark_iron', 'wood': 'ghostwood_planks', 'bone': 'remains_bone', 'iron': 'dark_iron', 'rag': 'remains_rag',
                                 'skull': 'skull_bone', 'face': 'skull_front'})
WRACK = [el([1, 1, 14.5], [15, 15, 16], '#wood'), el([1, 0, 13.5], [3, 16, 16], '#wood'), el([13, 0, 13.5], [15, 16, 16], '#wood'),
         el([2, 11, 12], [14, 12, 14.5], '#wood'), el([2, 3, 12], [14, 4, 14.5], '#wood')]
for x in (5, 8, 11):
    WRACK += [el([x - 0.5, 2.5, 12.6], [x + 0.5, 12.5, 13.4], '#iron'), el([x - 1.6, 12.5, 12.2], [x + 1.6, 13.3, 13.8], '#iron'),
              el([x - 0.4, 13.3, 12.6], [x + 0.4, 15.6, 13.4], '#bone')]
decor_model('weapon_rack', WRACK, {'particle': 'ghostwood_planks', 'wood': 'ghostwood_planks', 'iron': 'dark_iron', 'bone': 'remains_bone'})
ANVIL = [el([3, 0, 4], [13, 2.5, 12], '#iron'), el([5, 2.5, 5.5], [11, 6, 10.5], '#iron'), el([1, 6, 4.5], [15, 10, 11.5], '#iron'),
         el([-1.5, 7.2, 6.5], [1, 9.6, 9.5], '#iron'), el([15, 7.5, 6], [16.5, 10, 10], '#iron')]
decor_model('grave_anvil', ANVIL, {'particle': 'dark_iron', 'iron': 'dark_iron'})
ROLL = [el([1, 0, 3], [15, 1.6, 13], '#rag'), el([11, 1.6, 4], [15, 3.2, 12], '#rag'), el([1, 1.6, 3.5], [4, 3.0, 12.5], '#rag')]
decor_model('bedroll', ROLL, {'particle': 'remains_rag', 'rag': 'remains_rag'})

# ---------------------------------------------------------------------- the Ferryman's fare bowl
FARE = [el([4, 0, 4], [12, 1, 12], '#iron'), el([3, 1, 3], [13, 3, 4], '#iron'), el([3, 1, 12], [13, 3, 13], '#iron'),
        el([3, 1, 4], [4, 3, 12], '#iron'), el([12, 1, 4], [13, 3, 12], '#iron'),
        el([4.5, 1, 4.5], [11.5, 2.2, 11.5], '#gold'), el([6, 2.2, 6], [10, 3, 9.5], '#gold'), el([7.5, 3, 7], [9, 3.6, 8.5], '#gold')]
decor_model('fare_bowl', FARE, {'particle': 'dark_iron', 'iron': 'dark_iron', 'gold': 'curio_gold'})

# ---------------------------------------------------------------------- the puzzles that hide the keys to the Death King's gate
BELL = [el([7, 14, 7], [9, 16, 9], '#iron'), el([5.5, 12, 5.5], [10.5, 14, 10.5], '#iron'), el([4.5, 7, 4.5], [11.5, 12, 11.5], '#iron'),
        el([3.5, 5, 3.5], [12.5, 7, 12.5], '#iron'), el([7.5, 3.5, 7.5], [8.5, 5, 8.5], '#bone')]
decor_model('grave_bell', BELL, {'particle': 'dark_iron', 'iron': 'dark_iron', 'bone': 'skull_bone'})
FLAME = lambda angle: {'from': [5, 8, 8], 'to': [11, 15, 8], 'light_emission': 15, 'shade_direction_override': 'up',
                       'rotation': {'origin': [8, 8, 8], 'axis': 'y', 'angle': angle},
                       'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#fire'}, 'south': {'uv': [0, 0, 16, 16], 'texture': '#fire'}}}
LAMP = [el([5, 0, 5], [11, 2, 11], '#stone'), el([6.5, 2, 6.5], [9.5, 6, 9.5], '#stone'), el([4.5, 6, 4.5], [11.5, 8, 11.5], '#iron'),
        el([4, 7.5, 4], [5, 8.5, 12], '#iron'), el([11, 7.5, 4], [12, 8.5, 12], '#iron')]
LAMP_T = {'particle': f'{NS}:block/soulstone', 'stone': f'{NS}:block/soulstone', 'iron': f'{NS}:block/dark_iron', 'fire': f'{NS}:block/soul_fire'}
for nm, extra in (('grave_lamp', []), ('grave_lamp_lit', [FLAME(45), FLAME(-45)])):
    write(os.path.join(A, 'models/block', nm + '.json'), {'parent': 'minecraft:block/block', 'ambientocclusion': False, 'textures': LAMP_T,
                                                          'elements': LAMP + extra})
write(os.path.join(A, 'blockstates/grave_lamp.json'), {'variants': {'lit=false': {'model': f'{NS}:block/grave_lamp'},
                                                                     'lit=true': {'model': f'{NS}:block/grave_lamp_lit'}}})
item_model('grave_lamp')
TURNS = (('north', 0), ('east', 90), ('south', 180), ('west', 270))
write(os.path.join(A, 'blockstates/watcher_skull.json'), {'variants': {f'facing={f}': {'model': f'{NS}:block/skull', **({'y': r} if r else {})} for f, r in TURNS}})
item_model('watcher_skull', f'{NS}:block/skull')
for kind in ('kings', 'watchers', 'bell'):
    nm = f'lock_{kind}'
    sides = {f: {'uv': [0, 0, 16, 16], 'texture': '#front' if f == 'north' else '#side', 'cullface': f} for f in DIRS}
    tex = {'particle': f'{NS}:block/dark_soulstone_bricks', 'side': f'{NS}:block/dark_soulstone_bricks', 'front': f'{NS}:block/{nm}',
           'glow': f'{NS}:block/{nm}_glow'}
    write(os.path.join(A, 'models/block', nm + '.json'), {'parent': 'minecraft:block/block', 'textures': tex,
                                                          'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': sides}]})
    write(os.path.join(A, 'models/block', nm + '_filled.json'), {'parent': 'minecraft:block/block', 'textures': tex, 'elements': [
        {'from': [0, 0, 0], 'to': [16, 16, 16], 'faces': sides},
        {'from': [0, 0, -0.01], 'to': [16, 16, 0], 'light_emission': 15, 'faces': {'north': {'uv': [0, 0, 16, 16], 'texture': '#glow', 'cullface': 'north'}}}]})
    write(os.path.join(A, 'blockstates', nm + '.json'), {'variants': {
        f'facing={f},filled={b}': {'model': f'{NS}:block/{nm}' + ('_filled' if b == 'true' else ''), **({'y': r} if r else {})}
        for f, r in TURNS for b in ('false', 'true')}})
    item_model(nm)
    generated(f'sigil_{kind}')
generated('spent_chain')
generated('soulwater_bucket')
for _q in ('underworld_journal', 'aldous_lantern', 'miras_ribbon', 'ferrymans_oar', 'pips_ball', 'sentrys_tag', 'lantern_wisp'):
    generated(_q)
chest('quest_oar', [pool([entry(f'{NS}:ferrymans_oar')]), pool([entry('minecraft:bone', 1, 3), entry(f'{NS}:soul', 1, 2)], rolls(1, 2))])
chest('quest_ball', [pool([entry(f'{NS}:pips_ball')]), pool([entry('minecraft:bone', 1, 2)])])
chest('quest_tag', [pool([entry(f'{NS}:sentrys_tag')]), pool([entry('minecraft:arrow', 2, 6), entry(f'{NS}:soul', 1, 3)], rolls(1, 2))])
write(os.path.join(A, 'blockstates/soulwater.json'), {'variants': {'': {'model': f'{NS}:block/soulwater'}}})
write(os.path.join(A, 'models/block/soulwater.json'), {'textures': {'particle': f'{NS}:block/soulwater_still'}})
write(os.path.join(A, 'blockstates/curio_shelf.json'), {'variants': {'': {'model': f'{NS}:block/curio_shelf'}}})
item_model('curio_shelf')
self_drop('curio_shelf')



glowing_cube('chiseled_soulstone', 'chiseled_soulstone', 'chiseled_soulstone_glow')
for b in ('polished_soulstone', 'dark_soulstone_bricks', 'soul_veined_bricks'):
    simple_block(b)
    self_drop(b)
glowing_cube('soul_veined_bricks', 'soul_veined_bricks', 'soul_veined_bricks_glow')
pick += ['polished_soulstone', 'dark_soulstone_bricks', 'soul_veined_bricks', 'small_soul_shard', 'medium_soul_shard', 'large_soul_shard', 'soul_brazier', 'skull_spike']
write(os.path.join(MC, 'tags/block/mineable/pickaxe.json'), tag(pick))

# ====================================================================== language
lang = {
    'itemGroup.deathbound': 'DeathBound',
    'item.deathbound.deathbound_relic': 'Deathbound Relic',
    'effect.deathbound.warding': 'Warding',
    'item.minecraft.potion.effect.deathbound.warding': 'Potion of Warding',
    'item.minecraft.splash_potion.effect.deathbound.warding': 'Splash Potion of Warding',
    'item.minecraft.lingering_potion.effect.deathbound.warding': 'Lingering Potion of Warding',
    'item.minecraft.tipped_arrow.effect.deathbound.warding': 'Arrow of Warding',
    'effect.deathbound.grave_sight': 'Grave Sight',
    'item.minecraft.potion.effect.deathbound.grave_sight': 'Potion of Grave Sight',
    'item.minecraft.splash_potion.effect.deathbound.grave_sight': 'Splash Potion of Grave Sight',
    'item.minecraft.lingering_potion.effect.deathbound.grave_sight': 'Lingering Potion of Grave Sight',
    'item.minecraft.tipped_arrow.effect.deathbound.grave_sight': 'Arrow of Grave Sight',
    'effect.deathbound.last_breath': 'Last Breath',
    'item.minecraft.potion.effect.deathbound.last_breath': 'Potion of Last Breath',
    'item.minecraft.splash_potion.effect.deathbound.last_breath': 'Splash Potion of Last Breath',
    'item.minecraft.lingering_potion.effect.deathbound.last_breath': 'Lingering Potion of Last Breath',
    'item.minecraft.tipped_arrow.effect.deathbound.last_breath': 'Arrow of Last Breath',
    'block.deathbound.jar': 'Jar',
    'block.deathbound.soul_jar': 'Soul Jar',
    'block.deathbound.bone_jar': 'Jar of Bones',
    'block.deathbound.eye_jar': 'Jar of Eyes',
    'block.deathbound.jar_crown': 'Jar with a Crown in It',
    'block.deathbound.jar_keys': 'Jar of Keys',
    'block.deathbound.jar_heart': 'Jar with a Heart in It',
    'block.deathbound.jar_moth': 'Jar with a Moth in It',
    'block.deathbound.bottled_ship': 'Ship in a Bottle',
    'block.deathbound.skull': 'Skull',
    'block.deathbound.pierced_skull': 'Skull, Shot Through',
    'block.deathbound.hunter_stake': "Hunter's Trophy",
    'block.deathbound.dead_grass': 'Dead Grass',
    'block.deathbound.tall_dead_grass': 'Tall Dead Grass',
    'block.deathbound.ghost_bloom': 'Ghost Bloom',
    'block.deathbound.ashen_lily': 'Ashen Lily',
    'block.deathbound.whetstone': 'Whetstone',
    'block.deathbound.hanging_blade': 'Hanging Blade',
    'block.deathbound.coffin': 'Coffin',
    'block.deathbound.urn': 'Funeral Urn',
    'block.deathbound.tattered_banner': 'Tattered Banner',
    'block.deathbound.bone_chandelier': 'Bone Chandelier',
    'block.deathbound.giant_rib': 'Giant Rib',
    'block.deathbound.ferry': "The Ferryman's Boat",
    'block.deathbound.stuck_arrows': 'Spent Hunter Arrows',
    'block.deathbound.ossified_log': 'Ossified Log',
    'block.deathbound.curio_shelf': 'Curio Shelf',
    'block.deathbound.glowcap': 'Glowcap',
    'block.deathbound.wall_glowcap': 'Shelf Glowcap',
    'entity.deathbound.hunter_arrow': 'Hunter Arrow',
    'item.deathbound.reaper_scythe.reap': 'Sneak + use (%s souls): Soul Reap.',
    'item.deathbound.reaper_scythe.desc2': "Use: Reaper's Step.",
    'item.deathbound.reapers_charm.desc3': 'Healing stops at 30%.',
    'item.deathbound.reapers_charm.desc2': 'Speed and Resistance.',
    'item.deathbound.ferrymans_charm.desc2': 'Three crossings.',
    'item.deathbound.wraiths_charm.desc2': 'and a sip of health.',
    'item.deathbound.seers_charm.desc2': 'and hidden stone.',
    'item.deathbound.soulbound_charm.desc2': 'Shatters after use.',
    'item.deathbound.deathbound_relic.lore': "Cast from a king's funeral bell.",
    'item.deathbound.deathbound_relic.awakened': 'Awakened: cross worlds at will.',
    'item.deathbound.deathbound_relic.use': 'Off hand + die: go below.',
    'item.deathbound.deathbound_relic.sneak': 'Sneak + use: bind charms.',
    'item.deathbound.charm.bind_hint': 'Bind it to a Deathbound Relic.',
    'item.deathbound.soulbound_charm': 'Soulbound Charm',
    'item.deathbound.soulbound_charm.lore': 'Death cannot claim what is yours.',
    'item.deathbound.soulbound_charm.desc': 'Keep everything when you die.',
    'item.deathbound.seers_charm': "Seer's Charm",
    'item.deathbound.seers_charm.lore': 'The dead see what you cannot.',
    'item.deathbound.seers_charm.desc': 'Reveals nearby souls, chests',
    'item.deathbound.wraiths_charm': "Wraith's Charm",
    'item.deathbound.wraiths_charm.lore': 'Each soul makes you more like them.',
    'item.deathbound.wraiths_charm.desc': 'Kills grant Wraith Fury',
    'item.deathbound.ferrymans_charm': "Ferryman's Charm",
    'item.deathbound.ferrymans_charm.lore': 'The Ferryman always takes his coin.',
    'item.deathbound.ferrymans_charm.desc': 'Below: the relic takes you home.',
    'item.deathbound.reapers_charm': "Reaper's Charm",
    'item.deathbound.reapers_charm.lore': 'Nothing left to lose.',
    'item.deathbound.reapers_charm.desc': 'Under 30% health: Strength,',
    'item.deathbound.soul': 'Soul',
    'item.deathbound.heart_of_death': 'Heart of Death',
    'item.deathbound.heart_of_death.lore': 'It still beats. Slowly.',
    'item.deathbound.heart_of_death.desc': 'Use with a relic to awaken it.',
    'item.deathbound.reaper_scythe': "Reaper's Scythe",
    'item.deathbound.reaper_scythe.lore': 'Forged to harvest souls.',
    'item.deathbound.reaper_scythe.reaped': 'Reaped: %s / %s',
    'item.deathbound.reaper_scythe.desc': 'Every 3rd swing: finisher.',
    'item.deathbound.soul_bolt': 'Soul Bolt',
    'item.deathbound.death_orb': 'Death Orb',
    'item.deathbound.gravebound_spawn_egg': 'Gravebound Spawn Egg',
    'item.deathbound.soul_wisp_spawn_egg': 'Soul Wisp Spawn Egg',
    'item.deathbound.deaths_guard_spawn_egg': "Death's Guard Spawn Egg",
    'item.deathbound.death_spawn_egg': 'Death Spawn Egg',
    'item.deathbound.lost_soul_spawn_egg': 'Lost Soul Spawn Egg',
    'block.deathbound.soulstone': 'Soulstone',
    'block.deathbound.soulstone_bricks': 'Soulstone Bricks',
    'block.deathbound.cracked_soulstone_bricks': 'Cracked Soulstone Bricks',
    'block.deathbound.chiseled_soulstone': 'Chiseled Soulstone',
    'block.deathbound.soulstone_tiles': 'Soulstone Tiles',
    'block.deathbound.soulstone_pillar': 'Soulstone Pillar',
    'block.deathbound.soulstone_brick_stairs': 'Soulstone Brick Stairs',
    'block.deathbound.soulstone_brick_slab': 'Soulstone Brick Slab',
    'block.deathbound.soulstone_brick_wall': 'Soulstone Brick Wall',
    'block.deathbound.veiled_soulstone': 'Veiled Soulstone',
    'block.deathbound.ashen_soil': 'Ashen Soil',
    'block.deathbound.gloom_grass': 'Gloom Grass',
    'block.deathbound.ghostwood_log': 'Ghostwood Log',
    'block.deathbound.ghostwood_planks': 'Ghostwood Planks',
    'block.deathbound.soul_crystal': 'Soul Crystal',
    'block.deathbound.wraith_lantern': 'Wraith Lantern',
    'block.deathbound.polished_soulstone': 'Polished Soulstone',
    'block.deathbound.dark_soulstone_bricks': 'Dark Soulstone Bricks',
    'block.deathbound.soul_veined_bricks': 'Soul-Veined Bricks',
    'block.deathbound.small_soul_shard': 'Small Soul Shard',
    'block.deathbound.medium_soul_shard': 'Medium Soul Shard',
    'block.deathbound.large_soul_shard': 'Large Soul Shard',
    'block.deathbound.soul_brazier': 'Soul Brazier',
    'block.deathbound.remains': 'Remains',
    'block.deathbound.slumped_remains': 'Slumped Remains',
    'block.deathbound.hanging_remains': 'Hanging Remains',
    'block.deathbound.gibbet_cage': 'Gibbet Cage',
    'block.deathbound.bone_pile': 'Bone Pile',
    'block.deathbound.skull_spike': 'Skull Spike',
    'block.deathbound.soul_seal': 'Soul Seal',
    'block.deathbound.soul_rift': 'Soul Rift',
    'entity.deathbound.gravebound': 'Gravebound',
    'entity.deathbound.soul_wisp': 'Soul Wisp',
    'entity.deathbound.deaths_guard': "Death's Guard",
    'entity.deathbound.death': 'Death',
    'entity.deathbound.lost_soul': 'Lost Soul',
    'entity.deathbound.shade': 'Shade',
    'entity.deathbound.soul_anchor': 'Soul Anchor',
    'entity.deathbound.soul_bolt': 'Soul Bolt',
    'entity.deathbound.death_orb': 'Death Orb',
    'effect.deathbound.wraith_fury': 'Wraith Fury',
    'boss.deathbound.death.throne': 'Death, Upon the Throne',
    'boss.deathbound.death.reaper': 'Death, the Reaper',
    'boss.deathbound.death.beast': 'Death, Unbound',
    'container.deathbound.relic': 'Deathbound Relic',
    'container.deathbound.relic.hint': 'Bind up to one of each charm',
    'death.attack.deathbound.soul_rend': '%1$s had their soul torn loose',
    'death.attack.deathbound.crush': '%1$s was crushed',
    'death.attack.deathbound.crush.player': '%1$s was crushed by %2$s',
    'death.attack.deathbound.soul_rend.player': '%1$s had their soul torn loose by %2$s',
    'message.deathbound.relic.silent': 'The relic is silent. Only the Ferryman can carry you back from here.',
    'message.deathbound.relic.bound': 'Death holds you here. Conquer him first.',
    'message.deathbound.relic.broken': 'The rite is broken.',
    'message.deathbound.charm.shattered': 'Your %s shatters.',
    'message.deathbound.soulbound.saved': 'What is yours shall not be claimed by death.',
    'message.deathbound.heart.no_relic': 'The heart needs a relic to beat in.',
    'message.deathbound.heart.already': 'Your relic is already awake.',
    'message.deathbound.heart.awakened': 'The relic drinks the heart. It is awake.',
    'message.deathbound.scythe.ready': 'The scythe hungers: sneak and use for Soul Reap.',
    'message.deathbound.scythe.hungry': 'The scythe needs more souls (%s / %s).',
    'message.deathbound.death.bound': 'Death is bound to his throne. Break the Soul Anchors.',
    'cinematic.deathbound.conquer': 'CONQUER DEATH',
    'cinematic.deathbound.forgotten': 'OR BE FORGOTTEN',
    'cinematic.deathbound.journey': 'Your life has ended. Your journey has not.',
    'cinematic.deathbound.return': 'You return to the living.',
    'cinematic.deathbound.were_forgotten': 'YOU HAVE BEEN FORGOTTEN',
    'cinematic.deathbound.were_forgotten.sub': 'The dead keep what they took.',
    'cinematic.deathbound.door': 'The Great Door opens',
    'cinematic.deathbound.death': 'DEATH',
    'cinematic.deathbound.death.throne': 'Upon the Throne',
    'cinematic.deathbound.death.reaper': 'DEATH RISES',
    'cinematic.deathbound.death.beast': 'DEATH, UNBOUND',
    'cinematic.deathbound.victory': 'DEATH IS CONQUERED',
    'cinematic.deathbound.victory.sub': 'Step into the rift to return to life.',
    'cinematic.deathbound.rite.ask': 'Do you truly wish to die?',
    'cinematic.deathbound.rite.ferry': 'The Ferryman hears your coin...',
}
write(os.path.join(D, 'trim_pattern/soulforged.json'), {'asset_id': f'{NS}:soulforged', 'decal': False,
       'description': {'translate': 'trim_pattern.deathbound.soulforged'}})
write(os.path.join(D, 'trim_material/soul.json'), {'palette_id': f'{NS}:trim/soul',
       'description': {'translate': 'trim_material.deathbound.soul', 'color': '#ad70f4'}})
lang.update(lore.LANG)
lore.generate(write, A, D)
talk.write_java(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))
write(os.path.join(A, 'lang/en_us.json'), lang)
print('datagen done')
