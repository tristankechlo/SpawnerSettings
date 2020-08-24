package com.tristankechlo.spawnersettings.util;

import java.util.ArrayList;
import java.util.List;
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
	public static short spawnRange;

	public SpawnerData(BlockPos pos, short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange, short spawnRange) {
		SpawnerData.pos = pos;
		SpawnerData.delay = delay;
		SpawnerData.minSpawnDelay = minSpawnDelay;
		SpawnerData.maxSpawnDelay = maxSpawnDelay;
		SpawnerData.spawnCount = spawnCount;
		SpawnerData.maxNearbyEntities = maxNearbyEntities;
		SpawnerData.requiredPlayerRange = requiredPlayerRange;
		SpawnerData.spawnRange = spawnRange;
	}
	

	
	public static ListNBT createSpawnPotentials(Inventory inventory) {
		
		List<ItemStack> spawn_potentials_list = new ArrayList<>();
		
		for(int i = 0; i < inventory.getSizeInventory(); i++) {
			Item item = inventory.getStackInSlot(i).getItem();
			int weight = inventory.getStackInSlot(i).getCount();
			if(item == Items.AIR || weight < 1) {
				continue;
			}
			if(!(item instanceof SpawnEggItem)) {
				continue;
			}
			SpawnEggItem spawnegg = (SpawnEggItem)item;
	
			for (int j = 0; j < spawn_potentials_list.size(); j++){
				if(spawn_potentials_list.get(j).getItem() == spawnegg) {
					int before = spawn_potentials_list.get(j).getCount();
					if(before < 64) {
						int missing = 64 - before;
						if(weight >= missing) {
							spawn_potentials_list.get(j).setCount(64);
							weight = weight - missing;
						} else {
							spawn_potentials_list.get(j).setCount(before + weight);
							weight = 0;
						}
					}
				}
			}
			
			if(weight > 0) {
				spawn_potentials_list.add(new ItemStack(spawnegg, weight));
			}
			
		}

		ListNBT spawn_potentials = new ListNBT();
		
		if(spawn_potentials_list.isEmpty()) {
		    CompoundNBT entity = new CompoundNBT();
		    entity.putString("id", EntityType.AREA_EFFECT_CLOUD.getRegistryName().toString());
		    
		    CompoundNBT entry = new CompoundNBT();
		    entry.put("Entity", entity);
		    entry.putInt("Weight", 1);
		    
		    spawn_potentials.add(entry);
			
		} else {
			for (ItemStack stack : spawn_potentials_list) {
				String entity_name = ((SpawnEggItem) stack.getItem()).getType(null).getRegistryName().toString();
				int weight = stack.getCount();
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
	
	public static CompoundNBT createSpawnerNBT(short delay, short minSpawnDelay, short maxSpawnDelay, short spawnCount, short maxNearbyEntities, short requiredPlayerRange, short spawnRange) {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putShort("Delay", delay);
		nbt.putShort("MinSpawnDelay", minSpawnDelay);
		nbt.putShort("MaxSpawnDelay", maxSpawnDelay);
		nbt.putShort("SpawnCount", spawnCount);
		nbt.putShort("MaxNearbyEntities", maxNearbyEntities);
		nbt.putShort("RequiredPlayerRange", requiredPlayerRange);
		nbt.putShort("SpawnRange", spawnRange);
			
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
		short spawnRange = nbt.getShort("SpawnRange");
		
		SpawnerSettingsPacketHandler.INSTANCE.sendTo(
			new SyncCurrentSpawnerDataToClient(pos, delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requieredPlayRange, spawnRange),
			player.connection.getNetworkManager(), 
			NetworkDirection.PLAY_TO_CLIENT);
	}
	
	public static Inventory getInvfromSpawner (final World world, final BlockPos pos) {
		
		Inventory inv = new Inventory(11);

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
		    	int min = Math.min(11, listnbt.size());
		    	
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
