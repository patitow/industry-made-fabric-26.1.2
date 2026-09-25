package dev.patitow.industrymade.init

import com.mojang.serialization.Codec
import dev.patitow.industrymade.IndustryMade
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs

object ModDataComponents {

    val MOLTEN_METAL: DataComponentType<String> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        IndustryMade.id("molten_metal"),
        DataComponentType.builder<String>()
            .persistent(Codec.STRING)
            .networkSynchronized(ByteBufCodecs.STRING_UTF8)
            .build()
    )

    val COOLING_TICKS: DataComponentType<Int> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        IndustryMade.id("cooling_ticks"),
        DataComponentType.builder<Int>()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build()
    )

    val COOL_DOWN_AT: DataComponentType<Long> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        IndustryMade.id("cool_down_at"),
        DataComponentType.builder<Long>()
            .persistent(Codec.LONG)
            .networkSynchronized(ByteBufCodecs.VAR_LONG)
            .build()
    )

    fun initialize() {
        // Trigger classloading
    }
}
