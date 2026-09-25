package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.SteamPistonBlock
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModSounds
import dev.patitow.industrymade.thermal.SteamProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class SteamPistonBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.STEAM_PISTON, pos, state) {

    companion object {
        const val CYCLE_TICKS: Int = 20 // 1 second full stroke cycle
        const val EVENT_STROKE: Int = 1

        fun createTicker(): BlockEntityTicker<SteamPistonBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.serverTick(level, pos, state)
            }
        }
    }

    var active: Boolean = false
    var animTicks: Int = 0
    var prevAnimTicks: Int = 0

    fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        val serverLevel = level as ServerLevel

        // Check for adjacent steam provider (boiler or steam pipe)
        var sourceProvider: SteamProvider? = null
        for (dir in Direction.entries) {
            val neighborBe = level.getBlockEntity(pos.relative(dir))
            if (neighborBe is SteamProvider && neighborBe.getAvailablePressure() >= 1.5) {
                sourceProvider = neighborBe
                break
            }
        }

        if (sourceProvider != null) {
            // Draw working steam
            sourceProvider.drawPressure(0.002)
            active = true

            animTicks = (animTicks + 1) % CYCLE_TICKS

            // Stroke sound and steam puff
            if (animTicks == 0) {
                level.playSound(
                    null,
                    pos,
                    ModSounds.PISTON_CHUG,
                    SoundSource.BLOCKS,
                    0.8f,
                    1.0f
                )

                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    pos.x + 0.5, pos.y + 1.1, pos.z + 0.5,
                    2,
                    0.05, 0.1, 0.05,
                    0.02
                )
            }

            if (!state.getValue(SteamPistonBlock.POWERED)) {
                level.setBlock(pos, state.setValue(SteamPistonBlock.POWERED, true), 3)
            }
        } else {
            active = false
            if (state.getValue(SteamPistonBlock.POWERED)) {
                level.setBlock(pos, state.setValue(SteamPistonBlock.POWERED, false), 3)
            }
        }
    }

    fun clientTick(level: Level, pos: BlockPos, state: BlockState) {
        prevAnimTicks = animTicks
        if (state.getValue(SteamPistonBlock.POWERED)) {
            animTicks = (animTicks + 1) % CYCLE_TICKS
        } else {
            animTicks = 0
        }
    }

    fun getInterpolatedStroke(partialTicks: Float): Float {
        val current = prevAnimTicks.toFloat() + (animTicks - prevAnimTicks).toFloat() * partialTicks
        val angle = (current / CYCLE_TICKS.toFloat()) * (Math.PI.toFloat() * 2.0f)
        return (kotlin.math.sin(angle) + 1.0f) * 0.5f
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putBoolean("Active", active)
        output.putInt("AnimTicks", animTicks)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        active = input.getBooleanOr("Active", false)
        animTicks = input.getIntOr("AnimTicks", 0)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }
}
