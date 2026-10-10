"""Trailer 4: trailer 3's cut, cards and score, every shot refilmed (shots.py t9a..t9i). Default brightness, no night
vision, subjects lit by hidden key lights; the scythe as it swings now; the Sentry with a Shade drifting in. The soul
dust only drifts over the black screens and cards, never over the footage. It's a beta: a small BETA tipped over the
logo, and the last line asks for 100 likes for the beta drop.

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
    (12.5, 'clip', 't9_arrival', 1.2, {'fadein': 0.4, **R}),   # creeping toward the altar
    (16.25, 'clip', 't9_ferryman', 1.4, R),
    (18.75, 'clip', 't9_line', 1.0, R),
    (21.25, 'clip', 't9_market', 0.8, R),
    (23.75, 'clip', 't9_lamplighter', 1.5, R),
    (26.25, 'clip', 't9_forge', 3.2, R),
    (28.75, 'clip', 't9_collector', 0.2, R),
    (31.25, 'clip', 't9_prophet', 1.0, R),
    (33.75, 'clip', 't9_soul', 0.4, {'fadeout': 0.4, **R}),   # face to face with one of the dead
    # its people
    (37.5, 'card', ['THE DEAD', 'STILL KEEP HOUSE.']),
    (40.0, 'clip', 't9_mere', 1.0, {'fadein': 0.3, **R}),
    (43.75, 'clip', 't9_quench', 0.6, R),
    (48.75, 'clip', 't9_ribs', 1.5, R),
    (51.25, 'clip', 't9_sentry', 1.6, R),   # the Sentry at his post, and a Shade drifting in
    (55.0, 'clip', 't9_watch', 1.2, R),   # the Watch's brazier, a slow push in
    (58.125, 'clip', 't9_wisp', 1.0, {'fadeout': 0.3, **R}),
    # the hunt
    (60.0, 'card', ['AND DEATH', 'STOPPED LETTING GO.']),
    (62.5, 'clip', 't9_rise', 0.45, {'flash': True, **R}),   # he's under the ground from here and climbs out
    (65.0, 'clip', 't9_hollow', 0.8, R),
    (68.75, 'clip', 't9_hunter', 1.0, R),   # facing us, a light on him
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
    (TITLE, 'title', 't9_gate'),   # the logo over the Gate's door, blurred
    (136.25, 'last', 'The beta drops at 100 likes.'),
    (LENGTH, 'end'),
]


TITLE_SUB = ['SOME DOORS ONLY OPEN ONE WAY.']
TITLE_TAG = 'BETA'   # tipped over the logo's top right, like Minecraft's splash text
DUST_FADE = 0.5


def dust_gain(t):
    """How much soul dust shows at t: all of it over the black screens and cards (and the title), none over footage,
    eased in and out over DUST_FADE at the edges."""
    spans = []
    for i, (start, kind, *_) in enumerate(EDL[:-1]):
        if kind != 'clip':
            end = EDL[i + 1][0]
            if spans and abs(spans[-1][1] - start) < 1e-6:
                spans[-1][1] = end
            else:
                spans.append([start, end])
    return max([0.0] + [min(1.0, (t - a) / DUST_FADE, (b - t) / DUST_FADE) for a, b in spans])


def card_dust(out):
    """build/trailer4/soul_dust.mp4 (what trailer3.finish screens over the cut): the full dust, faded by dust_gain."""
    import subprocess
    import numpy as np
    full, masked = out / 'soul_dust_full.mp4', out / 'soul_dust.mp4'
    if not full.exists():
        trailer3.soul_dust(full, LENGTH)
    w, h = 1920 // trailer3.DUST_SCALE, 1080 // trailer3.DUST_SCALE
    src = subprocess.Popen([edit.FF, '-loglevel', 'error', '-i', str(full), '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-'], stdout=subprocess.PIPE)
    dst = subprocess.Popen([edit.FF, '-hide_banner', '-loglevel', 'error', '-y', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-s', f'{w}x{h}',
                            '-r', str(edit.FPS), '-i', '-', '-c:v', 'libx264', '-preset', 'fast', '-crf', '16', '-pix_fmt', 'yuv420p', str(masked)],
                           stdin=subprocess.PIPE)
    f = 0
    while True:
        buf = src.stdout.read(w * h * 3)
        if len(buf) < w * h * 3:
            break
        g = dust_gain(f / edit.FPS)
        frame = np.frombuffer(buf, np.uint8)
        dst.stdin.write(frame.tobytes() if g >= 1.0 else (frame.astype(np.float32) * g).astype(np.uint8).tobytes())
        f += 1
    dst.stdin.close()
    dst.wait()
    src.wait()


def main(only=None):
    keep = ROOT / 'run' / 'trailer_keep'
    keep.mkdir(parents=True, exist_ok=True)
    for src in (ROOT / 'run' / 'trailer').glob('t9_*.mp4'):
        shutil.copy(src, keep / src.name)
    trailer3.EDL = EDL
    trailer3.check_lengths()
    edit.EDL = EDL
    edit.GRADE = NIGHT_RAW
    edit.TITLE_SUB = TITLE_SUB
    edit.TITLE_TAG = TITLE_TAG
    import text
    if not isinstance(text.chat, functools.partial):
        text.chat = functools.partial(text.chat, y=850)
    edit.OUT = ROOT / 'build' / 'trailer4'
    score.compose = trailer3.compose
    (edit.OUT / 'seg').mkdir(parents=True, exist_ok=True)
    shutil.copy(ROOT / 'build' / 'trailer' / 'ascii.png', edit.OUT / 'ascii.png')
    edit.main(only)
    final = edit.OUT / 'deathbound_trailer_full.mp4'
    card_dust(edit.OUT)
    trailer3.finish(edit.OUT / 'deathbound_trailer.mp4', final)
    print('wrote', final)


if __name__ == '__main__':
    main({int(a) for a in sys.argv[1:]} or None)
