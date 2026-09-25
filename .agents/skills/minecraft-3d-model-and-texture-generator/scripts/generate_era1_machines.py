"""
Generates rich 3D voxel models, blockstates, modern item definitions,
and pixel art textures for all Era 1 machinery in Industry Made.
"""

import os
import sys
from PIL import Image, ImageDraw

# Add current directory to path
sys.path.append(os.path.dirname(__file__))
from model_builder import VoxelModel, create_base_canvas, apply_subtle_noise, add_bevel_border, add_rivet

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
# 1. SMELTING CRUCIBLE
# ==============================================================================
def generate_crucible():
    # Model
    model = VoxelModel("crucible")
    model.set_texture("particle", "industry-made:block/crucible")
    model.set_texture("texture", "industry-made:block/crucible")

    # Base bottom plate (2..14 horizontally, 0..3 height)
    model.add_box("base", 2, 0, 2, 14, 3, 14)
    # North wall
    model.add_box("wall_north", 2, 3, 2, 14, 14, 4)
    # South wall
    model.add_box("wall_south", 2, 3, 12, 14, 14, 14)
    # West wall
    model.add_box("wall_west", 2, 3, 4, 4, 14, 12)
    # East wall
    model.add_box("wall_east", 12, 3, 4, 14, 14, 12)
    # Top rim lip overhang
    model.add_box("rim_north", 1.5, 13, 1.5, 14.5, 14.5, 3.5)
    model.add_box("rim_south", 1.5, 13, 12.5, 14.5, 14.5, 14.5)
    model.add_box("rim_west", 1.5, 13, 3.5, 3.5, 14.5, 12.5)
    model.add_box("rim_east", 12.5, 13, 3.5, 14.5, 14.5, 12.5)
    # Side lifting lugs / iron handles
    model.add_box("handle_west", 0.5, 8, 6, 2, 10, 10)
    model.add_box("handle_east", 14, 8, 6, 15.5, 10, 10)
    # Interior bottom basin
    model.add_box("interior_bottom", 4, 3, 4, 12, 3.5, 12)

    model.export_block_model(os.path.join(MODELS_DIR, "crucible.json"))
    model.export_item_definition(os.path.join(ITEMS_DIR, "crucible.json"), "industry-made:block/crucible")
    model.export_blockstate(os.path.join(BLOCKSTATES_DIR, "crucible.json"), "industry-made:block/crucible", directional=False)

    # Texture: 32x32 dark refractory ceramic with charred fire-streaks & wrought iron lugs
    tex = create_base_canvas(32, 32, (62, 50, 46))
    apply_subtle_noise(tex, amount=12, seed=101)
    draw = ImageDraw.Draw(tex)
    # Charred fire markings at bottom (y: 20..31)
    for y in range(22, 32):
        for x in range(32):
            if (x * 7 + y * 13) % 5 == 0:
                draw.point((x, y), fill=(38, 30, 28, 255))
    # Reinforced dark iron rim band at top (y: 0..3)
    draw.rectangle([0, 0, 31, 3], fill=(42, 44, 46, 255))
    draw.rectangle([0, 0, 31, 0], fill=(68, 70, 74, 255))  # highlight
    # Side handles region (around x: 24..31, y: 12..18)
    draw.rectangle([25, 13, 30, 18], fill=(55, 57, 60, 255))
    add_bevel_border(tex, (80, 68, 62), (32, 26, 24))
    tex.save(os.path.join(TEXTURES_DIR, "crucible.png"))
    print("Generated Crucible assets.")


# ==============================================================================
# 2. MANUAL BELLOWS
# ==============================================================================
def generate_bellows():
    model = VoxelModel("bellows")
    model.set_texture("particle", "industry-made:block/bellows")
    model.set_texture("texture", "industry-made:block/bellows")

    # Base wooden plate (0..16, 0..2, 0..14)
    model.add_box("base_plate", 1, 0, 1, 15, 2, 14)
    # Front air nozzle (collar + spout extending south towards +Z)
    model.add_box("nozzle_collar", 5, 1, 13, 11, 5, 15)
    model.add_box("nozzle_pipe", 6, 2, 15, 10, 4, 17)
    # Flexible leather bellows accordion chamber (ribs/pleats)
    model.add_box("leather_lower", 2, 2, 2, 14, 5, 13)
    model.add_box("leather_middle_expansion", 1.5, 5, 1.5, 14.5, 7, 13.5)
    model.add_box("leather_upper", 2, 7, 2, 14, 10, 13)
    # Top wooden press plate
    model.add_box("top_plate", 1, 10, 1, 15, 12, 14)
    # Wooden pumping handle at the rear (-Z direction)
    model.add_box("handle_beam", 6, 11, 0, 10, 13, 3)
    model.add_box("handle_grip", 5, 11, -1, 11, 13, 0)
    # Side brass hinge brackets
    model.add_box("hinge_west", 0.5, 1, 1, 1.5, 11, 3)
    model.add_box("hinge_east", 14.5, 1, 1, 15.5, 11, 3)

    model.export_block_model(os.path.join(MODELS_DIR, "bellows.json"))
    model.export_item_definition(os.path.join(ITEMS_DIR, "bellows.json"), "industry-made:block/bellows")
    model.export_blockstate(os.path.join(BLOCKSTATES_DIR, "bellows.json"), "industry-made:block/bellows", directional=True)

    # Texture: 32x32 dark spruce wood boards + rich pleated tanned leather + iron nozzle
    tex = create_base_canvas(32, 32, (108, 68, 44))  # Tanned leather tone
    apply_subtle_noise(tex, amount=10, seed=202)
    draw = ImageDraw.Draw(tex)
    # Dark spruce wood for top/bottom plates (y: 0..8 and y: 24..31)
    draw.rectangle([0, 0, 31, 8], fill=(78, 56, 34, 255))
    draw.rectangle([0, 24, 31, 31], fill=(78, 56, 34, 255))
    # Wood grain lines
    for x in [6, 14, 22]:
        draw.line([(x, 0), (x, 8)], fill=(60, 42, 24, 255))
        draw.line([(x, 24), (x, 31)], fill=(60, 42, 24, 255))
    # Accordion leather pleats / shadow lines in middle (y: 9..23)
    for y in [12, 16, 20]:
        draw.line([(0, y), (31, y)], fill=(68, 40, 24, 255))
        draw.line([(0, y + 1), (31, y + 1)], fill=(132, 86, 56, 255))  # highlight
    # Front cast-iron nozzle metal region (x: 24..31, y: 10..18)
    draw.rectangle([24, 10, 31, 18], fill=(62, 64, 68, 255))
    draw.rectangle([24, 10, 31, 11], fill=(86, 88, 92, 255))
    tex.save(os.path.join(TEXTURES_DIR, "bellows.png"))
    print("Generated Bellows assets.")


# ==============================================================================
# 3. LOW PRESSURE BOILER
# ==============================================================================
def generate_boiler():
    model = VoxelModel("low_pressure_boiler")
    model.set_texture("particle", "industry-made:block/low_pressure_boiler")
    model.set_texture("texture", "industry-made:block/low_pressure_boiler")

    # 4 Cast Iron Corner Mounting Feet
    model.add_box("leg_nw", 1, 0, 1, 4, 3, 4)
    model.add_box("leg_ne", 12, 0, 1, 15, 3, 4)
    model.add_box("leg_sw", 1, 0, 12, 4, 3, 15)
    model.add_box("leg_se", 12, 0, 12, 15, 3, 15)
    # Bottom furnace fire hearth / ash tray
    model.add_box("hearth_base", 2, 1.5, 2, 14, 3.5, 14)
    # Main Bronze Boiler Pressure Vessel (octagonal feel via beveled side slabs)
    model.add_box("boiler_core", 3, 3.5, 3, 13, 13.5, 13)
    model.add_box("boiler_north_slab", 4, 3.5, 2, 12, 13.5, 3)
    model.add_box("boiler_south_slab", 4, 3.5, 13, 12, 13.5, 14)
    model.add_box("boiler_west_slab", 2, 3.5, 4, 3, 13.5, 12)
    model.add_box("boiler_east_slab", 13, 3.5, 4, 14, 13.5, 12)
    # Heavy Bronze Rivet Reinforcement Belts
    model.add_box("belt_lower", 1.8, 5, 1.8, 14.2, 6.2, 14.2)
    model.add_box("belt_upper", 1.8, 11, 1.8, 14.2, 12.2, 14.2)
    # Top Boiler Dome / Crown
    model.add_box("dome", 4, 13.5, 4, 12, 15, 12)
    # Steam Outlet Flange Pipe (top)
    model.add_box("steam_outlet", 6, 15, 6, 10, 16.5, 10)
    # Front Brass Pressure Gauge & Sight Glass (mounted on North face)
    model.add_box("gauge_bracket", 6, 7, 1, 10, 11, 2)
    model.add_box("sight_glass", 7, 4, 1.2, 9, 7, 2)

    model.export_block_model(os.path.join(MODELS_DIR, "low_pressure_boiler.json"))
    model.export_item_definition(os.path.join(ITEMS_DIR, "low_pressure_boiler.json"), "industry-made:block/low_pressure_boiler")
    model.export_blockstate(os.path.join(BLOCKSTATES_DIR, "low_pressure_boiler.json"), "industry-made:block/low_pressure_boiler", directional=True)

    # Texture: 32x32 warm aged bronze plate + steel rivets + brass pressure dial
    tex = create_base_canvas(32, 32, (158, 98, 52))  # Bronze base
    apply_subtle_noise(tex, amount=12, seed=303)
    draw = ImageDraw.Draw(tex)
    # Dark cast-iron legs at bottom (y: 26..31)
    draw.rectangle([0, 26, 31, 31], fill=(45, 47, 50, 255))
    # Rivet lines across bronze belts
    for y in [6, 14]:
        draw.line([(0, y), (31, y)], fill=(122, 74, 38, 255))
        for x in range(2, 32, 4):
            add_rivet(draw, x, y, (210, 150, 90), (90, 50, 24))
    # Brass Pressure Gauge face (x: 22..30, y: 16..24)
    draw.rectangle([22, 16, 30, 24], fill=(195, 155, 60, 255))  # Brass rim
    draw.rectangle([24, 18, 28, 22], fill=(230, 235, 230, 255))  # White dial
    draw.point((26, 20), fill=(180, 20, 20, 255))  # Red needle
    draw.point((26, 19), fill=(180, 20, 20, 255))
    add_bevel_border(tex, (190, 125, 75), (95, 55, 28))
    tex.save(os.path.join(TEXTURES_DIR, "low_pressure_boiler.png"))
    print("Generated Low Pressure Boiler assets.")


# ==============================================================================
# 4. STEAM PISTON
# ==============================================================================
def generate_steam_piston():
    model = VoxelModel("steam_piston")
    model.set_texture("particle", "industry-made:block/steam_piston")
    model.set_texture("texture", "industry-made:block/steam_piston")

    # Bottom Mounting Pedestal / Cast-Iron Flange
    model.add_box("pedestal", 1, 0, 1, 15, 3, 15)
    # Heavy Bronze Cylinder Barrel
    model.add_box("cylinder_barrel", 3, 3, 3, 13, 11, 13)
    # Cylinder Top Gland Seal / Head
    model.add_box("cylinder_head", 4, 11, 4, 12, 12.5, 12)
    # Polished Steel Piston Shaft / Rod extending up
    model.add_box("piston_rod", 6.5, 12.5, 6.5, 9.5, 15, 9.5)
    # Top Mechanical Coupling / Hammer Ram Pusher Plate
    model.add_box("ram_plate", 5, 15, 5, 11, 16.5, 11)
    # Side Steam Intake Manifold Pipe (East face)
    model.add_box("steam_intake", 13, 5, 6, 16, 9, 10)
    # Side Exhaust Valve / Relief Petcock (West face)
    model.add_box("exhaust_valve", 0.5, 6, 7, 3, 8, 9)

    model.export_block_model(os.path.join(MODELS_DIR, "steam_piston.json"))
    model.export_item_definition(os.path.join(ITEMS_DIR, "steam_piston.json"), "industry-made:block/steam_piston")
    model.export_blockstate(os.path.join(BLOCKSTATES_DIR, "steam_piston.json"), "industry-made:block/steam_piston", directional=False)

    # Texture: 32x32 dark cast iron base + warm bronze cylinder + gleaming chrome steel rod
    tex = create_base_canvas(32, 32, (155, 95, 50))  # Bronze cylinder body
    apply_subtle_noise(tex, amount=10, seed=404)
    draw = ImageDraw.Draw(tex)
    # Cast Iron Pedestal (y: 24..31)
    draw.rectangle([0, 24, 31, 31], fill=(48, 50, 52, 255))
    # Polished Steel Rod / Coupling zone (x: 20..31, y: 0..10)
    draw.rectangle([20, 0, 31, 10], fill=(160, 165, 172, 255))
    draw.line([(22, 0), (22, 10)], fill=(210, 215, 222, 255))  # Rod reflection
    draw.line([(28, 0), (28, 10)], fill=(110, 115, 122, 255))  # Shadow
    # Cylinder bolt studs around barrel (y: 12)
    for x in range(3, 30, 4):
        add_rivet(draw, x, 12, (200, 140, 80), (80, 45, 20))
    add_bevel_border(tex, (185, 120, 70), (90, 50, 25))
    tex.save(os.path.join(TEXTURES_DIR, "steam_piston.png"))
    print("Generated Steam Piston assets.")


# ==============================================================================
# 5. MECHANICAL FORGE HAMMER
# ==============================================================================
def generate_mechanical_hammer():
    model = VoxelModel("mechanical_hammer")
    model.set_texture("particle", "industry-made:block/mechanical_hammer")
    model.set_texture("texture", "industry-made:block/mechanical_hammer")

    # Sturdy Cast Iron Anvil Base
    model.add_box("anvil_base", 2, 0, 2, 14, 5, 14)
    # Hardened Steel Anvil Work Die (forming bed)
    model.add_box("anvil_die", 4, 5, 4, 12, 7, 12)
    # Rear Structural A-Frame Upright Columns
    model.add_box("stanchion_left", 2, 5, 10, 5, 15, 14)
    model.add_box("stanchion_right", 11, 5, 10, 14, 15, 14)
    # Heavy Cross-Axle Beam & Pivot Bushing
    model.add_box("pivot_axle", 3, 12.5, 11, 13, 14.5, 13)
    # Cantilever Hammer Arm extending forward over the anvil
    model.add_box("hammer_arm", 6, 12, 3, 10, 14, 12)
    # Heavy Forged Steel Hammer Tup / Drop Ram Head
    model.add_box("hammer_head", 5, 8, 2, 11, 12, 6)
    # Piston Drive Linkage / Cam Pivot Cleat at rear
    model.add_box("drive_cleat", 6.5, 14, 10.5, 9.5, 16.5, 13.5)

    model.export_block_model(os.path.join(MODELS_DIR, "mechanical_hammer.json"))
    model.export_item_definition(os.path.join(ITEMS_DIR, "mechanical_hammer.json"), "industry-made:block/mechanical_hammer")
    model.export_blockstate(os.path.join(BLOCKSTATES_DIR, "mechanical_hammer.json"), "industry-made:block/mechanical_hammer", directional=True)

    # Texture: 32x32 dark pitted cast iron + polished tool steel anvil face + bronze pivot bearings
    tex = create_base_canvas(32, 32, (44, 46, 48))  # Dark cast iron
    apply_subtle_noise(tex, amount=12, seed=505)
    draw = ImageDraw.Draw(tex)
    # Hardened tool steel bright face (x: 0..14, y: 0..10)
    draw.rectangle([0, 0, 14, 10], fill=(138, 144, 152, 255))
    draw.rectangle([2, 2, 12, 8], fill=(168, 174, 182, 255))  # Mirror finish strike face
    # Bronze pivot bearings (x: 20..30, y: 2..8)
    draw.rectangle([20, 2, 30, 8], fill=(170, 115, 60, 255))
    draw.rectangle([22, 4, 28, 6], fill=(210, 150, 85, 255))
    # Structural frame rivet studs
    for y in [16, 24]:
        for x in [4, 12, 20, 28]:
            add_rivet(draw, x, y, (90, 94, 98), (28, 30, 32))
    add_bevel_border(tex, (68, 72, 76), (25, 26, 28))
    tex.save(os.path.join(TEXTURES_DIR, "mechanical_hammer.png"))
    print("Generated Mechanical Hammer assets.")


if __name__ == "__main__":
    generate_crucible()
    generate_bellows()
    generate_boiler()
    generate_steam_piston()
    generate_mechanical_hammer()
    print("All 5 Era 1 machine 3D models and textures successfully generated!")
