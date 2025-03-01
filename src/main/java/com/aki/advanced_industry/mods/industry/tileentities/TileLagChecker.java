package com.aki.advanced_industry.mods.industry.tileentities;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class TileLagChecker extends TileEntityBase {
    @Override
    public void update() {
        super.update();
    }

    public void OnClick(EntityPlayer player) {
        player.sendMessage(new TextComponentString("New - Old SystemTime: " + this.LagTickDifference).setStyle(new Style().setColor(TextFormatting.AQUA).setUnderlined(true)));
    }
}
