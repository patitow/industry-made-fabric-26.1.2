package dev.patitow.industrymade.thermal

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

/**
 * Interface implemented by blocks or block entities that can receive forced air/oxygen blasts
 * (e.g. from bellows) to increase combustion rate, heat, and reaction speed.
 */
fun interface OxygenReceiver {
    /**
     * Called when an air blast hits this block or entity.
     * @param level The current level
     * @param pos The block position of this receiver
     * @param fromDirection The direction the air came from
     * @param intensity Blast strength (1.0f for manual bellows)
     */
    fun receiveAirBlast(level: Level, pos: BlockPos, fromDirection: Direction, intensity: Float)
}

/**
 * Interface for refractory blocks that provide thermal insulation to adjacent heat chambers.
 */
interface ThermalInsulator {
    /**
     * Fraction of heat preserved / loss prevented (from 0.0 to 1.0).
     */
    val insulationFactor: Float
}
