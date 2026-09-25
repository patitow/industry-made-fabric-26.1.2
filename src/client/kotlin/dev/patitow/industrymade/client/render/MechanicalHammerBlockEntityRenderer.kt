package dev.patitow.industrymade.client.render

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.patitow.industrymade.block.MechanicalHammerBlock
import dev.patitow.industrymade.block.entity.MechanicalHammerBlockEntity
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.item.ItemStackRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.phys.Vec3

class MechanicalHammerRenderState : BlockEntityRenderState() {
    var facing: Direction = Direction.NORTH
    var hasItem: Boolean = false
    val itemRenderState: ItemStackRenderState = ItemStackRenderState()
}

class MechanicalHammerBlockEntityRenderer(private val context: BlockEntityRendererProvider.Context) :
    BlockEntityRenderer<MechanicalHammerBlockEntity, MechanicalHammerRenderState> {

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
        if (state.hasItem) {
            poseStack.pushPose()
            poseStack.translate(0.5, 0.465, 0.5)

            when (state.facing) {
                Direction.NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0f))
                Direction.SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f))
                Direction.WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f))
                Direction.EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270f))
                else -> {}
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(90f))
            poseStack.scale(0.5f, 0.5f, 0.5f)
            state.itemRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0)
            poseStack.popPose()
        }
    }
}
