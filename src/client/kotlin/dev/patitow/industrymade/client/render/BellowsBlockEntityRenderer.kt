package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.BellowsBlock
import dev.patitow.industrymade.block.entity.BellowsBlockEntity
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
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.Vec3

class BellowsRenderState : BlockEntityRenderState() {
    var facing: Direction = Direction.NORTH
    var progress: Float = 0.0f
}

class BellowsBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<BellowsBlockEntity, BellowsRenderState> {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(IndustryMade.id("bellows"), "main")
        val TEXTURE: Identifier = IndustryMade.id("textures/entity/bellows.png")

        fun createLayerDefinition(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Base plate (fixed on floor)
            root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-7.0f, 0.0f, -7.0f, 14.0f, 2.0f, 14.0f),
                PartPose.ZERO
            )

            // Front air nozzle (fixed)
            root.addOrReplaceChild(
                "nozzle",
                CubeListBuilder.create()
                    .texOffs(0, 32)
                    .addBox(-2.0f, 1.0f, 7.0f, 4.0f, 4.0f, 4.0f),
                PartPose.ZERO
            )

            // Flexible leather bellows chamber (collapses/scales vertically)
            root.addOrReplaceChild(
                "leather",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-6.0f, 2.0f, -6.0f, 12.0f, 8.0f, 12.0f),
                PartPose.ZERO
            )

            // Top wooden plate (compresses downwards towards base)
            root.addOrReplaceChild(
                "top",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-7.0f, 10.0f, -7.0f, 14.0f, 2.0f, 14.0f),
                PartPose.ZERO
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val rootPart: ModelPart = context.bakeLayer(LAYER_LOCATION)
    private val basePart: ModelPart = rootPart.getChild("base")
    private val nozzlePart: ModelPart = rootPart.getChild("nozzle")
    private val leatherPart: ModelPart = rootPart.getChild("leather")
    private val topPart: ModelPart = rootPart.getChild("top")

    override fun createRenderState(): BellowsRenderState {
        return BellowsRenderState()
    }

    override fun extractRenderState(
        entity: BellowsBlockEntity,
        state: BellowsRenderState,
        partialTicks: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay)
        state.facing = entity.blockState.getValue(BellowsBlock.FACING)
        state.progress = entity.getInterpolatedProgress(partialTicks)
    }

    override fun submit(
        state: BellowsRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        poseStack.pushPose()

        // Center block origin
        poseStack.translate(0.5, 0.0, 0.5)

        // Rotate towards facing direction
        val yRot = when (state.facing) {
            Direction.SOUTH -> 0.0f
            Direction.WEST -> 90.0f
            Direction.NORTH -> 180.0f
            Direction.EAST -> 270.0f
            else -> 0.0f
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot))

        // Compression animation calculations
        val progress = state.progress.coerceIn(0.0f, 1.0f)
        val maxCompressionOffset = 6.0f

        // Adjust top plate offset
        topPart.y = -progress * maxCompressionOffset

        // Scale leather chamber vertically
        val leatherScaleY = 1.0f - (progress * 0.65f)
        leatherPart.yScale = leatherScaleY

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE)) { _, consumer ->
            basePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            nozzlePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            leatherPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            topPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
        }

        poseStack.popPose()
    }
}
