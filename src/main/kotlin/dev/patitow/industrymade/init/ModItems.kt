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

    val RAW_TIN: Item = registerItem("raw_tin") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val TIN_INGOT: Item = registerItem("tin_ingot") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val BRONZE_INGOT: Item = registerItem("bronze_ingot") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val BRONZE_PLATE: Item = registerItem("bronze_plate") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val BRONZE_ROD: Item = registerItem("bronze_rod") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val BRONZE_GEAR: Item = registerItem("bronze_gear") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val CLAY_MOLD: Item = registerItem("clay_mold") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(16))
    }

    val CERAMIC_MOLD: Item = registerItem("ceramic_mold") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(16))
    }

    val HOT_INGOT_MOLD: dev.patitow.industrymade.item.HotIngotMoldItem = registerItem("hot_ingot_mold") { id ->
        dev.patitow.industrymade.item.HotIngotMoldItem(
            Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1)
        )
    } as dev.patitow.industrymade.item.HotIngotMoldItem

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
                output.accept(ModBlocks.CRUCIBLE)
                output.accept(RAW_TIN)
                output.accept(TIN_INGOT)
                output.accept(BRONZE_INGOT)
                output.accept(BRONZE_PLATE)
                output.accept(BRONZE_ROD)
                output.accept(BRONZE_GEAR)
                output.accept(CLAY_MOLD)
                output.accept(CERAMIC_MOLD)
                output.accept(HOT_INGOT_MOLD)
                output.accept(ModBlocks.LOW_PRESSURE_BOILER)
                output.accept(ModBlocks.STEAM_PISTON)
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
