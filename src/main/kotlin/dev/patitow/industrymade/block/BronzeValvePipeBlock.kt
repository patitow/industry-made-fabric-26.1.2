package dev.patitow.industrymade.block

import com.mojang.serialization.MapCodec
import dev.patitow.industrymade.block.entity.BronzeValvePipeBlockEntity
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.BlockHitResult

class BronzeValvePipeBlock(properties: Properties) : BronzeSteamPipeBlock(properties) {

    companion object {
        val CODEC: MapCodec<BronzeValvePipeBlock> = simpleCodec(::BronzeValvePipeBlock)
        val OPEN: BooleanProperty = BlockStateProperties.OPEN
    }

    init {
        registerDefaultState(
            defaultBlockState().setValue(OPEN, true)
        )
    }

    override fun codec(): MapCodec<out BronzeSteamPipeBlock> = CODEC

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(OPEN)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState {
        return super.getStateForPlacement(context).setValue(OPEN, true)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        val newOpen = !state.getValue(OPEN)
        level.setBlock(pos, state.setValue(OPEN, newOpen), 3)

        if (!level.isClientSide) {
            val sound = if (newOpen) SoundEvents.IRON_TRAPDOOR_OPEN else SoundEvents.IRON_TRAPDOOR_CLOSE
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.8f, 1.2f)
            val statusMsg = if (newOpen) "§aAberta (Fluxo liberado)§r" else "§cFechada (Fluxo bloqueado)§r"
            player.sendSystemMessage(Component.literal("§6Válvula de Bronze§r: $statusMsg"))
        }

        return InteractionResult.SUCCESS
    }

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
        return BronzeValvePipeBlockEntity(pos, state)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) return null
        if (blockEntityType != ModBlockEntities.BRONZE_VALVE_PIPE) return null
        return BronzeValvePipeBlockEntity.createTicker() as BlockEntityTicker<T>
    }
}
