package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.BronzeSteamPipeBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.thermal.SteamConsumer
import dev.patitow.industrymade.thermal.SteamProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.Locale

class BronzeSteamPipeBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val CODEC: MapCodec<BronzeSteamPipeBlock> = simpleCodec(::BronzeSteamPipeBlock)

        val NORTH: BooleanProperty = BlockStateProperties.NORTH
        val SOUTH: BooleanProperty = BlockStateProperties.SOUTH
        val EAST: BooleanProperty = BlockStateProperties.EAST
        val WEST: BooleanProperty = BlockStateProperties.WEST
        val UP: BooleanProperty = BlockStateProperties.UP
        val DOWN: BooleanProperty = BlockStateProperties.DOWN

        private val CORE_SHAPE: VoxelShape = box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0)
        private val NORTH_SHAPE: VoxelShape = box(5.0, 5.0, 0.0, 11.0, 11.0, 5.0)
        private val SOUTH_SHAPE: VoxelShape = box(5.0, 5.0, 11.0, 11.0, 11.0, 16.0)
        private val WEST_SHAPE: VoxelShape = box(0.0, 5.0, 5.0, 5.0, 11.0, 11.0)
        private val EAST_SHAPE: VoxelShape = box(11.0, 5.0, 5.0, 16.0, 11.0, 11.0)
        private val UP_SHAPE: VoxelShape = box(5.0, 11.0, 5.0, 11.0, 16.0, 11.0)
        private val DOWN_SHAPE: VoxelShape = box(5.0, 0.0, 5.0, 11.0, 5.0, 11.0)

        fun canConnectTo(level: BlockGetter, neighborPos: BlockPos, fromSide: Direction): Boolean {
            val state = level.getBlockState(neighborPos)
            if (state.block is BronzeSteamPipeBlock ||
                state.block is LowPressureBoilerBlock ||
                state.block is SteamPistonBlock ||
                state.block is MechanicalHammerBlock
            ) {
                return true
            }

            val be = level.getBlockEntity(neighborPos)
            return be is SteamProvider || be is SteamConsumer
        }
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(EAST, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        val level = context.level
        val pos = context.clickedPos
        return defaultBlockState()
            .setValue(NORTH, canConnectTo(level, pos.north(), Direction.NORTH))
            .setValue(SOUTH, canConnectTo(level, pos.south(), Direction.SOUTH))
            .setValue(WEST, canConnectTo(level, pos.west(), Direction.WEST))
            .setValue(EAST, canConnectTo(level, pos.east(), Direction.EAST))
            .setValue(UP, canConnectTo(level, pos.above(), Direction.UP))
            .setValue(DOWN, canConnectTo(level, pos.below(), Direction.DOWN))
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource
    ): BlockState {
        val property = when (directionToNeighbour) {
            Direction.NORTH -> NORTH
            Direction.SOUTH -> SOUTH
            Direction.WEST -> WEST
            Direction.EAST -> EAST
            Direction.UP -> UP
            Direction.DOWN -> DOWN
        }
        val connects = canConnectTo(level, neighbourPos, directionToNeighbour)
        return state.setValue(property, connects)
    }

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape {
        var shape = CORE_SHAPE
        if (state.getValue(NORTH)) shape = Shapes.or(shape, NORTH_SHAPE)
        if (state.getValue(SOUTH)) shape = Shapes.or(shape, SOUTH_SHAPE)
        if (state.getValue(WEST)) shape = Shapes.or(shape, WEST_SHAPE)
        if (state.getValue(EAST)) shape = Shapes.or(shape, EAST_SHAPE)
        if (state.getValue(UP)) shape = Shapes.or(shape, UP_SHAPE)
        if (state.getValue(DOWN)) shape = Shapes.or(shape, DOWN_SHAPE)
        return shape
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos) as? BronzeSteamPipeBlockEntity ?: return InteractionResult.PASS

        if (!level.isClientSide) {
            val pressure = String.format(Locale.US, "%.1f", be.steamPressure)
            val pressureColor = when {
                be.steamPressure >= 5.0 -> "§c"
                be.steamPressure >= 1.5 -> "§a"
                be.steamPressure >= 0.5 -> "§e"
                else -> "§7"
            }

            val connectedCount = listOf(
                state.getValue(NORTH), state.getValue(SOUTH),
                state.getValue(WEST), state.getValue(EAST),
                state.getValue(UP), state.getValue(DOWN)
            ).count { it }

            player.sendSystemMessage(
                Component.literal("§6Tubo de Vapor§r: $pressureColor${pressure} / 6.0 bar§r | §b$connectedCount conexões§r")
            )
            level.playSound(
                null,
                pos,
                SoundEvents.COMPARATOR_CLICK,
                SoundSource.BLOCKS,
                0.5f,
                1.6f
            )
        }

        return InteractionResult.SUCCESS
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BronzeSteamPipeBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        if (blockEntityType != ModBlockEntities.BRONZE_STEAM_PIPE) return null
        return BronzeSteamPipeBlockEntity.createTicker() as BlockEntityTicker<T>
    }
}
