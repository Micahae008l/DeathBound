"""Trailer shot lists for the dev harness. `python tools/trailer/shots.py a` writes run/test-script.txt for session a;
then reset run/saves/dbtest and `gradlew runTestClient`. Clips land in run/trailer/<name>.mp4.

Camera verbs (see Cine.java): cam / pan / orbit / follow, all on wall-clock seconds with ease in/out.
follow T id radius height fromDeg toDeg lookUp  -- deg 0 puts the camera on the entity's +Z side.
"""
import sys
from pathlib import Path

L = []


def say(*lines):
    L.extend(lines)


def take(name, verb, secs, start, full, warm=40, events=(), tail=4, fps=30):
    """Hold the start pose while the shot loads, then record the move. events: (tick after rec, line)."""
    say(f'{verb} 0 {start}', f'wait {warm}', f'rec {name} {fps}', f'{verb} {secs} {full}')
    t = 0
    for at, line in sorted(events, key=lambda e: e[0]):
        if at > t:
            say(f'wait {at - t}')
            t = at
        say(line)
    end = int(secs * 20) + tail
    if end > t:
        say(f'wait {end - t}')
    say('stop')


def follow(name, secs, who, r, h, a, b, up, **kw):
    take(name, 'follow', secs, f'{who} {r} {h} {a} {a} {up}', f'{who} {r} {h} {a} {b} {up}', **kw)


def shoulder(name, secs, frm, to, a, b=None, **kw):
    """Over frm's shoulder toward to; a/b = (side, back, height) at the start and end of the move."""
    b = b or a
    fa, fb = ' '.join(map(str, a)), ' '.join(map(str, b))
    take(name, 'shoulder', secs, f'{frm} {to} {fa}', f'{frm} {to} {fa} {fb}', **kw)


def pan(name, secs, a, b, at, **kw):
    fmt = lambda v: ' '.join(str(x) for x in v)
    take(name, 'pan', secs, f'{fmt(a)} {fmt(a)} {fmt(at)}', f'{fmt(a)} {fmt(b)} {fmt(at)}', **kw)


def orbit(name, secs, c, r, h, a, b, **kw):
    fmt = ' '.join(str(x) for x in c)
    take(name, 'orbit', secs, f'{fmt} {r} {h} {a} {a}', f'{fmt} {r} {h} {a} {b}', **kw)


TOUGH = ['effect give @s resistance infinite 4 true', 'effect give @s saturation infinite 0 true',
         'effect give @s regeneration infinite 3 true', 'effect give @s fire_resistance infinite 0 true']
U = 'execute in deathbound:underworld run '


def forged():
    trim = '[trim={material:"deathbound:soul",pattern:"deathbound:soulforged"}]'
    say('clear @s',
        f'item replace entity @s armor.head with minecraft:netherite_helmet{trim}',
        f'item replace entity @s armor.chest with minecraft:netherite_chestplate{trim}',
        f'item replace entity @s armor.legs with minecraft:netherite_leggings{trim}',
        f'item replace entity @s armor.feet with minecraft:netherite_boots{trim}',
        'item replace entity @s weapon.mainhand with deathbound:reaper_scythe',
        'item replace entity @s weapon.offhand with deathbound:deathbound_relic')


def session_a():
    """The living world, the rite, waking below, the ones who talk."""
    say('hud off', 'fov 60', 'gui 3', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s weapon.offhand with deathbound:deathbound_relic',
        'execute in minecraft:overworld run spreadplayers 0 0 0 1 false @s',
        'time set 18000', 'weather thunder', 'view back', 'look 0 -6', 'wait 200')
    follow('a1_rite', 7.5, 'minecraft:player', 3.4, 1.3, -40, 20, 1.1, events=[(40, 'deathbound descend')])
    say('wait 100', 'look 180 0')
    pan('a2_arrival', 7, (1.3, 101.7, 9.8), (3.0, 104.8, 14.0), (0, 102.6, -5))
    say('gamemode creative', 'view first')
    pan('a3_ferryman', 5, (1.0, 103.3, -0.6), (0.45, 103.45, -2.3), (0, 103.6, -5))
    pan('a4_dialog', 9, (1.9, 103.3, -1.0), (1.6, 103.35, -1.6), (0.2, 103.6, -5),
        warm=20, events=[(2, 'dialog show @s deathbound:ferryman')])
    say('close', 'tp @s -8 102 5', 'wait 60')
    follow('a5_soul', 6, 'deathbound:lost_soul', 3.2, 1.2, 30, 80, 0.9)
    say(U + 'tp @s 80 102 -38', 'wait 120')
    follow('a6_gravedigger', 6, 'deathbound:gravedigger', 3.4, 1.5, -70, -105, 1.4)
    take('a7_kitchen', 'cam', 5, '72 101.25 -52 108 14 72 101.25 -52 108 14', '72 101.25 -52 108 14 72.3 101.3 -52.5 128 17', warm=60)
    say(U + 'tp @s -54 89 -168', 'wait 120')
    follow('a8_prophet', 6, 'deathbound:prophet', 2.6, 0.9, -25, 20, 1.3)
    say('quit')


def session_r():
    """The opening: a lone figure in a storm-dark clearing, lightning behind them, the rite."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s weapon.offhand with deathbound:deathbound_relic',
        'execute in minecraft:overworld run spreadplayers -232 -290 0 1 false @s', 'wait 40',
        'fill ~-14 ~ ~-14 ~14 ~22 ~14 air', 'kill @e[type=item]',
        'time set 18000', 'weather thunder', 'view back', 'look 0 -4', 'wait 200')
    follow('a1_storm', 5, 'minecraft:player', 3.0, 0.6, -35, -15, 1.5, events=[(30, 'summon lightning_bolt ~-4 ~ ~-14')])
    follow('a1_rite', 7.5, 'minecraft:player', 4.3, 1.5, -45, 15, 1.0,
           events=[(34, 'summon lightning_bolt ~-7 ~ ~-11'), (40, 'deathbound descend')])
    say('quit')


def session_b2():
    """Re-shoots: closer graves, wisps around a fixed point, the Spire from its foot, the gate, the islands."""
    say('hud off', 'fov 60', 'gamemode creative', 'view first',
        'locate biome minecraft:stony_peaks', 'locate biome minecraft:meadow', 'locate biome minecraft:windswept_hills',
        U + 'tp @s -62 104 -140', 'wait 160',
        'summon deathbound:gravebound -64 104 -146 {variant:0}', 'summon deathbound:gravebound -61 104 -145 {variant:2}',
        'summon deathbound:gravebound -58 104 -147 {variant:3}', 'summon deathbound:gravebound -62 104 -149 {variant:1}',
        'tp @s -61 103 -139', 'wait 60')
    follow('b3_rise', 6, 'deathbound:gravebound', 3.8, 0.7, -20, 15, 1.1, events=[(6, 'deathbound rise')])
    say('kill @e[type=deathbound:gravebound]', 'gamemode survival', *TOUGH,
        'summon deathbound:soul_wisp -60 106 -142 {variant:0}', 'summon deathbound:soul_wisp -57 107 -144 {variant:1}',
        'summon deathbound:soul_wisp -63 105.5 -145 {variant:2}', 'tp @s -60 105 -135', 'wait 40')
    orbit('b4_wisps', 6, (-60, 106, -143), 7.5, 0.5, -25, 20)
    say('kill @e[type=deathbound:soul_wisp]', 'gamemode creative')
    take('b2_spire', 'cam', 6, '89 102 -141 212 8 89 102 -141 212 8', '89 102 -141 212 8 89.5 101.6 -140.5 212 -52', warm=140)
    say(U + 'tp @s 0 103 -215', 'wait 220')
    pan('b5_gate', 8, (0.5, 102.4, -221), (0.3, 103.6, -235), (0, 107.5, -253))
    pan('b6_islands', 9, (30, 112, -60), (18, 109, -78), (-20, 100, -170), warm=160)
    say('quit')


def session_b():
    """The places and the things that live there."""
    say('hud off', 'fov 60', 'gamemode creative', 'view first', U + 'tp @s 0 104 -160', 'wait 320')
    follow('b1_line', 7, 'deathbound:lost_soul', 4.0, 1.4, -25, 20, 1.0)
    orbit('b2_spire', 8, (96, 118, -152), 26, -6, 200, 240, warm=120)
    say(U + 'tp @s -62 104 -140', 'wait 120',
        'summon deathbound:gravebound -66 104 -146 {variant:0}', 'summon deathbound:gravebound -62 104 -145 {variant:2}',
        'summon deathbound:gravebound -58 104 -146 {variant:3}', 'summon deathbound:gravebound -63 104 -149 {variant:1}',
        'tp @s -62 103 -138', 'wait 60')
    follow('b3_rise', 6, 'deathbound:gravebound', 5.5, 0.4, -12, 14, 1.0, events=[(6, 'deathbound rise')])
    say('summon deathbound:soul_wisp -60 106 -139 {variant:0}', 'summon deathbound:soul_wisp -56 107 -141 {variant:1}',
        'summon deathbound:soul_wisp -64 105.5 -142 {variant:2}', 'tp @s -60 106 -134', 'wait 40')
    follow('b4_wisps', 6, 'deathbound:soul_wisp', 3.6, 0.5, -30, 25, 0.4)
    say(U + 'tp @s 0 103 -215', 'wait 220')
    pan('b5_gate', 8, (0.5, 102.4, -221), (0.3, 103.6, -235), (0, 107.5, -253))
    say(U + 'tp @s 30 104 -100', 'wait 80')
    pan('b6_islands', 9, (30, 112, -60), (18, 109, -78), (-20, 100, -170), warm=120)
    say('quit')


def session_c():
    """Soulforged armor, the scythe, the Warden."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH)
    forged()
    say(U + 'tp @s 6 106 -88', 'view back', 'look 200 4', 'wait 200')
    follow('c1_armor', 6, 'minecraft:player', 2.8, 1.35, 160, 235, 1.2)
    say('summon deathbound:gravebound ~3 ~ ~-5 {variant:2}', 'summon deathbound:gravebound ~-2 ~ ~-6 {variant:0}',
        'summon deathbound:gravebound ~0 ~ ~-8 {variant:3}', 'aim deathbound:gravebound', 'wait 30')
    clicks = [(t, 'click') for t in range(20, 160, 13)]
    follow('c2_scythe', 8, 'minecraft:player', 4.4, 1.7, 120, 50, 1.0, events=clicks)
    say('kill @e[type=deathbound:gravebound]', 'aim off',
        U + 'tp @s 0 101 -233 180 0', 'wait 240', 'effect clear @s darkness', 'aim deathbound:deaths_guard', 'wait 20')
    follow('c3_judge', 6, 'deathbound:deaths_guard', 9, 3.2, -55, -25, 2.4, events=[(4, 'deathbound action 6')])
    say('effect clear @s darkness')
    follow('c4_slam', 5, 'deathbound:deaths_guard', 8, 2.0, 40, 70, 2.2, events=[(4, 'deathbound action 2')])
    follow('c5_sweep', 5, 'deathbound:deaths_guard', 8.5, 2.6, -80, -50, 2.2, events=[(4, 'deathbound action 1')])
    follow('c6_condemn', 6, 'deathbound:deaths_guard', 10, 4.0, 150, 190, 2.4, events=[(4, 'deathbound action 5')])
    follow('c7_fight', 8, 'deathbound:deaths_guard', 9, 2.6, 20, -30, 2.2,
           events=[(t, 'click') for t in range(10, 160, 15)])
    say('quit')


def session_w():
    """The Warden again, now that his blade leads with its edge. Side-on stills of a slam first, to check it."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH, 'nodark on')
    forged()
    say(U + 'tp @s 0 101 -233 180 0', 'view back', 'wait 240', 'aim deathbound:deaths_guard', 'wait 20',
        'follow 0 deathbound:deaths_guard 7 1.0 90 90 2.0', 'wait 20', 'deathbound action 2',
        'wait 12', 'shot w_slam1', 'wait 6', 'shot w_slam2', 'wait 5', 'shot w_slam3', 'wait 5', 'shot w_slam4', 'wait 40',
        'deathbound action 1', 'wait 8', 'shot w_sweep1', 'wait 5', 'shot w_sweep2', 'wait 5', 'shot w_sweep3', 'wait 40')
    follow('c3_judge', 6, 'deathbound:deaths_guard', 7.5, 2.4, -55, -25, 2.4, events=[(4, 'deathbound action 6')])
    follow('c4_slam', 5, 'deathbound:deaths_guard', 7, 1.6, 50, 80, 2.2, events=[(4, 'deathbound action 2')])
    follow('c5_sweep', 5, 'deathbound:deaths_guard', 7, 2.2, -80, -50, 2.2, events=[(4, 'deathbound action 1')])
    follow('c6_condemn', 6, 'deathbound:deaths_guard', 8.5, 3.2, 150, 190, 2.4, events=[(4, 'deathbound action 5')])
    follow('c7_fight', 8, 'deathbound:deaths_guard', 8, 2.2, 20, -30, 2.2, events=[(t, 'click') for t in range(10, 160, 15)])
    say('quit')


def clicks(start, end, every):
    return [(t, 'click') for t in range(start, end, every)]


def session_f():
    """The fights, re-staged: the player dodges telegraphs and answers with combos; the camera rides their shoulder."""
    G, D = 'deathbound:deaths_guard', 'deathbound:death'
    DEATH = '@e[type=deathbound:death,limit=1]'
    say('hud off', 'fov 64', 'gamemode survival', *TOUGH, 'nodark on', 'filmlight on', 'handheld 1.0')
    forged()
    # the Warden
    say(U + 'tp @s 0 101 -240 180 0', 'view back', 'wait 240', 'aim ' + G, 'wait 20')
    shoulder('f1_dodge', 5.5, 'player', G, (1.3, 3.2, 0.6), (1.0, 2.4, 0.4), fps=60,
             events=[(4, 'deathbound action 2'), (22, 'hold right 9'), (24, 'hold jump 3'),
                     (48, 'hold forward 16'), (48, 'hold sprint 16')] + clicks(62, 100, 12))
    follow('f2_judge', 5, G, 5, 0.2, 25, 55, 2.6, events=[(4, 'deathbound action 6')])
    shoulder('f3_combo', 5, 'player', G, (0.9, 2.0, 0.5), (1.4, 2.6, 0.8), fps=60,
             events=[(0, 'hold forward 12')] + clicks(12, 50, 11) + [(58, 'deathbound action 1'), (68, 'hold back 10'), (69, 'hold jump 3')])
    # Death
    say('aim off', U + 'tp @s 0 101 -335 180 0', 'wait 200', 'kill @e[type=deathbound:soul_anchor]', 'wait 120', 'aim ' + D)
    say(f'tp {DEATH} 0 101 -345', 'tp @s 0 101 -335 180 0', 'wait 6')
    shoulder('f5_barrage', 5, 'player', D, (1.4, 3.0, 0.8), (1.2, 2.6, 0.6),
             events=[(4, 'deathbound action 20'), (30, 'hold left 22')])
    say(f'tp {DEATH} 0 101 -344', 'tp @s 0 101 -335 180 0', 'wait 4')
    pan('f6_blink', 4.5, (1.0, 102.8, -338.6), (0.7, 102.7, -337.8), (0, 102.4, -334.5), warm=6,
        events=[(6, 'deathbound action 21')] + clicks(40, 80, 12))
    follow('f7_nova', 5, D, 7, 0.3, -20, 10, 2.2, fps=60, events=[(4, 'deathbound action 22'), (26, 'hold jump 3')])
    follow('f8_transform', 7, D, 6.5, 0.6, 20, -25, 2.4, events=[(4, 'deathbound action 29')])
    say('deathbound phase 3', 'wait 20', f'tp {DEATH} 0 101 -343', 'tp @s 0 101 -335 180 0', 'wait 4')
    shoulder('f9_roar', 3.5, 'player', D, (0.7, 1.2, -0.3), (0.5, 0.9, -0.4), fps=60, warm=10, events=[(4, 'deathbound action 34')])
    say(f'tp {DEATH} 0 101 -349', 'tp @s 0 101 -335 180 0', 'wait 4')
    shoulder('f10_charge', 4.5, 'player', D, (1.6, 3.4, 0.7), fps=60, warm=10,
             events=[(4, 'deathbound action 32'), (22, 'hold right 10'), (23, 'hold jump 3')])
    say(f'tp {DEATH} 0 101 -347', 'tp @s 0 101 -335 180 0', 'wait 4')
    shoulder('f11_leap', 4, 'player', D, (2.5, 6.5, 2.5), (2.2, 5.5, 2.0), warm=10,
             events=[(4, 'deathbound action 31'), (20, 'hold back 8'), (21, 'hold jump 3')])
    say(f'tp {DEATH} 0 101 -339', 'tp @s 0 101 -335 180 0', 'wait 4')
    follow('f12_finisher', 6, 'minecraft:player', 3.4, 1.2, 110, 160, 1.1, fps=60, warm=10,
           events=clicks(8, 48, 10) + [(50, 'hold sneak 16'), (52, 'use 8')] + clicks(72, 110, 11))
    follow('f13_claw', 4, D, 5, 0.0, -30, 0, 3.2, warm=10, events=[(4, 'deathbound action 30')])
    say('quit')


def session_g():
    """Death again, kept close: the boss within a few blocks for every shot, a side-on blink, nothing in the off hand."""
    D = 'deathbound:death'
    DEATH = '@e[type=deathbound:death,limit=1]'
    say('hud off', 'fov 64', 'gamemode survival', *TOUGH, 'nodark on', 'filmlight on', 'handheld 1.0')
    forged()
    say('item replace entity @s weapon.offhand with air',  # the relic would open its menu on sneak + use
        U + 'tp @s 0 101 -335 180 0', 'view back', 'wait 220', 'kill @e[type=deathbound:soul_anchor]', 'wait 120', 'aim ' + D)

    def place(z):
        say(f'tp {DEATH} 0 101 {z}', 'tp @s 0 101 -335 180 0', 'wait 4')

    place(-340)
    pan('g1_blink', 4.5, (5.5, 102.8, -336.5), (4.8, 102.7, -336.0), (0, 102.0, -336.5), warm=6,
        events=[(6, 'deathbound action 21')] + clicks(36, 80, 12))
    place(-341)
    shoulder('g2_reaper', 5, 'player', D, (1.1, 2.3, 0.5), (0.8, 1.8, 0.4), events=[(0, 'hold forward 8')] + clicks(10, 90, 12))
    place(-340)
    follow('g3_nova', 4, D, 4.5, 0.2, -30, -5, 2.0, events=[(4, 'deathbound action 22'), (26, 'hold jump 3')])
    place(-340)
    follow('g4_transform', 7, D, 4.5, 0.4, 20, -20, 2.2, events=[(4, 'deathbound action 29')])
    say('deathbound phase 3', 'wait 20')
    place(-340)
    shoulder('g5_roar', 3.5, 'player', D, (1.1, 2.4, 0.3), (0.9, 2.0, 0.2), warm=10, events=[(4, 'deathbound action 34')])
    place(-338.5)
    shoulder('g6_claw', 4, 'player', D, (0.9, 2.2, 0.6), warm=10,
             events=[(4, 'deathbound action 30'), (20, 'hold left 8')] + clicks(44, 80, 11))
    place(-349)
    shoulder('g7_charge', 4, 'player', D, (2.2, 3.0, 0.2), warm=10,
             events=[(4, 'deathbound action 32'), (24, 'hold right 10'), (25, 'hold jump 3')])
    place(-341)
    shoulder('g8_rip', 4, 'player', D, (1.6, 3.2, 1.0), warm=10, events=[(4, 'deathbound action 33'), (16, 'hold left 10')])
    place(-339)
    follow('g9_beastfight', 6, 'minecraft:player', 3.4, 1.2, 110, 160, 1.1, warm=10, events=clicks(8, 110, 10))
    say('quit')


def session_s():
    """Scouting stills of the new landmarks (not trailer footage)."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true')
    views = [('l1_landing', (6, 104, -2), (13, 98, -11), 260), ('l2_ferry', (20, 101, -6), (12, 98.5, -11), 60),
             ('l3_crossing', (7, 103, -71), (0, 97, -80), 200), ('l4_forge', (80, 103, -29), (74, 100, -36), 200),
             ('l5_apothecary', (69, 101.6, -38.3), (65, 100.6, -35), 60), ('l6_tombs_a', (-70, 89.8, -168.5), (-70, 88.4, -173), 200),
             ('l7_tombs_b', (-54, 89.8, -168.5), (-54, 88.4, -173), 60), ('l8_vault', (100.5, 91.8, -147.5), (93, 90, -153), 200),
             ('l9_vault_entry', (96, 102.6, -149), (98, 100, -150), 60), ('l10_lake', (44, 101, -322), (0, 106, -340), 240),
             ('l11_lake_high', (60, 125, -290), (0, 100, -340), 60), ('l12_bridge', (-146, 101, -94), (-172, 94, -112), 240),
             ('l13_hollow', (-188, 106, -108), (-206, 94, -126), 200), ('l14_hollow_in', (-200, 95, -122), (-197, 97, -133), 60)]
    for name, at, look, warm in views:
        fmt = lambda v: ' '.join(map(str, v))
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')
    say('dialog show @s deathbound:throne', 'wait 120', 'shot l15_throne_dialog', 'close',
        'dialog show @s deathbound:prophet.slain', 'wait 160', 'shot l16_king_dialog', 'close', 'quit')


def session_t():
    """Test pass for the new content: the landing and forge, the Collector, the Hollow Hunter, an ending."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 10 108 0', 'wait 200')
    still('t1_landing_top', (13, 112, -1), (13, 98, -11))
    still('t2_ferry', (6, 101.6, -5), (14, 98.5, -11))
    say(U + 'tp @s 79 102 -30', 'wait 160')
    still('t3_forge', (77.5, 102.4, -30.5), (73.5, 100.5, -36))
    # the Collector
    say(U + 'tp @s 98 90 -148', 'wait 160')
    still('t4_vault', (100.5, 91.8, -147.5), (95, 90.5, -150), 20)
    say('dialog show @s deathbound:collector', 'wait 120', 'shot t5_collector_dialog', 'close',
        'give @s deathbound:ferry_coin', 'give @s deathbound:kings_ring', 'hud on', 'deathbound collect', 'wait 30', 'shot t6_collect_chat', 'hud off')
    # the Hollow Hunter
    say('gamemode survival', *TOUGH, 'clear @s', 'item replace entity @s weapon.mainhand with minecraft:bow', 'give @s minecraft:arrow 64',
        U + 'tp @s -200 100 -128', 'wait 120', 'camoff')
    say('view back', 'aim deathbound:hollow_hunter', 'wait 40')
    say('shoulder 0 player deathbound:hollow_hunter 1.4 3.4 0.8', 'wait 10', 'shot t7_hunter_found')
    for i, (act, waits) in enumerate(((1, (14, 10)), (2, (18, 18)), (3, (10, 8)), (4, (6, 8)), (5, (11, 14)))):
        say('deathbound action 0', 'wait 10', f'deathbound action {act}', f'wait {waits[0]}', f'shot t8_hunter_{act}a', f'wait {waits[1]}', f'shot t8_hunter_{act}b', 'wait 30')
    say('hud on', 'kill @e[type=deathbound:hollow_hunter]', 'wait 50', 'shot t9_hunter_down', 'hud off', 'camoff', 'aim off')
    # an ending: break the throne
    say('gamemode creative', 'view first', U + 'tp @s 0 101 -345 180 0', 'wait 160', 'deathbound story 2 0', 'wait 20', 'hud on',
        'deathbound ending break', 'wait 50', 'shot t10_ending_cinematic', 'hud off', 'wait 100')
    still('t11_throne_broken', (0, 106, -352), (0, 104, -364), 20)
    say('camoff', U + 'tp @s 79 101 -40', 'wait 120', 'look 90 10', 'use 1', 'wait 120', 'shot t12_aldous_after', 'close', 'quit')


def session_d():
    """Death: the throne, the Reaper, the Beast."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH)
    forged()
    say(U + 'tp @s 0 101 -331 180 0', 'view back', 'wait 260', 'effect clear @s darkness')
    pan('d1_throne', 8, (1.3, 103.0, -327.0), (0.9, 103.6, -338.0), (0, 106.5, -364), warm=30)
    say('aim deathbound:death')
    follow('d2_rise', 6, 'deathbound:death', 8, 1.5, -15, 15, 2.6, events=[(4, 'kill @e[type=deathbound:soul_anchor]')], tail=40)
    say('wait 40')
    follow('d3_barrage', 5, 'deathbound:death', 9, 2.5, 30, 60, 2.4, events=[(4, 'deathbound action 20')])
    follow('d4_nova', 5, 'deathbound:death', 10, 4.0, -40, -10, 2.4, events=[(4, 'deathbound action 22')])
    follow('d5_blink', 5, 'minecraft:player', 6, 2.2, 150, 120, 1.2, events=[(4, 'deathbound action 21')])
    follow('d6_reaper', 8, 'deathbound:death', 9, 2.2, 80, 20, 2.4, events=[(t, 'click') for t in range(10, 160, 16)])
    follow('d7_transform', 8, 'deathbound:death', 11, 2.0, 10, -25, 3.0, events=[(4, 'deathbound action 29')])
    say('deathbound phase 3', 'wait 20')
    follow('d8_roar', 4, 'deathbound:death', 8, 1.2, -20, 5, 3.0, events=[(4, 'deathbound action 34')])
    follow('d9_charge', 5, 'deathbound:death', 12, 3.5, 60, 30, 2.6, events=[(4, 'deathbound action 32')])
    follow('d10_leap', 5, 'minecraft:player', 9, 3.0, -120, -90, 1.5, events=[(4, 'deathbound action 31')])
    follow('d11_beast', 8, 'deathbound:death', 10, 2.5, 140, 200, 2.6, events=[(t, 'click') for t in range(10, 160, 14)])
    say('quit')


def session_d2():
    """Re-shoots of Death with its darkness stripped and the camera kept out of walls."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH, 'nodark on')
    forged()
    say(U + 'tp @s 0 101 -340 180 0', 'view back', 'wait 260')
    pan('d1_throne', 7, (2.5, 103.2, -349.0), (1.2, 104.0, -355.5), (0, 106.2, -364), warm=30)
    say('aim deathbound:death', 'kill @e[type=deathbound:soul_anchor]', 'wait 120')
    follow('d6_reaper', 7, 'deathbound:death', 8, 2.0, 70, 20, 2.4, events=[(t, 'click') for t in range(10, 140, 16)])
    follow('d7_transform', 8, 'deathbound:death', 11, 2.0, 10, -25, 3.0, events=[(4, 'deathbound action 29')])
    say('deathbound phase 3', 'wait 20')
    follow('d8_roar', 4, 'deathbound:death', 8, 1.2, -20, 5, 3.0, events=[(4, 'deathbound action 34')])
    follow('d9_charge', 5, 'deathbound:death', 12, 3.5, 60, 30, 2.6, events=[(4, 'deathbound action 32')])
    follow('d10_leap', 5, 'minecraft:player', 9, 3.0, -120, -90, 1.5, events=[(4, 'deathbound action 31')])
    follow('d11_beast', 8, 'deathbound:death', 10, 2.5, 140, 200, 2.6, events=[(t, 'click') for t in range(10, 160, 14)])
    follow('d12_rip', 5, 'deathbound:death', 9, 2.0, -60, -30, 2.6, events=[(4, 'deathbound action 33')])
    say('quit')


def session_v():
    """Scouting stills for the relics pass: the new skulls and arrows, the Hollow, the Collector's room, the scales over the Door."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 10 108 0', 'wait 200')
    # a showroom in the void: every new block in two rows
    say(U + 'fill 38 130 38 51 130 45 deathbound:soulstone_tiles', U + 'fill 39 131 43 49 133 43 deathbound:curio_shelf')
    row1 = ['skull', 'pierced_skull', 'stuck_arrows', 'remains', 'slumped_remains', 'bone_pile', 'skull_spike', 'gibbet_cage']
    row2 = ['jar_crown', 'jar_keys', 'jar_heart', 'jar_moth', 'bottled_ship', 'soul_jar', 'eye_jar', 'glowcap']
    for i, b in enumerate(row1):
        say(U + f'setblock {40 + i} 131 40 deathbound:{b}[facing=north]')
    for i, b in enumerate(row2):
        say(U + f'setblock {40 + i} 131 42 deathbound:{b}[facing=north]')
    say(U + 'setblock 41 132 42 deathbound:wall_glowcap[facing=north]', U + 'setblock 44 133 42 deathbound:wall_glowcap[facing=north]',
        U + 'fill 50 131 42 50 134 42 deathbound:ossified_log', U + 'setblock 49 131 42 deathbound:chiseled_soulstone')
    still('v1_showroom', (44, 133.4, 35.5), (44, 131.2, 41.5), 60)
    still('v2_skulls_close', (42, 132.2, 37.6), (42, 131.3, 40.5), 20)
    still('v3_jars_close', (43.5, 132.6, 39.4), (43.5, 131.4, 42.5), 20)
    still('v4_skulls_side', (37.5, 132.4, 40.5), (42, 131.3, 40.5), 20)
    say('effect clear @s night_vision', 'wait 20')
    still('v5_showroom_dark', (44, 133.4, 35.5), (44, 131.2, 41.5), 30)
    say('effect give @s night_vision infinite 0 true')
    # places
    still('v6_ferry', (20, 101, -6), (12, 98.5, -11), 160)
    still('v7_apothecary', (69, 101.6, -38.3), (65, 100.6, -35), 160)
    still('v8_forge', (80, 103, -29), (74, 100, -36), 40)
    still('v9_collector_room', (98.5, 91.6, -149.5), (94, 90.6, -153.5), 160)
    still('v10_collector_room_b', (94, 91.6, -153), (98.5, 90.8, -149), 20)
    say('effect clear @s night_vision', 'wait 20')
    still('v11_collector_room_dark', (98.5, 91.6, -149.5), (94, 90.6, -153.5), 20)
    say('effect give @s night_vision infinite 0 true')
    still('v12_watch_trail', (-138, 99.5, -97), (-146, 97, -101), 160)
    still('v13_bridge', (-146, 101, -94), (-172, 94, -112), 40)
    still('v14_hollow', (-188, 106, -108), (-206, 94, -126), 160)
    still('v15_trophy', (-197, 96.5, -120.5), (-199, 95, -123), 30)
    still('v16_blind', (-200, 95, -122), (-197, 99, -133), 20)
    still('v17_door_scales', (0, 124, -234), (0, 126, -251), 160)
    still('v18_lake_bones', (30, 101, -330), (40, 96, -338), 160)
    # the Hunter shooting
    say('gamemode survival', *TOUGH, 'clear @s', U + 'tp @s -200 100 -128', 'wait 120', 'camoff', 'view back', 'aim deathbound:hollow_hunter', 'wait 40',
        'shoulder 0 player deathbound:hollow_hunter 1.4 3.4 0.8', 'wait 10', 'shot v19_hunter')
    for act, w in ((1, 18), (2, 20)):
        say('deathbound action 0', 'wait 10', f'deathbound action {act}', f'wait {w}', f'shot v20_hunter_{act}a', 'wait 6', f'shot v20_hunter_{act}b', 'wait 40')
    say('camoff', 'aim off', 'view first', 'look 0 60', 'wait 10', 'shot v21_arrows_ground', 'quit')


def session_p():
    """Test pass: the darker air, the Shade, the hooded Hunter drawing from his back, the three puzzles and the sealed gate."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    def cam(name, at, look, warm=5):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 10 108 0', 'wait 200')
    still('p1_landing_dark', (13, 112, -1), (13, 98, -11), 60)
    still('p2_hub_dark', (0, 104, -66), (0, 99, -95), 160)
    # the Shade, behind the player; the free camera looks where the player doesn't
    say(U + 'tp @s 4 99 -84 0 0', 'wait 60', 'look 0 0', 'fov 90', 'deathbound shade', 'wait 10')
    cam('p3_shade_dark', (4.5, 101.2, -83.5), (4.5, 100.5, -110))
    say('effect give @s night_vision infinite 0 true', 'wait 5')
    cam('p4_shade_nv', (4.5, 101.2, -83.5), (4.5, 100.5, -110))
    say('camoff', 'fov 70', 'look 180 0', 'wait 30')
    say('hud on', 'wait 5', 'shot p5_shade_gone', 'hud off')
    # the Hunter: hood, arrows in his back, the pull and draw
    say('gamemode survival', *TOUGH, 'clear @s', U + 'tp @s -200 100 -128', 'wait 120', 'camoff', 'view back', 'aim deathbound:hollow_hunter', 'wait 40',
        'shoulder 0 player deathbound:hollow_hunter 1.4 3.4 0.8', 'wait 10', 'shot p6_hunter_front')
    for a in (90, 180, 270):
        say(f'follow 0 deathbound:hollow_hunter 4.2 1.4 {a} {a} 1.9', 'wait 6', f'shot p7_hunter_{a}')
    for cam_cmd, tag in (('shoulder 0 player deathbound:hollow_hunter 1.4 3.4 0.8', 'front'), ('follow 0 deathbound:hollow_hunter 4.0 1.3 200 200 1.9', 'back')):
        say('deathbound action 0', 'wait 10', cam_cmd, 'deathbound action 1', 'wait 6', f'shot p8_{tag}_reach', 'wait 4', f'shot p8_{tag}_pull',
            'wait 5', f'shot p8_{tag}_nock', 'wait 6', f'shot p8_{tag}_draw', 'wait 40')
    say('deathbound action 0', 'wait 10', 'shoulder 0 player deathbound:hollow_hunter 1.4 3.4 0.8', 'deathbound action 2', 'wait 12', 'shot p9_volley', 'wait 40')
    say('kill @e[type=deathbound:hollow_hunter]', 'camoff', 'aim off', 'view first', 'gamemode creative', 'clear @s', 'wait 40')
    # the Tomb of Kings: wrong first, then right
    say(U + 'tp @s -62 88 -167', 'wait 120')
    still('p10_tomb_room', (-70, 90.4, -169.5), (-70, 88.3, -175), 20)
    say('deathbound click -56 88 -175', 'wait 5', 'deathbound click -72 88 -175', 'wait 5', 'deathbound click -52 88 -175', 'wait 10', 'hud on', 'wait 5',
        'shot p11_lamps_wrong', 'hud off')
    say('deathbound click -72 88 -175', 'wait 5', 'deathbound click -68 88 -175', 'wait 5')
    cam('p12_lamps_two', (-70, 90.4, -169.5), (-70, 88.3, -175))
    say('deathbound click -56 88 -175', 'wait 25')
    still('p13_kings_sigil', (-54, 90.4, -169.5), (-55.5, 88.8, -172.5), 5)
    say('hud on', 'wait 3', 'shot p14_kings_chat', 'hud off')
    # the Spire's watchers
    say(U + 'tp @s 96 141 -151', 'wait 120')
    still('p15_watchers_before', (96.5, 142.5, -150.5), (96.5, 142.3, -155), 10)
    for pos in ('96 142 -155', '99 142 -152', '96 142 -149', '93 142 -152'):
        say(f'deathbound click {pos}', 'wait 4', f'deathbound click {pos}', 'wait 4')
    say('wait 20')
    still('p16_watchers_sigil', (98.4, 143.2, -150.0), (96.5, 141.5, -151.5), 5)
    say('hud on', 'wait 3', 'shot p17_watchers_chat', 'hud off')
    # Lantern's End: two tolls (wrong), then four
    say(U + 'tp @s 103 112 -24', 'wait 120')
    still('p18_bell_tower', (106, 109, -20), (100, 106, -30), 10)
    say('deathbound ringbell', 'wait 10', 'deathbound ringbell', 'wait 70', 'hud on', 'wait 3', 'shot p19_bell_wrong', 'hud off')
    for _ in range(4):
        say('deathbound ringbell', 'wait 10')
    say('wait 60', 'follow 0 minecraft:item 2.6 0.4 30 30 0.2', 'wait 5', 'shot p20_bell_sigil', 'camoff', 'hud on', 'wait 3', 'shot p21_bell_chat', 'hud off')
    # the sealed gate
    say(U + 'tp @s 0 101 -298', 'wait 120')
    still('p22_gate_sealed', (0.5, 104, -297), (0.5, 103, -306), 10)
    for item, x in (('sigil_kings', -2), ('sigil_watchers', 0), ('sigil_bell', 2)):
        say(f'item replace entity @s weapon.mainhand with deathbound:{item}', f'deathbound click {x} 102 -306', 'wait 10')
        if x == 0:
            cam('p23_gate_two', (0.5, 104, -297), (0.5, 103, -306))
    say('hud on', 'wait 20', 'shot p24_gate_open', 'hud off', 'wait 40')
    still('p25_gate_through', (0.5, 104, -297), (0.5, 103, -330), 10)
    # the Collector's room with its glowcaps, without night vision
    say('effect clear @s night_vision', U + 'tp @s 96 90 -151', 'wait 120')
    still('p26_collector_dark', (98.5, 91.6, -149.5), (94, 90.6, -153.5), 10)
    still('p27_collector_dark_b', (94, 91.6, -153), (98.5, 90.8, -149), 5)
    say('quit')


def session_q():
    """Follow-up checks: the Hunter on open ground (no AI) drawing from his back, the Shade up close, lamps on the tombs, the iron bell."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    def cam(name, at, look, warm=3):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 10 108 0', 'wait 200')
    say(U + 'fill 36 130 36 53 130 53 deathbound:soulstone_tiles', U + 'tp @s 44.5 131 38.5 0 0', 'wait 40',
        U + 'summon deathbound:hollow_hunter 44.5 131 46.5 {NoAI:1b,Rotation:[180f,0f],PersistenceRequired:1b}', 'wait 30')
    H = (44.5, 132.6, 46.5)
    cam('q1_hunter_front', (44.5, 133.4, 41.5), H)
    cam('q2_hunter_back', (44.5, 133.6, 51.5), (44.5, 132.9, 46.5))
    cam('q3_hunter_side', (49.5, 133.2, 46.5), H)
    cam('q4_hunter_back34', (48.5, 134.0, 50.5), (44.5, 132.8, 46.5))
    for view, at in (('side', (49.0, 133.0, 45.0)), ('front', (45.5, 133.2, 41.5))):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(H)}', 'deathbound action 0', 'wait 5', 'deathbound action 1',
            'wait 6', f'shot q5_{view}_reach', 'wait 3', f'shot q5_{view}_pull', 'wait 4', f'shot q5_{view}_nock', 'wait 7', f'shot q5_{view}_draw', 'wait 30')
    say('deathbound action 0', 'effect clear @s night_vision', 'wait 10')
    cam('q6_hunter_dark', (44.5, 133.4, 41.5), H, 10)
    say('effect give @s night_vision infinite 0 true', 'kill @e[type=deathbound:hollow_hunter]', 'camoff')
    # the Shade, the camera on it
    say(U + 'tp @s 4 99 -84 0 0', 'wait 80', 'look 0 0', 'deathbound shade', 'wait 5', 'follow 0 deathbound:shade 7 1.0 0 0 1.8', 'wait 5', 'shot q7_shade_nv',
        'effect clear @s night_vision', 'wait 5', 'shot q8_shade_dark', 'follow 0 deathbound:shade 3.5 0.8 20 20 2.0', 'wait 3', 'shot q9_shade_close', 'camoff',
        'effect give @s night_vision infinite 0 true')
    # lamps on the tombs
    say(U + 'tp @s -62 88 -167', 'wait 120')
    still('q10_lamps_dark', (-70, 90.6, -169.5), (-70, 88.8, -175), 20)
    say('deathbound click -72 89 -174', 'wait 3', 'deathbound click -68 89 -174', 'wait 10')
    cam('q11_lamps_lit', (-70, 90.6, -169.5), (-70, 88.8, -175))
    say('deathbound click -56 89 -174', 'wait 20')
    still('q12_kings_sigil', (-54, 90.6, -169.5), (-55.5, 88.8, -174), 5)
    # the iron bell
    say(U + 'tp @s 103 112 -24', 'wait 120')
    still('q13_bell', (102.5, 113.5, -26.5), (100.5, 111.5, -29.5), 10)
    for _ in range(4):
        say('deathbound ringbell', 'wait 10')
    say('wait 60', 'follow 0 minecraft:item 2.6 0.4 30 30 0.2', 'wait 5', 'shot q14_bell_sigil', 'camoff', 'quit')


def session_r():
    """Test pass for the playtest fixes: Spire stair, Citadel towers, the Tomb, soul orbs, Beast music, the bow and the Hunter's drops, dialogs."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    def cam(name, at, look, warm=3):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 10 108 0', 'wait 200')
    # the Crossing's title, short now
    say('camoff', U + 'tp @s 0 99 -80', 'wait 30', 'hud on', 'wait 20', 'shot r1_crossing_title', 'hud off')
    # the Spire: one stair all the way up
    say(U + 'tp @s 96 101 -149', 'wait 140')
    still('r2_spire_stair_low', (93.6, 102.6, -149.8), (96.5, 108, -151.5), 10)
    still('r3_spire_stair_mid', (93.8, 122.8, -151.5), (96.5, 128, -151.5), 10)
    still('r4_spire_stair_top', (94.4, 135.6, -151.5), (96.5, 140, -151.5), 10)
    still('r5_spire_top_room', (97.8, 142.4, -152.5), (95.5, 140.6, -151.5), 10)
    # a Citadel tower from the arena side, and its upper floor
    say(U + 'tp @s 23 101 -323', 'wait 140')
    still('r6_tower_door', (23.5, 102.6, -326.5), (23.5, 102, -322), 10)
    still('r7_tower_ground', (23.5, 102.6, -320.0), (23.5, 101.2, -315), 5)
    still('r8_tower_upper', (21.0, 115.6, -317.5), (25.5, 114.5, -317.5), 5)
    # the Tomb of Kings: nothing floating, the lamp hints and counts
    say(U + 'tp @s -62 88 -167', 'wait 120')
    still('r9_tomb_room', (-70, 90.6, -169.5), (-70, 88.8, -175), 10)
    say(U + 'tp @s -71 88 -172', 'wait 30', 'hud on', 'wait 10', 'shot r10_lamp_hint', 'deathbound click -72 89 -174', 'wait 5', 'shot r11_lamp_ok', 'hud off')
    still('r12_tomb_room_b', (-54, 90.6, -169.5), (-54, 88.8, -175), 10)
    # the Death King's orbs and bolts, in the dark
    say('effect clear @s night_vision', U + 'tp @s 0 101 -298', 'wait 80',
        U + 'summon deathbound:death_orb 0.5 103.5 -304.5', U + 'summon deathbound:soul_bolt 2.5 103 -303.5', U + 'summon deathbound:soul_bolt -1.5 103.6 -303.0', 'wait 6')
    cam('r13_orbs_dark', (0.5, 103.4, -299.5), (0.5, 103.3, -304))
    say('effect give @s night_vision infinite 0 true', 'wait 4')
    cam('r14_orbs_nv', (0.5, 103.4, -299.5), (0.5, 103.3, -304))
    say('camoff', 'kill @e[type=deathbound:death_orb]', 'kill @e[type=deathbound:soul_bolt]')
    # the Beast's music: what's playing in each phase
    say('deathbound story 2 0', 'deathbound story 1 0', 'deathbound seals 7', 'gamemode survival', *TOUGH, U + 'tp @s 0 101 -330', 'wait 200', 'music', 'wait 100', 'music')
    say('deathbound phase 3', 'wait 200', 'music', 'wait 100', 'music', 'kill @e[type=deathbound:death]', 'kill @e[type=deathbound:soul_anchor]', 'wait 20')
    # the bow: drawn, loosed, the arrow in flight and what it hits glowing
    say('gamemode creative', 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:hunters_bow', 'give @s minecraft:arrow 32',
        U + 'tp @s 40 131 38 0 0', U + 'fill 34 130 34 54 130 60 deathbound:soulstone_tiles', 'wait 40',
        U + 'summon deathbound:gravebound 40.5 131 50.5 {NoAI:1b,PersistenceRequired:1b}', 'wait 10', 'look 0 0', 'view first', 'use 30', 'wait 18', 'shot r15_bow_drawn', 'wait 14')
    say('view back', 'wait 2', 'shot r16_arrow_flight', 'wait 20', 'shot r17_hit_glow', 'view first')
    # the Hunter's drops, really killed
    say('kill @e[type=deathbound:gravebound]', 'gamemode survival', *TOUGH, 'clear @s', U + 'tp @s -200 95 -128', 'wait 160',
        U + 'damage @e[type=deathbound:hollow_hunter,limit=1,sort=nearest] 9999 minecraft:player_attack by @s', 'wait 80',
        U + 'tp @e[type=minecraft:item,distance=..80] @s', 'wait 30', 'inv', 'wait 10', 'shot r18_hunter_drops', 'close')
    # going back to the top of a conversation: a short line, not the whole greeting again
    say('gamemode creative', 'dialog show @s deathbound:ferryman_who', 'wait 60', 'dialog show @s deathbound:ferryman', 'wait 40', 'shot r19_dialog_back', 'close', 'quit')


def session_u():
    """Test pass: NPCs at their stations (from a distance, so they keep working), a journal opened, the soul orbs in the dark."""
    fmt = lambda v: ' '.join(map(str, v))

    def cam(name, at, look, warm=3):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 0 101 3', 'wait 220')
    cam('u1_ferryman_a', (2.5, 102.8, -5.5), (6.5, 101.6, -9.5), 10)
    say('wait 40')
    cam('u2_ferryman_b', (9.5, 102.6, -6.0), (6.5, 101.6, -9.5))
    say(U + 'tp @s 84 100 -46', 'wait 200')
    cam('u3_aldous_a', (72.5, 101.9, -38.5), (75.5, 101.0, -35.5), 10)
    say('wait 7')
    cam('u4_aldous_b', (72.5, 101.9, -38.5), (75.5, 101.0, -35.5))
    say(U + 'tp @s 96 101 -146', 'wait 200')
    cam('u5_collector', (98.5, 91.6, -149.5), (96.4, 90.8, -151.6), 10)
    say('wait 60')
    cam('u6_collector_b', (98.5, 91.6, -149.5), (96.4, 90.8, -151.6))
    say(U + 'tp @s -62 88 -167', 'wait 200')
    cam('u7_prophet', (-54.5, 90.4, -168.0), (-54.5, 89.4, -172.5), 10)
    say('camoff', 'clear @s', 'loot give @s loot deathbound:journal', 'wait 10', 'use 1', 'wait 20', 'shot u8_book_open', 'close')
    say('effect clear @s night_vision', U + 'tp @s 0 101 -298', 'wait 120',
        U + 'summon deathbound:death_orb 0.5 103.5 -304.5', U + 'summon deathbound:soul_bolt 2.5 103 -303.5', U + 'summon deathbound:soul_bolt -1.5 103.6 -303.0', 'wait 6')
    cam('u9_orbs_dark', (0.5, 103.4, -299.5), (0.5, 103.3, -304))
    say('camoff', 'quit')


def session_bk():
    """Can a book be read? Ours from the loot table, and a plain vanilla one, both held and used."""
    say('hud on', 'gamemode creative', 'view first', U + 'tp @s 0 101 3 0 30', 'wait 200', 'clear @s', 'loot give @s loot deathbound:journal', 'wait 10',
        'data get entity @s Inventory', 'use 8', 'wait 25', 'shot bk1_journal', 'close', 'wait 10', 'clear @s',
        'give @s written_book[written_book_content={title:"Test",author:"me",pages:["hello"]}]', 'wait 10', 'use 8', 'wait 25', 'shot bk2_vanilla', 'close', 'quit')


def session_e():
    """The three endings, each from the throne, with stills through the cutscene."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0 101 -340', 'wait 220', 'deathbound seals 7', 'deathbound story 2 0',
        U + 'tp @s 0.5 101 -354 180 0', 'wait 60', 'gamemode survival', *TOUGH)
    for which, stills in (('take', (60, 150, 250, 330)), ('king', (40, 160, 300, 380, 440)), ('break', (40, 150, 290, 380, 440))):
        say('deathbound story 2 0', f'deathbound ending {which}')
        last = 0
        for t in stills:
            say(f'wait {t - last}', f'shot e_{which}_{t}')
            last = t
        say('wait 120', U + 'tp @s 0.5 101 -354 180 0', 'wait 60')
    say('quit')


def session_ev(only=None, intro=True):
    """The endings video: the Death King falls, the throne is empty, the Heart is brought to it, and each choice plays."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0 101 -340', 'wait 240',
        'deathbound story 1 0', 'deathbound seals 7', 'clear @s', U + 'tp @s 0.5 101 -330 180 -8', 'wait 220')
    # the fall
    if intro:
        say('kill @e[type=deathbound:death]', 'wait 200', 'rec ev_intro 30', 'pan 9 5.5 104.0 -332 2.5 105.6 -354 0.5 105.0 -364', 'wait 270', 'stop', 'camoff')
    for which in [w for w in (only or ('take', 'king', 'break')) if w != 'none']:
        say('deathbound story 2 0', 'kill @e[type=deathbound:death]', 'kill @e[type=deathbound:deaths_guard]', 'kill @e[type=deathbound:prophet]', 'kill @e[type=deathbound:lost_soul]',
            'clear @s', 'item replace entity @s weapon.mainhand with deathbound:heart_of_death', U + 'tp @s 0.5 104 -360.5 180 30', 'wait 200',
            'hud on', 'look 180 32', f'rec ev_{which} 30', 'wait 30', 'use 5', 'wait 60', 'close', 'wait 5', f'deathbound ending {which}', f"wait {465 if which == 'take' else 585}", 'stop', 'wait 20')
    say('quit')


def session_npcv():
    """Character clips for the NPC video (each at their station, from out of earshot so they keep working), their dialog
    screens, and gallery stills of what changed."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    def film(name, secs, a, b, look):
        say(f'rec {name} 30', f'pan {secs} {fmt(a)} {fmt(b)} {fmt(look)}', f'wait {int(secs * 20) + 5}', 'stop', 'camoff')

    def talk(npc):
        say('hud on', f'dialog show @s deathbound:{npc}', 'wait 130', f'shot dlg_{npc}', 'close', 'hud off')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 16 101 2', 'wait 240')
    film('npc_ferryman', 7, (11.5, 102.4, -13.0), (9.8, 102.0, -7.0), (6.5, 102.2, -8.8))
    still('gal_ferryman', (10.4, 102.2, -10.0), (6.5, 102.0, -8.8), 5)
    talk('ferryman')
    say(U + 'tp @s 84 100 -44', 'wait 200')
    film('npc_aldous', 7, (81.5, 102.0, -38.5), (80.5, 101.8, -34.0), (75.5, 101.3, -35.8))
    still('gal_aldous', (80.6, 101.9, -36.5), (75.5, 101.3, -35.8), 5)
    talk('gravedigger')
    still('gal_house', (87.5, 109.5, -53.0), (87.5, 100.0, -59.5), 60)
    say(U + 'tp @s 96 101 -146', 'wait 200')
    film('npc_collector', 6, (98.6, 91.6, -148.8), (97.8, 91.5, -149.6), (96.3, 90.9, -151.2))
    say(U + 'tp @s 96 101 -146', 'wait 10')
    still('gal_collector', (98.4, 91.6, -149.2), (96.3, 90.9, -151.3), 5)
    say(U + 'tp @s 96 101 -146', 'wait 10')
    talk('collector')
    say(U + 'tp @s -62 88 -160', 'wait 200')
    film('npc_prophet', 6, (-56.8, 90.6, -167.4), (-53.2, 90.4, -168.2), (-54.5, 89.6, -172.5))
    say(U + 'tp @s -62 88 -160', 'wait 10')
    talk('prophet')
    say(U + 'tp @s 0 101 -222', 'wait 240')
    say('rec npc_warden 30', 'orbit 7 0.5 102.2 -246.5 7 2.2 -35 40', 'wait 145', 'stop', 'camoff')
    still('gal_warden', (3.5, 103.0, -240.0), (0.5, 102.5, -246.5), 5)
    say(U + 'tp @s -200 100 -126', 'wait 160', U + 'summon deathbound:hollow_hunter -203.5 100 -121.5', 'wait 60',
        'data merge entity @e[type=deathbound:hollow_hunter,limit=1,sort=nearest] {NoAI:1b,Rotation:[0f,0f]}', 'wait 10')
    say('rec npc_hunter 30', 'orbit 7 -203.5 101.6 -121.5 4.8 1.0 -40 45', 'wait 145', 'stop', 'camoff')
    say('pan 0 -203.0 101.8 -117.5 -203.0 101.8 -117.5 -203.5 101.6 -121.5', 'deathbound action 1', 'wait 22', 'shot gal_hunter_draw', 'camoff',
        'kill @e[type=deathbound:hollow_hunter]')
    say('deathbound story 1 0', 'deathbound seals 7', U + 'tp @s 0.5 101 -345', 'wait 260')
    film('npc_deathking', 7, (4.5, 103.0, -350.0), (1.5, 104.6, -355.5), (0.5, 105.6, -364.0))
    still('gal_carved', (9.0, 105.0, -329.0), (0.5, 100.5, -340.0), 5)
    say('kill @e[type=deathbound:death]', 'wait 20')
    # the showroom: every new block, in the dark (it should glow) and lit
    say(U + 'fill 36 130 36 56 130 50 deathbound:soulstone_tiles', U + 'fill 37 131 44 55 133 44 deathbound:dark_soulstone_bricks', U + 'tp @s 46 131 38', 'wait 40')
    row1 = ['skull', 'pierced_skull', 'stuck_arrows', 'remains', 'slumped_remains', 'bone_pile', 'skull_spike', 'grave_lamp[lit=true]', 'watcher_skull',
            'grave_bell', 'fare_bowl', 'glowcap', 'tombstone']
    row2 = ['jar_crown', 'jar_keys', 'jar_heart', 'jar_moth', 'bottled_ship', 'soul_jar', 'eye_jar', 'bone_jar', 'bone_candelabra']
    for i, b in enumerate(row1):
        state = b if '[' in b else b + '[facing=north]'
        if b == 'grave_lamp[lit=true]':
            state = b
        say(U + f'setblock {38 + i} 131 40 deathbound:{state}')
    for i, b in enumerate(row2):
        say(U + f'setblock {40 + i} 131 43 deathbound:{b}[facing=north]')
    say(U + 'setblock 38 131 44 deathbound:lock_kings[facing=north,filled=true]', U + 'setblock 39 131 44 deathbound:lock_watchers[facing=north]',
        U + 'setblock 40 131 44 deathbound:lock_bell[facing=north,filled=true]', U + 'setblock 50 132 43 deathbound:wall_glowcap[facing=north]',
        U + 'fill 51 131 44 51 133 44 deathbound:chiseled_soulstone', U + 'fill 52 131 44 52 133 44 deathbound:ossified_log',
        U + 'fill 53 131 44 54 133 44 deathbound:curio_shelf', U + 'setblock 49 131 44 deathbound:chiseled_soulstone', 'wait 20')
    still('gal_showroom', (46.5, 134.6, 33.0), (46.5, 131.4, 41.5), 30)
    still('gal_showroom_close', (42.0, 132.4, 36.8), (42.0, 131.3, 40.5), 10)
    say('effect clear @s night_vision', 'wait 10')
    still('gal_showroom_dark', (46.5, 134.6, 33.0), (46.5, 131.4, 41.5), 20)
    say('effect give @s night_vision infinite 0 true')
    # the bow, drawn, from in front
    say(U + 'tp @s 46.5 131 37.5 180 0', 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:hunters_bow', 'give @s minecraft:arrow 16',
        'view back', 'wait 20', 'pan 0 46.5 132.7 34.6 46.5 132.7 34.6 46.5 132.3 37.5', 'use 40', 'wait 22', 'shot gal_bow', 'wait 30', 'camoff', 'view first', 'quit')


def session_npcv2():
    """Re-shoots: Aldous from the open side of his forge, the block showroom (loaded first this time), the bow drawn."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 86 100 -44', 'wait 260')
    say('rec npc_aldous 30', 'pan 7 81.0 101.7 -36.4 79.2 101.5 -35.8 75.5 101.3 -35.9', 'wait 145', 'stop', 'camoff')
    still('gal_aldous', (79.6, 101.6, -36.1), (75.5, 101.3, -35.9), 5)
    say(U + 'tp @s 86 100 -44', 'wait 5')
    still('gal_forge', (80.5, 104.5, -40.5), (74.5, 100.5, -36.0), 5)
    say(U + 'tp @s 46 131 38', 'wait 80', U + 'fill 36 130 36 56 130 50 deathbound:soulstone_tiles', U + 'fill 37 131 44 55 133 44 deathbound:dark_soulstone_bricks', 'wait 10')
    row1 = ['skull', 'pierced_skull', 'stuck_arrows', 'remains', 'slumped_remains', 'bone_pile', 'skull_spike', 'grave_lamp[lit=true]', 'watcher_skull',
            'grave_bell', 'fare_bowl', 'glowcap', 'tombstone']
    row2 = ['jar_crown', 'jar_keys', 'jar_heart', 'jar_moth', 'bottled_ship', 'soul_jar', 'eye_jar', 'bone_jar', 'bone_candelabra']
    for i, b in enumerate(row1):
        say(U + f'setblock {38 + i} 131 40 deathbound:' + (b if '[' in b else b + '[facing=north]'))
    for i, b in enumerate(row2):
        say(U + f'setblock {40 + i} 131 43 deathbound:{b}[facing=north]')
    say(U + 'setblock 38 132 44 deathbound:lock_kings[facing=north,filled=true]', U + 'setblock 39 132 44 deathbound:lock_watchers[facing=north]',
        U + 'setblock 40 132 44 deathbound:lock_bell[facing=north,filled=true]', U + 'setblock 50 132 43 deathbound:wall_glowcap[facing=north]',
        U + 'fill 51 131 43 51 133 43 deathbound:chiseled_soulstone', U + 'fill 52 131 43 52 133 43 deathbound:ossified_log',
        U + 'fill 53 131 43 54 132 43 deathbound:curio_shelf', 'wait 20')
    still('gal_showroom', (46.5, 134.6, 33.0), (46.5, 131.4, 41.5), 30)
    still('gal_showroom_close', (42.0, 132.4, 36.8), (42.0, 131.3, 40.5), 10)
    still('gal_showroom_jars', (44.0, 132.6, 39.6), (44.0, 131.5, 43.0), 10)
    still('gal_showroom_right', (52.0, 133.0, 38.0), (51.5, 131.8, 42.5), 10)
    say('effect clear @s night_vision', 'wait 10')
    still('gal_showroom_dark', (46.5, 134.6, 33.0), (46.5, 131.4, 41.5), 20)
    say('effect give @s night_vision infinite 0 true')
    say(U + 'tp @s 46.5 131 37.5 180 0', 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:hunters_bow', 'give @s minecraft:arrow 16',
        'view back', 'wait 20', 'pan 0 47.6 132.6 35.2 47.6 132.6 35.2 46.5 132.4 37.5', 'use 40', 'wait 22', 'shot gal_bow', 'wait 30', 'camoff', 'view first', 'quit')


def session_forge():
    """Aldous's rebuilt forge, and his graves (on the ground now)."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 88 100 -44', 'wait 260')
    still('forge_east', (83.5, 102.4, -35.5), (75.5, 101.0, -35.8), 10)
    still('forge_ne', (82.0, 103.5, -41.0), (75.5, 100.8, -35.5), 5)
    still('forge_inside', (78.5, 101.8, -34.2), (74.0, 101.0, -37.0), 5)
    still('forge_racks', (77.5, 101.6, -34.5), (78.0, 101.2, -37.0), 5)
    still('forge_graves', (75.0, 104.0, -25.0), (74.5, 100.0, -31.0), 5)
    say('quit')


def session_aldous():
    """Aldous at his rebuilt forge (for the characters video)."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 88 100 -44', 'wait 260',
        'rec npc_aldous 30', 'pan 7 84.0 102.6 -37.5 82.0 102.0 -34.8 75.5 101.2 -35.9', 'wait 145', 'stop', 'camoff', 'quit')


def session_e2(which=('take', 'king', 'break')):
    """The reworked endings: the Warden's kneel, the King's price and what it starts in him, the fall and the run home."""
    fmt = lambda v: ' '.join(map(str, v))

    def cam(name, at, look, warm=5):
        say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    def stills(prefix, ts):
        last = 0
        for t in ts:
            say(f'wait {t - last}', f'shot {prefix}_{t}')
            last = t

    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0 101 -340', 'wait 220', 'deathbound story 2 0', 'deathbound seals 7',
        'wait 40', 'kill @e[type=deathbound:death]', U + 'tp @s 0.5 101 -354 180 0', 'wait 60', 'gamemode survival', *TOUGH)
    if 'take' in which:
        say('deathbound story 2 0', 'kill @e[type=deathbound:deaths_guard]', 'deathbound ending take')
        stills('e2_take', (60, 200, 250, 290, 330))
        say('wait 200', 'gamemode spectator', 'effect give @s night_vision infinite 0 true')
        cam('e2_kneel_front', (-2.0, 102.8, -358.5), (0.5, 101.6, -354.0), 20)
        cam('e2_kneel_side', (4.8, 102.0, -354.2), (0.5, 101.5, -354.0))
        cam('e2_kneel_back', (3.2, 103.2, -350.2), (0.5, 101.5, -354.0))
        cam('e2_kneel_low', (-1.2, 101.4, -356.8), (0.5, 102.0, -354.0))
        say('camoff', 'effect clear @s night_vision', 'gamemode survival', *TOUGH, U + 'tp @s 0.5 101 -354 180 0', 'wait 40')
    if 'king' in which:
        say('deathbound story 2 0', 'kill @e[type=deathbound:deaths_guard]', 'kill @e[type=deathbound:prophet]', 'clear @s',
            'give @s deathbound:deathbound_relic', 'wait 10', 'deathbound ending king')
        stills('e2_king', (50, 125, 170, 300, 430, 520, 585, 640, 720))
        say('wait 40', 'data get entity @s Inventory', 'inv', 'wait 10', 'shot e2_king_inv', 'close',
            'gamemode spectator', 'effect give @s night_vision infinite 0 true', 'wait 20')
        cam('e2_king_front', (0.5, 106.4, -359.8), (0.5, 105.9, -363.5), 20)
        cam('e2_king_close', (1.4, 106.5, -361.6), (0.5, 106.3, -363.5))
        cam('e2_king_side', (3.6, 105.8, -362.0), (0.5, 105.6, -363.5))
        say('camoff', 'effect clear @s night_vision', 'gamemode survival', *TOUGH, U + 'tp @s 0.5 101 -354 180 0', 'wait 40')
    if 'break' in which:
        say('deathbound story 2 0', 'kill @e[type=deathbound:deaths_guard]', 'kill @e[type=deathbound:prophet]', 'clear @s', 'deathbound ending break')
        stills('e2_break', (40, 150, 236))
        say('wait 20', 'look 0 6', 'hold forward 1040', 'hold sprint 1040', 'hold jump 1040')
        for k in range(10):
            say('wait 50', f'shot e2_run_{k:02d}a', 'view front', 'wait 50', f'shot e2_run_{k:02d}b', 'view first', U + 'data get entity @s Pos', 'look 0 6')
        say('wait 60', 'shot e2_fallen_a', 'wait 60', 'shot e2_fallen_b', 'wait 60', 'shot e2_fallen_c', 'wait 100', U + 'data get entity @s Pos')
    say('quit')


def session_fx():
    """The Ferryman's boat, the Hunter's trophy stakes, purple hearts from a charred one's touch."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 8 104 -6', 'wait 240')
    still('fx_ferry_dock', (7.0, 103.2, -4.5), (12.0, 99.6, -11.0), 10)
    still('fx_ferry_north', (11.5, 103.6, -17.5), (11.8, 99.6, -11.0), 5)
    still('fx_ferry_bow', (17.0, 101.6, -8.6), (13.6, 100.6, -11.0), 5)
    still('fx_ferry_stern', (6.5, 101.4, -13.5), (10.5, 99.8, -11.0), 5)
    still('fx_ferry_inside', (11.0, 102.8, -11.0), (14.0, 99.5, -11.0), 5)
    say(U + 'tp @s -200 100 -126', 'wait 240')
    still('fx_stake_a', (-195.5, 95.6, -119.5), (-198.6, 93.6, -122.9), 10)
    still('fx_stake_b', (-201.0, 94.4, -120.5), (-198.6, 93.9, -122.9), 5)
    still('fx_stake_far', (-190.0, 99.0, -112.0), (-206.0, 93.0, -126.0), 5)
    say(U + 'tp @s -140 98 -100', 'wait 200')
    still('fx_stake_watch', (-138.5, 98.6, -99.5), (-143.0, 97.6, -103.0), 10)
    # a charred one's touch
    say('camoff', U + 'tp @s 0.5 101 3 180 0', 'wait 60', 'gamemode survival', 'effect give @s resistance infinite 3 true', 'hud on', 'view first',
        U + 'summon deathbound:gravebound 0.5 101 1.0 {variant:3,PersistenceRequired:1b}', 'look 180 10', 'wait 120',
        U + 'data get entity @s active_effects', 'shot fx_marked_hud', 'inv', 'wait 10', 'shot fx_marked_inv', 'close',
        'kill @e[type=deathbound:gravebound]', 'effect give @s deathbound:marked 30 0', 'wait 10', 'shot fx_marked_hud2', 'quit')


def session_mere():
    """The Mere and Clatter: soulwater, the pier and crane, the ribcage forge, the quench, his dialog, a tempered sword."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 40 104 90', 'wait 260')
    still('m1_mere_over', (54.0, 109.0, 92.0), (38.0, 97.0, 74.0), 20)
    still('m2_mere_low', (26.0, 99.6, 86.0), (40.0, 98.0, 73.0), 5)
    still('m3_pier', (36.0, 102.0, 67.5), (45.0, 98.6, 75.0), 5)
    still('m4_clatter_face', (39.6, 99.3, 75.6), (42.5, 98.9, 75.5), 5)
    still('m5_clatter_34', (40.2, 99.8, 72.2), (42.5, 98.8, 75.5), 5)
    say('wait 50', 'shot m6_clatter_work_a', 'wait 25', 'shot m6_clatter_work_b', 'wait 25', 'shot m6_clatter_work_c')
    still('m6_crane', (35.5, 100.0, 69.0), (40.0, 100.0, 73.5), 5)
    still('m7_forge', (52.5, 100.4, 78.6), (48.0, 99.0, 74.0), 5)
    still('m7_forge_b', (49.0, 100.6, 71.2), (51.5, 99.0, 76.0), 5)
    still('m8_water_close', (36.5, 98.8, 76.5), (34.0, 97.6, 76.0), 5)
    still('m9_landing_grass', (8.0, 102.5, 10.0), (0.0, 100.5, 0.0), 30)
    # talk to him, and have a sword tempered
    say('camoff', U + 'tp @s 44.3 98 75.5 90 10', 'wait 40', 'gamemode survival', 'hud on',
        'clear @s', 'give @s diamond_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10',
        'look 90 15', 'use 5', 'wait 30', 'shot m10_dialog', 'close', 'wait 5',
        'deathbound temper 1', 'wait 12', 'shot m11_temper', 'wait 30', 'shot m11_temper_b', 'quit')

def session_folk():
    """The small folk and their favors: kids, the Lamplighter, Mira, the Sentry, the quest chests, the journal, the wisp, more ghosts."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', U + 'tp @s 40 104 90', 'wait 240')
    still('f1_forge_ribs', (53.0, 101.5, 81.0), (49.0, 100.0, 75.0), 10)
    still('f2_forge_ribs_b', (44.5, 100.0, 79.0), (50.0, 101.5, 74.0), 5)
    # Lantern's End: the children, the Lamplighter
    say(U + 'tp @s 82 104 -44', 'wait 260')
    still('f3_kids', (90.0, 103.0, -36.0), (82.0, 100.0, -44.0), 60)
    say('wait 60', 'shot f3_kids_b')
    still('f4_lamplighter', (87.5, 101.6, -46.5), (87.5, 101.2, -50.0), 10)
    say('rec f_kids 30', 'pan 6 92.0 103.5 -34.0 74.0 103.0 -36.0 82.0 100.0 -44.0', 'wait 125', 'stop', 'camoff')
    # quests from the Lamplighter and a kid, by command, the dialogs shot
    say('gamemode survival', 'hud on', U + 'tp @s 87.5 100 -47.5 180 0', 'wait 30', 'deathbound quest lamplighter', 'wait 15', 'shot f5_lamp_offer', 'close',
        'deathbound accept lamps', 'wait 10', 'give @s deathbound:soul_jar 3', 'deathbound quest lamplighter', 'wait 15', 'shot f6_lamp_done', 'close',
        'deathbound quest kid', 'wait 15', 'shot f7_kid_offer', 'close', 'deathbound accept ball', 'deathbound accept oar', 'deathbound accept name',
        'deathbound accept mira', 'wait 10', 'use 5', 'wait 20', 'shot f8_journal', 'close', 'wait 5')
    # the wisp
    say('effect clear @s night_vision', U + 'tp @s 70 101 -30 90 10', 'wait 40', 'clear @s deathbound:underworld_journal', 'give @s deathbound:lantern_wisp',
        'wait 5', 'use 3', 'wait 40', 'view back', 'wait 20', 'shot f9_wisp_back', 'view front', 'wait 20', 'shot f9_wisp_front', 'view first', 'wait 5',
        'summon deathbound:gravebound ~6 ~ ~', 'wait 30', 'shot f10_wisp_warn', 'kill @e[type=deathbound:gravebound]', 'gamemode creative',
        'effect give @s night_vision infinite 0 true')
    # Mira, the Sentry
    say(U + 'tp @s 0 104 -215', 'wait 240')
    still('f11_mira', (-3.5, 101.8, -223.5), (-2.5, 101.4, -227.0), 10)
    say(U + 'tp @s -2.5 101 -223.5 180 0', 'wait 10', 'deathbound quest mira', 'wait 15', 'shot f12_mira_met', 'close')
    say(U + 'tp @s -136 100 -100', 'wait 240')
    still('f13_sentry', (-137.0, 98.5, -99.0), (-140.5, 98.2, -99.0), 10)
    still('f14_sentry_b', (-144.0, 99.0, -96.0), (-140.5, 98.0, -99.0), 5)
    # where the lost things are
    still('f15_tag_bridge', (-162.0, 95.0, -106.0), (-167.0, 92.0, -109.0), 30)
    say(U + 'tp @s -72 106 -20', 'wait 220')
    still('f16_boatshed', (-66.0, 106.0, -15.0), (-71.0, 103.0, -21.5), 10)
    say(U + 'tp @s 0 104 -80', 'wait 220')
    still('f17_footbridge_ball', (4.5, 99.5, -78.5), (1.0, 97.5, -81.0), 10)
    # more ghosts
    say(U + 'tp @s 0 101 -110 0 0', 'wait 60')
    for i, t in enumerate((3, 4, 5, 0, 1, 2)):
        say(U + f'summon deathbound:lost_soul {-3 + i * 1.3:.1f} 99 -104 {{tint:{t},NoAI:1b,Rotation:[180f,0f]}}')
    still('f18_ghosts', (0.5, 101.0, -108.5), (0.5, 100.0, -104.0), 30)
    say('quit')


def session_town():
    """Lantern's End, bigger: the new streets and lamps, the market, the chapel, the fountain and swing, the children."""
    fmt = lambda v: ' '.join(map(str, v))

    def still(name, at, look, warm=40):
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}', f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')

    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 82 110 -44', 'wait 300')
    still('t1_town_over', (118.0, 128.0, -8.0), (82.0, 99.0, -44.0), 60)
    still('t2_town_over_b', (46.0, 125.0, -80.0), (82.0, 99.0, -44.0), 60)
    still('t3_market', (91.0, 102.5, -50.0), (90.5, 100.5, -56.0), 10)
    still('t4_chapel', (66.0, 102.5, -55.0), (61.5, 101.0, -62.0), 10)
    still('t5_chapel_in', (61.5, 101.6, -58.0), (61.5, 100.8, -63.5), 5)
    still('t6_fountain', (88.0, 102.5, -12.0), (83.0, 99.5, -17.0), 10)
    still('t7_street_lamps', (83.0, 101.0, -18.0), (83.0, 100.5, -44.0), 5)
    say('wait 100')
    still('t8_kids', (88.0, 104.0, -36.0), (82.0, 99.5, -44.0), 10)
    say('wait 60', 'shot t8_kids_b')
    say('rec t_kids 30', 'pan 7 92.0 103.0 -36.0 74.0 103.0 -34.0 82.0 99.5 -44.0', 'wait 145', 'stop', 'camoff')
    # the journal, properly this time: looking at the ground
    say('gamemode survival', 'hud on', 'deathbound accept lamps', 'deathbound accept oar', 'wait 10', 'look 0 80', 'wait 5', 'use 5', 'wait 20', 'shot t9_journal',
        'close', 'quit')


def session_ev2(only=('take', 'king', 'break')):
    """The endings, reworked, filmed whole: power (the Warden kneels), order with a price, and the fall (the run home)."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 0 101 -340', 'wait 240',
        'deathbound story 2 0', 'deathbound seals 7', 'wait 40', 'kill @e[type=deathbound:death]', 'clear @s', U + 'tp @s 0.5 101 -330 180 -8', 'wait 160')
    for which in only:
        say('deathbound story 2 0', 'kill @e[type=deathbound:death]', 'kill @e[type=deathbound:deaths_guard]', 'kill @e[type=deathbound:prophet]',
            'kill @e[type=deathbound:lost_soul]', 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:heart_of_death',
            'give @s deathbound:deathbound_relic', U + 'tp @s 0.5 104 -360.5 180 30', 'gamemode survival', *TOUGH, 'wait 200',
            'hud on', 'look 180 32', f'rec ev2_{which} 30', 'wait 30', 'use 5', 'wait 60', 'close', 'wait 5', f'deathbound ending {which}')
        if which == 'take':
            say('wait 470', 'stop', 'wait 20')
        elif which == 'king':
            say('wait 760', 'stop', 'wait 20')
        else:   # the cutscene, then the run home with the camera out in front of you, then the last shot from the Landing
            say('wait 265', 'look 0 6', 'view front', 'hold forward 1100', 'hold sprint 1100', 'hold jump 1100', 'wait 1400', 'stop', 'view first')
        say('gamemode creative', U + 'tp @s 0.5 101 -330 180 -8', 'wait 60')
    say('quit')


def session_tr2():
    """Footage for the update trailer: the Mere and Clatter, soulwater, the bigger town and its children, the folk, the
    ferry, the wisp, the journal, the Hunter's ground, purple hearts, more ghosts."""
    fmt = lambda v: ' '.join(map(str, v))

    def shot(name, secs, a, b, at, warm=40):
        pan(name, secs, a, b, at, warm=warm)

    say('hud off', 'fov 64', 'gamemode creative', 'view first', 'filmlight on', 'effect give @s night_vision infinite 0 true',
        U + 'tp @s 40 104 90', 'wait 260')
    # the Mere
    shot('tr_mere_wide', 7, (20.0, 104.0, 92.0), (28.0, 101.5, 84.0), (42.0, 98.0, 74.0), 30)
    say('effect clear @s night_vision', 'wait 5')
    shot('tr_soulwater', 5, (30.0, 98.4, 79.0), (34.0, 98.2, 78.0), (40.0, 97.2, 74.5), 20)
    shot('tr_clatter_work', 6, (39.4, 99.6, 77.5), (39.6, 99.4, 73.5), (42.5, 98.9, 75.5), 20)
    # a quench, done for you
    say(U + 'tp @s 46.5 98 75.5 90 5', 'gamemode survival', 'effect give @s invisibility infinite 0 true', 'clear @s', 'give @s diamond_sword',
        'give @s deathbound:grave_rune 3', 'give @s deathbound:soul 40', 'wait 20')
    say('orbit 0 42.5 99.0 75.5 3.6 1.2 200 200', 'wait 30', 'rec tr_quench 30', 'orbit 6 42.5 99.0 75.5 3.6 1.2 200 260', 'wait 20',
        'deathbound temper 1', 'wait 110', 'stop', 'camoff', 'gamemode creative', 'effect clear @s invisibility')
    say('effect give @s night_vision infinite 0 true')
    shot('tr_ribcage', 6, (54.0, 101.0, 80.0), (52.0, 101.5, 70.0), (49.0, 100.5, 75.0), 20)
    # Lantern's End
    say(U + 'tp @s 82 106 -44', 'wait 300')
    shot('tr_town', 8, (112.0, 118.0, -20.0), (100.0, 112.0, -70.0), (82.0, 100.0, -44.0), 40)
    say('effect clear @s night_vision', 'wait 5')
    say('follow 0 deathbound:skeleton_kid 3.2 1.0 -30 -30 0.5', 'wait 30', 'rec tr_kids 30', 'follow 6 deathbound:skeleton_kid 3.2 1.0 -30 50 0.5', 'wait 125', 'stop')
    say('orbit 0 87.5 101.2 -50.0 3.4 1.4 -40 -40', 'wait 30', 'rec tr_lamplighter 30', 'orbit 6 87.5 101.2 -50.0 3.4 1.4 -40 30', 'wait 125', 'stop')
    shot('tr_fountain', 6, (88.0, 102.0, -12.0), (78.0, 102.0, -12.0), (83.0, 99.5, -17.0), 20)
    shot('tr_market', 5, (95.0, 102.5, -50.0), (87.0, 102.5, -50.0), (90.5, 100.5, -56.0), 20)
    # the Gate: Mira in the line; the Watch: the Sentry
    say('effect give @s night_vision infinite 0 true', U + 'tp @s 0 104 -215', 'wait 260')
    shot('tr_mira', 6, (-6.5, 102.0, -221.0), (-4.5, 101.6, -224.0), (-2.5, 101.2, -227.0), 20)
    say(U + 'tp @s -136 100 -100', 'wait 260')
    shot('tr_sentry', 7, (-138.0, 101.0, -96.0), (-138.5, 101.5, -101.0), (-160.0, 97.0, -108.0), 20)
    # the Hollow: his trophies
    say(U + 'tp @s -200 100 -126', 'wait 260')
    shot('tr_hollow', 7, (-190.0, 99.0, -112.0), (-196.0, 96.0, -118.0), (-206.0, 93.5, -126.0), 20)
    # the Landing: the ferry
    say(U + 'tp @s 10 104 -8', 'wait 220')
    shot('tr_ferry', 7, (8.0, 102.5, -6.0), (16.0, 101.5, -7.0), (11.8, 99.6, -11.0), 20)
    # the wisp, in the dark streets, and the journal
    say('effect clear @s night_vision', U + 'tp @s 70 100 -44 270 5', 'wait 60', 'gamemode survival', *TOUGH, 'clear @s', 'give @s deathbound:lantern_wisp',
        'wait 5', 'use 3', 'wait 40', 'look 270 5', 'follow 0 minecraft:player 3.2 1.8 140 140 1.5', 'wait 20', 'rec tr_wisp 30',
        'hold forward 120', 'follow 6 minecraft:player 3.2 1.8 140 200 1.5', 'wait 125', 'stop', 'camoff')
    say('deathbound accept mira', 'deathbound accept ball', 'deathbound accept lamps', 'wait 10', 'clear @s deathbound:aldous_lantern', 'hud on', 'look 0 85',
        'use 5', 'wait 10', 'rec tr_journal 30', 'wait 70', 'stop', 'close', 'hud off')
    # purple hearts
    say(U + 'tp @s 0.5 101 3 180 0', 'wait 40', 'gamemode survival', *TOUGH, 'hud on',
        U + 'summon deathbound:gravebound 0.5 101 1.0 {variant:3,PersistenceRequired:1b}', 'look 180 10', 'rec tr_marked 30', 'wait 140', 'stop',
        'kill @e[type=deathbound:gravebound]', 'hud off', 'gamemode creative')
    # more of the dead
    say('effect give @s night_vision infinite 0 true', U + 'tp @s 0 101 -110 0 0', 'wait 60')
    for i, t in enumerate((3, 4, 5, 0, 1, 2, 3, 5)):
        say(U + f'summon deathbound:lost_soul {-4.5 + i * 1.3:.1f} 99 {-104 - (i % 2) * 1.5} {{tint:{t},NoAI:1b,Rotation:[180f,0f]}}')
    shot('tr_ghosts', 6, (-5.0, 100.8, -109.0), (5.0, 100.8, -109.0), (0.0, 100.2, -104.5), 30)
    say('quit')


def session_tr2b():
    """Re-shoots for the update trailer: the Mere wide, the quench, Mira, the Sentry, the ferry, the wisp, the journal, the ghosts."""
    def shot(name, secs, a, b, at, warm=40):
        pan(name, secs, a, b, at, warm=warm)

    say('hud off', 'fov 64', 'gamemode creative', 'view first', 'filmlight on', 'effect give @s night_vision infinite 0 true',
        U + 'tp @s 40 104 90', 'wait 260')
    shot('tr_mere_wide', 7, (22.0, 103.5, 70.0), (24.0, 102.0, 80.0), (40.0, 97.5, 75.0), 30)
    say('effect clear @s night_vision', 'wait 5')
    say(U + 'tp @s 46.5 98 75.5 90 5', 'gamemode survival', 'effect give @s invisibility infinite 0 true', 'clear @s', 'give @s diamond_sword',
        'give @s deathbound:grave_rune 3', 'give @s deathbound:soul 40', 'wait 20')
    say('orbit 0 42.5 99.2 75.5 5.2 1.0 230 230', 'wait 30', 'rec tr_quench 30', 'orbit 6 42.5 99.2 75.5 5.2 1.0 230 290', 'wait 20',
        'deathbound temper 1', 'wait 110', 'stop', 'camoff', 'gamemode creative', 'effect clear @s invisibility')
    say('effect give @s night_vision infinite 0 true', U + 'tp @s 0 104 -215', 'wait 300', U + 'tp @s 6 104 -215', 'wait 40')
    shot('tr_mira', 6, (-6.0, 102.2, -219.5), (-5.5, 101.8, -222.5), (-3.0, 101.0, -227.0), 30)
    say(U + 'tp @s -136 100 -100', 'wait 260')
    say('effect clear @s night_vision', 'wait 5')
    shot('tr_sentry', 7, (-137.5, 100.3, -94.0), (-139.0, 100.0, -97.0), (-141.5, 99.6, -99.5), 20)
    say('effect give @s night_vision infinite 0 true', U + 'tp @s 10 104 -18', 'wait 220')
    shot('tr_ferry', 7, (6.5, 101.4, -15.5), (16.0, 101.4, -15.5), (11.8, 99.5, -11.0), 20)
    # the wisp: you walk toward the camera down a dark street, the lantern at your shoulder
    say('effect clear @s night_vision', U + 'tp @s 76 100 -44 90 5', 'wait 60', 'gamemode survival', *TOUGH, 'clear @s', 'give @s deathbound:lantern_wisp',
        'wait 5', 'use 3', 'wait 40', 'look 90 5', 'follow 0 minecraft:player 3.6 1.9 70 70 1.6', 'wait 20', 'rec tr_wisp 30',
        'hold forward 120', 'follow 6 minecraft:player 3.6 1.9 70 110 1.6', 'wait 125', 'stop', 'camoff')
    say('use 3', 'clear @s', 'deathbound accept mira', 'deathbound accept ball', 'deathbound accept lamps', 'wait 10', 'clear @s deathbound:aldous_lantern',
        'hud on', 'look 0 85', 'use 5', 'wait 15', 'rec tr_journal 30', 'wait 70', 'stop', 'close', 'hud off', 'gamemode creative')
    say('effect give @s night_vision infinite 0 true', U + 'tp @s 0 101 -114 0 0', 'wait 60')
    for i, t in enumerate((3, 4, 5, 0, 1, 2, 3, 5)):
        say(U + f'summon deathbound:lost_soul {-4.5 + i * 1.3:.1f} 99 {-104 - (i % 2) * 1.5} {{tint:{t},NoAI:1b,Rotation:[180f,0f]}}')
    shot('tr_ghosts', 6, (-6.0, 101.0, -111.5), (6.0, 101.0, -111.5), (0.0, 100.4, -104.5), 30)
    say('quit')


def session_dbg_run():
    """Why doesn't the player run in ev2? The same steps, with positions and effects logged."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 0 101 -340', 'wait 240',
        'deathbound story 2 0', 'deathbound seals 7', 'wait 40', 'kill @e[type=deathbound:death]', 'clear @s', U + 'tp @s 0.5 101 -330 180 -8', 'wait 160')
    say('deathbound story 2 0', 'kill @e[type=deathbound:deaths_guard]', 'kill @e[type=deathbound:prophet]', 'clear @s',
        'item replace entity @s weapon.mainhand with deathbound:heart_of_death', 'give @s deathbound:deathbound_relic', U + 'tp @s 0.5 104 -360.5 180 30',
        'gamemode survival', *TOUGH, 'wait 200', 'look 180 32', 'rec dbg_run 30', 'wait 30', 'use 5', 'wait 60', 'close', 'wait 5', 'deathbound ending break',
        'wait 265', U + 'data get entity @s Pos', U + 'data get entity @s active_effects', U + 'data get entity @s playerGameType', 'look 0 6',
        'hold forward 400', 'hold sprint 400', 'hold jump 400')
    for k in range(8):
        say('wait 50', U + 'data get entity @s Pos', f'shot dbg_{k}')
    say('stop', 'quit')


def session_tr2c():
    """Last re-shoots: the wisp (front view, walking), the journal open, the Sentry with a little light."""
    say('hud off', 'fov 64', 'gamemode creative', 'view first', 'filmlight on')
    say(U + 'tp @s 76 100 -44 90 5', 'wait 260', 'gamemode survival', *TOUGH, 'clear @s', 'give @s deathbound:lantern_wisp',
        'wait 5', 'use 3', 'wait 40', 'look 90 8', 'view front', 'wait 20', 'rec tr_wisp 30', 'hold forward 120', 'wait 130', 'stop', 'view first')
    say('use 3', 'clear @s', 'deathbound accept mira', 'deathbound accept ball', 'deathbound accept lamps', 'wait 10', 'clear @s',
        'item replace entity @s weapon.mainhand with deathbound:underworld_journal', 'hud on', 'look 0 85', 'wait 5', 'use 5', 'wait 15',
        'rec tr_journal 30', 'wait 70', 'stop', 'close', 'hud off', 'gamemode creative')
    say('effect give @s night_vision infinite 0 true', U + 'tp @s -136 100 -100', 'wait 260')
    pan('tr_sentry', 7, (-137.5, 100.3, -94.0), (-139.0, 100.0, -97.0), (-141.5, 99.6, -99.5), warm=20)
    say('quit')


def session_tr2d():
    """The Sentry, from the side he isn't watching."""
    say('hud off', 'fov 64', 'gamemode creative', 'view first', 'filmlight on', 'effect give @s night_vision infinite 0 true', U + 'tp @s -136 100 -100', 'wait 260')
    pan('tr_sentry', 7, (-145.5, 99.8, -94.5), (-144.0, 99.3, -97.0), (-140.5, 98.7, -99.0), warm=20)
    say('quit')


# ---------------------------------------------------------------------------------------------------- batch 3 checks
def _still(name, at, look, warm=40, tp=True):
    fmt = lambda v: ' '.join(map(str, v))
    if tp:
        say(U + f'tp @s {at[0]} {at[1] - 1.62} {at[2]}')
    say(f'pan 0 {fmt(at)} {fmt(at)} {fmt(look)}', f'wait {warm}', f'shot {name}')


def _timed(prefix, ticks):
    last = 0
    for t in ticks:
        say(f'wait {t - last}', f'shot {prefix}_{t:03d}')
        last = t


def session_t3a():
    """The Mere: the new soulwater, the rebuilt ribcage, talking to Clatter, and the quench done in front of you."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 40 104 90', 'wait 260')
    _still('t3_water_close', (36.5, 98.8, 76.5), (33.0, 97.4, 75.0), 20)
    _still('t3_water_wide', (26.0, 100.2, 86.0), (38.0, 97.5, 74.0), 5)
    _still('t3_ribs_axis', (57.5, 101.2, 75.5), (48.0, 101.0, 75.5), 5)
    _still('t3_ribs_side', (52.5, 100.4, 79.6), (49.0, 100.5, 74.0), 5)
    _still('t3_ribs_under', (49.5, 99.6, 75.5), (49.0, 104.0, 72.5), 5)
    _still('t3_ribs_out', (60.0, 104.0, 84.0), (49.0, 100.5, 75.5), 5)
    say('camoff', U + 'tp @s 44.0 98 75.5 90 15', 'wait 40', 'gamemode survival', 'hud on', *TOUGH,
        'clear @s', 'give @s diamond_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    # the conversation: typed pages, then the buttons; the question list; one answer; back
    say('use 3', 'wait 10', 'shot t3_dlg_typing')
    for i in range(4):
        say('next', 'wait 4', f'shot t3_dlg_page{i}', 'wait 30')
    say('wait 10', 'shot t3_dlg_buttons', 'pick 0', 'wait 30', 'shot t3_dlg_ask', 'pick 0', 'wait 50', 'shot t3_dlg_topic', 'next', 'next', 'wait 4', 'next',
        'wait 30', 'shot t3_dlg_topic_end', 'pick -1', 'wait 30', 'shot t3_dlg_back_to_ask', 'pick -1', 'wait 30', 'shot t3_dlg_back_to_talk')
    say('pick 1', 'wait 40', 'shot t3_dlg_temper_menu', 'close', 'wait 10')
    # the quench, from the side
    say('pan 0 41.5 99.6 80.0 41.5 99.6 80.0 41.0 98.4 75.5', 'wait 20', 'rec t3_quench 30', 'deathbound temper 1')
    _timed('t3_quench', (6, 14, 20, 26, 30, 38, 46, 54, 62, 70, 78, 86, 94, 102))
    say('wait 30', 'stop', 'camoff', 'wait 5', 'shot t3_quench_card', 'inv', 'wait 5', 'shot t3_quench_inv', 'close')
    # a tempered blade's bite
    say(U + 'summon deathbound:gravebound 41.5 98 75.5 {PersistenceRequired:1b,NoAI:1b}', 'wait 10', 'look 90 20', 'swing', 'click', 'wait 2', 'shot t3_bite', 'quit')


def _climb(name, cx, cz, y, ring, top):
    """Walk a stair step by step: put you on each step facing the next, walk, and log where you got to."""
    import math
    for k in range(top):
        p, q = ring[k % len(ring)], ring[(k + 1) % len(ring)]
        dx, dz = q[0] - p[0], q[1] - p[1]
        yaw = math.degrees(math.atan2(-dx, dz))
        h = y + 1 + k
        say(U + f'tp @s {cx + p[0] + 0.5} {h + 1} {cz + p[1] + 0.5} {yaw:.0f} 10', 'hold forward 7', 'wait 9',
            f'tellraw @s "{name} {k} want {h + 2}"', U + 'data get entity @s Pos[1]')


SPIRE_RING = [(1, 1), (1, 0), (1, -1), (0, -1), (-1, -1), (-1, 0), (-1, 1), (0, 1)]
WATCH_RING = [(2, -1), (2, -2), (1, -2), (0, -2), (-1, -2), (-2, -2), (-2, -1), (-2, 0), (-2, 1), (-2, 2), (-1, 2), (0, 2), (1, 2), (2, 2), (2, 1), (2, 0)]


def session_t3b():
    """Aldous forging a chestplate in front of you; the Western Watch furnished, its new stair; both stairs climbed."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 88 100 -44', 'wait 260')
    say(U + 'tp @s 78.0 100 -35.5 90 10', 'wait 20', 'gamemode survival', 'hud on', *TOUGH, 'clear @s',
        'item replace entity @s armor.chest with minecraft:iron_chestplate', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    say('pan 0 78.6 101.9 -33.4 78.6 101.9 -33.4 74.8 100.8 -36.6', 'wait 20', 'rec t3_forge 30', 'deathbound forge chest')
    _timed('t3_forge', (6, 12, 20, 30, 42, 48, 56, 66, 78, 90, 98, 106, 112, 118, 126, 134))
    say('wait 30', 'stop', 'camoff', 'wait 5', 'shot t3_forge_card', 'inv', 'wait 5', 'shot t3_forge_inv', 'close')
    # the Watch
    say('gamemode creative', 'hud off', 'effect give @s night_vision infinite 0 true', U + 'tp @s -130 100 -96', 'wait 240')
    _still('t3_watch_out', (-124.0, 104.0, -88.0), (-136.0, 106.0, -100.0), 20)
    _still('t3_watch_ground', (-134.4, 98.8, -98.4), (-137.0, 97.6, -101.0), 5)
    _still('t3_watch_well', (-135.6, 98.0, -99.4), (-136.5, 112.0, -100.5), 5)
    _still('t3_watch_f1', (-134.5, 106.7, -101.5), (-136.0, 105.0, -99.0), 5)
    _still('t3_watch_f1b', (-137.5, 106.7, -98.5), (-134.5, 105.0, -101.0), 5)
    _still('t3_watch_f2', (-134.5, 114.7, -98.5), (-136.0, 113.0, -101.0), 5)
    _still('t3_watch_f3', (-134.5, 122.8, -98.5), (-136.5, 121.0, -100.5), 5)
    _still('t3_watch_f3b', (-137.5, 123.4, -98.5), (-135.0, 121.0, -101.0), 5)
    # climbing: every step of both stairs, walked onto from the one before
    say('camoff', 'hud on', 'gamemode survival', *TOUGH)
    _climb('watch', -136, -100, 96, WATCH_RING, 23)
    say(U + 'tp @s 96 102 -146', 'wait 200')
    _climb('spire', 96, -152, 100, SPIRE_RING, 39)
    say('quit')


def session_t3c():
    """The Hunter's riven shot, and the First Hunter's Bow loosing its pack."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s -206 94 -126', 'wait 260', 'gamemode survival', *TOUGH,
        U + 'tp @s -210 93 -122 0 0', 'wait 140')
    say('shoulder 0 deathbound:hollow_hunter minecraft:player 1.4 2.8 0.7', 'wait 30', 'rec t3_riven 30', U + 'deathbound action 6')
    _timed('t3_riven', (10, 24, 33, 38, 41, 43, 46, 52, 60))
    say('wait 20', 'stop', 'camoff', 'wait 5', 'view back', 'wait 5', 'shot t3_riven_after', 'view first')
    # the bow, at the Landing
    say(U + 'tp @s 0.5 101 10 180 0', 'wait 200', 'kill @e[type=deathbound:gravebound]', 'clear @s', 'give @s deathbound:hunters_bow', 'give @s arrow 32',
        U + 'summon deathbound:gravebound -5 101 -6 {PersistenceRequired:1b}', U + 'summon deathbound:gravebound 0.5 101 -9 {PersistenceRequired:1b}',
        U + 'summon deathbound:gravebound 6 101 -6 {PersistenceRequired:1b}', 'wait 10', 'look 180 -4', 'view back')
    say('rec t3_pack 30', 'use 80')
    _timed('t3_pack', (20, 62, 70, 82, 85, 89, 94, 100, 110))
    say('wait 10', 'stop', 'view first', 'quit')


def session_t3d():
    """The Death King's death, slow; the music handing over; the victory card on its own."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0 101 -330', 'wait 240',
        'deathbound story 1 0', 'deathbound seals 7', 'wait 30', 'gamemode survival', *TOUGH, U + 'tp @s 0.5 101 -340 180 0', 'wait 300')
    say(U + 'deathbound phase 3', 'wait 40', 'kill @e[type=deathbound:soul_anchor]', 'wait 20')
    follow('t3_death', 11, 'deathbound:death', 9.0, 2.5, -35, 35, 2.2,
           events=[(10, 'kill @e[type=deathbound:death]')] + [(t, f'shot t3_death_{t:03d}') for t in (14, 30, 45, 60, 80, 100, 120, 140, 160, 175, 182, 190, 205)])
    say('camoff', 'wait 10', 'shot t3_after_a', 'wait 60', 'shot t3_after_b', 'wait 200', 'shot t3_after_c', 'quit')


def session_tr3a():
    """Trailer 3, the Mere: the new water, the rebuilt ribcage, the quench."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 40 104 90', 'wait 260')
    pan('tr3_mere', 8, (22.0, 101.5, 90.0), (28.0, 100.6, 86.0), (42.0, 98.0, 74.0), warm=30)
    pan('tr3_water', 6, (31.0, 98.9, 80.0), (34.5, 98.7, 79.0), (36.0, 97.2, 74.0), warm=20)
    orbit('tr3_ribs', 7, (50.0, 101.0, 75.5), 9.0, 1.2, 215, 255, warm=20)
    say('camoff', U + 'tp @s 44.0 98 75.5 90 15', 'wait 30', 'gamemode survival', *TOUGH,
        'clear @s', 'give @s netherite_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('tr3_quench', 'pan', 6, '41.6 99.5 79.8 41.6 99.5 79.8 41.0 98.4 75.5', '41.6 99.5 79.8 40.8 99.7 79.4 41.0 98.6 75.5',
         warm=20, events=[(4, 'deathbound temper 1')], tail=10)
    say('quit')


def session_tr3b():
    """Trailer 3: Aldous forging, and inside the Western Watch."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 88 100 -44', 'wait 260')
    say(U + 'tp @s 78.0 100 -35.5 90 10', 'wait 20', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s armor.chest with minecraft:netherite_chestplate', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('tr3_forge', 'pan', 7, '78.6 101.9 -33.4 78.6 101.9 -33.4 74.8 100.8 -36.6', '78.6 101.9 -33.4 78.2 101.6 -33.9 75.2 100.8 -36.4',
         warm=20, events=[(4, 'deathbound forge chest')], tail=6)
    say('gamemode creative', U + 'tp @s -130 100 -96', 'wait 240')
    pan('tr3_watch', 6, (-134.5, 106.7, -98.5), (-134.6, 106.4, -101.4), (-137.0, 105.2, -100.0), warm=20)
    say('quit')


def session_tr3c():
    """Trailer 3: the Hunter's riven shot, the bow's pack, the Death King's slow death."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s -206 94 -126', 'wait 260', 'gamemode survival', *TOUGH,
        U + 'tp @s -210 93 -122 0 0', 'wait 140')
    take('tr3_riven', 'shoulder', 4, 'deathbound:hollow_hunter minecraft:player 1.4 2.8 0.7', 'deathbound:hollow_hunter minecraft:player 1.4 2.8 0.7 1.0 2.2 0.6',
         warm=20, events=[(2, U + 'deathbound action 6')], tail=40)
    say('camoff', U + 'tp @s 0.5 101 10 180 0', 'wait 200', 'kill @e[type=deathbound:gravebound]', 'clear @s', 'give @s deathbound:hunters_bow',
        'give @s arrow 32', U + 'summon deathbound:gravebound -5 101 -6 {PersistenceRequired:1b}',
        U + 'summon deathbound:gravebound 0.5 101 -9 {PersistenceRequired:1b}', U + 'summon deathbound:gravebound 6 101 -6 {PersistenceRequired:1b}',
        'wait 10', 'look 180 -4')
    take('tr3_pack', 'pan', 6, '3.4 102.8 13.5 3.4 102.8 13.5 -0.5 101.6 2.0', '3.4 102.8 13.5 2.6 102.6 12.4 -0.5 101.4 -2.0',
         warm=20, events=[(2, 'use 80')], tail=20)
    say('gamemode creative', U + 'tp @s 0 101 -330', 'wait 200', 'deathbound story 1 0', 'deathbound seals 7', 'wait 30', 'gamemode survival', *TOUGH,
        'nodark on', U + 'tp @s 0.5 101 -340 180 0', 'wait 300', U + 'deathbound phase 3', 'wait 40', 'kill @e[type=deathbound:soul_anchor]', 'wait 20')
    follow('tr3_death', 10, 'deathbound:death', 9.0, 2.5, -35, 25, 2.2, events=[(6, 'kill @e[type=deathbound:death]')], tail=10)
    say('quit')


ISLAND_CENTERS = [('arrival', 0, 0, 100), ('hub', 0, -95, 98), ('forest', -78, -38, 102), ('village', 82, -44, 99), ('crypt', -62, -168, 97),
                  ('spire', 96, -152, 100), ('watch', -136, -100, 96), ('gate', 0, -228, 100), ('citadel', 0, -340, 100), ('hollow', -206, -126, 92),
                  ('mere', 40, 74, 97)]


def session_t4():
    """The scythe held blade-forward; then a tour of every island auditing for anything hanging from nothing or floating."""
    say('hud off', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0.5 101 10 270 0', 'wait 240', 'clear @s',
        'item replace entity @s weapon.mainhand with deathbound:reaper_scythe', 'wait 10')
    _still('t4_scythe_side', (0.5, 102.4, 13.5), (0.5, 101.9, 10.5), 10, tp=False)
    say('swing', 'wait 2', 'shot t4_scythe_swing_a', 'wait 2', 'shot t4_scythe_swing_b', 'wait 2', 'shot t4_scythe_swing_c')
    _still('t4_scythe_front', (4.2, 102.2, 10.5), (0.5, 101.9, 10.5), 10, tp=False)
    _still('t4_scythe_back34', (-2.6, 102.6, 13.0), (0.8, 101.8, 10.3), 10, tp=False)
    say(U + 'summon deathbound:gravebound 3.0 101 10.5 {PersistenceRequired:1b,NoAI:1b,Rotation:[90f,0f]}', 'wait 10')
    _still('t4_scythe_target', (1.2, 102.4, 14.5), (1.8, 101.9, 10.5), 10, tp=False)
    say('swing', 'wait 3', 'shot t4_scythe_target_swing')
    say('camoff', 'hud on', 'wait 5', 'shot t4_scythe_fp', 'swing', 'wait 2', 'shot t4_scythe_fp_swing_a', 'wait 2', 'shot t4_scythe_fp_swing_b',
        'kill @e[type=deathbound:gravebound]')
    # the audit, island by island (a fresh world, so everything was generated with the settle pass)
    for name, x, z, y in ISLAND_CENTERS:
        say(U + f'tp @s {x} {y + 30} {z}', 'wait 160', f'tellraw @s "audit {name}"', U + 'deathbound audit 7')
    # where hanging things live: Aldous's forge, Clatter's ribcage, the hanged under the Landing, a bridge cage
    say('effect give @s night_vision infinite 0 true', 'hud off', U + 'tp @s 88 100 -44', 'wait 200')
    _still('t4_forge_in', (78.4, 101.8, -34.2), (74.0, 103.0, -37.0), 10)
    _still('t4_forge_roof', (75.5, 101.0, -35.5), (75.5, 104.5, -36.5), 5)
    say(U + 'tp @s 40 104 90', 'wait 200')
    _still('t4_ribs_blade', (52.0, 100.4, 76.5), (51.0, 102.6, 75.5), 10)
    say(U + 'tp @s 0 80 0', 'wait 160')
    _still('t4_under_landing', (6.0, 66.0, 6.0), (0.0, 75.0, 0.0), 10)
    say('quit')


def session_t5():
    """The scythe's in-hand placement, variant by variant (custom_model_data a..d, o = the old sprite)."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0.5 101 10 270 0', 'wait 200', 'clear @s')
    for k in 'oabcd':
        say(f'item replace entity @s weapon.mainhand with deathbound:reaper_scythe[custom_model_data={{strings:["{k}"]}}]', 'view first', 'wait 25',
            f'shot t5_{k}_fp', 'swing', 'wait 2', f'shot t5_{k}_fp_sw', 'wait 15', 'view front', 'wait 10', f'shot t5_{k}_front', 'swing', 'wait 2',
            f'shot t5_{k}_front_sw', 'wait 15', 'view back', 'wait 10', f'shot t5_{k}_back', 'wait 5')
    say('quit')


def session_t6():
    """The scythe in third person, from the side and three-quarter front, variants o / c / d."""
    say('hud off', 'fov 60', 'gamemode creative', 'view back', U + 'tp @s 0.5 101 10.5 270 0', 'wait 200', 'clear @s')
    for k in 'ocd':
        say(f'item replace entity @s weapon.mainhand with deathbound:reaper_scythe[custom_model_data={{strings:["{k}"]}}]', 'look 270 0', 'wait 10')
        _still(f't6_{k}_side', (0.6, 102.0, 14.0), (0.6, 101.9, 10.5), 10, tp=False)
        say('swing', 'wait 2', f'shot t6_{k}_side_sw2', 'wait 2', f'shot t6_{k}_side_sw4')
        _still(f't6_{k}_34', (3.6, 102.2, 13.2), (0.6, 101.8, 10.5), 10, tp=False)
        say('swing', 'wait 2', f'shot t6_{k}_34_sw2', 'wait 2', f'shot t6_{k}_34_sw4', 'camoff', 'wait 5')
    say('quit')


def session_t7():
    """The scythe after the fix: side, three-quarter and first person, idle and mid-swing, with something to swing at."""
    say('hud off', 'fov 60', 'gamemode creative', 'view back', U + 'tp @s 0.5 101 10.5 270 0', 'wait 200', 'clear @s',
        'item replace entity @s weapon.mainhand with deathbound:reaper_scythe',
        U + 'summon deathbound:gravebound 3.2 101 10.5 {PersistenceRequired:1b,NoAI:1b,Rotation:[90f,0f]}', 'look 270 0', 'wait 10')
    _still('t7_side', (1.4, 102.0, 14.2), (1.4, 101.9, 10.5), 10, tp=False)
    say('swing', 'wait 1', 'shot t7_side_sw1', 'wait 1', 'shot t7_side_sw2', 'wait 1', 'shot t7_side_sw3', 'wait 1', 'shot t7_side_sw4')
    _still('t7_34', (4.4, 102.2, 13.6), (0.8, 101.8, 10.5), 10, tp=False)
    say('swing', 'wait 2', 'shot t7_34_sw2', 'wait 2', 'shot t7_34_sw4')
    _still('t7_behind', (-2.8, 102.6, 12.2), (2.0, 101.6, 10.5), 10, tp=False)
    say('swing', 'wait 2', 'shot t7_behind_sw2')
    say('camoff', 'view first', 'hud on', 'wait 20', 'shot t7_fp', 'swing', 'wait 1', 'shot t7_fp_sw1', 'wait 1', 'shot t7_fp_sw2', 'wait 1', 'shot t7_fp_sw3', 'quit')


def session_t8():
    """First-person poses for the scythe, a..h."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0.5 101 10.5 270 0', 'wait 200', 'clear @s',
        U + 'summon deathbound:gravebound 3.2 101 10.5 {PersistenceRequired:1b,NoAI:1b,Rotation:[90f,0f]}')
    for k in 'abcdefgh':
        say(f'item replace entity @s weapon.mainhand with deathbound:reaper_scythe[custom_model_data={{strings:["{k}"]}}]', 'wait 25',
            f'shot t8_{k}', 'swing', 'wait 2', f'shot t8_{k}_sw2', 'wait 2', f'shot t8_{k}_sw4', 'wait 15')
    say('quit')


def session_t9():
    """Checks: a wisp's spit marks you (purple hearts), the new soul bolts up close, the King seated on his throne, the Hunter on the ground."""
    say('hud on', 'fov 70', 'gamemode creative', 'view first', U + 'tp @s 0.5 101 10.5 0 0', 'wait 200', 'gamemode survival',
        'effect give @s resistance infinite 4 true', 'effect give @s regeneration infinite 3 true',
        U + 'summon deathbound:soul_wisp 0.5 104 18.5 {PersistenceRequired:1b}', 'look 0 -15', 'wait 60')
    for k in range(6):
        say('wait 10', f'shot t9_wisp_{k}')
    say(U + 'data get entity @s active_effects', 'wait 40', 'shot t9_hearts')
    # the bolts, close, from the side
    say('pan 0 4.5 103.0 14.5 4.5 103.0 14.5 0.5 103.0 15.5', 'wait 20')
    for k in range(6):
        say('wait 6', f'shot t9_bolt_{k}')
    say('camoff', 'kill @e[type=deathbound:soul_wisp]', 'gamemode creative')
    # the King, freed and seated
    say(U + 'tp @s 0 101 -340', 'wait 220', 'deathbound story 2 2', 'kill @e[type=deathbound:death]', 'wait 60', 'effect give @s night_vision infinite 0 true',
        U + 'tp @s 0.5 102 -354 180 0', 'wait 60')
    _still('t9_king_front', (0.5, 106.0, -359.0), (0.5, 105.4, -364.0), 20)
    _still('t9_king_side', (3.8, 105.6, -363.0), (0.5, 105.0, -364.0), 5)
    _still('t9_king_low', (1.6, 104.9, -361.0), (0.5, 105.0, -364.0), 5)
    # the Hunter, on his ground, dark (no night vision)
    say('effect clear @s night_vision', 'deathbound story 1 0', U + 'tp @s -206 94 -126', 'wait 240', 'gamemode survival',
        'effect give @s resistance infinite 4 true', 'wait 160', 'hud off')
    _still('t9_hunter_a', (-200.0, 96.0, -118.0), (-206.0, 93.5, -126.0), 10, tp=False)
    say('wait 60', 'shot t9_hunter_b', 'wait 60', 'shot t9_hunter_c')
    say('quit')


# ---------------------------------------------------------------------------------------------------- the dark re-shoot
# No night vision anywhere: the Underworld as it really is, lit only by what burns in it.
DARK = ('hud off', 'fov 64', 'gamemode creative', 'view first', 'effect clear @s night_vision', 'filmlight off')


def session_tr4a():
    """The town by its lamps, the ferry, Mira in the line, the Sentry, the Watch's signal fire."""
    say(*DARK, U + 'tp @s 90 108 -30', 'wait 280')
    pan('tr4_town', 8, (101.0, 107.0, -21.0), (93.0, 105.0, -28.0), (82.0, 100.0, -44.0), warm=30)
    say(U + 'tp @s 10 104 -8', 'wait 220')
    pan('tr4_ferry', 7, (7.5, 102.4, -5.5), (15.0, 101.3, -7.0), (11.8, 99.6, -11.0), warm=20)
    say(U + 'tp @s 0 104 -215', 'wait 240')
    pan('tr4_mira', 6, (-6.5, 102.0, -221.0), (-4.6, 101.6, -224.2), (-2.5, 101.2, -227.0), warm=20)
    say(U + 'tp @s -136 100 -100', 'wait 240')
    pan('tr4_sentry', 7, (-145.5, 99.8, -94.5), (-144.0, 99.3, -97.0), (-140.5, 98.7, -99.0), warm=20)
    pan('tr4_watch', 6, (-134.5, 122.8, -98.5), (-135.0, 122.6, -101.2), (-136.5, 121.2, -100.2), warm=20)
    say('quit')


def session_tr4b():
    """The Hollow in the dark; the Hunter on his ground, drawing on you; the bow's pack."""
    say(*DARK, U + 'tp @s -200 100 -126', 'wait 260')
    pan('tr4_hollow', 7, (-190.0, 99.0, -112.0), (-196.0, 96.0, -118.0), (-206.0, 93.5, -126.0), warm=20)
    say(U + 'tp @s -210 93 -122 0 0', 'gamemode survival', *TOUGH, 'wait 300')
    take('tr4_hunter', 'shoulder', 5, 'minecraft:player deathbound:hollow_hunter 1.0 2.8 0.8', 'minecraft:player deathbound:hollow_hunter 1.0 2.8 0.8 0.7 2.2 0.6',
         warm=20, events=[(6, U + 'deathbound action 1')], tail=10)
    say('camoff', 'gamemode creative', U + 'tp @s 0.5 101 10.5 180 0', 'wait 200', 'kill @e[type=deathbound:gravebound]', 'gamemode survival', *TOUGH,
        'clear @s', 'give @s deathbound:hunters_bow', 'give @s arrow 32',
        U + 'summon deathbound:gravebound -5 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 0.5 101 -5 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 6 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}', 'wait 10', 'look 180 -2')
    take('tr4_pack', 'pan', 6, '9.0 103.4 7.0 9.0 103.4 7.0 0.5 101.8 1.5', '9.0 103.4 7.0 8.0 103.2 5.5 0.5 101.6 0.0',
         warm=20, events=[(2, 'use 80')], tail=20)
    say('quit')


def session_tr4c():
    """The Death King's first form, with the new bolts: a barrage, a nova. (His last form stays a secret.)"""
    say(*DARK, U + 'tp @s 0 101 -330', 'wait 240', 'deathbound story 1 0', 'deathbound seals 7', 'wait 30', 'gamemode survival', *TOUGH,
        'nodark on', 'filmlight on', 'handheld 1', U + 'tp @s 0.5 101 -338 180 0', 'wait 300', U + 'deathbound phase 2', 'wait 20',
        U + 'tp @e[type=deathbound:death] 0.5 101 -345 0 0', 'wait 20')
    take('tr4_barrage', 'shoulder', 4, 'minecraft:player deathbound:death 1.3 3.2 0.9', 'minecraft:player deathbound:death 1.3 3.2 0.9 1.0 2.6 0.8',
         warm=20, events=[(4, U + 'deathbound action 20')], tail=20)
    say(U + 'tp @e[type=deathbound:death] 0.5 101 -345 0 0', 'wait 30')
    take('tr4_nova', 'shoulder', 4, 'minecraft:player deathbound:death 1.6 3.6 1.2', 'minecraft:player deathbound:death 1.6 3.6 1.2 1.2 3.0 1.0',
         warm=20, events=[(4, U + 'deathbound action 22')], tail=20)
    say('quit')


def session_tr4d():
    """The empty throne, and the choice on it (what each one does stays a secret)."""
    say(*DARK, U + 'tp @s 0 101 -340', 'wait 240', 'deathbound story 2 0', 'kill @e[type=deathbound:death]', 'wait 40',
        U + 'tp @s 0.5 104 -358 180 0', 'wait 60')
    pan('tr4_throne', 7, (0.5, 105.6, -350.5), (0.5, 105.4, -356.5), (0.5, 105.2, -364.0), warm=30)
    say('camoff', 'hud on', 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:heart_of_death', U + 'tp @s 0.5 104 -360 180 10', 'wait 20',
        'rec tr4_choice 30', U + 'deathbound click 0 104 -364', 'wait 30', 'next', 'wait 40', 'next', 'wait 40', 'next', 'wait 70', 'stop', 'close')
    say('quit')


STEADY = 'attribute @s minecraft:knockback_resistance base set 1'


def session_tr5():
    """Re-shoots with fixed cameras: the bow's pack, the Death King's barrage and nova with the new bolts, the throne's
    choice; and a close look at a wisp and its spit."""
    say(*DARK, U + 'tp @s 0.5 101 10.5 180 0', 'wait 220', 'kill @e[type=deathbound:gravebound]', 'gamemode survival', *TOUGH, STEADY,
        'clear @s', 'give @s deathbound:hunters_bow', 'give @s arrow 32',
        U + 'summon deathbound:gravebound -5 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 0.5 101 -5 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 6 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}', 'wait 10', 'look 180 -2', 'view back')
    take('tr5_pack', 'pan', 6, '2.3 102.7 13.4 2.3 102.7 13.4 0.0 101.8 0.0', '2.3 102.7 13.4 2.0 102.6 12.8 0.0 101.6 -1.0',
         warm=20, events=[(2, 'use 80')], tail=20)
    say('camoff', 'view first', 'kill @e[type=deathbound:gravebound]')
    # a wisp and its spit, close
    say(U + 'summon deathbound:soul_wisp 0.5 103.5 4.5 {PersistenceRequired:1b}', 'look 180 -10', 'wait 20')
    take('tr5_wisp', 'pan', 5, '3.6 103.4 8.0 3.6 103.4 8.0 0.5 103.0 5.0', '3.6 103.4 8.0 3.0 103.3 7.6 0.5 103.0 5.5', warm=10, tail=10)
    say('camoff', 'kill @e[type=deathbound:soul_wisp]', 'gamemode creative')
    # the Death King's first form
    say(U + 'tp @s 0 101 -330', 'wait 240', 'deathbound story 1 0', 'deathbound seals 7', 'wait 30', 'gamemode survival', *TOUGH, STEADY,
        'nodark on', 'filmlight on', U + 'tp @s 0.5 101 -337 180 0', 'wait 300', U + 'deathbound phase 2', 'wait 20')
    for name, action in (('tr5_barrage', 20), ('tr5_nova', 22)):
        say(U + 'tp @s 0.5 101 -337 180 0', U + 'tp @e[type=deathbound:death] 0.5 101 -344 0 0', 'wait 10')
        take(name, 'pan', 4, '2.6 103.0 -333.6 2.6 103.0 -333.6 0.5 102.6 -344.0', '2.6 103.0 -333.6 2.2 102.8 -334.4 0.5 102.6 -344.0',
             warm=10, events=[(3, U + 'deathbound action ' + str(action))], tail=20)
    say('camoff', 'nodark off', 'filmlight off', 'gamemode creative')
    # the choice on the throne
    say('deathbound story 2 0', 'kill @e[type=deathbound:death]', 'wait 40', 'clear @s', 'hud on',
        'item replace entity @s weapon.mainhand with deathbound:heart_of_death', U + 'tp @s 0.5 104 -360 180 8', 'wait 30', 'gamemode survival')
    say('rec tr5_choice 30', 'use 3', 'wait 40', 'next', 'wait 50', 'next', 'wait 50', 'next', 'wait 80', 'stop', 'close')
    say('quit')


def session_tr5b():
    """The throne's choice, alone: the Death King already gone (no victory card over it), no HUD (no chat behind it)."""
    say(*DARK, U + 'tp @s 0 101 -340', 'wait 200', 'deathbound story 2 0', 'wait 40', 'kill @e[type=deathbound:death]', 'wait 300', 'clear @s',
        'item replace entity @s weapon.mainhand with deathbound:heart_of_death', U + 'tp @s 0.5 104 -360 180 8', 'wait 40', 'gamemode survival')
    say('rec tr5_choice 30', 'use 3', 'wait 30', 'next', 'wait 25', 'next', 'wait 40', 'next', 'wait 20', 'next', 'wait 60', 'next', 'wait 70', 'stop', 'close')
    say('quit')


def session_tr5c():
    """The Death King's barrage and nova again, now that his bolts are plain balls of fire. His minions cleared before each
    take, the camera back and up so nothing bursts in the lens."""
    say(*DARK, U + 'tp @s 0 101 -330', 'wait 240', 'deathbound story 1 0', 'deathbound seals 7', 'wait 30', 'gamemode survival', *TOUGH, STEADY,
        'nodark on', 'filmlight on', U + 'tp @s 0.5 101 -337 180 0', 'wait 300', U + 'deathbound phase 2', 'wait 20')
    for name, action in (('tr5_barrage', 20), ('tr5_nova', 22)):
        say('kill @e[type=deathbound:gravebound]', 'kill @e[type=deathbound:soul_wisp]', U + 'tp @s 0.5 101 -337 180 0',
            U + 'tp @e[type=deathbound:death] 0.5 101 -345 0 0', 'wait 10')
        # third person, so the player stays where they stand (in first person the harness carries the player along with the
        # film camera, and everything he throws comes down the lens); from the side, both of them in frame
        say('view back')
        cam = '9.0 103.4 -341.0' if action == 20 else '12.0 105.5 -341.0'
        take(name, 'pan', 4, f'{cam} {cam} 0.5 102.0 -341.0', f'{cam} {cam[:-6]}-340.2 0.5 102.0 -341.0',
             warm=10, events=[(3, U + 'deathbound action ' + str(action))], tail=24)
        say('view first')
    say('quit')

def session_t10():
    """A wisp burning in Minecraft's purple fire, spitting it at you: from the side, third person (the player stays put)."""
    say(*DARK, U + 'tp @s 0.5 101 10.5 180 0', 'wait 200', 'gamemode survival', *TOUGH, STEADY, 'view back',
        U + 'summon deathbound:soul_wisp 0.5 103 3.5 {PersistenceRequired:1b}', 'wait 20')
    take('t10_wisp', 'pan', 5, '5.0 103.2 7.0 5.0 103.2 7.0 0.5 102.6 6.5', '5.0 103.2 7.0 4.6 103.1 7.4 0.5 102.6 6.5', warm=10, tail=10)
    say('quit')


def session_tr6():
    """The bosses, still: the Hunter standing on his ground in the dark, staring; the Death King on his throne, close. Slow
    push-ins only; no attacks, nothing turning fast."""
    say(*DARK, U + 'tp @s -200 100 -120', 'wait 240',
        U + 'summon deathbound:hollow_hunter -206 99 -126 {PersistenceRequired:1b,Rotation:[135f,0f]}', 'wait 80')
    pan('tr6_hunter', 6, (-198.0, 95.6, -118.5), (-200.6, 95.3, -121.0), (-206.0, 94.7, -126.0), warm=20)
    say('kill @e[type=deathbound:hollow_hunter]', U + 'tp @s 0 101 -330', 'wait 240', 'deathbound story 1 0', 'deathbound seals 7',
        U + 'tp @s 0.5 101 -350 180 0', 'wait 320')
    pan('tr6_king', 6, (0.5, 107.0, -356.5), (0.5, 106.8, -359.2), (0.5, 106.4, -363.6), warm=20)
    say('quit')


def session_t11():
    """The forge's ribcage, rebuilt of whole bone blocks: from the bank (the user's angle), along its axis, from under it."""
    say(*DARK, 'effect give @s night_vision infinite 0 true', U + 'tp @s 40 104 90', 'wait 260')
    _still('t11_ribs_bank', (44.5, 101.6, 81.5), (51.0, 101.0, 74.5), 20)
    _still('t11_ribs_axis', (58.5, 101.4, 75.5), (48.0, 102.0, 75.5), 5)
    _still('t11_ribs_side', (50.0, 101.0, 84.5), (50.0, 102.5, 75.5), 5)
    _still('t11_ribs_under', (50.5, 99.7, 75.5), (49.0, 105.0, 72.0), 5)
    say('effect clear @s night_vision', 'wait 5')
    _still('t11_ribs_dark', (44.5, 101.6, 81.5), (51.0, 101.0, 74.5), 10)
    say('quit')


def session_tr7():
    """Re-shoots of the Mere now that the forge's ribs are whole blocks: the Mere from the bank, the water, a slow push down
    the ribcage. Same look as tr3a; no turning cameras."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 40 104 90', 'wait 260')
    _still('t7_check', (44.5, 101.6, 81.5), (51.0, 101.0, 74.5), 20)
    pan('tr7_mere', 6, (27.0, 100.8, 87.0), (30.0, 100.4, 84.5), (42.0, 98.0, 74.0), warm=20)
    pan('tr7_water', 6, (31.0, 98.9, 80.0), (34.5, 98.7, 79.0), (36.0, 97.2, 74.0), warm=20)
    pan('tr7_ribs', 7, (61.0, 101.8, 75.5), (57.0, 101.5, 75.5), (47.0, 102.2, 75.5), warm=20)
    say('quit')


def session_tr8():
    """Re-shoots now the soul fire is purple everywhere (no cyan): Clatter's quench, Aldous's forge, the bow's pack."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 40 104 90', 'wait 260')
    say(U + 'tp @s 44.0 98 75.5 90 15', 'wait 30', 'gamemode survival', *TOUGH,
        'clear @s', 'give @s netherite_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('tr3_quench', 'pan', 6, '41.6 99.5 79.8 41.6 99.5 79.8 41.0 98.4 75.5', '41.6 99.5 79.8 40.8 99.7 79.4 41.0 98.6 75.5',
         warm=20, events=[(4, 'deathbound temper 1')], tail=10)
    say('gamemode creative', U + 'tp @s 88 100 -44', 'wait 240')
    say(U + 'tp @s 78.0 100 -35.5 90 10', 'wait 20', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s armor.chest with minecraft:netherite_chestplate', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('tr3_forge', 'pan', 7, '78.6 101.9 -33.4 78.6 101.9 -33.4 74.8 100.8 -36.6', '78.6 101.9 -33.4 78.2 101.6 -33.9 75.2 100.8 -36.4',
         warm=20, events=[(4, 'deathbound forge chest')], tail=6)
    say('gamemode creative', 'effect clear @s night_vision', 'filmlight off', U + 'tp @s 0.5 101 10.5 180 0', 'wait 220',
        'kill @e[type=deathbound:gravebound]', 'kill @e[type=deathbound:soul_wisp]', 'gamemode survival', *TOUGH, STEADY,
        'clear @s', 'give @s deathbound:hunters_bow', 'give @s arrow 32',
        U + 'summon deathbound:gravebound -5 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 0.5 101 -5 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}',
        U + 'summon deathbound:gravebound 6 101 -3 {PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}', 'wait 10', 'look 180 -2', 'view back')
    take('tr5_pack', 'pan', 6, '2.3 102.7 13.4 2.3 102.7 13.4 0.0 101.8 0.0', '2.3 102.7 13.4 2.0 102.6 12.8 0.0 101.6 -1.0',
         warm=20, events=[(2, 'use 80')], tail=20)
    say('quit')


def session_tr8b():
    """The quench once more, its flare purple now (no white sparks)."""
    say('hud off', 'fov 66', 'gamemode creative', 'view first', 'filmlight on', U + 'tp @s 40 104 90', 'wait 260')
    say(U + 'tp @s 44.0 98 75.5 90 15', 'wait 30', 'gamemode survival', *TOUGH,
        'clear @s', 'give @s netherite_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('tr3_quench', 'pan', 6, '41.6 99.5 79.8 41.6 99.5 79.8 41.0 98.4 75.5', '41.6 99.5 79.8 40.8 99.7 79.4 41.0 98.6 75.5',
         warm=20, events=[(4, 'deathbound temper 1')], tail=10)
    say('quit')


def session_scout():
    """Trailer 4 scouting: every island from its four sides (night vision on, just to see the layout), for picking angles."""
    import math
    say('hud off', 'fov 70', 'gamemode creative', 'view first', 'effect give @s night_vision infinite 0 true', 'filmlight on')
    for name, x, z, y in ISLAND_CENTERS:
        say(U + f'tp @s {x} {y + 20} {z}', 'wait 220')
        for k, deg in enumerate((0, 90, 180, 270)):
            a = math.radians(deg)
            at = (round(x + 34 * math.sin(a), 1), y + 14, round(z + 34 * math.cos(a), 1))
            _still(f'sc_{name}_{deg}', at, (x, y + 2, z), 25 if k == 0 else 15)
    say('quit')


# ------------------------------------------------------------------ trailer 4: the same cut as trailer 3, every shot new
# Film t9a..t9i then t9k, in that order (later sessions refilm some of the earlier takes under the same names), then
# `python tools/trailer/trailer4.py`.
# Default brightness (not Moody, not Bright), fog on, no night vision. Subjects are lit like a film set: an invisible
# minecraft:light (key) near them, so they sit in a pool of light and everything around stays dark. Slow pushes, cranes and
# tracks only: nothing orbits, nothing turns fast. Mobs that must stand still are summoned high, let fall, then frozen
# (NoAI mobs don't fall: summoned at the island's nominal top they end up buried in uneven ground).
T9 = ('hud off', 'fov 62', 'gamemode creative', 'view first', 'effect clear @s night_vision', 'filmlight off', 'nodark on')


def key(x, y, z, level=12):
    """An invisible light (a film set's key light): lights the subject, leaves the dark around it alone."""
    return U + f'setblock {x} {y} {z} minecraft:light[level={level}]'


def freeze(kind):
    """The nearest `kind` stops where it landed."""
    return f'data merge entity @e[type={kind},limit=1,sort=nearest] {{NoAI:1b}}'


def session_t9a():
    """The living world: a figure in a storm-dark clearing, lightning ahead; then the rite, from the front."""
    say('hud off', 'fov 60', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s weapon.offhand with deathbound:deathbound_relic',
        'execute in minecraft:overworld run spreadplayers -232 -290 0 1 false @s', 'wait 40',
        'fill ~-14 ~ ~-14 ~14 ~22 ~14 air', 'kill @e[type=item]',
        'time set 18000', 'weather thunder', 'view back', 'look 0 -4', 'wait 200')
    follow('t9_storm', 5.5, 'minecraft:player', 3.0, 0.35, 165, 172, 1.7, events=[(26, 'summon lightning_bolt ~-3 ~ ~16')])
    follow('t9_rite', 7.5, 'minecraft:player', 3.4, 0.5, -12, -4, 1.65,
           events=[(30, 'summon lightning_bolt ~5 ~ ~-12'), (40, 'deathbound descend')])
    say('quit')


def session_t9b():
    """Below: waking among the pillars, the Ferryman, the line of the dead, one of them close, the Spire."""
    say(*T9, U + 'tp @s 0 110 0', 'wait 240', 'gamemode survival', *TOUGH, STEADY, 'clear @s',
        'item replace entity @s weapon.offhand with deathbound:deathbound_relic',
        key(1, 102, 2, 11), key(2, 102, 7, 9), U + 'tp @s 0.5 101 7.5 180 0', 'view back', 'wait 20')
    pan('t9_arrival', 7, (3.5, 109.0, 15.5), (2.2, 103.0, 10.6), (0.5, 102.3, 3.0), warm=30, events=[(30, 'hold forward 60')])
    say('gamemode creative', 'view first', key(9, 103, -9, 13), key(8, 104, -11, 8))
    pan('t9_ferryman', 6, (10.8, 102.3, -9.4), (8.9, 102.5, -9.25), (6.5, 102.85, -9.0), warm=20)
    # the line: souls walking away from us toward the Gate, on the Hub's path
    say(U + 'tp @s 0 104 -130', 'wait 320')
    pan('t9_line', 8.5, (3.2, 99.5, -126.0), (2.6, 99.7, -133.0), (0.0, 100.4, -175.0), warm=20)
    say('follow 0 deathbound:lost_soul 2.3 1.25 172 172 1.35', 'wait 30', 'rec t9_soul 30', 'follow 6 deathbound:lost_soul 2.0 1.3 172 180 1.35',
        'wait 124', 'stop')
    say(U + 'tp @s 90 104 -136', 'wait 240')
    pan('t9_spire', 6, (86.0, 101.6, -136.0), (87.0, 104.6, -137.5), (96.0, 131.0, -152.0), warm=20)
    say('quit')


def session_t9c():
    """Its people: the Collector among his shelves, the Lamplighter, Aldous at the forge, the Prophet in the crypt."""
    say(*T9, U + 'tp @s 96 110 -150', 'wait 240', key(96, 92, -153, 12))
    pan('t9_collector', 6, (96.5, 91.3, -155.6), (96.5, 91.4, -153.9), (96.5, 91.7, -150.5), warm=20)
    say(U + 'tp @s 85 104 -44', 'wait 240', key(87, 102, -48, 12), key(89, 101, -49, 7))
    pan('t9_lamplighter', 6, (87.6, 101.0, -45.2), (87.5, 101.2, -46.8), (87.5, 101.8, -50.0), warm=20)
    say(key(77, 102, -35, 11), U + 'tp @s 78.0 100 -35.5 90 10', 'wait 20', 'gamemode survival', *TOUGH, 'clear @s',
        'item replace entity @s armor.chest with minecraft:netherite_chestplate', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('t9_forge', 'pan', 7, '77.6 101.2 -33.9 77.6 101.2 -33.9 75.0 101.0 -36.4', '77.6 101.2 -33.9 77.1 101.2 -34.5 75.0 101.0 -36.4',
         warm=20, events=[(4, 'deathbound forge chest')], tail=6)
    say('gamemode creative', U + 'tp @s -54 95 -168', 'wait 240', key(-54, 90, -169, 12))
    pan('t9_prophet', 6, (-54.0, 88.9, -167.6), (-54.0, 89.1, -168.8), (-54.0, 90.4, -172.0), warm=20)
    say('quit')


def session_t9d():
    """The Mere and Clatter's quench; the Western Watch and its Sentry; a Soul Wisp in the Ghostwood."""
    say(*T9, U + 'tp @s 40 104 90', 'wait 260')
    pan('t9_mere', 6, (27.0, 99.6, 87.0), (30.0, 99.3, 84.5), (42.0, 98.4, 74.0), warm=20)
    pan('t9_water', 6, (31.0, 98.7, 80.0), (34.0, 98.6, 79.2), (36.0, 97.3, 74.0), warm=20)
    pan('t9_ribs', 7, (61.0, 101.4, 75.5), (57.0, 101.2, 75.5), (47.0, 102.0, 75.5), warm=20)
    say(key(41, 100, 77, 12), U + 'tp @s 44.0 98 75.5 90 15', 'wait 30', 'gamemode survival', *TOUGH,
        'clear @s', 'give @s netherite_sword', 'give @s deathbound:grave_rune 6', 'give @s deathbound:soul 64', 'wait 10')
    take('t9_quench', 'pan', 6.5, '41.6 99.5 79.8 41.6 99.5 79.8 41.0 98.4 75.5', '41.6 99.5 79.8 40.9 99.6 79.2 41.0 98.6 75.5',
         warm=20, events=[(4, 'deathbound temper 1')], tail=10)
    say('gamemode creative', U + 'tp @s -136 104 -96', 'wait 240', key(-143, 99, -99, 12))
    pan('t9_sentry', 6, (-145.8, 98.2, -99.5), (-143.9, 98.4, -99.2), (-140.8, 98.85, -98.9), warm=20)
    pan('t9_watch', 6, (-118.0, 97.4, -95.0), (-121.0, 98.6, -96.0), (-136.0, 116.0, -100.0), warm=20)
    say(U + 'tp @s -76 110 -36', 'wait 240', U + 'summon deathbound:soul_wisp -76 106 -36 {PersistenceRequired:1b,NoAI:1b}', 'wait 20')
    pan('t9_wisp', 5, (-70.6, 105.2, -30.6), (-72.8, 105.6, -32.8), (-76.0, 106.4, -36.0), warm=20)
    say('quit')


def session_t9e():
    """The hunt: the dead clawing out between the gravestones; the Hollow; the Hunter standing in the dark; the scythe."""
    say(*T9, U + 'tp @s 0 106 -222', 'wait 260', 'kill @e[type=deathbound:gravebound]',
        U + 'summon deathbound:gravebound 3.5 101 -229 {PersistenceRequired:1b,NoAI:1b,Rotation:[160f,0f]}', key(2, 103, -226, 11), 'wait 10')
    pan('t9_rise', 6, (1.2, 101.3, -222.0), (1.6, 101.4, -223.4), (3.5, 102.4, -229.0), warm=10, events=[(4, U + 'deathbound rise')])
    say('kill @e[type=deathbound:gravebound]',
        *[U + f'summon deathbound:gravebound {x} 101 {z} {{PersistenceRequired:1b,NoAI:1b,Rotation:[180f,0f]}}'
          for x, z in ((-4.5, -232), (4.0, -236), (-2.0, -240), (6.5, -230), (-7.0, -238))],
        key(0, 103, -234, 9), 'wait 10')
    pan('t9_risefar', 7, (0.5, 101.4, -215.0), (0.5, 101.7, -218.0), (0.0, 102.4, -238.0), warm=10, events=[(4, U + 'deathbound rise')])
    say('kill @e[type=deathbound:gravebound]', U + 'tp @s -196 106 -116', 'wait 240')
    pan('t9_hollow', 7, (-194.0, 94.6, -110.0), (-190.0, 94.6, -115.0), (-206.0, 94.4, -126.0), warm=20)
    say(U + 'summon deathbound:hollow_hunter -206 99 -126 {PersistenceRequired:1b,Rotation:[135f,0f]}', 'wait 60',
        freeze('deathbound:hollow_hunter'), key(-203, 96, -123, 11), 'wait 10')
    pan('t9_hunter', 7, (-198.8, 94.2, -118.6), (-201.4, 94.4, -121.2), (-206.0, 95.6, -126.0), warm=20)
    say('kill @e[type=deathbound:hollow_hunter]', U + 'tp @s 0.5 106 10.5', 'wait 220', 'kill @e[type=deathbound:gravebound]',
        'gamemode survival', *TOUGH, STEADY, 'clear @s', 'item replace entity @s weapon.mainhand with deathbound:reaper_scythe',
        U + 'tp @s 0.5 101 10.5 180 0',
        *[U + f'summon deathbound:gravebound {x} 101 {z} {{PersistenceRequired:1b,NoAI:1b,Rotation:[0f,0f]}}'
          for x, z in ((0.5, 8.0), (-1.6, 8.6), (2.6, 8.6))],
        key(0, 103, 9, 12), 'wait 10', 'view back', 'aim deathbound:gravebound')
    take('t9_reap', 'pan', 6, '2.6 102.5 13.2 2.6 102.5 13.2 0.3 101.9 7.6', '2.6 102.5 13.2 2.2 102.4 12.6 0.3 101.8 7.6',
         warm=20, events=[(4, 'click'), (27, 'click'), (50, 'click')], tail=20)
    say('aim off', 'quit')


def session_t9f():
    """The Warden at his gate; the Death King on his throne, from the hall and close; the throne, empty."""
    say(*T9, U + 'tp @s 0 106 -224', 'wait 280', key(2, 103, -244, 10), key(-2, 103, -244, 10))
    pan('t9_gate', 9, (0.5, 101.8, -223.5), (0.5, 102.4, -236.0), (0.0, 104.2, -247.0), warm=20)
    say(U + 'tp @s 0 101 -330', 'wait 240', 'deathbound story 1 0', 'deathbound seals 7',
        U + 'tp @s 0.5 101 -340 180 0', 'wait 320', key(0, 105, -360, 14), key(3, 106, -362, 9), key(-3, 106, -362, 9))
    pan('t9_hall', 8, (0.5, 102.8, -336.0), (0.5, 103.4, -347.0), (0.5, 105.6, -364.0), warm=20)
    pan('t9_king', 7, (0.5, 104.4, -357.2), (0.5, 104.8, -359.6), (0.5, 106.2, -364.0), warm=20)
    say('kill @e[type=deathbound:death]', 'wait 40')
    pan('t9_throne', 8, (0.5, 104.4, -351.0), (0.5, 104.8, -357.5), (0.5, 105.4, -364.0), warm=20)
    say('quit')

def session_t9g():
    """Trailer 4 reshoots: the Collector inside his vault, the Lamplighter, the market, the line of the dead (with its
    dead), a Gravebound climbing out close, the Hunter clear of the trees."""
    say(*T9, U + 'tp @s 96 110 -150', 'wait 240', key(97, 92, -150, 12))
    pan('t9_collector', 6, (98.6, 91.3, -149.6), (98.2, 91.4, -150.3), (96.0, 91.4, -152.4), warm=20)
    say(U + 'tp @s 85 104 -44', 'wait 240', key(87, 102, -48, 12), key(89, 101, -49, 7))
    pan('t9_lamplighter', 6, (87.5, 101.2, -46.4), (87.5, 101.3, -47.3), (87.5, 101.8, -50.0), warm=20)
    say(key(91, 103, -53, 11))
    pan('t9_market', 6, (95.0, 102.0, -50.0), (92.0, 102.0, -50.0), (90.5, 100.5, -56.0), warm=20)
    # the line: the dead walking away from us toward the Gate
    say(U + 'tp @s 0 104 -130', 'wait 300',
        *[U + f'summon deathbound:lost_soul {x} 99 {z} {{PersistenceRequired:1b,Rotation:[180f,0f]}}'
          for x, z in ((0.5, -131), (-0.5, -135.5), (1.0, -140), (0.0, -145), (1.2, -150), (-0.6, -155), (0.6, -161))], 'wait 20')
    pan('t9_line', 8.5, (2.6, 100.2, -126.5), (2.0, 100.3, -132.0), (0.0, 100.6, -175.0), warm=20)
    say(U + 'tp @s -136 104 -96', 'wait 240')
    pan('t9_watch', 6, (-134.5, 122.8, -98.5), (-135.0, 122.6, -101.2), (-136.5, 121.2, -100.2), warm=20)
    # a Gravebound climbs out between the gravestones, close
    say(U + 'tp @s 0 106 -222', 'wait 260', 'kill @e[type=deathbound:gravebound]',
        U + 'summon deathbound:gravebound 3.5 101 -229 {PersistenceRequired:1b,NoAI:1b,Rotation:[160f,0f]}', key(3, 103, -227, 12), 'wait 10')
    pan('t9_rise', 6, (2.0, 101.5, -225.4), (2.3, 101.6, -226.2), (3.5, 101.9, -229.0), warm=10, events=[(4, U + 'deathbound rise')])
    say('kill @e[type=deathbound:gravebound]', U + 'tp @s -200 100 -120', 'wait 240',
        U + 'summon deathbound:hollow_hunter -206 99 -126 {PersistenceRequired:1b,Rotation:[135f,0f]}', 'wait 60',
        freeze('deathbound:hollow_hunter'), key(-203, 96, -123, 12), 'wait 10')
    pan('t9_hunter', 7, (-199.8, 94.3, -119.6), (-201.8, 94.5, -121.6), (-206.0, 95.6, -126.0), warm=20)
    say('kill @e[type=deathbound:hollow_hunter]')
    say('quit')

def session_t9h():
    """Trailer 4: the throne, empty. Film it before the King ever wakes in this world: killing him ends the story and
    sends the player home."""
    say(*T9, U + 'tp @s 0 101 -330', 'wait 300', key(0, 105, -360, 12), key(3, 106, -362, 8), key(-3, 106, -362, 8), 'wait 20')
    pan('t9_throne', 8, (0.5, 104.4, -351.0), (0.5, 104.8, -357.5), (0.5, 105.4, -364.0), warm=20)
    say('quit')

def session_t9i():
    """The Western Watch's Sentry at his post. The film camera keeps the player (and so the Director's attention) wherever
    it last was: `camoff` and stand there first, or he's never put at his post."""
    say(*T9, 'camoff', U + 'tp @s -136 104 -96', 'wait 300', key(-143, 99, -99, 12), 'wait 10')
    pan('t9_sentry', 6.5, (-146.2, 99.4, -99.3), (-144.6, 99.2, -99.15), (-141.0, 98.7, -99.0), warm=20)
    say('quit')

def session_t9k():
    """Trailer 4: the Western Watch's brazier again, a slow push in this time (the first swung round it)."""
    say(*T9, U + 'tp @s -136 126 -96', 'wait 260')
    pan('t9_watch', 6, (-133.9, 123.0, -97.9), (-134.5, 122.8, -98.8), (-136.5, 121.3, -100.2), warm=20)
    say('quit')

def session_t9m():
    """Trailer 4, the weak shots again: the Hunter facing us with a light on him (the first take showed his back), a Lost
    Soul up close with the camera held over the middle of the path (off the lantern posts), the Mere's water lit, and
    the Watch's brazier framed clear of the table in front of it."""
    at_hunter = U + 'execute at @e[type=deathbound:hollow_hunter,limit=1,sort=nearest] run '
    say(*T9, U + 'tp @s -200 100 -120', 'wait 240',
        U + 'summon deathbound:hollow_hunter -206 99 -126 {PersistenceRequired:1b,Rotation:[-45f,0f]}', 'wait 60',
        freeze('deathbound:hollow_hunter'), 'data merge entity @e[type=deathbound:hollow_hunter,limit=1,sort=nearest] {Rotation:[-45f,0f]}',
        at_hunter + 'setblock ~1 ~2 ~1 minecraft:light[level=15]', at_hunter + 'setblock ~-1 ~3 ~-1 minecraft:light[level=9]', 'wait 10')
    pan('t9_hunter', 7, (-199.8, 94.3, -119.6), (-201.6, 94.5, -121.4), (-206.0, 95.4, -126.0), warm=20)
    say('kill @e[type=deathbound:hollow_hunter]', U + 'tp @s 0 104 -128', 'wait 280',
        U + 'kill @e[type=deathbound:lost_soul,distance=..40]',
        U + 'summon deathbound:lost_soul 0.5 99 -133 {PersistenceRequired:1b,Rotation:[180f,0f]}', 'wait 5')
    say('follow 0 deathbound:lost_soul 2.1 1.45 180 180 1.3', 'wait 20', 'rec t9_soul 30', 'follow 6 deathbound:lost_soul 2.0 1.5 180 184 1.3',
        'wait 124', 'stop')
    say(U + 'tp @s 40 104 90', 'wait 260', key(35, 99, 77, 11), key(38, 99, 75, 9), key(33, 99, 74, 8), 'wait 10')
    pan('t9_water', 6, (31.0, 98.9, 80.0), (34.0, 98.8, 79.2), (36.0, 97.4, 74.0), warm=20)
    say(U + 'tp @s -136 126 -96', 'wait 240')
    pan('t9_watch', 6, (-134.25, 122.9, -98.4), (-134.75, 122.7, -99.2), (-136.5, 121.3, -100.2), warm=20)
    say('quit')

if __name__ == '__main__':
    {'a': session_a, 'r': session_r, 'b': session_b, 'b2': session_b2, 'c': session_c, 'w': session_w, 'f': session_f, 'g': session_g, 's': session_s, 't': session_t, 'd': session_d, 'd2': session_d2, 'v': session_v, 'p': session_p, 'q': session_q, 'r': session_r, 'u': session_u, 'bk': session_bk, 'e': session_e, 'ev': session_ev, 'ev_take': lambda: session_ev(('take',), False), 'ev_intro': lambda: session_ev(('none',), True), 'npcv': session_npcv, 'npcv2': session_npcv2, 'forge': session_forge, 'aldous': session_aldous, 'e2': session_e2, 'fx': session_fx, 'mere': session_mere, 'folk': session_folk, 'town': session_town, 'ev2': session_ev2, 'tr2': session_tr2, 'tr2b': session_tr2b, 'tr2c': session_tr2c, 'tr2d': session_tr2d, 'dbg_run': session_dbg_run, 'ev2_break': lambda: session_ev2(('break',)), 'e2_break': lambda: session_e2(('break',)), 't3a': session_t3a, 't3b': session_t3b, 't3c': session_t3c, 't3d': session_t3d, 'tr3a': session_tr3a, 'tr3b': session_tr3b, 'tr3c': session_tr3c, 't4': session_t4, 't5': session_t5, 't6': session_t6, 't7': session_t7, 't8': session_t8, 't9': session_t9, 'tr4a': session_tr4a, 'tr4b': session_tr4b, 'tr4c': session_tr4c, 'tr4d': session_tr4d, 'tr5': session_tr5, 'tr5b': session_tr5b, 'tr5c': session_tr5c, 't10': session_t10, 'tr6': session_tr6, 't11': session_t11, 'tr7': session_tr7, 'tr8': session_tr8, 'tr8b': session_tr8b, 'scout': session_scout, 't9a': session_t9a, 't9b': session_t9b, 't9c': session_t9c, 't9d': session_t9d, 't9e': session_t9e, 't9f': session_t9f, 't9g': session_t9g, 't9h': session_t9h, 't9i': session_t9i, 't9k': session_t9k, 't9m': session_t9m}[sys.argv[1]]()
    out = Path(__file__).resolve().parents[2] / 'run' / 'test-script.txt'
    out.write_text('\n'.join(L) + '\n')
    print(f'{len(L)} lines -> {out}')
