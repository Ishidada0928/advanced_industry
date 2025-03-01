package com.aki.advanced_industry.api.gui;

import com.aki.advanced_industry.AdvancedIndustryCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class ModGuiSlotTex {
    private ResourceLocation slotTex = new ResourceLocation(AdvancedIndustryCore.ModID, "textures/gui/part/slot/gui_slot.png");
    public int PosX;
    public int PosY;
    public ModGuiSlotTex(int posX, int posY) {
        this.PosX = posX;
        this.PosY = posY;
    }

    public ModGuiSlotTex(int posX, int posY, ResourceLocation location) {
        this.PosX = posX;
        this.PosY = posY;
        this.slotTex = location;
    }

    public void SlotRender(Minecraft mc, float particleTick, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // bind the background gui texture
        mc.getTextureManager().bindTexture(this.slotTex);

        // render the gui background
        this.drawTexturedModalRect(this.PosX, this.PosY, 0, 0, 16, 16);

        GlStateManager.popMatrix();
    }

    private void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(((float)(textureX) * 0.00390625F), ((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(((float)(textureX + width) * 0.00390625F), ((float)(textureY + height) * 0.00390625F)).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(((float)(textureX + width) * 0.00390625F), ((float)(textureY) * 0.00390625F)).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(((float)(textureX) * 0.00390625F), ((float)(textureY) * 0.00390625F)).endVertex();
        tessellator.draw();
    }
}
