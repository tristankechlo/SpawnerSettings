package com.tristankechlo.spawnersettings.client;

import com.tristankechlo.spawnersettings.SpawnerSettings;
import com.tristankechlo.spawnersettings.client.screen.SpawnerScreen;
import com.tristankechlo.spawnersettings.init.ModContainerTypes;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SpawnerSettings.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    public static void init(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainerTypes.SPAWNER_CONTAINER.get(), SpawnerScreen::new);
    }
}
