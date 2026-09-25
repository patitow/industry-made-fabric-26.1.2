package dev.patitow.industrymade

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

class AssetAndModelIntegrityTest {

    private val rootDir = File(".").canonicalFile
    private val assetsDir = File(rootDir, "src/main/resources/assets/industry-made")
    private val generatedAssetsDir = File(rootDir, "src/main/generated/assets/industry-made")

    private val expectedMachines = listOf(
        "crucible",
        "bellows",
        "low_pressure_boiler",
        "steam_piston",
        "mechanical_hammer"
    )

    @Test
    @DisplayName("Verify all 5 machine 3D models exist, parse correctly, and have valid voxel geometry")
    fun testMachineModelsIntegrity() {
        val modelsDir = File(assetsDir, "models/block")
        assertTrue(modelsDir.exists(), "Models directory must exist: ${modelsDir.path}")

        for (machine in expectedMachines) {
            val modelFile = File(modelsDir, "$machine.json")
            assertTrue(modelFile.exists(), "Model file must exist: ${modelFile.path}")

            val content = modelFile.readText()
            assertTrue(content.contains("\"elements\""), "Model $machine must contain elements array")
            assertTrue(content.contains("\"display\""), "Model $machine must define isometric display transforms")
            assertTrue(content.contains("\"gui\""), "Model $machine must define gui display transform")

            // Simple element validation: verify 'from' and 'to' coordinates exist and are valid numbers
            val fromMatches = Regex(""""from"\s*:\s*\[\s*(-?[\d.]+)\s*,\s*(-?[\d.]+)\s*,\s*(-?[\d.]+)\s*\]""").findAll(content)
            val toMatches = Regex(""""to"\s*:\s*\[\s*(-?[\d.]+)\s*,\s*(-?[\d.]+)\s*,\s*(-?[\d.]+)\s*\]""").findAll(content)

            val fromList = fromMatches.toList()
            val toList = toMatches.toList()

            assertEquals(fromList.size, toList.size, "Mismatched from and to counts in $machine")
            assertTrue(fromList.isNotEmpty(), "$machine must have at least one element")

            for (i in fromList.indices) {
                val fX = fromList[i].groupValues[1].toDouble()
                val fY = fromList[i].groupValues[2].toDouble()
                val fZ = fromList[i].groupValues[3].toDouble()

                val tX = toList[i].groupValues[1].toDouble()
                val tY = toList[i].groupValues[2].toDouble()
                val tZ = toList[i].groupValues[3].toDouble()

                // Element bounds must not be inverted
                assertTrue(fX <= tX, "Element $i in $machine has inverted X bounds: from $fX > to $tX")
                assertTrue(fY <= tY, "Element $i in $machine has inverted Y bounds: from $fY > to $tY")
                assertTrue(fZ <= tZ, "Element $i in $machine has inverted Z bounds: from $fZ > to $tZ")

                // Must stay within Minecraft's model engine limits (-16 to 32)
                assertTrue(fX >= -16.0 && tX <= 32.0, "Element $i X out of bounds in $machine")
                assertTrue(fY >= -16.0 && tY <= 32.0, "Element $i Y out of bounds in $machine")
                assertTrue(fZ >= -16.0 && tZ <= 32.0, "Element $i Z out of bounds in $machine")
            }
        }
    }

    @Test
    @DisplayName("Verify modern 26.x item definitions exist and link to 3D block models")
    fun testItemDefinitionsIntegrity() {
        val itemsDir = File(assetsDir, "items")
        assertTrue(itemsDir.exists(), "Items directory must exist: ${itemsDir.path}")

        for (machine in expectedMachines) {
            val itemFile = File(itemsDir, "$machine.json")
            assertTrue(itemFile.exists(), "Item definition must exist: ${itemFile.path}")

            val content = itemFile.readText()
            assertTrue(content.contains("minecraft:model"), "Item $machine must declare model type")
            assertTrue(
                content.contains("industry-made:block/$machine"),
                "Item $machine must reference industry-made:block/$machine"
            )
        }
    }

    @Test
    @DisplayName("Verify blockstate definitions exist for all machines")
    fun testBlockstatesIntegrity() {
        val blockstatesDir = File(assetsDir, "blockstates")
        assertTrue(blockstatesDir.exists(), "Blockstates directory must exist: ${blockstatesDir.path}")

        for (machine in expectedMachines) {
            val stateFile = File(blockstatesDir, "$machine.json")
            assertTrue(stateFile.exists(), "Blockstate must exist: ${stateFile.path}")

            val content = stateFile.readText()
            assertTrue(
                content.contains("industry-made:block/$machine"),
                "Blockstate $machine must reference block model"
            )
        }
    }

    @Test
    @DisplayName("Verify all block textures exist as valid PNG images with Minecraft power-of-two dimensions")
    fun testBlockTexturesIntegrity() {
        val texDir = File(assetsDir, "textures/block")
        assertTrue(texDir.exists(), "Block textures directory must exist: ${texDir.path}")

        for (machine in expectedMachines) {
            val texFile = File(texDir, "$machine.png")
            assertTrue(texFile.exists(), "Texture must exist: ${texFile.path}")
            assertTrue(texFile.length() > 0, "Texture must not be empty: ${texFile.path}")

            // Verify valid PNG header bytes (89 50 4E 47 0D 0A 1A 0A)
            val bytes = Files.readAllBytes(texFile.toPath())
            assertTrue(bytes.size >= 8, "Texture too small to be PNG")
            assertEquals(0x89.toByte(), bytes[0], "Invalid PNG signature")
            assertEquals(0x50.toByte(), bytes[1], "Invalid PNG signature")
            assertEquals(0x4E.toByte(), bytes[2], "Invalid PNG signature")
            assertEquals(0x47.toByte(), bytes[3], "Invalid PNG signature")

            // Read image and verify power-of-two resolution (e.g. 16x16, 32x32, 64x64)
            val img = ImageIO.read(texFile)
            assertNotNull(img, "ImageIO must decode texture: $machine.png")
            val w = img.width
            val h = img.height
            assertTrue(w in listOf(16, 32, 64), "Texture width must be standard 16, 32, or 64: was $w")
            assertTrue(h in listOf(16, 32, 64), "Texture height must be standard 16, 32, or 64: was $h")
        }
    }

    @Test
    @DisplayName("Verify every texture variable referenced across all 3D models resolves to an existing PNG file")
    fun testModelTextureVariablesResolve() {
        val modelsDir = File(assetsDir, "models/block")
        val texDir = File(assetsDir, "textures/block")

        for (machine in expectedMachines) {
            val modelFile = File(modelsDir, "$machine.json")
            val content = modelFile.readText()

            // Find all "industry-made:block/<name>"
            val matches = Regex(""""industry-made:block/([^"]+)"""").findAll(content)
            val referencedTextures = matches.map { it.groupValues[1] }.toSet()
            assertTrue(referencedTextures.isNotEmpty(), "Model $machine must reference at least one texture")

            for (texName in referencedTextures) {
                val texFile = File(texDir, "$texName.png")
                assertTrue(
                    texFile.exists(),
                    "Model $machine references missing texture '$texName.png' at ${texFile.path}"
                )
                assertTrue(texFile.length() > 0, "Texture $texName.png must not be empty")
            }
        }
    }

    @Test
    @DisplayName("Verify dual localization (en_us and pt_br) has zero unlocalized machine keys")
    fun testLocalizationCompleteness() {
        val enFile = File(generatedAssetsDir, "lang/en_us.json")
        val ptFile = File(generatedAssetsDir, "lang/pt_br.json")

        assertTrue(enFile.exists(), "en_us.json must exist")
        assertTrue(ptFile.exists(), "pt_br.json must exist")

        val enText = enFile.readText()
        val ptText = ptFile.readText()

        for (machine in expectedMachines) {
            // Verify both block.industry-made.<name> AND item.industry-made.<name> exist in English
            assertTrue(
                enText.contains("\"block.industry-made.$machine\""),
                "en_us missing block key for $machine"
            )
            assertTrue(
                enText.contains("\"item.industry-made.$machine\""),
                "en_us missing item key for $machine"
            )

            // Verify both block.industry-made.<name> AND item.industry-made.<name> exist in Portuguese
            assertTrue(
                ptText.contains("\"block.industry-made.$machine\""),
                "pt_br missing block key for $machine"
            )
            assertTrue(
                ptText.contains("\"item.industry-made.$machine\""),
                "pt_br missing item key for $machine"
            )
        }
    }

    @Test
    @DisplayName("Verify Bronze Steam Pipe 3D models, multipart blockstate, textures and localization")
    fun testBronzeSteamPipeIntegrity() {
        val modelsDir = File(assetsDir, "models/block")
        val pipeModels = listOf(
            "bronze_steam_pipe_core",
            "bronze_steam_pipe_arm",
            "bronze_steam_pipe_inventory"
        )

        for (m in pipeModels) {
            val modelFile = File(modelsDir, "$m.json")
            assertTrue(modelFile.exists(), "Pipe model $m must exist at ${modelFile.path}")
            val content = modelFile.readText()
            assertTrue(content.contains("\"elements\""), "Pipe model $m must contain elements")
            assertTrue(content.contains("industry-made:block/bronze_pipe"), "Pipe model $m must reference bronze_pipe texture")
        }

        // Verify inventory model has standard display transforms
        val invFile = File(modelsDir, "bronze_steam_pipe_inventory.json")
        val invContent = invFile.readText()
        assertTrue(invContent.contains("\"gui\""), "Inventory model must contain gui display")
        assertTrue(invContent.contains("\"ground\""), "Inventory model must contain ground display")

        // Verify item file points to inventory model
        val itemFile = File(assetsDir, "items/bronze_steam_pipe.json")
        assertTrue(itemFile.exists(), "Item file bronze_steam_pipe.json must exist")
        val itemContent = itemFile.readText()
        assertTrue(itemContent.contains("industry-made:block/bronze_steam_pipe_inventory"))

        // Verify multipart blockstate
        val stateFile = File(assetsDir, "blockstates/bronze_steam_pipe.json")
        assertTrue(stateFile.exists(), "Blockstate bronze_steam_pipe.json must exist")
        val stateContent = stateFile.readText()
        assertTrue(stateContent.contains("\"multipart\""), "Pipe blockstate must use multipart format")
        assertTrue(stateContent.contains("industry-made:block/bronze_steam_pipe_core"), "Blockstate must reference core")
        assertTrue(stateContent.contains("industry-made:block/bronze_steam_pipe_arm"), "Blockstate must reference arm")

        // Verify texture
        val texFile = File(assetsDir, "textures/block/bronze_pipe.png")
        assertTrue(texFile.exists(), "Texture bronze_pipe.png must exist")
        val img = ImageIO.read(texFile)
        assertNotNull(img, "ImageIO must decode bronze_pipe.png")
        assertEquals(16, img.width, "bronze_pipe.png width must be 16")
        assertEquals(16, img.height, "bronze_pipe.png height must be 16")

        // Verify dual localization
        val enFile = File(generatedAssetsDir, "lang/en_us.json")
        val ptFile = File(generatedAssetsDir, "lang/pt_br.json")
        assertTrue(enFile.readText().contains("\"block.industry-made.bronze_steam_pipe\""))
        assertTrue(enFile.readText().contains("\"item.industry-made.bronze_steam_pipe\""))
        assertTrue(ptFile.readText().contains("\"block.industry-made.bronze_steam_pipe\""))
        assertTrue(ptFile.readText().contains("\"item.industry-made.bronze_steam_pipe\""))
    }
}

