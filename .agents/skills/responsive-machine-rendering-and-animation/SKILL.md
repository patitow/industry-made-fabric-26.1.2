---
name: responsive-machine-rendering-and-animation
description: >-
  Guidelines, specifications, and code patterns for implementing live responsive states in Minecraft
  blocks and machines: cavity fluid rendering, visual telemetry dials, sight glasses, firebox illumination,
  valve handwheels, and partialTicks continuous animations.
---

# ⚡ Responsive Machine Rendering & Animation Skill

This skill provides the architecture, mathematical formulas, and safe Minecraft 26.x rendering patterns to ensure **no machine or industrial block feels static, frozen, or like a dead box**. Every machine must visibly respond to contents, energy, pressure, and operational state.

---

## 🌟 1. The 4 Pillars of Living Machinery

```
                                  LIVING MACHINERY
                                         │
        ┌──────────────────┬─────────────┴─────────────┬──────────────────┐
        ▼                  ▼                           ▼                  ▼
┌──────────────┐   ┌──────────────┐            ┌──────────────┐   ┌──────────────┐
│ Cavity Fluid │   │ Visual Dials │            │ Firebox Glow │   │ Kinetic Flow │
│  Visibility  │   │  & Telemetry │            │ & Particles  │   │(partialTicks)│
└──────────────┘   └──────────────┘            └──────────────┘   └──────────────┘
```

1. **Cavity Fluid Visibility:** If a machine contains liquid (metal in crucible, water in boiler, oil in tank), the surface MUST be visible in-world at the correct level and color.
2. **Visual Telemetry & Dials:** Pressure, torque, and temperatures must be indicated directly on the block via gauge needles, sight glasses, or valve angles.
3. **Firebox & Thermal Glow:** Heated and fueled machines must change texture state when lit (`LIT=true`), displaying bright embers and casting block light.
4. **Continuous Kinetic Animation (`partialTicks`):** Pistons, hammer arms, bellows, and saw blades must glide smoothly at 60+ FPS instead of jumping every 20 TPS server tick.

---

## 🌊 2. Cavity Fluid Surface Rendering (Safe OpenGL in 26.x)

When rendering molten metals or liquids inside block cavities (e.g. `CrucibleBlock`):
- The block's solid casing is rendered natively by the chunk engine (`RenderShape.MODEL`) for 1000+ FPS ambient occlusion.
- The `BlockEntityRenderer` ONLY renders the dynamic fluid surface.

### 🛑 CRITICAL MINECRAFT 26.x RULE:
Never pass `poseStack` into deferred consumer lambdas after `popPose()`:
```kotlin
// ❌ WRONG (Causes sky vertex explosions):
collector.submitCustomGeometry(poseStack, renderType) { _, consumer ->
    modelPart.render(poseStack, consumer, light, overlay) // poseStack is already popped!
}

// ✅ CORRECT (Safe deferred OpenGL geometry):
collector.submitCustomGeometry(poseStack, renderType) { pose, consumer ->
    val matrix = pose.pose() // Uses the CAPTURED matrix pose!
    consumer.addVertex(matrix, x, y, z).setColor(r, g, b, 1.0f).setUv(u, v).setLight(FULL_BRIGHT).setNormal(pose, 0f, 1f, 0f)
}
```

### Fluid Level & Color Formula:
```kotlin
val liquidY = baseCavityBottom + (contentAmount * stepHeightPerUnit)
val r = ((colorRgb shr 16) and 0xFF) / 255.0f
val g = ((colorRgb shr 8) and 0xFF) / 255.0f
val b = (colorRgb and 0xFF) / 255.0f
```

---

## ⏱️ 3. Visual Telemetry: Pressure Gauges & Dials

For pressure vessels (boilers, steam pipes), use discrete blockstate properties for native chunk speed:
- `PRESSURE_LEVEL: IntegerProperty.create("pressure_level", 0, 3)`
  - `0`: Idle (needle far left, 0.0 bar, black zone)
  - `1`: Low Operating (needle tilted left, 1.5 bar, yellow zone)
  - `2`: Nominal Power (needle upright, 3.5 bar, green zone)
  - `3`: Overpressure Danger (needle far right, 6.5+ bar, red zone)

---

## 🔄 4. Smooth Kinetic Animation with `partialTicks`

For kinetic machinery (pistons, hammer arms, flywheel):
1. **Server/Logic Ticker:** Updates integer ticks:
   ```kotlin
   prevAnimTicks = animTicks
   animTicks = (animTicks + 1) % CYCLE_TICKS
   ```
2. **Client Render State Extractor:**
   ```kotlin
   override fun extractRenderState(entity: T, state: S, partialTicks: Float, ...) {
       super.extractRenderState(entity, state, partialTicks, ...)
       state.progress = Mth.lerp(partialTicks, entity.prevAnimTicks.toFloat(), entity.animTicks.toFloat()) / CYCLE_TICKS
   }
   ```
3. **Renderer:** Calculates smooth rotation or translation from `state.progress`.

---

## 📋 5. Feature Design Verification Checklist

When specifying or implementing any new industrial block, verify:
```
[ ] Does this block contain liquid? If yes, is the fluid surface rendered inside?
[ ] Does this block have pressure or heat? If yes, is there a visual needle/gauge or flame glow?
[ ] Does this block have kinetic motion? If yes, does it interpolate with partialTicks?
[ ] Are state-driven models defined in blockstates with all permutations?
[ ] Is OpenGL custom geometry safely using pose.pose() instead of popped outer stacks?
```
