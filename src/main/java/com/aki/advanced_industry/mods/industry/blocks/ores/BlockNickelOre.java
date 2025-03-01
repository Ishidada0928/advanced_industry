package com.aki.advanced_industry.mods.industry.blocks.ores;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.api.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockNickelOre extends BlockBase {
    public BlockNickelOre() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "ore_nickel");
        this.setUnlocalizedName("ore_nickel");
        this.setCreativeTab(ModMaterials.tabs);
    }
}