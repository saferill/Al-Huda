#!/usr/bin/env python3
import sys
from fontTools.ttLib import TTFont

VAZIRMATN_ASCENT_RATIO = 2100 / 2048
VAZIRMATN_DESCENT_RATIO = -1100 / 2048


def patch(path: str) -> None:
    font = TTFont(path)
    upm = font["head"].unitsPerEm
    ascent = round(VAZIRMATN_ASCENT_RATIO * upm)
    descent = round(VAZIRMATN_DESCENT_RATIO * upm)

    hhea = font["hhea"]
    hhea.ascent, hhea.descent, hhea.lineGap = ascent, descent, 0

    os2 = font["OS/2"]
    os2.sTypoAscender, os2.sTypoDescender, os2.sTypoLineGap = ascent, descent, 0
    os2.fsSelection |= (1 << 7)

    font.save(path)
    line = ascent - descent
    print(f"patched {path}: ascent={ascent} descent={descent} lineGap=0 (ratio {line / upm:.4f}em)")


if __name__ == "__main__":
    patch(sys.argv[1] if len(sys.argv) > 1 else "app/src/main/res/font/noto_sans_arabic.ttf")
