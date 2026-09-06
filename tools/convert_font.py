from fontTools.ttLib import TTFont
from pathlib import Path
src = next(Path('/home/ubuntu/.local/share/pnpm/store').glob('**/fontawesome-free*/webfonts/fa-solid-900.woff2'))
out = Path('/home/ubuntu/waw-messenger/app/src/main/res/font/fa_solid_900.ttf')
out.parent.mkdir(parents=True, exist_ok=True)
font = TTFont(str(src))
font.flavor = None
font.save(str(out))
print(out, out.stat().st_size)
