package com.aki.advanced_industry.gui.test;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.util.HashMap;
import java.util.Map;

/**
 * 色と文字の大きさを変更可能
 * Main GUI
 * */
public class TestModGuiBase extends GuiContainer {
    public EntityPlayer player;

    public int xPos;
    public int yPos;

    //上に表示するGUI
    //IDを取得するためにあるようなもん...
    //Integer はID
    private HashMap<TestModSubGuiBase, Integer> guiList = new HashMap();

    public TestModGuiBase(EntityPlayer player, Container inventorySlotsIn) {
        super(inventorySlotsIn);
        this.player = player;
        this.xSize = this.ySize = 184;//基本のサイズ
        this.xPos = (this.width - this.xSize) / 2;
        this.yPos = (this.height - this.ySize) / 2;
    }

    public int addSubGuiAndID(TestModSubGuiBase subGuiBase) {
        int ID = this.guiList.size();
        this.guiList.put(subGuiBase, ID);
        return ID;
    }

    @Override
    public void initGui() {
        super.initGui();
        for(Map.Entry<TestModSubGuiBase, Integer> entry : guiList.entrySet()) {
            entry.getKey().initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX,mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

        for(Map.Entry<TestModSubGuiBase, Integer> entry : guiList.entrySet()) {
            entry.getKey().drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        }
    }
}