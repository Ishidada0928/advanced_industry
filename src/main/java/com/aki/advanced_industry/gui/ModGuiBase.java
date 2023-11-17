package com.aki.advanced_industry.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import java.awt.*;

/**
 * 没
 * */
public class ModGuiBase extends Gui {
    private long LastMoveTime;
    /**
     * 大きいほど前面に来ます。
     * */
    private int Priority;

    public int BaseX;
    public int BaseY;
    public int BaseWidthIn;
    public int BaseHeightIn;
    //横はWidthに合わせます。
    private final int MarginHeight = 5;

    public boolean visible;
    public boolean enable;
    public boolean move;

    public boolean NotMove = false;

    public Color MarginColor = Color.DARK_GRAY;
    public ModGuiBase(int Priority, int X, int Y, int baseWidthIn, int baseHeightIn, boolean NotMove) {
        this.Priority = Priority;
        this.LastMoveTime = 0;
        this.BaseX = X;
        this.BaseY = Y;
        this.BaseWidthIn = baseWidthIn;
        this.BaseHeightIn = baseHeightIn;
        this.visible = true;
        this.enable = true;
        this.move = false;
        this.NotMove = NotMove;
    }

    public int getPriority() {
        return Priority;
    }

    public long getLastMoveTime() {
        return LastMoveTime != 0 ? LastMoveTime : this.getPriority();
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            //boolean hovered = mouseX >= this.BaseX && mouseY >= this.BaseY - this.MarginHeight && mouseX < this.BaseX + this.BaseWidthIn && mouseY < this.BaseY;
            GlStateManager.color(MarginColor.getRed(), MarginColor.getGreen(), MarginColor.getBlue(), MarginColor.getAlpha());
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos((double)(this.BaseX), (double)(this.BaseY - this.MarginHeight), (double)this.zLevel).color(MarginColor.getRed(), MarginColor.getGreen(), MarginColor.getBlue(), MarginColor.getAlpha()).endVertex();
            bufferbuilder.pos((double)(this.BaseX + this.BaseWidthIn), (double)(this.BaseY - this.MarginHeight), (double)this.zLevel).color(MarginColor.getRed(), MarginColor.getGreen(), MarginColor.getBlue(), MarginColor.getAlpha()).endVertex();
            bufferbuilder.pos((double)(this.BaseX + this.BaseWidthIn), (double)(this.BaseY), (double)this.zLevel).color(MarginColor.getRed(), MarginColor.getGreen(), MarginColor.getBlue(), MarginColor.getAlpha()).endVertex();
            bufferbuilder.pos((double)(this.BaseX), (double)(this.BaseY), (double)this.zLevel).color(MarginColor.getRed(), MarginColor.getGreen(), MarginColor.getBlue(), MarginColor.getAlpha()).endVertex();
            tessellator.draw();
        }
    }

    //押しても、押してないのどちらの状態の時
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {

    }

    public void mouseReleased(int mouseX, int mouseY)
    {
        this.move = false;
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        boolean hovered = mouseX >= this.BaseX && mouseY >= this.BaseY - this.MarginHeight && mouseX < this.BaseX + this.BaseWidthIn && mouseY < this.BaseY;
        if(hovered && !NotMove) {
            this.BaseX = mouseX;
            this.BaseY = mouseY;
            this.LastMoveTime = Minecraft.getSystemTime();
            this.move = true;
        }
        return hovered;
    }

    public void SetColor(Color color) {
        this.MarginColor = color;
    }
}
