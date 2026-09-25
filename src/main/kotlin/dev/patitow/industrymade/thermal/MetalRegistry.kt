package dev.patitow.industrymade.thermal

import dev.patitow.industrymade.init.ModItems
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.function.Supplier

data class MoltenMetalType(
    val id: String,
    val translationKey: String,
    val meltingPoint: Double,
    val colorRgb: Int,
    val resultIngot: Supplier<Item>
)

object MetalRegistry {

    val COPPER: MoltenMetalType = MoltenMetalType(
        id = "copper",
        translationKey = "metal.industry-made.copper",
        meltingPoint = TemperatureHelper.COPPER_MELTING_POINT, // 1085°C
        colorRgb = 0xEE5522, // Glowing incandescent orange
        resultIngot = { Items.COPPER_INGOT }
    )

    val TIN: MoltenMetalType = MoltenMetalType(
        id = "tin",
        translationKey = "metal.industry-made.tin",
        meltingPoint = TemperatureHelper.TIN_MELTING_POINT, // 232°C
        colorRgb = 0xC0C8D8, // Silvery liquid gray
        resultIngot = { ModItems.TIN_INGOT }
    )

    val BRONZE: MoltenMetalType = MoltenMetalType(
        id = "bronze",
        translationKey = "metal.industry-made.bronze",
        meltingPoint = TemperatureHelper.BRONZE_MELTING_POINT, // 950°C
        colorRgb = 0xDF9B28, // Warm rich bronze amber
        resultIngot = { ModItems.BRONZE_INGOT }
    )

    val IRON: MoltenMetalType = MoltenMetalType(
        id = "iron",
        translationKey = "metal.industry-made.iron",
        meltingPoint = TemperatureHelper.CAST_IRON_MELTING_POINT, // 1200°C
        colorRgb = 0xFFF099, // Bright incandescent white-yellow
        resultIngot = { Items.IRON_INGOT }
    )

    private val METALS = listOf(COPPER, TIN, BRONZE, IRON).associateBy { it.id }

    fun getMetal(id: String): MoltenMetalType? = METALS[id]

    /**
     * Determines what metal an item stack converts into when melted, and the unit yield.
     * Returns a Pair(MoltenMetalType, yield) or null if not meltable.
     */
    fun getMeltableResult(stack: ItemStack): Pair<MoltenMetalType, Int>? {
        if (stack.isEmpty) return null
        val item = stack.item
        return when (item) {
            Items.RAW_COPPER, Items.COPPER_INGOT -> Pair(COPPER, 1)
            ModItems.RAW_TIN, ModItems.TIN_INGOT -> Pair(TIN, 1)
            ModItems.BRONZE_INGOT -> Pair(BRONZE, 1)
            Items.RAW_IRON, Items.IRON_INGOT -> Pair(IRON, 1)
            else -> null
        }
    }
}
