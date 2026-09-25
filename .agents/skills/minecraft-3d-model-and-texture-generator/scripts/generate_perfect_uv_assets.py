"""
Perfect UV and Multi-Material Texture Generator for Industry Made
Generates clean 16x16 component textures and pixel-perfect UV mappings for all Era 1 machines,
eliminating UV cross-sampling, texture stretching, and misaligned face maps.
"""

import json
import os
import sys
from PIL import Image, ImageDraw

sys.path.append(os.path.dirname(__file__))
from model_builder import STANDARD_BLOCK_DISPLAY

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../.."))
ASSETS_DIR = os.path.join(BASE_DIR, "src/main/resources/assets/industry-made")
TEXTURES_DIR = os.path.join(ASSETS_DIR, "textures/block")
MODELS_DIR = os.path.join(ASSETS_DIR, "models/block")
BLOCKSTATES_DIR = os.path.join(ASSETS_DIR, "blockstates")
ITEMS_DIR = os.path.join(ASSETS_DIR, "items")

os.makedirs(TEXTURES_DIR, exist_ok=True)
os.makedirs(MODELS_DIR, exist_ok=True)
os.makedirs(BLOCKSTATES_DIR, exist_ok=True)
os.makedirs(ITEMS_DIR, exist_ok=True)


# ==============================================================================
# 1. TEXTURE PAINTER HELPERS
# ==============================================================================
def save_png(img: Image.Image, name: str):
    path = os.path.join(TEXTURES_DIR, name)
    img.save(path)
    print(f"Saved texture: {name}")


def create_crucible_textures():
    # 1.1 Refractory Ceramic (terracotta / mud brick tone with clean horizontal mortar rows)
    # Palettes
    c_base = (92, 68, 56)
    c_dark = (52, 38, 32)
    c_shadow = (70, 52, 44)
    c_light = (118, 88, 72)
    c_bright = (142, 108, 90)

    img = Image.new("RGBA", (16, 16), (*c_base, 255))
    draw = ImageDraw.Draw(img)
    # Top rim highlight
    draw.line([(0, 0), (15, 0)], fill=(*c_bright, 255))
    draw.line([(0, 1), (15, 1)], fill=(*c_light, 255))
    # Brick seams
    for y in [4, 8, 12]:
        draw.line([(0, y), (15, y)], fill=(*c_dark, 255))
        draw.line([(0, y + 1), (15, y + 1)], fill=(*c_light, 255))
    # Vertical mortar staggered
    for x in [0, 8]:
        draw.line([(x, 2), (x, 4)], fill=(*c_dark, 255))
        draw.line([(x, 10), (x, 12)], fill=(*c_dark, 255))
    for x in [4, 12]:
        draw.line([(x, 6), (x, 8)], fill=(*c_dark, 255))
        draw.line([(x, 14), (x, 15)], fill=(*c_dark, 255))
    # Dark charred base
    draw.line([(0, 15), (15, 15)], fill=(*c_dark, 255))
    save_png(img, "crucible_ceramic.png")

    # 1.2 Crucible Cast Iron (dark graphite metal for rim & handles)
    iron_base = (56, 58, 64)
    iron_dark = (36, 37, 40)
    iron_light = (84, 88, 96)
    iron_spec = (118, 122, 132)

    img_iron = Image.new("RGBA", (16, 16), (*iron_base, 255))
    draw_iron = ImageDraw.Draw(img_iron)
    # Bevel frame
    draw_iron.line([(0, 0), (15, 0)], fill=(*iron_spec, 255))
    draw_iron.line([(0, 0), (0, 15)], fill=(*iron_light, 255))
    draw_iron.line([(0, 15), (15, 15)], fill=(*iron_dark, 255))
    draw_iron.line([(15, 0), (15, 15)], fill=(*iron_dark, 255))
    # Rivet / bolt studs
    for (x, y) in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        draw_iron.point((x, y), fill=(*iron_spec, 255))
        draw_iron.point((x + 1, y), fill=(*iron_dark, 255))
    save_png(img_iron, "crucible_iron.png")

    # Also save crucible.png as main fallback
    save_png(img, "crucible.png")


def create_bellows_textures():
    # 2.1 Spruce Planks
    w_base = (86, 60, 36)
    w_dark = (48, 34, 20)
    w_light = (112, 80, 50)
    w_bright = (136, 98, 62)

    img_wood = Image.new("RGBA", (16, 16), (*w_base, 255))
    draw_wood = ImageDraw.Draw(img_wood)
    # Horizontal board grooves
    for y in [0, 8]:
        draw_wood.line([(0, y), (15, y)], fill=(*w_bright, 255))
        draw_wood.line([(0, y + 1), (15, y + 1)], fill=(*w_light, 255))
    for y in [7, 15]:
        draw_wood.line([(0, y), (15, y)], fill=(*w_dark, 255))
    # Subtle vertical grain
    for x in [5, 11]:
        draw_wood.line([(x, 1), (x, 6)], fill=(*w_dark, 255))
        draw_wood.line([(x + 1, 1), (x + 1, 6)], fill=(*w_light, 255))
    save_png(img_wood, "bellows_wood.png")

    # 2.2 Accordion Leather Pleats
    l_base = (124, 68, 38)
    l_dark = (64, 32, 18)
    l_light = (162, 94, 54)
    l_spec = (194, 120, 72)

    img_leather = Image.new("RGBA", (16, 16), (*l_base, 255))
    draw_leather = ImageDraw.Draw(img_leather)
    # 3 horizontal accordion pleats with deep folds
    for y_start in [0, 5, 11]:
        draw_leather.line([(0, y_start), (15, y_start)], fill=(*l_spec, 255))
        draw_leather.line([(0, y_start + 1), (15, y_start + 1)], fill=(*l_light, 255))
        draw_leather.line([(0, y_start + 4), (15, y_start + 4)], fill=(*l_dark, 255))
    save_png(img_leather, "bellows_leather.png")

    # 2.3 Cast Iron Nozzle
    i_base = (50, 52, 56)
    i_dark = (32, 33, 36)
    i_light = (82, 86, 92)
    img_iron = Image.new("RGBA", (16, 16), (*i_base, 255))
    draw_iron = ImageDraw.Draw(img_iron)
    draw_iron.line([(0, 0), (15, 0)], fill=(*i_light, 255))
    draw_iron.line([(0, 15), (15, 15)], fill=(*i_dark, 255))
    save_png(img_iron, "bellows_iron.png")

    # Main fallback
    save_png(img_wood, "bellows.png")


def create_boiler_textures():
    # 3.1 Bronze Pressure Vessel
    b_base = (172, 94, 46)
    b_dark = (100, 52, 26)
    b_light = (212, 126, 66)
    b_bright = (242, 158, 92)
    b_rivet = (255, 214, 100)

    img_bronze = Image.new("RGBA", (16, 16), (*b_base, 255))
    draw_b = ImageDraw.Draw(img_bronze)
    # Top and bottom seam
    draw_b.line([(0, 0), (15, 0)], fill=(*b_bright, 255))
    draw_b.line([(0, 15), (15, 15)], fill=(*b_dark, 255))
    # Rivet belt horizontal (row 5 and row 11)
    for y in [4, 10]:
        draw_b.line([(0, y), (15, y)], fill=(*b_dark, 255))
        draw_b.line([(0, y + 1), (15, y + 1)], fill=(*b_light, 255))
        for x in range(1, 16, 3):
            draw_b.point((x, y), fill=(*b_rivet, 255))
            draw_b.point((x + 1, y), fill=(*b_dark, 255))
    save_png(img_bronze, "boiler_bronze.png")

    # 3.2 Cast Iron Legs
    img_iron = Image.new("RGBA", (16, 16), (46, 48, 52, 255))
    draw_i = ImageDraw.Draw(img_iron)
    draw_i.line([(0, 0), (15, 0)], fill=(74, 78, 84, 255))
    draw_i.line([(0, 15), (15, 15)], fill=(28, 29, 32, 255))
    save_png(img_iron, "boiler_iron.png")

    # 3.3 Brass Pressure Gauge (Full 16x16 Texture)
    g_brass = (194, 146, 52)
    g_brass_dark = (124, 90, 30)
    g_dial = (238, 242, 240)
    g_dial_shadow = (200, 206, 204)
    g_needle = (210, 30, 30)

    img_gauge = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_g = ImageDraw.Draw(img_gauge)
    # Outer brass circular housing (radius 7, centered at 8, 8)
    draw_g.ellipse([1, 1, 14, 14], fill=(*g_brass, 255), outline=(*g_brass_dark, 255))
    # White dial face (radius 5)
    draw_g.ellipse([3, 3, 12, 12], fill=(*g_dial, 255), outline=(*g_dial_shadow, 255))
    # Dial tick marks
    draw_g.point((7, 4), fill=(60, 60, 60, 255))
    draw_g.point((4, 7), fill=(60, 60, 60, 255))
    draw_g.point((11, 7), fill=(60, 60, 60, 255))
    # Red pressure needle pointing up-right
    draw_g.line([(7, 8), (10, 5)], fill=(*g_needle, 255))
    draw_g.point((7, 8), fill=(40, 40, 40, 255))  # Pivot cap
    save_png(img_gauge, "boiler_gauge.png")

    # Main fallback
    save_png(img_bronze, "low_pressure_boiler.png")


def create_piston_textures():
    # 4.1 Heavy Bronze Cylinder
    img_bronze = Image.new("RGBA", (16, 16), (168, 92, 44, 255))
    draw_b = ImageDraw.Draw(img_bronze)
    draw_b.line([(0, 0), (15, 0)], fill=(235, 150, 85, 255))
    draw_b.line([(0, 15), (15, 15)], fill=(96, 50, 24, 255))
    # Vertical rib reflection
    draw_b.line([(4, 1), (4, 14)], fill=(205, 120, 60, 255))
    draw_b.line([(12, 1), (12, 14)], fill=(120, 62, 30, 255))
    save_png(img_bronze, "piston_bronze.png")

    # 4.2 Cast Iron Pedestal
    img_iron = Image.new("RGBA", (16, 16), (48, 50, 54, 255))
    draw_i = ImageDraw.Draw(img_iron)
    draw_i.line([(0, 0), (15, 0)], fill=(80, 84, 90, 255))
    draw_i.line([(0, 15), (15, 15)], fill=(28, 29, 32, 255))
    save_png(img_iron, "piston_iron.png")

    # 4.3 Polished Chrome/Steel Piston Rod & Ram
    s_base = (140, 146, 156)
    s_dark = (82, 88, 96)
    s_light = (184, 190, 200)
    s_spec = (228, 234, 244)

    img_steel = Image.new("RGBA", (16, 16), (*s_base, 255))
    draw_s = ImageDraw.Draw(img_steel)
    draw_s.line([(0, 0), (15, 0)], fill=(*s_spec, 255))
    # Vertical reflection column
    draw_s.line([(5, 1), (5, 15)], fill=(*s_spec, 255))
    draw_s.line([(6, 1), (6, 15)], fill=(*s_light, 255))
    draw_s.line([(11, 1), (11, 15)], fill=(*s_dark, 255))
    save_png(img_steel, "piston_steel.png")

    save_png(img_bronze, "steam_piston.png")


def create_hammer_textures():
    # 5.1 Cast Iron Anvil & Frame
    img_frame = Image.new("RGBA", (16, 16), (52, 54, 58, 255))
    draw_f = ImageDraw.Draw(img_frame)
    draw_f.line([(0, 0), (15, 0)], fill=(82, 86, 92, 255))
    draw_f.line([(0, 15), (15, 15)], fill=(32, 33, 36, 255))
    # Frame structural bolts
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        draw_f.point((x, y), fill=(110, 114, 122, 255))
        draw_f.point((x + 1, y), fill=(26, 27, 30, 255))
    save_png(img_frame, "hammer_frame.png")

    # 5.2 Hardened Tool Steel Die & Tup Head
    img_steel = Image.new("RGBA", (16, 16), (130, 136, 146, 255))
    draw_s = ImageDraw.Draw(img_steel)
    draw_s.line([(0, 0), (15, 0)], fill=(220, 226, 236, 255))
    draw_s.line([(0, 1), (15, 1)], fill=(180, 186, 196, 255))
    draw_s.line([(0, 15), (15, 15)], fill=(76, 80, 88, 255))
    save_png(img_steel, "hammer_steel.png")

    # 5.3 Bronze Pivot Bushings & Axle
    img_bronze = Image.new("RGBA", (16, 16), (174, 102, 50, 255))
    draw_b = ImageDraw.Draw(img_bronze)
    draw_b.line([(0, 0), (15, 0)], fill=(240, 168, 96, 255))
    draw_b.line([(0, 15), (15, 15)], fill=(104, 56, 28, 255))
    save_png(img_bronze, "hammer_bronze.png")

    save_png(img_frame, "mechanical_hammer.png")


# ==============================================================================
# 2. MODEL DEFINITIONS (Exact UVs and Clean Texture Keys)
# ==============================================================================

def make_box(name, x1, y1, z1, x2, y2, z2, tex, face_uvs=None):
    from_p = [min(x1, x2), min(y1, y2), min(z1, z2)]
    to_p = [max(x1, x2), max(y1, y2), max(z1, z2)]
    dx = to_p[0] - from_p[0]
    dy = to_p[1] - from_p[1]
    dz = to_p[2] - from_p[2]

    faces = {}
    for d in ["north", "south", "east", "west", "up", "down"]:
        f_entry = {"texture": tex}
        if face_uvs and d in face_uvs:
            f_entry["uv"] = face_uvs[d]
        else:
            # Clean local UV mapping (anchored to 0 or properly wrapped)
            if d == "north":
                f_entry["uv"] = [from_p[0], 16 - to_p[1], to_p[0], 16 - from_p[1]]
            elif d == "south":
                f_entry["uv"] = [from_p[0], 16 - to_p[1], to_p[0], 16 - from_p[1]]
            elif d == "west":
                f_entry["uv"] = [from_p[2], 16 - to_p[1], to_p[2], 16 - from_p[1]]
            elif d == "east":
                f_entry["uv"] = [from_p[2], 16 - to_p[1], to_p[2], 16 - from_p[1]]
            elif d == "up":
                f_entry["uv"] = [from_p[0], from_p[2], to_p[0], to_p[2]]
            elif d == "down":
                f_entry["uv"] = [from_p[0], 16 - to_p[2], to_p[0], 16 - from_p[2]]
        faces[d] = f_entry

    return {
        "name": name,
        "from": from_p,
        "to": to_p,
        "shade": True,
        "faces": faces
    }


def build_crucible_model():
    elements = [
        make_box("base", 2, 0, 2, 14, 2, 14, "#ceramic"),
        make_box("wall_north", 2, 2, 2, 14, 12, 4, "#ceramic"),
        make_box("wall_south", 2, 2, 12, 14, 12, 14, "#ceramic"),
        make_box("wall_west", 2, 2, 4, 4, 12, 12, "#ceramic"),
        make_box("wall_east", 12, 2, 4, 14, 12, 12, "#ceramic"),
        # Beveled cast iron rim
        make_box("rim_north", 1, 12, 1, 15, 14, 3, "#iron"),
        make_box("rim_south", 1, 12, 13, 15, 14, 15, "#iron"),
        make_box("rim_west", 1, 12, 3, 3, 14, 13, "#iron"),
        make_box("rim_east", 13, 12, 3, 15, 14, 13, "#iron"),
        # Side lifting handles
        make_box("handle_west", 0, 6, 6, 2, 9, 10, "#iron"),
        make_box("handle_east", 14, 6, 6, 16, 9, 10, "#iron"),
        # Interior floor
        make_box("interior", 4, 2, 4, 12, 3, 12, "#ceramic", {
            "up": [4, 4, 12, 12]
        })
    ]

    model = {
        "textures": {
            "particle": "industry-made:block/crucible_ceramic",
            "ceramic": "industry-made:block/crucible_ceramic",
            "iron": "industry-made:block/crucible_iron"
        },
        "elements": elements,
        "display": STANDARD_BLOCK_DISPLAY
    }

    with open(os.path.join(MODELS_DIR, "crucible.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def build_bellows_model():
    elements = [
        # Top & Bottom spruce boards
        make_box("base_plate", 1, 0, 1, 15, 2, 15, "#wood"),
        make_box("top_plate", 1, 10, 1, 15, 12, 15, "#wood"),
        make_box("handle_shaft", 6, 11, 0, 10, 13, 3, "#wood"),
        # Pleated accordion leather
        make_box("leather_low", 2, 2, 2, 14, 5, 14, "#leather"),
        make_box("leather_mid", 1, 5, 1, 15, 7, 15, "#leather"),
        make_box("leather_high", 2, 7, 2, 14, 10, 14, "#leather"),
        # Front iron nozzle
        make_box("nozzle_collar", 6, 1, 13, 10, 5, 15, "#iron"),
        make_box("nozzle_pipe", 7, 2, 15, 9, 4, 16, "#iron")
    ]

    model = {
        "textures": {
            "particle": "industry-made:block/bellows_wood",
            "wood": "industry-made:block/bellows_wood",
            "leather": "industry-made:block/bellows_leather",
            "iron": "industry-made:block/bellows_iron"
        },
        "elements": elements,
        "display": STANDARD_BLOCK_DISPLAY
    }

    with open(os.path.join(MODELS_DIR, "bellows.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def build_boiler_model():
    elements = [
        # 4 Cast Iron Corner Legs
        make_box("leg_nw", 1, 0, 1, 4, 3, 4, "#iron"),
        make_box("leg_ne", 12, 0, 1, 15, 3, 4, "#iron"),
        make_box("leg_sw", 1, 0, 12, 4, 3, 15, "#iron"),
        make_box("leg_se", 12, 0, 12, 15, 3, 15, "#iron"),
        # Cast Iron Hearth Tray
        make_box("hearth", 2, 1, 2, 14, 3, 14, "#iron"),
        # Cylindrical Bronze Vessel
        make_box("core", 3, 3, 3, 13, 13, 13, "#bronze"),
        make_box("side_n", 4, 3, 2, 12, 13, 3, "#bronze"),
        make_box("side_s", 4, 3, 13, 12, 13, 14, "#bronze"),
        make_box("side_w", 2, 3, 4, 3, 13, 12, "#bronze"),
        make_box("side_e", 13, 3, 4, 14, 13, 12, "#bronze"),
        # Rivet Belts
        make_box("belt_low", 2, 5, 2, 14, 6, 14, "#bronze"),
        make_box("belt_high", 2, 10, 2, 14, 11, 14, "#bronze"),
        # Top Dome & Steam Outlet
        make_box("dome", 4, 13, 4, 12, 15, 12, "#bronze"),
        make_box("outlet", 6, 15, 6, 10, 16, 10, "#bronze"),
        # Brass Pressure Gauge (Front face has exact 0..16 mapping to boiler_gauge.png!)
        make_box("gauge", 6, 7, 1, 10, 11, 2, "#gauge", {
            "north": [0, 0, 16, 16],
            "south": [0, 0, 16, 16],
            "up": [0, 0, 16, 4],
            "down": [0, 12, 16, 16],
            "east": [12, 0, 16, 16],
            "west": [0, 0, 4, 16]
        })
    ]

    model = {
        "textures": {
            "particle": "industry-made:block/boiler_bronze",
            "bronze": "industry-made:block/boiler_bronze",
            "iron": "industry-made:block/boiler_iron",
            "gauge": "industry-made:block/boiler_gauge"
        },
        "elements": elements,
        "display": STANDARD_BLOCK_DISPLAY
    }

    with open(os.path.join(MODELS_DIR, "low_pressure_boiler.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def build_piston_model():
    elements = [
        # Cast Iron Pedestal
        make_box("pedestal", 1, 0, 1, 15, 3, 15, "#iron"),
        # Bronze Cylinder Body
        make_box("barrel", 3, 3, 3, 13, 11, 13, "#bronze"),
        make_box("head", 4, 11, 4, 12, 12, 12, "#bronze"),
        make_box("intake", 13, 5, 6, 16, 9, 10, "#bronze"),
        make_box("petcock", 0, 6, 7, 3, 8, 9, "#bronze"),
        # Polished Steel Shaft Rod & Ram
        make_box("rod", 6, 12, 6, 10, 15, 10, "#steel"),
        make_box("ram", 5, 15, 5, 11, 16, 11, "#steel")
    ]

    model = {
        "textures": {
            "particle": "industry-made:block/piston_bronze",
            "bronze": "industry-made:block/piston_bronze",
            "iron": "industry-made:block/piston_iron",
            "steel": "industry-made:block/piston_steel"
        },
        "elements": elements,
        "display": STANDARD_BLOCK_DISPLAY
    }

    with open(os.path.join(MODELS_DIR, "steam_piston.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def build_hammer_model():
    elements = [
        # Cast Iron Anvil Base & Stanchions
        make_box("anvil_base", 2, 0, 2, 14, 5, 14, "#frame"),
        make_box("stanchion_l", 2, 5, 10, 5, 15, 14, "#frame"),
        make_box("stanchion_r", 11, 5, 10, 14, 15, 14, "#frame"),
        make_box("hammer_arm", 6, 12, 3, 10, 14, 12, "#frame"),
        # Hardened Steel Anvil Work Die & Drop Tup Head
        make_box("anvil_die", 4, 5, 4, 12, 7, 12, "#steel"),
        make_box("hammer_tup", 5, 8, 2, 11, 12, 6, "#steel"),
        # Bronze Pivot Axle
        make_box("pivot_axle", 3, 12, 11, 13, 14, 13, "#bronze")
    ]

    model = {
        "textures": {
            "particle": "industry-made:block/hammer_frame",
            "frame": "industry-made:block/hammer_frame",
            "steel": "industry-made:block/hammer_steel",
            "bronze": "industry-made:block/hammer_bronze"
        },
        "elements": elements,
        "display": STANDARD_BLOCK_DISPLAY
    }

    with open(os.path.join(MODELS_DIR, "mechanical_hammer.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


if __name__ == "__main__":
    print("Generating Component Textures...")
    create_crucible_textures()
    create_bellows_textures()
    create_boiler_textures()
    create_piston_textures()
    create_hammer_textures()

    print("\nGenerating Accurate 3D Models...")
    build_crucible_model()
    build_bellows_model()
    build_boiler_model()
    build_piston_model()
    build_hammer_model()

    print("\nAll component textures and accurate 3D models generated successfully!")
