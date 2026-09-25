package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.MechanicalHammerBlock
import dev.patitow.industrymade.block.entity.MechanicalHammerBlockEntity
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
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.phys.Vec3

class MechanicalHammerRenderState : BlockEntityRenderState() {
    var facing: Direction = Direction.NORTH
    var hammerAngle: Float = 0.0f
    var hasItem: Boolean = false
    val itemRenderState: ItemStackRenderState = ItemStackRenderState()
}

class MechanicalHammerBlockEntityRenderer(private val context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<MechanicalHammerBlockEntity, MechanicalHammerRenderState> {

    companion object {
        val LAYER_LOCATION: ModelLayerLocation =
            ModelLayerLocation(IndustryMade.id("mechanical_hammer"), "main")
        val TEXTURE: Identifier = IndustryMade.id("textures/entity/mechanical_hammer.png")

        fun createLayerDefinition(): LayerDefinition {
            val mesh = MeshDefinition()
            val root = mesh.root

            // Base mounting plate
            root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-7.0f, 0.0f, -7.0f, 14.0f, 4.0f, 14.0f),
                PartPose.ZERO
            )

            // Anvil striking bed in center
            root.addOrReplaceChild(
                "anvil",
                CubeListBuilder.create()
                    .texOffs(0, 18)
                    .addBox(-4.0f, 4.0f, -4.0f, 8.0f, 4.0f, 8.0f),
                PartPose.ZERO
            )

            // Heavy upright rear support frame
            root.addOrReplaceChild(
                "frame",
                CubeListBuilder.create()
                    .texOffs(32, 0)
                    .addBox(-3.0f, 4.0f, 3.0f, 6.0f, 12.0f, 4.0f),
                PartPose.ZERO
            )

            // Pivot point for hammer arm at (0, 14, 4)
            val hammerArm = root.addOrReplaceChild(
                "hammer_arm",
                CubeListBuilder.create()
                    .texOffs(0, 30)
                    .addBox(-2.0f, -1.0f, -10.0f, 4.0f, 2.0f, 11.0f),
                PartPose.offset(0.0f, 14.0f, 4.0f)
            )

            // Heavy strike head at the front of the arm
            hammerArm.addOrReplaceChild(
                "hammer_head",
                CubeListBuilder.create()
                    .texOffs(0, 43)
                    .addBox(-3.0f, 0.0f, -11.0f, 6.0f, 6.0f, 5.0f),
                PartPose.ZERO
            )

            return LayerDefinition.create(mesh, 64, 64)
        }
    }

    private val rootPart: ModelPart = context.bakeLayer(LAYER_LOCATION)
    private val basePart: ModelPart = rootPart.getChild("base")
    private val anvilPart: ModelPart = rootPart.getChild("anvil")
    private val framePart: ModelPart = rootPart.getChild("frame")
    private val hammerArmPart: ModelPart = rootPart.getChild("hammer_arm")

    override fun createRenderState(): MechanicalHammerRenderState {
        return MechanicalHammerRenderState()
    }

    override fun extractRenderState(
        entity: MechanicalHammerBlockEntity,
        state: MechanicalHammerRenderState,
        partialTicks: Float,
        cameraPos: Vec3,
        crumblingOverlay: ModelFeatureRenderer.CrumblingOverlay?
    ) {
        BlockEntityRenderState.extractBase(entity, state, crumblingOverlay)
        state.facing = entity.blockState.getValue(MechanicalHammerBlock.FACING)

        val prev = entity.prevAnimTicks.toFloat()
        val curr = entity.animTicks.toFloat()
        val delta = if (curr < prev) curr + 40f - prev else curr - prev
        val tick = (prev + delta * partialTicks) % 40f

        // Kinematic curve: 0..26 slow rise, 26..30 violent slam down, 30..40 rest
        val angle = when {
            tick < 26f -> (tick / 26f) * 32.0f
            tick < 30f -> (1.0f - (tick - 26f) / 4.0f) * 32.0f
            else -> 0.0f
        }
        state.hammerAngle = angle

        state.hasItem = !entity.itemOnBed.isEmpty
        if (state.hasItem && entity.level != null) {
            context.itemModelResolver().updateForTopItem(
                state.itemRenderState,
                entity.itemOnBed,
                ItemDisplayContext.GROUND,
                entity.level,
                null,
                0
            )
        }
    }

    override fun submit(
        state: MechanicalHammerRenderState,
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        cameraRenderState: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.translate(0.5, 0.0, 0.5)

        // Rotate facing
        when (state.facing) {
            Direction.SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f))
            Direction.WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f))
            Direction.EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270f))
            else -> {}
        }

        // Set interpolated hammer arm angle
        hammerArmPart.xRot = -Math.toRadians(state.hammerAngle.toDouble()).toFloat()

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(TEXTURE)) { _, consumer ->
            basePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            anvilPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            framePart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
            hammerArmPart.render(poseStack, consumer, state.lightCoords, OverlayTexture.NO_OVERLAY)
        }

        // Render placed item on the anvil bed in the 3D world
        if (state.hasItem) {
            poseStack.pushPose()
            poseStack.translate(0.0, 0.51, 0.0)
            poseStack.mulPose(Axis.XP.rotationDegrees(90f))
            poseStack.scale(0.55f, 0.55f, 0.55f)
            state.itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)
            poseStack.popPose()
        }

        poseStack.popPose()
    }
}
