package com.aki.advanced_industry.mods.industry.items.ingots;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemCopperIngot extends Item {
    public ItemCopperIngot() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("ingot_copper");
        this.setRegistryName(AdvancedIndustryCore.ModID, "ingot_copper");
    }
}
