"""HD block textures (32x32): height-field surfaces lit from the top-left, with cavity occlusion, settled ash and stain streaks.
Every field wraps, so textures tile seamlessly."""
import math
import numpy as np
from core import Ramp, hx, BAYER4

N = 32
rng = np.random.default_rng


# ------------------------------------------------------------------ tileable fields
def value_noise(seed, cells, n=N):
    r = rng(seed)
    g = r.random((cells, cells))
    ys, xs = np.mgrid[0:n, 0:n] / n * cells
    x0, y0 = np.floor(xs).astype(int), np.floor(ys).astype(int)
    tx, ty = xs - x0, ys - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
    x1, y1 = (x0 + 1) % cells, (y0 + 1) % cells
    x0, y0 = x0 % cells, y0 % cells
    a, b, c, d = g[y0, x0], g[y0, x1], g[y1, x0], g[y1, x1]
    return (a + (b - a) * tx) * (1 - ty) + (c + (d - c) * tx) * ty


def fbm(seed, base=4, octaves=4, n=N):
    out = np.zeros((n, n))
    amp, tot = 1.0, 0.0
    for o in range(octaves):
        cells = base * 2 ** o
        if cells > n:
            break
        out += value_noise(seed + o * 101, cells, n) * amp
        tot += amp
        amp *= 0.5
    return out / tot * 2 - 1


def worley(seed, points, n=N):
    """F1, F2 distances and the nearest point id, wrapped."""
    r = rng(seed)
    p = r.random((points, 2)) * n
    ys, xs = np.mgrid[0:n, 0:n]
    best = np.full((n, n), 1e9)
    second = np.full((n, n), 1e9)
    ident = np.zeros((n, n), dtype=int)
    for i, (px, py) in enumerate(p):
        dx = np.abs(xs - px)
        dy = np.abs(ys - py)
        dx = np.minimum(dx, n - dx)
        dy = np.minimum(dy, n - dy)
        d = np.sqrt(dx * dx + dy * dy)
        closer = d < best
        second = np.where(closer, best, np.minimum(second, d))
        ident = np.where(closer, i, ident)
        best = np.where(closer, d, best)
    return best, second, ident


def blur(h, r=2):
    out = np.zeros_like(h)
    k = 0
    for dy in range(-r, r + 1):
        for dx in range(-r, r + 1):
            w = math.exp(-(dx * dx + dy * dy) / (r * r))
            out += np.roll(np.roll(h, dy, 0), dx, 1) * w
            k += w
    return out / k


# ------------------------------------------------------------------ lighting
L = np.array([-0.55, -0.62, 0.56])
L = L / np.linalg.norm(L)


def light(h, relief=3.0):
    gx = (np.roll(h, -1, 1) - np.roll(h, 1, 1)) * 0.5
    gy = (np.roll(h, -1, 0) - np.roll(h, 1, 0)) * 0.5
    nx, ny, nz = -gx * relief, -gy * relief, np.ones_like(h)
    norm = np.sqrt(nx * nx + ny * ny + nz * nz)
    lam = (nx * L[0] + ny * L[1] + nz * L[2]) / norm
    return lam - L[2]   # 0 on flat ground, + lit slopes, - shadowed slopes


def cavity(h, r=2):
    return np.clip(blur(h, r) - h, 0, None)


def render(level, ramp, dither=0.18, tint=None):
    """level: float field of ramp indices. tint: optional (mask 0..1, rgb) list blended on top."""
    img = np.zeros((level.shape[0], level.shape[1], 4), dtype=np.uint8)
    for y in range(level.shape[0]):
        for x in range(level.shape[1]):
            c = list(ramp.at(level[y, x], x, y, dither))
            if tint:
                for mask, rgb in tint:
                    m = mask[y, x]
                    if m > 0.01:
                        c[0] = int(c[0] * (1 - m) + rgb[0] * m)
                        c[1] = int(c[1] * (1 - m) + rgb[1] * m)
                        c[2] = int(c[2] * (1 - m) + rgb[2] * m)
            img[y, x] = c
    return img


def surface(h, albedo, ramp, relief=3.0, shade=2.6, ao=6.0, dust=None, dither=0.18, extra_tint=None):
    lam = light(h, relief)
    cav = cavity(h)
    level = albedo + lam * shade - cav * ao
    tints = []
    if dust is not None:
        tints.append((np.clip(cav * dust, 0, 0.55), hx('#7c7884')[:3]))
    if extra_tint:
        tints += extra_tint
    return render(level, ramp, dither, tints)


# ------------------------------------------------------------------ palettes
BRICK = Ramp('#0b090e', '#121016', '#19161e', '#211d27', '#2a2531', '#342d3c', '#3e3647', '#494053', '#554a60', '#62566e', '#72657e')
BRICK_GREY = Ramp('#0f0e13', '#18161d', '#211f28', '#2b2833', '#36323f', '#423d4c', '#4f495a', '#5d5669', '#6c6479', '#7d758a', '#928a9e')
ROCK = Ramp('#121116', '#1a191f', '#232229', '#2d2b34', '#38353f', '#43404b', '#4f4b58', '#5c5866', '#6b6675', '#7c7786')
DARK = Ramp('#08070a', '#0e0d12', '#15131a', '#1c1a22', '#24212b', '#2c2934', '#36323e', '#413c4a')
SOIL = Ramp('#141016', '#1c171f', '#251f29', '#2f2734', '#3a3140', '#463b4d', '#53475b', '#62556a')
WOOD = Ramp('#2a2630', '#36313d', '#433d4b', '#514a5a', '#605869', '#706879', '#827a8b', '#958d9d', '#a8a1b0')
GLOW = Ramp('#3c1170', '#6020b0', '#8a3cf0', '#b070ff', '#d6aaff', '#f2e6ff')


def stains(seed, mortar_rows, strength=1.0):
    """Darker streaks running down from horizontal joints (water and soul-rot)."""
    r = rng(seed)
    s = np.zeros((N, N))
    for row in mortar_rows:
        for x in range(N):
            if r.random() < 0.18:
                length = r.integers(3, 11)
                for k in range(length):
                    s[(row + 1 + k) % N, x] += (1 - k / length) * 0.6 * strength
    return blur(s, 1) * 1.6


# ------------------------------------------------------------------ bricks family
def brick_field(seed, bw=16, bh=8, chip=0.35):
    """Bricks whose edges are worn ragged by noise, faces pitted, each laid with its own slight tilt and tone."""
    r = rng(seed)
    h = np.zeros((N, N))
    alb = np.zeros((N, N))
    grey = np.zeros((N, N), dtype=bool)
    rough = fbm(seed + 1, 4, 4)
    wear = fbm(seed + 4, 8, 3)
    fine = fbm(seed + 2, 16, 2)
    pf1, pf2, pid = worley(seed + 5, 40)
    pits = np.clip(1 - pf1 / 1.3, 0, 1) * (r.random(40)[pid] < 0.4)
    tones = r.random((N // bh, N // bw + 2)) * 0.9 - 0.45
    tilts = r.random((N // bh, N // bw + 2, 2)) * 0.1 - 0.05
    greys = r.random((N // bh, N // bw + 2)) < 0.15
    for y in range(N):
        row = y // bh
        off = 0 if row % 2 == 0 else bw // 2
        for x in range(N):
            col = (x + off) // bw
            bx, by = (x + off) % bw, y % bh
            e = min(bx, bw - 2 - bx, by, bh - 2 - by) + wear[y, x] * 1.1 * (0.45 + chip)
            if bx == bw - 1 or by == bh - 1 or e < -0.2:
                h[y, x] = 0.05 + rough[y, x] * 0.05
                alb[y, x] = 1.3
                continue
            edge = 1 - math.exp(-(max(e, 0) + 0.5) * 1.15)
            t = tilts[row, col]
            h[y, x] = 0.5 + edge * 0.48 + rough[y, x] * 0.1 + fine[y, x] * 0.05 - pits[y, x] * 0.22                 + (bx - bw / 2) * t[0] + (by - bh / 2) * t[1]
            alb[y, x] = 5.1 + tones[row, col] + rough[y, x] * 0.5 + fine[y, x] * 0.3 + (0.3 if by < 2 else 0)
            grey[y, x] = greys[row, col]
    return h, alb, grey


def bricks_tex(seed, ramp=BRICK, grey_ramp=BRICK_GREY, chip=0.35, cracks=0, veins=0):
    h, alb, grey = brick_field(seed, chip=chip)
    r = rng(seed + 9)
    glow = np.zeros((N, N, 4), dtype=np.uint8)
    def walk(n_steps, depth, glow_on):
        x, y = r.integers(0, N), r.integers(0, N)
        for _ in range(n_steps):
            h[y % N, x % N] -= depth
            if glow_on:
                c = GLOW.cols[4 if r.random() < 0.3 else 3]
                glow[y % N, x % N] = c
            y += 1
            x += r.integers(-1, 2)
    for _ in range(cracks):
        walk(int(r.integers(10, 22)), 0.55, False)
    for _ in range(veins):
        walk(int(r.integers(14, 26)), 0.45, True)
    st = stains(seed + 3, [7, 15, 23, 31])
    alb = alb - st * 1.4
    lam = light(h, 3.2)
    cav = cavity(h)
    level = alb + lam * 2.8 - cav * 6.5
    img = np.zeros((N, N, 4), dtype=np.uint8)
    dust = np.clip(cav * 5.0, 0, 0.5)
    for y in range(N):
        for x in range(N):
            rp = grey_ramp if grey[y, x] else ramp
            c = list(rp.at(level[y, x], x, y, 0.16))
            m = dust[y, x]
            if m > 0.02:
                d = hx('#6e6a78')
                c = [int(c[i] * (1 - m) + d[i] * m) for i in range(3)] + [255]
            if glow[y, x, 3]:
                c = list(hx('#5d24a8'))
            img[y, x] = c
    return (img, glow) if veins else img


def tiles_tex(seed):
    rough = fbm(seed, 4, 4)
    fine = fbm(seed + 1, 16, 2)
    r = rng(seed)
    tone = r.random((2, 2)) * 1.0 - 0.5
    h = np.zeros((N, N))
    alb = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            tx, ty = x % 16, y % 16
            e = min(tx, 14 - tx, ty, 14 - ty)
            if tx == 15 or ty == 15:
                h[y, x] = 0.05
                alb[y, x] = 1.0
                continue
            bevel = min(1.0, (e + 0.5) / 2.5)
            wear = -0.12 * math.exp(-((tx - 7) ** 2 + (ty - 7) ** 2) / 30)
            h[y, x] = 0.4 + bevel * 0.5 + wear + rough[y, x] * 0.08 + fine[y, x] * 0.04
            alb[y, x] = 5.2 + tone[y // 16, x // 16] + rough[y, x] * 0.35
    # an engraved ring on each tile
    for y in range(N):
        for x in range(N):
            tx, ty = x % 16 - 7, y % 16 - 7
            if abs(math.hypot(tx, ty) - 4.6) < 0.55:
                h[y, x] -= 0.18
    return surface(h, alb - stains(seed + 2, [15, 31], 0.6), BRICK, relief=3.0, shade=2.6, ao=6.0, dust=4.0)


def polished_tex(seed):
    rough = fbm(seed, 2, 4)
    h = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            e = min(x, N - 1 - x, y, N - 1 - y)
            h[y, x] = 0.3 + min(1.0, (e + 0.5) / 3.0) * 0.6 + rough[y, x] * 0.05
            if e == 5:
                h[y, x] -= 0.08   # inset panel line
    alb = 5.6 + rough * 0.6 - (np.mgrid[0:N, 0:N][0] + np.mgrid[0:N, 0:N][1]) * 0.012
    return surface(h, alb, BRICK, relief=3.2, shade=2.8, ao=5.0, dust=3.0)


SKULL = ["......XXXXXXXXXX......",
         "....XXXXXXXXXXXXXX....",
         "...XXXXXXXXXXXXXXXX...",
         "..XXXXXXXXXXXXXXXXXX..",
         "..XXXXXXXXXXXXXXXXXX..",
         ".XXXXXXXXXXXXXXXXXXXX.",
         ".XXXoooooXXXXoooooXXX.",
         ".XXoooooooXXoooooooXX.",
         ".XXooEEoooXXoooEEooXX.",
         ".XXooEEoooXXoooEEooXX.",
         ".XXXooooXXXXXXooooXXX.",
         "..XXXXXXXXooXXXXXXXX..",
         "..XXXXXXXooooXXXXXXX..",
         "...XXXXXXooooXXXXXX...",
         "....XXXXXXXXXXXXXX....",
         "....XtXtXtXtXtXtXX....",
         "....XtXtXtXtXtXtXX....",
         ".....XXXXXXXXXXXX.....",
         "......XXXXXXXXXX......"]


def chiseled_tex(seed):
    rough = fbm(seed, 2, 4)
    h = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            e = min(x, N - 1 - x, y, N - 1 - y)
            h[y, x] = 0.3 + min(1.0, (e + 0.5) / 3.0) * 0.4 + rough[y, x] * 0.04
    glow = np.zeros((N, N, 4), dtype=np.uint8)
    ox, oy = 5, 7
    for r_, row in enumerate(SKULL):
        for c_, ch in enumerate(row):
            x, y = ox + c_, oy + r_
            if ch == 'X':
                h[y, x] += 0.45
            elif ch == 't':
                h[y, x] += 0.3
            elif ch == 'o':
                h[y, x] -= 0.25
            elif ch == 'E':
                h[y, x] -= 0.1
                glow[y, x] = GLOW.cols[3] if (r_ + c_) % 2 == 0 else GLOW.cols[2]
    for y in range(N):
        for x in range(N):
            if glow[y, x, 3] == 0 and 0 < y < N - 1 and 0 < x < N - 1 and (glow[y - 1, x, 3] or glow[y + 1, x, 3] or glow[y, x - 1, 3] or glow[y, x + 1, 3]):
                if h[y, x] < 0.6:
                    glow[y, x] = (*GLOW.cols[1][:3], 150)
    alb = 5.2 + rough * 0.5
    img = surface(h, alb, BRICK, relief=3.4, shade=2.8, ao=7.0, dust=4.0)
    img[glow[:, :, 3] > 0] = hx('#4a1a80')
    return img, glow


def pillar_side_tex(seed):
    rough = fbm(seed, 4, 4)
    h = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            flute = 0.5 + 0.5 * math.cos((x + 0.5) * 2 * math.pi / 8)
            h[y, x] = 0.4 + flute * 0.35 + rough[y, x] * 0.08
            if y < 4 or y > N - 5:
                h[y, x] = 0.9 - (0.25 if y in (3, N - 4) else 0) + rough[y, x] * 0.05
    return surface(h, 5.0 + rough * 0.4 - stains(seed + 1, [4], 0.7), BRICK, relief=3.0, shade=2.6, ao=6.0, dust=4.0)


def pillar_top_tex(seed):
    rough = fbm(seed, 4, 4)
    h = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            d = max(abs(x - 15.5), abs(y - 15.5))
            h[y, x] = 0.5 + 0.25 * math.cos(d * 2 * math.pi / 5) + rough[y, x] * 0.06 - (0.3 if d > 14.5 else 0)
    return surface(h, 5.0 + rough * 0.4, BRICK, relief=2.6, shade=2.4, ao=5.0, dust=3.0)


# ------------------------------------------------------------------ natural
def rock_tex(seed, ramp=ROCK, points=11, base=4.6):
    f1, f2, ident = worley(seed, points)
    rough = fbm(seed + 1, 4, 4)
    fine = fbm(seed + 2, 16, 2)
    r = rng(seed)
    tones = r.random(points) * 1.2 - 0.6
    edge = np.clip((f2 - f1) / 3.0, 0, 1)
    h = np.sqrt(edge) * 0.8 + rough * 0.18 + fine * 0.06
    alb = base + tones[ident] + rough * 0.6
    ys = np.mgrid[0:N, 0:N][0]
    strata = np.sin(ys * 2 * math.pi / N * 3 + rough * 2) * 0.35
    img = surface(h, alb + strata, ramp, relief=3.4, shade=2.6, ao=6.5, dust=3.5)
    # faint soul seams in the deepest cracks
    for y in range(N):
        for x in range(N):
            if edge[y, x] < 0.08 and r.random() < 0.18:
                img[y, x] = hx('#46295f')
    return img


def soil_top_tex(seed):
    f1, f2, ident = worley(seed, 26)
    rough = fbm(seed + 1, 4, 4)
    r = rng(seed)
    pebble = np.clip(1 - f1 / 2.2, 0, 1) * (r.random(26)[ident] < 0.45)
    h = rough * 0.3 + pebble * 0.6
    alb = 4.0 + rough * 0.8 + pebble * 0.8
    img = surface(h, alb, SOIL, relief=3.0, shade=2.4, ao=5.0, dust=3.0)
    ash = fbm(seed + 3, 4, 3)
    for y in range(N):
        for x in range(N):
            if ash[y, x] > 0.35 and r.random() < 0.7:
                c = img[y, x]
                img[y, x] = (min(255, c[0] + 26), min(255, c[1] + 25), min(255, c[2] + 28), 255)
            if r.random() < 0.012:
                img[y, x] = hx('#7a3cc2')
    return img


def soil_side_tex(seed, top):
    rough = fbm(seed, 4, 4)
    f1, f2, ident = worley(seed + 5, 18)
    r = rng(seed)
    stone = np.clip(1 - f1 / 2.4, 0, 1) * (r.random(18)[ident] < 0.35)
    h = rough * 0.25 + stone * 0.5
    img = surface(h, 3.4 + rough * 0.7 + stone * 0.9, SOIL, relief=3.0, shade=2.3, ao=5.0)
    for x in range(N):
        depth = 4 + int((fbm(seed + 9, 8, 2)[0, x] + 1) * 3)
        img[0:depth, x] = top[0:depth, x]
        img[depth, x] = (int(img[depth, x][0] * 0.6), int(img[depth, x][1] * 0.6), int(img[depth, x][2] * 0.65), 255)
    return img


def planks_tex(seed):
    rough = fbm(seed, 4, 4)
    r = rng(seed)
    h = np.zeros((N, N))
    alb = np.zeros((N, N))
    for y in range(N):
        board = y // 8
        by = y % 8
        tone = (r.random() if False else 0)
        for x in range(N):
            grain = math.sin((x * 0.35 + board * 3.1) + math.sin(x * 0.11 + board) * 2.0 + by * 0.9) * 0.5
            e = min(by, 6 - by)
            h[y, x] = (0.05 if by == 7 else 0.5 + min(1, (e + 0.5) / 2) * 0.35) + grain * 0.06 + rough[y, x] * 0.05
            alb[y, x] = 4.6 + grain * 0.7 + ((board * 37) % 5) * 0.2 - 0.4 + rough[y, x] * 0.4
    img = surface(h, alb, WOOD, relief=3.0, shade=2.4, ao=5.5, dust=3.0)
    for board in range(4):   # iron nails
        for nx in (3, 28):
            y = board * 8 + 3
            img[y, nx] = hx('#1a171e')
            img[y - 1, nx] = hx('#5c5866')
    return img


def log_side_tex(seed):
    rough = fbm(seed, 4, 4)
    ys, xs = np.mgrid[0:N, 0:N]
    ridges = np.abs(np.sin((xs + rough * 5 + np.sin(ys * 0.2) * 1.5) * 2 * math.pi / 7))
    f1, f2, ident = worley(seed + 3, 6)
    knots = np.clip(1 - f1 / 2.5, 0, 1) * (ident % 3 == 0)
    h = ridges * 0.75 + rough * 0.2 - knots * 0.5
    return surface(h, 3.0 + rough * 0.6 + ridges * 0.9 - knots * 1.5, WOOD, relief=3.8, shade=2.8, ao=8.0, dust=1.5)


def log_top_tex(seed):
    rough = fbm(seed, 4, 3)
    ys, xs = np.mgrid[0:N, 0:N]
    d = np.sqrt((xs - 15.5) ** 2 + (ys - 15.5) ** 2)
    rings = 0.5 + 0.5 * np.cos(d * 2 * math.pi / 3.2 + rough * 2)
    h = np.where(d > 14.5, 0.9 + rough * 0.2, rings * 0.25 + 0.3)
    alb = np.where(d > 14.5, 2.4 + rough, 4.6 + rings * 0.7)
    img = surface(h, alb, WOOD, relief=2.6, shade=2.2, ao=4.0)
    for k in range(3):   # radial check cracks
        a = k * 2.1 + 0.4
        for t in range(3, 13):
            x, y = int(15.5 + math.cos(a) * t), int(15.5 + math.sin(a) * t)
            img[y, x] = hx('#26222b')
    return img


SEAL = Ramp('#060309', '#0d0616', '#170a27', '#24103d', '#361758', '#4d2180', '#6d33b0', '#9a62e0', '#c9a4ff', '#efe2ff')


def seal_frames(seed=77, n=16):
    """The Soul Seal: a membrane of dark smoke rising forever, lit by thin soul streaks. Loops every n frames."""
    a, b, c = fbm(seed, 2, 4), fbm(seed + 1, 4, 3), fbm(seed + 2, 2, 3)
    frames = []
    for f in range(n):
        s1, s2, s3 = f * N // n, f * 2 * N // n, f * N // n
        lay1 = np.roll(a, -s1, 0)          # slow body of smoke drifting up
        lay2 = np.roll(b, -s2, 0)          # faster wisps
        warp = np.roll(c, s3, 1)           # sideways sway
        v = lay1 * 0.6 + lay2 * 0.4 + warp * 0.25
        streak = np.clip(1 - np.abs(lay2 + warp * 0.5) * 6, 0, 1) ** 2   # thin bright veins where wisps cross zero
        level = 2.7 + v * 2.4 + streak * 4.0
        img = np.zeros((N, N, 4), dtype=np.uint8)
        for y in range(N):
            for x in range(N):
                col = SEAL.at(level[y, x], x, y, 0.2)
                img[y, x] = (col[0], col[1], col[2], int(np.clip(170 + level[y, x] * 12, 150, 245)))
        frames.append(img)
    return np.concatenate(frames, axis=0)
