package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.LowPressureBoilerBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
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
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

class LowPressureBoilerBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val CODEC: MapCodec<LowPressureBoilerBlock> = simpleCodec(::LowPressureBoilerBlock)

        val FACING: EnumProperty<Direction> = BlockStateProperties.HORIZONTAL_FACING
        val LIT: BooleanProperty = BlockStateProperties.LIT
        val PRESSURE_LEVEL: IntegerProperty = IntegerProperty.create("pressure_level", 0, 3)

        private val SHAPE: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0)
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
                .setValue(PRESSURE_LEVEL, 0)
        )
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING, LIT, PRESSURE_LEVEL)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return defaultBlockState().setValue(FACING, context.horizontalDirection.opposite)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    override fun getRenderShape(state: BlockState): RenderShape {
        return RenderShape.MODEL
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
        val be = level.getBlockEntity(pos) as? LowPressureBoilerBlockEntity
            ?: return InteractionResult.PASS

        // 1. Water filling with water bucket
        if (stack.`is`(Items.WATER_BUCKET)) {
            if (be.waterAmount < LowPressureBoilerBlockEntity.MAX_WATER) {
                if (!level.isClientSide) {
                    be.waterAmount = (be.waterAmount + 1000).coerceAtMost(LowPressureBoilerBlockEntity.MAX_WATER)
                    if (!player.isCreative) {
                        player.setItemInHand(hand, ItemStack(Items.BUCKET))
                    }
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.BUCKET_EMPTY,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                    )
                    val serverLevel = level as? ServerLevel
                    serverLevel?.sendParticles(
                        ParticleTypes.SPLASH,
                        pos.x + 0.5, pos.y + 0.9, pos.z + 0.5,
                        15,
                        0.25, 0.1, 0.25,
                        0.1
                    )
                    player.sendSystemMessage(
                        Component.literal("§bÁgua na Caldeira: ${be.waterAmount}/4000 mB (+1 Balde)§r")
                    )
                    be.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)
                }
                return InteractionResult.SUCCESS
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(
                        Component.literal("§eA caldeira já está cheia de água (4/4 Baldes)!§r")
                    )
                }
                return InteractionResult.CONSUME
            }
        }

        // 2. Emptying water with empty bucket
        if (stack.`is`(Items.BUCKET)) {
            if (be.waterAmount >= 1000) {
                if (!level.isClientSide) {
                    be.waterAmount -= 1000
                    if (!player.isCreative) {
                        stack.shrink(1)
                        val filledBucket = ItemStack(Items.WATER_BUCKET)
                        if (!player.inventory.add(filledBucket)) {
                            player.drop(filledBucket, false)
                        }
                    }
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.BUCKET_FILL,
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                    )
                    player.sendSystemMessage(
                        Component.literal("§bÁgua restante na Caldeira: ${be.waterAmount}/4000 mB (-1 Balde)§r")
                    )
                    be.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)
                }
                return InteractionResult.SUCCESS
            }
        }

        return InteractionResult.PASS
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos) as? LowPressureBoilerBlockEntity ?: return InteractionResult.PASS

        if (!level.isClientSide) {
            val temp = be.temperature.toInt()
            val waterBuckets = String.format(java.util.Locale.US, "%.1f", be.waterAmount / 1000.0)
            val pressure = String.format(java.util.Locale.US, "%.1f", be.steamPressure)

            val pressureColor = when {
                be.steamPressure >= 6.0 -> "§c"
                be.steamPressure >= 2.0 -> "§a"
                be.steamPressure >= 0.5 -> "§e"
                else -> "§7"
            }

            player.sendSystemMessage(
                Component.literal("§6Caldeira a Vapor§r: §e${temp}°C§r | §bÁgua: ${waterBuckets}/4.0 Baldes§r | $pressureColor${pressure} bar§r")
            )
            level.playSound(
                null,
                pos,
                SoundEvents.COMPARATOR_CLICK,
                SoundSource.BLOCKS,
                0.6f,
                1.4f
            )
        }

        return InteractionResult.SUCCESS
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return LowPressureBoilerBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        if (blockEntityType != ModBlockEntities.LOW_PRESSURE_BOILER) return null
        return LowPressureBoilerBlockEntity.createTicker() as BlockEntityTicker<T>
    }
}
