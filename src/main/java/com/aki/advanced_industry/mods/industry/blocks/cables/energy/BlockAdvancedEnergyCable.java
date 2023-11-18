package com.aki.advanced_industry.mods.industry.blocks.cables.energy;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.mods.industry.tileentities.cables.energy.TileAdvancedEnergyCable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAdvancedEnergyCable extends BlockEnergyCableBase {
    public BlockAdvancedEnergyCable() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "advanced_energy_cable");
        this.setUnlocalizedName("advanced_energy_cable");
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileAdvancedEnergyCable();
    }

    @Override
    public int getMaxSendEnergy() {
        return 10000;
    }
}
