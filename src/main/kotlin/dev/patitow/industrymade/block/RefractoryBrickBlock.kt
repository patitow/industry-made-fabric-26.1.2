package dev.patitow.industrymade.block

import dev.patitow.industrymade.thermal.ThermalInsulator
import net.minecraft.world.level.block.Block

class RefractoryBrickBlock(properties: Properties) : Block(properties), ThermalInsulator {
    override val insulationFactor: Float = 0.85f
}
