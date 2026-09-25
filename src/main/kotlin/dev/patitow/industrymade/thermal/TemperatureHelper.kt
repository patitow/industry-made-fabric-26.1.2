package dev.patitow.industrymade.thermal

object TemperatureHelper {
    const val ROOM_TEMPERATURE_CELSIUS: Double = 20.0
    const val COPPER_MELTING_POINT: Double = 1085.0
    const val TIN_MELTING_POINT: Double = 232.0
    const val BRONZE_MELTING_POINT: Double = 950.0
    const val CAST_IRON_MELTING_POINT: Double = 1200.0
    const val WROUGHT_IRON_FORGE_TEMP: Double = 900.0

    /**
     * Converts Celsius to Kelvin.
     */
    fun toKelvin(celsius: Double): Double = celsius + 273.15

    /**
     * Calculates ambient cooling towards room temperature.
     */
    fun calculateCooling(currentTemp: Double, ambientTemp: Double = ROOM_TEMPERATURE_CELSIUS, coolingRate: Double = 0.02): Double {
        if (currentTemp <= ambientTemp) return currentTemp
        return currentTemp - ((currentTemp - ambientTemp) * coolingRate).coerceAtLeast(0.1)
    }
}
