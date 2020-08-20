package com.tristankechlo.spawnersettings.eventhandler;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

import com.tristankechlo.spawnersettings.config.SpawnerSettingsConfig;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class MobDropHandler {

	@SubscribeEvent
	public void onMobDeath(final LivingDeathEvent event) {
		final LivingEntity entity = event.getEntityLiving();
		final World world = entity.world;
		if (!world.isRemote) {

			final Entity source = event.getSource().getTrueSource();
			if (source instanceof ServerPlayerEntity) {

				final ServerPlayerEntity player = (ServerPlayerEntity) source;
				if (player.isSpectator()) {
					return;
				}

				final int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, player.getHeldItemMainhand());
				final int dropchance = SpawnerSettingsConfig.SERVER.mobSpawnEggDropChance.get();

				if (dropchance >= 1 && dropchance <= 100) {

					// foreach lootinglevel there's an additional chance to drop the egg
					for (int i = 0; i < (1 + lootingLevel); i++) {

						if (Math.random() < ((double) dropchance / 100)) {

							final EntityType<?> type = (EntityType<?>) entity.getType();
							final ItemStack stack = new ItemStack((IItemProvider) ForgeRegistries.ITEMS.getValue(new ResourceLocation(type.getRegistryName() + "_spawn_egg")), 1);
							entity.entityDropItem(stack);

						}
					}
				}
			}
		}
	}
}