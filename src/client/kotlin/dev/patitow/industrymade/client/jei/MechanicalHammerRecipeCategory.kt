package dev.patitow.industrymade.client.jei

import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModItems
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder
import mezz.jei.api.gui.drawable.IDrawable
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.recipe.IFocusGroup
import mezz.jei.api.recipe.category.IRecipeCategory
import mezz.jei.api.recipe.types.IRecipeType
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

data class MechanicalHammerJeiRecipe(
    val input: ItemStack,
    val output: ItemStack,
    val strikes: Int = 4,
    val description: Component
)

class MechanicalHammerRecipeCategory(private val guiHelper: IGuiHelper) :
    IRecipeCategory<MechanicalHammerJeiRecipe> {

    companion object {
        val RECIPE_TYPE: IRecipeType<MechanicalHammerJeiRecipe> =
            IRecipeType.create("industry-made", "mechanical_hammer", MechanicalHammerJeiRecipe::class.java)

        fun createRecipes(): List<MechanicalHammerJeiRecipe> {
            return listOf(
                // 1. Ingot to Plate
                MechanicalHammerJeiRecipe(
                    input = ItemStack(ModItems.BRONZE_INGOT),
                    output = ItemStack(ModItems.BRONZE_PLATE),
                    description = Component.literal("Forjamento de Placa de Bronze")
                ),
                MechanicalHammerJeiRecipe(
                    input = ItemStack(Items.IRON_INGOT),
                    output = ItemStack(ModItems.WROUGHT_IRON_INGOT),
                    description = Component.literal("Descarbonetação: Ferro -> Ferro Forjado")
                ),
                MechanicalHammerJeiRecipe(
                    input = ItemStack(ModItems.WROUGHT_IRON_INGOT),
                    output = ItemStack(ModItems.IRON_PLATE),
                    description = Component.literal("Forjamento de Placa de Ferro")
                ),
                // 2. Ore Doubling / Crushing
                MechanicalHammerJeiRecipe(
                    input = ItemStack(Items.RAW_COPPER),
                    output = ItemStack(Items.COPPER_INGOT, 2),
                    description = Component.literal("Britagem & Refino de Cobre Bruto (2x)")
                ),
                MechanicalHammerJeiRecipe(
                    input = ItemStack(ModItems.RAW_TIN),
                    output = ItemStack(ModItems.TIN_INGOT, 2),
                    description = Component.literal("Britagem & Refino de Estanho Bruto (2x)")
                ),
                MechanicalHammerJeiRecipe(
                    input = ItemStack(Items.RAW_IRON),
                    output = ItemStack(Items.IRON_INGOT, 2),
                    description = Component.literal("Britagem & Refino de Ferro Bruto (2x)")
                )
            )
        }
    }

    private val icon: IDrawable = guiHelper.createDrawableItemStack(ItemStack(ModBlocks.MECHANICAL_HAMMER))

    override fun getRecipeType(): IRecipeType<MechanicalHammerJeiRecipe> = RECIPE_TYPE

    override fun getTitle(): Component = Component.translatable("block.industry-made.mechanical_hammer")

    override fun getWidth(): Int = 120

    override fun getHeight(): Int = 42

    override fun getIcon(): IDrawable = icon

    override fun setRecipe(
        builder: IRecipeLayoutBuilder,
        recipe: MechanicalHammerJeiRecipe,
        focuses: IFocusGroup
    ) {
        builder.addInputSlot(16, 13)
            .setStandardSlotBackground()
            .addItemStack(recipe.input)

        builder.addOutputSlot(80, 13)
            .setOutputSlotBackground()
            .addItemStack(recipe.output)
    }

    override fun createRecipeExtras(
        builder: IRecipeExtrasBuilder,
        recipe: MechanicalHammerJeiRecipe,
        focuses: IFocusGroup
    ) {
        builder.addAnimatedRecipeArrow(40).setPosition(46, 13)
        builder.addText(Component.literal("§6${recipe.strikes} Golpes§r"), 38, 1)
    }
}
