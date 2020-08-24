package com.tristankechlo.spawnersettings.network.packet;

import java.util.function.Supplier;

import com.tristankechlo.spawnersettings.util.SpawnerData;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.network.NetworkEvent;

public class SyncNewSpawnerConfigToServer {

	private final BlockPos pos;
	private final short delay;
	private final short minSpawnDelay;
	private final short maxSpawnDelay;
	private final short spawnCount;
	private final short maxNearbyEntities;
	private final short requiredPlayerRange;
	private final short spawnRange;
	
	public SyncNewSpawnerConfigToServer(BlockPos pos, short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange, short spawnRange) {
		this.pos = pos;
		this.delay = delay;
		this.minSpawnDelay = minSpawnDelay;
		this.maxSpawnDelay = maxSpawnDelay;
		this.spawnCount = spawnCount;
		this.maxNearbyEntities = maxNearbyEntities;
		this.requiredPlayerRange = requiredPlayerRange;
		this.spawnRange = spawnRange;
	}
	
	public static void encode (SyncNewSpawnerConfigToServer msg, PacketBuffer buffer) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeShort(msg.delay);
		buffer.writeShort(msg.minSpawnDelay);
		buffer.writeShort(msg.maxSpawnDelay);
		buffer.writeShort(msg.spawnCount);
		buffer.writeShort(msg.maxNearbyEntities);
		buffer.writeShort(msg.requiredPlayerRange);
		buffer.writeShort(msg.spawnRange);
	}
	
	public static SyncNewSpawnerConfigToServer decode (PacketBuffer buffer) {
		BlockPos pos = buffer.readBlockPos();
		short delay = buffer.readShort();
		short minSpawnDelay = buffer.readShort();
		short maxSpawnDelay = buffer.readShort();
		short spawnCount = buffer.readShort();
		short maxNearbyEntities = buffer.readShort();
		short requiredPlayerRange = buffer.readShort();
		short spawnRange = buffer.readShort();
		return new SyncNewSpawnerConfigToServer(pos, delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange, spawnRange);
	}
	
	@SuppressWarnings("deprecation")
	public static void handle (SyncNewSpawnerConfigToServer msg, Supplier<NetworkEvent.Context> context) {
		
		context.get().enqueueWork(() -> {
			
			ServerPlayerEntity player = context.get().getSender();
	        ServerWorld world = player.getServerWorld();
	        
	        if(world != null) {
	        	if(world.isBlockLoaded(msg.pos)){
	        		TileEntity tile = world.getTileEntity(msg.pos);
	        		if(tile instanceof MobSpawnerTileEntity) {
	        			MobSpawnerTileEntity spawner_tile = (MobSpawnerTileEntity)tile;
	        			AbstractSpawner spawner = spawner_tile.getSpawnerBaseLogic();
	        			CompoundNBT nbt = SpawnerData.createSpawnerNBT(msg.delay, msg.minSpawnDelay, msg.maxSpawnDelay, msg.spawnCount, msg.maxNearbyEntities, msg.requiredPlayerRange, msg.spawnRange);
	        			spawner.read(nbt);
	        			spawner_tile.markDirty();
	        			world.notifyBlockUpdate(msg.pos, world.getBlockState(msg.pos), world.getBlockState(msg.pos), 3);
	        		}
	        	}
	        }
	    
		});
	    context.get().setPacketHandled(true);
	}

}
