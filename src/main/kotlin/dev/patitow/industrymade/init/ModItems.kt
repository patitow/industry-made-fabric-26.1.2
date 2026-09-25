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

    val WROUGHT_IRON_INGOT: Item = registerItem("wrought_iron_ingot") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val IRON_PLATE: Item = registerItem("iron_plate") { id ->
        Item(Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)))
    }

    val BRONZE_SWORD: Item = registerItem("bronze_sword") { id ->
        Item(
            Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .sword(dev.patitow.industrymade.item.ModToolMaterials.BRONZE, 3.0f, -2.4f)
        )
    }

    val BRONZE_SHOVEL: Item = registerItem("bronze_shovel") { id ->
        net.minecraft.world.item.ShovelItem(
            dev.patitow.industrymade.item.ModToolMaterials.BRONZE,
            1.5f,
            -3.0f,
            Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
        )
    }

    val BRONZE_PICKAXE: Item = registerItem("bronze_pickaxe") { id ->
        Item(
            Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .pickaxe(dev.patitow.industrymade.item.ModToolMaterials.BRONZE, 1.0f, -2.8f)
        )
    }

    val BRONZE_AXE: Item = registerItem("bronze_axe") { id ->
        net.minecraft.world.item.AxeItem(
            dev.patitow.industrymade.item.ModToolMaterials.BRONZE,
            6.0f,
            -3.1f,
            Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
        )
    }

    val BRONZE_HOE: Item = registerItem("bronze_hoe") { id ->
        net.minecraft.world.item.HoeItem(
            dev.patitow.industrymade.item.ModToolMaterials.BRONZE,
            0.0f,
            -1.0f,
            Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
        )
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
                output.accept(ModBlocks.CRUCIBLE)
                output.accept(RAW_TIN)
                output.accept(TIN_INGOT)
                output.accept(BRONZE_INGOT)
                output.accept(BRONZE_PLATE)
                output.accept(BRONZE_ROD)
                output.accept(BRONZE_GEAR)
                output.accept(WROUGHT_IRON_INGOT)
                output.accept(IRON_PLATE)
                output.accept(BRONZE_SWORD)
                output.accept(BRONZE_SHOVEL)
                output.accept(BRONZE_PICKAXE)
                output.accept(BRONZE_AXE)
                output.accept(BRONZE_HOE)
                output.accept(CLAY_MOLD)
                output.accept(CERAMIC_MOLD)
                output.accept(HOT_INGOT_MOLD)
                output.accept(ModBlocks.LOW_PRESSURE_BOILER)
                output.accept(ModBlocks.BRONZE_STEAM_PIPE)
                output.accept(ModBlocks.BRONZE_VALVE_PIPE)
                output.accept(ModBlocks.BRONZE_GAUGE_PIPE)
                output.accept(ModBlocks.STEAM_PISTON)
                output.accept(ModBlocks.MECHANICAL_HAMMER)
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
