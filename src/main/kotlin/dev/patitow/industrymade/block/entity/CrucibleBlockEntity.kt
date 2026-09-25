package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.CrucibleBlock
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.thermal.MetalRegistry
import dev.patitow.industrymade.thermal.OxygenReceiver
import dev.patitow.industrymade.thermal.TemperatureHelper
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.core.NonNullList
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.ContainerHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState

class CrucibleBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.CRUCIBLE, pos, state),
    OxygenReceiver {

    val items: NonNullList<ItemStack> = NonNullList.withSize(4, ItemStack.EMPTY)
    var moltenMetal: String = ""
    var moltenAmount: Int = 0

    var temperature: Double = TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
    var targetTemperature: Double = TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
    var oxygenBoostTicks: Int = 0
    var meltProgress: Int = 0

    override fun receiveAirBlast(level: Level, pos: BlockPos, fromDirection: Direction, intensity: Float) {
        oxygenBoostTicks = (oxygenBoostTicks + (50 * intensity).toInt()).coerceAtMost(250)
        val serverLevel = level as? ServerLevel ?: return
        serverLevel.sendParticles(
            ParticleTypes.FLAME,
            pos.x + 0.5, pos.y + 0.3, pos.z + 0.5,
            6,
            0.2, 0.1, 0.2,
            0.03
        )
    }

    fun tick(level: Level, pos: BlockPos, state: BlockState) {
        if (level.isClientSide) return
        val serverLevel = level as ServerLevel

        // 1. Determine heat source underneath
        val belowPos = pos.below()
        val belowState = level.getBlockState(belowPos)

        val baseHeat = when {
            belowState.`is`(Blocks.FIRE) -> 500.0
            belowState.`is`(Blocks.SOUL_FIRE) -> 700.0
            belowState.`is`(Blocks.CAMPFIRE) && belowState.getValue(CampfireBlock.LIT) -> 400.0
            belowState.`is`(Blocks.SOUL_CAMPFIRE) && belowState.getValue(CampfireBlock.LIT) -> 600.0
            belowState.`is`(Blocks.LAVA) -> 900.0
            else -> TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
        }

        var insulationBonus = 0.0
        if (baseHeat > TemperatureHelper.ROOM_TEMPERATURE_CELSIUS) {
            // Check refractory brick insulation around fire chamber
            for (dir in Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(belowPos.relative(dir)).`is`(ModBlocks.REFRACTORY_BRICKS)) {
                    insulationBonus += 50.0 // Up to +200°C with 4 bricks surrounding the fire
                }
            }
        }

        val oxygenBonus = if (oxygenBoostTicks > 0) {
            oxygenBoostTicks--
            500.0 // Forced draft raises combustion temperature by +500°C!
        } else {
            0.0
        }

        targetTemperature = if (baseHeat > TemperatureHelper.ROOM_TEMPERATURE_CELSIUS) {
            baseHeat + insulationBonus + oxygenBonus
        } else {
            TemperatureHelper.ROOM_TEMPERATURE_CELSIUS
        }

        // 2. Interpolate current temperature
        if (temperature < targetTemperature) {
            val delta = (targetTemperature - temperature) * 0.03 + 0.2
            temperature = (temperature + delta).coerceAtMost(targetTemperature)
        } else if (temperature > targetTemperature) {
            val delta = (temperature - targetTemperature) * 0.015 + 0.1
            temperature = (temperature - delta).coerceAtLeast(targetTemperature)
        }

        // 3. Update BlockState HEAT_LEVEL
        val currentHeatLevel = state.getValue(CrucibleBlock.HEAT_LEVEL)
        val desiredHeatLevel = when {
            temperature >= 1000.0 -> 3
            temperature >= 600.0 -> 2
            temperature >= 200.0 -> 1
            else -> 0
        }
        if (currentHeatLevel != desiredHeatLevel) {
            level.setBlock(pos, state.setValue(CrucibleBlock.HEAT_LEVEL, desiredHeatLevel), 3)
        }

        // 4. Melting logic
        val solidItems = items.filter { !it.isEmpty }
        if (solidItems.isNotEmpty() && moltenAmount < 4) {
            var copperCount = 0
            var tinCount = 0
            var ironCount = 0
            var bronzeCount = 0

            for (stack in solidItems) {
                val melt = MetalRegistry.getMeltableResult(stack) ?: continue
                when (melt.first.id) {
                    "copper" -> copperCount += melt.second
                    "tin" -> tinCount += melt.second
                    "bronze" -> bronzeCount += melt.second
                    "iron" -> ironCount += melt.second
                }
            }

            // Determine required melting temperature
            val requiredTemp = when {
                copperCount > 0 && tinCount > 0 -> TemperatureHelper.BRONZE_MELTING_POINT // 950°C for bronze alloy!
                ironCount > 0 -> TemperatureHelper.CAST_IRON_MELTING_POINT // 1200°C
                copperCount > 0 -> TemperatureHelper.COPPER_MELTING_POINT // 1085°C
                bronzeCount > 0 -> TemperatureHelper.BRONZE_MELTING_POINT // 950°C
                tinCount > 0 -> TemperatureHelper.TIN_MELTING_POINT // 232°C
                else -> 9999.0
            }

            if (temperature >= requiredTemp) {
                meltProgress++

                // Bubbling effect
                if (meltProgress % 15 == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.LAVA,
                        pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                        2,
                        0.15, 0.05, 0.15,
                        0.02
                    )
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.LAVA_POP,
                        SoundSource.BLOCKS,
                        0.4f,
                        1.2f
                    )
                }

                if (meltProgress >= 80) { // 4 seconds of sustained melting heat
                    meltProgress = 0

                    // Alloy or pure melt conversion
                    if (copperCount > 0 && tinCount > 0) {
                        val bronzeYield = (copperCount + tinCount).coerceAtMost(4 - moltenAmount)
                        moltenMetal = "bronze"
                        moltenAmount += bronzeYield
                    } else if (copperCount > 0) {
                        moltenMetal = "copper"
                        moltenAmount = (moltenAmount + copperCount).coerceAtMost(4)
                    } else if (tinCount > 0) {
                        moltenMetal = "tin"
                        moltenAmount = (moltenAmount + tinCount).coerceAtMost(4)
                    } else if (bronzeCount > 0) {
                        moltenMetal = "bronze"
                        moltenAmount = (moltenAmount + bronzeCount).coerceAtMost(4)
                    } else if (ironCount > 0) {
                        moltenMetal = "iron"
                        moltenAmount = (moltenAmount + ironCount).coerceAtMost(4)
                    }

                    // Clear melted items
                    for (i in items.indices) {
                        items[i] = ItemStack.EMPTY
                    }

                    level.playSound(
                        null,
                        pos,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.6f,
                        0.7f
                    )

                    setChanged()
                    serverLevel.sendBlockUpdated(pos, state, state, 3)
                }
            } else {
                meltProgress = (meltProgress - 1).coerceAtLeast(0)
            }
        } else {
            meltProgress = 0
        }

        // Ambient particles
        if (temperature >= 200.0 && level.random.nextFloat() < 0.15f) {
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                pos.x + 0.5, pos.y + 0.8, pos.z + 0.5,
                1,
                0.1, 0.05, 0.1,
                0.01
            )
        }
    }

    override fun saveAdditional(output: net.minecraft.world.level.storage.ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output, items)
        output.putString("MoltenMetal", moltenMetal)
        output.putInt("MoltenAmount", moltenAmount)
        output.putDouble("Temperature", temperature)
        output.putDouble("TargetTemperature", targetTemperature)
        output.putInt("OxygenBoost", oxygenBoostTicks)
        output.putInt("MeltProgress", meltProgress)
    }

    override fun loadAdditional(input: net.minecraft.world.level.storage.ValueInput) {
        super.loadAdditional(input)
        items.clear()
        ContainerHelper.loadAllItems(input, items)
        moltenMetal = input.getStringOr("MoltenMetal", "")
        moltenAmount = input.getIntOr("MoltenAmount", 0)
        temperature = input.getDoubleOr("Temperature", TemperatureHelper.ROOM_TEMPERATURE_CELSIUS)
        targetTemperature = input.getDoubleOr("TargetTemperature", TemperatureHelper.ROOM_TEMPERATURE_CELSIUS)
        oxygenBoostTicks = input.getIntOr("OxygenBoost", 0)
        meltProgress = input.getIntOr("MeltProgress", 0)
    }

    override fun getUpdateTag(registries: HolderLookup.Provider): CompoundTag {
        return saveCustomOnly(registries)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    companion object {
        fun createTicker(): BlockEntityTicker<CrucibleBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.tick(level, pos, state)
            }
        }
    }
}
