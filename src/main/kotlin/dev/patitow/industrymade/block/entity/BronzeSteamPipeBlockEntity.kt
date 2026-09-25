package dev.patitow.industrymade.block.entity

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

class BronzeSteamPipeBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.BRONZE_STEAM_PIPE, pos, state),
    SteamProvider {

    companion object {
        const val MAX_PRESSURE: Double = 6.0 // 6.0 bar max for bronze pipes

        fun createTicker(): BlockEntityTicker<BronzeSteamPipeBlockEntity> {
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

        // 1. Equalize / pull steam with adjacent providers (Boilers & other Pipes)
        for (dir in Direction.entries) {
            val neighborBe = level.getBlockEntity(pos.relative(dir))
            if (neighborBe is LowPressureBoilerBlockEntity) {
                // Pull steam from boiler if boiler has higher pressure
                if (neighborBe.steamPressure > this.steamPressure) {
                    val flow = (neighborBe.steamPressure - this.steamPressure) * 0.15
                    this.steamPressure += flow
                    neighborBe.steamPressure -= flow
                    neighborBe.setChanged()
                    setChanged()
                }
            } else if (neighborBe is BronzeSteamPipeBlockEntity) {
                // Equalize between connected pipes
                if (this.steamPressure > neighborBe.steamPressure) {
                    val flow = (this.steamPressure - neighborBe.steamPressure) * 0.25
                    this.steamPressure -= flow
                    neighborBe.steamPressure += flow
                    neighborBe.setChanged()
                    setChanged()
                }
            }
        }

        // 2. Overpressure safety leak (Safe failure philosophy: No cratering explosions!)
        if (steamPressure > MAX_PRESSURE) {
            val excess = steamPressure - MAX_PRESSURE
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
                    pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                    5,
                    0.15, 0.15, 0.15,
                    0.03
                )

                // Minor steam burn hazard for careless engineers standing too close
                val burnArea = AABB(pos).inflate(0.5)
                val nearby = level.getEntitiesOfClass(LivingEntity::class.java, burnArea)
                for (entity in nearby) {
                    entity.hurtServer(serverLevel, level.damageSources().inFire(), 1.0f)
                }
            }
        }

        // 3. Very slight natural condensation / friction loss when idle
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
