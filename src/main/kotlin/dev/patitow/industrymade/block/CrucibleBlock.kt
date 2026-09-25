package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.CrucibleBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModDataComponents
import dev.patitow.industrymade.init.ModItems
import dev.patitow.industrymade.item.HotIngotMoldItem
import dev.patitow.industrymade.thermal.MetalRegistry
import net.minecraft.core.BlockPos
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
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class CrucibleBlock(properties: Properties) : BaseEntityBlock(properties) {

    companion object {
        val CODEC: MapCodec<CrucibleBlock> = simpleCodec(::CrucibleBlock)

        val HEAT_LEVEL: IntegerProperty = IntegerProperty.create("heat_level", 0, 3)

        private val BOTTOM: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0)
        private val NORTH_WALL: VoxelShape = box(2.0, 3.0, 2.0, 14.0, 14.0, 4.0)
        private val SOUTH_WALL: VoxelShape = box(2.0, 3.0, 12.0, 14.0, 14.0, 14.0)
        private val WEST_WALL: VoxelShape = box(2.0, 3.0, 4.0, 4.0, 14.0, 12.0)
        private val EAST_WALL: VoxelShape = box(12.0, 3.0, 4.0, 14.0, 14.0, 12.0)
        val SHAPE: VoxelShape = Shapes.or(BOTTOM, NORTH_WALL, SOUTH_WALL, WEST_WALL, EAST_WALL)

        private val COLLISION_BOTTOM: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0)
        val COLLISION_SHAPE: VoxelShape = Shapes.or(COLLISION_BOTTOM, NORTH_WALL, SOUTH_WALL, WEST_WALL, EAST_WALL)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(HEAT_LEVEL, 0))
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(HEAT_LEVEL)
    }

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return SHAPE
    }

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return COLLISION_SHAPE
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
        val be = level.getBlockEntity(pos) as? CrucibleBlockEntity
            ?: return InteractionResult.PASS

        // 1. Dipping a ceramic mold into molten metal
        if (stack.`is`(ModItems.CERAMIC_MOLD)) {
            if (be.moltenAmount > 0) {
                if (!level.isClientSide) {
                    val metalId = be.moltenMetal
                    be.moltenAmount--
                    if (be.moltenAmount == 0) {
                        be.moltenMetal = ""
                    }

                    if (!player.isCreative) {
                        stack.shrink(1)
                    }

                    val hotMold = ItemStack(ModItems.HOT_INGOT_MOLD, 1)
                    hotMold.set(ModDataComponents.MOLTEN_METAL, metalId)
                    hotMold.set(ModDataComponents.COOL_DOWN_AT, level.gameTime + HotIngotMoldItem.DEFAULT_COOLING_TICKS)

                    if (!player.inventory.add(hotMold)) {
                        player.drop(hotMold, false)
                    }

                    level.playSound(
                        null,
                        pos,
                        SoundEvents.BUCKET_EMPTY_LAVA,
                        SoundSource.BLOCKS,
                        0.7f,
                        1.1f
                    )
                    be.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)
                }
                return InteractionResult.SUCCESS
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(
                        Component.translatable("message.industry-made.crucible_no_molten")
                    )
                }
                return InteractionResult.CONSUME
            }
        }

        // 2. Inserting meltable items (Raw ores, ingots)
        val meltResult = MetalRegistry.getMeltableResult(stack)
        if (meltResult != null) {
            val hitX = hitResult.location.x - pos.x
            val hitZ = hitResult.location.z - pos.z
            val targetSlot = (if (hitZ >= 0.5) 2 else 0) + (if (hitX >= 0.5) 1 else 0)
            val slotToUse = if (be.items[targetSlot].isEmpty) targetSlot else be.items.indexOfFirst { it.isEmpty }

            if (slotToUse != -1) {
                if (!level.isClientSide) {
                    be.items[slotToUse] = stack.copyWithCount(1)
                    if (!player.isCreative) {
                        stack.shrink(1)
                    }

                    level.playSound(
                        null,
                        pos,
                        SoundEvents.STONE_PLACE,
                        SoundSource.BLOCKS,
                        0.8f,
                        1.0f
                    )
                    be.setChanged()
                    level.sendBlockUpdated(pos, state, state, 3)
                }
                return InteractionResult.SUCCESS
            } else {
                if (!level.isClientSide) {
                    player.sendSystemMessage(
                        Component.translatable("message.industry-made.crucible_full")
                    )
                }
                return InteractionResult.CONSUME
            }
        }

        // 3. Water bucket rejection with informative feedback
        if (stack.`is`(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                val serverLevel = level as? ServerLevel
                serverLevel?.sendParticles(
                    ParticleTypes.SMOKE,
                    pos.x + 0.5, pos.y + 0.6, pos.z + 0.5,
                    8,
                    0.2, 0.1, 0.2,
                    0.02
                )
                level.playSound(
                    null,
                    pos,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.7f,
                    1.2f
                )
                player.sendSystemMessage(
                    Component.literal("§cO Cadinho funde metais e não aceita água! Para vapor, use a Caldeira.§r")
                )
            }
            return InteractionResult.SUCCESS
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
        val be = level.getBlockEntity(pos) as? CrucibleBlockEntity ?: return InteractionResult.PASS

        // Determine which slot the player is targeting inside the cavity
        val hitX = hitResult.location.x - pos.x
        val hitZ = hitResult.location.z - pos.z
        val targetSlot = (if (hitZ >= 0.5) 2 else 0) + (if (hitX >= 0.5) 1 else 0)

        // Retrieve item: If targeting an occupied slot, or if crouching and any item exists
        val slotToRetrieve = if (!be.items[targetSlot].isEmpty) {
            targetSlot
        } else if (player.isShiftKeyDown) {
            be.items.indexOfLast { !it.isEmpty }
        } else {
            -1
        }

        if (slotToRetrieve != -1) {
            if (!level.isClientSide) {
                val removed = be.items[slotToRetrieve]
                be.items[slotToRetrieve] = ItemStack.EMPTY

                if (!player.inventory.add(removed)) {
                    player.drop(removed, false)
                }

                level.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.5f,
                    1.0f
                )
                be.setChanged()
                level.sendBlockUpdated(pos, state, state, 3)
            }
            return InteractionResult.SUCCESS
        }

        // Standing + empty hand = inspect crucible state
        if (!level.isClientSide) {
            val temp = be.temperature.toInt()
            val tempColor = when {
                temp >= 1000 -> "§c"
                temp >= 600 -> "§6"
                temp >= 200 -> "§e"
                else -> "§7"
            }

            val statusText = if (be.moltenAmount > 0) {
                val metal = MetalRegistry.getMetal(be.moltenMetal)
                val name = if (metal != null) Component.translatable(metal.translationKey).string else be.moltenMetal
                "§6$name (${be.moltenAmount}/4)§r"
            } else {
                val solidCount = be.items.count { !it.isEmpty }
                if (solidCount > 0) {
                    "§e$solidCount itens (Fusão: ${be.meltProgress}%)§r"
                } else {
                    "§7Vazio§r"
                }
            }

            player.sendSystemMessage(
                Component.literal("§6Cadinho§r: $tempColor${temp}°C§r | $statusText")
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
        return CrucibleBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        if (blockEntityType != ModBlockEntities.CRUCIBLE) return null
        return CrucibleBlockEntity.createTicker() as BlockEntityTicker<T>
    }
}
