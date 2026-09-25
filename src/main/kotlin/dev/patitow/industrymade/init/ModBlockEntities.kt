package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.block.entity.BellowsBlockEntity
import dev.patitow.industrymade.block.entity.CrucibleBlockEntity
import dev.patitow.industrymade.block.entity.LowPressureBoilerBlockEntity
import dev.patitow.industrymade.block.entity.SteamPistonBlockEntity
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

    val CRUCIBLE: BlockEntityType<CrucibleBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        IndustryMade.id("crucible"),
        FabricBlockEntityTypeBuilder.create(::CrucibleBlockEntity, ModBlocks.CRUCIBLE).build()
    )

    val LOW_PRESSURE_BOILER: BlockEntityType<LowPressureBoilerBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        IndustryMade.id("low_pressure_boiler"),
        FabricBlockEntityTypeBuilder.create(::LowPressureBoilerBlockEntity, ModBlocks.LOW_PRESSURE_BOILER).build()
    )

    val STEAM_PISTON: BlockEntityType<SteamPistonBlockEntity> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        IndustryMade.id("steam_piston"),
        FabricBlockEntityTypeBuilder.create(::SteamPistonBlockEntity, ModBlocks.STEAM_PISTON).build()
    )

    fun initialize() {
        // Classloading trigger
    }
}
