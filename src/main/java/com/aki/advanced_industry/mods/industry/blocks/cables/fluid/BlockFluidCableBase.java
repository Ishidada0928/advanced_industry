package com.aki.advanced_industry.mods.industry.blocks.cables.fluid;

import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.block.BlockBase;
import com.aki.advanced_industry.mods.industry.tileentities.cables.fluid.TileFluidCableBase;
import com.aki.advanced_industry.mods.industry.util.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.IBlockFacingBound;
import com.aki.advanced_industry.mods.industry.util.WrenchUtil;
import com.google.common.collect.Lists;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class BlockFluidCableBase extends BlockBase implements IBlockFacingBound {
    public AxisAlignedBB boundBox = FULL_BLOCK_AABB;
    public AxisAlignedBB baseBox = new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F);
    public BlockFluidCableBase(Material m) {
        super(m);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if(!world.isRemote) {
            TileEntity tile1 = world.getTileEntity(pos);
            if(tile1 instanceof TileFluidCableBase) {
                TileFluidCableBase fluidCable1 = (TileFluidCableBase) tile1;
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : fluidCable1.facingMode.entrySet()) {
                    BlockPos side_pos = pos.offset(entry.getKey());
                    TileEntity tile2 = world.getTileEntity(side_pos);
                    if (tile2 instanceof TileFluidCableBase) {
                        TileFluidCableBase fluidCable2 = (TileFluidCableBase) tile2;
                        if (fluidCable2.facingMode.get(entry.getKey().getOpposite()) != CableConnectionMode.CLOSE) {
                            fluidCable1.CableConnectionFacing[entry.getKey().getIndex()] = entry.getKey();
                            fluidCable1.facingMode.replace(entry.getKey(), CableConnectionMode.NORMAL);
                            fluidCable1.renderFacingMode.replace(entry.getKey(), CableConnectionMode.NORMAL);
                            fluidCable2.facingMode.replace(entry.getKey().getOpposite(), CableConnectionMode.NORMAL);
                            fluidCable2.renderFacingMode.replace(entry.getKey().getOpposite(), CableConnectionMode.NORMAL);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return true;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return boundBox.offset(pos);
    }

    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, baseBox);
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            for (Map.Entry<EnumFacing, CableConnectionMode> entry : ((TileFluidCableBase) tile).renderFacingMode.entrySet()) {
                if(entry.getValue() != CableConnectionMode.CLOSE)
                    addCollisionBoxToList(pos, entityBox, collidingBoxes, this.getFacingBoundingBox(worldIn, pos, null, false).get(entry.getKey()));
            }
        }
    }

    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        List<AxisAlignedBB> aabbList = new ArrayList<>(Lists.newArrayList(baseBox));
        TileEntity tile = worldIn.getTileEntity(pos);
        EntityPlayer player = worldIn.getClosestPlayer(start.x, start.y, start.z, 5.0d, false);
        if(tile instanceof TileFluidCableBase && player != null) {
            boolean PlayerHasWrench = WrenchUtil.PlayerHasWrench(player);
            for (Map.Entry<EnumFacing, CableConnectionMode> entry : ((TileFluidCableBase) tile).renderFacingMode.entrySet()) {
                if(entry.getValue() != CableConnectionMode.CLOSE || PlayerHasWrench)
                    aabbList.add(getFacingBoundingBox(worldIn, pos, player, PlayerHasWrench).get(entry.getKey()));
            }
        }

        List<RayTraceResult> rayTraceResults = new ArrayList<>();
        for(AxisAlignedBB alignedBB : aabbList) {
            rayTraceResults.add(this.rayTrace(pos, start, end, alignedBB));
        }

        RayTraceResult longRay = null;
        double dist = 0;
        for (int i = 0; i < rayTraceResults.size(); i++ ) {
            if (rayTraceResults.get(i) != null) {
                if (rayTraceResults.get(i).hitVec.squareDistanceTo(end) > dist) {
                    longRay = rayTraceResults.get(i);
                    dist = rayTraceResults.get(i).hitVec.squareDistanceTo(end);
                    this.boundBox = aabbList.get(i);
                }
            }
        }


        return longRay;
    }

    @Override
    public LinkedHashMap<EnumFacing, AxisAlignedBB> getFacingBoundingBox(World world, BlockPos pos, EntityPlayer player, boolean hasWrench) {
        LinkedHashMap<EnumFacing, AxisAlignedBB> facingMap = new LinkedHashMap<>();
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileFluidCableBase) {
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.DOWN) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.DOWN, new AxisAlignedBB(0.3125F, 0.0F, 0.3125F, 0.6875F, 0.3125F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.UP) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.UP, new AxisAlignedBB(0.3125F, 0.3125F, 0.3125F, 0.6875F, 1.0F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.NORTH) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.NORTH, new AxisAlignedBB(0.3125F, 0.3125F, 0.0F, 0.6875F, 0.6875F, 0.375));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.SOUTH) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.SOUTH, new AxisAlignedBB(0.3125F, 0.3125F, 0.6875F, 0.6875F, 0.6875F, 1.0F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.WEST) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.WEST, new AxisAlignedBB(0.0F, 0.3125F, 0.3125F, 0.3125F, 0.6875F, 0.6875F));
            if (((TileFluidCableBase) tile).renderFacingMode.get(EnumFacing.EAST) != CableConnectionMode.CLOSE || hasWrench)
                facingMap.put(EnumFacing.EAST, new AxisAlignedBB(0.6875F, 0.3125F, 0.3125F, 1.0F, 0.6875F, 0.6875F));
        }
        return facingMap;
    }

    public abstract int getMaxSendFluid();

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TextComponentTranslation("tooltip.fluid_pipe1.info", this.getMaxSendFluid()).getUnformattedComponentText());
        tooltip.add(new TextComponentTranslation("tooltip.fluid_pipe2.info").getUnformattedComponentText());
        super.addInformation(stack, player, tooltip, advanced);
    }
}
