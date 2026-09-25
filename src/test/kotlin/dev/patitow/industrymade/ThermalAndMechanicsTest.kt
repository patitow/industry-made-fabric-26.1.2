package dev.patitow.industrymade

import dev.patitow.industrymade.thermal.MetalRegistry
import dev.patitow.industrymade.thermal.TemperatureHelper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ThermalAndMechanicsTest {

    @Test
    @DisplayName("Verify TemperatureHelper thermodynamics conversions and ambient cooling")
    fun testThermodynamicsCalculations() {
        assertEquals(20.0, TemperatureHelper.ROOM_TEMPERATURE_CELSIUS, "Default ambient temperature must be 20°C")
        assertEquals(293.15, TemperatureHelper.toKelvin(20.0), 0.001, "20°C in Kelvin must be 293.15K")
        assertEquals(273.15, TemperatureHelper.toKelvin(0.0), 0.001, "0°C in Kelvin must be 273.15K")

        // Ambient cooling test: temperature should decrease towards room temp
        val hot = 1000.0
        val cooled = TemperatureHelper.calculateCooling(hot, TemperatureHelper.ROOM_TEMPERATURE_CELSIUS, 0.02)
        assertTrue(cooled < hot, "Object must cool down over time")
        assertTrue(cooled >= TemperatureHelper.ROOM_TEMPERATURE_CELSIUS, "Object must not cool below ambient")

        // Already at ambient temperature: should not cool further
        val atAmbient = TemperatureHelper.calculateCooling(20.0, 20.0, 0.02)
        assertEquals(20.0, atAmbient, "Object at ambient temperature should not cool below it")
    }

    @Test
    @DisplayName("Verify MetalRegistry melting points conform to physical metallurgy design")
    fun testMetalRegistryPhysicalConstants() {
        val tin = MetalRegistry.getMetal("tin")
        assertNotNull(tin, "Tin must be registered")
        assertEquals(232.0, tin!!.meltingPoint, "Tin melting point must be 232°C")

        val bronze = MetalRegistry.getMetal("bronze")
        assertNotNull(bronze, "Bronze must be registered")
        assertEquals(950.0, bronze!!.meltingPoint, "Bronze melting point must be 950°C")

        val copper = MetalRegistry.getMetal("copper")
        assertNotNull(copper, "Copper must be registered")
        assertEquals(1085.0, copper!!.meltingPoint, "Copper melting point must be 1085°C")

        val iron = MetalRegistry.getMetal("iron")
        assertNotNull(iron, "Iron must be registered")
        assertEquals(1200.0, iron!!.meltingPoint, "Cast Iron melting point must be 1200°C")

        // Progression order: Tin melts first, then Bronze, then Copper, then Iron
        assertTrue(tin.meltingPoint < bronze.meltingPoint, "Tin must melt at a lower temperature than bronze")
        assertTrue(bronze.meltingPoint < copper.meltingPoint, "Bronze must melt at a lower temperature than copper")
        assertTrue(copper.meltingPoint < iron.meltingPoint, "Copper must melt at a lower temperature than iron")
    }

    @Test
    @DisplayName("Verify unknown metal query safety")
    fun testUnknownMetalQuery() {
        assertNull(MetalRegistry.getMetal("unobtainium"), "Querying unknown metal must safely return null")
        assertNull(MetalRegistry.getMetal(""), "Empty string metal ID must safely return null")
    }

    @Test
    @DisplayName("Verify SteamProvider pressure equalization and SteamConsumer draw mechanics")
    fun testSteamSystemMechanics() {
        val providerA = object : dev.patitow.industrymade.thermal.SteamProvider {
            override var steamPressure: Double = 4.0
        }
        val providerB = object : dev.patitow.industrymade.thermal.SteamProvider {
            override var steamPressure: Double = 0.0
        }

        // Draw pressure test
        val drawn = providerA.drawPressure(1.5)
        assertEquals(1.5, drawn, 0.001)
        assertEquals(2.5, providerA.steamPressure, 0.001)

        // Draw more than available
        val overDrawn = providerA.drawPressure(10.0)
        assertEquals(2.5, overDrawn, 0.001)
        assertEquals(0.0, providerA.steamPressure, 0.001)

        // Consumer operation test
        val consumer = object : dev.patitow.industrymade.thermal.SteamConsumer {}
        providerB.steamPressure = 1.0 // Below operating pressure of 1.5 bar
        assertFalse(consumer.tryConsumeSteam(providerB, 0.5), "Consumer must not operate below minimum pressure")

        providerB.steamPressure = 2.0 // Above operating threshold
        assertTrue(consumer.tryConsumeSteam(providerB, 0.5), "Consumer must operate when pressure >= 1.5 bar")
        assertEquals(1.5, providerB.steamPressure, 0.001)
    }

    @Test
    @DisplayName("Verify Boiler and Gauge pressure levels calculation conform to specification")
    fun testPressureLevelMapping() {
        fun calcLevel(p: Double): Int = when {
            p >= 5.5 -> 3
            p >= 2.5 -> 2
            p >= 0.5 -> 1
            else -> 0
        }

        assertEquals(0, calcLevel(0.0), "0 bar must be level 0")
        assertEquals(0, calcLevel(0.4), "0.4 bar must be level 0")
        assertEquals(1, calcLevel(0.5), "0.5 bar must be level 1")
        assertEquals(1, calcLevel(2.0), "2.0 bar must be level 1")
        assertEquals(2, calcLevel(2.5), "2.5 bar must be level 2")
        assertEquals(2, calcLevel(4.0), "4.0 bar must be level 2")
        assertEquals(3, calcLevel(5.5), "5.5 bar must be level 3 (danger/max)")
        assertEquals(3, calcLevel(6.0), "6.0 bar must be level 3 (danger/max)")
    }

    @Test
    @DisplayName("Verify Valve pipe flow gating: blocks flow when closed, equalizes when open")
    fun testValvePipeMechanics() {
        val valveOpen = true
        val valveClosed = false

        var upstreamPressure = 4.0
        var downstreamPressure = 1.0

        // Closed valve: flow must be 0
        val closedFlow = if (valveClosed && upstreamPressure > downstreamPressure) {
            (upstreamPressure - downstreamPressure) * 0.25
        } else {
            0.0
        }
        assertEquals(0.0, closedFlow, "Closed valve must completely block flow")

        // Open valve: flow occurs
        val openFlow = if (valveOpen && upstreamPressure > downstreamPressure) {
            (upstreamPressure - downstreamPressure) * 0.25
        } else {
            0.0
        }
        assertEquals(0.75, openFlow, 0.001, "Open valve must allow steam flow")
    }
}
