package com.aki.advanced_industry.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 没
 * */
/**
 * 左上のボタンを押したらカーソルについてくるようにするといいかも？
 * 一番上に来ているものの枠を青色にしたり、下のものを暗くすると見やすくていいかも。
 * リストは別のウィンドウで表示するといいかも？
 * 階層表示やそのブロック、タイルの詳細情報などを表示できるようにするといいかも。
 * スライダーも同じ原理で実装できそう。
 * サイズの調整ができてもよさそう。
 * 文字やボタンの自動改行
 * Guiを動かすとき、中のボタンなどの位置も変更。
 * ===>>> Windowと同じようなものとする。
 * */
public class ModGUIContainerBase extends GuiContainer {
    public List<ModGuiBase> AddedGuis = new ArrayList<>();

    private long LastMoveTime;
    /**
     * 大きいほど前面に来ます。
     * メインのGuiほど大きくしてください。
     * */
    private int Priority;
    public ResourceLocation GuiTex;
    public boolean NotMove;
    public int BaseX;
    public int BaseY;

    public int BaseWidthIn;
    public int BaseHeightIn;
    //横はWidthに合わせます。
    private final int MarginHeight = 5;
    public Color MarginColor = Color.CYAN;

    public ModGUIContainerBase(Container p_i1072_1_, ResourceLocation GuiTexture, int priority, int SizeX, int SizeY, boolean NotMove) {
        super(p_i1072_1_);
        this.GuiTex = GuiTexture;
        this.Priority = priority;
        this.LastMoveTime = 0;
        this.xSize = SizeX;
        this.ySize = SizeY;
        this.BaseWidthIn = xSize;
        this.BaseHeightIn = ySize;
        this.NotMove = NotMove;
        this.BaseX = (this.width - this.xSize) / 2;
        this.BaseY = (this.height - this.ySize) / 2;
    }

    public int getPriority() {
        return Priority;
    }

    public long getLastMoveTime() {
        return LastMoveTime != 0 ? LastMoveTime : this.getPriority();
    }

    /**
     * テクスチャや描画関係
     * */
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
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
        GlStateManager.popMatrix();
    }

    /**
     * 文字など
     * */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        if(AddedGuis.size() > 0) {
            AddedGuis.sort(Comparator.comparing(ModGuiBase::getLastMoveTime).reversed());
            long Last = AddedGuis.get(0).getLastMoveTime();
            if(Last > this.getLastMoveTime()) {
                this.MarginColor = Color.DARK_GRAY;
                for(int i = 0; i < this.AddedGuis.size(); i++) {
                    if(i == 0) {
                        this.AddedGuis.get(0).SetColor(Color.CYAN);
                    } else {
                        this.AddedGuis.get(i).SetColor(Color.DARK_GRAY);
                    }
                }
            } else {
                this.MarginColor = Color.CYAN;
                for(int i = 0; i < this.AddedGuis.size(); i++) {
                    this.AddedGuis.get(i).SetColor(Color.DARK_GRAY);
                }
            }
            for (ModGuiBase base : this.AddedGuis) {
                base.drawButton(this.mc, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int par3) throws IOException {
        super.mouseClicked(mouseX, mouseY, par3);
        boolean hovered = mouseX >= this.BaseX && mouseY >= this.BaseY - this.MarginHeight && mouseX < this.BaseX + this.BaseWidthIn && mouseY < this.BaseY;
        int SX = BaseX;
        int SY = BaseY;
        if(hovered && !NotMove) {
            this.BaseX = mouseX;
            this.BaseY = mouseY;
            SX = this.BaseX - SX;
            SY = this.BaseY - SY;
            this.LastMoveTime = Minecraft.getSystemTime();
        }
        for(ModGuiBase base : this.AddedGuis) {
            if(hovered && !NotMove) {
                base.BaseX += SX;
                base.BaseY += SY;
            }
            base.mousePressed(this.mc, mouseX, mouseY);
        }
    }
}
