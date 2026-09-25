package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object ModItemTags {
    val BRONZE_TOOL_MATERIALS: TagKey<Item> = TagKey.create(
        Registries.ITEM,
        IndustryMade.id("bronze_tool_materials")
    )
}
