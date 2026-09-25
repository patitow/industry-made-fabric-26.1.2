package dev.patitow.industrymade.client

import dev.patitow.industrymade.client.render.BellowsBlockEntityRenderer
import dev.patitow.industrymade.client.render.CrucibleBlockEntityRenderer
import dev.patitow.industrymade.client.render.LowPressureBoilerBlockEntityRenderer
import dev.patitow.industrymade.client.render.MechanicalHammerBlockEntityRenderer
import dev.patitow.industrymade.client.render.SteamPistonBlockEntityRenderer
import dev.patitow.industrymade.init.ModBlockEntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry

object IndustryMadeClient : ClientModInitializer {
    override fun onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(BellowsBlockEntityRenderer.LAYER_LOCATION) {
            BellowsBlockEntityRenderer.createLayerDefinition()
        }
        ModelLayerRegistry.registerModelLayer(CrucibleBlockEntityRenderer.LAYER_LOCATION) {
            CrucibleBlockEntityRenderer.createLayerDefinition()
        }
        ModelLayerRegistry.registerModelLayer(LowPressureBoilerBlockEntityRenderer.LAYER_LOCATION) {
            LowPressureBoilerBlockEntityRenderer.createLayerDefinition()
        }
        ModelLayerRegistry.registerModelLayer(SteamPistonBlockEntityRenderer.LAYER_LOCATION) {
            SteamPistonBlockEntityRenderer.createLayerDefinition()
        }
        ModelLayerRegistry.registerModelLayer(MechanicalHammerBlockEntityRenderer.LAYER_LOCATION) {
            MechanicalHammerBlockEntityRenderer.createLayerDefinition()
        }

        BlockEntityRendererRegistry.register(ModBlockEntities.BELLOWS, ::BellowsBlockEntityRenderer)
        BlockEntityRendererRegistry.register(ModBlockEntities.CRUCIBLE, ::CrucibleBlockEntityRenderer)
        BlockEntityRendererRegistry.register(ModBlockEntities.LOW_PRESSURE_BOILER, ::LowPressureBoilerBlockEntityRenderer)
        BlockEntityRendererRegistry.register(ModBlockEntities.STEAM_PISTON, ::SteamPistonBlockEntityRenderer)
        BlockEntityRendererRegistry.register(ModBlockEntities.MECHANICAL_HAMMER, ::MechanicalHammerBlockEntityRenderer)
    }
}