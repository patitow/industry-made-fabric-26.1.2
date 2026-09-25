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
        }

        override fun generateItemModels(itemModelGenerators: ItemModelGenerators) {
            itemModelGenerators.generateFlatItem(ModItems.FIRE_CLAY, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.FIRE_BRICK, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.RAW_TIN, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.TIN_INGOT, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_INGOT, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_PLATE, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_ROD, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_GEAR, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.WROUGHT_IRON_INGOT, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.IRON_PLATE, ModelTemplates.FLAT_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_SWORD, ModelTemplates.FLAT_HANDHELD_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_SHOVEL, ModelTemplates.FLAT_HANDHELD_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_PICKAXE, ModelTemplates.FLAT_HANDHELD_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_AXE, ModelTemplates.FLAT_HANDHELD_ITEM)
            itemModelGenerators.generateFlatItem(ModItems.BRONZE_HOE, ModelTemplates.FLAT_HANDHELD_ITEM)
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

                    // 9. Bronze Plate (2 Bronze Ingots horizontal)
                    shaped(RecipeCategory.MISC, ModItems.BRONZE_PLATE, 2)
                        .define('B', ModItems.BRONZE_INGOT)
                        .pattern("BB")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_plate")))

                    // 10. Bronze Rod (2 Bronze Ingots vertical)
                    shaped(RecipeCategory.MISC, ModItems.BRONZE_ROD, 4)
                        .define('B', ModItems.BRONZE_INGOT)
                        .pattern("B")
                        .pattern("B")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_rod")))

                    // 11. Bronze Gear (4 Bronze Plates + 1 Bronze Rod)
                    shaped(RecipeCategory.MISC, ModItems.BRONZE_GEAR, 1)
                        .define('P', ModItems.BRONZE_PLATE)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern(" P ")
                        .pattern("PRP")
                        .pattern(" P ")
                        .unlockedBy("has_bronze_plate", has(ModItems.BRONZE_PLATE))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_gear")))

                    // 12. Low Pressure Boiler (Bronze Plates + Refractory Bricks)
                    shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.LOW_PRESSURE_BOILER, 1)
                        .define('P', ModItems.BRONZE_PLATE)
                        .define('B', ModBlocks.REFRACTORY_BRICKS)
                        .pattern("PPP")
                        .pattern("P P")
                        .pattern("BBB")
                        .unlockedBy("has_bronze_plate", has(ModItems.BRONZE_PLATE))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("low_pressure_boiler")))

                    // 13. Steam Mechanical Piston (Gear + Plate + Rod)
                    shaped(RecipeCategory.REDSTONE, ModBlocks.STEAM_PISTON, 1)
                        .define('G', ModItems.BRONZE_GEAR)
                        .define('P', ModItems.BRONZE_PLATE)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern(" G ")
                        .pattern(" P ")
                        .pattern(" R ")
                        .unlockedBy("has_bronze_gear", has(ModItems.BRONZE_GEAR))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("steam_piston")))

                    // 14. Mechanical Forge Hammer (Plates + Ingot + Piston + Bricks)
                    shaped(RecipeCategory.REDSTONE, ModBlocks.MECHANICAL_HAMMER, 1)
                        .define('P', ModItems.BRONZE_PLATE)
                        .define('I', ModItems.BRONZE_INGOT)
                        .define('S', ModBlocks.STEAM_PISTON)
                        .define('B', ModBlocks.REFRACTORY_BRICKS)
                        .pattern("PPP")
                        .pattern("ISI")
                        .pattern("BBB")
                        .unlockedBy("has_steam_piston", has(ModBlocks.STEAM_PISTON))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("mechanical_hammer")))

                    // 15. Iron Plate (Crafting fallback)
                    shaped(RecipeCategory.MISC, ModItems.IRON_PLATE, 2)
                        .define('I', Items.IRON_INGOT)
                        .pattern("II")
                        .pattern("II")
                        .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("iron_plate_from_crafting")))

                    // 16. Bronze Sword
                    shaped(RecipeCategory.COMBAT, ModItems.BRONZE_SWORD, 1)
                        .define('B', ModItems.BRONZE_INGOT)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern("B")
                        .pattern("B")
                        .pattern("R")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_sword")))

                    // 17. Bronze Shovel
                    shaped(RecipeCategory.TOOLS, ModItems.BRONZE_SHOVEL, 1)
                        .define('B', ModItems.BRONZE_INGOT)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern("B")
                        .pattern("R")
                        .pattern("R")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_shovel")))

                    // 18. Bronze Pickaxe
                    shaped(RecipeCategory.TOOLS, ModItems.BRONZE_PICKAXE, 1)
                        .define('B', ModItems.BRONZE_INGOT)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern("BBB")
                        .pattern(" R ")
                        .pattern(" R ")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_pickaxe")))

                    // 19. Bronze Axe
                    shaped(RecipeCategory.TOOLS, ModItems.BRONZE_AXE, 1)
                        .define('B', ModItems.BRONZE_INGOT)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern("BB")
                        .pattern("BR")
                        .pattern(" R")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_axe")))

                    // 20. Bronze Hoe
                    shaped(RecipeCategory.TOOLS, ModItems.BRONZE_HOE, 1)
                        .define('B', ModItems.BRONZE_INGOT)
                        .define('R', ModItems.BRONZE_ROD)
                        .pattern("BB")
                        .pattern(" R")
                        .pattern(" R")
                        .unlockedBy("has_bronze_ingot", has(ModItems.BRONZE_INGOT))
                        .save(output, ResourceKey.create(Registries.RECIPE, IndustryMade.id("bronze_hoe")))
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
            dropSelf(ModBlocks.LOW_PRESSURE_BOILER)
            dropSelf(ModBlocks.STEAM_PISTON)
            dropSelf(ModBlocks.MECHANICAL_HAMMER)
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
                .add(ModBlocks.LOW_PRESSURE_BOILER)
                .add(ModBlocks.STEAM_PISTON)
                .add(ModBlocks.MECHANICAL_HAMMER)
            valueLookupBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.REFRACTORY_BRICKS)
                .add(ModBlocks.CRUCIBLE)
                .add(ModBlocks.LOW_PRESSURE_BOILER)
                .add(ModBlocks.STEAM_PISTON)
                .add(ModBlocks.MECHANICAL_HAMMER)
            valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.BELLOWS)
        }
    }

    class ModItemTagsProvider(
        output: FabricPackOutput,
        registriesFuture: CompletableFuture<HolderLookup.Provider>
    ) : FabricTagsProvider.ItemTagsProvider(output, registriesFuture) {
        override fun addTags(wrapperLookup: HolderLookup.Provider) {
            valueLookupBuilder(ItemTags.SWORDS).add(ModItems.BRONZE_SWORD)
            valueLookupBuilder(ItemTags.SHOVELS).add(ModItems.BRONZE_SHOVEL)
            valueLookupBuilder(ItemTags.PICKAXES).add(ModItems.BRONZE_PICKAXE)
            valueLookupBuilder(ItemTags.AXES).add(ModItems.BRONZE_AXE)
            valueLookupBuilder(ItemTags.HOES).add(ModItems.BRONZE_HOE)
            valueLookupBuilder(dev.patitow.industrymade.init.ModItemTags.BRONZE_TOOL_MATERIALS)
                .add(ModItems.BRONZE_INGOT)
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
            translationBuilder.add("item.industry-made.refractory_bricks", "Refractory Bricks")
            translationBuilder.add(ModBlocks.BELLOWS, "Manual Bellows")
            translationBuilder.add("item.industry-made.bellows", "Manual Bellows")
            translationBuilder.add(ModBlocks.CRUCIBLE, "Smelting Crucible")
            translationBuilder.add("item.industry-made.crucible", "Smelting Crucible")
            translationBuilder.add(ModBlocks.LOW_PRESSURE_BOILER, "Low Pressure Steam Boiler")
            translationBuilder.add("item.industry-made.low_pressure_boiler", "Low Pressure Steam Boiler")
            translationBuilder.add(ModBlocks.STEAM_PISTON, "Mechanical Steam Piston")
            translationBuilder.add("item.industry-made.steam_piston", "Mechanical Steam Piston")
            translationBuilder.add(ModBlocks.MECHANICAL_HAMMER, "Mechanical Forge Hammer")
            translationBuilder.add("item.industry-made.mechanical_hammer", "Mechanical Forge Hammer")
            translationBuilder.add(ModItems.RAW_TIN, "Raw Tin")
            translationBuilder.add(ModItems.TIN_INGOT, "Tin Ingot")
            translationBuilder.add(ModItems.BRONZE_INGOT, "Bronze Ingot")
            translationBuilder.add(ModItems.BRONZE_PLATE, "Bronze Plate")
            translationBuilder.add(ModItems.BRONZE_ROD, "Bronze Rod")
            translationBuilder.add(ModItems.BRONZE_GEAR, "Bronze Gear")
            translationBuilder.add(ModItems.WROUGHT_IRON_INGOT, "Wrought Iron Ingot")
            translationBuilder.add(ModItems.IRON_PLATE, "Iron Plate")
            translationBuilder.add(ModItems.BRONZE_SWORD, "Bronze Sword")
            translationBuilder.add(ModItems.BRONZE_SHOVEL, "Bronze Shovel")
            translationBuilder.add(ModItems.BRONZE_PICKAXE, "Bronze Pickaxe")
            translationBuilder.add(ModItems.BRONZE_AXE, "Bronze Axe")
            translationBuilder.add(ModItems.BRONZE_HOE, "Bronze Hoe")
            translationBuilder.add(ModItems.CLAY_MOLD, "Clay Ingot Mold")
            translationBuilder.add(ModItems.CERAMIC_MOLD, "Ceramic Ingot Mold")
            translationBuilder.add(ModItems.HOT_INGOT_MOLD, "Hot Ingot Mold")
            translationBuilder.add("metal.industry-made.copper", "Copper")
            translationBuilder.add("metal.industry-made.tin", "Tin")
            translationBuilder.add("metal.industry-made.bronze", "Bronze")
            translationBuilder.add("metal.industry-made.iron", "Iron")
            translationBuilder.add("message.industry-made.crucible_no_molten", "The crucible has no molten metal to pour.")
            translationBuilder.add("message.industry-made.crucible_full", "The crucible is full (max 4 items).")
            translationBuilder.add("message.industry-made.boiler_water_full", "The boiler is already full of water.")
            translationBuilder.add("tooltip.industry-made.hot_mold_metal", "Molten Metal: %s")
            translationBuilder.add("tooltip.industry-made.hot_mold_quench_hint", "Dip into water or wait to cool down.")
            translationBuilder.add("itemGroup.industry-made", "Industry Made")
            translationBuilder.add(ModSounds.BELLOWS_BLOW, "Bellows blowing")
            translationBuilder.add(ModSounds.BELLOWS_PUMP, "Bellows pumping")
            translationBuilder.add(ModSounds.STEAM_HISS, "Steam hissing")
            translationBuilder.add(ModSounds.STEAM_WHISTLE, "Steam whistle blaring")
            translationBuilder.add(ModSounds.VALVE_CLICK, "Valve clicking")
            translationBuilder.add(ModSounds.PISTON_CHUG, "Steam piston chugging")
            translationBuilder.add(ModSounds.HAMMER_SLAM, "Mechanical hammer slamming")
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
            translationBuilder.add("item.industry-made.refractory_bricks", "Tijolos Refratários")
            translationBuilder.add(ModBlocks.BELLOWS, "Fole Manual")
            translationBuilder.add("item.industry-made.bellows", "Fole Manual")
            translationBuilder.add(ModBlocks.CRUCIBLE, "Cadinho de Fundição")
            translationBuilder.add("item.industry-made.crucible", "Cadinho de Fundição")
            translationBuilder.add(ModBlocks.LOW_PRESSURE_BOILER, "Caldeira a Vapor de Baixa Pressão")
            translationBuilder.add("item.industry-made.low_pressure_boiler", "Caldeira a Vapor de Baixa Pressão")
            translationBuilder.add(ModBlocks.STEAM_PISTON, "Pistão Mecânico a Vapor")
            translationBuilder.add("item.industry-made.steam_piston", "Pistão Mecânico a Vapor")
            translationBuilder.add(ModBlocks.MECHANICAL_HAMMER, "Martelo Forjador Mecânico")
            translationBuilder.add("item.industry-made.mechanical_hammer", "Martelo Forjador Mecânico")
            translationBuilder.add(ModItems.RAW_TIN, "Estanho Bruto")
            translationBuilder.add(ModItems.TIN_INGOT, "Lingote de Estanho")
            translationBuilder.add(ModItems.BRONZE_INGOT, "Lingote de Bronze")
            translationBuilder.add(ModItems.BRONZE_PLATE, "Placa de Bronze")
            translationBuilder.add(ModItems.BRONZE_ROD, "Haste de Bronze")
            translationBuilder.add(ModItems.BRONZE_GEAR, "Engrenagem de Bronze")
            translationBuilder.add(ModItems.WROUGHT_IRON_INGOT, "Lingote de Ferro Forjado")
            translationBuilder.add(ModItems.IRON_PLATE, "Placa de Ferro")
            translationBuilder.add(ModItems.BRONZE_SWORD, "Espada de Bronze")
            translationBuilder.add(ModItems.BRONZE_SHOVEL, "Pá de Bronze")
            translationBuilder.add(ModItems.BRONZE_PICKAXE, "Picareta de Bronze")
            translationBuilder.add(ModItems.BRONZE_AXE, "Machado de Bronze")
            translationBuilder.add(ModItems.BRONZE_HOE, "Enxada de Bronze")
            translationBuilder.add(ModItems.CLAY_MOLD, "Molde de Argila Cru")
            translationBuilder.add(ModItems.CERAMIC_MOLD, "Molde Cerâmico")
            translationBuilder.add(ModItems.HOT_INGOT_MOLD, "Molde de Lingote Fervente")
            translationBuilder.add("metal.industry-made.copper", "Cobre")
            translationBuilder.add("metal.industry-made.tin", "Estanho")
            translationBuilder.add("metal.industry-made.bronze", "Bronze")
            translationBuilder.add("metal.industry-made.iron", "Ferro")
            translationBuilder.add("message.industry-made.crucible_no_molten", "O cadinho não contém metal fundido para moldar.")
            translationBuilder.add("message.industry-made.crucible_full", "O cadinho está cheio (máx. 4 itens).")
            translationBuilder.add("message.industry-made.boiler_water_full", "A caldeira já está cheia de água.")
            translationBuilder.add("tooltip.industry-made.hot_mold_metal", "Metal Fundido: %s")
            translationBuilder.add("tooltip.industry-made.hot_mold_quench_hint", "Mergulhe na água ou aguarde resfriar.")
            translationBuilder.add("itemGroup.industry-made", "Industry Made")
            translationBuilder.add(ModSounds.BELLOWS_BLOW, "Fole soprando")
            translationBuilder.add(ModSounds.BELLOWS_PUMP, "Fole bombeando")
            translationBuilder.add(ModSounds.STEAM_HISS, "Vapor sibilando")
            translationBuilder.add(ModSounds.STEAM_WHISTLE, "Apito de vapor estridente")
            translationBuilder.add(ModSounds.VALVE_CLICK, "Válvula estalando")
            translationBuilder.add(ModSounds.PISTON_CHUG, "Pistão a vapor trabalhando")
            translationBuilder.add(ModSounds.HAMMER_SLAM, "Martelo mecânico batendo")
        }
    }
}