package com.aki.advanced_industry.api.gui;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModTileGuiBase extends ModGuiBase {
    public TileEntityBase inventoryTile;
    public List<ModGuiSlotTex> slotTexes = new ArrayList<>();

    public ModTileGuiBase(TileEntityBase inventoryTile, EntityPlayer player, Container inventorySlotsIn) {
        super(player, inventorySlotsIn);
        this.inventoryTile = inventoryTile;
        if(this.inventoryTile.HasPlayerInventory()) {
            for (int y = 0; y < 3; y++) {
                for (int i = 0; i < 9; i++)
                    slotTexes.add(new ModGuiSlotTex(12 + i * 18, 102 + y * 18));
            }
            for (int x = 0; x < 9; x++)
                //
                slotTexes.add(new ModGuiSlotTex(12 + x * 18, 160));
        }
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
