package dev.patitow.industrymade.thermal

/**
 * Interface implemented by blocks or block entities that produce, store, or transport steam pressure.
 */
interface SteamProvider {
    /**
     * Current steam pressure in bar (0.0 to max).
     */
    var steamPressure: Double

    /**
     * Returns current available pressure in bar.
     */
    fun getAvailablePressure(): Double = steamPressure

    /**
     * Draws an amount of pressure from this provider, returning the actual drawn pressure.
     */
    fun drawPressure(amount: Double): Double {
        val drawn = amount.coerceAtMost(steamPressure)
        steamPressure = (steamPressure - drawn).coerceAtLeast(0.0)
        return drawn
    }
}

/**
 * Interface implemented by blocks or machines that consume steam to perform work (e.g. pistons, hammers).
 */
interface SteamConsumer {
    /**
     * Minimum pressure in bar required to operate.
     */
    fun getMinimumOperatingPressure(): Double = 1.5

    /**
     * Attempts to consume steam from a provider.
     */
    fun tryConsumeSteam(provider: SteamProvider, amount: Double): Boolean {
        if (provider.getAvailablePressure() >= getMinimumOperatingPressure()) {
            provider.drawPressure(amount)
            return true
        }
        return false
    }
}
