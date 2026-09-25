package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.MechanicalHammerBlock
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModItems
import dev.patitow.industrymade.init.ModSounds
import dev.patitow.industrymade.thermal.SteamProvider
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class MechanicalHammerBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(ModBlockEntities.MECHANICAL_HAMMER, pos, state),
    Container {

    companion object {
        const val CYCLE_TICKS: Int = 40
        const val STRIKE_TICK: Int = 30
        const val REQUIRED_STRIKES: Int = 4

        fun isValidHammerInput(stack: ItemStack): Boolean {
            if (stack.isEmpty) return false
            return when (stack.item) {
                ModItems.BRONZE_INGOT,
                Items.IRON_INGOT,
                ModItems.WROUGHT_IRON_INGOT,
                Items.RAW_COPPER,
                ModItems.RAW_TIN,
                Items.RAW_IRON -> true
                else -> false
            }
        }

        fun getHammerResult(input: ItemStack): ItemStack? {
            if (input.isEmpty) return null
            return when (input.item) {
                ModItems.BRONZE_INGOT -> ItemStack(ModItems.BRONZE_PLATE)
                Items.IRON_INGOT -> ItemStack(ModItems.WROUGHT_IRON_INGOT)
                ModItems.WROUGHT_IRON_INGOT -> ItemStack(ModItems.IRON_PLATE)
                Items.RAW_COPPER -> ItemStack(Items.COPPER_INGOT, 2)
                ModItems.RAW_TIN -> ItemStack(ModItems.TIN_INGOT, 2)
                Items.RAW_IRON -> ItemStack(Items.IRON_INGOT, 2)
                else -> null
            }
        }

        fun createTicker(): BlockEntityTicker<MechanicalHammerBlockEntity> {
            return BlockEntityTicker { level, pos, state, blockEntity ->
                blockEntity.serverTick(level, pos, state)
            }
        }
    }

    var itemOnBed: ItemStack = ItemStack.EMPTY
    var strikes: Int = 0
    var animTicks: Int = 0
    var prevAnimTicks: Int = 0
    var active: Boolean = false

    fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        val serverLevel = level as? ServerLevel ?: return
        prevAnimTicks = animTicks

        // 1. Detect power source: Adjacent steam provider (boiler or pipe), steam piston, or redstone signal
        var hasPower = false
        var sourceProvider: SteamProvider? = null

        for (dir in Direction.entries) {
            val neighborBe = level.getBlockEntity(pos.relative(dir))
            if (neighborBe is SteamProvider && neighborBe.getAvailablePressure() >= 1.5) {
                sourceProvider = neighborBe
                hasPower = true
                break
            } else if (neighborBe is SteamPistonBlockEntity && neighborBe.active) {
                hasPower = true
                break
            }
        }

        if (!hasPower && level.hasNeighborSignal(pos)) {
            hasPower = true
        }

        // Draw small steam pressure when operating
        if (sourceProvider != null) {
            sourceProvider.drawPressure(0.002)
        }

        val wasPowered = state.getValue(MechanicalHammerBlock.POWERED)
        if (hasPower != wasPowered) {
            level.setBlock(pos, state.setValue(MechanicalHammerBlock.POWERED, hasPower), 3)
        }

        active = hasPower

        if (hasPower || animTicks != 0) {
            animTicks = (animTicks + 1) % CYCLE_TICKS

            // Strike frame at STRIKE_TICK
            if (animTicks == STRIKE_TICK) {
                level.playSound(
                    null,
                    pos,
                    ModSounds.HAMMER_SLAM,
                    SoundSource.BLOCKS,
                    1.2f,
                    0.9f + level.random.nextFloat() * 0.2f
                )

                // Mechanical impact particles
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    pos.x + 0.5, pos.y + 0.5, pos.z + 0.5,
                    4,
                    0.1, 0.05, 0.1,
                    0.02
                )

                if (!itemOnBed.isEmpty) {
                    val result = getHammerResult(itemOnBed)
                    if (result != null) {
                        strikes++

                        serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            pos.x + 0.5, pos.y + 0.55, pos.z + 0.5,
                            12,
                            0.15, 0.05, 0.15,
                            0.1
                        )
                        serverLevel.sendParticles(
                            ParticleTypes.LAVA,
                            pos.x + 0.5, pos.y + 0.55, pos.z + 0.5,
                            3,
                            0.1, 0.05, 0.1,
                            0.0
                        )

                        if (strikes >= REQUIRED_STRIKES) {
                            itemOnBed = result.copy()
                            strikes = 0
                            serverLevel.sendParticles(
                                ParticleTypes.HAPPY_VILLAGER,
                                pos.x + 0.5, pos.y + 0.7, pos.z + 0.5,
                                5,
                                0.2, 0.1, 0.2,
                                0.0
                            )
                        }

                        setChanged()
                        serverLevel.sendBlockUpdated(pos, state, state, 3)
                    }
                }
            }

            setChanged()
        }
    }

    // Container implementation (1 slot) for hopper / automation support
    override fun getContainerSize(): Int = 1

    override fun isEmpty(): Boolean = itemOnBed.isEmpty

    override fun getItem(slot: Int): ItemStack = if (slot == 0) itemOnBed else ItemStack.EMPTY

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        if (slot == 0 && !itemOnBed.isEmpty) {
            val split = itemOnBed.split(amount)
            if (itemOnBed.isEmpty) {
                strikes = 0
            }
            setChanged()
            return split
        }
        return ItemStack.EMPTY
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        if (slot == 0 && !itemOnBed.isEmpty) {
            val stack = itemOnBed
            itemOnBed = ItemStack.EMPTY
            strikes = 0
            return stack
        }
        return ItemStack.EMPTY
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        if (slot == 0) {
            itemOnBed = stack
            strikes = 0
            setChanged()
        }
    }

    override fun stillValid(player: Player): Boolean = Container.stillValidBlockEntity(this, player)

    override fun clearContent() {
        itemOnBed = ItemStack.EMPTY
        strikes = 0
        setChanged()
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        if (!itemOnBed.isEmpty) {
            val list = NonNullList.withSize(1, itemOnBed)
            ContainerHelper.saveAllItems(output, list)
        }
        output.putInt("Strikes", strikes)
        output.putInt("AnimTicks", animTicks)
        output.putBoolean("Active", active)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        val list = NonNullList.withSize(1, ItemStack.EMPTY)
        ContainerHelper.loadAllItems(input, list)
        itemOnBed = list[0]
        strikes = input.getIntOr("Strikes", 0)
        animTicks = input.getIntOr("AnimTicks", 0)
        active = input.getBooleanOr("Active", false)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }
}
