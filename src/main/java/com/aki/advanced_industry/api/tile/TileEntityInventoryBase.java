package com.aki.advanced_industry.api.tile;

import com.aki.akisutils.apis.data.manager.DataListManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileEntityInventoryBase extends TileEntityBase {
    public ItemStackHandler Inventory;
    public TileEntityInventoryBase(ItemStackHandler handler) {
        super();
        if(handler == null)
            throw new NullPointerException();
        this.Inventory = handler;
        this.addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (facing) -> TileEntityInventoryBase.this.Inventory);
    }

    public TileEntityInventoryBase(int maxEnergyStored, ItemStackHandler handler) {
        super(maxEnergyStored);
        if(handler == null)
            throw new NullPointerException();
        this.Inventory = handler;
        this.addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, (facing) -> TileEntityInventoryBase.this.Inventory);
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
}