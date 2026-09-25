package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.BronzeGaugePipeBlock
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
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB

class BronzeGaugePipeBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.BRONZE_GAUGE_PIPE, pos, state),
    SteamProvider {

    companion object {
        const val MAX_PRESSURE: Double = 6.0

        fun createTicker(): BlockEntityTicker<BronzeGaugePipeBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.serverTick(level, pos, state)
            }
        }
    }

    override var steamPressure: Double = 0.0
    private var leakSoundCooldown: Int = 0

    fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        if (level.isClientSide) return
        val serverLevel = level as ServerLevel

        if (leakSoundCooldown > 0) {
            leakSoundCooldown--
        }

        // 1. Equalize steam with adjacent providers
        for (dir in Direction.entries) {
            val neighborBe = level.getBlockEntity(pos.relative(dir))
            if (neighborBe is LowPressureBoilerBlockEntity) {
                if (neighborBe.steamPressure > this.steamPressure) {
                    val flow = (neighborBe.steamPressure - this.steamPressure) * 0.15
                    this.steamPressure += flow
                    neighborBe.steamPressure -= flow
                    neighborBe.setChanged()
                    setChanged()
                }
            } else if (neighborBe is SteamProvider) {
                if (this.steamPressure > neighborBe.getAvailablePressure()) {
                    val flow = (this.steamPressure - neighborBe.getAvailablePressure()) * 0.25
                    this.steamPressure -= flow
                    neighborBe.steamPressure += flow
                    (neighborBe as? BlockEntity)?.setChanged()
                    setChanged()
                }
            }
        }

        // 2. Update visual gauge pressure level on blockstate (0, 1, 2, 3)
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

        // 3. Overpressure safety leak
        if (steamPressure > MAX_PRESSURE) {
            steamPressure = (steamPressure - 0.05).coerceAtLeast(MAX_PRESSURE)
            setChanged()

            if (leakSoundCooldown <= 0) {
                leakSoundCooldown = 25
                level.playSound(
                    null,
                    pos,
                    ModSounds.STEAM_HISS,
                    SoundSource.BLOCKS,
                    0.8f,
                    1.2f + level.random.nextFloat() * 0.3f
                )

                serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    pos.x + 0.5, pos.y + 0.9, pos.z + 0.5,
                    5,
                    0.15, 0.15, 0.15,
                    0.03
                )

                val burnArea = AABB(pos).inflate(0.5)
                val nearby = level.getEntitiesOfClass(LivingEntity::class.java, burnArea)
                for (entity in nearby) {
                    entity.hurtServer(serverLevel, level.damageSources().inFire(), 1.0f)
                }
            }
        }

        // 4. Natural condensation
        if (steamPressure > 0.0) {
            steamPressure = (steamPressure - 0.0005).coerceAtLeast(0.0)
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putDouble("Pressure", steamPressure)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        steamPressure = input.getDoubleOr("Pressure", 0.0)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }
}
