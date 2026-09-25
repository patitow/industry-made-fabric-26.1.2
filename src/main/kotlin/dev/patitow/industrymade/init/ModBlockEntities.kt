package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.entity.BellowsBlockEntity
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object ModBlockEntities {

    val BELLOWS: BlockEntityType<BellowsBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        IndustryMade.id("bellows"),
        FabricBlockEntityTypeBuilder.create(::BellowsBlockEntity, ModBlocks.BELLOWS).build()
    )

    fun initialize() {
        // Classloading trigger
    }
}
