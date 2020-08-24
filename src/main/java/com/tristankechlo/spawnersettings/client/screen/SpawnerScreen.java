package com.tristankechlo.spawnersettings.client.screen;

import java.util.List;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tristankechlo.spawnersettings.SpawnerSettings;
import com.tristankechlo.spawnersettings.config.SpawnerSettingsConfig;
import com.tristankechlo.spawnersettings.container.SpawnerContainer;
import com.tristankechlo.spawnersettings.network.SpawnerSettingsPacketHandler;
import com.tristankechlo.spawnersettings.network.packet.SyncNewSpawnerConfigToServer;
import com.tristankechlo.spawnersettings.util.SpawnerData;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerScreen extends ContainerScreen<SpawnerContainer> {

	private ResourceLocation GUI = new ResourceLocation(SpawnerSettings.MOD_ID, "textures/gui/spawner.png");
	private BlockPos pos;

	private Button speedButton = null;
	private Button countButton = null;
	private Button playerRangeButton = null;
	private Button spawnRangeButton = null;
	private final int buttonHeight = 20;
	private final int buttonwidth = 96;

	private byte countValue, speedValue, playerRangeValue, spawnRangeValue;
	private byte[] previousConfig;
	private byte[] currentConfig;

	private String[] optionStrings = { "low", "default", "high", "highest" };

	private short[] minSpawnDelayReference = { 400, 200, 100 };
	private short[] maxSpawnDelayReference = { 1200, 800, 400 };
	private short[] spawnCountReference = { 2, 4, 8 };
	private short[] maxNearbyEntitiesReference = { 6, 12, 24 };
	private short[] requiredPlayerRangeReference = { 8, 16, 32, 64 };
	private short[] spawnRangeReference = { 2, 4, 8 };

	private short delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange, spawnRange;

	public SpawnerScreen(SpawnerContainer container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);

		// Gui dimensions
		this.xSize = 216;
		this.ySize = 187;
		// Offset for String "Spawner"
		this.titleX += 1;
		this.titleY += 0;
		// Offset for String "Inventory"
		this.playerInventoryTitleX += 19;
		this.playerInventoryTitleY += 20;

		this.loadCurrentSpawnerState();

		this.previousConfig = new byte[] { this.speedValue, this.countValue, this.playerRangeValue, this.spawnRangeValue };
		this.currentConfig = new byte[] { this.speedValue, this.countValue, this.playerRangeValue, this.spawnRangeValue };

	}

	@Override
	protected void init() {
		super.init();
		this.speedButton = new Button(this.guiLeft + 9, this.guiTop + 42, this.buttonwidth, this.buttonHeight,
				new TranslationTextComponent("container.button.speed." + this.optionStrings[this.speedValue]), (button) -> {
					this.handleSpeedButton();
				});
		this.countButton = new Button(this.guiLeft + 111, this.guiTop + 42, this.buttonwidth, this.buttonHeight,
				new TranslationTextComponent("container.button.count." + this.optionStrings[this.countValue]), (button) -> {
					this.handleCountButton();
				});
		this.playerRangeButton = new Button(this.guiLeft + 9, this.guiTop + 68, this.buttonwidth, this.buttonHeight,
				new TranslationTextComponent("container.button.playerRange." + this.optionStrings[this.playerRangeValue]), (button) -> {
					this.handlePlayerRangeButton();
				});
		this.spawnRangeButton = new Button(this.guiLeft + 111, this.guiTop + 68, this.buttonwidth, this.buttonHeight,
				new TranslationTextComponent("container.button.spawnRange." + this.optionStrings[this.spawnRangeValue]), (button) -> {
					this.handleSpawnRangeButton();
				});

		this.addButton(this.countButton);
		this.addButton(this.speedButton);
		this.addButton(this.playerRangeButton);
		this.addButton(this.spawnRangeButton);

		if (!SpawnerSettingsConfig.SERVER.buttonCountActive.get()) {
			this.countButton.setMessage(new TranslationTextComponent("container.button.count.disabled"));
			this.countButton.active = false;
		}

		if (!SpawnerSettingsConfig.SERVER.buttonSpeedActive.get()) {
			this.speedButton.setMessage(new TranslationTextComponent("container.button.speed.disabled"));
			this.speedButton.active = false;
		}

		if (!SpawnerSettingsConfig.SERVER.buttonPlayerRangeActive.get()) {
			this.playerRangeButton.setMessage(new TranslationTextComponent("container.button.playerRange.disabled"));
			this.playerRangeButton.active = false;
		}

		if (!SpawnerSettingsConfig.SERVER.buttonSpawnRangeActive.get()) {
			this.spawnRangeButton.setMessage(new TranslationTextComponent("container.button.spawnRange.disabled"));
			this.spawnRangeButton.active = false;
		}

	}
	
	@Override
	public void tick() {
		super.tick();
	}
	
	@Override
	public void onClose() {
		super.onClose();
		// check if config changed
		if (this.hasConfigChanged()) {
			// send new Config to Server
			SpawnerSettingsPacketHandler.INSTANCE.sendToServer(
					new SyncNewSpawnerConfigToServer(this.pos, this.delay, this.minSpawnDelay, this.maxSpawnDelay,
							this.spawnCount, this.maxNearbyEntities, this.requiredPlayerRange, this.spawnRange));
		}
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.func_230459_a_(matrixStack, mouseX, mouseY);

		//tooltip speedbutton
		if (this.isPointInRegion(9, 42, this.buttonwidth, this.buttonHeight, (double) mouseX, (double) mouseY)) {
			List<ITextComponent> tooltips = Lists.newArrayList();
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.speed.title")).mergeStyle(TextFormatting.AQUA));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.speed.text1")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.speed.text2")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.speed.text3")).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(matrixStack, tooltips, mouseX, mouseY);
		}
		//tooltip countbutton
		if (this.isPointInRegion(111, 42, this.buttonwidth, this.buttonHeight, (double) mouseX, (double) mouseY)) {
			List<ITextComponent> tooltips = Lists.newArrayList();
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.count.title")).mergeStyle(TextFormatting.AQUA));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.count.text1")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.count.text2")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.count.text3")).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(matrixStack, tooltips, mouseX, mouseY);
		}
		//tooltip playerRangebutton
		if (this.isPointInRegion(9, 68, this.buttonwidth, this.buttonHeight, (double) mouseX, (double) mouseY)) {
			List<ITextComponent> tooltips = Lists.newArrayList();
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.playerRange.title")).mergeStyle(TextFormatting.AQUA));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.playerRange.text1")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.playerRange.text2")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.playerRange.text3")).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(matrixStack, tooltips, mouseX, mouseY);
		}
		//tooltip spawnRangebutton
		if (this.isPointInRegion(111, 68, this.buttonwidth, this.buttonHeight, (double) mouseX, (double) mouseY)) {
			List<ITextComponent> tooltips = Lists.newArrayList();
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.spawnRange.title")).mergeStyle(TextFormatting.AQUA));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.spawnRange.text1")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.spawnRange.text2")).mergeStyle(TextFormatting.GRAY));
			tooltips.add((new TranslationTextComponent("container.spawner.tooltip.spawnRange.text3")).mergeStyle(TextFormatting.GRAY));
			this.func_243308_b(matrixStack, tooltips, mouseX, mouseY);
		}
	}
	
	

	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI);
		int relX = (this.width - this.xSize) / 2;
		int relY = (this.height - this.ySize) / 2;
		blit(matrixStack, relX, relY, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
	}

	private boolean hasConfigChanged() {
		return (this.previousConfig[0] != this.currentConfig[0] || this.previousConfig[1] != this.currentConfig[1]
				|| this.previousConfig[2] != this.currentConfig[2] || this.previousConfig[3] != this.currentConfig[3]);
	}

	private void handleSpeedButton() {
		if (this.speedValue == 0) {
			// set to default
			this.speedValue = 1;
			this.delay = this.maxSpawnDelayReference[1];
			this.minSpawnDelay = this.minSpawnDelayReference[1];
			this.maxSpawnDelay = this.maxSpawnDelayReference[1];

		} else if (this.speedValue == 1) {
			// set to high
			this.speedValue = 2;
			this.delay = this.maxSpawnDelayReference[2];
			this.minSpawnDelay = this.minSpawnDelayReference[2];
			this.maxSpawnDelay = this.maxSpawnDelayReference[2];

		} else {
			this.speedValue = 0;
			this.delay = this.maxSpawnDelayReference[0];
			this.minSpawnDelay = this.minSpawnDelayReference[0];
			this.maxSpawnDelay = this.maxSpawnDelayReference[0];
		}
		this.currentConfig[0] = this.speedValue;
		this.speedButton.setMessage(new TranslationTextComponent("container.button.speed." + this.optionStrings[this.speedValue]));
	}

	private void handleCountButton() {
		if (this.countValue == 0) {
			// set to default
			this.countValue = 1;
			this.spawnCount = this.spawnCountReference[1];
			this.maxNearbyEntities = this.maxNearbyEntitiesReference[1];

		} else if (this.countValue == 1) {
			// set to high
			this.countValue = 2;
			this.spawnCount = this.spawnCountReference[2];
			this.maxNearbyEntities = this.maxNearbyEntitiesReference[2];

		} else {
			// reset to low
			this.countValue = 0;
			this.spawnCount = this.spawnCountReference[0];
			this.maxNearbyEntities = this.maxNearbyEntitiesReference[0];
		}
		this.currentConfig[1] = this.countValue;
		this.countButton.setMessage(new TranslationTextComponent("container.button.count." + this.optionStrings[this.countValue]));
	}

	private void handlePlayerRangeButton() {
		if (this.playerRangeValue == 0) {
			// set to default
			this.playerRangeValue = 1;
			this.requiredPlayerRange = this.requiredPlayerRangeReference[1];

		} else if (this.playerRangeValue == 1) {
			// set to high
			this.playerRangeValue = 2;
			this.requiredPlayerRange = this.requiredPlayerRangeReference[2];

		} else if (this.playerRangeValue == 2) {
			// set to high
			this.playerRangeValue = 3;
			this.requiredPlayerRange = this.requiredPlayerRangeReference[3];

		} else {
			// reset to low
			this.playerRangeValue = 0;
			this.requiredPlayerRange = this.requiredPlayerRangeReference[0];
		}
		this.currentConfig[2] = this.playerRangeValue;
		this.playerRangeButton.setMessage(new TranslationTextComponent("container.button.playerRange." + this.optionStrings[this.playerRangeValue]));
	}

	private void handleSpawnRangeButton() {
		if (this.spawnRangeValue == 0) {
			// set to default
			this.spawnRangeValue = 1;
			this.spawnRange = this.spawnRangeReference[1];

		} else if (this.spawnRangeValue == 1) {
			// set to high
			this.spawnRangeValue = 2;
			this.spawnRange = this.spawnRangeReference[2];

		} else {
			this.spawnRangeValue = 0;
			this.spawnRange = this.spawnRangeReference[0];
		}
		this.currentConfig[3] = this.spawnRangeValue;
		this.spawnRangeButton.setMessage(new TranslationTextComponent("container.button.spawnRange." + this.optionStrings[this.spawnRangeValue]));
	}
	
	private void loadCurrentSpawnerState() {

		this.pos = SpawnerData.pos;

		this.delay = SpawnerData.delay;
		this.minSpawnDelay = SpawnerData.minSpawnDelay;
		this.maxSpawnDelay = SpawnerData.maxSpawnDelay;
		if (this.minSpawnDelay == this.minSpawnDelayReference[0]) {
			this.speedValue = 0;
		} else if (this.minSpawnDelay == this.minSpawnDelayReference[1]) {
			this.speedValue = 1;
		} else if (this.minSpawnDelay == this.minSpawnDelayReference[2]) {
			this.speedValue = 2;
		} else {
			this.speedValue = 0;
		}

		this.spawnCount = SpawnerData.spawnCount;
		this.maxNearbyEntities = SpawnerData.maxNearbyEntities;
		if (this.spawnCount == this.spawnCountReference[0]) {
			this.countValue = 0;
		} else if (this.spawnCount == this.spawnCountReference[1]) {
			this.countValue = 1;
		} else if (this.spawnCount == this.spawnCountReference[2]) {
			this.countValue = 2;
		} else {
			this.countValue = 0;
		}

		this.requiredPlayerRange = SpawnerData.requiredPlayerRange;
		if (this.requiredPlayerRange == this.requiredPlayerRangeReference[0]) {
			this.playerRangeValue = 0;
		} else if (this.requiredPlayerRange == this.requiredPlayerRangeReference[1]) {
			this.playerRangeValue = 1;
		} else if (this.requiredPlayerRange == this.requiredPlayerRangeReference[2]) {
			this.playerRangeValue = 2;
		} else if (this.requiredPlayerRange == this.requiredPlayerRangeReference[3]) {
			this.playerRangeValue = 3;
		} else {
			this.playerRangeValue = 0;
		}
		
		this.spawnRange = SpawnerData.spawnRange;
		if(this.spawnRange == this.spawnRangeReference[0]) {
			this.spawnRangeValue = 0;
		} else if(this.spawnRange == this.spawnRangeReference[1]) {
			this.spawnRangeValue = 1;
		} else if(this.spawnRange == this.spawnRangeReference[2]) {
			this.spawnRangeValue = 2;
		} else {
			this.spawnRangeValue = 0;
		}
	}

}
