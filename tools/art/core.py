"""DeathBound art pipeline core.

One model spec drives three outputs so they can never disagree:
  * the entity texture (painted per cube face),
  * the Java mesh code (CubeListBuilder / PartPose),
  * a preview render that reproduces Minecraft's ModelPart UV + lighting math.
"""
import math
import zlib
import numpy as np
from PIL import Image, ImageDraw


# ----------------------------------------------------------------------------- color
def hx(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


class Ramp:
    """Dark -> light list of colors. Index with a float level; quantised with ordered dither."""

    def __init__(self, *hexes):
        self.cols = [hx(h) for h in hexes]
        self.n = len(self.cols)

    def at(self, level, x=0, y=0, dither=0.35):
        level = max(0.0, min(self.n - 1.0, level))
        base = int(math.floor(level))
        frac = level - base
        t = 0.5 + (BAYER4[y & 3][x & 3] - 0.5) * dither * 2
        idx = base + (1 if frac > t else 0)
        return self.cols[min(idx, self.n - 1)]


BAYER4 = [[(v + 0.5) / 16 for v in row] for row in
          [[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]]]

# Hue-shifted ramps: shadows lean violet, highlights lean warm.
BONE = Ramp('#1c1820', '#38313d', '#5b5258', '#82776c', '#a69a86', '#c8bda5', '#e3dac4')
DARK_BONE = Ramp('#0d0c11', '#19171f', '#26232c', '#35313c', '#47424e', '#5c5764', '#76717d', '#948f99')
RAG = Ramp('#0f0e14', '#18171e', '#222129', '#2c2b34', '#383640', '#45424e', '#55515f', '#67626f')
ROBE = Ramp('#060509', '#0e0c13', '#16131c', '#1f1b27', '#2a2534', '#373042', '#463e54')
PURPLE = Ramp('#170a22', '#260f3a', '#381753', '#4c206f', '#62298d', '#7b36ad', '#9850cc')
SOUL = Ramp('#2c0b58', '#4f17a0', '#7a2fe6', '#a061ff', '#c597ff', '#e5cfff', '#fbf6ff')
STEEL = Ramp('#0c0c10', '#17171d', '#22222a', '#2f2f38', '#3f3f4a', '#53535f', '#6c6c79', '#8d8d9a', '#b2b2bd')
RUST = Ramp('#1f110b', '#361d12', '#52301c', '#714225', '#8f5730', '#ad713c', '#c48c52')
WOOD = Ramp('#140d09', '#22170f', '#332317', '#46311f', '#5a4029', '#6f5134')
LEATHER = Ramp('#110b0b', '#1e1414', '#2d1e1d', '#3e2a27', '#523833')
CLEAR = (0, 0, 0, 0)


# ----------------------------------------------------------------------------- noise
class Noise:
    """Deterministic value noise; `cluster` gives chunky 2px pixel-art clumps."""

    def __init__(self, seed):
        self.seed = seed

    def _h(self, x, y, s=0):
        n = (x * 374761393 + y * 668265263 + (self.seed + s) * 2246822519) & 0xFFFFFFFF
        n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
        return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0

    def white(self, x, y, s=0):
        return self._h(x, y, s) * 2 - 1

    def cluster(self, x, y, size=2, s=0):
        return self._h(x // size, y // size, s) * 2 - 1

    def smooth(self, x, y, scale=4.0, s=0):
        fx, fy = x / scale, y / scale
        x0, y0 = math.floor(fx), math.floor(fy)
        tx, ty = fx - x0, fy - y0
        tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
        a, b = self._h(x0, y0, s), self._h(x0 + 1, y0, s)
        c, d = self._h(x0, y0 + 1, s), self._h(x0 + 1, y0 + 1, s)
        return ((a + (b - a) * tx) + ((c + (d - c) * tx) - (a + (b - a) * tx)) * ty) * 2 - 1


# ----------------------------------------------------------------------------- model spec
FACES = ('top', 'bottom', 'right', 'front', 'left', 'back')
# directional light per face as seen in game (top lit, bottom dark); painted subtly so in-game shading still reads
FACE_LIGHT = {'top': 0.55, 'bottom': -0.9, 'right': -0.15, 'front': 0.0, 'left': -0.15, 'back': -0.35}


class Bone:
    def __init__(self, name, parent, pivot, rot=(0, 0, 0)):
        self.name, self.parent, self.pivot, self.rot = name, parent, pivot, rot  # rot in degrees


class Cube:
    def __init__(self, bone, origin, size, paint, inflate=0.0, mirror=False, uv=None, share=None, name=None, mat=None):
        self.bone, self.origin, self.size, self.mat = bone, origin, size, mat  # mat='skin' keeps the HD pass off bone/steel looks
        self.paint, self.inflate, self.mirror = paint, inflate, mirror
        self.uv, self.share, self.name = uv, share, name

    @property
    def uv_size(self):
        w, h, d = self.size
        return 2 * (d + w), d + h


class Model:
    def __init__(self, name, tex_w, tex_h, scale=2):
        self.name, self.tw, self.th, self.scale = name, tex_w, tex_h, scale
        self.bones, self.cubes, self.by_name = [], [], {}

    def bone(self, name, parent, pivot, rot=(0, 0, 0)):
        b = Bone(name, parent, pivot, rot)
        self.bones.append(b)
        self.by_name[name] = b
        return name

    def cube(self, bone, origin, size, paint, **kw):
        c = Cube(bone, origin, size, paint, **kw)
        self.cubes.append(c)
        return c

    # -- UV packing (skyline over integer columns)
    def pack(self):
        sky = [0] * self.tw
        todo = sorted([c for c in self.cubes if c.uv is None and c.share is None],
                      key=lambda c: (-c.uv_size[1], -c.uv_size[0]))
        for c in todo:
            rw, rh = c.uv_size
            best = None
            for x in range(0, self.tw - rw + 1):
                y = max(sky[x:x + rw]) if rw else sky[x]
                if y + rh <= self.th and (best is None or y < best[1]):
                    best = (x, y)
            if best is None:
                raise RuntimeError(f'{self.name}: texture {self.tw}x{self.th} too small for cube {c.size} on {c.bone}')
            c.uv = best
            for x in range(best[0], best[0] + rw):
                sky[x] = best[1] + rh
        for c in self.cubes:
            if c.share is not None:
                c.uv = c.share.uv

    # -- painting
    def paint(self):
        self.pack()
        img = np.zeros((self.th, self.tw, 4), dtype=np.uint8)
        glow = np.zeros((self.th, self.tw, 4), dtype=np.uint8)
        for ci, c in enumerate(self.cubes):
            if c.share is not None:
                continue
            for face, (u, v, fw, fh) in face_rects(c).items():
                if fw <= 0 or fh <= 0:
                    continue
                ctx = FaceCtx(self, c, face, fw, fh, ci)
                c.paint(ctx)
                for y in range(fh):
                    for x in range(fw):
                        img[v + y, u + x] = ctx.px[y][x]
                        glow[v + y, u + x] = ctx.glow[y][x]
        self.img, self.glow = img, glow
        if self.scale > 1:  # LayerDefinition keeps the logical size; the PNG carries scale x the pixels
            self.img, self.glow = refine(self, img, glow, self.scale)
        return self.img, self.glow

    def save(self, path, glow_path=None):
        Image.fromarray(self.img, 'RGBA').save(path)
        if glow_path is not None:
            Image.fromarray(self.glow, 'RGBA').save(glow_path)

    # -- java
    def java(self, method):
        out = [f'\tpublic static LayerDefinition {method}() {{',
               '\t\tMeshDefinition mesh = new MeshDefinition();',
               '\t\tPartDefinition root = mesh.getRoot();']
        var = {None: 'root'}
        for b in self.bones:
            cubes = [c for c in self.cubes if c.bone == b.name]
            parts = ['CubeListBuilder.create()']
            mirror = False
            for c in cubes:
                if c.mirror != mirror:
                    parts.append('.mirror()' if c.mirror else '.mirror(false)')
                    mirror = c.mirror
                x, y, z = c.origin
                w, h, d = c.size
                args = f'{f(x)}, {f(y)}, {f(z)}, {f(w)}, {f(h)}, {f(d)}'
                if c.inflate:
                    args += f', new CubeDeformation({f(c.inflate)})'
                parts.append(f'.texOffs({c.uv[0]}, {c.uv[1]}).addBox({args})')
            px, py, pz = b.pivot
            rx, ry, rz = (math.radians(a) for a in b.rot)
            pose = (f'PartPose.offsetAndRotation({f(px)}, {f(py)}, {f(pz)}, {f(rx)}, {f(ry)}, {f(rz)})'
                    if any(b.rot) else f'PartPose.offset({f(px)}, {f(py)}, {f(pz)})')
            v = jvar(b.name)
            var[b.name] = v
            out.append(f'\t\tPartDefinition {v} = {var[b.parent]}.addOrReplaceChild("{b.name}", '
                       + ''.join(parts) + f', {pose});')
        out.append(f'		return LayerDefinition.create(mesh, {self.tw}, {self.th});')
        out.append('\t}')
        return '\n'.join(out)


def f(x):
    return f'{float(x):.4f}'.rstrip('0').rstrip('.') + 'F' if x != int(x) else f'{int(x)}.0F'


def jvar(name):
    p = name.split('_')
    return p[0] + ''.join(s.title() for s in p[1:]) + 'Part'


def face_rects(c):
    """(u, v, w, h) of each face's texture region, using Minecraft's ModelPart.Cube layout."""
    u, v = c.uv
    w, h, d = c.size
    return {
        'top': (u + d, v, w, d),
        'bottom': (u + d + w, v, w, d),
        'right': (u, v + d, d, h),
        'front': (u + d, v + d, w, h),
        'left': (u + d + w, v + d, d, h),
        'back': (u + d + w + d, v + d, w, h),
    }


class FaceCtx:
    """Paint target for one cube face. px/glow are row-major [y][x] RGBA."""

    def __init__(self, model, cube, face, w, h, index):
        self.model, self.cube, self.face, self.w, self.h = model, cube, face, w, h
        self.px = [[CLEAR] * w for _ in range(h)]
        self.glow = [[CLEAR] * w for _ in range(h)]
        self.noise = Noise(zlib.crc32(f'{model.name}/{index}/{face}'.encode()) & 0xFFFF)
        self.seed = zlib.crc32(f'{model.name}/{index}'.encode()) & 0xFFFF
        self.light = FACE_LIGHT[face]
        self.side = face in ('right', 'front', 'left', 'back')

    def set(self, x, y, col):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y][x] = col

    def get(self, x, y):
        return self.px[y][x]

    def emit(self, x, y, col):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.glow[y][x] = col

    def fill(self, fn):
        for y in range(self.h):
            for x in range(self.w):
                c = fn(x, y)
                if c is not None:
                    self.px[y][x] = c

    def stamp(self, rows, legend, ox=0, oy=0, glow_legend=None):
        """Draw an ASCII pixel map. '.' leaves the pixel alone."""
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch == '.':
                    continue
                if ch in legend:
                    self.set(ox + x, oy + y, legend[ch])
                if glow_legend and ch in glow_legend:
                    self.emit(ox + x, oy + y, glow_legend[ch])


# ----------------------------------------------------------------------------- HD refinement
def _blur(a, m, r):
    """Mask-aware gaussian blur with clamped edges (a: HxW or HxWxC, m: HxW weights)."""
    n = int(math.ceil(r * 2))
    k = np.exp(-np.arange(-n, n + 1) ** 2 / (2.0 * r * r))
    w = m if a.ndim == 2 else m[..., None]
    num, den = a * w, m.astype(float)

    def conv(arr, ax):
        return np.apply_along_axis(lambda v: np.convolve(np.pad(v, n, mode='edge'), k, 'valid'), ax, arr)
    for ax in (0, 1):
        num, den = conv(num, ax), conv(den, ax)
    return num / np.maximum(den if a.ndim == 2 else den[..., None], 1e-6)


def _detail(rgb, alpha, glow, face, seed, s, mat=None):
    """Light one face at s x density: height-from-value relief, cavity occlusion, edge rim/AO, and material
    passes (brushed steel + glints, bone pores + hairline cracks, cloth threads + frayed hems, emissive halo)."""
    H, W = alpha.shape
    m = (alpha > 0).astype(float)
    if m.sum() == 0:
        return rgb, alpha, glow
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:H, 0:W]
    ones = np.ones((H, W))
    side = face in ('right', 'front', 'left', 'back')
    melt = 0.15 if mat == 'skin' else 0.38  # faces stay crisp
    rgb = rgb * (1 - melt) + _blur(rgb, m, 1.0) * melt  # melt the 1x dither into paint
    lum = (rgb @ np.array([0.3, 0.55, 0.15])) / 255.0
    mx, mn = rgb.max(2), rgb.min(2)
    sat = (mx - mn) / np.maximum(mx, 1)
    lit = glow[..., 3] > 0
    steel = (sat < 0.16) & (rgb[..., 2] >= rgb[..., 0] - 2) & (lum > 0.1) & ~lit
    bone = (rgb[..., 0] > rgb[..., 2] + 6) & (sat > 0.08) & (sat < 0.5) & (lum > 0.28) & ~lit
    cloth = (lum < 0.24) & ~steel & ~lit
    if mat == 'skin':
        steel = bone = np.zeros_like(lit)
    # height: value plus fine material grain, softened so it reads as surface rather than noise
    thread = np.repeat(rng.normal(0, 1, (1, W)), H, 0) * 0.7 + _blur(rng.normal(0, 1, (H, W)), ones, 1.0) * 0.3
    h = lum + _blur(rng.normal(0, 1, (H, W)), ones, 1.0) * 0.02 + cloth * thread * 0.04
    h = _blur(h, m, 1.0)
    gy, gx = np.gradient(h)
    relief = -(gy * 0.85 + gx * 0.35) * 5.5  # light from up-left
    cav = h - _blur(h, m, 2.2)
    shade = 1.0 + np.clip(relief, -0.35, 0.35) + np.clip(cav * 2.6, -0.4, 0.3)
    # cube edges: rim light on the top lip, occlusion pooling at the bottom and along cutout borders
    if side:
        shade += np.where(yy == 0, 0.22, 0) + np.where(yy == 1, 0.08, 0)
        shade -= np.clip((yy - (H - 1 - 2 * s)) / (2 * s), 0, 1) * 0.22
        shade -= ((xx == 0) | (xx == W - 1)) * 0.07
    elif face == 'top':
        shade += ((yy == 0) | (xx == 0) | (yy == H - 1) | (xx == W - 1)) * 0.12
    else:
        shade -= 0.08
    shade -= np.clip(_blur(1 - m, ones, 1.2) * 1.4, 0, 0.45) * m
    # hue-shifted shading: shadows sink toward violet, lights warm slightly
    dark = np.clip(1 - shade, 0, 1)[..., None]
    brt = np.clip(shade - 1, 0, 1)[..., None]
    out = rgb * shade[..., None]
    out = out * (1 - dark * 0.35) + out * dark * 0.35 * np.array([0.82, 0.8, 1.12]) + brt * np.array([10, 7, 2])
    if steel.any():
        streak = np.repeat(rng.normal(0, 1, (H, 1)), W, 1) * 0.5 + rng.normal(0, 1, (H, W)) * 0.5
        spec = np.clip(relief * 2.2 + cav * 7 + (yy == 0) * side * 0.8, 0, 1) ** 1.5
        out += steel[..., None] * (streak[..., None] * 3 + spec[..., None] * np.array([150, 150, 170]) * lum[..., None])
        glint = steel & (rng.random((H, W)) < 0.004) & (relief > 0.12)
        out[glint] = np.minimum(out[glint] + 90, 255)
    if bone.any():
        out[bone & (rng.random((H, W)) < 0.05)] *= 0.72
        for _ in range(max(1, (H * W) // 260)):
            y, x = int(rng.integers(0, H)), int(rng.integers(0, W))
            for _k in range(int(rng.integers(3, 3 + 2 * s))):
                if 0 <= y < H and 0 <= x < W and bone[y, x]:
                    out[y, x] *= 0.55
                    if x + 1 < W and bone[y, x + 1]:
                        out[y, x + 1] = np.minimum(out[y, x + 1] * 1.12, 255)
                y += 1
                x += int(rng.integers(-1, 2))
    if cloth.any():
        out += cloth[..., None] * (thread * 0.6 + rng.normal(0, 1, (H, W)) * 0.4)[..., None] * 5
        if side:  # frayed threads hanging off torn hems
            for x in range(W):
                for y in range(H - 1, 0, -1):
                    if alpha[y, x] == 0 and alpha[y - 1, x] > 0:
                        if cloth[y - 1, x] and rng.random() < 0.35:
                            for k in range(int(rng.integers(1, 2 + s))):
                                if y + k < H and alpha[y + k, x] == 0:
                                    out[y + k, x] = out[y - 1, x] * (0.8 - 0.1 * k)
                                    alpha[y + k, x] = alpha[y - 1, x]
                        break
    out += rng.normal(0, 1, (H, W, 1)) * 1.4
    out = np.where((alpha > 0)[..., None], out, rgb)
    # emissive bloom: soft halo around glowing pixels, kept on the surface
    g = glow.astype(float)
    ga = g[..., 3] / 255.0
    if ga.any():
        halo = _blur(g[..., :3] * ga[..., None], ones, 1.4)
        ha = _blur(ga, ones, 1.4)
        fresh = (ga == 0) & (ha > 0.03) & (alpha > 0)
        g[fresh, :3] = np.clip(halo[fresh] / np.maximum(ha[fresh, None], 1e-6), 0, 255)
        g[fresh, 3] = np.clip(ha[fresh] * 1.6, 0, 1) * 150
    return np.clip(out, 0, 255), alpha, g


def refine(model, img, glow, s):
    up = np.repeat(np.repeat(img, s, 0), s, 1).astype(float)
    gup = np.repeat(np.repeat(glow, s, 0), s, 1).astype(float)
    out, gout = up.copy(), gup.copy()
    for ci, c in enumerate(model.cubes):
        if c.share is not None:
            continue
        for face, (u, v, fw, fh) in face_rects(c).items():
            if fw <= 0 or fh <= 0:
                continue
            ys, xs = slice(v * s, (v + fh) * s), slice(u * s, (u + fw) * s)
            seed = zlib.crc32(f'{model.name}/{getattr(model, "variant", "")}/{ci}/{face}'.encode())
            rgb, a, g = _detail(up[ys, xs, :3].copy(), up[ys, xs, 3].copy(), gup[ys, xs], face, seed, s, c.mat)
            out[ys, xs, :3], out[ys, xs, 3], gout[ys, xs] = rgb, a, g
    return out.round().astype(np.uint8), gout.round().astype(np.uint8)


# ----------------------------------------------------------------------------- preview renderer
def _rot(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    Rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    Ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    Rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return Rz @ Ry @ Rx  # quaternion rotationZYX(z, y, x)


def _quads(c):
    """Replicates ModelPart.Cube: list of (vertices[4] xyz, uvs[4], normal)."""
    x0, y0, z0 = c.origin
    w, h, d = c.size
    g = c.inflate
    minX, minY, minZ = x0 - g, y0 - g, z0 - g
    maxX, maxY, maxZ = x0 + w + g, y0 + h + g, z0 + d + g
    if c.mirror:
        minX, maxX = maxX, minX
    t0, t1, t2, t3 = (minX, minY, minZ), (maxX, minY, minZ), (maxX, maxY, minZ), (minX, maxY, minZ)
    l0, l1, l2, l3 = (minX, minY, maxZ), (maxX, minY, maxZ), (maxX, maxY, maxZ), (minX, maxY, maxZ)
    U, V = c.uv
    u0, u1, u2, u22 = U, U + d, U + d + w, U + d + w + w
    u3, u4 = U + d + w + d, U + d + w + d + w
    v0, v1, v2 = V, V + d, V + d + h
    polys = [((l1, l0, t0, t1), u1, v0, u2, v1, (0, -1, 0)),
             ((t2, t3, l3, l2), u2, v1, u22, v0, (0, 1, 0)),
             ((t0, l0, l3, t3), u0, v1, u1, v2, (-1, 0, 0)),
             ((t1, t0, t3, t2), u1, v1, u2, v2, (0, 0, -1)),
             ((l1, t1, t2, l2), u2, v1, u3, v2, (1, 0, 0)),
             ((l0, l1, l2, l3), u3, v1, u4, v2, (0, 0, 1))]
    out = []
    for verts, a0, b0, a1, b1, n in polys:
        uvs = [(a1, b0), (a0, b0), (a0, b1), (a1, b1)]
        verts = list(verts)
        if c.mirror:
            verts.reverse()
            uvs.reverse()
            if n[0] != 0:
                n = (-n[0], n[1], n[2])
        out.append((verts, uvs, n))
    return out


def render_preview(model, pose=None, yaw=-30.0, pitch=12.0, scale=10, size=None, bg=(38, 34, 46), hide=()):
    """Orthographic render of the model as Minecraft would draw it (entity lighting, emissive overlay).
    pose: {bone: (rx, ry, rz, dx, dy, dz)} degrees/pixels added to the rest pose."""
    pose = pose or {}
    world = {}
    for b in model.bones:
        extra = pose.get(b.name, (0, 0, 0, 0, 0, 0))
        rot = [math.radians(b.rot[i] + extra[i]) for i in range(3)]
        R = _rot(*rot)
        T = np.array(b.pivot, dtype=float) + np.array(extra[3:6], dtype=float)
        if b.parent is None:
            world[b.name] = (R, T)
        else:
            PR, PT = world[b.parent]
            world[b.name] = (PR @ R, PT + PR @ T)
    # entity transform: scale(-1,-1,1), then view yaw/pitch
    flip = np.diag([-1.0, -1.0, 1.0])
    yr, pr = math.radians(yaw), math.radians(pitch)
    Vy = np.array([[math.cos(yr), 0, math.sin(yr)], [0, 1, 0], [-math.sin(yr), 0, math.cos(yr)]])
    Vx = np.array([[1, 0, 0], [0, math.cos(pr), -math.sin(pr)], [0, math.sin(pr), math.cos(pr)]])
    # model faces -Z; rotate 180 so the face looks at the camera (camera looks down +Z from -Z side)
    face_cam = np.array([[-1, 0, 0], [0, 1, 0], [0, 0, -1]], dtype=float)
    view = Vx @ Vy @ face_cam
    L0 = np.array([0.2, 1.0, -0.7]); L0 /= np.linalg.norm(L0)
    L1 = np.array([-0.2, 1.0, 0.7]); L1 /= np.linalg.norm(L1)
    tex = model.img.astype(float)
    glow = model.glow.astype(float)
    k = tex.shape[1] // model.tw
    quads = []
    for c in model.cubes:
        if c.bone in hide:
            continue
        R, T = world[c.bone]
        for verts, uvs, n in _quads(c):
            P = [flip @ (R @ np.array(v, dtype=float) + T) for v in verts]
            N = flip @ (R @ np.array(n, dtype=float))
            Nw = Vy @ face_cam @ N  # world-ish normal for lighting (lights fixed relative to the viewer, like inventory preview)
            light = min(1.0, 0.4 + 0.6 * (max(0, Nw @ L0) + max(0, Nw @ L1)))
            us = [uv[0] for uv in uvs]
            vs = [uv[1] for uv in uvs]
            ulo, uhi, vlo, vhi = min(us), max(us), min(vs), max(vs)
            if uhi - ulo <= 0 or vhi - vlo <= 0:
                continue
            # corner lookup by uv
            corner = {}
            for p, (uu, vv) in zip(P, uvs):
                corner[(uu == uhi, vv == vhi)] = p
            A, B, C, D = corner[(False, False)], corner[(True, False)], corner[(False, True)], corner[(True, True)]
            for ty in range(int(vlo * k), int(vhi * k)):
                for tx_ in range(int(ulo * k), int(uhi * k)):
                    col = tex[ty, tx_]
                    gcol = glow[ty, tx_]
                    if col[3] < 128 and gcol[:3].sum() == 0:
                        continue
                    pts = []
                    for (su, sv) in ((tx_ / k, ty / k), ((tx_ + 1) / k, ty / k), ((tx_ + 1) / k, (ty + 1) / k), (tx_ / k, (ty + 1) / k)):
                        s = (su - ulo) / (uhi - ulo)
                        t = (sv - vlo) / (vhi - vlo)
                        p = (A * (1 - s) + B * s) * (1 - t) + (C * (1 - s) + D * s) * t
                        pts.append(view @ p)
                    depth = sum(p[2] for p in pts) / 4
                    if col[3] >= 128:
                        ga = gcol[3] / 255.0
                        rgb = list(col[:3] * light * (1 - ga) + gcol[:3] * ga) + [255]
                    else:
                        rgb = list(gcol[:3]) + [gcol[3]]
                    quads.append((depth, pts, tuple(int(min(255, v)) for v in rgb)))
    if not quads:
        return Image.new('RGB', (64, 64), bg)
    xs = [p[0] for q in quads for p in q[1]]
    ys = [p[1] for q in quads for p in q[1]]
    pad = 6
    W = int((max(xs) - min(xs)) * scale) + pad * 2
    H = int((max(ys) - min(ys)) * scale) + pad * 2
    img = Image.new('RGB', (W, H) if size is None else size, bg)
    dr = ImageDraw.Draw(img, 'RGBA')
    mx, my = min(xs), max(ys)
    quads.sort(key=lambda q: q[0])  # camera sits at +Z looking down -Z: draw most negative z first
    for depth, pts, rgb in quads:
        poly = [((p[0] - mx) * scale + pad, (my - p[1]) * scale + pad) for p in pts]
        dr.polygon(poly, fill=rgb)
    return img


def sheet(images, bg=(24, 22, 30)):
    W = sum(i.width for i in images) + 8 * (len(images) + 1)
    H = max(i.height for i in images) + 16
    out = Image.new('RGB', (W, H), bg)
    x = 8
    for i in images:
        out.paste(i, (x, H - 8 - i.height))
        x += i.width + 8
    return out
