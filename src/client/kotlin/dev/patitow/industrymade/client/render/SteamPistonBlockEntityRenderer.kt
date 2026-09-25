package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.entity.SteamPistonBlockEntity
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

class SteamPistonRenderState : BlockEntityRenderState() {
    var stroke: Float = 0.0f
}

class SteamPistonBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<SteamPistonBlockEntity, SteamPistonRenderState> {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(IndustryMade.id("steam_piston"), "main")
        val TEXTURE: Identifier = IndustryMade.id("textures/entity/steam_piston.png")

        fun createLayerDefinition(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Base mounting plate
            root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f),
                PartPose.ZERO
            )

            // Outer cylinder body
            root.addOrReplaceChild(
                "cylinder",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-5.0f, 4.0f, -5.0f, 10.0f, 6.0f, 10.0f),
                PartPose.ZERO
            )

            // Moving piston rod & head (translates vertically)
            root.addOrReplaceChild(
                "shaft",
                CubeListBuilder.create()
                    .texOffs(0, 32)
                    .addBox(-3.0f, 6.0f, -3.0f, 6.0f, 6.0f, 6.0f),
                PartPose.ZERO
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val rootPart: ModelPart = context.bakeLayer(LAYER_LOCATION)
    private val basePart: ModelPart = rootPart.getChild("base")
    private val cylinderPart: ModelPart = rootPart.getChild("cylinder")
    private val shaftPart: ModelPart = rootPart.getChild("shaft")

    override fun createRenderState(): SteamPistonRenderState {
        return SteamPistonRenderState()
    }

    override fun extractRenderState(
        entity: SteamPistonBlockEntity,
        state: SteamPistonRenderState,
        partialTicks: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay)
        state.stroke = entity.getInterpolatedStroke(partialTicks)
    }

    override fun submit(
        state: SteamPistonRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        // Interpolated stroke movement (moves up by up to 5 pixels)
        val offset = state.stroke * 5.0f
        shaftPart.y = -offset

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE)) { _, consumer ->
            basePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            cylinderPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            shaftPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
        }

        poseStack.popPose()
    }
}
