package com.aki.advanced_industry.mods.industry.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class WrenchUtil {
    private static final List<Function<Item, Boolean>> Wrenches_Condition = new ArrayList<>();

    public static void AddWrench(Function<Item, Boolean> wrench_condition) {
        Wrenches_Condition.add(wrench_condition);
    }

    //ActiveHandはクリックしたときに呼び出される。
    public static boolean PlayerHasWrench(EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            for (Function<Item, Boolean> function : Wrenches_Condition) {
                if (function.apply(item))
                    return true;
            }
        }
        return false;
    }
}
