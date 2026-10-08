#!/usr/bin/env python3
"""Generates the app's flat sticker-style icons as Android VectorDrawables (and SVG previews).

Run:  python3 tools/gen_icons.py            # writes app/src/main/res/drawable/ic_*.xml
      python3 tools/gen_icons.py --svg out/  # also writes SVG previews
"""
import math, os, sys

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "drawable")
V = 100  # viewport

# ---------- path helpers ----------
def circle(cx, cy, r):
    return f"M{cx - r},{cy}a{r},{r} 0 1,0 {2 * r},0a{r},{r} 0 1,0 {-2 * r},0Z"

def ellipse(cx, cy, rx, ry):
    return f"M{cx - rx},{cy}a{rx},{ry} 0 1,0 {2 * rx},0a{rx},{ry} 0 1,0 {-2 * rx},0Z"

def rrect(x, y, w, h, r):
    r = min(r, w / 2, h / 2)
    return (f"M{x + r},{y}h{w - 2 * r}a{r},{r} 0 0,1 {r},{r}v{h - 2 * r}a{r},{r} 0 0,1 {-r},{r}"
            f"h{-(w - 2 * r)}a{r},{r} 0 0,1 {-r},{-r}v{-(h - 2 * r)}a{r},{r} 0 0,1 {r},{-r}Z")

def poly(*pts):
    return "M" + "L".join(f"{x},{y}" for x, y in pts) + "Z"

def star(cx, cy, r_out, r_in, n=5):
    pts = []
    for i in range(2 * n):
        a = -math.pi / 2 + i * math.pi / n
        r = r_out if i % 2 == 0 else r_in
        pts.append((round(cx + r * math.cos(a), 2), round(cy + r * math.sin(a), 2)))
    return poly(*pts)

def heart(cx, cy, s):
    return (f"M{cx},{cy + s * 0.9}C{cx - s * 1.6},{cy - s * 0.1} {cx - s * 0.9},{cy - s * 1.1} {cx},{cy - s * 0.4}"
            f"C{cx + s * 0.9},{cy - s * 1.1} {cx + s * 1.6},{cy - s * 0.1} {cx},{cy + s * 0.9}Z")

def smile(cx, cy, w, depth):
    return f"M{cx - w / 2},{cy}Q{cx},{cy + depth} {cx + w / 2},{cy}"

def line(x1, y1, x2, y2):
    return f"M{x1},{y1}L{x2},{y2}"

# A shape is (path, fill, stroke, stroke_width)
def F(path, fill): return (path, fill, None, 0)
def S(path, stroke, width): return (path, None, stroke, width)

# ---------- palette ----------
WHITE, BLACK, INK = "#FFFFFF", "#1F1A2E", "#2B2340"
PINK, DARKPINK, RED, ORANGE = "#F9A8C9", "#E8739C", "#EF4444", "#F97316"
YELLOW, TAN, BROWN, DARKBROWN = "#FACC15", "#E0B07A", "#9A6B3F", "#5C3D23"
GRAY, DARKGRAY, GREEN, DARKGREEN = "#A1A1AA", "#52525B", "#4ADE80", "#15803D"
SKY, CREAM = "#60A5FA", "#FFF4D6"

def eyes(y=48, dx=10, r=3.6, cx=50):
    return [F(circle(cx - dx, y, r), INK), F(circle(cx + dx, y, r), INK),
            F(circle(cx - dx + 1.2, y - 1.2, 1.2), WHITE), F(circle(cx + dx + 1.2, y - 1.2, 1.2), WHITE)]

ICONS = {}

# ----- animals -----
ICONS["dog"] = [
    F(ellipse(22, 55, 11, 20), BROWN), F(ellipse(78, 55, 11, 20), BROWN),
    F(circle(50, 54, 30), TAN),
    F(ellipse(50, 66, 15, 11), CREAM),
    *eyes(y=47),
    F(ellipse(50, 61, 6, 4.5), INK),
    S(smile(50, 70, 14, 5), INK, 2.5),
]
ICONS["cat"] = [
    F(poly((24, 44), (30, 18), (48, 36)), GRAY), F(poly((76, 44), (70, 18), (52, 36)), GRAY),
    F(poly((29, 40), (32, 25), (44, 36)), PINK), F(poly((71, 40), (68, 25), (56, 36)), PINK),
    F(circle(50, 56, 29), GRAY),
    *eyes(y=50, dx=11),
    F(poly((45, 62), (55, 62), (50, 67)), DARKPINK),
    S(line(20, 60, 40, 64), INK, 2), S(line(20, 70, 40, 68), INK, 2),
    S(line(80, 60, 60, 64), INK, 2), S(line(80, 70, 60, 68), INK, 2),
    S("M44,70Q50,76 56,70", INK, 2.5),
]
ICONS["cow"] = [
    F(poly((28, 30), (20, 10), (36, 24)), TAN), F(poly((72, 30), (80, 10), (64, 24)), TAN),
    F(ellipse(18, 44, 12, 7), WHITE), F(ellipse(82, 44, 12, 7), WHITE),
    F(circle(50, 52, 30), WHITE),
    F(ellipse(34, 38, 10, 9), INK), F(ellipse(68, 32, 7, 6), INK),
    *eyes(y=48),
    F(ellipse(50, 68, 19, 12), PINK),
    F(ellipse(43, 68, 3.5, 2.5), DARKPINK), F(ellipse(57, 68, 3.5, 2.5), DARKPINK),
]
ICONS["pig"] = [
    F(poly((24, 40), (24, 16), (44, 30)), PINK), F(poly((76, 40), (76, 16), (56, 30)), PINK),
    F(circle(50, 54, 30), PINK),
    *eyes(y=46, dx=12),
    F(ellipse(50, 64, 15, 10), DARKPINK),
    F(ellipse(44, 64, 3, 4), INK), F(ellipse(56, 64, 3, 4), INK),
]
ICONS["chicken"] = [
    F(circle(40, 22, 7), RED), F(circle(50, 18, 7), RED), F(circle(60, 22, 7), RED),
    F(circle(50, 54, 30), WHITE),
    *eyes(y=46, dx=11),
    F(poly((40, 56), (60, 56), (50, 68)), ORANGE),
    F(ellipse(50, 74, 6, 8), RED),
]
ICONS["duck"] = [
    F(circle(50, 48, 29), YELLOW),
    *eyes(y=42, dx=11),
    F(ellipse(50, 66, 22, 10), ORANGE),
    S(line(32, 66, 68, 66), "#C2410C", 1.6),
    F(ellipse(46, 61, 2, 1.5), "#C2410C"), F(ellipse(54, 61, 2, 1.5), "#C2410C"),
]
ICONS["sheep"] = [
    *[F(circle(x, y, 13), WHITE) for x, y in [(30, 34), (50, 24), (70, 34), (24, 54), (76, 54), (32, 72), (68, 72), (50, 78)]],
    F(circle(50, 50, 22), WHITE),
    F(ellipse(18, 56, 9, 5), DARKGRAY), F(ellipse(82, 56, 9, 5), DARKGRAY),
    F(ellipse(50, 58, 18, 20), DARKGRAY),
    *eyes(y=54, dx=8, r=3.2),
    F(ellipse(50, 67, 4, 3), INK),
]
ICONS["horse"] = [
    F(poly((34, 30), (36, 10), (46, 26)), BROWN), F(poly((66, 30), (64, 10), (54, 26)), BROWN),
    F(ellipse(50, 54, 24, 32), BROWN),
    F("M34,30Q50,12 66,30Q58,26 50,36Q42,26 34,30Z", DARKBROWN),
    *eyes(y=46, dx=11),
    F(ellipse(50, 74, 14, 9), TAN),
    F(ellipse(45, 74, 2.5, 3.5), INK), F(ellipse(55, 74, 2.5, 3.5), INK),
]
ICONS["lion"] = [
    F(circle(50, 50, 42), ORANGE),
    *[F(circle(50 + 36 * math.cos(a), 50 + 36 * math.sin(a), 9), "#EA580C") for a in [i * math.pi / 6 for i in range(12)]],
    F(circle(26, 34, 7), TAN), F(circle(74, 34, 7), TAN),
    F(circle(50, 52, 27), TAN),
    *eyes(y=46),
    F(ellipse(50, 61, 13, 9), CREAM),
    F(poly((45, 58), (55, 58), (50, 63)), INK),
    S("M44,66Q50,72 56,66", INK, 2.5),
]
ICONS["elephant"] = [
    F(circle(22, 50, 17), GRAY), F(circle(78, 50, 17), GRAY),
    F(circle(22, 50, 11), PINK), F(circle(78, 50, 11), PINK),
    F(circle(50, 48, 28), GRAY),
    *eyes(y=42),
    F(rrect(43, 56, 14, 34, 7), GRAY),
    F(ellipse(50, 88, 6, 3.5), DARKGRAY),
]
ICONS["frog"] = [
    F(circle(32, 30, 12), GREEN), F(circle(68, 30, 12), GREEN),
    F(ellipse(50, 56, 36, 26), GREEN),
    F(circle(32, 30, 7), WHITE), F(circle(68, 30, 7), WHITE),
    F(circle(33, 31, 3.5), INK), F(circle(67, 31, 3.5), INK),
    S("M30,60Q50,76 70,60", INK, 3),
    F(circle(44, 48, 1.8), DARKGREEN), F(circle(56, 48, 1.8), DARKGREEN),
]
ICONS["monkey"] = [
    F(circle(20, 50, 11), BROWN), F(circle(80, 50, 11), BROWN),
    F(circle(20, 50, 6), TAN), F(circle(80, 50, 6), TAN),
    F(circle(50, 50, 30), BROWN),
    F(ellipse(40, 46, 11, 10), TAN), F(ellipse(60, 46, 11, 10), TAN), F(ellipse(50, 62, 19, 13), TAN),
    *eyes(y=46, dx=10, r=3.2),
    F(ellipse(46, 60, 2, 1.5), INK), F(ellipse(54, 60, 2, 1.5), INK),
    S("M42,68Q50,74 58,68", INK, 2.5),
]

# ----- vehicles & robot (random call characters) -----
ICONS["firetruck"] = [
    F(rrect(8, 40, 84, 34, 6), RED),
    F(rrect(12, 24, 34, 22, 5), RED),
    F(rrect(17, 29, 24, 13, 3), SKY),
    F(rrect(50, 30, 42, 6, 3), WHITE),  # ladder rail
    F(rrect(50, 42, 42, 4, 2), WHITE),
    *[F(rrect(x, 30, 3, 16, 1), WHITE) for x in (56, 66, 76, 86)],
    F(circle(26, 76, 10), INK), F(circle(74, 76, 10), INK),
    F(circle(26, 76, 4), GRAY), F(circle(74, 76, 4), GRAY),
    F(circle(22, 20, 5), SKY),
]
ICONS["train"] = [
    F(rrect(8, 44, 84, 30, 6), SKY),
    F(rrect(56, 20, 36, 30, 6), "#2563EB"),
    F(rrect(62, 26, 24, 16, 3), CREAM),
    F(rrect(14, 48, 36, 18, 4), "#2563EB"),
    F(rrect(18, 14, 12, 32, 3), DARKGRAY),
    F(circle(26, 10, 6), WHITE), F(circle(34, 6, 5), WHITE),
    F(circle(24, 78, 9), INK), F(circle(50, 78, 9), INK), F(circle(76, 78, 9), INK),
    F(circle(24, 78, 3.5), GRAY), F(circle(50, 78, 3.5), GRAY), F(circle(76, 78, 3.5), GRAY),
]
ICONS["robot"] = [
    F(rrect(47, 8, 6, 14, 3), GRAY), F(circle(50, 8, 6), RED),
    F(rrect(18, 22, 64, 54, 12), GRAY),
    F(rrect(26, 32, 48, 26, 8), INK),
    F(circle(39, 45, 7), SKY), F(circle(61, 45, 7), SKY),
    F(circle(39, 45, 3), WHITE), F(circle(61, 45, 3), WHITE),
    F(rrect(34, 64, 32, 6, 3), INK),
    F(rrect(6, 36, 12, 24, 5), DARKGRAY), F(rrect(82, 36, 12, 24, 5), DARKGRAY),
    F(rrect(30, 76, 40, 16, 6), DARKGRAY),
]

# ----- people avatars (contacts without a photo) -----
def avatar(hair, skin, hair_long=False, grey=False):
    sh = []
    sh.append(F(ellipse(50, 92, 34, 24), "#6D5BD0"))  # shoulders
    if hair_long:
        sh.append(F(rrect(26, 30, 48, 50, 20), hair))
    sh.append(F(circle(50, 44, 22), skin))
    sh.append(F("M28,42Q30,18 50,18Q70,18 72,42Q66,30 50,30Q34,30 28,42Z", hair))
    sh += eyes(y=46, dx=8, r=3)
    sh.append(S("M44,54Q50,59 56,54", INK, 2.2))
    return sh
ICONS["avatar_woman"] = avatar(DARKBROWN, "#C68642", hair_long=True)
ICONS["avatar_man"] = avatar(INK, "#8D5524")
ICONS["avatar_girl"] = avatar("#7C2D12", "#E0AC69", hair_long=True)
ICONS["avatar_boy"] = avatar("#7C2D12", "#C68642")
ICONS["avatar_grandma"] = avatar("#D4D4D8", "#E0AC69", hair_long=True)
ICONS["avatar_grandpa"] = avatar("#D4D4D8", "#8D5524")

# ----- home tile glyphs (white on colored tile) -----
ICONS["tile_phone"] = [
    F("M26,14C20,14 14,20 14,26C14,60 40,86 74,86C80,86 86,80 86,74L86,64C86,61 84,59 81,58L66,53C63,52 60,53 58,56L54,62C44,57 43,56 38,46L44,42C47,40 48,37 47,34L42,19C41,16 39,14 36,14Z", WHITE),
]
ICONS["tile_family"] = [
    F(circle(34, 34, 12), WHITE), F(circle(66, 32, 14), WHITE),
    F("M14,86C14,66 24,56 34,56C44,56 54,66 54,86Z", WHITE),
    F("M46,86C46,62 56,52 66,52C78,52 88,62 88,86Z", WHITE),
]
ICONS["tile_animals"] = [  # paw print
    F(ellipse(28, 36, 9, 12), WHITE), F(ellipse(72, 36, 9, 12), WHITE),
    F(ellipse(44, 24, 9, 12), WHITE), F(ellipse(56, 24, 9, 12), WHITE),
    F("M50,44C66,44 80,58 80,70C80,80 72,84 64,82C58,80 54,78 50,78C46,78 42,80 36,82C28,84 20,80 20,70C20,58 34,44 50,44Z", WHITE),
]
ICONS["tile_piano"] = [
    F(rrect(12, 20, 76, 60, 6), WHITE),
    *[F(rrect(x, 20, 9, 36, 2), INK) for x in (23, 36, 55, 68)],
    *[S(line(x, 56, x, 80), INK, 1.5) for x in (27.5, 40.5, 59.5, 72.5)],
]
ICONS["tile_paint"] = [
    F("M50,14C28,14 12,30 12,50C12,70 28,86 50,86C56,86 58,82 56,78C54,74 56,70 62,70L72,70C80,70 88,64 88,52C88,30 72,14 50,14Z", WHITE),
    F(circle(30, 46, 7), RED), F(circle(42, 30, 7), YELLOW), F(circle(62, 30, 7), GREEN), F(circle(74, 46, 7), SKY),
]
ICONS["tile_peekaboo"] = [
    F(circle(50, 48, 30), WHITE),
    *eyes(y=44, dx=11, r=4),
    S("M42,60Q50,66 58,60", INK, 3),
    F(rrect(14, 40, 30, 36, 10), YELLOW), F(rrect(56, 40, 30, 36, 10), YELLOW),
    *[F(rrect(18 + i * 7, 30, 6, 20, 3), YELLOW) for i in range(4)],
    *[F(rrect(59 + i * 7, 30, 6, 20, 3), YELLOW) for i in range(4)],
]
ICONS["tile_call"] = ICONS["tile_phone"]

# ----- dial pad extras -----
ICONS["key_star"] = [F(star(50, 52, 36, 15), WHITE)]
ICONS["key_heart"] = [F(heart(50, 50, 32), WHITE)]
ICONS["hand_wave"] = [
    F(rrect(30, 40, 34, 44, 14), YELLOW),
    *[F(rrect(31 + i * 9, 16, 7, 34, 3.5), YELLOW) for i in range(4)],
    F("M26,52L18,42C15,38 20,33 24,37L34,48Z", YELLOW),
]
ICONS["signal"] = [F(poly((20, 70), (30, 70), (30, 58), (20, 58)), WHITE), F(poly((38, 70), (48, 70), (48, 48), (38, 48)), WHITE),
                   F(poly((56, 70), (66, 70), (66, 38), (56, 38)), WHITE), F(poly((74, 70), (84, 70), (84, 28), (74, 28)), WHITE)]
ICONS["battery"] = [S(rrect(16, 34, 60, 32, 6), WHITE, 5), F(rrect(78, 44, 8, 12, 2), WHITE), F(rrect(24, 42, 44, 16, 3), GREEN)]

# ---------- writers ----------
def to_vector(shapes):
    out = [f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
           f'    android:width="96dp" android:height="96dp"\n'
           f'    android:viewportWidth="{V}" android:viewportHeight="{V}">']
    for path, fill, stroke, width in shapes:
        attrs = [f'android:pathData="{path}"']
        if fill: attrs.append(f'android:fillColor="{fill}"')
        if stroke:
            attrs += [f'android:strokeColor="{stroke}"', f'android:strokeWidth="{width}"',
                      'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
        out.append("    <path " + " ".join(attrs) + " />")
    out.append("</vector>\n")
    return "\n".join(out)

def to_svg(shapes):
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {V} {V}" width="200" height="200">',
           '<rect width="100" height="100" fill="#2A1B4D"/>']
    for path, fill, stroke, width in shapes:
        attrs = [f'd="{path}"', f'fill="{fill or "none"}"']
        if stroke: attrs += [f'stroke="{stroke}"', f'stroke-width="{width}"', 'stroke-linecap="round"', 'stroke-linejoin="round"']
        out.append("<path " + " ".join(attrs) + "/>")
    out.append("</svg>")
    return "\n".join(out)

if __name__ == "__main__":
    os.makedirs(OUT, exist_ok=True)
    svg_dir = sys.argv[2] if len(sys.argv) > 2 and sys.argv[1] == "--svg" else None
    for name, shapes in ICONS.items():
        with open(os.path.join(OUT, f"ic_{name}.xml"), "w") as f:
            f.write(to_vector(shapes))
        if svg_dir:
            os.makedirs(svg_dir, exist_ok=True)
            with open(os.path.join(svg_dir, f"{name}.svg"), "w") as f:
                f.write(to_svg(shapes))
    print(f"wrote {len(ICONS)} icons")
