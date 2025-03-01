package com.aki.advanced_industry.mods.industry.blocks.machines;

import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.ModMaterials;
import com.aki.advanced_industry.api.block.BlockBase;
import com.aki.advanced_industry.mods.industry.tileentities.misc.TileCompressionCrusher;
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

public class BlockCompressionCrusher extends BlockBase {
    public BlockCompressionCrusher() {
        super(Material.ROCK);
        this.blockHardness = 3.0F;
        this.blockResistance = 5.0F;
        this.blockSoundType = SoundType.STONE;
        this.setRegistryName(AdvancedIndustryCore.ModID, "compression_crusher");
        this.setUnlocalizedName("compression_crusher");
        this.setCreativeTab(ModMaterials.tabs);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        TileEntity tile = worldIn.getTileEntity(pos);
        if(!worldIn.isRemote) {
            if(tile instanceof TileCompressionCrusher) {
                TileCompressionCrusher crusher = (TileCompressionCrusher) tile;
                if(((TileCompressionCrusher) tile).IsItemStackEmpty(crusher.Inventory.getStackInSlot(0))) {
                    if (!playerIn.inventory.getCurrentItem().isEmpty()) {
                        crusher.InputStack(playerIn.inventory.getCurrentItem());
                    }
                } else {
                    crusher.OnClick();
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileCompressionCrusher();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
