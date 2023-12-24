package com.aki.advanced_industry.gui.test;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.util.IMoveGuiObject;
import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

/**
 * 画面(MainGui)の端に表示するような情報に使う
 * */
public class TestModSubGuiBase extends TestModGuiBase implements IMoveGuiObject {
    public ResourceLocation base_texture = new ResourceLocation(AdvancedIndustryCore.ModID, "textures/gui/part/gui_base.png");
    public ResourceLocation inventory_texture = new ResourceLocation(AdvancedIndustryCore.ModID, "textures/gui/part/inventory/gui_inventory.png");
    public int mouseX;
    public int mouseY;

    //MouseとDefaultとの差
    public int MouseSubX;
    public int MouseSubY;
    public int DefaultX;
    public int DefaultY;

    public int xPos;
    public int yPos;
    public boolean Movable = false;

    public TestModSubGuiBase(EntityPlayer player, Container inventorySlotsIn, int sizeX, int sizeY, int xPos, int yPos, boolean isMovable) {
        super(player, inventorySlotsIn);
        this.Movable = isMovable;
        this.mouseX = this.DefaultX = this.xPos = xPos;
        this.mouseY = this.DefaultY = this.yPos = yPos;
        this.MouseSubX = this.MouseSubY = 0;
        this.xSize = sizeX;
        this.ySize = sizeY;
    }

    @Override
    public boolean IsMoval(int mouseX, int mouseY) {
        return this.Movable && this.xPos <= mouseX && this.yPos <= mouseY && mouseX <= (this.xPos + xSize)  && mouseY <= (this.yPos + ySize);
    }

    @Override
    public void Move(int mouseX, int mouseY) {
        this.xPos = this.mouseX = mouseX;
        this.yPos = this.mouseY = mouseY;
        this.MouseSubX = this.mouseX - this.DefaultX;
        this.MouseSubY = this.mouseY - this.DefaultY;
    }

    @Override
    public void Reset() {
        this.xPos = this.DefaultX;
        this.yPos = this.DefaultY;
    }

    @Override
    public Pair<Integer, Integer> getXY() {
        return new Pair<>(this.xPos, this.yPos);
    }
}
