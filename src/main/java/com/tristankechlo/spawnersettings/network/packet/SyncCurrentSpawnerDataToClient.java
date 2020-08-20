package com.tristankechlo.spawnersettings.network.packet;

import java.util.function.Supplier;

import com.tristankechlo.spawnersettings.util.SpawnerData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncCurrentSpawnerDataToClient {

	private final BlockPos pos;
	private final short delay;
	private final short minSpawnDelay;
	private final short maxSpawnDelay;
	private final short spawnCount;
	private final short maxNearbyEntities;
	private final short requiredPlayerRange;
	
	public SyncCurrentSpawnerDataToClient(BlockPos pos, short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange) {
		this.pos = pos;
		this.delay = delay;
		this.minSpawnDelay = minSpawnDelay;
		this.maxSpawnDelay = maxSpawnDelay;
		this.spawnCount = spawnCount;
		this.maxNearbyEntities = maxNearbyEntities;
		this.requiredPlayerRange = requiredPlayerRange;
	}
	
	public static void encode (SyncCurrentSpawnerDataToClient msg, PacketBuffer buffer) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeShort(msg.delay);
		buffer.writeShort(msg.minSpawnDelay);
		buffer.writeShort(msg.maxSpawnDelay);
		buffer.writeShort(msg.spawnCount);
		buffer.writeShort(msg.maxNearbyEntities);
		buffer.writeShort(msg.requiredPlayerRange);
	}
	
	public static SyncCurrentSpawnerDataToClient decode (PacketBuffer buffer) {
		BlockPos pos = buffer.readBlockPos();
		short delay = buffer.readShort();
		short minSpawnDelay = buffer.readShort();
		short maxSpawnDelay = buffer.readShort();
		short spawnCount = buffer.readShort();
		short maxNearbyEntities = buffer.readShort();
		short requiredPlayerRange = buffer.readShort();
		return new SyncCurrentSpawnerDataToClient(pos, delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange);
	}
	

	public static void handle (SyncCurrentSpawnerDataToClient msg, Supplier<NetworkEvent.Context> context) {
		
		context.get().enqueueWork(() -> {
				        
    		SpawnerData.pos = msg.pos;
    		SpawnerData.delay = msg.delay;
    		SpawnerData.minSpawnDelay = msg.minSpawnDelay;
    		SpawnerData.maxSpawnDelay = msg.maxSpawnDelay;
    		SpawnerData.spawnCount = msg.spawnCount;
    		SpawnerData.maxNearbyEntities = msg.maxNearbyEntities;
    		SpawnerData.requiredPlayerRange = msg.requiredPlayerRange;
	        	
	    
		});
	    context.get().setPacketHandled(true);
	}
}
