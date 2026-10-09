"""Trailer 4: trailer 3's cut, cards and score, every shot refilmed (shots.py t9a..t9i). Default brightness, no night
vision, subjects lit by hidden key lights; the scythe as it swings now; the Sentry with a Shade drifting in.

    python tools/trailer/trailer4.py   -> build/trailer4/deathbound_trailer_full.mp4

Footage: run/trailer/t9_*.mp4 (copied to run/trailer_keep first).
"""
import functools
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import score  # noqa: E402
import trailer3  # noqa: E402
from trailer3 import NIGHT_RAW, TITLE, LENGTH  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
R = {'grade': NIGHT_RAW}   # all of it shot without night vision

EDL = [
    (0.0, 'black'),
    (1.0, 'card', ['MOST WHO DIE', 'WAKE UP IN BED.']),
    (4.5, 'clip', 't9_storm', 0.1, {'fadein': 0.4, **R}),   # its lightning lands on the score's thunder (5.77)
    (6.9, 'clip', 't9_rite', 0.8, R),
    (10.0, 'card', ['SOME WAKE UP BELOW.']),
    # the Underworld
    (12.5, 'clip', 't9_arrival', 0.6, {'fadein': 0.4, **R}),
    (16.25, 'clip', 't9_ferryman', 1.4, R),
    (18.75, 'clip', 't9_line', 1.0, R),
    (21.25, 'clip', 't9_market', 0.8, R),
    (23.75, 'clip', 't9_lamplighter', 1.5, R),
    (26.25, 'clip', 't9_forge', 3.2, R),
    (28.75, 'clip', 't9_collector', 0.2, R),
    (31.25, 'clip', 't9_prophet', 1.0, R),
    (33.75, 'clip', 't9_soul', 3.15, {'fadeout': 0.4, **R}),   # after the follow camera clears a lantern post
    (36.75, 'black'),
    # its people
    (37.5, 'card', ['THE DEAD', 'STILL KEEP HOUSE.']),
    (40.0, 'clip', 't9_mere', 1.0, {'fadein': 0.3, **R}),
    (42.5, 'clip', 't9_water', 1.0, R),
    (43.75, 'clip', 't9_quench', 0.6, R),
    (48.75, 'clip', 't9_ribs', 1.5, R),
    (51.25, 'clip', 't9_sentry', 1.6, R),   # the Sentry at his post, and a Shade drifting in
    (55.0, 'clip', 't9_watch', 3.0, R),   # the Watch's brazier, a slow push in
    (58.125, 'clip', 't9_wisp', 1.0, {'fadeout': 0.3, **R}),
    # the hunt
    (60.0, 'card', ['AND DEATH', 'STOPPED LETTING GO.']),
    (62.5, 'clip', 't9_rise', 0.45, {'flash': True, **R}),   # he's under the ground from here and climbs out
    (65.0, 'clip', 't9_hollow', 0.8, R),
    (68.75, 'clip', 't9_hunter', 1.0, R),
    (73.75, 'clip', 't9_reap', 0.5, {'fadeout': 0.3, **R}),   # the scythe's three cuts, the slam last
    # the Warden at his gate
    (80.0, 'clip', 't9_gate', 0.3, {'flash': True, **R}),
    # the Death King on his throne (none of his fight)
    (87.5, 'card', ['FACE WHAT', 'DEATH BECAME.']),
    (89.375, 'clip', 't9_hall', 0.0, {'say': ('Death', "So the king's chain found a new neck."), **R}),
    (96.25, 'clip', 't9_king', 0.5, {'fadeout': 0.6, **R}),
    (101.875, 'black'),
    (104.375, 'card', ['WHAT HE BECOMES NEXT,', 'YOU WILL HAVE TO SEE.']),
    (109.3, 'black'),
    # the choice: the empty throne, and nothing of what it leads to
    (110.0, 'card', ['THEN CHOOSE', 'WHAT IT ALL BECOMES.']),
    (112.5, 'clip', 't9_throne', 0.1, R),
    (119.375, 'clip', 't9_line', 1.2, {'fadeout': 0.8, **R}),
    (126.25, 'black'),
    (127.5, 'black'),
    (TITLE, 'title', 't9_water'),
    (136.25, 'last', 'Three endings. One throne. The dead are waiting.'),
    (LENGTH, 'end'),
]


def main(only=None):
    keep = ROOT / 'run' / 'trailer_keep'
    keep.mkdir(parents=True, exist_ok=True)
    for src in (ROOT / 'run' / 'trailer').glob('t9_*.mp4'):
        shutil.copy(src, keep / src.name)
    trailer3.EDL = EDL
    trailer3.check_lengths()
    edit.EDL = EDL
    edit.GRADE = NIGHT_RAW
    import text
    if not isinstance(text.chat, functools.partial):
        text.chat = functools.partial(text.chat, y=850)
    edit.OUT = ROOT / 'build' / 'trailer4'
    score.compose = trailer3.compose
    (edit.OUT / 'seg').mkdir(parents=True, exist_ok=True)
    shutil.copy(ROOT / 'build' / 'trailer' / 'ascii.png', edit.OUT / 'ascii.png')
    edit.main(only)
    final = edit.OUT / 'deathbound_trailer_full.mp4'
    trailer3.finish(edit.OUT / 'deathbound_trailer.mp4', final)
    print('wrote', final)


if __name__ == '__main__':
    main({int(a) for a in sys.argv[1:]} or None)
