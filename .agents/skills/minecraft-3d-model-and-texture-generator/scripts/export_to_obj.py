"""
Minecraft JSON Model to Wavefront OBJ / MTL Exporter for Blender
Industry Made - Supports Multi-Material Textures and UV coordinates
"""

import json
import os
import sys
from typing import Dict, List, Tuple

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../.."))
ASSETS_DIR = os.path.join(BASE_DIR, "src/main/resources/assets/industry-made")
TEXTURES_DIR = os.path.join(ASSETS_DIR, "textures/block")
MODELS_DIR = os.path.join(ASSETS_DIR, "models/block")
EXPORT_DIR = os.path.join(BASE_DIR, "blender_export")

def resolve_texture_path(tex_var_val: str) -> str:
    # Example: "industry-made:block/crucible_ceramic" -> "src/main/resources/assets/industry-made/textures/block/crucible_ceramic.png"
    if ":" in tex_var_val:
        _, rel = tex_var_val.split(":", 1)
    else:
        rel = tex_var_val
    if rel.startswith("block/"):
        tex_name = rel[6:] + ".png"
    else:
        tex_name = rel + ".png"
    return os.path.join(TEXTURES_DIR, tex_name)


def convert_minecraft_json_to_obj(json_path: str, output_obj_path: str):
    with open(json_path, "r", encoding="utf-8") as f:
        model_data = json.load(f)

    elements = model_data.get("elements", [])
    textures = model_data.get("textures", {})
    if not elements:
        print(f"No elements found in {json_path}")
        return

    os.makedirs(os.path.dirname(output_obj_path), exist_ok=True)
    mtl_filename = os.path.splitext(os.path.basename(output_obj_path))[0] + ".mtl"
    output_mtl_path = os.path.join(os.path.dirname(output_obj_path), mtl_filename)

    # 1. Write MTL (Material) File with all textures
    with open(output_mtl_path, "w", encoding="utf-8") as mtl_file:
        mtl_file.write(f"# Materials for {os.path.basename(json_path)}\n\n")
        for key, path_val in textures.items():
            if key == "particle":
                continue
            mat_name = f"mat_{key}"
            tex_file = resolve_texture_path(path_val)
            rel_tex_path = os.path.relpath(tex_file, os.path.dirname(output_mtl_path)).replace("\\", "/")
            mtl_file.write(f"newmtl {mat_name}\n")
            mtl_file.write("Ka 1.000 1.000 1.000\n")
            mtl_file.write("Kd 1.000 1.000 1.000\n")
            mtl_file.write("Ks 0.000 0.000 0.000\n")
            mtl_file.write("d 1.0\n")
            mtl_file.write("illum 1\n")
            mtl_file.write(f"map_Kd {rel_tex_path}\n\n")

    # 2. Write OBJ File
    vertices: List[Tuple[float, float, float]] = []
    uvs: List[Tuple[float, float]] = []
    # (mat_name, quad_vertex_vt_indices)
    material_faces: Dict[str, List[List[Tuple[int, int]]]] = {}

    for elem in elements:
        x1, y1, z1 = [v / 16.0 for v in elem["from"]]
        x2, y2, z2 = [v / 16.0 for v in elem["to"]]
        elem_faces = elem.get("faces", {})

        base_v = len(vertices) + 1
        box_verts = [
            (x1, y1, z1), # 0
            (x2, y1, z1), # 1
            (x2, y2, z1), # 2
            (x1, y2, z1), # 3
            (x1, y1, z2), # 4
            (x2, y1, z2), # 5
            (x2, y2, z2), # 6
            (x1, y2, z2), # 7
        ]
        vertices.extend(box_verts)

        def add_quad(mat_key: str, v_indices, uv_coords):
            clean_mat = f"mat_{mat_key.lstrip('#')}"
            if clean_mat not in material_faces:
                material_faces[clean_mat] = []
            base_vt = len(uvs) + 1
            for (u, v) in uv_coords:
                uvs.append((u / 16.0, 1.0 - (v / 16.0)))
            quad = [
                (base_v + v_indices[0], base_vt + 0),
                (base_v + v_indices[1], base_vt + 1),
                (base_v + v_indices[2], base_vt + 2),
                (base_v + v_indices[3], base_vt + 3),
            ]
            material_faces[clean_mat].append(quad)

        # North Face (-Z)
        if "north" in elem_faces:
            f = elem_faces["north"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [16 - elem["to"][0], 16 - elem["to"][1], 16 - elem["from"][0], 16 - elem["from"][1]])
            add_quad(tex, [1, 0, 3, 2], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # South Face (+Z)
        if "south" in elem_faces:
            f = elem_faces["south"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [elem["from"][0], 16 - elem["to"][1], elem["to"][0], 16 - elem["from"][1]])
            add_quad(tex, [4, 5, 6, 7], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # West Face (-X)
        if "west" in elem_faces:
            f = elem_faces["west"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [elem["from"][2], 16 - elem["to"][1], elem["to"][2], 16 - elem["from"][1]])
            add_quad(tex, [0, 4, 7, 3], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # East Face (+X)
        if "east" in elem_faces:
            f = elem_faces["east"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [16 - elem["to"][2], 16 - elem["to"][1], 16 - elem["from"][2], 16 - elem["from"][1]])
            add_quad(tex, [5, 1, 2, 6], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # Up Face (+Y)
        if "up" in elem_faces:
            f = elem_faces["up"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [elem["from"][0], elem["from"][2], elem["to"][0], elem["to"][2]])
            add_quad(tex, [3, 7, 6, 2], [(u1, v1), (u1, v2), (u2, v2), (u2, v1)])

        # Down Face (-Y)
        if "down" in elem_faces:
            f = elem_faces["down"]
            tex = f.get("texture", "#texture")
            u1, v1, u2, v2 = f.get("uv", [elem["from"][0], 16 - elem["to"][2], elem["to"][0], 16 - elem["from"][2]])
            add_quad(tex, [0, 1, 5, 4], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

    with open(output_obj_path, "w", encoding="utf-8") as f:
        f.write(f"# Exported from Minecraft Model: {os.path.basename(json_path)}\n")
        f.write(f"mtllib {mtl_filename}\n\n")

        # Vertices
        for vx, vy, vz in vertices:
            f.write(f"v {vx:.4f} {vy:.4f} {vz:.4f}\n")

        # Texture Coordinates
        for tu, tv in uvs:
            f.write(f"vt {tu:.4f} {tv:.4f}\n")

        # Faces grouped by material
        f.write("\n")
        for mat_name, q_list in material_faces.items():
            f.write(f"usemtl {mat_name}\n")
            for quad in q_list:
                f.write(f"f {quad[0][0]}/{quad[0][1]} {quad[1][0]}/{quad[1][1]} {quad[2][0]}/{quad[2][1]} {quad[3][0]}/{quad[3][1]}\n")
            f.write("\n")

    print(f"Exported Multi-Material OBJ: {output_obj_path}")


def export_all():
    os.makedirs(EXPORT_DIR, exist_ok=True)
    machines = ["crucible", "bellows", "low_pressure_boiler", "steam_piston", "mechanical_hammer"]
    for m in machines:
        json_file = os.path.join(MODELS_DIR, f"{m}.json")
        out_obj = os.path.join(EXPORT_DIR, f"{m}.obj")
        if os.path.exists(json_file):
            convert_minecraft_json_to_obj(json_file, out_obj)

    print(f"\nAll models exported with complete multi-material textures to: {EXPORT_DIR}")


if __name__ == "__main__":
    export_all()
