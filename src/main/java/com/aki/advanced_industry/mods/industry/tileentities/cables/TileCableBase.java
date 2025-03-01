package com.aki.advanced_industry.mods.industry.tileentities.cables;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import com.aki.advanced_industry.mods.industry.util.enums.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.implement.IMachineConfiguration;
import com.aki.advanced_industry.mods.industry.util.network.cable.EnergyCableManager;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import java.util.HashMap;

public abstract class TileCableBase extends TileEntityBase implements IMachineConfiguration {
    public final HashMap<EnumFacing, CableConnectionMode> facingMode = new HashMap<>();
    public long Tick = -1L;
    public long LastChangeTick = 0L;
    public long LastUpdateTick = 0L;
    public int ECM_Index = -1;

    public TileCableBase() {
        for(EnumFacing facing : EnumFacing.VALUES) {
            this.facingMode.put(facing, CableConnectionMode.NORMALCLOSE);
        }
    }

    @Override
    public void validate() {
        super.validate();
    }

    //周りのFacingModeをリセット
    @Override
    public void invalidate() {
        super.invalidate();
        if(!this.world.isRemote) {
            for(EnumFacing facing : EnumFacing.VALUES) {
                TileEntity tile = this.world.getTileEntity(this.getPos().offset(facing));
                if(tile instanceof TileCableBase) {
                    TileCableBase cableBase = (TileCableBase) tile;
                    cableBase.SetFacingState(facing.getOpposite(), CableConnectionMode.NORMALCLOSE);
                }
            }
        }
    }

    public long getLastUpdateTick() {
        return this.LastUpdateTick;
    }

    public int getECM_Index() {
        return this.ECM_Index;
    }

    public void setECM_Index(int index) {
        this.ECM_Index = index;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.Tick = compound.getLong("Tick");
        this.LastChangeTick = compound.getLong("LastChangeTick");
        this.LastUpdateTick = compound.getLong("LastUpdateTick");
        for(EnumFacing facing : EnumFacing.VALUES) {
            this.facingMode.put(facing, CableConnectionMode.getCableMode(compound.getInteger("ModeFacing_" + facing.getName())));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setLong("Tick", this.Tick);
        compound.setLong("LastChangeTick", this.LastChangeTick);
        compound.setLong("LastUpdateTick", this.LastUpdateTick);

        for(EnumFacing facing : EnumFacing.VALUES) {
            compound.setInteger("ModeFacing_" + facing.getName(), this.facingMode.get(facing).getIndex());
        }
        return compound;
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager data = super.getNetWorkData();
        data.addDataInt(this.ECM_Index);
        for(EnumFacing facing : EnumFacing.VALUES) {
            data.addDataInt(this.facingMode.get(facing).getIndex());
        }

        return data;
    }

    @Override
    public void ReceivePacketData(DataListManager data) {
        super.ReceivePacketData(data);
        this.ECM_Index = data.getDataInt();
        for(EnumFacing facing : EnumFacing.VALUES) {
            this.facingMode.put(facing, CableConnectionMode.getCableMode(data.getDataInt()));
        }
    }

    public abstract void ChangeFacingState(EnumFacing facing);

    public void SetFacingState(EnumFacing facing, CableConnectionMode mode) {
        this.facingMode.put(facing, mode);
        this.sendUpdates();
    }

    public HashMap<EnumFacing, CableConnectionMode> getFacingMode() {
        return this.facingMode;
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        if (!this.world.isRemote) {
            this.invalidate();
            this.world.destroyBlock(this.getPos(), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        if(!this.world.isRemote)
            this.ChangeFacingState(side);
        return EnumActionResult.SUCCESS;
    }
}