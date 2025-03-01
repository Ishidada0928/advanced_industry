package com.aki.advanced_industry.api.gui.test;

import com.aki.advanced_industry.api.util.IMoveGuiObject;
import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Slot自体は移動しない。
 * */
public class TestModGuiSlot extends SlotItemHandler implements IMoveGuiObject {
    public int mouseX;
    public int mouseY;
    public int DefaultX;
    public int DefaultY;


    public TestModGuiSlot(ItemStackHandler stackHandler, int index, int xPosition, int yPosition) {
        super(stackHandler, index, xPosition, yPosition);
        this.mouseX = this.DefaultX = xPosition;
        this.mouseY = this.DefaultY = yPosition;
    }

    @Override
    public boolean IsMoval(int mouseX, int mouseY) {
        return this.xPos <= mouseX && this.yPos <= mouseY && mouseX <= (this.xPos + 16) && mouseY <= (this.yPos + 16);
    }

    @Override
    public void Move(int mouseX, int mouseY) {
        this.xPos = this.mouseX = mouseX;
        this.yPos = this.mouseY = mouseY;
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
