package com.aki.advanced_industry.mods.industry.render.machines;

import com.aki.advanced_industry.mods.industry.tileentities.misc.TileCompressionCrusher;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly( Side.CLIENT )
public class TileRenderCompressionCrusher extends TileEntitySpecialRenderer<TileCompressionCrusher> {

    @Override
    public void render(TileCompressionCrusher te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        {
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableLighting();

            Tessellator tessellator = Tessellator.getInstance();
            IBlockState cobblestone = Blocks.COBBLESTONE.getDefaultState();
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBakedModel model = dispatcher.getModelForState(cobblestone);

            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            buffer.setTranslation(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
            dispatcher.getBlockModelRenderer().renderModel(te.getWorld(), model, cobblestone, te.getPos(), buffer, false);
            buffer.setTranslation(0, 0, 0);

            // Apply GL transformations relative to the center of the block: 1) TE rotation and 2) crank rotation
            GlStateManager.scale(0.8, 0.8, 0.8);
            GlStateManager.translate((x / 0.8) + 0.125, (y  / 0.8) + ( + 0.5 - (0.05 * te.Step)) * 1.2, (z / 0.8) + 0.125);
            tessellator.draw();

            GlStateManager.disableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }

        {
            GlStateManager.pushMatrix();
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableLighting();

            GlStateManager.scale(1.0,1.0,1.0);
            GlStateManager.translate(x + 0.5, y + 0.2, z + 0.5);

            Minecraft.getMinecraft().getRenderItem().renderItem(te.Inventory.getStackInSlot(0), ItemCameraTransforms.TransformType.GROUND);

            GlStateManager.disableRescaleNormal();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}
