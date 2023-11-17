package com.aki.advanced_industry.mods.industry.items.dusts;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemSulfurDust extends Item {
    public ItemSulfurDust() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("dust_sulfur");
        this.setRegistryName(AdvancedIndustryCore.ModID, "dust_sulfur");
    }
}
