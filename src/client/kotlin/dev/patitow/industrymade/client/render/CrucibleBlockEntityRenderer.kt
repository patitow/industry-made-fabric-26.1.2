package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.entity.CrucibleBlockEntity
import dev.patitow.industrymade.thermal.MetalRegistry
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
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
    var hasSolidItems: Boolean = false
}

class CrucibleBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<CrucibleBlockEntity, CrucibleRenderState> {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(IndustryMade.id("crucible"), "main")
        val TEXTURE: Identifier = IndustryMade.id("textures/entity/crucible.png")
        val LIQUID_TEXTURE: Identifier = Identifier.fromNamespaceAndPath("minecraft", "textures/block/lava_still.png")
        const val FULL_BRIGHT: Int = 0x00F000F0

        fun createLayerDefinition(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Bottom base plate
            root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-6.0f, 0.0f, -6.0f, 12.0f, 3.0f, 12.0f),
                PartPose.ZERO
            )

            // North wall
            root.addOrReplaceChild(
                "wall_north",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-6.0f, 3.0f, -6.0f, 12.0f, 11.0f, 2.0f),
                PartPose.ZERO
            )

            // South wall
            root.addOrReplaceChild(
                "wall_south",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-6.0f, 3.0f, 4.0f, 12.0f, 11.0f, 2.0f),
                PartPose.ZERO
            )

            // West wall
            root.addOrReplaceChild(
                "wall_west",
                CubeListBuilder.create()
                    .texOffs(0, 30)
                    .addBox(-6.0f, 3.0f, -4.0f, 2.0f, 11.0f, 8.0f),
                PartPose.ZERO
            )

            // East wall
            root.addOrReplaceChild(
                "wall_east",
                CubeListBuilder.create()
                    .texOffs(0, 30)
                    .addBox(4.0f, 3.0f, -4.0f, 2.0f, 11.0f, 8.0f),
                PartPose.ZERO
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val rootPart: ModelPart = context.bakeLayer(LAYER_LOCATION)
    private val basePart: ModelPart = rootPart.getChild("base")
    private val northPart: ModelPart = rootPart.getChild("wall_north")
    private val southPart: ModelPart = rootPart.getChild("wall_south")
    private val westPart: ModelPart = rootPart.getChild("wall_west")
    private val eastPart: ModelPart = rootPart.getChild("wall_east")

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
        state.hasSolidItems = entity.items.any { !it.isEmpty }
    }

    override fun submit(
        state: CrucibleRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        // 1. Render crucible structure
        val light = if (state.temperature >= 600.0) FULL_BRIGHT else state.lightCoords
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE)) { _, consumer ->
            basePart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY)
            northPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY)
            southPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY)
            westPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY)
            eastPart.render(poseStack, consumer, light, OverlayTexture.NO_OVERLAY)
        }

        // 2. If there is molten metal, render the glowing liquid level inside the crucible cavity
        if (state.moltenAmount > 0) {
            val metal = MetalRegistry.getMetal(state.moltenMetal)
            val color = metal?.colorRgb ?: 0xDF9B28
            val r = ((color shr 16) and 0xFF) / 255.0f
            val g = ((color shr 8) and 0xFF) / 255.0f
            val b = (color and 0xFF) / 255.0f

            // Liquid height rises with amount: 1 -> 0.28, 2 -> 0.38, 3 -> 0.48, 4 -> 0.58
            val liquidY = 0.18f + (state.moltenAmount * 0.10f)

            // Submit custom geometry for the glowing fluid surface
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(LIQUID_TEXTURE)) { pose, consumer ->
                val matrix = pose.pose()

                val x1 = -0.25f
                val x2 = 0.25f
                val z1 = -0.25f
                val z2 = 0.25f

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
        }

        poseStack.popPose()
    }
}
