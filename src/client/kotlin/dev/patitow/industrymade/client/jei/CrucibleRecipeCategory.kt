package dev.patitow.industrymade.client.jei

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModItems
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.recipe.types.IRecipeType
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

data class CrucibleJeiRecipe(
    val inputs: List<ItemStack>,
    val mold: ItemStack,
    val output: ItemStack,
    val requiredTemp: Int,
    val title: Component
)

class CrucibleRecipeCategory(private val guiHelper: IGuiHelper) : IRecipeCategory<CrucibleJeiRecipe> {

    companion object {
        val RECIPE_TYPE: IRecipeType<CrucibleJeiRecipe> =
            IRecipeType.create("industry-made", "crucible", CrucibleJeiRecipe::class.java)

        fun createRecipes(): List<CrucibleJeiRecipe> {
            val mold = ItemStack(ModItems.CERAMIC_MOLD)
            return listOf(
                // 1. Bronze Alloy (3 Copper + 1 Tin + Mold -> 4 Bronze Ingots)
                CrucibleJeiRecipe(
                    inputs = listOf(
                        ItemStack(Items.COPPER_INGOT, 1),
                        ItemStack(Items.COPPER_INGOT, 1),
                        ItemStack(Items.COPPER_INGOT, 1),
                        ItemStack(ModItems.TIN_INGOT, 1)
                    ),
                    mold = mold,
                    output = ItemStack(ModItems.BRONZE_INGOT, 4),
                    requiredTemp = 950,
                    title = Component.literal("Liga de Bronze (4x)")
                ),
                // 2. Bronze Alloy (1 Copper + 1 Tin + Mold -> 2 Bronze Ingots)
                CrucibleJeiRecipe(
                    inputs = listOf(
                        ItemStack(Items.COPPER_INGOT, 1),
                        ItemStack(ModItems.TIN_INGOT, 1)
                    ),
                    mold = mold,
                    output = ItemStack(ModItems.BRONZE_INGOT, 2),
                    requiredTemp = 950,
                    title = Component.literal("Liga de Bronze (2x)")
                ),
                // 3. Copper Smelting (Raw Copper / Copper Ingot -> Copper Ingot)
                CrucibleJeiRecipe(
                    inputs = listOf(ItemStack(Items.RAW_COPPER, 1)),
                    mold = mold,
                    output = ItemStack(Items.COPPER_INGOT, 1),
                    requiredTemp = 1085,
                    title = Component.literal("Cobre Fundido")
                ),
                // 4. Tin Smelting (Raw Tin / Tin Ingot -> Tin Ingot)
                CrucibleJeiRecipe(
                    inputs = listOf(ItemStack(ModItems.RAW_TIN, 1)),
                    mold = mold,
                    output = ItemStack(ModItems.TIN_INGOT, 1),
                    requiredTemp = 232,
                    title = Component.literal("Estanho Fundido")
                ),
                // 5. Cast Iron (Raw Iron / Iron Ingot -> Iron Ingot)
                CrucibleJeiRecipe(
                    inputs = listOf(ItemStack(Items.RAW_IRON, 1)),
                    mold = mold,
                    output = ItemStack(Items.IRON_INGOT, 1),
                    requiredTemp = 1200,
                    title = Component.literal("Ferro Fundido")
                )
            )
        }
    }

    private val background: IDrawable = guiHelper.createBlankDrawable(140, 52)
    private val icon: IDrawable = guiHelper.createDrawableItemStack(ItemStack(ModBlocks.CRUCIBLE))

    override fun getRecipeType(): IRecipeType<CrucibleJeiRecipe> = RECIPE_TYPE

    override fun getTitle(): Component = Component.translatable("block.industry-made.crucible")

    override fun getWidth(): Int = 140

    override fun getHeight(): Int = 52

    override fun getIcon(): IDrawable = icon

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        recipe: CrucibleJeiRecipe,
        focuses: IFocusGroup
    ) {
        // 4 Input slots in 2x2 grid
        val slotPositions = arrayOf(
            Pair(4, 8),
            Pair(22, 8),
            Pair(4, 26),
            Pair(22, 26)
        )

        for (i in 0 until 4) {
            val (x, y) = slotPositions[i]
            val slot = builder.addInputSlot(x, y).setStandardSlotBackground()
            if (i < recipe.inputs.size) {
                slot.addItemStack(recipe.inputs[i])
            }
        }

        // Ceramic mold slot
        builder.addInputSlot(44, 17)
            .setStandardSlotBackground()
            .addItemStack(recipe.mold)

        // Output slot
        builder.addOutputSlot(108, 17)
            .setOutputSlotBackground()
            .addItemStack(recipe.output)
    }

    override fun createRecipeExtras(
        builder: IRecipeExtrasBuilder,
        recipe: CrucibleJeiRecipe,
        focuses: IFocusGroup
    ) {
        builder.addAnimatedRecipeArrow(60).setPosition(68, 17)
        builder.addAnimatedRecipeFlame(120).setPosition(73, 34)
        builder.addText(Component.literal("§c≥ ${recipe.requiredTemp}°C§r"), 58, 2)
    }
}
