package com.aki.advanced_industry.mods.industry.blocks.ores;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.api.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockCopperOre extends BlockBase {
    public BlockCopperOre() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "ore_copper");
        this.setUnlocalizedName("ore_copper");
        this.setCreativeTab(ModMaterials.tabs);
    }
}
