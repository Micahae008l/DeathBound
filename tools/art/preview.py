"""python preview.py <module> [variant]  -> writes preview sheets to the scratch dir given by DB_PREVIEW (or ./_preview)."""
import importlib
import os
import sys
from PIL import Image
from core import render_preview, sheet

out = os.environ.get('DB_PREVIEW', '_preview')
os.makedirs(out, exist_ok=True)
mod = importlib.import_module(sys.argv[1])
variants = sys.argv[2:] or [None]
for v in variants:
    m = mod.build(v) if v else mod.build()
    tag = f'{sys.argv[1]}_{v}' if v else sys.argv[1]
    poses = getattr(mod, 'PREVIEW_POSES', {'rest': {}})
    views = []
    for name, pose in poses.items():
        views.append(render_preview(m, pose, yaw=-35, pitch=10, scale=12))
        views.append(render_preview(m, pose, yaw=145, pitch=10, scale=12))
    views.append(render_preview(m, {}, yaw=-90, pitch=0, scale=12))
    sheet(views).save(f'{out}/{tag}.png')
    tex = Image.fromarray(m.img, 'RGBA').resize((m.img.shape[1] * 4, m.img.shape[0] * 4), Image.NEAREST)
    bg = Image.new('RGBA', tex.size, (255, 0, 255, 255))
    bg.alpha_composite(tex)
    bg.save(f'{out}/{tag}_tex.png')
    print('ok', tag)
