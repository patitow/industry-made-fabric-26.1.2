package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.BellowsBlock
import dev.patitow.industrymade.block.RefractoryBrickBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour

object ModBlocks {

    val REFRACTORY_BRICKS: RefractoryBrickBlock = registerBlock("refractory_bricks") { id ->
        RefractoryBrickBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(2.0f, 6.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        )
    }

    val BELLOWS: BellowsBlock = registerBlock("bellows") { id ->
        BellowsBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(1.5f)
                .sound(SoundType.WOOD)
                .noOcclusion()
        )
    }

    val CRUCIBLE: dev.patitow.industrymade.block.CrucibleBlock = registerBlock("crucible") { id ->
        dev.patitow.industrymade.block.CrucibleBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(2.0f, 6.0f)
                .sound(SoundType.STONE)
                .lightLevel { state ->
                    when (state.getValue(dev.patitow.industrymade.block.CrucibleBlock.HEAT_LEVEL)) {
                        1 -> 4
                        2 -> 9
                        3 -> 15
                        else -> 0
                    }
                }
                .noOcclusion()
                .requiresCorrectToolForDrops()
        )
    }

    val LOW_PRESSURE_BOILER: dev.patitow.industrymade.block.LowPressureBoilerBlock = registerBlock("low_pressure_boiler") { id ->
        dev.patitow.industrymade.block.LowPressureBoilerBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(3.0f, 6.0f)
                .sound(SoundType.COPPER)
                .lightLevel { state ->
                    if (state.getValue(dev.patitow.industrymade.block.LowPressureBoilerBlock.LIT)) 8 else 0
                }
                .noOcclusion()
                .requiresCorrectToolForDrops()
        )
    }

    val STEAM_PISTON: dev.patitow.industrymade.block.SteamPistonBlock = registerBlock("steam_piston") { id ->
        dev.patitow.industrymade.block.SteamPistonBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(2.5f, 5.0f)
                .sound(SoundType.COPPER)
                .noOcclusion()
                .requiresCorrectToolForDrops()
        )
    }

    val MECHANICAL_HAMMER: dev.patitow.industrymade.block.MechanicalHammerBlock = registerBlock("mechanical_hammer") { id ->
        dev.patitow.industrymade.block.MechanicalHammerBlock(
            BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, id))
                .strength(3.0f, 6.0f)
                .sound(SoundType.ANVIL)
                .noOcclusion()
                .requiresCorrectToolForDrops()
        )
    }

    private fun <T : Block> registerBlock(name: String, blockFactory: (net.minecraft.resources.Identifier) -> T): T {
        val id = IndustryMade.id(name)
        val block = blockFactory(id)
        Registry.register(BuiltInRegistries.BLOCK, id, block)

        val itemKey = ResourceKey.create(Registries.ITEM, id)
        val item = BlockItem(block, Item.Properties().setId(itemKey).useBlockDescriptionPrefix())
        Registry.register(BuiltInRegistries.ITEM, id, item)

        return block
    }

    fun initialize() {
        // Classloading trigger
    }
}
