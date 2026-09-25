"""
Vanilla+ Asset Generator for Industry Made
Generates 16x16 texel-accurate Minecraft models, textures, blockstates, and items
strictly conforming to Vanilla Jappa art standards (no mixels, no static noise).
"""

import os
import sys
from PIL import Image, ImageDraw

sys.path.append(os.path.dirname(__file__))
from model_builder import VoxelModel

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../.."))
ASSETS_DIR = os.path.join(BASE_DIR, "src/main/resources/assets/industry-made")
TEXTURES_DIR = os.path.join(ASSETS_DIR, "textures/block")
MODELS_DIR = os.path.join(ASSETS_DIR, "models/block")
BLOCKSTATES_DIR = os.path.join(ASSETS_DIR, "blockstates")
ITEMS_DIR = os.path.join(ASSETS_DIR, "items")


# ==============================================================================
# PALETTES (Standard Vanilla+ Jappa compliant)
# ==============================================================================
PAL_REFRACTORY = [
    (48, 36, 30),   # 0: Dark outline / shadow
    (68, 50, 42),   # 1: Dark base
    (92, 68, 56),   # 2: Midtone
    (118, 88, 72),  # 3: Highlight
    (142, 108, 90)  # 4: Bright rim specular
]

PAL_IRON_DARK = [
    (34, 35, 38),   # 0: Cast iron deep shadow
    (48, 50, 54),   # 1: Cast iron dark base
    (66, 68, 74),   # 2: Cast iron midtone
    (88, 91, 98),   # 3: Cast iron highlight
    (115, 118, 126) # 4: Cast iron rim bevel
]

PAL_STEEL = [
    (58, 62, 68),   # 0: Steel shadow
    (82, 88, 96),   # 1: Steel dark base
    (114, 120, 130),# 2: Steel midtone
    (150, 156, 166),# 3: Steel bright face
    (192, 198, 208) # 4: Steel mirror highlight
]

PAL_BRONZE = [
    (92, 48, 24),   # 0: Bronze deep shadow
    (130, 68, 32),  # 1: Bronze base shadow
    (172, 94, 46),  # 2: Bronze midtone
    (208, 122, 62), # 3: Bronze warm highlight
    (238, 154, 88)  # 4: Bronze rim shine
]

PAL_SPRUCE = [
    (44, 30, 18),   # 0: Deep spruce groove
    (62, 42, 24),   # 1: Spruce shadow
    (86, 60, 36),   # 2: Spruce midtone
    (112, 80, 50),  # 3: Spruce highlight
    (136, 98, 62)   # 4: Spruce edge
]

PAL_LEATHER = [
    (64, 32, 18),   # 0: Leather deep crease
    (92, 48, 26),   # 1: Leather shadow
    (124, 68, 38),  # 2: Leather midtone
    (156, 90, 52),  # 3: Leather highlight
    (184, 112, 68)  # 4: Leather rim
]

PAL_BRASS = [
    (120, 90, 30),  # 0: Brass shadow
    (168, 130, 44), # 1: Brass midtone
    (214, 172, 62), # 2: Brass highlight
    (248, 210, 96)  # 3: Brass specular
]


# ==============================================================================
# 1. SMELTING CRUCIBLE
# ==============================================================================
def make_crucible():
    m = VoxelModel("crucible")
    m.set_texture("particle", "industry-made:block/crucible")
    m.set_texture("texture", "industry-made:block/crucible")

    # Clean integer grid coordinates
    m.add_box("base", 2, 0, 2, 14, 2, 14)
    m.add_box("wall_north", 2, 2, 2, 14, 12, 4)
    m.add_box("wall_south", 2, 2, 12, 14, 12, 14)
    m.add_box("wall_west", 2, 2, 4, 4, 12, 12)
    m.add_box("wall_east", 12, 2, 4, 14, 12, 12)
    # Beveled rim
    m.add_box("rim_north", 1, 12, 1, 15, 14, 3)
    m.add_box("rim_south", 1, 12, 13, 15, 14, 15)
    m.add_box("rim_west", 1, 12, 3, 3, 14, 13)
    m.add_box("rim_east", 13, 12, 3, 15, 14, 13)
    # Cast iron side handles
    m.add_box("handle_west", 0, 6, 6, 2, 9, 10)
    m.add_box("handle_east", 14, 6, 6, 16, 9, 10)
    # Basin interior floor
    m.add_box("interior", 4, 2, 4, 12, 3, 12)

    m.export_block_model(os.path.join(MODELS_DIR, "crucible.json"))
    m.export_item_definition(os.path.join(ITEMS_DIR, "crucible.json"), "industry-made:block/crucible")
    m.export_blockstate(os.path.join(BLOCKSTATES_DIR, "crucible.json"), "industry-made:block/crucible", directional=False)

    # Handcrafted 16x16 pixel art texture
    img = Image.new("RGBA", (16, 16), (*PAL_REFRACTORY[2], 255))
    draw = ImageDraw.Draw(img)

    # Top rim band (y: 0..2)
    draw.rectangle([0, 0, 15, 1], fill=(*PAL_REFRACTORY[4], 255))
    draw.line([(0, 2), (15, 2)], fill=(*PAL_REFRACTORY[3], 255))
    # Wall brick body (y: 3..12) with clean brick seams
    for x in range(16):
        for y in range(3, 13):
            # Alternating brick pattern
            row = (y - 3) // 3
            col = (x + (row % 2) * 4) // 4
            if y in [5, 8, 11]:  # Horizontal mortar seam
                draw.point((x, y), fill=(*PAL_REFRACTORY[0], 255))
            elif (x + (row % 2) * 4) % 4 == 0:  # Vertical mortar seam
                draw.point((x, y), fill=(*PAL_REFRACTORY[0], 255))
            elif y in [3, 6, 9]:
                draw.point((x, y), fill=(*PAL_REFRACTORY[3], 255))  # Brick top highlight
            else:
                draw.point((x, y), fill=(*PAL_REFRACTORY[2], 255))
    # Charred fire-box base (y: 13..15)
    draw.rectangle([0, 13, 15, 15], fill=(*PAL_REFRACTORY[1], 255))
    draw.line([(0, 15), (15, 15)], fill=(*PAL_REFRACTORY[0], 255))
    # Add dark iron lug pixels (bottom right corner x: 12..15, y: 12..15 for UV mapping handles)
    draw.rectangle([12, 12, 15, 15], fill=(*PAL_IRON_DARK[2], 255))
    draw.point((12, 12), fill=(*PAL_IRON_DARK[4], 255))
    draw.point((15, 15), fill=(*PAL_IRON_DARK[0], 255))

    img.save(os.path.join(TEXTURES_DIR, "crucible.png"))


# ==============================================================================
# 2. MANUAL BELLOWS
# ==============================================================================
def make_bellows():
    m = VoxelModel("bellows")
    m.set_texture("particle", "industry-made:block/bellows")
    m.set_texture("texture", "industry-made:block/bellows")

    # Base wooden plate
    m.add_box("base_plate", 1, 0, 1, 15, 2, 14)
    # Front nozzle
    m.add_box("nozzle_collar", 6, 1, 13, 10, 5, 15)
    m.add_box("nozzle_pipe", 7, 2, 15, 9, 4, 16)
    # Accordion leather chamber (pleated stepped volume)
    m.add_box("leather_low", 2, 2, 2, 14, 5, 13)
    m.add_box("leather_mid", 1, 5, 1, 15, 7, 14)
    m.add_box("leather_high", 2, 7, 2, 14, 10, 13)
    # Top wooden plate
    m.add_box("top_plate", 1, 10, 1, 15, 12, 14)
    # Rear lever handle
    m.add_box("handle_shaft", 6, 11, 0, 10, 13, 3)

    m.export_block_model(os.path.join(MODELS_DIR, "bellows.json"))
    m.export_item_definition(os.path.join(ITEMS_DIR, "bellows.json"), "industry-made:block/bellows")
    m.export_blockstate(os.path.join(BLOCKSTATES_DIR, "bellows.json"), "industry-made:block/bellows", directional=True)

    # 16x16 Texture
    img = Image.new("RGBA", (16, 16), (*PAL_LEATHER[2], 255))
    draw = ImageDraw.Draw(img)

    # Top spruce board (y: 0..3)
    draw.rectangle([0, 0, 15, 2], fill=(*PAL_SPRUCE[2], 255))
    draw.line([(0, 0), (15, 0)], fill=(*PAL_SPRUCE[4], 255))
    draw.line([(0, 3), (15, 3)], fill=(*PAL_SPRUCE[0], 255))  # Groove
    # Wood grain lines
    draw.line([(4, 0), (4, 2)], fill=(*PAL_SPRUCE[1], 255))
    draw.line([(11, 0), (11, 2)], fill=(*PAL_SPRUCE[3], 255))

    # Leather pleats in middle (y: 4..11)
    # Pleat 1
    draw.line([(0, 4), (15, 4)], fill=(*PAL_LEATHER[4], 255))
    draw.rectangle([0, 5, 15, 6], fill=(*PAL_LEATHER[2], 255))
    draw.line([(0, 7), (15, 7)], fill=(*PAL_LEATHER[0], 255))
    # Pleat 2
    draw.line([(0, 8), (15, 8)], fill=(*PAL_LEATHER[3], 255))
    draw.rectangle([0, 9, 15, 10], fill=(*PAL_LEATHER[2], 255))
    draw.line([(0, 11), (15, 11)], fill=(*PAL_LEATHER[0], 255))

    # Bottom spruce board (y: 12..15)
    draw.rectangle([0, 12, 15, 14], fill=(*PAL_SPRUCE[2], 255))
    draw.line([(0, 12), (15, 12)], fill=(*PAL_SPRUCE[3], 255))
    draw.line([(0, 15), (15, 15)], fill=(*PAL_SPRUCE[0], 255))

    # Front cast-iron nozzle patch (x: 12..15, y: 12..15)
    draw.rectangle([12, 12, 15, 15], fill=(*PAL_IRON_DARK[2], 255))
    draw.point((12, 12), fill=(*PAL_IRON_DARK[4], 255))
    draw.point((15, 15), fill=(*PAL_IRON_DARK[0], 255))

    img.save(os.path.join(TEXTURES_DIR, "bellows.png"))


# ==============================================================================
# 3. LOW PRESSURE BOILER
# ==============================================================================
def make_boiler():
    m = VoxelModel("low_pressure_boiler")
    m.set_texture("particle", "industry-made:block/low_pressure_boiler")
    m.set_texture("texture", "industry-made:block/low_pressure_boiler")

    # 4 Corner Cast Iron Legs
    m.add_box("leg_nw", 1, 0, 1, 4, 3, 4)
    m.add_box("leg_ne", 12, 0, 1, 15, 3, 4)
    m.add_box("leg_sw", 1, 0, 12, 4, 3, 15)
    m.add_box("leg_se", 12, 0, 12, 15, 3, 15)
    # Hearth base
    m.add_box("hearth", 2, 1, 2, 14, 3, 14)
    # Cylindrical bronze chamber (chamfered octagon)
    m.add_box("core", 3, 3, 3, 13, 13, 13)
    m.add_box("side_n", 4, 3, 2, 12, 13, 3)
    m.add_box("side_s", 4, 3, 13, 12, 13, 14)
    m.add_box("side_w", 2, 3, 4, 3, 13, 12)
    m.add_box("side_e", 13, 3, 4, 14, 13, 12)
    # Heavy Bronze Rivet Belts
    m.add_box("belt_low", 2, 5, 2, 14, 6, 14)
    m.add_box("belt_high", 2, 10, 2, 14, 11, 14)
    # Boiler top dome & steam outlet
    m.add_box("dome", 4, 13, 4, 12, 15, 12)
    m.add_box("outlet", 6, 15, 6, 10, 16, 10)
    # Brass Pressure Gauge on front face
    m.add_box("gauge", 6, 7, 1, 10, 11, 2)

    m.export_block_model(os.path.join(MODELS_DIR, "low_pressure_boiler.json"))
    m.export_item_definition(os.path.join(ITEMS_DIR, "low_pressure_boiler.json"), "industry-made:block/low_pressure_boiler")
    m.export_blockstate(os.path.join(BLOCKSTATES_DIR, "low_pressure_boiler.json"), "industry-made:block/low_pressure_boiler", directional=True)

    # 16x16 Texture
    img = Image.new("RGBA", (16, 16), (*PAL_BRONZE[2], 255))
    draw = ImageDraw.Draw(img)

    # Dome top bevel (y: 0..1)
    draw.line([(0, 0), (15, 0)], fill=(*PAL_BRONZE[4], 255))
    draw.line([(0, 1), (15, 1)], fill=(*PAL_BRONZE[3], 255))

    # Upper rivet belt (y: 3..4)
    draw.line([(0, 3), (15, 3)], fill=(*PAL_BRONZE[1], 255))
    for x in range(1, 16, 3):
        draw.point((x, 3), fill=(*PAL_BRASS[3], 255))  # Rivet highlight
        draw.point((x + 1, 3), fill=(*PAL_BRASS[0], 255))
    draw.line([(0, 4), (15, 4)], fill=(*PAL_BRONZE[0], 255))

    # Lower rivet belt (y: 8..9)
    draw.line([(0, 8), (15, 8)], fill=(*PAL_BRONZE[1], 255))
    for x in range(1, 16, 3):
        draw.point((x, 8), fill=(*PAL_BRASS[3], 255))  # Rivet highlight
        draw.point((x + 1, 8), fill=(*PAL_BRASS[0], 255))
    draw.line([(0, 9), (15, 9)], fill=(*PAL_BRONZE[0], 255))

    # Cast Iron Legs / Base (y: 13..15)
    draw.rectangle([0, 13, 15, 15], fill=(*PAL_IRON_DARK[1], 255))
    draw.line([(0, 13), (15, 13)], fill=(*PAL_IRON_DARK[3], 255))
    draw.line([(0, 15), (15, 15)], fill=(*PAL_IRON_DARK[0], 255))

    # Brass Pressure Gauge (x: 11..15, y: 11..15)
    draw.rectangle([11, 11, 15, 15], fill=(*PAL_BRASS[1], 255))
    draw.rectangle([12, 12, 14, 14], fill=(240, 242, 240, 255))  # White dial face
    draw.point((13, 13), fill=(200, 30, 30, 255))  # Red needle
    draw.point((13, 12), fill=(160, 20, 20, 255))

    img.save(os.path.join(TEXTURES_DIR, "low_pressure_boiler.png"))


# ==============================================================================
# 4. STEAM PISTON
# ==============================================================================
def make_steam_piston():
    m = VoxelModel("steam_piston")
    m.set_texture("particle", "industry-made:block/steam_piston")
    m.set_texture("texture", "industry-made:block/steam_piston")

    # Cast iron pedestal
    m.add_box("pedestal", 1, 0, 1, 15, 3, 15)
    # Bronze cylinder barrel
    m.add_box("barrel", 3, 3, 3, 13, 11, 13)
    # Gland seal head
    m.add_box("head", 4, 11, 4, 12, 12, 12)
    # Polished steel shaft rod
    m.add_box("rod", 6, 12, 6, 10, 15, 10)
    # Hammer ram plate
    m.add_box("ram", 5, 15, 5, 11, 16, 11)
    # Steam pipe intake (east)
    m.add_box("intake", 13, 5, 6, 16, 9, 10)
    # Relief petcock (west)
    m.add_box("petcock", 0, 6, 7, 3, 8, 9)

    m.export_block_model(os.path.join(MODELS_DIR, "steam_piston.json"))
    m.export_item_definition(os.path.join(ITEMS_DIR, "steam_piston.json"), "industry-made:block/steam_piston")
    m.export_blockstate(os.path.join(BLOCKSTATES_DIR, "steam_piston.json"), "industry-made:block/steam_piston", directional=False)

    # 16x16 Texture
    img = Image.new("RGBA", (16, 16), (*PAL_BRONZE[2], 255))
    draw = ImageDraw.Draw(img)

    # Steel rod & ram patch (x: 10..15, y: 0..5)
    draw.rectangle([10, 0, 15, 5], fill=(*PAL_STEEL[2], 255))
    draw.line([(11, 0), (11, 5)], fill=(*PAL_STEEL[4], 255))  # Reflection
    draw.line([(14, 0), (14, 5)], fill=(*PAL_STEEL[0], 255))  # Shadow

    # Bronze cylinder barrel (y: 2..11)
    draw.line([(0, 2), (9, 2)], fill=(*PAL_BRONZE[4], 255))
    draw.line([(0, 5), (9, 5)], fill=(*PAL_BRONZE[1], 255))
    for x in range(1, 9, 3):
        draw.point((x, 5), fill=(*PAL_BRASS[2], 255))
    draw.line([(0, 10), (15, 10)], fill=(*PAL_BRONZE[1], 255))

    # Cast Iron Pedestal (y: 12..15)
    draw.rectangle([0, 12, 15, 15], fill=(*PAL_IRON_DARK[1], 255))
    draw.line([(0, 12), (15, 12)], fill=(*PAL_IRON_DARK[3], 255))
    draw.line([(0, 15), (15, 15)], fill=(*PAL_IRON_DARK[0], 255))

    img.save(os.path.join(TEXTURES_DIR, "steam_piston.png"))


# ==============================================================================
# 5. MECHANICAL FORGE HAMMER
# ==============================================================================
def make_mechanical_hammer():
    m = VoxelModel("mechanical_hammer")
    m.set_texture("particle", "industry-made:block/mechanical_hammer")
    m.set_texture("texture", "industry-made:block/mechanical_hammer")

    # Cast Iron Anvil Base
    m.add_box("anvil_base", 2, 0, 2, 14, 5, 14)
    # Hardened Steel Anvil Work Die
    m.add_box("anvil_die", 4, 5, 4, 12, 7, 12)
    # Rear Structural Uprights
    m.add_box("stanchion_l", 2, 5, 10, 5, 15, 14)
    m.add_box("stanchion_r", 11, 5, 10, 14, 15, 14)
    # Pivot Axle
    m.add_box("pivot_axle", 3, 12, 11, 13, 14, 13)
    # Hammer Lever Arm
    m.add_box("hammer_arm", 6, 12, 3, 10, 14, 12)
    # Forged Steel Hammer Tup Head
    m.add_box("hammer_tup", 5, 8, 2, 11, 12, 6)

    m.export_block_model(os.path.join(MODELS_DIR, "mechanical_hammer.json"))
    m.export_item_definition(os.path.join(ITEMS_DIR, "mechanical_hammer.json"), "industry-made:block/mechanical_hammer")
    m.export_blockstate(os.path.join(BLOCKSTATES_DIR, "mechanical_hammer.json"), "industry-made:block/mechanical_hammer", directional=True)

    # 16x16 Texture
    img = Image.new("RGBA", (16, 16), (*PAL_IRON_DARK[1], 255))
    draw = ImageDraw.Draw(img)

    # Steel Anvil & Hammer Face (x: 0..6, y: 0..6)
    draw.rectangle([0, 0, 6, 6], fill=(*PAL_STEEL[2], 255))
    draw.line([(0, 0), (6, 0)], fill=(*PAL_STEEL[4], 255))
    draw.line([(0, 0), (0, 6)], fill=(*PAL_STEEL[3], 255))
    draw.line([(6, 1), (6, 6)], fill=(*PAL_STEEL[0], 255))
    draw.line([(1, 6), (6, 6)], fill=(*PAL_STEEL[0], 255))

    # Bronze Pivot Bushings (x: 8..15, y: 0..4)
    draw.rectangle([8, 0, 15, 4], fill=(*PAL_BRONZE[2], 255))
    draw.line([(8, 0), (15, 0)], fill=(*PAL_BRONZE[4], 255))
    draw.line([(8, 4), (15, 4)], fill=(*PAL_BRONZE[0], 255))

    # Cast Iron Anvil & Frame Body (y: 7..15)
    draw.rectangle([0, 7, 15, 15], fill=(*PAL_IRON_DARK[2], 255))
    draw.line([(0, 7), (15, 7)], fill=(*PAL_IRON_DARK[3], 255))
    draw.line([(0, 15), (15, 15)], fill=(*PAL_IRON_DARK[0], 255))
    # Framing bolts
    for (x, y) in [(2, 9), (13, 9), (2, 13), (13, 13)]:
        draw.point((x, y), fill=(*PAL_IRON_DARK[4], 255))
        draw.point((x + 1, y), fill=(*PAL_IRON_DARK[0], 255))

    img.save(os.path.join(TEXTURES_DIR, "mechanical_hammer.png"))


if __name__ == "__main__":
    make_crucible()
    make_bellows()
    make_boiler()
    make_steam_piston()
    make_mechanical_hammer()
    print("Vanilla+ 16x16 assets successfully generated!")
