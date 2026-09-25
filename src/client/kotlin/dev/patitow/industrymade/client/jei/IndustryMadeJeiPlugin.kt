package dev.patitow.industrymade.client.jei

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.init.ModBlocks
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.registration.IRecipeCatalystRegistration
import mezz.jei.api.registration.IRecipeCategoryRegistration
import mezz.jei.api.registration.IRecipeRegistration
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack

@JeiPlugin
class IndustryMadeJeiPlugin : IModPlugin {

    companion object {
        val PLUGIN_UID: Identifier = IndustryMade.id("jei_plugin")
    }

    override fun getPluginUid(): Identifier = PLUGIN_UID

    override fun registerCategories(registration: IRecipeCategoryRegistration) {
        val guiHelper = registration.jeiHelpers.guiHelper
        registration.addRecipeCategories(
            CrucibleRecipeCategory(guiHelper),
            MechanicalHammerRecipeCategory(guiHelper)
        )
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        registration.addRecipes(CrucibleRecipeCategory.RECIPE_TYPE, CrucibleRecipeCategory.createRecipes())
        registration.addRecipes(MechanicalHammerRecipeCategory.RECIPE_TYPE, MechanicalHammerRecipeCategory.createRecipes())
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        registration.addRecipeCatalyst(ItemStack(ModBlocks.CRUCIBLE), CrucibleRecipeCategory.RECIPE_TYPE)
        registration.addRecipeCatalyst(ItemStack(ModBlocks.MECHANICAL_HAMMER), MechanicalHammerRecipeCategory.RECIPE_TYPE)
    }
}
