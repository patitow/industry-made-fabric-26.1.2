package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.BronzeGaugePipeBlock
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState

class BronzeGaugePipeBlockEntity(pos: BlockPos, state: BlockState) :
    BronzeSteamPipeBlockEntity(ModBlockEntities.BRONZE_GAUGE_PIPE, pos, state) {

    companion object {
        fun createTicker(): BlockEntityTicker<BronzeGaugePipeBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.serverTick(level, pos, state)
            }
        }
    }

    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        if (level.isClientSide) return
        super.serverTick(level, pos, state)

        // Update visual gauge pressure level on blockstate (0, 1, 2, 3)
        val desiredLevel = when {
            steamPressure >= 5.5 -> 3
            steamPressure >= 2.5 -> 2
            steamPressure >= 0.5 -> 1
            else -> 0
        }

        val currentLevel = state.getValue(BronzeGaugePipeBlock.PRESSURE_LEVEL)
        if (currentLevel != desiredLevel) {
            level.setBlock(pos, state.setValue(BronzeGaugePipeBlock.PRESSURE_LEVEL, desiredLevel), 3)
        }
    }
}

