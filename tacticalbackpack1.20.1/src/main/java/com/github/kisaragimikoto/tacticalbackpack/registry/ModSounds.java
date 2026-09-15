package com.github.kisaragimikoto.tacticalbackpack.registry;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpackMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TacticalBackpackMod.MODID);

    public static final RegistryObject<SoundEvent> JETPACK_START = register("jetpack_start");
    public static final RegistryObject<SoundEvent> JETPACK_LOOP = register("jetpack_loop");
    public static final RegistryObject<SoundEvent> JETPACK_BOOST = register("jetpack_boost");

    // 🔥 追加
    public static final RegistryObject<SoundEvent> JETPACK_STOP = register("jetpack_stop");
    public static final RegistryObject<SoundEvent> JETPACK_OVERHEAT = register("jetpack_overheat");

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(TacticalBackpackMod.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}