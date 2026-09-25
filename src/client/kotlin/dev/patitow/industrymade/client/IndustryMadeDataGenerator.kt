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
            blockModelGenerators.createParticleOnlyBlock(ModBlocks.CRUCIBLE, ModBlocks.REFRACTORY_BRICKS)
        }

        override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {
            itemModelGenerators.generateFlatItem(ModItems.FIRE_CLAY, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.FIRE_BRICK, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModBlocks.BELLOWS.asItem(), ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModBlocks.CRUCIBLE.asItem(), ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.RAW_TIN, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.TIN_INGOT, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_INGOT, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.CLAY_MOLD, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.CERAMIC_MOLD, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.HOT_INGOT_MOLD, ModelTemplates.FLAT_ITEM)
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

                    // 5. Smelting Crucible (5 Fire Bricks in U-shape)
                    shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CRUCIBLE, 1)
                        .define('B', ModItems.FIRE_BRICK)
                        .pattern("B B")
                        .pattern("BBB")
                        .unlockedBy("has_fire_brick", has(ModItems.FIRE_BRICK))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("crucible")))

                    // 6. Clay Ingot Mold (3 Clay Balls)
                    shapeless(RecipeCategory.MISC, ModItems.CLAY_MOLD, 1)
                        .requires(Items.CLAY_BALL, 3)
                        .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("clay_mold")))

                    // 7. Ceramic Ingot Mold (Smelting/Blasting Clay Mold)
                    SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.CLAY_MOLD),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.CERAMIC_MOLD,
                        0.2f,
                        200
                    ).unlockedBy("has_clay_mold", has(ModItems.CLAY_MOLD))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("ceramic_mold_from_smelting")))

                    SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.CLAY_MOLD),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.CERAMIC_MOLD,
                        0.2f,
                        100
                    ).unlockedBy("has_clay_mold", has(ModItems.CLAY_MOLD))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("ceramic_mold_from_blasting")))

                    // 8. Tin Ingot from Raw Tin
                    SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModItems.RAW_TIN),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.TIN_INGOT,
                        0.7f,
                        200
                    ).unlockedBy("has_raw_tin", has(ModItems.RAW_TIN))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("tin_ingot_from_smelting")))

                    SimpleCookingRecipeBuilder.blasting(
                        Ingredient.of(ModItems.RAW_TIN),
                        RecipeCategory.MISC,
                        CookingBookCategory.MISC,
                        ModItems.TIN_INGOT,
                        0.7f,
                        100
                    ).unlockedBy("has_raw_tin", has(ModItems.RAW_TIN))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("tin_ingot_from_blasting")))
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
            dropSelf(ModBlocks.CRUCIBLE)
        }
    }

    class ModBlockTagsProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricTagsProvider.BlockTagsProvider(output, registriesFuture) {
        override fun addTags(wrapperLookup: HolderLookup.Provider) {
            valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.REFRACTORY_BRICKS)
                .add(ModBlocks.CRUCIBLE)
            valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.REFRACTORY_BRICKS)
                .add(ModBlocks.CRUCIBLE)
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
            translationBuilder.add(ModBlocks.CRUCIBLE, "Smelting Crucible")
            translationBuilder.add(ModItems.RAW_TIN, "Raw Tin")
            translationBuilder.add(ModItems.TIN_INGOT, "Tin Ingot")
            translationBuilder.add(ModItems.BRONZE_INGOT, "Bronze Ingot")
            translationBuilder.add(ModItems.CLAY_MOLD, "Clay Ingot Mold")
            translationBuilder.add(ModItems.CERAMIC_MOLD, "Ceramic Ingot Mold")
            translationBuilder.add(ModItems.HOT_INGOT_MOLD, "Hot Ingot Mold")
            translationBuilder.add("metal.industry-made.copper", "Copper")
            translationBuilder.add("metal.industry-made.tin", "Tin")
            translationBuilder.add("metal.industry-made.bronze", "Bronze")
            translationBuilder.add("metal.industry-made.iron", "Iron")
            translationBuilder.add("message.industry-made.crucible_no_molten", "The crucible has no molten metal to pour.")
            translationBuilder.add("message.industry-made.crucible_full", "The crucible is full (max 4 items).")
            translationBuilder.add("tooltip.industry-made.hot_mold_metal", "Molten Metal: %s")
            translationBuilder.add("tooltip.industry-made.hot_mold_quench_hint", "Dip into water or wait to cool down.")
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
            translationBuilder.add(ModBlocks.CRUCIBLE, "Cadinho de Fundição")
            translationBuilder.add(ModItems.RAW_TIN, "Estanho Bruto")
            translationBuilder.add(ModItems.TIN_INGOT, "Lingote de Estanho")
            translationBuilder.add(ModItems.BRONZE_INGOT, "Lingote de Bronze")
            translationBuilder.add(ModItems.CLAY_MOLD, "Molde de Argila Cru")
            translationBuilder.add(ModItems.CERAMIC_MOLD, "Molde Cerâmico")
            translationBuilder.add(ModItems.HOT_INGOT_MOLD, "Molde de Lingote Fervente")
            translationBuilder.add("metal.industry-made.copper", "Cobre")
            translationBuilder.add("metal.industry-made.tin", "Estanho")
            translationBuilder.add("metal.industry-made.bronze", "Bronze")
            translationBuilder.add("metal.industry-made.iron", "Ferro")
            translationBuilder.add("message.industry-made.crucible_no_molten", "O cadinho não contém metal fundido para moldar.")
            translationBuilder.add("message.industry-made.crucible_full", "O cadinho está cheio (máx. 4 itens).")
            translationBuilder.add("tooltip.industry-made.hot_mold_metal", "Metal Fundido: %s")
            translationBuilder.add("tooltip.industry-made.hot_mold_quench_hint", "Mergulhe na água ou aguarde resfriar.")
            translationBuilder.add("itemGroup.industry-made", "Industry Made")
            translationBuilder.add(ModSounds.BELLOWS_BLOW, "Fole soprando")
            translationBuilder.add(ModSounds.BELLOWS_PUMP, "Fole bombeando")
        }
    }
}