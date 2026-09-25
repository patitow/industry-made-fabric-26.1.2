package dev.patitow.industrymade.command

import com.mojang.brigadier.CommandDispatcher
import dev.patitow.industrymade.block.BellowsBlock
import dev.patitow.industrymade.block.BronzeGaugePipeBlock
import dev.patitow.industrymade.block.BronzeSteamPipeBlock
import dev.patitow.industrymade.block.BronzeValvePipeBlock
import dev.patitow.industrymade.block.LowPressureBoilerBlock
import dev.patitow.industrymade.block.MechanicalHammerBlock
import dev.patitow.industrymade.block.SteamPistonBlock
import dev.patitow.industrymade.block.entity.LowPressureBoilerBlockEntity
import dev.patitow.industrymade.block.entity.MechanicalHammerBlockEntity
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModItems
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CampfireBlock
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.ChestBlockEntity

object TestLabCommand {

    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            registerCommands(dispatcher, "industrymade")
            registerCommands(dispatcher, "im")
        }
    }

    private fun registerCommands(dispatcher: CommandDispatcher<CommandSourceStack>, rootLiteral: String) {
        dispatcher.register(
            Commands.literal(rootLiteral)
                .then(
                    Commands.literal("testlab")
                        .executes { ctx ->
                            executeTestLab(ctx.source)
                        }
                )
                .then(
                    Commands.literal("kit")
                        .executes { ctx ->
                            executeKit(ctx.source)
                        }
                )
                .then(
                    Commands.literal("clear")
                        .executes { ctx ->
                            executeClear(ctx.source)
                        }
                )
                .then(
                    Commands.literal("help")
                        .executes { ctx ->
                            executeHelp(ctx.source)
                        }
                )
                .executes { ctx ->
                    executeHelp(ctx.source)
                }
        )
    }

    private fun executeTestLab(source: CommandSourceStack): Int {
        val player = source.player ?: run {
            source.sendFailure(Component.literal("§cEste comando só pode ser executado por um jogador no mundo.§r"))
            return 0
        }

        val level: ServerLevel = source.level
        val dir = player.direction
        val origin = player.blockPosition().relative(dir, 4)

        // 1. Clear area (width 9, height 6, depth 7)
        for (dx in -4..4) {
            for (dz in -3..3) {
                for (dy in 0..5) {
                    level.setBlock(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3)
                }
                // Flooring at y = -1
                val isBorder = dx == -4 || dx == 4 || dz == -3 || dz == 3
                val floorBlock = if (isBorder) Blocks.STONE_BRICKS else Blocks.SMOOTH_STONE
                level.setBlock(origin.offset(dx, -1, dz), floorBlock.defaultBlockState(), 3)
            }
        }

        // ==========================================
        // 2. ESTAÇÃO 1: METALURGIA TÉRMICA & CADINHO (X = -3)
        // ==========================================
        val firePos = origin.offset(-3, 0, -1)
        val cruciblePos = origin.offset(-3, 1, -1)
        val brickPos = origin.offset(-3, 0, -2)
        val bellowsPos = origin.offset(-3, 1, -2)
        val chest1Pos = origin.offset(-3, 0, 1)

        level.setBlock(firePos, Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 3)
        level.setBlock(cruciblePos, ModBlocks.CRUCIBLE.defaultBlockState(), 3)
        level.setBlock(brickPos, ModBlocks.REFRACTORY_BRICKS.defaultBlockState(), 3)
        level.setBlock(bellowsPos, ModBlocks.BELLOWS.defaultBlockState().setValue(BellowsBlock.FACING, Direction.SOUTH), 3)

        // Geological Samples (Left wall)
        level.setBlock(origin.offset(-4, 0, -1), ModBlocks.TIN_ORE.defaultBlockState(), 3)
        level.setBlock(origin.offset(-4, 0, 0), ModBlocks.DEEPSLATE_TIN_ORE.defaultBlockState(), 3)
        level.setBlock(origin.offset(-4, 0, 1), ModBlocks.FIRE_CLAY_BLOCK.defaultBlockState(), 3)

        level.setBlock(chest1Pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 3)
        val chest1 = level.getBlockEntity(chest1Pos) as? ChestBlockEntity
        if (chest1 != null) {
            chest1.setItem(0, ItemStack(Items.RAW_COPPER, 32))
            chest1.setItem(1, ItemStack(ModItems.RAW_TIN, 32))
            chest1.setItem(2, ItemStack(ModItems.CERAMIC_MOLD, 8))
            chest1.setItem(3, ItemStack(ModItems.FIRE_CLAY, 16))
            chest1.setItem(4, ItemStack(ModItems.FIRE_BRICK, 16))
            chest1.setItem(5, ItemStack(Items.WATER_BUCKET, 1))
            chest1.setItem(6, ItemStack(ModBlocks.TIN_ORE, 16))
            chest1.setItem(7, ItemStack(ModBlocks.DEEPSLATE_TIN_ORE, 16))
            chest1.setItem(8, ItemStack(ModBlocks.FIRE_CLAY_BLOCK, 16))
            chest1.setItem(9, ItemStack(ModItems.BRONZE_PICKAXE, 1))
            chest1.setItem(10, ItemStack(ModItems.BRONZE_SHOVEL, 1))
            chest1.setChanged()
        }

        // ==========================================
        // 3. ESTAÇÃO 2: CALDEIRA & REDE DE TUBULAÇÃO (X = 0)
        // ==========================================
        val boilerFirePos = origin.offset(0, 0, -2)
        val boilerPos = origin.offset(0, 1, -2)
        val pipe1Pos = origin.offset(0, 2, -2)
        val pipe2Pos = origin.offset(0, 2, -1)
        val valvePos = origin.offset(0, 2, 0)
        val gaugePos = origin.offset(0, 2, 1)
        val chest2Pos = origin.offset(0, 0, 2)

        level.setBlock(boilerFirePos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true), 3)
        level.setBlock(boilerPos, ModBlocks.LOW_PRESSURE_BOILER.defaultBlockState().setValue(LowPressureBoilerBlock.FACING, Direction.SOUTH), 3)

        // Pre-fill boiler with 4000 mB of water for instant steam demonstration
        val boilerBe = level.getBlockEntity(boilerPos) as? LowPressureBoilerBlockEntity
        if (boilerBe != null) {
            boilerBe.waterAmount = 4000
            boilerBe.temperature = 105.0 // Already at boiling point!
            boilerBe.steamPressure = 2.0 // Ready to supply working steam
            boilerBe.setChanged()
            level.sendBlockUpdated(boilerPos, level.getBlockState(boilerPos), level.getBlockState(boilerPos), 3)
        }

        // Overhead bronze pipe connection
        level.setBlock(
            pipe1Pos,
            ModBlocks.BRONZE_STEAM_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.DOWN, true)
                .setValue(BronzeSteamPipeBlock.SOUTH, true),
            3
        )
        level.setBlock(
            pipe2Pos,
            ModBlocks.BRONZE_STEAM_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.NORTH, true)
                .setValue(BronzeSteamPipeBlock.SOUTH, true),
            3
        )
        level.setBlock(
            valvePos,
            ModBlocks.BRONZE_VALVE_PIPE.defaultBlockState()
                .setValue(BronzeValvePipeBlock.OPEN, true)
                .setValue(BronzeSteamPipeBlock.NORTH, true)
                .setValue(BronzeSteamPipeBlock.SOUTH, true),
            3
        )
        level.setBlock(
            gaugePos,
            ModBlocks.BRONZE_GAUGE_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.NORTH, true)
                .setValue(BronzeSteamPipeBlock.EAST, true),
            3
        )

        level.setBlock(chest2Pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 3)
        val chest2 = level.getBlockEntity(chest2Pos) as? ChestBlockEntity
        if (chest2 != null) {
            chest2.setItem(0, ItemStack(Items.WATER_BUCKET, 4))
            chest2.setItem(1, ItemStack(ModBlocks.BRONZE_STEAM_PIPE, 16))
            chest2.setItem(2, ItemStack(ModBlocks.BRONZE_VALVE_PIPE, 4))
            chest2.setItem(3, ItemStack(ModBlocks.BRONZE_GAUGE_PIPE, 4))
            chest2.setChanged()
        }

        // ==========================================
        // 4. ESTAÇÃO 3: FORJARIA MECÂNICA A VAPOR (X = +3)
        // ==========================================
        val pipeBridge1Pos = origin.offset(1, 2, 1)
        val pipeBridge2Pos = origin.offset(2, 2, 1)
        val pipeDropPos = origin.offset(3, 2, 1)
        val hammerPos = origin.offset(3, 1, 1)
        val pistonPos = origin.offset(3, 1, -1)
        val chest3Pos = origin.offset(3, 0, -2)

        level.setBlock(
            pipeBridge1Pos,
            ModBlocks.BRONZE_STEAM_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.WEST, true)
                .setValue(BronzeSteamPipeBlock.EAST, true),
            3
        )
        level.setBlock(
            pipeBridge2Pos,
            ModBlocks.BRONZE_STEAM_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.WEST, true)
                .setValue(BronzeSteamPipeBlock.EAST, true),
            3
        )
        level.setBlock(
            pipeDropPos,
            ModBlocks.BRONZE_STEAM_PIPE.defaultBlockState()
                .setValue(BronzeSteamPipeBlock.WEST, true)
                .setValue(BronzeSteamPipeBlock.DOWN, true),
            3
        )

        level.setBlock(
            hammerPos,
            ModBlocks.MECHANICAL_HAMMER.defaultBlockState()
                .setValue(MechanicalHammerBlock.FACING, Direction.NORTH),
            3
        )

        // Pre-place a bronze ingot on the hammer bed to showcase item rendering!
        val hammerBe = level.getBlockEntity(hammerPos) as? MechanicalHammerBlockEntity
        if (hammerBe != null) {
            hammerBe.itemOnBed = ItemStack(ModItems.BRONZE_INGOT)
            hammerBe.setChanged()
            level.sendBlockUpdated(hammerPos, level.getBlockState(hammerPos), level.getBlockState(hammerPos), 3)
        }

        level.setBlock(
            pistonPos,
            ModBlocks.STEAM_PISTON.defaultBlockState()
                .setValue(SteamPistonBlock.FACING, Direction.SOUTH),
            3
        )

        level.setBlock(chest3Pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH), 3)
        val chest3 = level.getBlockEntity(chest3Pos) as? ChestBlockEntity
        if (chest3 != null) {
            chest3.setItem(0, ItemStack(ModItems.BRONZE_INGOT, 16))
            chest3.setItem(1, ItemStack(Items.IRON_INGOT, 16))
            chest3.setItem(2, ItemStack(ModItems.WROUGHT_IRON_INGOT, 16))
            chest3.setItem(3, ItemStack(ModItems.BRONZE_PLATE, 16))
            chest3.setItem(4, ItemStack(Items.RAW_IRON, 16))
            chest3.setItem(5, ItemStack(Items.RAW_COPPER, 16))
            chest3.setItem(6, ItemStack(ModItems.RAW_TIN, 16))
            chest3.setChanged()
        }

        // ==========================================
        // 5. FEEDBACK AUDIOVISUAL E MENSAGENS
        // ==========================================
        level.playSound(null, origin, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.0f)
        level.playSound(null, origin, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8f, 1.2f)

        source.sendSuccess(
            {
                Component.literal(
                    """
                    §6===================================================§r
                    §e🏭 [INDUSTRY MADE] LABORATÓRIO DE TESTES CRIADO!§r
                    §6===================================================§r
                    §aTrês estações totalmente operacionais montadas à sua frente:§r
                    
                    §6[Estação 1 - Esquerda] Cadinho & Fole Térmico:§r
                      • Cadinho sobre fogo aceso pronto para fundir minérios.
                      • Fole manual ao lado (clique repetidamente para soprar oxigênio e elevar o calor a 1200°C).
                      • Amostras geológicas de §eMinério de Estanho§r e §eArgila Refratária§r na parede lateral.
                      • Baú com Cobre, Estanho, Argila Refratária, Moldes e Ferramentas de Bronze.
                    
                    §6[Estação 2 - Centro] Caldeira & Rede de Tubulação:§r
                      • Caldeira de Baixa Pressão pré-abastecida fervendo a 105°C com vapor ativo.
                      • Manômetro analógico no painel girando em tempo real.
                      • Tubulação com §eVálvula Manual§r (clique com botão direito para abrir/fechar o vapor).
                      • §eManômetro de Linha§r (mostrador em 3D e clique para ler a pressão exata em bar).
                    
                    §6[Estação 3 - Direita] Forjaria Mecânica a Vapor:§r
                      • Martelo Mecânico conectado à linha de vapor via duto superior.
                      • Lingote de Bronze já repousando na bigorna para demonstração visual!
                      • Baú de Conformação com lingotes e minérios para dobrar rendimento.
                    
                    §bDica: Use §f/im kit§b para receber o kit completo no inventário!§r
                    §6===================================================§r
                    """.trimIndent()
                )
            },
            false
        )

        return 1
    }

    private fun executeKit(source: CommandSourceStack): Int {
        val player = source.player ?: run {
            source.sendFailure(Component.literal("§cEste comando só pode ser executado por um jogador.§r"))
            return 0
        }

        fun give(stack: ItemStack) {
            if (!player.inventory.add(stack)) {
                player.drop(stack, false)
            }
        }

        give(ItemStack(ModBlocks.CRUCIBLE, 2))
        give(ItemStack(ModBlocks.BELLOWS, 2))
        give(ItemStack(ModBlocks.REFRACTORY_BRICKS, 16))
        give(ItemStack(ModBlocks.LOW_PRESSURE_BOILER, 2))
        give(ItemStack(ModBlocks.BRONZE_STEAM_PIPE, 32))
        give(ItemStack(ModBlocks.BRONZE_VALVE_PIPE, 4))
        give(ItemStack(ModBlocks.BRONZE_GAUGE_PIPE, 4))
        give(ItemStack(ModBlocks.STEAM_PISTON, 2))
        give(ItemStack(ModBlocks.MECHANICAL_HAMMER, 2))
        give(ItemStack(ModBlocks.TIN_ORE, 16))
        give(ItemStack(ModBlocks.DEEPSLATE_TIN_ORE, 16))
        give(ItemStack(ModBlocks.FIRE_CLAY_BLOCK, 16))
        give(ItemStack(ModItems.RAW_TIN, 32))
        give(ItemStack(ModItems.TIN_INGOT, 32))
        give(ItemStack(ModItems.FIRE_CLAY, 32))
        give(ItemStack(ModItems.CERAMIC_MOLD, 8))
        give(ItemStack(ModItems.BRONZE_INGOT, 16))
        give(ItemStack(ModItems.WROUGHT_IRON_INGOT, 16))
        give(ItemStack(ModItems.BRONZE_PICKAXE, 1))
        give(ItemStack(ModItems.BRONZE_SHOVEL, 1))
        give(ItemStack(Items.WATER_BUCKET, 2))
        give(ItemStack(Items.CAMPFIRE, 4))

        source.sendSuccess(
            {
                Component.literal("§a[Industry Made] Kit completo de engenharia, mineração e metalurgia adicionado ao seu inventário!§r")
            },
            false
        )
        return 1
    }

    private fun executeClear(source: CommandSourceStack): Int {
        val player = source.player ?: return 0
        val level = source.level
        val origin = player.blockPosition().relative(player.direction, 4)

        for (dx in -4..4) {
            for (dz in -3..3) {
                for (dy in -1..5) {
                    level.setBlock(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3)
                }
            }
        }

        source.sendSuccess(
            {
                Component.literal("§e[Industry Made] Área do laboratório de testes limpa com sucesso.§r")
            },
            false
        )
        return 1
    }

    private fun executeHelp(source: CommandSourceStack): Int {
        source.sendSuccess(
            {
                Component.literal(
                    """
                    §6=== COMANDOS INDUSTRY MADE ===§r
                    §e/im testlab§r ou §e/industrymade testlab§r:
                      Gera uma plataforma completa de laboratório de testes à sua frente com cadinho, caldeira, válvulas, manômetros e martelo a vapor já conectados.
                    §e/im kit§r ou §e/industrymade kit§r:
                      Entrega no seu inventário todas as máquinas, tubulações, válvulas, moldes e lingotes do mod.
                    §e/im clear§r:
                      Limpa a área da frente onde o laboratório foi gerado.
                    """.trimIndent()
                )
            },
            false
        )
        return 1
    }
}
