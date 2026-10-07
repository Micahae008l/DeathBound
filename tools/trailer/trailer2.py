"""The update trailer: the people and places of the Underworld (the Mere and Clatter, the bigger town and its children,
the Lamplighter, Mira, the Sentry, the ferry, the wisp, the journal), the Hunter's ground, then the bosses and the
three reworked endings. Same score and cutting as edit.py (the score's bar grid is unchanged).

    python tools/trailer/trailer2.py   -> build/trailer2/deathbound_trailer_2.mp4

New footage comes from `shots.py tr2` and `shots.py ev2` (run/trailer); it's copied into run/trailer_keep first.
"""
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import score  # noqa: E402
import text  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
NEW = ['tr_mere_wide', 'tr_soulwater', 'tr_quench', 'tr_ribcage', 'tr_town', 'tr_kids', 'tr_lamplighter', 'tr_mira', 'tr_ferry', 'tr_journal',
       'tr_wisp', 'tr_sentry', 'tr_hollow', 'tr_marked', 'tr_ghosts', 'npc_hunter', 'ev2_take', 'ev2_king', 'ev2_break']
G, C = edit.GRADE, edit.COLD

EDL = [
    (0.0, 'black'),
    (1.0, 'card', ['THE UNDERWORLD', 'IS BIGGER NOW.']),
    (4.5, 'clip', 'tr_mere_wide', 0.8, {'grade': C, 'fadein': 0.4}),
    (7.5, 'clip', 'tr_soulwater', 0.8, {}),
    (10.0, 'card', ['THE DEAD', 'STILL KEEP HOUSE.']),
    (12.5, 'clip', 'tr_quench', 0.8, {'fadein': 0.3}),
    (17.5, 'clip', 'tr_ribcage', 1.0, {}),
    (20.0, 'clip', 'tr_town', 1.6, {'grade': C}),
    (23.75, 'clip', 'tr_kids', 1.2, {}),
    (27.5, 'clip', 'tr_lamplighter', 1.5, {}),
    (30.0, 'clip', 'tr_mira', 1.2, {}),
    (32.5, 'clip', 'tr_ferry', 1.4, {'grade': C}),
    (35.0, 'clip', 'tr_wisp', 1.6, {}),
    (36.875, 'clip', 'tr_journal', 0.6, {'fadeout': 0.3}),
    (37.5, 'card', ['AND SOME', 'STILL HUNT.']),
    (40.0, 'clip', 'tr_sentry', 1.2, {'grade': C, 'fadein': 0.3}),
    (42.5, 'clip', 'tr_hollow', 1.4, {}),
    (45.0, 'clip', 'npc_hunter', 1.2, {}),
    (47.5, 'clip', 'tr_marked', 3.4, {}),
    (48.75, 'clip', 'tr_ghosts', 1.0, {}),
    (50.0, 'clip', 'b5_gate', 5.3, {'flash': True, 'say': ('The Warden', 'The living do not pass.')}),
    (52.5, 'clip', 'f2_judge', 0.9, {'say': ('The Warden', 'I judge you!'), 'hits': [1.87]}),
    (54.375, 'clip', 'g4_transform', 2.4, {'say': ('Death', 'Then see what the king made of me!')}),
    (56.25, 'clip', 'g6_claw', 2.45, {'flash': True, 'hits': [3.07, 3.27]}),
    (57.5, 'card', ['THEN CHOOSE', 'WHAT IT ALL BECOMES.']),
    (60.0, 'clip', 'ev2_take', 15.6, {'fadein': 0.25}),
    (62.5, 'clip', 'ev2_take', 11.0, {}),
    (65.0, 'clip', 'ev2_king', 9.6, {}),
    (67.5, 'clip', 'ev2_king', 29.8, {}),
    (70.0, 'clip', 'ev2_break', 17.2, {'flash': True}),
    (72.5, 'clip', 'ev2_break', 71.0, {'fadeout': 0.4}),
    (75.0, 'black'),
    (edit.TITLE, 'title', 'tr_mere_wide'),
    (83.75, 'last', 'Three endings. One throne. The dead are waiting.'),
    (88.0, 'end'),
]


def main():
    keep = ROOT / 'run' / 'trailer_keep'
    for n in NEW:
        src = ROOT / 'run' / 'trailer' / f'{n}.mp4'
        if src.exists():
            shutil.copy(src, keep / f'{n}.mp4')
    edit.EDL = EDL
    edit.OUT = ROOT / 'build' / 'trailer2'
    (edit.OUT / 'seg').mkdir(parents=True, exist_ok=True)
    shutil.copy(ROOT / 'build' / 'trailer' / 'ascii.png', edit.OUT / 'ascii.png')
    edit.main()
    out = edit.OUT / 'deathbound_trailer.mp4'
    final = edit.OUT / 'deathbound_trailer_2.mp4'
    shutil.move(out, final)
    print('wrote', final)


if __name__ == '__main__':
    main()
