"""
Model Builder Utility for Minecraft 3D JSON Models and Textures
Industry Made - Minecraft 26.1.2 Fabric
"""

import json
import os
import random
from typing import Dict, List, Optional, Tuple, Any
from PIL import Image, ImageDraw

# Standard isometric block display transforms for Minecraft
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

class VoxelElement:
    def __init__(
        self,
        name: str,
        from_pos: Tuple[float, float, float],
        to_pos: Tuple[float, float, float],
        texture: str = "#texture",
        uv_map: Optional[Dict[str, List[float]]] = None,
        cull_faces: Optional[Dict[str, str]] = None,
        rotation: Optional[Dict[str, Any]] = None,
        shade: bool = True
    ):
        self.name = name
        self.from_pos = [round(float(v), 3) for v in from_pos]
        self.to_pos = [round(float(v), 3) for v in to_pos]
        self.texture = texture
        self.uv_map = uv_map or {}
        self.cull_faces = cull_faces or {}
        self.rotation = rotation
        self.shade = shade

    def to_dict(self) -> Dict[str, Any]:
        data: Dict[str, Any] = {
            "name": self.name,
            "from": self.from_pos,
            "to": self.to_pos,
            "shade": self.shade,
            "faces": {}
        }
        
        # Calculate auto UVs if not provided
        # Coordinate ranges in 0..16
        x1, y1, z1 = self.from_pos
        x2, y2, z2 = self.to_pos
        
        directions = ["north", "south", "east", "west", "up", "down"]
        for d in directions:
            face_data: Dict[str, Any] = {"texture": self.texture}
            
            if d in self.uv_map:
                face_data["uv"] = self.uv_map[d]
            else:
                # Default auto-UV projected from 0..16
                if d == "north":
                    face_data["uv"] = [16 - x2, 16 - y2, 16 - x1, 16 - y1]
                elif d == "south":
                    face_data["uv"] = [x1, 16 - y2, x2, 16 - y1]
                elif d == "west":
                    face_data["uv"] = [z1, 16 - y2, z2, 16 - y1]
                elif d == "east":
                    face_data["uv"] = [16 - z2, 16 - y2, 16 - z1, 16 - y1]
                elif d == "up":
                    face_data["uv"] = [x1, z1, x2, z2]
                elif d == "down":
                    face_data["uv"] = [x1, 16 - z2, x2, 16 - z1]
            
            if d in self.cull_faces:
                face_data["cullface"] = self.cull_faces[d]
                
            data["faces"][d] = face_data
            
        if self.rotation:
            data["rotation"] = self.rotation
            
        return data


class VoxelModel:
    def __init__(self, name: str, parent: Optional[str] = None):
        self.name = name
        self.parent = parent
        self.textures: Dict[str, str] = {}
        self.elements: List[VoxelElement] = []
        self.display: Dict[str, Any] = STANDARD_BLOCK_DISPLAY.copy()

    def set_texture(self, key: str, path: str):
        self.textures[key] = path

    def add_box(
        self,
        name: str,
        x1: float, y1: float, z1: float,
        x2: float, y2: float, z2: float,
        texture: str = "#texture",
        uv_map: Optional[Dict[str, List[float]]] = None,
        cull_faces: Optional[Dict[str, str]] = None,
        rotation: Optional[Dict[str, Any]] = None,
        shade: bool = True
    ) -> VoxelElement:
        elem = VoxelElement(
            name=name,
            from_pos=(min(x1, x2), min(y1, y2), min(z1, z2)),
            to_pos=(max(x1, x2), max(y1, y2), max(z1, z2)),
            texture=texture,
            uv_map=uv_map,
            cull_faces=cull_faces,
            rotation=rotation,
            shade=shade
        )
        self.elements.append(elem)
        return elem

    def to_json_dict(self) -> Dict[str, Any]:
        result: Dict[str, Any] = {}
        if self.parent:
            result["parent"] = self.parent
        if self.textures:
            result["textures"] = self.textures
        if self.elements:
            result["elements"] = [elem.to_dict() for elem in self.elements]
        if self.display:
            result["display"] = self.display
        return result

    def export_block_model(self, file_path: str):
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(self.to_json_dict(), f, indent=2)

    def export_item_definition(self, file_path: str, model_id: str):
        """Generates modern Minecraft 26.x items/<id>.json definition."""
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        item_data = {
            "model": {
                "type": "minecraft:model",
                "model": model_id
            }
        }
        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(item_data, f, indent=2)

    def export_blockstate(self, file_path: str, model_id: str, directional: bool = False):
        """Generates blockstates/<id>.json."""
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        if directional:
            state_data = {
                "variants": {
                    "facing=north": {"model": model_id},
                    "facing=east": {"model": model_id, "y": 90},
                    "facing=south": {"model": model_id, "y": 180},
                    "facing=west": {"model": model_id, "y": 270}
                }
            }
        else:
            state_data = {
                "variants": {
                    "": {"model": model_id}
                }
            }
        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(state_data, f, indent=2)


# --- Texture Art Generator Helpers ---

def create_base_canvas(width: int = 16, height: int = 16, bg_color: Tuple[int, int, int] = (60, 60, 60)) -> Image.Image:
    return Image.new("RGBA", (width, height), (*bg_color, 255))

def apply_subtle_noise(img: Image.Image, amount: int = 8, seed: int = 42):
    random.seed(seed)
    w, h = img.size
    pixels = img.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a > 0:
                delta = random.randint(-amount, amount)
                nr = max(0, min(255, r + delta))
                ng = max(0, min(255, g + delta))
                nb = max(0, min(255, b + delta))
                pixels[x, y] = (nr, ng, nb, a)

def add_bevel_border(img: Image.Image, highlight_color: Tuple[int, int, int], shadow_color: Tuple[int, int, int]):
    w, h = img.size
    draw = ImageDraw.Draw(img)
    # Top and Left edges (light)
    draw.line([(0, 0), (w - 1, 0)], fill=(*highlight_color, 255))
    draw.line([(0, 0), (0, h - 1)], fill=(*highlight_color, 255))
    # Bottom and Right edges (shadow)
    draw.line([(0, h - 1), (w - 1, h - 1)], fill=(*shadow_color, 255))
    draw.line([(w - 1, 0), (w - 1, h - 1)], fill=(*shadow_color, 255))

def add_rivet(draw: ImageDraw.ImageDraw, x: int, y: int, light_col: Tuple[int, int, int], dark_col: Tuple[int, int, int]):
    draw.point((x, y), fill=(*light_col, 255))
    draw.point((x + 1, y), fill=(*dark_col, 255))
    draw.point((x, y + 1), fill=(*dark_col, 255))
