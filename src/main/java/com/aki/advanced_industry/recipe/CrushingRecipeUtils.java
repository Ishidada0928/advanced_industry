package com.aki.advanced_industry.recipe;

import com.aki.advanced_industry.ModMaterials;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class CrushingRecipeUtils {
    /**
     * 必ず ItemStackは　Copy して setCount で １ にしたものを使う
     * */
    public static List<Function<ItemStack, ItemStack>> CrushRecipeRules = new ArrayList<>();
    public static HashMap<ItemStack, Function<ItemStack, ItemStack>> CrushRecipes = new HashMap<>();

    public static void Init() {
        AddCrushRecipeRules(stack -> {
            if(OreDictionary.getOreIDs(stack).length > 0) {
                String name = OreDictionary.getOreName(OreDictionary.getOreIDs(stack)[0]);
                if (name.length() >= 3) {
                    if (name.startsWith("ore")) {
                        NonNullList<ItemStack> ores = OreDictionary.getOres("dust" + name.substring(3));
                        if (ores.size() > 0) {
                            return new ItemStack(ores.get(0).getItem(), 2, ores.get(0).getMetadata());
                        }
                    } else if (name.startsWith("ingot")) {
                        NonNullList<ItemStack> ores = OreDictionary.getOres("dust" + name.substring(5));
                        if (ores.size() > 0) {
                            return new ItemStack(ores.get(0).getItem(), 1, ores.get(0).getMetadata());
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        });

        AddCrushRecipeRules(stack -> {
            if(OreDictionary.getOreIDs(stack).length > 0) {
                String name = OreDictionary.getOreName(OreDictionary.getOreIDs(stack)[0]);
                if (name.length() >= 3) {
                    if (name.startsWith("crystal")) {
                        NonNullList<ItemStack> ores = OreDictionary.getOres("dust" + name.substring(7));
                        if (ores.size() > 0) {
                            return new ItemStack(ores.get(0).getItem(), 1, ores.get(0).getMetadata());
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        });

        AddCrushRecipeRules(stack -> {
            if(stack.getItem().getRegistryName().getResourcePath().equals(Blocks.STONE.getRegistryName().getResourcePath())) {
                return new ItemStack(Blocks.COBBLESTONE, 1);
            } else if(stack.getItem().getRegistryName().getResourcePath().equals(Blocks.COBBLESTONE.getRegistryName().getResourcePath()) || stack.getItem().getRegistryName().getResourcePath().equals(Blocks.NETHERRACK.getRegistryName().getResourcePath())) {
                return new ItemStack(Blocks.GRAVEL, 1);
            } else if(stack.getItem().getRegistryName().getResourcePath().equals(Blocks.GRAVEL.getRegistryName().getResourcePath())) {
                return new ItemStack(Blocks.SAND, 1);
            }
            return ItemStack.EMPTY;
        });

        AddCrushRecipeRules(stack -> {
            if(OreDictionary.getOreIDs(stack).length > 0) {
                String name = OreDictionary.getOreName(OreDictionary.getOreIDs(stack)[0]);
                if(name.equals("dustSulfur"))
                    return new ItemStack(Items.GUNPOWDER, 4);
            }
            return ItemStack.EMPTY;
        });

        AddCrushRecipe(Blocks.NETHER_BRICK, stack -> new ItemStack(Items.NETHERBRICK, 4));
        AddCrushRecipe(Blocks.BRICK_BLOCK, stack -> new ItemStack(Items.BRICK, 4));
        AddCrushRecipe(Blocks.STONE_SLAB, stack -> new ItemStack(Blocks.STONE_SLAB, 1, 3));
        AddCrushRecipe(Blocks.QUARTZ_BLOCK, stack -> new ItemStack(Items.QUARTZ, 4));
        AddCrushRecipe(Blocks.QUARTZ_STAIRS, stack -> new ItemStack(Items.QUARTZ, 3));
        AddCrushRecipe(Blocks.STONE_STAIRS, stack -> new ItemStack(Blocks.GRAVEL, 3));
        AddCrushRecipe(Blocks.BRICK_STAIRS, stack -> new ItemStack(Items.BRICK, 3));
        AddCrushRecipe(Blocks.NETHER_BRICK_STAIRS, stack -> new ItemStack(Blocks.NETHER_BRICK, 3));
        AddCrushRecipe(Blocks.MAGMA, stack -> new ItemStack(Items.MAGMA_CREAM, 4));
        AddCrushRecipe(Blocks.GLOWSTONE, stack -> new ItemStack(Items.GLOWSTONE_DUST, 4));
    }

    public static void AddCrushRecipeRules(Function<ItemStack, ItemStack> function) {
        CrushRecipeRules.add(function);
    }

    public static void AddCrushRecipe(ItemStack Input, Function<ItemStack, ItemStack> function) {
        CrushRecipes.put(Input, function);
    }

    public static void AddCrushRecipe(Item Input, Function<ItemStack, ItemStack> function) {
        CrushRecipes.put(new ItemStack(Input), function);
    }

    public static void AddCrushRecipe(Block Input, Function<ItemStack, ItemStack> function) {
        CrushRecipes.put(new ItemStack(Input), function);
    }
}
