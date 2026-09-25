package dev.patitow.industrymade.item

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.ToolMaterial
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.block.Block
import java.util.function.Consumer

open class DescriptiveItem(
    properties: Properties,
    private val tooltipKey: String? = null
) : Item(properties) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag)
        val key = tooltipKey ?: "tooltip.industry-made.${descriptionId.substringAfterLast('.')}"
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY))
    }
}

open class DescriptiveBlockItem(
    block: Block,
    properties: Properties,
    private val tooltipKey: String? = null
) : BlockItem(block, properties) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag)
        val key = tooltipKey ?: "tooltip.industry-made.${block.descriptionId.substringAfterLast('.')}"
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY))
    }
}

class DescriptiveShovelItem(
    material: ToolMaterial,
    attackDamage: Float,
    attackSpeed: Float,
    properties: Properties,
    private val tooltipKey: String? = null
) : ShovelItem(material, attackDamage, attackSpeed, properties) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag)
        val key = tooltipKey ?: "tooltip.industry-made.${descriptionId.substringAfterLast('.')}"
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY))
    }
}

class DescriptiveAxeItem(
    material: ToolMaterial,
    attackDamage: Float,
    attackSpeed: Float,
    properties: Properties,
    private val tooltipKey: String? = null
) : AxeItem(material, attackDamage, attackSpeed, properties) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag)
        val key = tooltipKey ?: "tooltip.industry-made.${descriptionId.substringAfterLast('.')}"
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY))
    }
}

class DescriptiveHoeItem(
    material: ToolMaterial,
    attackDamage: Float,
    attackSpeed: Float,
    properties: Properties,
    private val tooltipKey: String? = null
) : HoeItem(material, attackDamage, attackSpeed, properties) {

    override fun appendHoverText(
        itemStack: ItemStack,
        context: TooltipContext,
        display: TooltipDisplay,
        builder: Consumer<Component>,
        tooltipFlag: TooltipFlag
    ) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag)
        val key = tooltipKey ?: "tooltip.industry-made.${descriptionId.substringAfterLast('.')}"
        builder.accept(Component.translatable(key).withStyle(ChatFormatting.GRAY))
    }
}
