"""Cuts the trailer: graded game footage, story cards and boss lines on the score's bar grid (score.py).

    python tools/trailer/score.py
    python tools/trailer/edit.py        -> build/trailer/deathbound_trailer.mp4

Footage comes from run/trailer_keep/<clip>.mp4 (filmed with shots.py).
"""
import subprocess
import sys
from pathlib import Path

import imageio_ffmpeg
from PIL import Image, ImageFilter

sys.path.insert(0, str(Path(__file__).parent))
import score
import text

ROOT = Path(__file__).resolve().parents[2]
CLIPS = ROOT / 'run' / 'trailer_keep'
OUT = ROOT / 'build' / 'trailer'
FF = imageio_ffmpeg.get_ffmpeg_exe()
FPS = 30
TITLE = 75.625

# The look: a soft bloom on everything that glows, the haze crushed to black, mids lifted, a vignette and grain.
BLOOM = "split[a][b];[b]curves=all='0/0 0.5/0 1/1',gblur=sigma=16[g];[a][g]blend=all_mode=screen:all_opacity=0.75,"
FINISH = "curves=all='0/0 0.06/0.035 0.35/0.4 1/1',vignette=angle=0.55,noise=alls=3:allf=t"
GRADE = BLOOM + "eq=contrast=1.06:saturation=1.35:gamma=1.28," + FINISH
COLD = BLOOM + "eq=contrast=1.05:saturation=0.9:gamma=1.35,colorbalance=bs=0.07:bm=0.03," + FINISH

# (start, kind, *args). Each runs until the next one starts.
EDL = [
    (0.0, 'black'),
    (1.0, 'card', ['MOST WHO DIE', 'WAKE UP IN BED.']),
    (4.5, 'clip', 'a1_storm2', 0.5, {'grade': COLD, 'fadein': 0.4}),
    (6.9, 'clip', 'a1_rite2', 0.8, {'grade': COLD}),
    (10.0, 'card', ['SOME WAKE UP BELOW.']),
    (12.5, 'clip', 'a2_arrival', 0.6, {'fadein': 0.4}),
    (16.25, 'clip', 'a3_ferryman', 1.4, {}),
    (18.75, 'clip', 'b1_line', 1.0, {}),
    (22.5, 'card', ['THE RIVER OF THE DEAD', 'RAN DRY.']),
    (25.0, 'clip', 'b2_spire', 0.6, {'fadein': 0.3}),
    (28.75, 'clip', 'a8_prophet', 1.0, {}),
    (31.25, 'clip', 'a5_soul', 2.0, {}),
    (33.125, 'clip', 'a6_gravedigger', 0.8, {}),
    (35.0, 'clip', 'a4_dialog', 2.4, {'fadeout': 0.3}),
    (37.5, 'card', ['AND DEATH', 'STOPPED LETTING GO.']),
    (40.0, 'clip', 'b3_rise', 0.5, {'flash': True}),
    (42.5, 'clip', 'b4_wisps', 1.0, {}),
    (44.375, 'clip', 'c1_armor', 0.5, {}),
    (46.25, 'clip', 'c2_scythe', 2.4, {}),
    (48.75, 'clip', 'c6_condemn', 1.1, {}),
    (50.0, 'clip', 'b5_gate', 5.3, {'flash': True, 'say': ('The Warden', 'The living do not pass.')}),
    (52.5, 'clip', 'f2_judge', 0.9, {'say': ('The Warden', 'I judge you!'), 'hits': [1.87]}),
    (54.375, 'clip', 'f1_dodge', 1.205, {'hits': [1.83]}),  # the slam lands on the braam at 55.0
    (55.625, 'clip', 'f3_combo', 0.55, {'speed': 0.6, 'hits': [0.73, 1.33]}),
    (57.5, 'card', ['FACE WHAT', 'DEATH BECAME.']),
    (59.375, 'clip', 'd2_rise', 3.4, {'say': ('Death', "So the king's chain found a new neck.")}),
    (62.5, 'clip', 'g2_reaper', 0.45, {'hits': [0.67]}),
    (63.75, 'clip', 'g3_nova', 0.9, {'hits': [1.5]}),
    (65.0, 'clip', 'g1_blink', 0.2, {'say': ('Death', 'Behind you.'), 'hits': [0.77]}),
    (66.875, 'clip', 'f5_barrage', 0.7, {'hits': [1.2]}),
    (68.125, 'clip', 'g4_transform', 2.4, {'say': ('Death', 'Then see what the king made of me!')}),
    (70.0, 'clip', 'g6_claw', 2.45, {'flash': True, 'hits': [3.07, 3.27]}),
    (71.25, 'clip', 'g7_charge', 1.1, {}),
    (71.875, 'clip', 'g8_rip', 1.05, {'hits': [1.27]}),
    (72.5, 'clip', 'g9_beastfight', 0.45, {'hits': [0.57]}),
    (73.125, 'clip', 'f12_finisher', 0.45, {'hits': [0.57, 0.93]}),
    (73.75, 'clip', 'g9_beastfight', 1.0, {'hits': [1.17, 1.47]}),
    (74.375, 'clip', 'g5_roar', 2.7, {}),
    (75.0, 'black'),
    (TITLE, 'title', 'b1_line'),
    (83.75, 'last', 'Keep it in your off hand... and die.'),
    (88.0, 'end'),
]


def frames(t):
    return round(t * FPS)


def ff(*args):
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', *map(str, args)], check=True)


ENCODE = ['-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-r', str(FPS)]


def overlay_fades(n, fade_in=0.35, hold_from=0.0, out_at=None):
    """Alpha fades for a still overlay that shows from hold_from to out_at (seconds into the segment)."""
    out_at = out_at if out_at is not None else n / FPS - 0.35
    return f"fade=in:st={hold_from}:d={fade_in}:alpha=1,fade=out:st={out_at}:d=0.35:alpha=1"


def segment(i, start, end, kind, args, font):
    n = frames(end) - frames(start)
    path = OUT / 'seg' / f'{i:03d}.mp4'
    black = ['-f', 'lavfi', '-i', f'color=c=black:s=1920x1080:r={FPS}']
    if kind == 'black':
        ff(*black, '-frames:v', n, *ENCODE, path)
    elif kind == 'card':
        png = OUT / 'seg' / f'{i:03d}.png'
        text.card(args[0]).save(png)
        ff(*black, '-loop', '1', '-i', png, '-filter_complex',
           f"[1]format=rgba,{overlay_fades(n, 0.45, 0.1)}[t];[0][t]overlay=format=auto", '-frames:v', n, *ENCODE, path)
    elif kind == 'clip':
        name, at, opt = args
        speed = opt.get('speed', 1.0)  # below 1 is slow motion; film those at 60 fps
        chain = [f'setpts=PTS/{speed},fps={FPS}', opt.get('grade', GRADE)]
        if opt.get('fadein'):
            chain.append(f"fade=in:st=0:d={opt['fadein']}")
        if opt.get('fadeout'):
            chain.append(f"fade=out:st={n / FPS - opt['fadeout']:.3f}:d={opt['fadeout']}")
        if opt.get('flash'):
            chain.append("fade=in:st=0:d=0.18:color=white")
        vf = ','.join(chain)
        if 'say' in opt:
            png = OUT / 'seg' / f'{i:03d}.png'
            text.chat(font, *opt['say']).save(png)
            ff('-ss', at, '-i', CLIPS / f'{name}.mp4', '-loop', '1', '-i', png, '-filter_complex',
               f"[0]{vf}[v];[1]format=rgba,{overlay_fades(n, 0.2, 0.15, n / FPS - 0.3)}[s];[v][s]overlay=format=auto",
               '-frames:v', n, *ENCODE, path)
        else:
            ff('-ss', at, '-i', CLIPS / f'{name}.mp4', '-vf', vf, '-frames:v', n, *ENCODE, path)
    elif kind == 'title':
        # the logo over a smear of the line of souls, slammed in on the final hit
        logo, bottom = text.logo(font)
        sub = text.card(['A FABRIC MOD FOR MINECRAFT 26.3'], size=30, spacing=0.3, y=bottom + 44, glow=False)
        lp, sp = OUT / 'seg' / 'logo.png', OUT / 'seg' / 'logo_sub.png'
        logo.save(lp)
        sub.save(sp)
        ff('-ss', 1.0, '-i', CLIPS / f'{args[0]}.mp4', '-loop', '1', '-i', lp, '-loop', '1', '-i', sp, '-filter_complex',
           f"[0]gblur=sigma=18,eq=brightness=-0.04:saturation=1.15:gamma=0.85,vignette=angle=0.9,fade=in:st=0:d=1.5[bg];"
           f"[1]format=rgba,fade=in:st=0:d=0.08:alpha=1,fade=out:st={n / FPS - 0.6:.3f}:d=0.6:alpha=1[l];"
           f"[2]format=rgba,fade=in:st=1.9:d=1.0:alpha=1,fade=out:st={n / FPS - 0.6:.3f}:d=0.6:alpha=1[s];"
           f"[bg][l]overlay=format=auto[a];[a][s]overlay=format=auto,fade=in:st=0:d=0.25:color=white,"
           f"fade=out:st={n / FPS - 0.6:.3f}:d=0.6",
           '-frames:v', n, *ENCODE, path)
    elif kind == 'last':
        png = OUT / 'seg' / f'{i:03d}.png'
        text.chat(font, '', args[0], scale=5, y=520, speakerless=True).save(png)
        ff(*black, '-loop', '1', '-i', png, '-filter_complex',
           f"[1]format=rgba,{overlay_fades(n, 0.8, 0.45, n / FPS - 0.9)}[t];[0][t]overlay=format=auto", '-frames:v', n, *ENCODE, path)
    return path


def thumbnail(clip, at, font):
    """1280x720 for YouTube: a graded frame, the logo, one line."""
    frame = OUT / 'thumb_frame.png'
    ff('-ss', at, '-i', CLIPS / f'{clip}.mp4', '-vf', GRADE.replace('noise=alls=4:allf=t', 'null') + ',eq=brightness=0.03:gamma=1.12',
       '-frames:v', 1, frame)
    img = Image.open(frame).convert('RGBA')
    shade = Image.new('RGBA', img.size, (0, 0, 0, 0))
    for y in range(img.height):  # darken the top for the logo
        a = int(max(0, 1 - y / (img.height * 0.55)) * 170)
        Image.Image.paste(shade, (0, 0, 0, a), (0, y, img.width, y + 1))
    img.alpha_composite(shade)
    logo, _ = text.logo(font, scale=13)
    img.alpha_composite(logo, (0, 0), (0, 300))  # lift it into the dark top
    img.convert('RGB').resize((1280, 720), Image.LANCZOS).save(OUT / 'thumbnail.png')


def main(only=None):
    (OUT / 'seg').mkdir(parents=True, exist_ok=True)
    font = text.PixelFont(OUT / 'ascii.png')
    paths = []
    for i, (start, kind, *args) in enumerate(EDL[:-1]):
        end = EDL[i + 1][0]
        p = OUT / 'seg' / f'{i:03d}.mp4'
        if only is None or i in only or not p.exists():
            print(f'{i:2d} {start:7.3f}-{end:7.3f} {kind} {args[0] if args else ""}')
            segment(i, start, end, kind, args, font)
        paths.append(p)
    hits = [start + (h - args[1]) / args[2].get('speed', 1.0)
            for (start, kind, *args) in EDL[:-1] if kind == 'clip' for h in args[2].get('hits', ())]
    score.write_wav(OUT / 'score.wav', score.compose(hits))
    (OUT / 'seg' / 'list.txt').write_text(''.join(f"file '{p.as_posix()}'\n" for p in paths))
    ff('-f', 'concat', '-safe', 0, '-i', OUT / 'seg' / 'list.txt', '-i', OUT / 'score.wav',
       '-map', '0:v', '-map', '1:a', '-c:v', 'copy', '-c:a', 'aac', '-b:a', '320k', '-shortest',
       '-movflags', '+faststart', OUT / 'deathbound_trailer.mp4')
    print('wrote', OUT / 'deathbound_trailer.mp4')


if __name__ == '__main__':
    main({int(a) for a in sys.argv[1:]} or None)
