package dev.patitow.industrymade.item

import dev.patitow.industrymade.init.ModItemTags
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.ToolMaterial

object ModToolMaterials {
    val BRONZE: ToolMaterial = ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL, // Can mine everything Iron can mine
        380,                                // Higher durability than iron (250)
        6.5f,                               // Mining speed (Iron is 6.0f)
        2.0f,                               // Attack damage bonus
        14,                                 // Enchantability (same as Iron)
        ModItemTags.BRONZE_TOOL_MATERIALS
    )
}
