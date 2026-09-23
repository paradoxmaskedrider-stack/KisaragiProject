package com.github.kisaragimikoto.tacticalbackpack;

import com.github.kisaragimikoto.tacticalbackpack.compat.ModCompatRegistry;
import com.github.kisaragimikoto.tacticalbackpack.compat.ae2.AE2Integration;
import com.github.kisaragimikoto.tacticalbackpack.compat.cctweaked.CCTweakedIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.cobblemon.CobblemonIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.create.CreateIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.curios.CuriosIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.epicfight.EpicFightIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.farmersdelight.FarmersDelightIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.gunpack.GunPackManager;
import com.github.kisaragimikoto.tacticalbackpack.compat.l2.L2Integration;
import com.github.kisaragimikoto.tacticalbackpack.compat.mekanism.MekanismIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.nightfall.NightfallIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.slashblade.SlashBladeIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.tacz.TaczIntegration;
import com.github.kisaragimikoto.tacticalbackpack.compat.tconstruct.TinkersIntegration;
import com.github.kisaragimikoto.tacticalbackpack.config.BackpackConfig;
import com.github.kisaragimikoto.tacticalbackpack.network.ModNetwork;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModCreativeTabs;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModItems;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModMenus;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModSounds;
import com.github.kisaragimikoto.tacticalbackpack.registry.ModBlocks;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TacticalBackpack.MODID)
public class TacticalBackpack {

    public static final String MODID = "tacticalbackpack";

    public TacticalBackpack() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);

        // Sound 登録
        ModSounds.SOUNDS.register(modEventBus);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                BackpackConfig.SPEC
        );

        registerCore();
        registerCompat();
    }

    private void registerCore() {
        ModNetwork.register();
    }

    private void registerCompat() {
        ModCompatRegistry.init();
        GunPackManager.init();

        EpicFightIntegration.init();
        NightfallIntegration.init();
        TinkersIntegration.init();
        MekanismIntegration.init();
        AE2Integration.init();
        CreateIntegration.init();
        FarmersDelightIntegration.init();
        CCTweakedIntegration.init();

        CuriosIntegration.init();
        L2Integration.init();
        SlashBladeIntegration.init();
        TaczIntegration.init();

        CobblemonIntegration.init();
    }
}