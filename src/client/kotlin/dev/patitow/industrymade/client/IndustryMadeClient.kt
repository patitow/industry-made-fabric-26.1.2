package dev.patitow.industrymade.client

import dev.patitow.industrymade.client.render.CrucibleBlockEntityRenderer
import dev.patitow.industrymade.client.render.MechanicalHammerBlockEntityRenderer
import dev.patitow.industrymade.init.ModBlockEntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry

object IndustryMadeClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Dynamic molten fluid pool inside the Crucible
        BlockEntityRendererRegistry.register(
            ModBlockEntities.CRUCIBLE,
            ::CrucibleBlockEntityRenderer
        )

        // Item rendering resting on the Mechanical Hammer anvil
        BlockEntityRendererRegistry.register(
            ModBlockEntities.MECHANICAL_HAMMER,
            ::MechanicalHammerBlockEntityRenderer
        )
    }
}