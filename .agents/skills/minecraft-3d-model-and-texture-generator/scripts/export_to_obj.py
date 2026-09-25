"""
Minecraft JSON Model to Wavefront OBJ / MTL Exporter for Blender
Industry Made
"""

import json
import os
import sys
from typing import Dict, List, Tuple

def convert_minecraft_json_to_obj(json_path: str, output_obj_path: str, texture_png_path: str):
    with open(json_path, "r", encoding="utf-8") as f:
        model_data = json.load(f)

    elements = model_data.get("elements", [])
    if not elements:
        print(f"No elements found in {json_path}")
        return

    os.makedirs(os.path.dirname(output_obj_path), exist_ok=True)
    mtl_filename = os.path.splitext(os.path.basename(output_obj_path))[0] + ".mtl"
    output_mtl_path = os.path.join(os.path.dirname(output_obj_path), mtl_filename)

    # 1. Write MTL (Material) File
    mat_name = "MinecraftMaterial"
    rel_tex_path = os.path.relpath(texture_png_path, os.path.dirname(output_mtl_path)).replace("\\", "/")
    with open(output_mtl_path, "w", encoding="utf-8") as mtl_file:
        mtl_file.write(f"# Material for {os.path.basename(json_path)}\n")
        mtl_file.write(f"newmtl {mat_name}\n")
        mtl_file.write("Ka 1.000 1.000 1.000\n")
        mtl_file.write("Kd 1.000 1.000 1.000\n")
        mtl_file.write("Ks 0.000 0.000 0.000\n")
        mtl_file.write("d 1.0\n")
        mtl_file.write("illum 1\n")
        mtl_file.write(f"map_Kd {rel_tex_path}\n")

    # 2. Write OBJ File
    # Minecraft coordinate system: X East, Y Up, Z South, normalized to 1 unit = 16 voxels (centered at origin or [0..1])
    vertices: List[Tuple[float, float, float]] = []
    uvs: List[Tuple[float, float]] = []
    faces: List[List[Tuple[int, int]]] = []  # list of quads, each vertex has (v_idx, vt_idx)

    # Face definitions for cuboid:
    # vertices: 0: (x1, y1, z1), 1: (x2, y1, z1), 2: (x2, y2, z1), 3: (x1, y2, z1)   (north face, -Z)
    #           4: (x1, y1, z2), 5: (x2, y1, z2), 6: (x2, y2, z2), 7: (x1, y2, z2)   (south face, +Z)
    
    for elem in elements:
        x1, y1, z1 = [v / 16.0 for v in elem["from"]]
        x2, y2, z2 = [v / 16.0 for v in elem["to"]]
        elem_faces = elem.get("faces", {})

        # 8 box corners
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

        # Helper to add quad face
        # Blender UV: (u, 1.0 - v) because OBJ UV origin (0,0) is bottom-left, while Minecraft UV origin (0,0) is top-left
        def add_quad(v_indices, uv_coords):
            base_vt = len(uvs) + 1
            for (u, v) in uv_coords:
                uvs.append((u / 16.0, 1.0 - (v / 16.0)))
            quad = [
                (base_v + v_indices[0], base_vt + 0),
                (base_v + v_indices[1], base_vt + 1),
                (base_v + v_indices[2], base_vt + 2),
                (base_v + v_indices[3], base_vt + 3),
            ]
            faces.append(quad)

        # North Face (-Z)
        if "north" in elem_faces:
            u1, v1, u2, v2 = elem_faces["north"].get("uv", [16 - elem["to"][0], 16 - elem["to"][1], 16 - elem["from"][0], 16 - elem["from"][1]])
            add_quad([1, 0, 3, 2], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # South Face (+Z)
        if "south" in elem_faces:
            u1, v1, u2, v2 = elem_faces["south"].get("uv", [elem["from"][0], 16 - elem["to"][1], elem["to"][0], 16 - elem["from"][1]])
            add_quad([4, 5, 6, 7], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # West Face (-X)
        if "west" in elem_faces:
            u1, v1, u2, v2 = elem_faces["west"].get("uv", [elem["from"][2], 16 - elem["to"][1], elem["to"][2], 16 - elem["from"][1]])
            add_quad([0, 4, 7, 3], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # East Face (+X)
        if "east" in elem_faces:
            u1, v1, u2, v2 = elem_faces["east"].get("uv", [16 - elem["to"][2], 16 - elem["to"][1], 16 - elem["from"][2], 16 - elem["from"][1]])
            add_quad([5, 1, 2, 6], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

        # Up Face (+Y)
        if "up" in elem_faces:
            u1, v1, u2, v2 = elem_faces["up"].get("uv", [elem["from"][0], elem["from"][2], elem["to"][0], elem["to"][2]])
            add_quad([3, 7, 6, 2], [(u1, v1), (u1, v2), (u2, v2), (u2, v1)])

        # Down Face (-Y)
        if "down" in elem_faces:
            u1, v1, u2, v2 = elem_faces["down"].get("uv", [elem["from"][0], 16 - elem["to"][2], elem["to"][0], 16 - elem["from"][2]])
            add_quad([0, 1, 5, 4], [(u1, v2), (u2, v2), (u2, v1), (u1, v1)])

    with open(output_obj_path, "w", encoding="utf-8") as f:
        f.write(f"# Exported from Minecraft Model: {os.path.basename(json_path)}\n")
        f.write(f"mtllib {mtl_filename}\n")
        f.write(f"usemtl {mat_name}\n\n")

        # Vertices
        for vx, vy, vz in vertices:
            f.write(f"v {vx:.4f} {vy:.4f} {vz:.4f}\n")

        # Texture Coordinates
        for tu, tv in uvs:
            f.write(f"vt {tu:.4f} {tv:.4f}\n")

        # Faces
        f.write("\n")
        for quad in faces:
            f.write(f"f {quad[0][0]}/{quad[0][1]} {quad[1][0]}/{quad[1][1]} {quad[2][0]}/{quad[2][1]} {quad[3][0]}/{quad[3][1]}\n")

    print(f"Exported OBJ: {output_obj_path}")


def export_all_models_to_blender():
    models_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../../src/main/resources/assets/industry-made/models/block"))
    textures_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../../src/main/resources/assets/industry-made/textures/block"))
    export_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../../../blender_export"))
    os.makedirs(export_dir, exist_ok=True)

    machines = ["crucible", "bellows", "low_pressure_boiler", "steam_piston", "mechanical_hammer"]
    for m in machines:
        json_file = os.path.join(models_dir, f"{m}.json")
        tex_file = os.path.join(textures_dir, f"{m}.png")
        out_obj = os.path.join(export_dir, f"{m}.obj")
        if os.path.exists(json_file):
            convert_minecraft_json_to_obj(json_file, out_obj, tex_file)

    print(f"\nAll models exported to OBJ in: {export_dir}")


if __name__ == "__main__":
    export_all_models_to_blender()
