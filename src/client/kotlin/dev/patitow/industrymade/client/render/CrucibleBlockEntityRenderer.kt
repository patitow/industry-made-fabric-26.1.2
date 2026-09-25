package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.entity.CrucibleBlockEntity
import dev.patitow.industrymade.thermal.MetalRegistry
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.phys.Vec3

class CrucibleRenderState : BlockEntityRenderState() {
    var moltenMetal: String = ""
    var moltenAmount: Int = 0
    var temperature: Double = 20.0
    val itemRenderStates: Array<ItemStackRenderState> = Array(4) { ItemStackRenderState() }
    val hasItems: BooleanArray = BooleanArray(4)
}

class CrucibleBlockEntityRenderer(private val context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderState> {

    companion object {
        val FLUID_TEXTURE: Identifier = IndustryMade.id("textures/entity/molten_fluid.png")
        const val FULL_BRIGHT: Int = 0x00F000F0

        // 2x2 grid offsets for the 4 item slots inside the crucible cavity
        val SLOT_OFFSETS = arrayOf(
            Pair(-0.12f, -0.12f), // Slot 0: NW
            Pair(0.12f, -0.12f),  // Slot 1: NE
            Pair(-0.12f, 0.12f),  // Slot 2: SW
            Pair(0.12f, 0.12f)    // Slot 3: SE
        )
    }

    override fun createRenderState(): CrucibleRenderState {
        return CrucibleRenderState()
    }

    override fun extractRenderState(
        entity: CrucibleBlockEntity,
        state: CrucibleRenderState,
        partialTicks: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay)
        state.moltenMetal = entity.moltenMetal
        state.moltenAmount = entity.moltenAmount
        state.temperature = entity.temperature

        for (i in 0 until 4) {
            val stack = entity.items[i]
            state.hasItems[i] = !stack.isEmpty
            if (state.hasItems[i] && entity.level != null) {
                context.itemModelResolver().updateForTopItem(
                    state.itemRenderStates[i],
                    stack,
                    ItemDisplayContext.GROUND,
                    entity.level,
                    null,
                    0
                )
            }
        }
    }

    override fun submit(
        state: CrucibleRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        // 1. Render solid items sitting inside the crucible cavity (if cavity isn't full of molten metal)
        if (state.moltenAmount < 4) {
            for (i in 0 until 4) {
                if (state.hasItems[i]) {
                    val (ox, oz) = SLOT_OFFSETS[i]
                    poseStack.pushPose()
                    poseStack.translate(0.5 + ox, 0.22, 0.5 + oz)
                    poseStack.mulPose(Axis.XP.rotationDegrees(90f))
                    val rotationDeg = i * 45f
                    poseStack.mulPose(Axis.ZP.rotationDegrees(rotationDeg))
                    poseStack.scale(0.32f, 0.32f, 0.32f)
                    state.itemRenderStates[i].submit(
                        poseStack,
                        collector,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        0
                    )
                    poseStack.popPose()
                }
            }
        }

        // 2. Render glowing molten liquid surface
        if (state.moltenAmount > 0) {
            val metal = MetalRegistry.getMetal(state.moltenMetal)
            val color = metal?.colorRgb ?: 0xDF9B28
            val r = ((color shr 16) and 0xFF) / 255.0f
            val g = ((color shr 8) and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f

            // Liquid height rises with amount inside crucible cavity (cavity from y=3 to y=14)
            val liquidY = 0.24f + (state.moltenAmount * 0.14f)

            poseStack.pushPose()
            poseStack.translate(0.5, 0.0, 0.5)

            // Submit custom geometry for the glowing fluid surface (double-sided quad for reliability)
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(FLUID_TEXTURE)) { pose, consumer ->
                val matrix = pose.pose()

                val x1 = -0.245f
                val x2 = 0.245f
                val z1 = -0.245f
                val z2 = 0.245f

                // Top face
                consumer.addVertex(matrix, x1, liquidY, z1)
                    .setColor(r, g, b, 1.0f)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)

                consumer.addVertex(matrix, x1, liquidY, z2)
                    .setColor(r, g, b, 1.0f)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)

                consumer.addVertex(matrix, x2, liquidY, z2)
                    .setColor(r, g, b, 1.0f)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)

                consumer.addVertex(matrix, x2, liquidY, z1)
                    .setColor(r, g, b, 1.0f)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, 1.0f, 0.0f)

                // Bottom face
                consumer.addVertex(matrix, x2, liquidY, z1)
                    .setColor(r, g, b, 1.0f)
                    .setUv(1.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f)

                consumer.addVertex(matrix, x2, liquidY, z2)
                    .setColor(r, g, b, 1.0f)
                    .setUv(1.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f)

                consumer.addVertex(matrix, x1, liquidY, z2)
                    .setColor(r, g, b, 1.0f)
                    .setUv(0.0f, 1.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f)

                consumer.addVertex(matrix, x1, liquidY, z1)
                    .setColor(r, g, b, 1.0f)
                    .setUv(0.0f, 0.0f)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(FULL_BRIGHT)
                    .setNormal(pose, 0.0f, -1.0f, 0.0f)
            }

            poseStack.popPose()
        }
    }
}
