package com.aki.advanced_industry.mods.industry.items.dusts;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemCopperDust extends Item {
    public ItemCopperDust() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("dust_copper");
        this.setRegistryName(AdvancedIndustryCore.ModID, "dust_copper");
    }
}
