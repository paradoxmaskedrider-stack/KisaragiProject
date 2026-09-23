package com.github.kisaragimikoto.tacticalbackpack.client.gunpack;

import com.github.kisaragimikoto.tacticalbackpack.TacticalBackpack;
import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Client entry point for opening the unified Gun Pack Manager. */
public final class GunPackClientEvents {
    public static final KeyMapping OPEN_GUN_PACK_MANAGER = new KeyMapping(
            "key.tacticalbackpack.open_gunpack_manager",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.tacticalbackpack"
    );

    @Mod.EventBusSubscriber(modid = TacticalBackpack.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_GUN_PACK_MANAGER);
        }
    }

    @Mod.EventBusSubscriber(modid = TacticalBackpack.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return;
            while (OPEN_GUN_PACK_MANAGER.consumeClick()) {
                if (BackpackConfig.ENABLE_GUN_PACK_MANAGER.get()) {
                    minecraft.setScreen(new GunPackManagerScreen());
                }
            }
        }
    }

    private GunPackClientEvents() { }
}
