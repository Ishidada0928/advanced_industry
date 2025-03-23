package com.aki.advanced_industry;

import com.aki.advanced_industry.api.util.RaytraceUtil;
import com.aki.advanced_industry.mods.industry.util.WrenchUtil;
import com.aki.advanced_industry.mods.industry.util.implement.IBlockFacingBound;
import com.aki.advanced_industry.mods.industry.util.implement.IMachineConfiguration;
import com.aki.akisutils.apis.util.list.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class AdvancedIndustryEvents {
    private static long LastChangeTime = 0L;
    @SubscribeEvent
    public void OnRightClickEvent(PlayerInteractEvent.RightClickBlock rightClickBlock) {
        EntityPlayer playerIn = rightClickBlock.getEntityPlayer();
        World worldIn = rightClickBlock.getWorld();
        EnumHand handIn = rightClickBlock.getHand();
        BlockPos pos = rightClickBlock.getPos();
        if(!playerIn.isSpectator() && WrenchUtil.PlayerHasWrench(playerIn)) {
            ItemStack stack = playerIn.getHeldItem(handIn);
            if(!stack.isEmpty() && !worldIn.isRemote && (Minecraft.getSystemTime() - LastChangeTime) >= 5) {
                TileEntity tile = worldIn.getTileEntity(pos);
                Pair<Vec3d, Vec3d> pair = RaytraceUtil.getRayTraceVectors(playerIn);
                RayTraceResult rayTraceResult = worldIn.rayTraceBlocks(pair.getKey(), pair.getValue());
                EnumFacing facing = rightClickBlock.getFace();
                Block block = worldIn.getBlockState(pos).getBlock();
                if(block instanceof IBlockFacingBound) {
                    if(rayTraceResult != null) {
                        double dist = Double.POSITIVE_INFINITY;
                        Pair<Vec3d, Vec3d> vecs = RaytraceUtil.getRayTraceVectors(playerIn);
                        for (Map.Entry<EnumFacing, AxisAlignedBB> entry : ((IBlockFacingBound) block).getFacingBoundingBox(worldIn, pos, playerIn, WrenchUtil.PlayerHasWrench(playerIn)).entrySet()) {
                            RayTraceResult result = entry.getValue().offset(pos).calculateIntercept(vecs.getKey(), vecs.getValue());
                            if (result != null && dist > result.hitVec.distanceTo(vecs.getKey())) {
                                dist = result.hitVec.distanceTo(vecs.getKey());
                                facing = entry.getKey();
                                rayTraceResult = result;
                            }
                        }
                    }
                }
                if(tile instanceof IMachineConfiguration && rayTraceResult != null) {
                    if(playerIn.isSneaking()) {
                        rightClickBlock.setCancellationResult(((IMachineConfiguration) tile).onSneakRightClick(playerIn, facing, rayTraceResult));
                    } else {
                        rightClickBlock.setCancellationResult(((IMachineConfiguration) tile).onRightClick(playerIn, facing, rayTraceResult));
                    }
                }
                LastChangeTime = Minecraft.getSystemTime();
            }
        }
    }
}