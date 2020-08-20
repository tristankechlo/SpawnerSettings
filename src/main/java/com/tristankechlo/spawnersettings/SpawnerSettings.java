package com.tristankechlo.spawnersettings;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tristankechlo.spawnersettings.client.ClientSetup;
import com.tristankechlo.spawnersettings.config.SpawnerSettingsConfig;
import com.tristankechlo.spawnersettings.eventhandler.MobDropHandler;
import com.tristankechlo.spawnersettings.eventhandler.SpawnerEventHandler;
import com.tristankechlo.spawnersettings.init.ModContainerTypes;
import com.tristankechlo.spawnersettings.network.SpawnerSettingsPacketHandler;
import com.tristankechlo.spawnersettings.util.Reference;

@Mod(SpawnerSettings.MOD_ID)
public class SpawnerSettings{
	
    public static SpawnerSettings instance;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "spawnersettings";

    public SpawnerSettings() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ClientSetup::init);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SpawnerSettingsConfig.spec);
        
        SpawnerSettingsPacketHandler.registerPackets();
        
		ModContainerTypes.CONTAINER_TYPES.register(modEventBus);
		
		//EventHandler
        MinecraftForge.EVENT_BUS.register(new SpawnerEventHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());   


        SpawnerSettings.instance = this;
        MinecraftForge.EVENT_BUS.register((Object)this);
    }

    private void commonSetup(FMLCommonSetupEvent evt) {
    	Reference.improvedVanillaLoaded = ModList.get().isLoaded("improvedvanilla");
    }
}
