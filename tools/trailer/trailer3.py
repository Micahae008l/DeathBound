"""The whole-mod trailer: waking below, the Underworld and its people, the hunt, the Warden and the Death King (and his
slow death, the music dropping out under it as it does in the game), the three endings, the title. About two and a
half minutes on its own, longer arrangement of the same score (score.py's instruments, its bar grid).

    python tools/trailer/trailer3.py   -> build/trailer3/deathbound_trailer_full.mp4

Footage: run/trailer_keep (the first two trailers' clips) plus `shots.py tr3` (run/trailer, copied over first).
"""
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import edit  # noqa: E402
import score  # noqa: E402
from score import BAR, BEAT, CHORDS, LULLABY, bar, hz, boom, bell, braam, chains, choir, drone, heartbeat, impact, music_box, \
    ostinato, riser, taiko, clank, thunder, tremolo, whoosh, wind, Mix  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
NEW = ['tr7_mere', 'tr7_water', 'tr7_ribs', 'tr6_hunter', 'tr6_king', 'tr_market', 'tr3_quench', 'tr3_forge', 'tr3_riven', 'tr4_mira', 'tr4_sentry', 'tr4_watch',
       'tr4_hollow', 'tr4_throne', 'tr5_pack', 'tr5_barrage', 'tr5_nova', 'tr5_choice']
# Darker than the first two trailers: the mids aren't lifted, the shadows are crushed, the glow is kept. The Underworld
# is never lit, so it shouldn't look lit here either.
BLOOM = edit.BLOOM
NIGHT = BLOOM + "eq=contrast=1.08:saturation=1.3:gamma=1.22,curves=all='0/0 0.08/0.035 0.45/0.42 1/1',vignette=angle=0.8,noise=alls=3:allf=t"
# footage shot without night vision is darker to begin with: more lift so it still reads (tuned on the clips)
NIGHT_RAW = BLOOM + "eq=contrast=1.06:saturation=1.3:gamma=1.4,curves=all='0/0 0.08/0.04 0.45/0.45 1/1',vignette=angle=0.8,noise=alls=3:allf=t"
G, C = NIGHT, NIGHT
R = {'grade': NIGHT_RAW}
TITLE = 128.125
LENGTH = 140.5

EDL = [
    (0.0, 'black'),
    (1.0, 'card', ['MOST WHO DIE', 'WAKE UP IN BED.']),
    (4.5, 'clip', 'a1_storm2', 0.5, {'fadein': 0.4}),
    (6.9, 'clip', 'a1_rite2', 0.8, {}),
    (10.0, 'card', ['SOME WAKE UP BELOW.']),
    # the Underworld
    (12.5, 'clip', 'a2_arrival', 0.6, {'fadein': 0.4}),
    (16.25, 'clip', 'a3_ferryman', 1.4, {}),
    (18.75, 'clip', 'b1_line', 1.0, {}),
    (21.25, 'clip', 'tr_market', 0.8, R),
    (23.75, 'clip', 'tr_lamplighter', 1.5, {}),
    (26.25, 'clip', 'tr3_forge', 4.0, {}),
    (28.75, 'clip', 'b2_spire', 0.6, R),
    (31.25, 'clip', 'a8_prophet', 1.0, {}),
    (33.75, 'clip', 'a5_soul', 1.5, {'fadeout': 0.4}),
    # its people
    (37.5, 'card', ['THE DEAD', 'STILL KEEP HOUSE.']),
    (40.0, 'clip', 'tr7_mere', 1.0, {'fadein': 0.3, **R}),
    (42.5, 'clip', 'tr7_water', 1.0, R),
    (43.75, 'clip', 'tr3_quench', 0.6, {}),
    (48.75, 'clip', 'tr7_ribs', 1.5, R),
    (51.25, 'clip', 'tr4_sentry', 0.6, R),
    (55.0, 'clip', 'tr4_watch', 0.6, R),
    (58.125, 'clip', 'tr_wisp', 1.6, {'fadeout': 0.3}),
    # the hunt: the dead rising, the Hollow, and the Hunter just standing there in the dark
    (60.0, 'card', ['AND DEATH', 'STOPPED LETTING GO.']),
    (62.5, 'clip', 'b3_rise', 0.5, {'flash': True}),
    (65.0, 'clip', 'tr4_hollow', 0.8, R),
    (68.75, 'clip', 'tr6_hunter', 1.0, R),
    (73.75, 'clip', 'tr5_pack', 2.4, R),
    (76.25, 'clip', 'b3_rise_far', 2.2, {'fadeout': 0.3}),
    # the Warden at his gate
    (80.0, 'clip', 'b5_gate', 0.3, {'flash': True}),   # he says his line in the clip itself: no second caption over it
    # the Death King on his throne (none of his fight: the players find that out)
    (87.5, 'card', ['FACE WHAT', 'DEATH BECAME.']),
    (89.375, 'clip', 'd1_throne', 0.0, {'say': ('Death', "So the king's chain found a new neck.")}),
    (96.25, 'clip', 'tr6_king', 0.5, {'fadeout': 0.6, **R}),
    (101.875, 'black'),
    (104.375, 'card', ['WHAT HE BECOMES NEXT,', 'YOU WILL HAVE TO SEE.']),
    (109.3, 'black'),
    # the choice: the empty throne, and nothing of what it leads to
    (110.0, 'card', ['THEN CHOOSE', 'WHAT IT ALL BECOMES.']),
    (112.5, 'clip', 'tr4_throne', 0.1, R),
    (119.375, 'clip', 'b1_line', 0.2, {'fadeout': 0.8}),   # the dead, still waiting: what happens to them is the player's to find out
    (126.25, 'black'),
    (127.5, 'black'),
    (TITLE, 'title', 'tr7_water'),
    (136.25, 'last', 'Three endings. One throne. The dead are waiting.'),
    (LENGTH, 'end'),
]


def compose(hits=()):
    m = Mix(LENGTH)
    for at in hits:
        m.add(at, impact(), 0.45, rev=0.2)
    # waking below (as the first trailer: the storm, the rite, the bell)
    m.add(0.0, wind(37.5), 0.1, rev=0.1)
    m.add(0.4, drone([hz('D1'), hz('A1'), hz('D2')], 12.5, 150), 0.2, rev=0.15)
    m.add(9.5, drone([hz('D1'), hz('A1'), hz('D2')], 28.0, 200), 0.3, rev=0.15)
    for note, beat in LULLABY[:8]:
        m.add(1.25 + beat * BEAT, music_box(hz(note)), 0.16, pan=0.2, rev=0.55)
    m.add(4.3, whoosh(0.9), 0.12, rev=0.3)
    m.add(5.77, thunder(4.5), 0.42, pan=0.3, rev=0.3)
    m.add(7.9, thunder(3.0), 0.32, pan=-0.3, rev=0.3)
    m.add(8.2, chains(0.9), 0.2, pan=-0.1, rev=0.4)
    m.add(8.27, boom(3.0, 110, 30), 0.35, rev=0.35)
    m.add(7.95, whoosh(1.1), 0.2, rev=0.4)
    m.add(10.0, bell(hz('D3'), 10), 0.55, rev=0.7)
    m.add(10.0, boom(4.0, 120, 28, 2.5), 0.55, rev=0.5)
    # the Underworld: the chords walk down and back, the lullaby finishes
    for k, name in enumerate(['Dm', 'Bb', 'Gm', 'A', 'Dm']):
        root, voicing = CHORDS[name]
        at = bar(5 + 2 * k)
        m.add(at, choir([hz(v) for v in voicing], 2 * BAR + 1.5, 1.6, 1.8), 0.15 + 0.03 * k, rev=0.5)
        ostinato(m, at, at + 2 * BAR, root, 0.08 + 0.03 * k, 900 + 300 * k)
    for note, beat in LULLABY[8:]:
        m.add(bar(5) + 1.25 + (beat - 8) * BEAT, music_box(hz(note)), 0.14, pan=0.2, rev=0.6)
    m.add(37.2, whoosh(0.7), 0.12)
    m.add(37.5, boom(2.5, 100, 30), 0.3, rev=0.4)
    m.add(37.5, bell(hz('A3'), 7), 0.2, rev=0.7)
    # its people: quieter, the whole lullaby once more over soft choir
    m.add(37.5, drone([hz('D1'), hz('A1')], 22.5, 170), 0.22, rev=0.15)
    for k, name in enumerate(['Gm', 'Dm', 'Bb', 'A']):
        root, voicing = CHORDS[name]
        at = bar(16 + 2 * k)
        m.add(at, choir([hz(v) for v in voicing], 2 * BAR + 1.5, 1.8, 2.0, 0.8), 0.16, rev=0.6)
        ostinato(m, at, at + 2 * BAR, root, 0.07, 800)
    for note, beat in LULLABY:
        m.add(bar(16) + beat * BEAT * 1.5, music_box(hz(note)), 0.13, pan=-0.15, rev=0.65)
    for k in range(8):
        m.add(56.25 + k * 2 * BEAT * 0.75, heartbeat(), 0.3 + 0.04 * k, rev=0.1)
    m.add(57.5, riser(2.5), 0.25, rev=0.2)
    # the hunt
    m.add(60.0, boom(3.0, 140, 30, 2.5), 0.6, rev=0.4)
    m.add(60.0, chains(0.5), 0.12, rev=0.3)
    m.add(60.0, drone([hz('D1'), hz('A1')], 20.0, 220), 0.35, rev=0.1)
    for k, (name, bars) in enumerate([('Dm', 2), ('Bb', 2), ('Gm', 2), ('A', 2)]):
        root, voicing = CHORDS[name]
        at = bar(24 + 2 * k)
        m.add(at, choir([hz(v) for v in voicing], bars * BAR + 1.0, 0.8, 1.0), 0.3, rev=0.5)
        ostinato(m, max(at, 62.5), at + bars * BAR, root, 0.22, 2600, sixteenths=True)
        m.add(at, tremolo([hz(voicing[-1]) * 2, hz(voicing[0]) * 2], bars * BAR + 0.5), 0.12, pan=0.3, rev=0.4)
        if at < 62.5:
            ostinato(m, at, 62.5, root, 0.18, 1200)
    for b in range(25, 31):
        for beat in (0, 2):
            m.add(bar(b, beat), taiko(80), 0.5, rev=0.25)
        m.add(bar(b, 3.5), taiko(110, 0.2), 0.25, pan=0.3, rev=0.2)
    for k in range(16):
        m.add(bar(31) + k * BEAT / 4, taiko(95 + 3 * k, 0.15), 0.12 + 0.025 * k, pan=(-0.3, 0.3)[k % 2], rev=0.2)
    m.add(76.25, riser(3.75), 0.4, rev=0.2)
    # the Warden and the Death King
    for k, name in enumerate(['Dm', 'Bb', 'Gm', 'A', 'Dm']):
        root, voicing = CHORDS[name]
        at = bar(32 + 2 * k)
        m.add(at, braam(hz('D1') if name == 'Dm' else hz(root), 4.6), 0.75, rev=0.35)
        m.add(at, boom(3.0, 150, 30, 3.0), 0.5, rev=0.3)
        m.add(at, choir([hz(v) * 2 for v in voicing] + [hz(v) for v in voicing], 2 * BAR + 0.6, 0.3, 0.8, 1.6), 0.36, rev=0.5)
        ostinato(m, at, min(at + 2 * BAR, 103.75), root, 0.3, 3200, sixteenths=True)
        m.add(at, tremolo([hz(v) * 2 for v in voicing], min(2 * BAR + 0.4, 103.75 - at)), 0.16, pan=-0.3, rev=0.4)
    m.add(80.0, drone([hz('D1'), hz('A1'), hz('D2')], 23.75, 300), 0.4, rev=0.1)
    for b in range(32, 41):
        for six, g in ((0, 1.0), (3, 0.6), (6, 0.7), (8, 0.9), (11, 0.6), (14, 0.7)):
            m.add(bar(b) + six * BEAT / 4, taiko(78 if six in (0, 8) else 100, 0.3), 0.55 * g, pan=((six % 3) - 1) * 0.3, rev=0.22)
        for beat in (1, 3):
            m.add(bar(b, beat), clank(640 + 40 * (b % 3)), 0.09, pan=0.4, rev=0.4)
    m.add(87.5, boom(2.0, 160, 32, 3), 0.4, rev=0.3)
    m.add(87.5, chains(0.6), 0.14, rev=0.35)
    for k in range(20):
        m.add(bar(40) + k * BEAT / 4, taiko(90 + 2 * k, 0.12), 0.15 + 0.012 * k, pan=(-0.35, 0.35)[k % 2], rev=0.2)
    # his death: everything stops but a low hum and a heart that isn't there, slowing; the souls leave; the last flare
    m.add(103.75, boom(4.0, 90, 24, 2.0), 0.5, rev=0.6)
    m.add(103.75, drone([hz('D1'), hz('A1')], 6.25, 110), 0.3, rev=0.3)
    for k, gap in enumerate([0.0, 0.9, 1.9, 3.1]):
        m.add(104.2 + gap, heartbeat(), 0.45 - 0.07 * k, rev=0.2)
    m.add(109.3, boom(3.0, 140, 28, 2.5), 0.55, rev=0.6)   # the last flare, on screen at 109.3
    m.add(109.3, bell(hz('D3'), 8), 0.3, rev=0.8)
    # the choice: no drums; the choir rises chord by chord into the title
    m.add(110.0, chains(0.6), 0.12, rev=0.5)
    m.add(110.0, drone([hz('D1'), hz('A1'), hz('D2')], 17.5, 180), 0.3, rev=0.2)
    for k, name in enumerate(['Dm', 'Bb', 'Gm', 'A']):
        root, voicing = CHORDS[name]
        at = bar(44 + 2 * k) - (BAR if k == 0 else 0)
        m.add(at, choir([hz(v) for v in voicing] + [hz(v) * 2 for v in voicing], 2 * BAR + 1.5 + (BAR if k == 0 else 0), 1.0, 1.4, 1.2),
              0.26 + 0.05 * k, rev=0.6)
        ostinato(m, max(at, 112.5), bar(44 + 2 * k) + 2 * BAR, root, 0.12 + 0.04 * k, 1500 + 500 * k)
    m.add(112.5, bell(hz('A3'), 7), 0.25, rev=0.8)
    m.add(122.5, riser(5.0), 0.5, rev=0.15)
    # the title (as the first trailer's, moved along)
    m.add(TITLE, braam(hz('D1'), 7.0), 1.0, rev=0.5)
    m.add(TITLE, boom(5.0, 160, 26, 3.5), 0.85, rev=0.6)
    m.add(TITLE, bell(hz('D3'), 11), 0.55, rev=0.8)
    m.add(TITLE, chains(0.7), 0.2, rev=0.5)
    m.add(TITLE, choir([hz(v) for v in ['D3', 'A3', 'D4', 'F4', 'A4', 'D5']], 7.5, 0.05, 5.5, 1.4), 0.4, rev=0.6)
    m.add(TITLE, drone([hz('D1'), hz('A1')], 12.0, 140), 0.35, rev=0.2)
    m.add(TITLE + 5.625, bell(hz('A3'), 7), 0.22, rev=0.8)
    for note, dt in (('A4', 8.275), ('D5', 8.875), ('F5', 9.475), ('D5', 10.125)):
        m.add(TITLE + dt, music_box(hz(note), 3.5), 0.16, pan=0.2, rev=0.7)
    m.add(TITLE + 10.125, chains(0.5), 0.07, rev=0.6)
    return m.render(silence=(127.5, TITLE))


# ------------------------------------------------------------------ the finishing pass
BARS = 138   # 2.39:1 inside 1080: the cinema letterbox
DUST_SCALE = 2   # the dust is drawn at half size and scaled up: it's soft anyway


def soul_dust(path, seconds, fps=edit.FPS):
    """Motes of soul-light drifting up through the frame at three depths: far ones small, sharp and slow; near ones big,
    soft, faster and dimmer. Each twinkles on its own beat. Black ground, to be screened over the picture."""
    import subprocess
    import numpy as np
    w, h = 1920 // DUST_SCALE, 1080 // DUST_SCALE
    rng = np.random.default_rng(31)
    layers = [(90, 1.3, 6.0, 1.6), (40, 2.6, 14.0, 1.3), (9, 7.0, 30.0, 0.7)]   # (count, radius px at half size, rise px/s, brightness)
    motes = []
    for count, rad, rise, bright in layers:
        for _ in range(count):
            motes.append(dict(x=rng.random() * w, y=rng.random() * h, r=rad * (0.7 + 0.6 * rng.random()), v=rise * (0.6 + 0.8 * rng.random()),
                              b=bright * (0.6 + 0.4 * rng.random()), sway=rng.random() * 2 * np.pi, swayf=0.15 + rng.random() * 0.25,
                              tw=rng.random() * 2 * np.pi, twf=0.4 + rng.random() * 1.2, hue=rng.random()))
    sprites = {}

    def sprite(r):
        k = round(r * 2) / 2
        if k not in sprites:
            n = int(np.ceil(k * 3)) * 2 + 1
            yy, xx = np.mgrid[0:n, 0:n] - n // 2
            sprites[k] = np.exp(-(xx * xx + yy * yy) / (2 * k * k))
        return sprites[k]

    lilac, violet = np.array([205, 160, 255]), np.array([140, 70, 235])
    ff = subprocess.Popen([edit.FF, '-hide_banner', '-loglevel', 'error', '-y', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-s', f'{w}x{h}',
                           '-r', str(fps), '-i', '-', '-c:v', 'libx264', '-preset', 'fast', '-crf', '16', '-pix_fmt', 'yuv420p', str(path)],
                          stdin=subprocess.PIPE)
    for f in range(int(seconds * fps)):
        t = f / fps
        img = np.zeros((h, w, 3))
        for m in motes:
            y = (m['y'] - m['v'] * t) % (h + 40) - 20
            x = (m['x'] + 10 * np.sin(m['sway'] + t * m['swayf'] * 2 * np.pi) + 4 * t) % (w + 40) - 20
            a = m['b'] * (0.55 + 0.45 * np.sin(m['tw'] + t * m['twf'] * 2 * np.pi))
            spr = sprite(m['r'])
            n = spr.shape[0]
            x0, y0 = int(x) - n // 2, int(y) - n // 2
            xa, ya, xb, yb = max(0, x0), max(0, y0), min(w, x0 + n), min(h, y0 + n)
            if xa >= xb or ya >= yb:
                continue
            col = violet + (lilac - violet) * m['hue']
            img[ya:yb, xa:xb] += spr[ya - y0:yb - y0, xa - x0:xb - x0, None] * col * a
        ff.stdin.write(np.clip(img, 0, 255).astype(np.uint8).tobytes())
    ff.stdin.close()
    ff.wait()


def finish(src, dst):
    """Over the whole cut: the soul dust screened in, film grain over everything (the black cards too, so it's all one film),
    and the cinema letterbox. Sound copied as it is."""
    dust = src.with_name('soul_dust.mp4')
    if not dust.exists():   # it never changes with the cut: delete it to redraw
        soul_dust(dust, LENGTH)
    vf = (f"[1:v]scale=1920:1080,format=gbrp[d];[0:v]format=gbrp[v];[v][d]blend=all_mode=screen:all_opacity=0.85,format=yuv420p,"
          f"noise=alls=2:allf=t,drawbox=x=0:y=0:w=iw:h={BARS}:color=black:t=fill,drawbox=x=0:y=ih-{BARS}:w=iw:h={BARS}:color=black:t=fill")
    # delivery size, not the near-lossless intermediate encode: grain is expensive, so a little more compression
    edit.ff('-i', src, '-i', dust, '-filter_complex', vf, '-map', '0:a', '-c:a', 'copy', '-c:v', 'libx264', '-preset', 'slow', '-crf', '23',
            '-pix_fmt', 'yuv420p', '-r', str(edit.FPS), '-movflags', '+faststart', dst)


def check_lengths():
    """Every clip has to fill its slot: a short one silently shortens the whole cut and slides it off the music."""
    import re
    import subprocess
    keep = ROOT / 'run' / 'trailer_keep'
    short = []
    for i, (start, kind, *args) in enumerate(EDL[:-1]):
        if kind != 'clip':
            continue
        name, at, opt = args
        src = keep / f'{name}.mp4'
        out = subprocess.run([edit.FF, '-i', str(src)], capture_output=True, text=True).stderr
        h, m, sec = re.search(r'Duration: (\d+):(\d+):([\d.]+)', out).groups()
        have = int(h) * 3600 + int(m) * 60 + float(sec) - at
        need = (EDL[i + 1][0] - start) * opt.get('speed', 1.0)
        if have < need - 0.05:
            short.append(f'{start}: {name} has {have:.2f}s from {at}, needs {need:.2f}s')
    assert not short, 'clips too short:\n' + '\n'.join(short)


def main(only=None):
    keep = ROOT / 'run' / 'trailer_keep'
    for n in NEW:
        src = ROOT / 'run' / 'trailer' / f'{n}.mp4'
        if src.exists():
            shutil.copy(src, keep / f'{n}.mp4')
    check_lengths()
    edit.EDL = EDL
    edit.GRADE = NIGHT
    import functools
    import text
    if not isinstance(text.chat, functools.partial):
        text.chat = functools.partial(text.chat, y=850)   # above the letterbox (an explicit y, like the last line's, still wins)
    edit.OUT = ROOT / 'build' / 'trailer3'
    score.compose = compose
    (edit.OUT / 'seg').mkdir(parents=True, exist_ok=True)
    shutil.copy(ROOT / 'build' / 'trailer' / 'ascii.png', edit.OUT / 'ascii.png')
    edit.main(only)
    out = edit.OUT / 'deathbound_trailer.mp4'
    final = edit.OUT / 'deathbound_trailer_full.mp4'
    finish(out, final)
    print('wrote', final)


if __name__ == '__main__':
    main({int(a) for a in sys.argv[1:]} or None)
