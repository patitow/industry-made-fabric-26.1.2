package dev.patitow.industrymade.client

import dev.patitow.industrymade.client.render.BellowsBlockEntityRenderer
import dev.patitow.industrymade.client.render.CrucibleBlockEntityRenderer
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
        BlockEntityRendererRegistry.register(ModBlockEntities.BELLOWS, ::BellowsBlockEntityRenderer)
        BlockEntityRendererRegistry.register(ModBlockEntities.CRUCIBLE, ::CrucibleBlockEntityRenderer)
    }
}