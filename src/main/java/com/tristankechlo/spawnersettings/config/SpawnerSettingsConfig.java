package com.tristankechlo.spawnersettings.config;

import com.tristankechlo.spawnersettings.SpawnerSettings;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber
public class SpawnerSettingsConfig {
	
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Server SERVER = new Server(BUILDER);
    public static final ForgeConfigSpec spec = BUILDER.build();
    
	public static class Server {

		public final IntValue spawnerDropChance;		
		public final IntValue spawnEggDropChanceOnSpawnerDestroyed;		
		public final IntValue mobSpawnEggDropChance;
		
		Server(ForgeConfigSpec.Builder builder){
            builder.comment("Server configuration settings")
                   .push("server");            
            
            spawnerDropChance = builder
            		.comment("Drop-chance for the spawner to drop itself when mined with a silk-touch pickaxe (default 100, 100 -> always, 0 -> never)")
            		.defineInRange("spawnerDropChance", 100, 0, 100);
            
            spawnEggDropChanceOnSpawnerDestroyed = builder
            		.comment("Drop-chance for each stack, in a spawner, in % (default 100, 100 -> always, 0 -> never)")
            		.defineInRange("spawnEggDropChanceOnSpawnerDestroyed", 100, 0, 100);         
                        
            mobSpawnEggDropChance = builder
            		.comment("Drop-chance for all mobs to drop their spawn-egg in % (default 1, 100 -> always, 0 -> never)")
            		.defineInRange("mobSpawnEggDropChance", 1, 0, 100);
            
            builder.pop();
		}
	}

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        SpawnerSettings.LOGGER.debug("Loaded config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
    	SpawnerSettings.LOGGER.debug("Config just got changed on the file system!");
    }
	
}
