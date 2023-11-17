package com.aki.advanced_industry.mods.industry.items.ingots;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.item.Item;

public class ItemNickelIngot extends Item {
    public ItemNickelIngot() {
        this.setNoRepair();
        this.maxStackSize = 64;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("ingot_nickel");
        this.setRegistryName(AdvancedIndustryCore.ModID, "ingot_nickel");
    }
}
