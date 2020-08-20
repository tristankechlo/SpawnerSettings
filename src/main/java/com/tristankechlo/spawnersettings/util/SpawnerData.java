package com.tristankechlo.spawnersettings.util;

import java.util.HashMap;
import java.util.Map;

import com.tristankechlo.spawnersettings.network.SpawnerSettingsPacketHandler;
import com.tristankechlo.spawnersettings.network.packet.SyncCurrentSpawnerDataToClient;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;

public class SpawnerData {
	
	public static BlockPos pos;
	public static short delay;
	public static short minSpawnDelay;
	public static short maxSpawnDelay;
	public static short spawnCount;
	public static short maxNearbyEntities;
	public static short requiredPlayerRange;

	public SpawnerData(BlockPos pos, short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange) {
		SpawnerData.pos = pos;
		SpawnerData.delay = delay;
		SpawnerData.minSpawnDelay = minSpawnDelay;
		SpawnerData.maxSpawnDelay = maxSpawnDelay;
		SpawnerData.spawnCount = spawnCount;
		SpawnerData.maxNearbyEntities = maxNearbyEntities;
		SpawnerData.requiredPlayerRange = requiredPlayerRange;
	}
	

	
	public static ListNBT createSpawnPotentials(Inventory inventory) {
		
		Map<String, Integer> spawn_potentials_map = new HashMap<>();
		
		for(int i = 0; i < 9; i++) {
			Item item = inventory.getStackInSlot(i).getItem();
			int weight = inventory.getStackInSlot(i).getCount();
			if(item == Items.AIR || weight < 1) {
				continue;
			}
			if(!(item instanceof SpawnEggItem)) {
				continue;
			}
			SpawnEggItem spawnegg = (SpawnEggItem)item;
			String entity_name = spawnegg.getType(null).getRegistryName().toString();
	
			if(spawn_potentials_map.containsKey(entity_name)) {
				int before = spawn_potentials_map.get(entity_name);
				int current = before + weight;
				if(current > 64) {
					current = 64;
				}
				spawn_potentials_map.put(entity_name, current);
			} else {
				spawn_potentials_map.put(entity_name, weight);
			}
		    
		}

		ListNBT spawn_potentials = new ListNBT();
		
		if(spawn_potentials_map.isEmpty()) {
		    CompoundNBT entity = new CompoundNBT();
		    entity.putString("id", EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString());
		    
		    CompoundNBT entry = new CompoundNBT();
		    entry.put("Entity", entity);
		    entry.putInt("Weight", 1);
		    
		    spawn_potentials.add(entry);
			
		} else {
			for (Map.Entry<String, Integer> spawn_potential : spawn_potentials_map.entrySet()) {
				String entity_name = spawn_potential.getKey();
				int weight = spawn_potential.getValue();
			    CompoundNBT entity = new CompoundNBT();
			    entity.putString("id", entity_name);
			    
			    CompoundNBT entry = new CompoundNBT();
			    entry.put("Entity", entity);
			    entry.putInt("Weight", weight);
			    
			    spawn_potentials.add(entry);
			}
		}
		
		return spawn_potentials;
	}
	
	public static CompoundNBT createSpawnerNBT(short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putShort("Delay", delay);
		nbt.putShort("MinSpawnDelay", minSpawnDelay);
		nbt.putShort("MaxSpawnDelay", maxSpawnDelay);
		nbt.putShort("SpawnCount", spawnCount);
		nbt.putShort("MaxNearbyEntities", maxNearbyEntities);
		nbt.putShort("RequiredPlayerRange", requiredPlayerRange);
			
		return nbt;
	}
	
	public static void sendSpawnerDataToClient(final ServerPlayerEntity player, final World world, final BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if(!(tile instanceof MobSpawnerTileEntity)) {
			return;
		}
		AbstractSpawner spawner = ((MobSpawnerTileEntity)tile).getSpawnerBaseLogic();
		CompoundNBT nbt = new CompoundNBT();
		nbt = spawner.write(nbt);
				
		short delay = nbt.getShort("Delay");
		short minSpawnDelay = nbt.getShort("MinSpawnDelay");
		short maxSpawnDelay = nbt.getShort("MaxSpawnDelay");
		short spawnCount = nbt.getShort("SpawnCount");
		short maxNearbyEntities = nbt.getShort("MaxNearbyEntities");
		short requieredPlayRange = nbt.getShort("RequiredPlayerRange");
		
		SpawnerSettingsPacketHandler.INSTANCE.sendTo(
			new SyncCurrentSpawnerDataToClient(pos, delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requieredPlayRange),
			player.connection.getNetworkManager(), 
			NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static Inventory getInvfromSpawner (final World world, final BlockPos pos) {
		
		Inventory inv = new Inventory(9);

		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			
			final TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof MobSpawnerTileEntity)) {
				return inv;
			}
			final AbstractSpawner logic = ((MobSpawnerTileEntity)tile).getSpawnerBaseLogic();
			CompoundNBT nbt = new CompoundNBT();
			nbt = logic.write(nbt);

		    if (nbt.contains("SpawnPotentials", 9)) {
		    	ListNBT listnbt = nbt.getList("SpawnPotentials", 10);
		    	int min = Math.min(8, listnbt.size());
		    			    	
		        for(int i = 0; i < min; ++i) {
		        	CompoundNBT entry = listnbt.getCompound(i);
		        	String entity = entry.getCompound("Entity").toString();
		        	entity = entity.substring(entity.indexOf("\"") + 1);
		    		entity = entity.substring(0, entity.indexOf("\""));
		        	int weight = entry.getShort("Weight");
		    		if (entity.equalsIgnoreCase(EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString())) {
		    			continue;
		    		}
		    		final ItemStack itemStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entity + "_spawn_egg")), weight);
		    		inv.setInventorySlotContents(i, itemStack);
		        }
		    }
		}
		
		return inv;
	}
	
	public static void dropMonsterEggs(final World world, final BlockPos pos, int eggDropChance) {
		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			
			final Inventory inv = getInvfromSpawner(world, pos);
			
			if (eggDropChance < 1 || eggDropChance > 100) {
				eggDropChance = 100;
			}
			
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				
				Item item = inv.getStackInSlot(i).getItem();
				int weight = inv.getStackInSlot(i).getCount();
				
				if(item == Items.AIR || weight < 1) {
					continue;
				}

				if (Math.random() < ((double) eggDropChance / 100)) {
					final ItemEntity entityItem = new ItemEntity(world, pos.getX(), (pos.getY() + 1.0f), pos.getZ(), inv.getStackInSlot(i));
					world.addEntity(entityItem);
				}
			}
		}
	}
	
	public static void resetSpawner(final World world, final BlockPos pos) {
		if(world.getBlockState(pos).getBlock().equals(Blocks.SPAWNER)) {
			world.removeTileEntity(pos);
			world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
			MobSpawnerTileEntity tile = (MobSpawnerTileEntity) world.getTileEntity(pos);
			
			CompoundNBT entity = new CompoundNBT();
			entity.putString("id", "minecraft:area_effect_cloud");
			
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Entity", entity);
			nbt.putInt("Weight", 1);
			
			final WeightedSpawnerEntity nextSpawnData = new WeightedSpawnerEntity(nbt);
			tile.getSpawnerBaseLogic().setNextSpawnData(nextSpawnData);
			tile.getSpawnerBaseLogic().setEntityType(EntityType.AREA_EFFECT_CLOUD);
			tile.markDirty();
	    	world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
		}
	}

}
