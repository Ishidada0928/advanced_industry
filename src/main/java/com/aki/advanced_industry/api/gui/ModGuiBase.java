package com.aki.advanced_industry.api.gui;

import com.aki.advanced_industry.AdvancedIndustryCore;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ModGuiBase extends GuiContainer {
    /**
     * 文字などの表示用に。
     * */
    public ResourceLocation base_texture = new ResourceLocation(AdvancedIndustryCore.ModID, "textures/gui/part/gui_base.png");
    /**
     * インベントリのあるものに。
     * */
    public ResourceLocation inventory_texture = new ResourceLocation(AdvancedIndustryCore.ModID, "textures/gui/part/inventory/gui_inventory.png");

    public int baseX;
    public int baseY;
    public EntityPlayer player;

    public ModGuiBase(EntityPlayer player, Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.player = player;
        this.xSize = this.ySize = 184;
        this.baseX = (this.width - this.xSize) / 2;
        this.baseY = (this.height - this.ySize) / 2;
    }

    /**
     *　文字などを描画
     * */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        /*GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // bind the background gui texture
        this.mc.getTextureManager().bindTexture(base_texture);

        // render the gui background
        this.drawTexturedModalRect(baseX, baseY, 0, 0, this.xSize, this.ySize);

        GlStateManager.popMatrix();*/
    }

    /**
     * 独自のボタンや、テキストフィールドの実装に。
     * mouseX, mouseY, KeyCode?
     * */
    public void mouseClicked(int mouseX, int mouseY, int keycode) throws IOException {
        super.mouseClicked(mouseX, mouseY, keycode);
    }

    /**
     * ボタンなどを追加
     * */
    @Override
    public void initGui() {
        super.initGui();
    }
}
