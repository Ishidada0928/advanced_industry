package com.aki.advanced_industry.mods.industry.render.cables.energy;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.mods.industry.render.cables.CableRendererBase;
import com.aki.advanced_industry.mods.industry.tileentities.cables.energy.TileEnergyCableBase;
import com.aki.advanced_industry.mods.industry.util.CableConnectionMode;
import com.aki.mcutils.APICore.Loaders.ObjModel.AdvancedModelLoader;
import com.aki.mcutils.APICore.Loaders.ObjModel.IModelCustom;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Map;

@SideOnly(Side.CLIENT)
public class TileRenderEnergyCable extends CableRendererBase<TileEnergyCableBase> {
    public TileRenderEnergyCable() {
        super();
    }

    @Override
    public void render(TileEnergyCableBase te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        super.render(te, x, y, z, partialTicks, destroyStage, alpha);
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        //GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());


        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);


        for(Map.Entry<EnumFacing, CableConnectionMode> entry : te.renderFacingMode.entrySet()) {
            GlStateManager.pushMatrix();
            switch (entry.getKey()) {
                case UP:
                    GlStateManager.rotate(180, 1.0f,0.0f,0.0f);
                    break;
                case DOWN:
                    GlStateManager.rotate(0, 1.0f,0.0f,0.0f);
                    GlStateManager.rotate(0, 0.0f,1.0f,0.0f);
                    break;
                case WEST:
                    GlStateManager.rotate(90, 1.0f,0.0f,0.0f);
                    GlStateManager.rotate(-90, 0.0f,0.0f,1.0f);
                    break;
                case EAST:
                    GlStateManager.rotate(-90, 1.0f,0.0f,0.0f);
                    GlStateManager.rotate(90, 0.0f,0.0f,1.0f);
                    break;
                case NORTH:
                    GlStateManager.rotate(90, 1.0f,0.0f,0.0f);
                    break;
                case SOUTH:
                    GlStateManager.rotate(-90, 1.0f,0.0f,0.0f);
                    break;
                default:
                    break;
            }

            switch (entry.getValue()) {
                case NORMAL:
                    bindTexture(new ResourceLocation(AdvancedIndustryCore.ModID, "textures/blocks/cables/cable_connection_bridge.png"));
                    Bridge.renderAll();
                    break;
                case PUSH:
                    bindTexture(new ResourceLocation(AdvancedIndustryCore.ModID, "textures/blocks/cables/cable_connection_push.png"));
                    Push.renderAll();
                    break;
                case PULL:
                    bindTexture(new ResourceLocation(AdvancedIndustryCore.ModID, "textures/blocks/cables/cable_connection_pull.png"));
                    Pull.renderAll();
                    break;
                default:
                    break;
            }
            GlStateManager.popMatrix();
        }


        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEnergyCableBase te) {
        return true;
    }
}
