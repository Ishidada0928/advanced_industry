package com.aki.advanced_industry.mods.industry.items.dusts;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemNickelDust extends Item {
    public ItemNickelDust() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("dust_nickel");
        this.setRegistryName(AdvancedIndustryCore.ModID, "dust_nickel");
    }
}
