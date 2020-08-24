package com.tristankechlo.spawnersettings.eventhandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import com.tristankechlo.spawnersettings.config.SpawnerSettingsConfig;
import com.tristankechlo.spawnersettings.util.Reference;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class MobDropHandler {

	@SubscribeEvent
	public void onMobDeath(final LivingDeathEvent event) {
		final LivingEntity entity = event.getEntityLiving();
		final World world = entity.world;
		if (!world.isRemote) {
			
			if(Reference.improvedVanillaLoaded) {
				return;
			}

			final boolean onlyWhenKilledByPlayer = SpawnerSettingsConfig.SERVER.dropOnlyWhenKilledByPlayer.get();
			final int dropchance = SpawnerSettingsConfig.SERVER.mobSpawnEggDropChance.get();
			final Entity source = event.getSource().getTrueSource();
			final EntityType<?> type = (EntityType<?>) entity.getType();
			final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg"));
			final ItemStack stack = new ItemStack(item, 1);
			
			if ((source instanceof ServerPlayerEntity) && onlyWhenKilledByPlayer) {
				//drop only when entity was killed by a player
				final ServerPlayerEntity player = (ServerPlayerEntity) source;
				if (player.isSpectator()) {
					return;
				}

				final int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, player.getHeldItemMainhand());
				final boolean lootingAffective = SpawnerSettingsConfig.SERVER.lootingAffective.get();

				if (dropchance >= 1 && dropchance <= 100) {
					
					if(lootingAffective) {
						// foreach lootinglevel there's an additional chance to drop the egg
						for (int i = 0; i < (1 + lootingLevel); i++) {
							if (Math.random() < ((double) dropchance / 100)) {
								entity.entityDropItem(stack);
							}
						}
					} else {						
						if (Math.random() < ((double) dropchance / 100)) {
							entity.entityDropItem(stack);
						}
					}

				}
			} else if (!onlyWhenKilledByPlayer) {
				//always try to drop the egg when entity dies
				if (dropchance >= 1 && dropchance <= 100) {
					if (Math.random() < ((double) dropchance / 100)) {
						entity.entityDropItem(stack);
					}
				}
			} else {
				return;
			}
		}
	}
}