package dev.patitow.industrymade.client

import net.fabricmc.api.ClientModInitializer

object IndustryMadeClient : ClientModInitializer {
    override fun onInitializeClient() {
        // Blocks are now rendered natively by Minecraft's chunk model engine
        // using our Blockbench-compliant 3D models and multi-material textures.
    }
}