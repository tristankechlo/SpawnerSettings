package com.tristankechlo.spawnersettings.network.packet;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetNewSpawnerConfig {

	private final BlockPos pos;
	private final CompoundNBT mob_spawner_nbt;
	
	public SetNewSpawnerConfig(BlockPos pos, CompoundNBT mob_spawner_nbt) {
		this.pos = pos;
		this.mob_spawner_nbt = mob_spawner_nbt;
	}
	
	public static void encode (SetNewSpawnerConfig msg, PacketBuffer buffer) {
		buffer.writeBlockPos(msg.pos);
		buffer.writeCompoundTag(msg.mob_spawner_nbt);
	}
	
	public static SetNewSpawnerConfig decode (PacketBuffer buffer) {
		BlockPos pos = buffer.readBlockPos();
		CompoundNBT mob_spawner_nbt = buffer.readCompoundTag();
		return new SetNewSpawnerConfig(pos, mob_spawner_nbt);
	}
	
	@SuppressWarnings("deprecation")
	public static void handle (SetNewSpawnerConfig msg, Supplier<NetworkEvent.Context> context) {
		
		context.get().enqueueWork(() -> {
						
			ServerPlayerEntity player = context.get().getSender();
	        ServerWorld world = player.getServerWorld();
	        
	        if(world != null) {
	        	if(world.isBlockLoaded(msg.pos)){
	        		TileEntity tile = world.getTileEntity(msg.pos);
	        		if(tile instanceof MobSpawnerTileEntity) {
	        			MobSpawnerTileEntity spawner_tile = (MobSpawnerTileEntity)tile;
	        			AbstractSpawner spawner = spawner_tile.getSpawnerBaseLogic();
	        			spawner.read(msg.mob_spawner_nbt);
	        			spawner_tile.markDirty();
	        			world.notifyBlockUpdate(msg.pos, world.getBlockState(msg.pos), world.getBlockState(msg.pos), 3);
	        		}
	        	}
	        }
	    
		});
	    context.get().setPacketHandled(true);
	}

}
