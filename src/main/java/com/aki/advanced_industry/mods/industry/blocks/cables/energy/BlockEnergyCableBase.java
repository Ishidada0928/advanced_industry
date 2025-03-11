package com.aki.advanced_industry.mods.industry.blocks.cables.energy;

import com.aki.advanced_industry.mods.industry.blocks.cables.BlockCableBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public abstract class BlockEnergyCableBase extends BlockCableBase {
    public BlockEnergyCableBase(Material blockMaterialIn) {
        super(blockMaterialIn);
    }

    public abstract int getMaxSendEnergy();

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TextComponentTranslation("tooltip.energy_pipe1.info", this.getMaxSendEnergy()).getUnformattedComponentText());
        tooltip.add(new TextComponentTranslation("tooltip.energy_pipe2.info").getUnformattedComponentText());
        super.addInformation(stack, player, tooltip, advanced);
    }
}
