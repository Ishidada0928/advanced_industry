package com.aki.advanced_industry.mods.industry.blocks.cables.fluid;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.mods.industry.tileentities.cables.fluid.TileAdvancedFluidCable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockAdvancedFluidCable extends BlockFluidCableBase {
    public BlockAdvancedFluidCable() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "advanced_fluid_cable");
        this.setUnlocalizedName("advanced_fluid_cable");
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileAdvancedFluidCable();
    }
}
