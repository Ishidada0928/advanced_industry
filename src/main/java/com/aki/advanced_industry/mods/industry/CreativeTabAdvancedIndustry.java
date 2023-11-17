package com.aki.advanced_industry.mods.industry;

import com.aki.advanced_industry.ModMaterials;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class CreativeTabAdvancedIndustry extends CreativeTabs {
    public CreativeTabAdvancedIndustry(String p_i47634_1_) {
        super(p_i47634_1_);
    }

    @Override
    public String getTranslatedTabLabel() {
        return "AdvancedIndustry";
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModMaterials.nickelOre);
    }
}
