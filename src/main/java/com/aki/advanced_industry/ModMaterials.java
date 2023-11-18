package com.aki.advanced_industry;

import com.aki.advanced_industry.mods.industry.CreativeTabAdvancedIndustry;
import com.aki.advanced_industry.mods.industry.blocks.BlockLagChecker;
import com.aki.advanced_industry.mods.industry.blocks.cables.energy.BlockAdvancedEnergyCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.energy.BlockBasicEnergyCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.energy.BlockExtremeEnergyCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.energy.BlockUltimateEnergyCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.fluid.BlockAdvancedFluidCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.fluid.BlockBasicFluidCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.fluid.BlockExtremeFluidCable;
import com.aki.advanced_industry.mods.industry.blocks.cables.fluid.BlockUltimateFluidCable;
import com.aki.advanced_industry.mods.industry.blocks.machines.BlockCompressionCrusher;
import com.aki.advanced_industry.mods.industry.blocks.ores.*;
import com.aki.advanced_industry.mods.industry.items.dusts.*;
import com.aki.advanced_industry.mods.industry.items.ingots.*;
import com.aki.advanced_industry.mods.industry.items.tools.ItemWrench;
import net.minecraft.creativetab.CreativeTabs;

public class ModMaterials {
    public static CreativeTabs tabs = new CreativeTabAdvancedIndustry("AdvancedIndustry");

    public static BlockCopperOre copperOre = new BlockCopperOre();
    public static BlockLeadOre leadOre = new BlockLeadOre();
    public static BlockNickelOre nickelOre = new BlockNickelOre();
    public static BlockOsmiumOre osmiumOre = new BlockOsmiumOre();
    public static BlockSilverOre silverOre = new BlockSilverOre();
    public static BlockTinOre tinOre = new BlockTinOre();

    //cables
    public static BlockBasicEnergyCable basicEnergyCable = new BlockBasicEnergyCable();
    public static BlockAdvancedEnergyCable advancedEnergyCable = new BlockAdvancedEnergyCable();
    public static BlockExtremeEnergyCable extremeEnergyCable = new BlockExtremeEnergyCable();
    public static BlockUltimateEnergyCable ultimateEnergyCable = new BlockUltimateEnergyCable();

    public static BlockBasicFluidCable basicFluidCable = new BlockBasicFluidCable();
    public static BlockAdvancedFluidCable advancedFluidCable = new BlockAdvancedFluidCable();
    public static BlockExtremeFluidCable extremeFluidCable = new BlockExtremeFluidCable();
    public static BlockUltimateFluidCable ultimateFluidCable = new BlockUltimateFluidCable();

    //machines
    public static BlockCompressionCrusher compressionCrusher = new BlockCompressionCrusher();

    //misc
    public static BlockLagChecker lagChecker = new BlockLagChecker();

    public static void RegisterBlock() {
        AdvancedIndustryCore.blockRegistryHelper
                .Init()
                .add(copperOre)
                .add(leadOre)
                .add(nickelOre)
                .add(osmiumOre)
                .add(silverOre)
                .add(tinOre)
                .add(basicEnergyCable)
                .add(advancedEnergyCable)
                .add(extremeEnergyCable)
                .add(ultimateEnergyCable)
                .add(compressionCrusher)
                .add(basicFluidCable)
                .add(advancedFluidCable)
                .add(extremeFluidCable)
                .add(ultimateFluidCable)
                .add(lagChecker);
    }

    public static ItemCopperDust copperDust = new ItemCopperDust();
    public static ItemLeadDust leadDust = new ItemLeadDust();
    public static ItemNickelDust nickelDust = new ItemNickelDust();
    public static ItemOsmiumDust osmiumDust = new ItemOsmiumDust();
    public static ItemSilverDust silverDust = new ItemSilverDust();
    public static ItemTinDust tinDust = new ItemTinDust();
    public static ItemSulfurDust sulfurDust = new ItemSulfurDust();
    public static ItemIronDust ironDust = new ItemIronDust();
    public static ItemGoldDust goldDust = new ItemGoldDust();
    public static ItemDiamondDust diamondDust = new ItemDiamondDust();
    public static ItemObsidianDust obsidianDust = new ItemObsidianDust();

    public static ItemCopperIngot copperIngot = new ItemCopperIngot();
    public static ItemLeadIngot leadIngot = new ItemLeadIngot();
    public static ItemNickelIngot nickelIngot = new ItemNickelIngot();
    public static ItemOsmiumIngot osmiumIngot = new ItemOsmiumIngot();
    public static ItemSilverIngot silverIngot = new ItemSilverIngot();
    public static ItemTinIngot tinIngot = new ItemTinIngot();
    public static ItemWrench wrench = new ItemWrench();

    public static void RegisterItem() {
        AdvancedIndustryCore.itemRegistryHelper
                .Init()
                .add(copperDust)
                .add(leadDust)
                .add(nickelDust)
                .add(osmiumDust)
                .add(silverDust)
                .add(tinDust)
                .add(sulfurDust)
                .add(ironDust)
                .add(goldDust)
                .add(diamondDust)
                .add(obsidianDust)
                .add(copperIngot)
                .add(leadIngot)
                .add(nickelIngot)
                .add(osmiumIngot)
                .add(silverIngot)
                .add(tinIngot)
                .add(wrench);
    }
}
