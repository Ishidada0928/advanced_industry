package com.aki.advanced_industry.mods.industry.items.dusts;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemOsmiumDust extends Item {
    public ItemOsmiumDust() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("dust_osmium");
        this.setRegistryName(AdvancedIndustryCore.ModID, "dust_osmium");
    }
}
