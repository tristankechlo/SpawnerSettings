package com.tristankechlo.spawnersettings.network;

import com.tristankechlo.spawnersettings.SpawnerSettings;
import com.tristankechlo.spawnersettings.network.packet.SetNewSpawnerConfig;
import com.tristankechlo.spawnersettings.network.packet.SyncCurrentSpawnerDataToClient;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class SpawnerSettingsPacketHandler {
	
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(SpawnerSettings.MOD_ID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	public static void registerPackets() {

        int id = 0;

        INSTANCE.registerMessage(id++, 
        		SyncCurrentSpawnerDataToClient.class, 
        		SyncCurrentSpawnerDataToClient::encode, 
        		SyncCurrentSpawnerDataToClient::decode, 
        		SyncCurrentSpawnerDataToClient::handle);

        
        INSTANCE.registerMessage(id++, 
        		SetNewSpawnerConfig.class, 
        		SetNewSpawnerConfig::encode, 
        		SetNewSpawnerConfig::decode, 
        		SetNewSpawnerConfig::handle);
	}
}
