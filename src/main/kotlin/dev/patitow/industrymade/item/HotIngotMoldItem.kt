package dev.patitow.industrymade.item

import dev.patitow.industrymade.init.ModDataComponents
import dev.patitow.industrymade.init.ModItems
import dev.patitow.industrymade.thermal.MetalRegistry
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LayeredCauldronBlock

class HotIngotMoldItem(properties: Properties) : Item(properties) {

    companion object {
        const val DEFAULT_COOLING_TICKS = 240 // 12 seconds
    }

    override fun inventoryTick(itemStack: ItemStack, level: ServerLevel, owner: Entity, slot: net.minecraft.world.entity.EquipmentSlot?) {
        val player = owner as? Player ?: return

        val ticks = itemStack.get(ModDataComponents.COOLING_TICKS) ?: DEFAULT_COOLING_TICKS
        if (ticks > 0) {
            itemStack.set(ModDataComponents.COOLING_TICKS, ticks - 1)
        } else {
            // Cool down completely
            quenchMold(itemStack, level, player)
        }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val hitResult = getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY)
        if (hitResult.type == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            val pos = hitResult.blockPos
            val state = level.getBlockState(pos)
            if (state.`is`(Blocks.WATER)) {
                if (!level.isClientSide) {
                    val serverLevel = level as? ServerLevel
                    serverLevel?.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.x + 0.5, pos.y + 0.8, pos.z + 0.5,
                        15,
                        0.2, 0.2, 0.2,
                        0.05
                    )
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.BLOCKS,
                        0.8f,
                        1.2f
                    )
                    quenchMold(player.getItemInHand(hand), level, player)
                }
                return InteractionResult.SUCCESS
            }
        }
        return super.use(level, player, hand)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos = context.clickedPos
        val player = context.player ?: return InteractionResult.PASS
        val state = level.getBlockState(pos)

        // Dipping into water or water cauldron
        val isWater = state.`is`(Blocks.WATER) ||
                (state.block is LayeredCauldronBlock && state.getValue(LayeredCauldronBlock.LEVEL) > 0)

        if (isWater) {
            if (!level.isClientSide) {
                val serverLevel = level as? ServerLevel
                serverLevel?.sendParticles(
                    ParticleTypes.SMOKE,
                    pos.x + 0.5, pos.y + 0.8, pos.z + 0.5,
                    15,
                    0.2, 0.2, 0.2,
                    0.05
                )
                level.playSound(
                    null,
                    pos,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    0.8f,
                    1.2f
                )
                quenchMold(context.itemInHand, level, player)
            }
            return InteractionResult.SUCCESS
        }

        return super.useOn(context)
    }

    private fun quenchMold(stack: ItemStack, level: Level, player: Player) {
        val metalId = stack.get(ModDataComponents.MOLTEN_METAL) ?: "bronze"
        val metal = MetalRegistry.getMetal(metalId)
        val ingotItem = metal?.resultIngot?.get() ?: ModItems.BRONZE_INGOT

        // 1. Consume 1 hot mold
        stack.shrink(1)

        // 2. Award the cooled ingot
        val ingotStack = ItemStack(ingotItem, 1)
        if (!player.inventory.add(ingotStack)) {
            player.drop(ingotStack, false)
        }

        // 3. Return the reusable ceramic mold
        val moldStack = ItemStack(ModItems.CERAMIC_MOLD, 1)
        if (!player.inventory.add(moldStack)) {
            player.drop(moldStack, false)
        }

        player.sendSystemMessage(
            Component.literal("§aMetal temperado com sucesso! Lingote obtido.§r")
        )

        level.playSound(
            null,
            player.blockPosition(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            0.5f,
            1.0f
        )
    }

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: net.minecraft.world.item.component.TooltipDisplay,
        builder: java.util.function.Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val metalId = itemStack.get(ModDataComponents.MOLTEN_METAL) ?: "bronze"
        val metal = MetalRegistry.getMetal(metalId)
        val metalName = if (metal != null) Component.translatable(metal.translationKey) else Component.literal(metalId)

        builder.accept(Component.translatable("tooltip.industry-made.hot_mold_metal", metalName))
        builder.accept(Component.translatable("tooltip.industry-made.hot_mold_quench_hint"))
    }
}
