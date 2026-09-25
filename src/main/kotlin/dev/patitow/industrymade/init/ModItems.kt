package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object ModItems {

    val FIRE_CLAY: Item = registerItem("fire_clay") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val FIRE_BRICK: Item = registerItem("fire_brick") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val INDUSTRY_MADE_TAB_KEY: ResourceKey<CreativeModeTab> =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB, IndustryMade.id("industry_made_tab"))

    val INDUSTRY_MADE_TAB: CreativeModeTab = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        INDUSTRY_MADE_TAB_KEY,
        FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.industry-made"))
            .icon { ItemStack(ModBlocks.REFRACTORY_BRICKS) }
            .displayItems { _, output ->
                output.accept(FIRE_CLAY)
                output.accept(FIRE_BRICK)
                output.accept(ModBlocks.REFRACTORY_BRICKS)
                output.accept(ModBlocks.BELLOWS)
            }
            .build()
    )

    private fun registerItem(name: String, itemFactory: (net.minecraft.resources.Identifier) -> Item): Item {
        val id = IndustryMade.id(name)
        val item = itemFactory(id)
        return Registry.register(BuiltInRegistries.ITEM, id, item)
    }

    fun initialize() {
        // Classloading trigger
    }
}
