"""
Generates comprehensive blockstate JSON files for all Industry Made machines,
covering 100% of StateDefinition property permutations (facing, lit, powered, pressure_level, heat_level)
to ensure Minecraft's ModelLoader never encounters missing variants.
"""

import json
import os

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../.."))
BLOCKSTATES_DIR = os.path.join(BASE_DIR, "src/main/resources/assets/industry-made/blockstates")
os.makedirs(BLOCKSTATES_DIR, exist_ok=True)

ROTATIONS = {
    "north": 0,
    "east": 90,
    "south": 180,
    "west": 270
}

def make_variant_entry(model_id: str, y_rot: int):
    if y_rot == 0:
        return {"model": model_id}
    return {"model": model_id, "y": y_rot}


def generate_bellows():
    # Properties: FACING (north, east, south, west), POWERED (true, false)
    variants = {}
    for facing, y_rot in ROTATIONS.items():
        for powered in ["false", "true"]:
            key = f"facing={facing},powered={powered}"
            variants[key] = make_variant_entry("industry-made:block/bellows", y_rot)

    path = os.path.join(BLOCKSTATES_DIR, "bellows.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print("Generated blockstate: bellows.json")


def generate_crucible():
    # Property: HEAT_LEVEL (0, 1, 2, 3)
    variants = {}
    for heat in [0, 1, 2, 3]:
        key = f"heat_level={heat}"
        variants[key] = {"model": "industry-made:block/crucible"}

    path = os.path.join(BLOCKSTATES_DIR, "crucible.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print("Generated blockstate: crucible.json")


def generate_boiler():
    # Properties: FACING (4), LIT (2), PRESSURE_LEVEL (4) = 32 variants
    variants = {}
    for facing, y_rot in ROTATIONS.items():
        for lit in ["false", "true"]:
            for pressure in [0, 1, 2, 3]:
                key = f"facing={facing},lit={lit},pressure_level={pressure}"
                variants[key] = make_variant_entry("industry-made:block/low_pressure_boiler", y_rot)

    path = os.path.join(BLOCKSTATES_DIR, "low_pressure_boiler.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print("Generated blockstate: low_pressure_boiler.json")


def generate_steam_piston():
    # Properties: FACING (4), POWERED (2) = 8 variants
    variants = {}
    for facing, y_rot in ROTATIONS.items():
        for powered in ["false", "true"]:
            key = f"facing={facing},powered={powered}"
            variants[key] = make_variant_entry("industry-made:block/steam_piston", y_rot)

    path = os.path.join(BLOCKSTATES_DIR, "steam_piston.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print("Generated blockstate: steam_piston.json")


def generate_mechanical_hammer():
    # Properties: FACING (4), POWERED (2) = 8 variants
    variants = {}
    for facing, y_rot in ROTATIONS.items():
        for powered in ["false", "true"]:
            key = f"facing={facing},powered={powered}"
            variants[key] = make_variant_entry("industry-made:block/mechanical_hammer", y_rot)

    path = os.path.join(BLOCKSTATES_DIR, "mechanical_hammer.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
    print("Generated blockstate: mechanical_hammer.json")


if __name__ == "__main__":
    generate_bellows()
    generate_crucible()
    generate_boiler()
    generate_steam_piston()
    generate_mechanical_hammer()
    print("All 5 machine blockstates generated successfully!")
