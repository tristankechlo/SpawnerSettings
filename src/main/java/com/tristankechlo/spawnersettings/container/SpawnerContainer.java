package com.tristankechlo.spawnersettings.container;

import com.tristankechlo.spawnersettings.init.ModContainerTypes;
import com.tristankechlo.spawnersettings.util.SpawnerData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;

public class SpawnerContainer extends Container {

	public Inventory inventory;
	public PlayerInventory playerInv;

	public SpawnerContainer(final int windowId, final PlayerInventory playerInv, final PacketBuffer data) {
		this(windowId, playerInv, new Inventory(9));
	}
		
	public SpawnerContainer(final int windowId, final PlayerInventory playerInv, Inventory inventoryIN) {
		super(ModContainerTypes.SPAWNER_CONTAINER.get(), windowId);

		this.playerInv = playerInv;
		this.inventory = inventoryIN;
		
		for (int column = 0; column < 9; column++) {
			this.addSlot(new SpawnerSlot(this.inventory, column, 10 + (column * 18), 18));
		}

		// Main Inventory
		int startX = 32;
		int startY = 103;
		int slotSizePlus2 = 18;
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(playerInv, 9 + (row * 9) + column, startX + (column * slotSizePlus2),	startY + (row * slotSizePlus2)));
			}
		}

		// Hotbar
		for (int column = 0; column < 9; column++) {
			this.addSlot(new Slot(playerInv, column, startX + (column * slotSizePlus2), 161));
		}
	}
		
	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		if(playerIn.world.isRemote) {
			return;
		}
		//set new SpawnPotentials
		ListNBT spawn_potentials = SpawnerData.createSpawnPotentials(this.inventory);
		/*nbt.put("SpawnPotentials", spawn_potentials);
		nbt.put("SpawnData", spawn_potentials.getCompound(0).getCompound("Entity"));*/
		this.inventory.clear();
		super.onContainerClosed(playerIn);
	}
			
	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return this.inventory.isUsableByPlayer(playerIn);
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < 9) {
				if (!this.mergeItemStack(itemstack1, 9, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
	
	class SpawnerSlot extends Slot {
		
		public SpawnerSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			Item item = stack.getItem();
			if (item instanceof SpawnEggItem) {
				return true;
			}
			return false;
		}
		
	}

}
