package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.LowPressureBoilerBlock
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModSounds
import dev.patitow.industrymade.thermal.OxygenReceiver
import dev.patitow.industrymade.thermal.TemperatureHelper
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
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB

class LowPressureBoilerBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.LOW_PRESSURE_BOILER, pos, state),
    OxygenReceiver {

    companion object {
        const val MAX_WATER: Int = 4000 // 4 buckets (4000 mB)
        const val MAX_PRESSURE: Double = 8.0 // 8.0 bar max before heavy relief
        const val RELIEF_THRESHOLD: Double = 6.0 // Safety valve triggers at 6.0 bar
    }

    var waterAmount: Int = 0
    var steamPressure: Double = 0.0
    var temperature: Double = TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
    var targetTemperature: Double = TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
    var oxygenBoostTicks: Int = 0
    var safetyWhistleCooldown: Int = 0

    override fun receiveAirBlast(level: Level, pos: BlockPos, fromDirection: Direction, intensity: Float) {
        oxygenBoostTicks = (oxygenBoostTicks + (50 * intensity).toInt()).coerceAtMost(250)
        val serverLevel = level as? ServerLevel ?: return
        serverLevel.sendParticles(
            ParticleTypes.FLAME,
            pos.x + 0.5, pos.y + 0.2, pos.z + 0.5,
            5,
            0.2, 0.1, 0.2,
            0.02
        )
    }

    fun tick(level: Level, pos: BlockPos, state: BlockState) {
        if (level.isClientSide) return
        val serverLevel = level as ServerLevel

        if (safetyWhistleCooldown > 0) {
            safetyWhistleCooldown--
        }

        // 1. Detect heat source underneath
        val belowPos = pos.below()
        val belowState = level.getBlockState(belowPos)

        val baseHeat = when {
            belowState.`is`(Blocks.FIRE) -> 350.0
            belowState.`is`(Blocks.SOUL_FIRE) -> 500.0
            belowState.`is`(Blocks.CAMPFIRE) && belowState.getValue(CampfireBlock.LIT) -> 280.0
            belowState.`is`(Blocks.SOUL_CAMPFIRE) && belowState.getValue(CampfireBlock.LIT) -> 450.0
            belowState.`is`(Blocks.LAVA) -> 600.0
            else -> TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
        }

        var insulationBonus = 0.0
        if (baseHeat > TemperatureHelper.ROOM_TEMPERATURE_CELSIUS) {
            for (dir in Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(belowPos.relative(dir)).`is`(ModBlocks.REFRACTORY_BRICKS)) {
                    insulationBonus += 30.0
                }
            }
        }

        val oxygenBonus = if (oxygenBoostTicks > 0) {
            oxygenBoostTicks--
            300.0
        } else {
            0.0
        }

        targetTemperature = if (baseHeat > TemperatureHelper.ROOM_TEMPERATURE_CELSIUS) {
            baseHeat + insulationBonus + oxygenBonus
        } else {
            TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
        }

        // 2. Smooth temperature adjustment
        if (temperature < targetTemperature) {
            val delta = (targetTemperature - temperature) * 0.02 + 0.15
            temperature = (temperature + delta).coerceAtMost(targetTemperature)
        } else if (temperature > targetTemperature) {
            val delta = (temperature - targetTemperature) * 0.01 + 0.1
            temperature = (temperature - delta).coerceAtLeast(targetTemperature)
        }

        // 3. Steam generation
        val isBoiling = temperature >= 100.0 && waterAmount > 0
        if (isBoiling) {
            // Water consumption
            if (level.random.nextInt(6) == 0) {
                waterAmount = (waterAmount - 1).coerceAtLeast(0)
            }

            // Steam pressure accumulation
            val pressureGain = ((temperature - 90.0) / 250.0) * 0.005
            steamPressure = (steamPressure + pressureGain).coerceAtMost(MAX_PRESSURE)
        } else {
            // Passive steam condensation / cooling
            if (temperature < 100.0 && steamPressure > 0.0) {
                steamPressure = (steamPressure - 0.005).coerceAtLeast(0.0)
            }
        }

        // 4. Safety Relief Valve (Philosophy: No Cratering Explosions, Interactive Thermal Hazard!)
        if (steamPressure >= RELIEF_THRESHOLD) {
            if (safetyWhistleCooldown <= 0) {
                safetyWhistleCooldown = 25 // Repeat whistle every 1.25 seconds

                // Audio cue: Loud piercing steam whistle & hiss
                level.playSound(
                    null,
                    pos,
                    ModSounds.STEAM_WHISTLE,
                    SoundSource.BLOCKS,
                    1.0f,
                    1.0f
                )
                level.playSound(
                    null,
                    pos,
                    ModSounds.STEAM_HISS,
                    SoundSource.BLOCKS,
                    0.8f,
                    1.2f
                )

                // Visual cue: High-pressure jet of steam blasting upward from safety nozzle
                val topX = pos.x + 0.5
                val topY = pos.y + 1.05
                val topZ = pos.z + 0.5

                serverLevel.sendParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    topX, topY, topZ,
                    12,
                    0.1, 0.35, 0.1,
                    0.08
                )
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    topX, topY, topZ,
                    8,
                    0.15, 0.25, 0.15,
                    0.1
                )

                // Proximity heat damage to careless players or mobs directly above the safety nozzle
                val dangerZone = AABB(topX - 0.75, topY - 0.2, topZ - 0.75, topX + 0.75, topY + 1.5, topZ + 0.75)
                val victims = level.getEntitiesOfClass(LivingEntity::class.java, dangerZone)
                for (victim in victims) {
                    victim.hurt(serverLevel.damageSources().hotFloor(), 2.5f)
                    victim.setRemainingFireTicks(40)
                }

                // Relieve excess pressure safely
                steamPressure = (steamPressure - 0.6).coerceAtLeast(0.0)
            }
        }

        // 5. Update BlockState (LIT and PRESSURE_LEVEL)
        val desiredPressureLevel = when {
            steamPressure >= 6.0 -> 3
            steamPressure >= 2.5 -> 2
            steamPressure >= 0.5 -> 1
            else -> 0
        }

        val currentLit = state.getValue(LowPressureBoilerBlock.LIT)
        val currentPressure = state.getValue(LowPressureBoilerBlock.PRESSURE_LEVEL)

        if (currentLit != isBoiling || currentPressure != desiredPressureLevel) {
            level.setBlock(
                pos,
                state.setValue(LowPressureBoilerBlock.LIT, isBoiling)
                    .setValue(LowPressureBoilerBlock.PRESSURE_LEVEL, desiredPressureLevel),
                3
            )
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putInt("Water", waterAmount)
        output.putDouble("Pressure", steamPressure)
        output.putDouble("Temperature", temperature)
        output.putDouble("TargetTemperature", targetTemperature)
        output.putInt("OxygenBoost", oxygenBoostTicks)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        waterAmount = input.getIntOr("Water", 0)
        steamPressure = input.getDoubleOr("Pressure", 0.0)
        temperature = input.getDoubleOr("Temperature", TemperatureHelper.ROOM_TEMPERATURE_CELSIUS)
        targetTemperature = input.getDoubleOr("TargetTemperature", TemperatureHelper.ROOM_TEMPERATURE_CELSIUS)
        oxygenBoostTicks = input.getIntOr("OxygenBoost", 0)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    companion object {
        fun createTicker(): BlockEntityTicker<LowPressureBoilerBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.tick(level, pos, state)
            }
        }
    }
}
