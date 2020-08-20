package com.tristankechlo.spawnersettings.init;

import com.tristankechlo.spawnersettings.SpawnerSettings;
import com.tristankechlo.spawnersettings.container.SpawnerContainer;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModContainerTypes {
	
	public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = DeferredRegister.create(ForgeRegistries.CONTAINERS, SpawnerSettings.MOD_ID);
	
	public static final RegistryObject<ContainerType<SpawnerContainer>> SPAWNER_CONTAINER = 
			CONTAINER_TYPES.register("spawner_container", () -> IForgeContainerType.create(SpawnerContainer::new));
	
}
