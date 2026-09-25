package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.MechanicalHammerBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class MechanicalHammerBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val CODEC: MapCodec<MechanicalHammerBlock> = simpleCodec(::MechanicalHammerBlock)
        val FACING: EnumProperty<Direction> = HorizontalDirectionalBlock.FACING
        val POWERED: BooleanProperty = BlockStateProperties.POWERED

        private val BASE_SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
        private val ANVIL_BED: VoxelShape = box(4.0, 4.0, 4.0, 12.0, 8.0, 12.0)
        private val FRAME: VoxelShape = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0)
        val SHAPE: VoxelShape = Shapes.or(BASE_SHAPE, ANVIL_BED, FRAME)
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, POWERED)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState()
            .setValue(FACING, context.horizontalDirection.opposite)
            .setValue(POWERED, false)
    }

    override fun rotate(state: BlockState, rotation: Rotation): BlockState {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
    }

    override fun mirror(state: BlockState, mirror: Mirror): BlockState {
        return state.rotate(mirror.getRotation(state.getValue(FACING)))
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    override fun useItemOn(
        stack: ItemStack,
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hitResult: BlockHitResult
    ): InteractionResult {
        val blockEntity = level.getBlockEntity(pos) as? MechanicalHammerBlockEntity ?: return InteractionResult.PASS

        // Player wants to retrieve item (empty hand or sneaking)
        if (player.isShiftKeyDown || stack.isEmpty) {
            if (!blockEntity.itemOnBed.isEmpty) {
                if (!level.isClientSide) {
                    val toGive = blockEntity.itemOnBed.copy()
                    blockEntity.itemOnBed = ItemStack.EMPTY
                    blockEntity.strikes = 0
                    blockEntity.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)

                    if (!player.inventory.add(toGive)) {
                        Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.6, pos.z + 0.5, toGive)
                    }
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f)
                }
                return InteractionResult.SUCCESS
            }
            return InteractionResult.PASS
        }

        // Player wants to place item on anvil bed
        if (blockEntity.itemOnBed.isEmpty) {
            if (MechanicalHammerBlockEntity.isValidHammerInput(stack)) {
                if (!level.isClientSide) {
                    blockEntity.itemOnBed = stack.split(1)
                    blockEntity.strikes = 0
                    blockEntity.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)
                    level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.BLOCKS, 1.0f, 1.0f)
                }
                return InteractionResult.SUCCESS
            }
        }

        return InteractionResult.PASS
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState) {
        if (!state.`is`(newState.block)) {
            val blockEntity = level.getBlockEntity(pos) as? MechanicalHammerBlockEntity
            if (blockEntity != null && !blockEntity.itemOnBed.isEmpty) {
                Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, blockEntity.itemOnBed)
            }
            super.onRemove(state, level, pos, newState)
        }
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return MechanicalHammerBlockEntity(pos, state)
    }

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        return if (!level.isClientSide) {
            createTickerHelper(blockEntityType, ModBlockEntities.MECHANICAL_HAMMER, MechanicalHammerBlockEntity.createTicker())
        } else null
    }
}
