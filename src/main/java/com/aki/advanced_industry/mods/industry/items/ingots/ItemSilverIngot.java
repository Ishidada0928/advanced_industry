package com.aki.advanced_industry.mods.industry.items.ingots;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemSilverIngot extends Item {
    public ItemSilverIngot() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("ingot_silver");
        this.setRegistryName(AdvancedIndustryCore.ModID, "ingot_silver");
    }
}
