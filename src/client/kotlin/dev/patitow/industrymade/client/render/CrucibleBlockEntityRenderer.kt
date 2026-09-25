package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.patitow.industrymade.block.entity.CrucibleBlockEntity
import dev.patitow.industrymade.thermal.MetalRegistry
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3

class CrucibleRenderState : BlockEntityRenderState() {
    var moltenMetal: String = ""
    var moltenAmount: Int = 0
    var temperature: Double = 20.0
}

class CrucibleBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderState> {

    companion object {
        val LIQUID_TEXTURE: Identifier = Identifier.fromNamespaceAndPath("minecraft", "textures/block/lava_still.png")
        const val FULL_BRIGHT: Int = 0x00F000F0
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
    }

    override fun submit(
        state: CrucibleRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        // If there is molten metal, render the glowing liquid level inside the crucible cavity
        if (state.moltenAmount > 0) {
            val metal = MetalRegistry.getMetal(state.moltenMetal)
            val color = metal?.colorRgb ?: 0xDF9B28
            val r = ((color shr 16) and 0xFF) / 255.0f
            val g = ((color shr 8) and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f

            // Liquid height rises with amount inside crucible cavity (cavity from y=4 to y=14)
            // In 0.0 to 1.0 block space: y = 0.25 (1 item) up to 0.70 (4 items)
            val liquidY = 0.22f + (state.moltenAmount * 0.12f)

            poseStack.pushPose()
            poseStack.translate(0.5, 0.0, 0.5)

            // Submit custom geometry for the glowing fluid surface using captured pose matrix
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(LIQUID_TEXTURE)) { pose, consumer ->
                val matrix = pose.pose()

                val x1 = -0.245f
                val x2 = 0.245f
                val z1 = -0.245f
                val z2 = 0.245f

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
            }

            poseStack.popPose()
        }
    }
}
