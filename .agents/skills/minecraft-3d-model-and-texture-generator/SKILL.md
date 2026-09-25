---
name: minecraft-3d-model-and-texture-generator
description: >-
  Guides the design, texturing, UV mapping, and generation of rich 3D voxel models for Minecraft blocks
  and items. Covers Blockbench format, JSON element specifications, UV mapping, display transforms
  (GUI, First/Third Person, Ground, Fixed), and procedural texture generation using Python and Pillow.
---

# 🧊 Minecraft 3D Model & Texture Generator Skill

This skill provides the complete engineering specifications, formulas, and tooling for creating authentic **3D voxel models** and **high-fidelity pixel art textures** for Minecraft Java Edition (Minecraft 26.x / Fabric), eliminating flat 2D sprites and plain 16x16 cubes for machinery, equipment, and interactive blocks.

---

## 📐 1. Minecraft 3D Coordinate Space & Geometry

### The 16x16x16 Voxel Grid
- Every Minecraft block occupies a normalized bounding volume from `[0, 0, 0]` to `[16, 16, 16]`.
- Coordinate axes:
  - **+X**: East (West is -X)
  - **+Y**: Up (Down is -Y)
  - **+Z**: South (North is -Z)
- Custom models can extend slightly beyond bounds (e.g. `[-16, -16, -16]` to `[32, 32, 32]`), but interactive blocks should remain within or near `[0, 0, 0]..[16, 16, 16]` to prevent clipping.

### Cuboid Elements (`elements`)
Every 3D block model is composed of a list of rectangular boxes (`elements`):
```json
{
  "name": "base_plate",
  "from": [1, 0, 1],
  "to": [15, 2, 15],
  "shade": true,
  "faces": {
    "down":  { "uv": [1, 1, 15, 15], "texture": "#texture", "cullface": "down" },
    "up":    { "uv": [1, 1, 15, 15], "texture": "#texture" },
    "north": { "uv": [1, 14, 15, 16], "texture": "#texture" },
    "south": { "uv": [1, 14, 15, 16], "texture": "#texture" },
    "west":  { "uv": [1, 14, 15, 16], "texture": "#texture" },
    "east":  { "uv": [1, 14, 15, 16], "texture": "#texture" }
  }
}
```

### Rotation Constraints
- Rotation can only occur along **one axis at a time** (`"axis": "x" | "y" | "z"`).
- Allowed angles are strictly limited to: `-45.0, -22.5, 0.0, 22.5, 45.0`.
- An `"origin": [x, y, z]` pivot point must be declared.

---

## 🎨 2. UV Mapping & Texture Resolution

### Normalized UV Coordinates
- Face UVs are ALWAYS mapped to a normalized `[u1, v1, u2, v2]` coordinate box between `0.0` and `16.0`, regardless of whether the texture PNG is 16x16, 32x32, or 64x64.
- UV coordinate orientation:
  - `(0, 0)` is top-left of the texture.
  - `(16, 16)` is bottom-right of the texture.

### Face Orientation Formulas
When projecting auto-UVs from coordinates `[x1, y1, z1]` to `[x2, y2, z2]`:
- **North face (+Z looking -Z):** `uv: [16 - x2, 16 - y2, 16 - x1, 16 - y1]`
- **South face (-Z looking +Z):** `uv: [x1, 16 - y2, x2, 16 - y1]`
- **West face (+X looking -X):** `uv: [z1, 16 - y2, z2, 16 - y1]`
- **East face (-X looking +X):** `uv: [16 - z2, 16 - y2, 16 - z1, 16 - y1]`
- **Up face (+Y looking down):** `uv: [x1, z1, x2, z2]`
- **Down face (-Y looking up):** `uv: [x1, 16 - z2, x2, 16 - z1]`

---

## 🖥️ 3. Isometric Display Transforms for 3D Items

To display a 3D block model properly in player inventories, the hotbar, player hands, item frames, and dropped on the ground, the `"display"` block is mandatory in the model JSON:

```json
"display": {
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
```

---

## 📦 4. Modern Minecraft 26.x Item Definition

In Minecraft 26.x, item definitions are stored in `assets/<namespace>/items/<id>.json`.
To connect an item to the 3D block model:

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "industry-made:block/<model_name>"
  }
}
```
This single file instructs Minecraft to render the block's full 3D geometry in the GUI, hand, and ground using the `"display"` transforms defined in `models/block/<model_name>.json`.

---

## 🎨 5. Industrial Aesthetic Palette & Texturing Guidelines

When generating textures for Industry Made, strictly follow the gritty Vanilla+ industrial color palette:

| Material | Primary Hex | Highlight Hex | Shadow Hex | Texture Details |
| :--- | :--- | :--- | :--- | :--- |
| **Refractory Clay / Ceramic** | `#3A2E2B` | `#50413E` | `#241C1A` | Dark fire-hardened earthenware, scorched banding |
| **Cast Iron** | `#353839` | `#4B4E50` | `#222324` | Rough pitted metal, dark charcoal sheen |
| **Wrought Iron** | `#5C5E62` | `#7D8086` | `#3E4044` | Hammered steel/iron, visible grain |
| **Bronze** | `#A76B38` | `#C88D54` | `#774B24` | Warm copper-tin alloy, dark brass rivets |
| **Spruce Wood** | `#523E27` | `#6E5537` | `#382A1A` | Dark timber planks, iron banding |
| **Hardened Leather** | `#75482F` | `#965F3E` | `#523220` | Bellows bellows pleats, tanned animal hide |
| **Steam Glass** | `#8AA8B0` | `#C4E4EE` | `#5A747B` | Pressure gauge tube, frosted translucent |

---

## 🛠️ 6. Automated Generation via `model_builder.py`

Use the included helper script to generate models, blockstates, items, and textures programmatically:

```python
from model_builder import VoxelModel, create_base_canvas, apply_subtle_noise

# 1. Create 3D Model
model = VoxelModel("crucible")
model.set_texture("particle", "industry-made:block/crucible")
model.set_texture("texture", "industry-made:block/crucible")

# Base plate
model.add_box("base", 2, 0, 2, 14, 3, 14)
# North wall
model.add_box("wall_north", 2, 3, 2, 14, 14, 4)

# 2. Export assets
model.export_block_model("src/main/resources/assets/industry-made/models/block/crucible.json")
model.export_item_definition("src/main/resources/assets/industry-made/items/crucible.json", "industry-made:block/crucible")
model.export_blockstate("src/main/resources/assets/industry-made/blockstates/crucible.json", "industry-made:block/crucible")
```
