"""The people of the Underworld: each character's footage with their name over it, their story on a card, and their
dialog. Clips from `python tools/trailer/shots.py npcv` (+ npcv2). Output: build/npcs/deathbound_characters.mp4."""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).parent))
from text import card  # noqa: E402
from endings_video import ff, asset, ENC, FPS  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
CLIPS, SHOTS = ROOT / 'run' / 'trailer', ROOT / 'run' / 'screenshots'
OUT = ROOT / 'build' / 'npcs'

PEOPLE = [
    ('ferryman', 'THE FERRYMAN', 'Keeper of the River of the Dead', 'dlg_ferryman', [
        'He rowed every soul down the River of the Dead.',
        'He rowed every King down too. Halden. Ysolde. The King. Silas.',
        'Then the Death King drank the river dry.',
        'Now he waits on his dock over the dry mud, and keeps what the dead drop.']),
    ('aldous', 'ALDOUS, THE TRAVELER', 'A smith who came down alive', 'dlg_gravedigger', [
        'He came down alive, looking for his daughter, Mira.',
        'She is somewhere in the line of souls at the Great Door.',
        'He wears a chain he should not have. Someone sent it to him.',
        'He forges Grave Runes into your armor, and he waits.']),
    ('prophet', 'THE CHAINED PROPHET', 'Hung in the Tomb of Kings', 'dlg_prophet', [
        'Blind, chained to the ceiling of the Tomb of Kings.',
        'He knows where every key is hidden.',
        'His secret: he is the old King.',
        'The Death King took his eyes and his throne. He sent your chain up.']),
    ('collector', 'THE COLLECTOR', 'Under the floor of the Spire', 'dlg_collector', [
        'A small room under the Spire, lit by glowcaps.',
        'He keeps what the Underworld forgets, in jars.',
        'Bring him the eight lost things of the Kings.',
        "He doesn't want your soul. He wants your pockets."]),
    ('warden', 'THE WARDEN', 'Judge of the Great Door', None, [
        'He judged every soul that came to the Great Door.',
        'He serves the throne. Whoever sits on it.',
        'Right now, that is the Death King.',
        'Beat him, and he kneels.']),
    ('hunter', 'THE HOLLOW HUNTER', 'The first to come down', 'gal_hunter_draw', [
        'The first living soul ever to come down.',
        'He refused the throne, and went hunting instead.',
        'He has no quiver. Every arrow he shoots is one somebody put in his back.',
        'Skulls with purple arrows through them mark his ground.']),
    ('bonesmith', 'CLATTER, THE BONESMITH', 'Smith of the Mere', 'm11_temper', [
        'A little golem of bones and grave-stone. He cannot talk. He clacks.',
        'Ysolde, the second King, built him from the bones of the first smiths.',
        'He tempers your weapons in the Mere: the one water the Death King never drank.',
        'Three quenches. The last two only once the Warden kneels, and the Hunter is dead.']),
    ('mira', 'MIRA', "Aldous's daughter", 'f12_mira_met', [
        'Nine years old. Nine forever now. Missing a front tooth.',
        'She waits in the line of souls at the Great Door, counting the people in it.',
        'Her father came down alive to find her. He cannot get past the Warden.',
        'You can. Take her his lantern. She will send something back.']),
    ('lamplighter', 'THE LAMPLIGHTER', "Keeper of Lantern's End's lamps", 'f4_lamplighter', [
        'Forty years lighting lamps up top. When he died, he found lamps down here too, going out.',
        'The Death King has been eating the souls that keep them lit.',
        'Bring him three soul jars and the street gets a little brighter.',
        'He gives you a wisp of his own lamp. It follows you home.']),
    ('sentry', 'THE SENTRY', 'Still at his post on the Western Watch', 'f14_sentry_b', [
        'Posted on the Watch to warn the King if the Hunter ever came east.',
        'He never came. The Sentry never left.',
        'He has forgotten his own name. It is on a tag he dropped, running from the Hollow.',
        'Find it, and he will tell you how the Hunter hunts.']),
    ('kids', 'THE CHILDREN', "Lantern's End", 't8_kids_b', [
        "Pip, Tib, Nell, and the little one who hasn't picked a name yet.",
        'They play Judgement: one is the Warden, everyone else is the line.',
        'They cannot be hurt. They are already dead.',
        'They lost their ball at the Crossing. They would very much like it back.']),
    ('deathking', 'THE DEATH KING', 'Silas, who came to win', None, [
        'A player like you, who came down to beat the Underworld.',
        'Then he learned the rule: one day, another chain comes for you.',
        'So he shut the Door, ate the souls, drank the Lake dry,',
        'and melted every chain he could find. All but yours.']),
]


def band(img):
    """Darken a band behind the text so it reads over footage."""
    shade = Image.new('RGBA', img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(shade)
    for y in range(360, 720):
        a = int(150 * (1 - abs(y - 540) / 180) ** 0.7)
        d.line([(0, y), (1920, y)], fill=(0, 0, 0, a))
    shade.alpha_composite(img)
    return shade


def name_png(key, name, role):
    img = card([name], size=72, y=440, glow=True)
    img.alpha_composite(card([role.upper()], size=30, spacing=0.3, color=(185, 170, 215), y=560, glow=False))
    path = OUT / f'{key}_name.png'
    band(img).save(path)
    return path


def footage(key, name, role):
    src = CLIPS / f'npc_{key}.mp4'
    out = OUT / f'{key}_clip.mp4'
    if out.exists() and out.stat().st_mtime > src.stat().st_mtime:
        return out
    png = name_png(key, name, role)
    ff('-ss', 0.4, '-t', 6.4, '-i', src, '-loop', '1', '-i', png, '-filter_complex',
       '[0]fade=in:st=0:d=0.6,fade=out:st=5.8:d=0.6[v];[1]format=rgba,fade=in:st=0.6:d=0.7:alpha=1,fade=out:st=4.4:d=0.7:alpha=1[t];'
       '[v][t]overlay=shortest=1', '-an', *ENC, out)
    return out


def story(key, lines, seconds=7.0):
    png = OUT / f'{key}_story.png'
    card(lines, size=40, spacing=0.03, color=(222, 214, 236), glow=False).save(png)
    out = OUT / f'{key}_story.mp4'
    ff('-f', 'lavfi', '-i', f'color=c=0x07050b:s=1920x1080:d={seconds}:r={FPS}', '-loop', '1', '-t', seconds, '-i', png,
       '-filter_complex', f'[1]format=rgba,fade=in:st=0.3:d=0.8:alpha=1,fade=out:st={seconds - 1.0}:d=0.7:alpha=1[t];[0][t]overlay', *ENC, out)
    return out


def still(key, shot, seconds=5.0):
    out = OUT / f'{key}_still.mp4'
    ff('-i', SHOTS / f'{shot}.png', '-vf',
       f"scale=2112:-1,zoompan=z='min(zoom+0.0006,1.06)':d={int(seconds * FPS)}:s=1920x1080:fps={FPS},fade=in:st=0:d=0.5,fade=out:st={seconds - 0.6}:d=0.6",
       *ENC, out)
    return out


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    intro = OUT / 'intro.mp4'
    png = OUT / 'intro.png'
    img = card(['DEATHBOUND'], size=80, y=430)
    img.alpha_composite(card(['THE PEOPLE OF THE UNDERWORLD'], size=34, spacing=0.3, color=(185, 170, 215), y=560, glow=False))
    img.save(png)
    ff('-f', 'lavfi', '-i', f'color=c=black:s=1920x1080:d=4:r={FPS}', '-loop', '1', '-t', 4, '-i', png,
       '-filter_complex', '[1]format=rgba,fade=in:st=0.4:d=0.8:alpha=1,fade=out:st=3.0:d=0.8:alpha=1[t];[0][t]overlay', *ENC, intro)
    parts = [intro]
    for key, name, role, shot, lines in PEOPLE:
        parts.append(footage(key, name, role))
        parts.append(story(key, lines))
        if shot:
            parts.append(still(key, shot))
    lst = OUT / 'parts.txt'
    lst.write_text(''.join(f"file '{p.as_posix()}'\n" for p in parts))
    silent = OUT / 'silent.mp4'
    ff('-f', 'concat', '-safe', 0, '-i', lst, '-c', 'copy', silent)
    final = OUT / 'deathbound_characters.mp4'
    ff('-i', silent, '-stream_loop', -1, '-i', asset('music/game/nether/crimson_forest/chrysopoeia'), '-map', '0:v', '-map', '1:a', '-shortest',
       '-af', 'volume=0.8,afade=in:st=0:d=2', '-c:v', 'copy', '-c:a', 'aac', '-b:a', '192k', final)
    print(final)


if __name__ == '__main__':
    main()
