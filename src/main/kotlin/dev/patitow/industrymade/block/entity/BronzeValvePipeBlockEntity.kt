package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.BronzeValvePipeBlock
import dev.patitow.industrymade.init.ModBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState

class BronzeValvePipeBlockEntity(pos: BlockPos, state: BlockState) :
    BronzeSteamPipeBlockEntity(ModBlockEntities.BRONZE_VALVE_PIPE, pos, state) {

    companion object {
        fun createTicker(): BlockEntityTicker<BronzeValvePipeBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.serverTick(level, pos, state)
            }
        }
    }

    fun isOpen(): Boolean {
        return blockState.getValue(BronzeValvePipeBlock.OPEN)
    }

    override fun getAvailablePressure(): Double {
        return if (isOpen()) steamPressure else 0.0
    }

    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        if (level.isClientSide) return
        val serverLevel = level as ServerLevel

        if (isOpen()) {
            super.serverTick(level, pos, state)
        } else {
            handleOverpressureAndCondensation(serverLevel, pos)
        }
    }
}

