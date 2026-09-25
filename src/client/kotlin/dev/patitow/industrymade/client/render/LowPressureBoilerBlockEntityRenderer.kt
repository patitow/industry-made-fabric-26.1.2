package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.LowPressureBoilerBlock
import dev.patitow.industrymade.block.entity.LowPressureBoilerBlockEntity
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

class LowPressureBoilerRenderState : BlockEntityRenderState() {
    var facing: Direction = Direction.NORTH
    var pressure: Float = 0.0f
    var isBoiling: Boolean = false
}

class LowPressureBoilerBlockEntityRenderer(context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<LowPressureBoilerBlockEntity, LowPressureBoilerRenderState> {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(IndustryMade.id("low_pressure_boiler"), "main")
        val TEXTURE: Identifier = IndustryMade.id("textures/entity/low_pressure_boiler.png")

        fun createLayerDefinition(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Base mounting legs
            root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-7.0f, 0.0f, -7.0f, 14.0f, 2.0f, 14.0f),
                PartPose.ZERO
            )

            // Main bronze pressure vessel
            root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                    .texOffs(0, 16)
                    .addBox(-6.0f, 2.0f, -6.0f, 12.0f, 12.0f, 12.0f),
                PartPose.ZERO
            )

            // Top exhaust steam pipe / relief nozzle
            root.addOrReplaceChild(
                "nozzle",
                CubeListBuilder.create()
                    .texOffs(0, 40)
                    .addBox(-2.0f, 14.0f, -2.0f, 4.0f, 2.0f, 4.0f),
                PartPose.ZERO
            )

            // Front brass pressure gauge housing
            root.addOrReplaceChild(
                "gauge",
                CubeListBuilder.create()
                    .texOffs(16, 40)
                    .addBox(-2.0f, 7.0f, 6.0f, 4.0f, 4.0f, 1.0f),
                PartPose.ZERO
            )

            // Pressure needle (rotates around center of dial)
            root.addOrReplaceChild(
                "needle",
                CubeListBuilder.create()
                    .texOffs(26, 40)
                    .addBox(-0.5f, 0.0f, 6.2f, 1.0f, 2.0f, 0.2f),
                PartPose.offset(0.0f, 9.0f, 0.0f)
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val rootPart: ModelPart = context.bakeLayer(LAYER_LOCATION)
    private val basePart: ModelPart = rootPart.getChild("base")
    private val bodyPart: ModelPart = rootPart.getChild("body")
    private val nozzlePart: ModelPart = rootPart.getChild("nozzle")
    private val gaugePart: ModelPart = rootPart.getChild("gauge")
    private val needlePart: ModelPart = rootPart.getChild("needle")

    override fun createRenderState(): LowPressureBoilerRenderState {
        return LowPressureBoilerRenderState()
    }

    override fun extractRenderState(
        entity: LowPressureBoilerBlockEntity,
        state: LowPressureBoilerRenderState,
        partialTicks: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay)
        state.facing = entity.blockState.getValue(LowPressureBoilerBlock.FACING)
        state.pressure = entity.steamPressure.toFloat()
        state.isBoiling = entity.blockState.getValue(LowPressureBoilerBlock.LIT)
    }

    override fun submit(
        state: LowPressureBoilerRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        // Rotate towards facing
        val yRot = when (state.facing) {
            Direction.SOUTH -> 0.0f
            Direction.WEST -> 90.0f
            Direction.NORTH -> 180.0f
            Direction.EAST -> 270.0f
            else -> 0.0f
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot))

        // Rotate pressure needle based on steam pressure (from -60 deg to +120 deg)
        val pressureRatio = (state.pressure / LowPressureBoilerBlockEntity.MAX_PRESSURE.toFloat()).coerceIn(0.0f, 1.0f)
        val needleAngleDeg = -60.0f + (pressureRatio * 180.0f)
        needlePart.zRot = (needleAngleDeg * Math.PI.toFloat() / 180.0f)

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE)) { _, consumer ->
            basePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            bodyPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            nozzlePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            gaugePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            needlePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
        }

        poseStack.popPose()
    }
}
