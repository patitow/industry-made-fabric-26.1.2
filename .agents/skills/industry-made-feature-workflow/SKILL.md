---
name: industry-made-feature-workflow
description: >-
  Standardized checklist and verification workflow for creating items, blocks, machines,
  and metallurgy features in the Industry Made Minecraft Fabric mod. Enforces complete auxiliary
  file creation: Data Components, DataGen models, recipes, tags, translations, textures, sound events,
  and continuous build validation.
---

# 🏭 Industry Made — Feature Implementation Workflow & Checklist

This skill defines the mandatory, standardized procedure for implementing any new item, block, entity, or mechanical system in **Industry Made** (Minecraft 26.1.2 / Fabric Loader 0.19.5 / Fabric Loom 1.18.2 / Kotlin 2.4.20 / Java 25).

---

## 📋 The 8-Point Implementation Checklist

Every feature in Industry Made must complete all 8 steps without skipping auxiliary files:

```
[ ] 1. Code Architecture (Zero Gray Boxes, In-World Interaction, Modern 26.x Data Components)
[ ] 2. Core Registration (ModBlocks, ModItems, ModBlockEntities, ModDataComponents, ModSounds)
[ ] 3. Creative Tab Placement (INDUSTRY_MADE_TAB in ModItems.kt)
[ ] 4. Visual Textures (16x16 pixel art for items/blocks, 64x64 for entity models)
[ ] 5. Client Rendering (BlockEntityRenderer with partialTicks interpolation for continuous animations)
[ ] 6. Fabric DataGen (IndustryMadeDataGenerator: blockstates, models, recipes, loot tables, tags)
[ ] 7. Full Dual Localization (English en_us and Portuguese pt_br)
[ ] 8. Continuous Build Validation (compileKotlin, compileClientKotlin, runDatagen, build)
```

---

## 🛠️ Step-by-Step Procedure

### 1. Code Architecture & Design Philosophy
- **Physical Interactions First:** Material transformations happen in the world via heat, air draft, or mechanical motion. Never create 2D magic gray GUI boxes for core thermal/metallurgical processing.
- **Failures without Catastrophes:** Overheating emits steam/smoke and burns nearby entities; it never destroys player bases or craters terrain.
- **Modern Minecraft 26.x APIs:**
  - **No Legacy NBT on ItemStacks:** Use `DataComponentType<T>` registered in `ModDataComponents.kt`.
  - **Modern BlockEntity Serialization:** Use `saveAdditional(output: ValueOutput)` and `loadAdditional(input: ValueInput)` with `ContainerHelper.saveAllItems` / `loadAllItems`.
  - **Resource Keys:** Use `ResourceKey.create(Registries.BLOCK, id)` and `ResourceKey.create(Registries.ITEM, id)`.

### 2. Core Registration
- Blocks ➔ `dev.patitow.industrymade.init.ModBlocks.kt`
- Items ➔ `dev.patitow.industrymade.init.ModItems.kt`
- Block Entities ➔ `dev.patitow.industrymade.init.ModBlockEntities.kt`
- Data Components ➔ `dev.patitow.industrymade.init.ModDataComponents.kt`
- Sound Events ➔ `dev.patitow.industrymade.init.ModSounds.kt`
- Ensure each registry is triggered in `IndustryMade.kt` during mod initialization.

### 3. Creative Tab Placement
- Every obtainable block and item must be added to `INDUSTRY_MADE_TAB` in `ModItems.kt` in a logical developmental order (e.g. Raw Material ➔ Refractory Blocks ➔ Molds ➔ Hot Molds ➔ Ingots ➔ Machines).

### 4. Visual Textures & Assets
- Textures live in `src/main/resources/assets/industry-made/textures/`:
  - `item/<name>.png`: 16x16 pixel art.
  - `block/<name>.png`: 16x16 pixel art.
  - `entity/<name>.png`: 64x64 texture map for custom block entity models.
- Palette consistency: Maintain gritty, grounded Minecraft Vanilla+ aesthetic.

### 5. Client Rendering (`src/client/kotlin/`)
- For kinetic or animated block entities (bellows, pistons, shafts, crucibles with molten fluids):
  - Register layer in `IndustryMadeClient.kt`: `ModelLayerRegistry.registerModelLayer(...)`.
  - Register renderer in `BlockEntityRendererRegistry.register(...)`.
  - Implement `BlockEntityRenderer<T, S : BlockEntityRenderState>`.
  - Use `extractRenderState(..., partialTicks, ...)` for silky 60+ FPS visual interpolation independent of server 20 TPS.
  - Molten liquid surfaces use `RenderTypes.entityCutout` with `FULL_BRIGHT (0x00F000F0)` for authentic incandescence.

### 6. Fabric DataGen Automation (`IndustryMadeDataGenerator.kt`)
Never write raw JSON files manually. Declare them in `IndustryMadeDataGenerator.kt`:
1. `ModModelProvider`:
   - `createTrivialCube` or `createParticleOnlyBlock` for blocks.
   - `generateFlatItem` for items.
2. `ModRecipeProvider`:
   - Shaped/shapeless crafting recipes.
   - Smelting and blasting recipes (`SimpleCookingRecipeBuilder`).
3. `ModBlockLootTableProvider`:
   - `dropSelf(...)` for blocks.
4. `ModBlockTagsProvider` & `ModItemTagsProvider`:
   - Tool tags (`BlockTags.MINEABLE_WITH_PICKAXE`, `BlockTags.NEEDS_STONE_TOOL`, etc.).

### 7. Dual Localization
Both languages are mandatory:
- `ModEnglishLanguageProvider` (`en_us`)
- `ModPortugueseLanguageProvider` (`pt_br`)
Include block names, item names, metal types, interaction messages (`message.industry-made.*`), and tooltips (`tooltip.industry-made.*`).

### 8. Continuous Verification
Always execute and verify the Gradle pipeline before considering a task complete:
```powershell
./gradlew.bat compileKotlin
./gradlew.bat compileClientKotlin
./gradlew.bat runDatagen
./gradlew.bat build
```
Verify that all tests and compilation tasks return `BUILD SUCCESSFUL`.
