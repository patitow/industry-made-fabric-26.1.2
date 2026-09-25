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

    private fun <T : Block> registerBlock(name: String, blockFactory: (net.minecraft.resources.Identifier) -> T): T {
        val id = IndustryMade.id(name)
        val block = blockFactory(id)
        Registry.register(BuiltInRegistries.BLOCK, id, block)

        val itemKey = ResourceKey.create(Registries.ITEM, id)
        val item = BlockItem(block, Item.Properties().setId(itemKey))
        Registry.register(BuiltInRegistries.ITEM, id, item)

        return block
    }

    fun initialize() {
        // Classloading trigger
    }
}
