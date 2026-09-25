"""
Generate 3D Models, Blockstate, and Textures for Bronze Steam Pipe
Industry Made - Minecraft 26.1.2 Fabric
"""

import os
import json
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

def generate_texture():
    # 16x16 bronze pipe texture
    img = Image.new("RGBA", (16, 16), (180, 115, 55, 255))
    draw = ImageDraw.Draw(img)

    # Shading gradient and metal bands
    bronze_base = (195, 125, 60, 255)
    bronze_dark = (145, 88, 38, 255)
    bronze_highlight = (225, 155, 85, 255)
    bronze_shadow = (110, 65, 25, 255)
    rivet_color = (235, 175, 100, 255)

    for y in range(16):
        for x in range(16):
            # Base metal noise
            val = (x * 7 + y * 13) % 5
            if val == 0:
                draw.point((x, y), bronze_dark)
            elif val == 1:
                draw.point((x, y), bronze_highlight)
            else:
                draw.point((x, y), bronze_base)

    # Darker borders / seams
    for x in range(16):
        draw.point((x, 0), bronze_highlight)
        draw.point((x, 15), bronze_shadow)
    for y in range(16):
        draw.point((0, y), bronze_shadow)
        draw.point((15, y), bronze_dark)

    # Rivets at corners
    for rx, ry in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        draw.point((rx, ry), rivet_color)
        draw.point((rx + 1, ry), bronze_shadow)

    tex_path = os.path.join(ASSETS_DIR, "textures", "block", "bronze_pipe.png")
    img.save(tex_path)
    print(f"Saved texture: {tex_path}")

def generate_models():
    # 1. Pipe Core Model
    core_model = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe"
        },
        "elements": [
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe")
        ]
    }
    core_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_steam_pipe_core.json")
    with open(core_path, "w", encoding="utf-8") as f:
        json.dump(core_model, f, indent=2)
    print(f"Saved core model: {core_path}")

    # 2. Pipe Arm Model (North pointing: from z=0 to z=5)
    arm_model = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe"
        },
        "elements": [
            # Main cylindrical shaft
            create_element("pipe_shaft", [5.0, 5.0, 0.0], [11.0, 11.0, 5.0], "#pipe"),
            # Flange collar ring at boundary
            create_element("pipe_flange", [4.0, 4.0, 0.0], [12.0, 12.0, 1.5], "#pipe")
        ]
    }
    arm_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_steam_pipe_arm.json")
    with open(arm_path, "w", encoding="utf-8") as f:
        json.dump(arm_model, f, indent=2)
    print(f"Saved arm model: {arm_path}")

    # 3. Pipe Inventory Model (Core + North arm + South arm)
    inv_model = {
        "parent": "block/block",
        "textures": {
            "particle": "industry-made:block/bronze_pipe",
            "pipe": "industry-made:block/bronze_pipe"
        },
        "display": STANDARD_BLOCK_DISPLAY,
        "elements": [
            create_element("pipe_core", [5.0, 5.0, 5.0], [11.0, 11.0, 11.0], "#pipe"),
            create_element("pipe_shaft_north", [5.0, 5.0, 0.0], [11.0, 11.0, 5.0], "#pipe"),
            create_element("pipe_flange_north", [4.0, 4.0, 0.0], [12.0, 12.0, 1.5], "#pipe"),
            create_element("pipe_shaft_south", [5.0, 5.0, 11.0], [11.0, 11.0, 16.0], "#pipe"),
            create_element("pipe_flange_south", [4.0, 4.0, 14.5], [12.0, 12.0, 16.0], "#pipe")
        ]
    }
    inv_path = os.path.join(ASSETS_DIR, "models", "block", "bronze_steam_pipe_inventory.json")
    with open(inv_path, "w", encoding="utf-8") as f:
        json.dump(inv_model, f, indent=2)
    print(f"Saved inventory model: {inv_path}")

    # 4. Item Model definition
    item_model = {
        "model": {
            "type": "minecraft:model",
            "model": "industry-made:block/bronze_steam_pipe_inventory"
        }
    }
    item_path = os.path.join(ASSETS_DIR, "items", "bronze_steam_pipe.json")
    with open(item_path, "w", encoding="utf-8") as f:
        json.dump(item_model, f, indent=2)
    print(f"Saved item model: {item_path}")

    # 5. Blockstate Multipart
    blockstate = {
        "multipart": [
            {
                "apply": { "model": "industry-made:block/bronze_steam_pipe_core" }
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
    bstate_path = os.path.join(ASSETS_DIR, "blockstates", "bronze_steam_pipe.json")
    with open(bstate_path, "w", encoding="utf-8") as f:
        json.dump(blockstate, f, indent=2)
    print(f"Saved blockstate: {bstate_path}")

if __name__ == "__main__":
    generate_texture()
    generate_models()
    print("Done generating steam pipe assets!")
