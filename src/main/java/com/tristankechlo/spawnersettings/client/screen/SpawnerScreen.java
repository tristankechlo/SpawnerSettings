package com.tristankechlo.spawnersettings.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tristankechlo.spawnersettings.SpawnerSettings;
import com.tristankechlo.spawnersettings.container.SpawnerContainer;
import com.tristankechlo.spawnersettings.network.SpawnerSettingsPacketHandler;
import com.tristankechlo.spawnersettings.network.packet.SetNewSpawnerConfig;
import com.tristankechlo.spawnersettings.util.SpawnerData;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerScreen extends ContainerScreen<SpawnerContainer> {
	
    private ResourceLocation GUI = new ResourceLocation(SpawnerSettings.MOD_ID, "textures/gui/spawner.png");
    private BlockPos pos;
    
	private Button speedButton = null;
	private Button countButton = null;
	private Button rangeButton = null;
	private Button saveButton = null;
	
	private byte countValue, speedValue, rangeValue;
	
	private String[] optionStrings = {"low", "default", "high", "highest"};
	
	private short[] minSpawnDelayReference = {300, 200, 100};
	private short[] maxSpawnDelayReference = {900, 800, 400};
	private short[] spawnCountReference = {2, 4, 8};
	private short[] maxNearbyEntitiesReference = {6, 12, 24};
	private short[] requiredPlayerRangeReference = {8, 16, 32, 64};
	
	private short delay, minSpawnDelay, maxSpawnDelay, spawnCount, maxNearbyEntities, requiredPlayerRange;

	
    public SpawnerScreen(SpawnerContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);

        //Gui dimensions
        this.xSize = 224;
        this.ySize = 187;
        //Offset String "Inventory"
        this.playerInventoryTitleX += 24;
        this.playerInventoryTitleY += 20;
                
        this.loadCurrentOptionState();
        
    }
    
    @Override
    protected void init() {
    	super.init();
    	
    	this.speedButton = new Button(this.guiLeft + 9, this.guiTop + 42, 100, 20,
    			new StringTextComponent(new TranslationTextComponent("container.spawner.button.speed").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[speedValue]).getString()),
    			(button) -> {
    				this.handleSpeedButton();
    			});    	
    	this.countButton = new Button (this.guiLeft + 115, this.guiTop + 42, 100, 20,
    			new StringTextComponent(new TranslationTextComponent("container.spawner.button.count").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[countValue]).getString()),
    			(button) -> {
    				this.handleCountButton();
    			}); 	
    	this.rangeButton = new Button (this.guiLeft + 9, this.guiTop + 68, 100, 20,
    			new StringTextComponent(new TranslationTextComponent("container.spawner.button.range").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[rangeValue]).getString()),
    			(button) -> {
    				this.handleRangeButton();
    			});
    	this.saveButton = new Button (this.guiLeft + 115, this.guiTop + 68, 100, 20,
    			new TranslationTextComponent("container.button.save"),
    			(button) -> {
    				this.closeScreen();
    			});
    	
    	this.addButton(this.countButton);
    	this.addButton(this.speedButton);
    	this.addButton(this.rangeButton);
    	this.addButton(this.saveButton);

    }
    
    @Override
    public void closeScreen() {
    	//check if config changed
		//send new Config to Server
		CompoundNBT nbt = SpawnerData.createSpawnerNBT(this.delay, this.minSpawnDelay, this.maxSpawnDelay, this.spawnCount, this.maxNearbyEntities, this.requiredPlayerRange);
		SpawnerSettingsPacketHandler.INSTANCE.sendToServer(new SetNewSpawnerConfig(this.pos, nbt));
		
    	super.closeScreen();
    }
        
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }
    
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width  - this.xSize) / 2;
        int relY = (this.height   - this.ySize) / 2;
        blit(matrixStack, relX, relY, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
	}
	
	private void handleSpeedButton() {
		switch (speedValue) {
			case 0:
				//set to default
				this.speedValue = 1;		
				this.delay = this.maxSpawnDelayReference[1];
				this.minSpawnDelay = this.minSpawnDelayReference[1];
				this.maxSpawnDelay = this.maxSpawnDelayReference[1];
				break;
			case 1:
				//set to high
				this.speedValue = 2;		
				this.delay = this.maxSpawnDelayReference[2];
				this.minSpawnDelay = this.minSpawnDelayReference[2];
				this.maxSpawnDelay = this.maxSpawnDelayReference[2];				
				break;
			case 2:
			default:
				//reset to low
				this.speedValue = 0;		
				this.delay = this.maxSpawnDelayReference[0];
				this.minSpawnDelay = this.minSpawnDelayReference[0];
				this.maxSpawnDelay = this.maxSpawnDelayReference[0];				
				break;
		}
		
		this.speedButton.setMessage(new StringTextComponent(new TranslationTextComponent("container.spawner.button.speed").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[speedValue]).getString()));
	}
	
	private void handleCountButton() {
		switch (countValue) {
			case 0:
				//set to default
				this.countValue = 1;
				this.spawnCount = this.spawnCountReference[1];
				this.maxNearbyEntities = this.maxNearbyEntitiesReference[1];
				break;
			case 1:
				//set to high
				this.countValue = 2;
				this.spawnCount = this.spawnCountReference[2];
				this.maxNearbyEntities = this.maxNearbyEntitiesReference[2];	
				break;
			case 2:
			default:
				//reset to low
				this.countValue = 0;
				this.spawnCount = this.spawnCountReference[0];
				this.maxNearbyEntities = this.maxNearbyEntitiesReference[0];
				break;
		}
		
		countButton.setMessage(new StringTextComponent(new TranslationTextComponent("container.spawner.button.count").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[countValue]).getString()));
	}
	
	private void handleRangeButton() {
		switch (rangeValue) {
			case 0:
				//set to default
				this.rangeValue = 1;
				this.requiredPlayerRange = this.requiredPlayerRangeReference[1];
				break;
			case 1:
				//set to high
				this.rangeValue = 2;
				this.requiredPlayerRange = this.requiredPlayerRangeReference[2];	
				break;
			case 2:
				//set to high
				this.rangeValue = 3;
				this.requiredPlayerRange = this.requiredPlayerRangeReference[3];	
				break;
			case 3:
			default:
				//reset to low
				this.rangeValue = 0;
				this.requiredPlayerRange = this.requiredPlayerRangeReference[0];
				break;
		}
		
		rangeButton.setMessage(new StringTextComponent(new TranslationTextComponent("container.spawner.button.range").getString() + ": " + new TranslationTextComponent("container.button.option." + optionStrings[rangeValue]).getString()));
	}
	
	private void loadCurrentOptionState() {
		        
		this.pos = SpawnerData.pos;
		
        this.delay = SpawnerData.delay;
        this.minSpawnDelay = SpawnerData.minSpawnDelay;
        this.maxSpawnDelay = SpawnerData.maxSpawnDelay;
        if(this.minSpawnDelay == this.minSpawnDelayReference[0]) {
            this.speedValue = 0;
        } else if(this.minSpawnDelay == this.minSpawnDelayReference[1]) {
            this.speedValue = 1;
        } else if(this.minSpawnDelay == this.minSpawnDelayReference[2]) {
            this.speedValue = 2;
        } else {
            this.speedValue = 0;
        }
        
        this.spawnCount = SpawnerData.spawnCount;
        this.maxNearbyEntities = SpawnerData.maxNearbyEntities;
        if(this.spawnCount == this.spawnCountReference[0]) {
            this.countValue = 0;
        } else if(this.spawnCount == this.spawnCountReference[1]) {
            this.countValue = 1;
        } else if(this.spawnCount == this.spawnCountReference[2]) {
            this.countValue = 2;
        } else {
            this.countValue = 0;
        }

        this.requiredPlayerRange = SpawnerData.requiredPlayerRange;
        if(this.requiredPlayerRange == this.requiredPlayerRangeReference[0]) {
            this.rangeValue = 0;
        } else if(this.requiredPlayerRange == this.requiredPlayerRangeReference[1]) {
            this.rangeValue = 1;
        } else if(this.requiredPlayerRange == this.requiredPlayerRangeReference[2]) {
            this.rangeValue = 2;
        } else if(this.requiredPlayerRange == this.requiredPlayerRangeReference[3]) {
            this.rangeValue = 3;
        } else {
        	this.rangeValue = 0;
        }
	}

}
