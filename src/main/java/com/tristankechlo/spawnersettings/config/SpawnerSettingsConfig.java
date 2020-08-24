package com.tristankechlo.spawnersettings.config;

import com.tristankechlo.spawnersettings.SpawnerSettings;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
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

		public final BooleanValue buttonSpeedActive;
		public final BooleanValue buttonCountActive;
		public final BooleanValue buttonPlayerRangeActive;
		public final BooleanValue buttonSpawnRangeActive;

		public final BooleanValue dropOnlyWhenKilledByPlayer;
		public final BooleanValue lootingAffective;
		public final IntValue mobSpawnEggDropChance;

		Server(ForgeConfigSpec.Builder builder) {

			builder.comment("Spawner related settings").push("Spawner");

			spawnerDropChance = builder.comment(
					"Drop-chance for the spawner to drop itself when mined with a silk-touch pickaxe (default 100, 100 -> always, 0 -> never)")
					.defineInRange("spawnerDropChance", 100, 0, 100);

			spawnEggDropChanceOnSpawnerDestroyed = builder
					.comment("Drop-chance for each stack, in a spawner, in % (default 100, 100 -> always, 0 -> never)")
					.defineInRange("spawnEggDropChanceOnSpawnerDestroyed", 100, 0, 100);

			builder.comment("buttons for the config gui can be deactivated").push("Buttons");

			buttonSpeedActive = builder.define("buttonSpeedActive", true);
			buttonCountActive = builder.define("buttonCountActive", true);
			buttonPlayerRangeActive = builder.define("buttonPlayerRangeActive", true);
			buttonSpawnRangeActive = builder.define("buttonSpawnRangeActive", true);
			
			builder.pop();
			builder.pop();

			builder.comment("Is not affective when ImprovedVanilla is installed").push("Mob-Drops");
            
            dropOnlyWhenKilledByPlayer = builder
            		.comment("If set to true, SpawnEggs only drop when the mob was killed by a player")
            		.define("dropOnlyWhenKilledByPlayer", true);
            
            lootingAffective = builder
            		.comment("If set to true, then foreach looting level on the players tool, there will by another possibility to drop the egg, only affective when \"dropOnlyWhenKilledByPlayer\" is set to true")
            		.define("lootingAffective", true);
            
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
