package com.aki.advanced_industry.mods.industry.items.tools;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWrench extends Item {
    public ItemWrench() {
        this.setNoRepair();
        this.maxStackSize = 1;
        this.setCreativeTab(ModMaterials.tabs);
        this.setUnlocalizedName("wrench");
        this.setRegistryName(AdvancedIndustryCore.ModID, "wrench");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TextComponentTranslation("tooltip.wrench.info").getUnformattedText());
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    /*@Override
    public EnumActionResult onItemUseFirst(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if(!stack.isEmpty() && !worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            RayTraceResult rayTraceResult = this.rayTrace(worldIn, playerIn, true);
            EnumFacing facing = side;
            Block block = worldIn.getBlockState(pos).getBlock();
            if(block instanceof IBlockFacingBound) {
                if(rayTraceResult != null) {
                    double dist = Double.POSITIVE_INFINITY;
                    Pair<Vec3d, Vec3d> vecs = RaytraceUtil.getRayTraceVectors(playerIn);
                    for (Map.Entry<EnumFacing, AxisAlignedBB> entry : ((IBlockFacingBound) block).getFacingBoundingBox(worldIn, pos).entrySet()) {
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
                    return ((IMachineConfiguration) tile).onSneakRightClick(playerIn, facing, rayTraceResult);
                } else {
                    return ((IMachineConfiguration) tile).onRightClick(playerIn, facing, rayTraceResult);
                }
            }
        }
        return EnumActionResult.PASS;
    }*/
}
