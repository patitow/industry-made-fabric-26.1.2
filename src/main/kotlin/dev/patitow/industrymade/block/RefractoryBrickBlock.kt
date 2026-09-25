package dev.patitow.industrymade.block

import dev.patitow.industrymade.thermal.ThermalInsulator
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class RefractoryBrickBlock(properties: Properties) : Block(properties), ThermalInsulator {
    override val insulationFactor: Float = 0.85f

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        if (!level.isClientSide) {
            player.sendSystemMessage(
                Component.literal("§6Tijolos Refratários§r: §eIsolação Térmica (+50°C por bloco ao redor da fornalha)§r")
            )
            level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.6f, 1.2f)
        }
        return InteractionResult.SUCCESS
    }
}

