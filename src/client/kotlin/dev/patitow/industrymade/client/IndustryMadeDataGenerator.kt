package dev.patitow.industrymade.client

import dev.patitow.industrymade.IndustryMade
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModItems
import dev.patitow.industrymade.init.ModSounds
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CookingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Blocks
import java.util.concurrent.CompletableFuture

object IndustryMadeDataGenerator : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack = fabricDataGenerator.createPack()

        pack.addProvider(::ModModelProvider)
        pack.addProvider(::ModRecipeProvider)
        pack.addProvider(::ModBlockLootTableProvider)
        pack.addProvider(::ModBlockTagsProvider)
        pack.addProvider(::ModItemTagsProvider)
        pack.addProvider(::ModEnglishLanguageProvider)
        pack.addProvider(::ModPortugueseLanguageProvider)
    }

    class ModModelProvider(output: FabricPackOutput) : FabricModelProvider(output) {
        override fun generateBlockStateModels(blockModelGenerators: BlockModelGenerators) {
            blockModelGenerators.createTrivialCube(ModBlocks.REFRACTORY_BRICKS)
            blockModelGenerators.createParticleOnlyBlock(ModBlocks.BELLOWS, Blocks.OAK_PLANKS)
        }

        override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {
            itemModelGenerators.generateFlatItem(ModItems.FIRE_CLAY, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.FIRE_BRICK, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModBlocks.BELLOWS.asItem(), ModelTemplates.FLAT_ITEM)
        }
    }

    class ModRecipeProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricRecipeProvider(output, registriesFuture) {

        override fun getName(): String = "Industry Made Recipes"

        override fun createRecipeProvider(
            registries: HolderLookup.Provider,
            output: RecipeOutput
        ): RecipeProvider {
            return object : RecipeProvider(registries, output) {
                override fun buildRecipes() {
                    // 1. Fire Clay from Clay Ball + Sand
                    shapeless(RecipeCategory.MISC, ModItems.FIRE_CLAY, 2)
                        .requires(Items.CLAY_BALL)
                        .requires(Items.SAND)
                        .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("fire_clay_from_sand")))

                    // Fire Clay from Clay Ball + Gravel
                    shapeless(RecipeCategory.MISC, ModItems.FIRE_CLAY, 2)
                        .requires(Items.CLAY_BALL)
                        .requires(Items.GRAVEL)
                        .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("fire_clay_from_gravel")))

                    // 2. Fire Brick from smelting Fire Clay
                    SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.FIRE_CLAY),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.FIRE_BRICK,
                        0.3f,
                        200
                    ).unlockedBy("has_fire_clay", has(ModItems.FIRE_CLAY))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("fire_brick_from_smelting")))

                    // Fire Brick from blasting Fire Clay (faster)
                    SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.FIRE_CLAY),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.FIRE_BRICK,
                        0.3f,
                        100
                    ).unlockedBy("has_fire_clay", has(ModItems.FIRE_CLAY))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("fire_brick_from_blasting")))

                    // 3. Refractory Bricks (2x2 of Fire Bricks)
                    shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.REFRACTORY_BRICKS, 1)
                        .define('B', ModItems.FIRE_BRICK)
                        .pattern("BB")
                        .pattern("BB")
                        .unlockedBy("has_fire_brick", has(ModItems.FIRE_BRICK))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("refractory_bricks")))

                    // 4. Manual Bellows (Wood planks, leather, iron ingot)
                    shaped(RecipeCategory.REDSTONE, ModBlocks.BELLOWS, 1)
                        .define('P', ItemTags.PLANKS)
                        .define('L', Items.LEATHER)
                        .define('I', Items.IRON_INGOT)
                        .pattern("PPP")
                        .pattern("LLI")
                        .pattern("PPP")
                        .unlockedBy("has_leather", has(Items.LEATHER))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bellows")))
                }
            }
        }
    }

    class ModBlockLootTableProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricBlockLootSubProvider(output, registriesFuture) {
        override fun generate() {
            dropSelf(ModBlocks.REFRACTORY_BRICKS)
            dropSelf(ModBlocks.BELLOWS)
        }
    }

    class ModBlockTagsProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricTagsProvider.BlockTagsProvider(output, registriesFuture) {
        override fun addTags(wrapperLookup: HolderLookup.Provider) {
            valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.REFRACTORY_BRICKS)
            valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.REFRACTORY_BRICKS)
            valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.BELLOWS)
        }
    }

    class ModItemTagsProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricTagsProvider.ItemTagsProvider(output, registriesFuture) {
        override fun addTags(wrapperLookup: HolderLookup.Provider) {
            // Placeholder for custom item tags in Era 1
        }
    }

    class ModEnglishLanguageProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricLanguageProvider(output, "en_us", registriesFuture) {
        override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
            translationBuilder.add(ModItems.FIRE_CLAY, "Fire Clay")
            translationBuilder.add(ModItems.FIRE_BRICK, "Fire Brick")
            translationBuilder.add(ModBlocks.REFRACTORY_BRICKS, "Refractory Bricks")
            translationBuilder.add(ModBlocks.BELLOWS, "Manual Bellows")
            translationBuilder.add("itemGroup.industry-made", "Industry Made")
            translationBuilder.add(ModSounds.BELLOWS_BLOW, "Bellows blowing")
            translationBuilder.add(ModSounds.BELLOWS_PUMP, "Bellows pumping")
        }
    }

    class ModPortugueseLanguageProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricLanguageProvider(output, "pt_br", registriesFuture) {
        override fun generateTranslations(registryLookup: HolderLookup.Provider, translationBuilder: TranslationBuilder) {
            translationBuilder.add(ModItems.FIRE_CLAY, "Argila Refratária")
            translationBuilder.add(ModItems.FIRE_BRICK, "Tijolo Refratário")
            translationBuilder.add(ModBlocks.REFRACTORY_BRICKS, "Tijolos Refratários")
            translationBuilder.add(ModBlocks.BELLOWS, "Fole Manual")
            translationBuilder.add("itemGroup.industry-made", "Industry Made")
            translationBuilder.add(ModSounds.BELLOWS_BLOW, "Fole soprando")
            translationBuilder.add(ModSounds.BELLOWS_PUMP, "Fole bombeando")
        }
    }
}