package com.aki.advanced_industry.mods.industry.blocks;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.api.block.BlockBase;
import com.aki.advanced_industry.mods.industry.tileentities.TileLagChecker;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockLagChecker extends BlockBase {
    public BlockLagChecker() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "lag_checker");
        this.setUnlocalizedName("lag_checker");
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {

            TileEntity tile = worldIn.getTileEntity(pos);
            if(tile instanceof TileLagChecker) {
                ((TileLagChecker) tile).OnClick(playerIn);
            }
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileLagChecker();
    }
}
