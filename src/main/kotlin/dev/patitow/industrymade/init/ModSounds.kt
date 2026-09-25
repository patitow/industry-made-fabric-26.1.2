package dev.patitow.industrymade.init

import dev.patitow.industrymade.IndustryMade
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent

object ModSounds {
    val BELLOWS_BLOW: SoundEvent = register("bellows_blow")
    val BELLOWS_PUMP: SoundEvent = register("bellows_pump")

    private fun register(name: String): SoundEvent {
        val id = IndustryMade.id(name)
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id))
    }

    fun initialize() {
        // Classloading trigger
    }
}
