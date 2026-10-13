"""Trailer 5: trailer 4's cut refilmed with the Mellow shader pack (Iris), tuned cold for the Underworld.

    python tools/trailer/trailer5.py   -> build/trailer5/deathbound_trailer_full.mp4

Filming: Iris + Sodium in run/mods, `Mellow v3.4.1a.zip` in run/shaderpacks with the settings in
`run/shaderpacks/Mellow v3.4.1a.zip.txt` (torch light turned cold violet, low floor light, no colored lights). Then the
shots.py sessions t9a..t9i, t9k, t9m, t9n, t9o, in that order. Mellow tone-maps and blooms by itself, so the grade
here only lifts and cools; it doesn't add trailer 4's bloom on top.
"""
import functools
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import score  # noqa: E402
import trailer3  # noqa: E402
import trailer4  # noqa: E402
from trailer3 import LENGTH  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
# Mellow's frames come out already tone-mapped: lift the mids a touch, keep the blacks black, a hint of vignette and grain
MELLOW = "eq=gamma=1.4:contrast=1.06:saturation=1.08,curves=all='0/0 0.05/0.03 0.4/0.42 1/1',vignette=angle=0.75,noise=alls=3:allf=t"
R = {'grade': MELLOW}

EDL = [(t, k, *[dict(a, grade=MELLOW) if isinstance(a, dict) and 'grade' in a else a for a in args]) for t, k, *args in trailer4.EDL]
# changes from trailer 4's cut: no shot used twice (the line at 0:19 and the closing shot were the same seconds of one clip)
_SWAP = {18.75: ('t9_waiting', 1.2),    # the dead drifting past, close
         119.375: ('t9_line', 1.8)}     # one of them walking away toward the Gate's door, then the empty path
EDL = [(t, k, *_SWAP[t], args[2]) if t in _SWAP else (t, k, *args) for t, k, *args in EDL]
# the darkest wide shots get more lift so they read (measured: mean luma 6-8 against 14+ for the close-ups)
LIFTED = MELLOW.replace('gamma=1.4', 'gamma=1.75').replace("curves=all='0/0 0.05/0.03 0.4/0.42 1/1'", "curves=all='0/0 0.08/0.02 0.4/0.44 1/1'")   # and the haze pushed back to black
_DARK = {'t9_arrival', 't9_waiting', 't9_mere', 't9_hall', 't9_throne'}
EDL = [(t, k, a[0], a[1], dict(a[2], grade=LIFTED)) if k == 'clip' and a[0] in _DARK else (t, k, *a) for t, k, *a in EDL]


def main(only=None):
    keep = ROOT / 'run' / 'trailer_keep'
    keep.mkdir(parents=True, exist_ok=True)
    for src in (ROOT / 'run' / 'trailer').glob('t9_*.mp4'):
        shutil.copy(src, keep / src.name)
    trailer3.EDL = EDL
    trailer3.check_lengths()
    trailer4.EDL = EDL
    edit.EDL = EDL
    edit.GRADE = MELLOW
    edit.TITLE_SUB = trailer4.TITLE_SUB
    edit.TITLE_TAG = trailer4.TITLE_TAG
    import text
    if not isinstance(text.chat, functools.partial):
        text.chat = functools.partial(text.chat, y=850)
    edit.OUT = ROOT / 'build' / 'trailer5'
    score.compose = trailer3.compose
    (edit.OUT / 'seg').mkdir(parents=True, exist_ok=True)
    shutil.copy(edit.font_sheet(), edit.OUT / 'ascii.png')
    edit.main(only)
    final = edit.OUT / 'deathbound_trailer_full.mp4'
    trailer4.card_dust(edit.OUT)
    trailer3.finish(edit.OUT / 'deathbound_trailer.mp4', final)
    print('wrote', final)


if __name__ == '__main__':
    main({int(a) for a in sys.argv[1:]} or None)
