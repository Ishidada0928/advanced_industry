package com.aki.advanced_industry.api.tile;

import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileEntityInventoryBase extends TileEntityBase {
    public ItemStackHandler Inventory;
    public TileEntityInventoryBase(ItemStackHandler handler) {
        super(0);
        if(handler == null)
            throw new NullPointerException();
        this.Inventory = handler;
    }

    public TileEntityInventoryBase(int maxEnergyStored, ItemStackHandler handler) {
        super(maxEnergyStored);
        if(handler == null)
            throw new NullPointerException();
        this.Inventory = handler;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.Inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("Inventory", this.Inventory.serializeNBT());
        return compound;
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager dataListManager = super.getNetWorkData();
        dataListManager.addDataTag(this.Inventory.serializeNBT());
        return dataListManager;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        this.lastChangeTime = dataListManager.getDataLong();
        this.Inventory.deserializeNBT(dataListManager.getDataTag());
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing)
    {
        if (capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) (this.Inventory);
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability, @javax.annotation.Nullable net.minecraft.util.EnumFacing facing)
    {
        return capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }
}