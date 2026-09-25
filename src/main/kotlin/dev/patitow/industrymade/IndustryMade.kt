package dev.patitow.industrymade

import dev.patitow.industrymade.init.ModBlockEntities
import dev.patitow.industrymade.init.ModBlocks
import dev.patitow.industrymade.init.ModDataComponents
import dev.patitow.industrymade.init.ModItems
import dev.patitow.industrymade.init.ModSounds
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

object IndustryMade : ModInitializer {
	const val MOD_ID: String = "industry-made"

	private val LOGGER = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		LOGGER.info("Initializing Industry Made...")

		ModSounds.initialize()
		ModDataComponents.initialize()
		ModBlocks.initialize()
		ModBlockEntities.initialize()
		ModItems.initialize()
		dev.patitow.industrymade.command.TestLabCommand.register()

		LOGGER.info("Industry Made initialized successfully!")
	}

	fun id(path: String): Identifier
		= Identifier.fromNamespaceAndPath(MOD_ID, path)
}
