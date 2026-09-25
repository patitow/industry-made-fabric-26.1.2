package dev.patitow.industrymade.block

import dev.patitow.industrymade.block.entity.BellowsBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.redstone.Orientation
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class BellowsBlock(properties: Properties) : Block(properties), EntityBlock {

    companion object {
        val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
        val POWERED: BooleanProperty = BlockStateProperties.POWERED

        private val SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0)
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, POWERED)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        // Points towards the block face the player is targeting / looking
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos) as? BellowsBlockEntity ?: return InteractionResult.PASS
        return if (be.pump()) {
            InteractionResult.SUCCESS
        } else {
            InteractionResult.CONSUME
        }
    }

    override fun neighborChanged(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        sourceBlock: Block,
        orientation: Orientation?,
        notify: Boolean
    ) {
        if (level.isClientSide) return
        val hasSignal = level.hasNeighborSignal(pos)
        val isPowered = state.getValue(POWERED)

        if (hasSignal != isPowered) {
            level.setBlock(pos, state.setValue(POWERED, hasSignal), UPDATE_CLIENTS)
            if (hasSignal) {
                (level.getBlockEntity(pos) as? BellowsBlockEntity)?.pump()
            }
        }
    }

    override fun triggerEvent(state: BlockState, level: Level, pos: BlockPos, id: Int, param: Int): Boolean {
        super.triggerEvent(state, level, pos, id, param)
        val be = level.getBlockEntity(pos) ?: return false
        return be.triggerEvent(id, param)
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BellowsBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (blockEntityType != ModBlockEntities.BELLOWS) return null
        return BlockEntityTicker { lvl, pos, st, be ->
            if (lvl.isClientSide) {
                BellowsBlockEntity.clientTick(lvl, pos, st, be as BellowsBlockEntity)
            } else {
                BellowsBlockEntity.serverTick(lvl, pos, st, be as BellowsBlockEntity)
            }
        }
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }
}
