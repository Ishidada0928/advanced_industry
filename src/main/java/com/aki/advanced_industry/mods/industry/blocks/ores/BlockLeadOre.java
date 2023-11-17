package com.aki.advanced_industry.mods.industry.blocks.ores;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.block.BlockBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;

public class BlockLeadOre extends BlockBase {
    public BlockLeadOre() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "ore_lead");
        this.setUnlocalizedName("ore_lead");
        this.setCreativeTab(ModMaterials.tabs);
    }
}
