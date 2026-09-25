"""
Generate Dynamic/Responsive 3D Models, Blockstates, and Textures for:
- Low Pressure Boiler Pressure Gauges (p0, p1, p2, p3)
- Bronze Valve Pipe (Open / Closed states, multipart)
- Bronze Gauge Pipe (Pressure level 0, 1, 2, 3, multipart)
Industry Made - Minecraft 26.1.2 Fabric
"""

import os
import json
import math
from PIL import Image, ImageDraw

ROOT_DIR = r"D:\.Minecraft Mod Development\industry-made-fabric-26.1.2"
ASSETS_DIR = os.path.join(ROOT_DIR, "src", "main", "resources", "assets", "industry-made")

STANDARD_BLOCK_DISPLAY = {
    "gui": {
        "rotation": [30, 225, 0],
        "translation": [0, 0, 0],
        "scale": [0.625, 0.625, 0.625]
    },
    "ground": {
        "rotation": [0, 0, 0],
        "translation": [0, 3, 0],
        "scale": [0.25, 0.25, 0.25]
    },
    "fixed": {
        "rotation": [0, 0, 0],
        "translation": [0, 0, 0],
        "scale": [0.5, 0.5, 0.5]
    },
    "thirdperson_righthand": {
        "rotation": [75, 45, 0],
        "translation": [0, 2.5, 0],
        "scale": [0.375, 0.375, 0.375]
    },
    "thirdperson_lefthand": {
        "rotation": [75, 45, 0],
        "translation": [0, 2.5, 0],
        "scale": [0.375, 0.375, 0.375]
    },
    "firstperson_righthand": {
        "rotation": [0, 45, 0],
        "translation": [0, 0, 0],
        "scale": [0.4, 0.4, 0.4]
    },
    "firstperson_lefthand": {
        "rotation": [0, 225, 0],
        "translation": [0, 0, 0],
        "scale": [0.4, 0.4, 0.4]
    }
}

def create_element(name, from_pos, to_pos, texture="#pipe"):
    x1, y1, z1 = from_pos
    x2, y2, z2 = to_pos
    return {
        "name": name,
        "from": [round(float(v), 3) for v in from_pos],
        "to": [round(float(v), 3) for v in to_pos],
        "shade": True,
        "faces": {
            "north": {"uv": [round(16.0 - x2, 2), round(16.0 - y2, 2), round(16.0 - x1, 2), round(16.0 - y1, 2)], "texture": texture},
            "south": {"uv": [round(x1, 2), round(16.0 - y2, 2), round(x2, 2), round(16.0 - y1, 2)], "texture": texture},
            "west":  {"uv": [round(z1, 2), round(16.0 - y2, 2), round(z2, 2), round(16.0 - y1, 2)], "texture": texture},
            "east":  {"uv": [round(16.0 - z2, 2), round(16.0 - y2, 2), round(16.0 - z1, 2), round(16.0 - y1, 2)], "texture": texture},
            "up":    {"uv": [round(x1, 2), round(z1, 2), round(x2, 2), round(z2, 2)], "texture": texture},
            "down":  {"uv": [round(x1, 2), round(16.0 - z2, 2), round(x2, 2), round(16.0 - z1, 2)], "texture": texture}
        }
    }

def generate_gauge_textures():
    base_path = os.path.join(ASSETS_DIR, "textures", "block", "boiler_gauge.png")
    base = Image.open(base_path).convert("RGBA")

    # Clean the inner circular face of the gauge
    cleaned = base.copy()
    for y in range(4, 12):
        for x in range(4, 12):
            dist = math.hypot(x - 7.5, y - 7.5)
            if dist < 4.1:
                cleaned.putpixel((x, y), (232, 228, 215, 255)) # Warm cream parchment face

    # Draw dial tick marks
    cleaned.putpixel((4, 8), (70, 70, 70, 255))   # Low stop pin (0 bar)
    cleaned.putpixel((5, 5), (50, 140, 50, 255))  # Green zone start (1.5 bar)
    cleaned.putpixel((7, 4), (50, 160, 50, 255))  # Green zone optimal (3.0 bar)
    cleaned.putpixel((8, 4), (50, 160, 50, 255))
    cleaned.putpixel((10, 5), (200, 45, 45, 255)) # Red zone warning (5.5 bar)
    cleaned.putpixel((11, 7), (210, 30, 30, 255)) # Red zone danger (6.0 bar)

    # 4 distinct target positions (screen coordinates, center at 7.5, 7.5)
    # Level 0: 7 o'clock (x=4, y=10)
    # Level 1: 10 o'clock (x=5, y=5)
    # Level 2: 12 o'clock (x=7.5, y=3.8)
    # Level 3: 2 o'clock (x=10.5, y=5)
    needle_targets = [
        ((4.2, 9.5), "boiler_gauge_0.png"),
        ((5.0, 5.2), "boiler_gauge_1.png"),
        ((7.5, 3.8), "boiler_gauge_2.png"),
        ((10.5, 5.2), "boiler_gauge_3.png")
    ]

    for (tx, ty), filename in needle_targets:
        img = cleaned.copy()
        draw = ImageDraw.Draw(img)
        # Hub in center
        draw.ellipse((6.2, 6.2, 8.8, 8.8), fill=(45, 45, 45, 255))
        draw.point((7, 7), (80, 80, 80, 255))
        # Needle pointer line
        draw.line([(7.5, 7.5), (tx, ty)], fill=(25, 25, 25, 255), width=1)
        # Needle red tip for visibility
        draw.point((int(round(tx)), int(round(ty))), (190, 30, 30, 255))

        out_path = os.path.join(ASSETS_DIR, "textures", "block", filename)
        img.save(out_path)
        print(f"Generated gauge texture: {out_path}")

def generate_valve_wheel_texture():
    # 16x16 Industrial Cast Iron Red Handwheel
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    red_dark = (140, 25, 25, 255)
    red_mid = (185, 40, 40, 255)
    red_bright = (220, 70, 70, 255)
    hub_metal = (80, 75, 70, 255)
    hub_highlight = (130, 125, 120, 255)

    # Circular rim of handwheel (radius 6.5)
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if 4.8 <= d <= 7.2:
                # Top-left highlight, bottom-right shadow
                if y < x:
                    draw.point((x, y), red_bright)
                elif y > x + 2:
                    draw.point((x, y), red_dark)
                else:
                    draw.point((x, y), red_mid)

    # 4 Spokes connecting rim to hub
    for i in range(1, 15):
        if 2 <= i <= 13:
            draw.point((i, 7), red_mid)
            draw.point((i, 8), red_dark)
            draw.point((7, i), red_mid)
            draw.point((8, i), red_dark)

    # Central metal hub & retaining nut
    draw.ellipse((5.5, 5.5, 9.5, 9.5), fill=hub_metal)
    draw.ellipse((6.5, 6.5, 8.5, 8.5), fill=hub_highlight)
    draw.point((7, 7), (40, 40, 40, 255))

    out_path = os.path.join(ASSETS_DIR, "textures", "block", "bronze_valve_wheel.png")
    img.save(out_path)
    print(f"Generated valve wheel texture: {out_path}")

def generate_boiler_models_and_blockstate():
    # Read original low_pressure_boiler.json to get base elements
    boiler_orig_path = os.path.join(ASSETS_DIR, "models", "block", "low_pressure_boiler.json")
    with open(boiler_orig_path, "r", encoding="utf-8") as f:
        boiler_base = json.load(f)

    # Create 4 models for pressure levels 0, 1, 2, 3
    for p in range(4):
        model_copy = json.loads(json.dumps(boiler_base))
        model_copy["textures"]["gauge"] = f"industry-made:block/boiler_gauge_{p}"
        p_path = os.path.join(ASSETS_DIR, "models", "block", f"low_pressure_boiler_p{p}.json")
        with open(p_path, "w", encoding="utf-8") as f:
            json.dump(model_copy, f, indent=2)
        print(f"Generated boiler model: {p_path}")

    # Generate blockstate variants for low_pressure_boiler
    variants = {}
    facings = [
        ("north", 0),
        ("east", 90),
        ("south", 180),
        ("west", 270)
    ]
    for facing_name, y_rot in facings:
        for lit in [False, True]:
            for p in range(4):
                key = f"facing={facing_name},lit={str(lit).lower()},pressure_level={p}"
                entry = {
                    "model": f"industry-made:block/low_pressure_boiler_p{p}"
                }
                if y_rot != 0:
                    entry["y"] = y_rot
                variants[key] = entry

    bs_path = os.path.join(ASSETS_DIR, "blockstates", "low_pressure_boiler.json")
    with open(bs_path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print(f"Updated boiler blockstate: {bs_path}")

def generate_valve_pipe_assets():
    # 1. Valve Core Open Model
    core_open = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe",
            "wheel": "industry-made:block/bronze_valve_wheel"
        },
        "elements": [
            # Main pipe junction core
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
            # Valve bonnet collar
            create_element("valve_bonnet", [6.0, 11.0, 6.0], [10.0, 12.0, 10.0], "#pipe"),
            # Valve spindle / stem (raised when open)
            create_element("valve_stem", [7.0, 12.0, 7.0], [9.0, 14.5, 9.0], "#pipe"),
            # Cast iron handwheel (horizontal wheel on top at y=14.5 to 15.5)
            {
                "name": "valve_wheel",
                "from": [3.5, 14.5, 3.5],
                "to": [12.5, 15.5, 12.5],
                "shade": True,
                "faces": {
                    "north": {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "south": {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "west":  {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "east":  {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "up":    {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel"},
                    "down":  {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel"}
                }
            }
        ]
    }
    open_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_valve_pipe_core_open.json")
    with open(open_path, "w", encoding="utf-8") as f:
        json.dump(core_open, f, indent=2)
    print(f"Generated valve open core: {open_path}")

    # 2. Valve Core Closed Model (wheel screwed down at y=12.5 to 13.5, stem lower)
    core_closed = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe",
            "wheel": "industry-made:block/bronze_valve_wheel"
        },
        "elements": [
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
            create_element("valve_bonnet", [6.0, 11.0, 6.0], [10.0, 12.0, 10.0], "#pipe"),
            create_element("valve_stem", [7.0, 12.0, 7.0], [9.0, 13.0, 9.0], "#pipe"),
            {
                "name": "valve_wheel",
                "from": [3.5, 12.5, 3.5],
                "to": [12.5, 13.5, 12.5],
                "shade": True,
                "faces": {
                    "north": {"uv": [3.5, 12.5, 12.5, 13.5], "texture": "#wheel"},
                    "south": {"uv": [3.5, 12.5, 12.5, 13.5], "texture": "#wheel"},
                    "west":  {"uv": [3.5, 12.5, 12.5, 13.5], "texture": "#wheel"},
                    "east":  {"uv": [3.5, 12.5, 12.5, 13.5], "texture": "#wheel"},
                    "up":    {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel", "rotation": 45},
                    "down":  {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel", "rotation": 45}
                }
            }
        ]
    }
    closed_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_valve_pipe_core_closed.json")
    with open(closed_path, "w", encoding="utf-8") as f:
        json.dump(core_closed, f, indent=2)
    print(f"Generated valve closed core: {closed_path}")

    # 3. Valve Inventory Model
    inv_model = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe",
            "wheel": "industry-made:block/bronze_valve_wheel"
        },
        "display": STANDARD_BLOCK_DISPLAY,
        "elements": [
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
            create_element("pipe_shaft_north", [5.0, 5.0, 0.0], [11.0, 11.0, 5.0], "#pipe"),
            create_element("pipe_flange_north", [4.0, 4.0, 0.0], [12.0, 12.0, 1.5], "#pipe"),
            create_element("pipe_shaft_south", [5.0, 5.0, 11.0], [11.0, 11.0, 16.0], "#pipe"),
            create_element("pipe_flange_south", [4.0, 4.0, 14.5], [12.0, 12.0, 16.0], "#pipe"),
            create_element("valve_bonnet", [6.0, 11.0, 6.0], [10.0, 12.0, 10.0], "#pipe"),
            create_element("valve_stem", [7.0, 12.0, 7.0], [9.0, 14.5, 9.0], "#pipe"),
            {
                "name": "valve_wheel",
                "from": [3.5, 14.5, 3.5],
                "to": [12.5, 15.5, 12.5],
                "shade": True,
                "faces": {
                    "north": {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "south": {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "west":  {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "east":  {"uv": [3.5, 14.5, 12.5, 15.5], "texture": "#wheel"},
                    "up":    {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel"},
                    "down":  {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#wheel"}
                }
            }
        ]
    }
    inv_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_valve_pipe_inventory.json")
    with open(inv_path, "w", encoding="utf-8") as f:
        json.dump(inv_model, f, indent=2)
    print(f"Generated valve inventory model: {inv_path}")

    # Item model
    item_model = {
        "model": {
            "type": "minecraft:model",
            "model": "industry-made:block/bronze_valve_pipe_inventory"
        }
    }
    item_path = os.path.join(ASSETS_DIR, "items", "bronze_valve_pipe.json")
    with open(item_path, "w", encoding="utf-8") as f:
        json.dump(item_model, f, indent=2)

    # 4. Valve Multipart Blockstate
    valve_bstate = {
        "multipart": [
            {
                "when": { "open": "true" },
                "apply": { "model": "industry-made:block/bronze_valve_pipe_core_open" }
            },
            {
                "when": { "open": "false" },
                "apply": { "model": "industry-made:block/bronze_valve_pipe_core_closed" }
            },
            {
                "when": { "north": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm" }
            },
            {
                "when": { "south": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 180 }
            },
            {
                "when": { "west": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 270 }
            },
            {
                "when": { "east": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 90 }
            },
            {
                "when": { "up": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "x": 270 }
            },
            {
                "when": { "down": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "x": 90 }
            }
        ]
    }
    vb_path = os.path.join(ASSETS_DIR, "blockstates", "bronze_valve_pipe.json")
    with open(vb_path, "w", encoding="utf-8") as f:
        json.dump(valve_bstate, f, indent=2)
    print(f"Generated valve blockstate: {vb_path}")

def generate_gauge_pipe_assets():
    # Generate 4 core models for pressure level 0, 1, 2, 3
    # Mounted on the north face (or vertical stem on top)
    for p in range(4):
        gauge_model = {
            "parent": "block/block",
            "textures": {
                "particle": "industry-made:block/bronze_pipe",
                "pipe": "industry-made:block/bronze_pipe",
                "gauge": f"industry-made:block/boiler_gauge_{p}"
            },
            "elements": [
                # Pipe core
                create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
                # Brass mounting stem on top of pipe
                create_element("gauge_stem", [7.0, 11.0, 7.0], [9.0, 12.5, 9.0], "#pipe"),
                # Gauge dial casing box (width 6, height 6, depth 3) on top, facing north
                {
                    "name": "gauge_casing",
                    "from": [5.0, 11.5, 6.5],
                    "to": [11.0, 16.0, 9.5],
                    "shade": True,
                    "faces": {
                        "north": {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#gauge"},
                        "south": {"uv": [5.0, 11.5, 11.0, 16.0], "texture": "#pipe"},
                        "west":  {"uv": [6.5, 11.5, 9.5, 16.0], "texture": "#pipe"},
                        "east":  {"uv": [6.5, 11.5, 9.5, 16.0], "texture": "#pipe"},
                        "up":    {"uv": [5.0, 6.5, 11.0, 9.5], "texture": "#pipe"},
                        "down":  {"uv": [5.0, 6.5, 11.0, 9.5], "texture": "#pipe"}
                    }
                }
            ]
        }
        gm_path = os.path.join(ASSETS_DIR, "models", "block", f"bronze_gauge_pipe_core_p{p}.json")
        with open(gm_path, "w", encoding="utf-8") as f:
            json.dump(gauge_model, f, indent=2)
        print(f"Generated gauge pipe model: {gm_path}")

    # Inventory Model for Gauge Pipe
    inv_model = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe",
            "gauge": "industry-made:block/boiler_gauge_2"
        },
        "display": STANDARD_BLOCK_DISPLAY,
        "elements": [
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
            create_element("pipe_shaft_north", [5.0, 5.0, 0.0], [11.0, 11.0, 5.0], "#pipe"),
            create_element("pipe_flange_north", [4.0, 4.0, 0.0], [12.0, 12.0, 1.5], "#pipe"),
            create_element("pipe_shaft_south", [5.0, 5.0, 11.0], [11.0, 11.0, 16.0], "#pipe"),
            create_element("pipe_flange_south", [4.0, 4.0, 14.5], [12.0, 12.0, 16.0], "#pipe"),
            create_element("gauge_stem", [7.0, 11.0, 7.0], [9.0, 12.5, 9.0], "#pipe"),
            {
                "name": "gauge_casing",
                "from": [5.0, 11.5, 6.5],
                "to": [11.0, 16.0, 9.5],
                "shade": True,
                "faces": {
                    "north": {"uv": [0.0, 0.0, 16.0, 16.0], "texture": "#gauge"},
                    "south": {"uv": [5.0, 11.5, 11.0, 16.0], "texture": "#pipe"},
                    "west":  {"uv": [6.5, 11.5, 9.5, 16.0], "texture": "#pipe"},
                    "east":  {"uv": [6.5, 11.5, 9.5, 16.0], "texture": "#pipe"},
                    "up":    {"uv": [5.0, 6.5, 11.0, 9.5], "texture": "#pipe"},
                    "down":  {"uv": [5.0, 6.5, 11.0, 9.5], "texture": "#pipe"}
                }
            }
        ]
    }
    inv_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_gauge_pipe_inventory.json")
    with open(inv_path, "w", encoding="utf-8") as f:
        json.dump(inv_model, f, indent=2)
    print(f"Generated gauge pipe inventory model: {inv_path}")

    # Item model
    item_model = {
        "model": {
            "type": "minecraft:model",
            "model": "industry-made:block/bronze_gauge_pipe_inventory"
        }
    }
    item_path = os.path.join(ASSETS_DIR, "items", "bronze_gauge_pipe.json")
    with open(item_path, "w", encoding="utf-8") as f:
        json.dump(item_model, f, indent=2)

    # Multipart blockstate for bronze_gauge_pipe
    gauge_bstate = {
        "multipart": [
            {
                "when": { "pressure_level": "0" },
                "apply": { "model": "industry-made:block/bronze_gauge_pipe_core_p0" }
            },
            {
                "when": { "pressure_level": "1" },
                "apply": { "model": "industry-made:block/bronze_gauge_pipe_core_p1" }
            },
            {
                "when": { "pressure_level": "2" },
                "apply": { "model": "industry-made:block/bronze_gauge_pipe_core_p2" }
            },
            {
                "when": { "pressure_level": "3" },
                "apply": { "model": "industry-made:block/bronze_gauge_pipe_core_p3" }
            },
            {
                "when": { "north": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm" }
            },
            {
                "when": { "south": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 180 }
            },
            {
                "when": { "west": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 270 }
            },
            {
                "when": { "east": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "y": 90 }
            },
            {
                "when": { "up": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "x": 270 }
            },
            {
                "when": { "down": "true" },
                "apply": { "model": "industry-made:block/bronze_steam_pipe_arm", "x": 90 }
            }
        ]
    }
    gb_path = os.path.join(ASSETS_DIR, "blockstates", "bronze_gauge_pipe.json")
    with open(gb_path, "w", encoding="utf-8") as f:
        json.dump(gauge_bstate, f, indent=2)
    print(f"Generated gauge blockstate: {gb_path}")

if __name__ == "__main__":
    generate_gauge_textures()
    generate_valve_wheel_texture()
    generate_boiler_models_and_blockstate()
    generate_valve_pipe_assets()
    generate_gauge_pipe_assets()
    print("All dynamic assets generated successfully!")
