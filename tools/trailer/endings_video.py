"""Cut the endings video: the Death King falls, then each ending with a title card before it, under one track.
Clips come from `python tools/trailer/shots.py ev` (run/trailer/ev_*.mp4). Output: build/endings/deathbound_endings.mp4."""
import json
import os
import subprocess
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
from text import card  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
CLIPS = ROOT / 'run' / 'trailer'
OUT = ROOT / 'build' / 'endings'
FF = r'C:\Users\Michael\AppData\Roaming\Python\Python312\site-packages\imageio_ffmpeg\binaries\ffmpeg-win-x86_64-v7.1.exe'
FPS = 30
ENC = ['-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-r', str(FPS)]


def ff(*args):
    subprocess.run([FF, '-hide_banner', '-loglevel', 'error', '-y', *map(str, args)], check=True)


def asset(name):
    """A sound from the game's own asset store."""
    loom = Path(os.path.expanduser('~/.gradle/caches/fabric-loom/assets'))
    index = json.loads((loom / 'indexes' / '26.3-34.json').read_text())['objects']
    h = index[f'minecraft/sounds/{name}.ogg']['hash']
    return loom / 'objects' / h[:2] / h


def title(name, lines, seconds=3.2):
    png = OUT / f'{name}.png'
    card(lines, size=64).save(png)
    mp4 = OUT / f'{name}.mp4'
    ff('-f', 'lavfi', '-i', f'color=c=black:s=1920x1080:d={seconds}:r={FPS}', '-loop', '1', '-t', seconds, '-i', png,
       '-filter_complex', f'[1]format=rgba,fade=in:st=0.3:d=0.6:alpha=1,fade=out:st={seconds - 0.8}:d=0.6:alpha=1[t];[0][t]overlay', *ENC, mp4)
    return mp4


def clip(name, start=0.0, end=None, tag=''):
    src = CLIPS / f'{name}.mp4'
    mp4 = OUT / f'{name}{tag}_cut.mp4'
    args = ['-ss', start, '-i', src]
    if end:
        args += ['-t', end - start]
    ff(*args, '-vf', 'fade=in:st=0:d=0.5', '-an', *ENC, mp4)
    return mp4


def duration(name):
    err = subprocess.run([FF, '-i', str(CLIPS / f'{name}.mp4')], capture_output=True, text=True).stderr
    h, m, sec = err.split('Duration: ')[1].split(',')[0].split(':')
    return int(h) * 3600 + int(m) * 60 + float(sec)


def captioned(name, lines, at=1.5, until=7.5):
    """A clip with a title card faded in over it."""
    base = clip(name, 0.3)
    png = OUT / f'{name}_card.png'
    card(lines, size=60).save(png)
    mp4 = OUT / f'{name}_titled.mp4'
    ff('-i', base, '-loop', '1', '-i', png, '-filter_complex',
       f'[1]format=rgba,fade=in:st={at}:d=0.8:alpha=1,fade=out:st={until}:d=0.8:alpha=1[t];[0][t]overlay=shortest=1', *ENC, mp4)
    return mp4


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    # the fall: the cutscene and the call to run, two stretches of the run itself, and the last shot from the Landing
    fall = duration('ev2_break')
    parts = [title('t0', ['DEATHBOUND', 'THE THREE ENDINGS']),
             captioned('ev_intro', ['THE THRONE IS EMPTY', 'BRING HIS HEART TO IT']),
             title('t1', ['I', 'TAKE THE THRONE']), clip('ev2_take', 0.5),
             title('t2', ['II', 'FREE THE OLD KING', 'ORDER, WITH A PRICE']), clip('ev2_king', 0.5),
             title('t3', ['III', 'BREAK THE THRONE', 'THE FALL']), clip('ev2_break', 0.5, 26.0, '_a'),
             clip('ev2_break', 38.0, 46.0, '_b'), clip('ev2_break', 54.0, 60.0, '_c'), clip('ev2_break', 66.0, min(fall, 84.5), '_d')]
    lst = OUT / 'parts.txt'
    lst.write_text(''.join(f"file '{p.as_posix()}'\n" for p in parts))
    silent = OUT / 'silent.mp4'
    ff('-f', 'concat', '-safe', 0, '-i', lst, '-c', 'copy', silent)
    music = asset('music/game/nether/soulsand_valley/so_below')
    final = OUT / 'deathbound_endings.mp4'
    ff('-i', silent, '-stream_loop', -1, '-i', music, '-map', '0:v', '-map', '1:a', '-shortest',
       '-af', 'volume=0.8,afade=in:st=0:d=2', '-c:v', 'copy', '-c:a', 'aac', '-b:a', '192k', final)
    print(final)


if __name__ == '__main__':
    main()
