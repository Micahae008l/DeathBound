"""An original score for the trailer, synthesised from scratch (no samples, nothing to get flagged).

D minor, 96 bpm, a bar is 2.5 s. The cut in edit.py lands on the same grid:
  0-12.5   intro: wind, a low drone, a music-box lullaby, the rite, one great bell
  12.5-37.5 the Underworld: choir, a pulsing low-string ostinato, the lullaby again, a heartbeat
  37.5-50  danger: drums come in, the strings double time, a riser
  50-75    the bosses: braams on every chord, full drums, choir up an octave, a last riser
  75-75.6  silence
  75.6-88  the title: the biggest braam, the bell, the lullaby's last note
`python tools/trailer/score.py` writes build/trailer/score.wav.
"""
import numpy as np
from pathlib import Path
from scipy.signal import butter, sosfilt, fftconvolve

SR = 48000
BEAT = 0.625
BAR = 4 * BEAT
LENGTH = 88.0
RNG = np.random.default_rng(7)


def bar(b, beat=0.0):
    return b * BAR + beat * BEAT


def hz(note):
    """'D4' / 'Bb3' / 'C#5' -> frequency."""
    names = {'C': 0, 'D': 2, 'E': 4, 'F': 5, 'G': 7, 'A': 9, 'B': 11}
    n = names[note[0]]
    rest = note[1:]
    if rest[0] in '#b':
        n += 1 if rest[0] == '#' else -1
        rest = rest[1:]
    return 440.0 * 2 ** ((n + 12 * (int(rest) + 1) - 69) / 12)


def ts(dur):
    return np.arange(int(dur * SR)) / SR


def lp(x, fc, order=4):
    return sosfilt(butter(order, min(fc, SR * 0.45), 'low', fs=SR, output='sos'), x)


def hp(x, fc, order=2):
    return sosfilt(butter(order, fc, 'high', fs=SR, output='sos'), x)


def bp(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, min(hi, SR * 0.45)], 'band', fs=SR, output='sos'), x)


def env(n, attack, release, total=None):
    """Linear attack, flat, linear release, over n samples."""
    e = np.ones(n)
    a, r = int(attack * SR), int(release * SR)
    if a:
        e[:a] = np.linspace(0, 1, a)
    if r:
        e[-r:] *= np.linspace(1, 0, r)
    return e


def saw(f, t, phase=0.0):
    p = np.cumsum(np.broadcast_to(f, t.shape) / SR) + phase
    return 2 * (p % 1.0) - 1


def sine_sweep(f, t):
    return np.sin(2 * np.pi * np.cumsum(np.broadcast_to(f, t.shape) / SR))


def norm(x, peak=1.0):
    m = np.max(np.abs(x))
    return x * (peak / m) if m > 0 else x


# ------------------------------------------------------------------ instruments
def drone(freqs, dur, cutoff=180):
    t = ts(dur)
    out = np.zeros(len(t))
    for f in freqs:
        for d in (-0.004, 0.0, 0.0035):
            out += saw(f * (1 + d) * (1 + 0.002 * np.sin(2 * np.pi * 0.07 * t + f)), t, RNG.random())
    out = lp(out, cutoff) + 0.3 * np.sin(2 * np.pi * freqs[0] * t)
    return norm(out) * env(len(t), 3.0, 3.0)


def choir(freqs, dur, attack=1.5, release=2.0, bright=1.0):
    """Detuned saws through 'ah' formants: a wordless choir."""
    t = ts(dur)
    src = np.zeros(len(t))
    for f in freqs:
        for k in range(4):
            vib = 1 + 0.004 * np.sin(2 * np.pi * (4.6 + 0.4 * k) * t + k) + (k - 1.5) * 0.003
            src += saw(f * vib, t, RNG.random())
    out = bp(src, 600, 820) + 0.7 * bp(src, 1050, 1300) + 0.4 * bright * bp(src, 2400, 2900) + 0.2 * lp(src, 500)
    return norm(out) * env(len(t), attack, release)


def string_note(f, dur, cutoff=900, decay=0.14):
    t = ts(dur)
    x = saw(f, t) + saw(f * 1.004, t, 0.3) + 0.5 * saw(f * 0.5, t, 0.6)
    x = lp(x, cutoff, 2)
    return x * np.exp(-t / decay) * env(len(t), 0.004, 0.02)


def music_box(f, dur=3.0):
    t = ts(dur)
    f = f * 2 ** (-14 / 1200)  # a little flat, like an old box
    wob = 1 + 0.0025 * np.sin(2 * np.pi * 5.5 * t)
    x = (np.sin(2 * np.pi * f * wob * t) * np.exp(-t / 0.9)
         + 0.28 * np.sin(2 * np.pi * 2 * f * t) * np.exp(-t / 0.4)
         + 0.1 * np.sin(2 * np.pi * 3.01 * f * t) * np.exp(-t / 0.2)
         + 0.07 * np.sin(2 * np.pi * 4.2 * f * t) * np.exp(-t / 0.08))
    return x * env(len(t), 0.002, 0.05)


def bell(f, dur=9.0):
    """Risset's bell: inharmonic partials, the high ones dying first."""
    t = ts(dur)
    parts = [(0.56, 1, 1), (0.56, 0.67, 0.9), (0.92, 1, 0.65), (0.92, 1.8, 0.55), (1.19, 2.67, 0.325), (1.7, 1.67, 0.35),
             (2.0, 1.46, 0.25), (2.74, 1.33, 0.2), (3.0, 1.33, 0.15), (3.76, 1, 0.1), (4.07, 1.33, 0.075)]
    out = np.zeros(len(t))
    for i, (ratio, amp, life) in enumerate(parts):
        beat = (1.0, 1.7)[i % 2] if i in (1, 3) else 0.0
        out += amp * np.sin(2 * np.pi * (f * ratio + beat) * t) * np.exp(-t / (dur * life / 4.6))
    return norm(out) * env(len(t), 0.003, 0.2)


def boom(dur=3.0, f0=150, f1=32, drive=2.0):
    t = ts(dur)
    f = f1 + (f0 - f1) * np.exp(-t / 0.07)
    body = np.tanh(drive * sine_sweep(f, t)) * np.exp(-t / 0.8)
    click = lp(RNG.standard_normal(len(t)), 900) * np.exp(-t / 0.05) * 0.8
    return norm(body + click) * env(len(t), 0.001, 0.3)


def taiko(f=80, decay=0.38, gain=1.0):
    t = ts(1.2)
    tone = sine_sweep(f * (1 + 0.7 * np.exp(-t / 0.018)), t) * np.exp(-t / decay)
    skin = bp(RNG.standard_normal(len(t)), 180, 1400) * np.exp(-t / 0.025)
    slap = bp(RNG.standard_normal(len(t)), 1500, 6000) * np.exp(-t / 0.008)
    return gain * norm(tone + 0.6 * skin + 0.35 * slap)


def clank(f=520):
    """A chain link striking another: short, metallic, inharmonic."""
    t = ts(0.5)
    out = sum(a * np.sin(2 * np.pi * f * r * t) * np.exp(-t / d) for r, a, d in
              [(1, 1, 0.09), (2.76, 0.6, 0.05), (5.4, 0.4, 0.03), (8.93, 0.3, 0.02)])
    return norm(out + 0.3 * hp(RNG.standard_normal(len(t)), 3000) * np.exp(-t / 0.01))


def chains(dur=0.8, density=26):
    out = np.zeros(int((dur + 0.5) * SR))
    for _ in range(int(dur * density)):
        at = int(RNG.random() ** 1.6 * dur * SR)
        c = clank(400 + RNG.random() * 700) * (0.3 + 0.7 * RNG.random())
        out[at:at + len(c)] += c
    return norm(out)


def tremolo(freqs, dur):
    """High strings bowed fast: the sound of something about to happen."""
    t = ts(dur)
    x = sum(saw(f * (1 + d), t, RNG.random()) for f in freqs for d in (-0.003, 0.003))
    x = bp(x, 700, 6000) * (0.6 + 0.4 * np.sin(2 * np.pi * 13 * t))
    return norm(x) * env(len(t), 0.6, 0.4)


def braam(root=hz('D1'), dur=4.5):
    t = ts(dur)
    raw = np.zeros(len(t))
    for mult, gain in ((1, 1.0), (2, 0.9), (3, 0.6), (4, 0.45), (6, 0.2)):
        for d in np.linspace(-0.006, 0.006, 5):
            raw += gain * saw(root * mult * (1 + d), t, RNG.random())
    dark, bright = lp(raw, 170), lp(raw, 2300)
    open_ = 0.12 + 0.88 * np.exp(-t / 0.45)
    x = np.tanh(2.2 * norm(dark * (1 - open_) * 1.6 + bright * open_))
    sub = np.sin(2 * np.pi * root * t) * np.exp(-t / 2.0)
    return norm(x + 0.8 * sub) * env(len(t), 0.02, dur * 0.5)


def riser(dur):
    n = int(dur * SR)
    noise = RNG.standard_normal(n)
    out = np.zeros(n)
    chunk = int(0.04 * SR)
    for i in range(0, n, chunk):
        k = i / n
        c = 250 * (30 ** k)
        seg = noise[max(0, i - 2048):i + chunk]
        out[i:i + chunk] = bp(seg, c * 0.7, c * 1.4)[-len(out[i:i + chunk]):]
    t = ts(dur)
    gliss = lp(saw(hz('D2') * 2 ** (2.5 * t / dur), t) + saw(hz('A2') * 2 ** (2.5 * t / dur), t), 3000)
    shape = (t / dur) ** 2.2
    return norm(norm(out) + 0.35 * norm(gliss)) * shape


def whoosh(dur=1.0):
    n = int(dur * SR)
    noise = RNG.standard_normal(n)
    out = np.zeros(n)
    chunk = int(0.03 * SR)
    for i in range(0, n, chunk):
        k = i / n
        c = 300 + 2600 * np.sin(np.pi * k)
        seg = noise[max(0, i - 2048):i + chunk]
        out[i:i + chunk] = bp(seg, c * 0.6, c * 1.6)[-len(out[i:i + chunk]):]
    return norm(out) * np.sin(np.pi * np.linspace(0, 1, n)) ** 2


def thunder(dur=4.0):
    """A close strike: a crack, then the sky rolling away."""
    n = int(dur * SR)
    t = ts(dur)
    crack = hp(RNG.standard_normal(n), 1200) * np.exp(-t / 0.05)
    roll = lp(RNG.standard_normal(n), 140, 4) * (1 - np.exp(-t / 0.12)) * np.exp(-t / 1.1)
    roll *= 0.6 + 0.4 * lp(np.abs(RNG.standard_normal(n)), 6, 2) / 0.8
    return norm(0.5 * norm(crack) + norm(roll))


def impact():
    """A hit landing on screen: a short low thud with a crack on top."""
    t = ts(1.2)
    body = np.tanh(2.5 * sine_sweep(45 + 140 * np.exp(-t / 0.03), t)) * np.exp(-t / 0.22)
    crack = bp(RNG.standard_normal(len(t)), 800, 5000) * np.exp(-t / 0.02)
    return norm(body + 0.45 * crack)


def heartbeat():
    t = ts(0.7)
    thump = lambda: sine_sweep(42 + 30 * np.exp(-t / 0.03), t) * np.exp(-t / 0.11)
    out = thump()
    lag = int(0.27 * SR)
    out[lag:] += 0.7 * thump()[:len(out) - lag]
    return norm(lp(out, 200))


def wind(dur):
    n = int(dur * SR)
    t = ts(dur)
    x = bp(RNG.standard_normal(n), 120, 700)
    gust = 0.55 + 0.45 * np.sin(2 * np.pi * 0.11 * t) * np.sin(2 * np.pi * 0.037 * t + 1)
    return norm(x * gust) * env(n, 2.0, 2.0)


# ------------------------------------------------------------------ the mix
class Mix:
    def __init__(self, seconds):
        n = int(seconds * SR)
        self.dry = np.zeros((2, n))
        self.wet = np.zeros((2, n))

    def add(self, at, sig, gain=1.0, pan=0.0, rev=0.25):
        i = int(at * SR)
        if i >= self.dry.shape[1]:
            return
        sig = sig[:self.dry.shape[1] - i] * gain
        l, r = np.cos((pan + 1) * np.pi / 4), np.sin((pan + 1) * np.pi / 4)
        for ch, g in ((0, l), (1, r)):
            self.dry[ch, i:i + len(sig)] += sig * g * 1.414
            self.wet[ch, i:i + len(sig)] += sig * g * 1.414 * rev

    def render(self, silence=None):
        rt = 3.6
        t = ts(rt)
        ir = []
        for ch in range(2):
            n = RNG.standard_normal(len(t)) * np.exp(-6.9 * t / rt)
            n[:int(0.012 * SR)] *= np.linspace(0, 1, int(0.012 * SR))
            ir.append(lp(n, 3500, 2))
        wet = np.stack([fftconvolve(self.wet[ch], ir[ch])[:self.wet.shape[1]] for ch in range(2)])
        out = self.dry + norm(wet, np.max(np.abs(self.dry)) * 0.55)
        if silence:  # a hard cut, tails and all
            a, b = int(silence[0] * SR), int(silence[1] * SR)
            out[:, a:b] = 0
            fade = int(0.01 * SR)
            out[:, a - fade:a] *= np.linspace(1, 0, fade)
        out = hp(out, 25, 2)
        out = out - 0.35 * lp(out, 70, 2) + 0.45 * bp(out, 1800, 5500)  # less mud, more bite on small speakers
        out = np.tanh(1.3 * out / np.max(np.abs(out))) / np.tanh(1.3)
        return norm(out, 0.89)


# ------------------------------------------------------------------ the arrangement
CHORDS = {  # (ostinato root, choir voicing)
    'Dm': ('D2', ['D4', 'F4', 'A4']), 'Bb': ('Bb1', ['D4', 'F4', 'Bb4']),
    'Gm': ('G1', ['D4', 'G4', 'Bb4']), 'A': ('A1', ['C#4', 'E4', 'A4']),
}
LULLABY = [('A4', 0), ('D5', 2), ('F5', 4), ('E5', 6), ('D5', 8), ('C5', 9), ('A4', 10), ('Bb4', 14), ('A4', 15),
           ('G4', 16), ('F4', 18), ('E4', 20), ('F4', 22), ('A4', 23), ('D4', 24)]


def ostinato(m, start, end, root, gain, cutoff, sixteenths=False):
    step = BEAT / 4 if sixteenths else BEAT / 2
    pattern = [0, 0, 12, 0, 0, 12, 7, 12] if not sixteenths else [0, 0, 12, 0, 0, 12, 0, 7, 0, 0, 12, 0, 0, 12, 7, 12]
    f0 = hz(root)
    i, t = 0, start
    while t < end - 1e-6:
        f = f0 * 2 ** (pattern[i % len(pattern)] / 12)
        accent = 1.0 if i % (4 if sixteenths else 2) == 0 else 0.72
        m.add(t, string_note(f, step * 1.6, cutoff, 0.09 if sixteenths else 0.15), gain * accent, pan=(-0.25, 0.25)[i % 2], rev=0.12)
        i += 1
        t += step


def compose(hits=()):
    """hits: seconds where a blow lands on screen (from edit.py), each gets its own thud."""
    m = Mix(LENGTH)
    for at in hits:
        m.add(at, impact(), 0.45, rev=0.2)
    # intro
    m.add(0.0, wind(37.5), 0.1, rev=0.1)
    m.add(0.4, drone([hz('D1'), hz('A1'), hz('D2')], 12.5, 150), 0.2, rev=0.15)
    m.add(9.5, drone([hz('D1'), hz('A1'), hz('D2')], 28.0, 200), 0.3, rev=0.15)
    for note, beat in LULLABY[:8]:
        m.add(1.25 + beat * BEAT, music_box(hz(note)), 0.16, pan=0.2, rev=0.55)
    m.add(4.3, whoosh(0.9), 0.12, rev=0.3)
    m.add(5.77, thunder(4.5), 0.42, pan=0.3, rev=0.3)  # the storm: timed to the lightning in a1_storm2
    m.add(7.9, thunder(3.0), 0.32, pan=-0.3, rev=0.3)
    m.add(8.2, chains(0.9), 0.2, pan=-0.1, rev=0.4)  # the rite: the relic's chain gives way
    m.add(8.27, boom(3.0, 110, 30), 0.35, rev=0.35)
    m.add(7.95, whoosh(1.1), 0.2, rev=0.4)
    m.add(9.6, whoosh(0.5), 0.1)
    m.add(10.0, bell(hz('D3'), 10), 0.55, rev=0.7)
    m.add(10.0, boom(4.0, 120, 28, 2.5), 0.55, rev=0.5)
    # the Underworld
    for k, name in enumerate(['Dm', 'Bb', 'Gm', 'A', 'Dm']):
        root, voicing = CHORDS[name]
        at = bar(5 + 2 * k)
        m.add(at, choir([hz(v) for v in voicing], 2 * BAR + 1.5, 1.6, 1.8), 0.15 + 0.03 * k, pan=0.0, rev=0.5)
        ostinato(m, at, at + 2 * BAR, root, 0.08 + 0.03 * k, 900 + 300 * k)
    for note, beat in LULLABY[8:]:
        m.add(bar(5) + 1.25 + (beat - 8) * BEAT, music_box(hz(note)), 0.14, pan=0.2, rev=0.6)
    m.add(22.2, whoosh(0.7), 0.12)
    m.add(22.5, boom(2.5, 100, 30), 0.3, rev=0.4)
    m.add(22.5, bell(hz('A3'), 7), 0.18, rev=0.7)
    for k in range(10):
        m.add(31.25 + k * 2 * BEAT, heartbeat(), 0.35 + 0.03 * k, rev=0.1)
    m.add(35.0, riser(2.5), 0.25, rev=0.2)
    # danger
    m.add(37.5, boom(3.0, 140, 30, 2.5), 0.6, rev=0.4)
    m.add(37.5, chains(0.5), 0.12, rev=0.3)
    m.add(37.5, drone([hz('D1'), hz('A1')], 13.0, 220), 0.35, rev=0.1)
    for k, (name, bars) in enumerate([('Dm', 2), ('Bb', 2), ('A', 1)]):
        root, voicing = CHORDS[name]
        at = bar(15 + 2 * k)
        m.add(at, choir([hz(v) for v in voicing], bars * BAR + 1.0, 0.8, 1.0), 0.3, rev=0.5)
        ostinato(m, max(at, 40.0), at + bars * BAR, root, 0.22, 2600, sixteenths=True)
        m.add(at, tremolo([hz(voicing[-1]) * 2, hz(voicing[0]) * 2], bars * BAR + 0.5), 0.12, pan=0.3, rev=0.4)
        if at < 40.0:
            ostinato(m, at, 40.0, root, 0.18, 1200)
    for b in range(16, 20):
        for beat in (0, 2):
            m.add(bar(b, beat), taiko(80), 0.5, rev=0.25)
        m.add(bar(b, 3.5), taiko(110, 0.2), 0.25, pan=0.3, rev=0.2)
    for k in range(16):  # tom roll into the bosses
        m.add(bar(19) + k * BEAT / 4, taiko(95 + 3 * k, 0.15), 0.12 + 0.025 * k, pan=(-0.3, 0.3)[k % 2], rev=0.2)
    m.add(46.25, riser(3.75), 0.4, rev=0.2)
    # the bosses
    for k, name in enumerate(['Dm', 'Bb', 'Gm', 'A', 'Dm']):
        root, voicing = CHORDS[name]
        at = bar(20 + 2 * k)
        m.add(at, braam(hz('D1') if name == 'Dm' else hz(root), 4.6), 0.75, rev=0.35)
        m.add(at, boom(3.0, 150, 30, 3.0), 0.5, rev=0.3)
        m.add(at, choir([hz(v) * 2 for v in voicing] + [hz(v) for v in voicing], 2 * BAR + 0.6, 0.3, 0.8, 1.6), 0.36, rev=0.5)
        ostinato(m, at, at + 2 * BAR, root, 0.3, 3200, sixteenths=True)
        m.add(at, tremolo([hz(v) * 2 for v in voicing], 2 * BAR + 0.4), 0.16, pan=-0.3, rev=0.4)
    m.add(50.0, drone([hz('D1'), hz('A1'), hz('D2')], 25.2, 300), 0.4, rev=0.1)
    for b in range(20, 30):
        for six, g in ((0, 1.0), (3, 0.6), (6, 0.7), (8, 0.9), (11, 0.6), (14, 0.7)):
            m.add(bar(b) + six * BEAT / 4, taiko(78 if six in (0, 8) else 100, 0.3), 0.55 * g, pan=((six % 3) - 1) * 0.3, rev=0.22)
        for beat in (1, 3):
            m.add(bar(b, beat), clank(640 + 40 * (b % 3)), 0.09, pan=0.4, rev=0.4)
    m.add(57.5, boom(2.0, 160, 32, 3), 0.4, rev=0.3)
    m.add(57.5, chains(0.6), 0.14, rev=0.35)
    for k in range(32):
        m.add(bar(28) + k * BEAT / 4, taiko(90 + 2 * k, 0.12), 0.15 + 0.012 * k, pan=(-0.35, 0.35)[k % 2], rev=0.2)
    m.add(70.0, riser(4.95), 0.55, rev=0.15)
    # the title
    title = 75.625
    m.add(title, braam(hz('D1'), 7.0), 1.0, rev=0.5)
    m.add(title, boom(5.0, 160, 26, 3.5), 0.85, rev=0.6)
    m.add(title, bell(hz('D3'), 11), 0.55, rev=0.8)
    m.add(title, chains(0.7), 0.2, rev=0.5)
    m.add(title, choir([hz(v) for v in ['D3', 'A3', 'D4', 'F4', 'A4', 'D5']], 7.5, 0.05, 5.5, 1.4), 0.4, rev=0.6)
    m.add(title, drone([hz('D1'), hz('A1')], 12.0, 140), 0.35, rev=0.2)
    m.add(81.25, bell(hz('A3'), 7), 0.22, rev=0.8)
    for note, at in (('A4', 83.9), ('D5', 84.5), ('F5', 85.1), ('D5', 85.75)):
        m.add(at, music_box(hz(note), 3.5), 0.16, pan=0.2, rev=0.7)
    m.add(85.75, chains(0.5), 0.07, rev=0.6)
    return m.render(silence=(75.0, title))


def write_wav(path, stereo):
    import wave
    pcm = (np.clip(stereo.T, -1, 1) * 32767).astype('<i2')
    with wave.open(str(path), 'wb') as w:
        w.setnchannels(2)
        w.setsampwidth(2)
        w.setframerate(SR)
        w.writeframes(pcm.tobytes())


if __name__ == '__main__':
    out = Path(__file__).resolve().parents[2] / 'build' / 'trailer'
    out.mkdir(parents=True, exist_ok=True)
    write_wav(out / 'score.wav', compose())
    print('wrote', out / 'score.wav')
