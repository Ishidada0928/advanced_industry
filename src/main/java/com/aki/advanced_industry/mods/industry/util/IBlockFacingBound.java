package com.aki.advanced_industry.mods.industry.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public interface IBlockFacingBound {
    HashMap<EnumFacing, AxisAlignedBB> getFacingBoundingBox(World world, BlockPos pos, EntityPlayer player, boolean hasWrench);
}
