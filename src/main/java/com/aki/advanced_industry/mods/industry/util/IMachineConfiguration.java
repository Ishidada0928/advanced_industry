package com.aki.advanced_industry.mods.industry.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

public interface IMachineConfiguration {
    EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult);

    EnumActionResult onRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult);
}
