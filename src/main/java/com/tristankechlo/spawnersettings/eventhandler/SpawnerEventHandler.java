package com.tristankechlo.spawnersettings.eventhandler;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import com.tristankechlo.spawnersettings.config.SpawnerSettingsConfig;
import com.tristankechlo.spawnersettings.container.SpawnerContainer;
import com.tristankechlo.spawnersettings.util.SpawnerData;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraftforge.common.ToolType;
import net.minecraft.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

public class SpawnerEventHandler {

	private static final ITextComponent name = new TranslationTextComponent("container.spawner");
	
	@SubscribeEvent
	public void onSpawnerPlaced(final BlockEvent.NeighborNotifyEvent event) {
		final World world = (World) event.getWorld();
		final BlockPos pos = event.getPos();

		if (world.isRemote) {
			return;
		}
		
		final Block targetblock = world.getBlockState(pos).getBlock();
		
		if (targetblock == Blocks.SPAWNER) {
			
	    	world.setBlockState(pos, Blocks.SPAWNER.getDefaultState(), 2);
	    	TileEntity tileentity = world.getTileEntity(pos);	    	
	    	((MobSpawnerTileEntity) tileentity).getSpawnerBaseLogic().setEntityType(EntityType.AREA_EFFECT_CLOUD);
	    	tileentity.markDirty();
	    	world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
	    	
		}
		
	}

	@SubscribeEvent
	public void onPlayerRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final PlayerEntity player = event.getPlayer();
		final World world = event.getWorld();

		if (player == null || world == null) {
			return;
		}
		if (world.isRemote || player.isSpectator() || player.getActiveHand() != Hand.MAIN_HAND) {
			return;
		}
		
		final BlockPos pos = event.getPos();
		final Block targetblock = world.getBlockState(pos).getBlock();
		
		if (targetblock == Blocks.SPAWNER) {
			Item item = player.getHeldItemMainhand().getItem();
			if (item instanceof SpawnEggItem) {
				event.setCanceled(true);
				return;
			} else if(item instanceof Item && !(item instanceof BlockItem)) {
				SpawnerData.sendSpawnerDataToClient((ServerPlayerEntity)player, world, pos);
				Inventory inv = SpawnerData.getInvfromSpawner(world, pos);
				NetworkHooks.openGui((ServerPlayerEntity)player, this.getContainer(inv));
			} else {
				return;
			}
		}
	}

	private INamedContainerProvider getContainer(Inventory inv) {
		return new SimpleNamedContainerProvider((windowID, playerInv, playerEntity) -> {
			return new SpawnerContainer(windowID, playerInv, inv);
		}, name);
	}

	@SubscribeEvent
	public void onBlockBreackEvent(final BlockEvent.BreakEvent event) {
		final PlayerEntity player = event.getPlayer();
		final Block targetBlock = event.getState().getBlock();
		final World world = (World) event.getWorld();
		final BlockPos pos = event.getPos();

		if (targetBlock == Blocks.SPAWNER) {
			if (player.getHeldItemMainhand().getToolTypes().contains(ToolType.PICKAXE)) {
				if (!player.isCreative() && !player.isSpectator()) {
					
					final int fortuneLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
					final int silkTouchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand());
					
					if (silkTouchLevel >= 1) {
						
						event.setExpToDrop(0);
						final int spawnerDropChance = SpawnerSettingsConfig.SERVER.spawnerDropChance.get();
						
						if (spawnerDropChance >= 1 && spawnerDropChance <= 100) {
							if (Math.random() < ((double) spawnerDropChance / 100)) {
								final ItemStack stack = new ItemStack(Items.SPAWNER, 1);
								final ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
								world.addEntity(entity);
							}
						}

						int eggDropChance = SpawnerSettingsConfig.SERVER.spawnEggDropChanceOnSpawnerDestroyed.get();
						SpawnerData.dropMonsterEggs(world, pos, eggDropChance);
						
						SpawnerData.resetSpawner(world, pos);

					} else if (silkTouchLevel == 0 && fortuneLevel >= 1) {
						int exp = event.getExpToDrop();
						exp += fortuneLevel * 10;
						event.setExpToDrop(exp);
					}
				}
			} else {
				event.setExpToDrop(0);
			}
		}
	}

}