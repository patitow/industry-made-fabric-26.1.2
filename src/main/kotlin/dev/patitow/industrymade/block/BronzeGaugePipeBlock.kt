package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.BronzeGaugePipeBlockEntity
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.phys.BlockHitResult
import java.util.Locale

class BronzeGaugePipeBlock(properties: Properties) : BronzeSteamPipeBlock(properties) {

    companion object {
        val CODEC: MapCodec<BronzeGaugePipeBlock> = simpleCodec(::BronzeGaugePipeBlock)
        val PRESSURE_LEVEL: IntegerProperty = IntegerProperty.create("pressure_level", 0, 3)
    }

    init {
        registerDefaultState(
            defaultBlockState().setValue(PRESSURE_LEVEL, 0)
        )
    }

    override fun codec(): MapCodec<out BronzeSteamPipeBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(PRESSURE_LEVEL)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return super.getStateForPlacement(context).setValue(PRESSURE_LEVEL, 0)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val be = level.getBlockEntity(pos) as? BronzeGaugePipeBlockEntity ?: return InteractionResult.PASS

        if (!level.isClientSide) {
            val pressureStr = String.format(Locale.US, "%.1f", be.steamPressure)
            val color = when {
                be.steamPressure >= 5.5 -> "§c"
                be.steamPressure >= 2.5 -> "§a"
                be.steamPressure >= 0.5 -> "§e"
                else -> "§7"
            }
            player.sendSystemMessage(
                Component.literal("§6Manômetro de Tubulação§r: $color${pressureStr} / 6.0 bar§r")
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
        return BronzeGaugePipeBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        if (blockEntityType != ModBlockEntities.BRONZE_GAUGE_PIPE) return null
        return BronzeGaugePipeBlockEntity.createTicker() as BlockEntityTicker<T>
    }
}
