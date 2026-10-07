"""Writes the DeathBound advancement tab (data/deathbound/advancement) and its English text.
Criteria named "done" (or the quest ids) use minecraft:impossible and are awarded from code: see world/Milestones.java."""
import json
import os

D = 'src/main/resources/data/deathbound/advancement'
LANG = 'src/main/resources/assets/deathbound/lang/en_us.json'

IMPOSSIBLE = {'trigger': 'minecraft:impossible'}

# id, parent, icon, frame, title, description, criteria (None = one impossible "done"), extra display
TREE = [
    ('root', None, 'deathbound:deathbound_relic', 'task', 'DeathBound', 'Death is only the beginning.',
     {'relic': {'trigger': 'minecraft:inventory_changed', 'conditions': {'items': [{'items': 'deathbound:deathbound_relic'}]}},
      'arrive': {'trigger': 'minecraft:changed_dimension', 'conditions': {'to': 'deathbound:underworld'}}},
     {'background': 'deathbound:block/soulstone_bricks', 'show_toast': False, 'announce_to_chat': False}),
    ('the_other_side', 'root', 'deathbound:soulstone_bricks', 'task', 'The Other Side', 'Wake up in the Underworld.',
     {'arrive': {'trigger': 'minecraft:changed_dimension', 'conditions': {'to': 'deathbound:underworld'}}}, {}),
    ('return_fare', 'the_other_side', 'deathbound:ferrymans_charm', 'task', 'Return Fare', 'Make it back to the living world.', None, {}),
    ('bound', 'the_other_side', 'deathbound:seers_charm', 'task', 'Bound to You', 'Bind a charm to the Deathbound Relic.', None, {}),
    ('full_hand', 'bound', 'deathbound:soulbound_charm', 'goal', 'Full Hand', 'Fill every charm slot on the Relic.', None, {}),
    ('not_quite_there', 'bound', 'deathbound:phantom_charm', 'task', 'Not Quite There', 'Let a hit pass through you with the Phantom Charm.', None, {}),
    ('soulforged', 'the_other_side', 'deathbound:grave_rune', 'task', 'Soulforged', 'Have Aldous forge souls into your armor.', None, {}),
    ('errand', 'the_other_side', 'deathbound:underworld_journal', 'task', 'An Errand for the Dead', 'Finish a task for someone in the Underworld.', None, {}),
    ('keep_it_lit', 'errand', 'deathbound:aldous_lantern', 'task', 'Keep It Lit', "Bring Mira her father's lantern, still burning.", None, {}),
    ('every_errand', 'errand', 'deathbound:miras_ribbon', 'challenge', "Lantern's End Remembers You", 'Finish every task in the Journal.',
     {q: IMPOSSIBLE for q in ('mira', 'oar', 'lamps', 'ball', 'name')}, {}),
    ('forgotten_things', 'the_other_side', 'deathbound:kings_ring', 'challenge', 'Keeper of Forgotten Things', 'Bring the Collector all eight artifacts.', None, {}),
    ('warden_falls', 'the_other_side', 'deathbound:warden_seal', 'goal', 'The Warden Falls', "Defeat Death's Guard at the Great Door.", None, {}),
    ('three_locks', 'warden_falls', 'deathbound:sigil_kings', 'goal', 'Three Locks', 'Fill the three seals and open the Citadel.', None, {}),
    ('death_of_death', 'three_locks', 'deathbound:heart_of_death', 'challenge', 'Death of Death', 'Defeat the Death King.', None, {}),
    ('ending_take', 'death_of_death', 'deathbound:reaper_scythe', 'goal', 'Long Live Death', 'Take the throne for yourself.', None, {}),
    ('ending_king', 'death_of_death', 'deathbound:kings_quill', 'goal', 'The Rightful King', 'Give the throne back to the King.', None, {}),
    ('ending_break', 'death_of_death', 'deathbound:cracked_soulstone_bricks', 'goal', 'No More Kings', 'Break the throne.', None, {}),
    ('hunter_hunted', 'the_other_side', 'deathbound:hunters_bow', 'challenge', 'The Hunter Hunted', 'Bring down the Hollow Hunter.',
     {'kill': {'trigger': 'minecraft:player_killed_entity', 'conditions': {'entity': {'type': 'minecraft:entity_properties', 'entity': 'this',
                                                                                     'predicate': {'minecraft:entity_type': 'deathbound:hollow_hunter'}}}}},
     {'hidden': True}),
]

if __name__ == '__main__':
    os.makedirs(D, exist_ok=True)
    lang = json.load(open(LANG))
    for aid, parent, icon, frame, title, desc, criteria, extra in TREE:
        criteria = criteria or {'done': IMPOSSIBLE}
        key = f'advancements.deathbound.{aid}'
        lang[key + '.title'] = title
        lang[key + '.description'] = desc
        display = {'icon': {'id': icon}, 'title': {'translate': key + '.title'}, 'description': {'translate': key + '.description'}, 'frame': frame}
        display.update(extra)
        adv = {}
        if parent:
            adv['parent'] = f'deathbound:{parent}'
        adv['criteria'] = criteria
        adv['display'] = display
        # the root takes either of its criteria; every other advancement needs all of its criteria
        adv['requirements'] = [list(criteria)] if aid == 'root' else [[c] for c in criteria]
        with open(f'{D}/{aid}.json', 'w') as f:
            json.dump(adv, f, indent=2)
            f.write('\n')
    with open(LANG, 'w') as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write('\n')
    print(len(TREE), 'advancements')
