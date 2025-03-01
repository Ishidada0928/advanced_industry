package com.aki.advanced_industry.mods.industry.blocks.ores;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.api.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockSilverOre extends BlockBase {
    public BlockSilverOre() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "ore_silver");
        this.setUnlocalizedName("ore_silver");
        this.setCreativeTab(ModMaterials.tabs);
    }
}
