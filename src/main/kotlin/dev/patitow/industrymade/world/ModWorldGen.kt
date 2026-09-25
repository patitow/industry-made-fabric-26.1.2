package dev.patitow.industrymade.world

import dev.patitow.industrymade.IndustryMade
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BiomeTags
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.placement.PlacedFeature

object ModWorldGen {
    val ORE_TIN_PLACED_KEY: ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, IndustryMade.id("ore_tin"))
    val ORE_FIRE_CLAY_PLACED_KEY: ResourceKey<PlacedFeature> =
        ResourceKey.create(Registries.PLACED_FEATURE, IndustryMade.id("ore_fire_clay"))

    fun initialize() {
        // Tin ore in all Overworld biomes (stone & deepslate)
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            ORE_TIN_PLACED_KEY
        )

        // Fire clay deposits in rivers and swamps
        BiomeModifications.addFeature(
            BiomeSelectors.tag(BiomeTags.IS_RIVER).or(BiomeSelectors.includeByKey(Biomes.SWAMP, Biomes.MANGROVE_SWAMP)),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            ORE_FIRE_CLAY_PLACED_KEY
        )
    }
}
