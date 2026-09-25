package dev.patitow.industrymade.block.entity

import dev.patitow.industrymade.block.BellowsBlock
import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModSounds
import dev.patitow.industrymade.thermal.OxygenReceiver
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.util.Mth
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseFireBlock
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.CampfireBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3

class BellowsBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntities.BELLOWS, pos, state) {

    companion object {
        const val EVENT_PUMP: Int = 1
        const val PUMP_DURATION_TICKS: Int = 12
        const val COMPRESSION_TICKS: Int = 4

        fun serverTick(level: Level, pos: BlockPos, state: BlockState, be: BellowsBlockEntity) {
            if (be.pumpCooldown > 0) {
                be.pumpCooldown--
            }
        }

        fun clientTick(level: Level, pos: BlockPos, state: BlockState, be: BellowsBlockEntity) {
            be.prevProgress = be.progress

            if (be.animating) {
                be.animTicks++
                if (be.animTicks <= COMPRESSION_TICKS) {
                    // Compressing down quickly
                    be.progress = be.animTicks.toFloat() / COMPRESSION_TICKS.toFloat()
                } else if (be.animTicks <= PUMP_DURATION_TICKS) {
                    // Expanding back up smoothly
                    val expandProgress = (be.animTicks - COMPRESSION_TICKS).toFloat() / (PUMP_DURATION_TICKS - COMPRESSION_TICKS).toFloat()
                    be.progress = 1.0f - expandProgress
                } else {
                    // Animation finished
                    be.progress = 0.0f
                    be.animating = false
                    be.animTicks = 0
                }
            } else {
                be.progress = 0.0f
            }
        }
    }

    private var pumpCooldown: Int = 0

    // Client-side animation variables
    var progress: Float = 0.0f
    var prevProgress: Float = 0.0f
    var animating: Boolean = false
    var animTicks: Int = 0

    fun getInterpolatedProgress(partialTicks: Float): Float {
        return Mth.lerp(partialTicks, prevProgress, progress)
    }

    fun pump(): Boolean {
        val level = this.level ?: return false
        if (pumpCooldown > 0) return false

        pumpCooldown = PUMP_DURATION_TICKS

        if (!level.isClientSide) {
            // Trigger animation on all watching clients via vanilla block event packet
            level.blockEvent(blockPos, blockState.block, EVENT_PUMP, 0)

            // Play sound
            level.playSound(
                null,
                blockPos,
                ModSounds.BELLOWS_BLOW,
                SoundSource.BLOCKS,
                0.9f,
                0.9f + level.random.nextFloat() * 0.2f
            )

            val facing = blockState.getValue(BellowsBlock.FACING)
            val serverLevel = level as? ServerLevel

            if (serverLevel != null) {
                // Shoot air blast particles from front nozzle
                val nozzlePos = Vec3.atCenterOf(blockPos).add(
                    facing.stepX * 0.55,
                    0.0,
                    facing.stepZ * 0.55
                )

                serverLevel.sendParticles(
                    ParticleTypes.SMALL_GUST,
                    nozzlePos.x, nozzlePos.y, nozzlePos.z,
                    2,
                    facing.stepX * 0.1, 0.05, facing.stepZ * 0.1,
                    0.15
                )

                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    nozzlePos.x, nozzlePos.y, nozzlePos.z,
                    4,
                    facing.stepX * 0.15, 0.05, facing.stepZ * 0.15,
                    0.08
                )
            }

            // Interact with the targeted adjacent block
            val targetPos = blockPos.relative(facing)
            val targetState = level.getBlockState(targetPos)
            val targetBe = level.getBlockEntity(targetPos)

            // 1. Custom OxygenReceiver (future Crucible / Industrial Forges)
            if (targetBe is OxygenReceiver) {
                targetBe.receiveAirBlast(level, targetPos, facing.opposite, 1.0f)
            }

            // 2. Vanilla Furnace boost
            if (targetBe is AbstractFurnaceBlockEntity) {
                if (targetBe.litTimeRemaining > 0) {
                    // Accelerate smelting progress by 20 ticks (1 second) per pump!
                    targetBe.cookingTimer = (targetBe.cookingTimer + 20).coerceAtMost(200)
                    serverLevel?.sendParticles(
                        ParticleTypes.SMALL_FLAME,
                        targetPos.x + 0.5, targetPos.y + 0.5, targetPos.z + 0.5,
                        6,
                        0.2, 0.2, 0.2,
                        0.02
                    )
                }
            }
            // 3. Vanilla Campfire boost
            else if (targetBe is CampfireBlockEntity) {
                for (i in targetBe.cookingProgress.indices) {
                    if (targetBe.cookingProgress[i] > 0) {
                        targetBe.cookingProgress[i] = (targetBe.cookingProgress[i] + 25).coerceAtMost(600)
                    }
                }
                serverLevel?.sendParticles(
                    ParticleTypes.FLAME,
                    targetPos.x + 0.5, targetPos.y + 0.3, targetPos.z + 0.5,
                    5,
                    0.2, 0.1, 0.2,
                    0.04
                )
            }
            // 4. Vanilla Fire block crackle
            else if (targetState.block is BaseFireBlock) {
                serverLevel?.sendParticles(
                    ParticleTypes.FLAME,
                    targetPos.x + 0.5, targetPos.y + 0.2, targetPos.z + 0.5,
                    8,
                    0.25, 0.2, 0.25,
                    0.05
                )
            }
        } else {
            // Client triggered directly (e.g. immediate feedback)
            startPumpAnimation()
        }

        return true
    }

    private fun startPumpAnimation() {
        animating = true
        animTicks = 0
    }

    override fun triggerEvent(id: Int, param: Int): Boolean {
        if (id == EVENT_PUMP) {
            startPumpAnimation()
            // Client sound
            val level = this.level
            if (level != null && level.isClientSide) {
                level.playLocalSound(
                    blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5,
                    ModSounds.BELLOWS_BLOW,
                    SoundSource.BLOCKS,
                    0.8f,
                    0.95f + level.random.nextFloat() * 0.1f,
                    false
                )
            }
            return true
        }
        return super.triggerEvent(id, param)
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putInt("Cooldown", pumpCooldown)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        pumpCooldown = input.getIntOr("Cooldown", 0)
    }
}
