package com.aki.advanced_industry.mods.industry.tileentities.cables.fluid;

import com.aki.advanced_industry.mods.industry.util.enums.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.implement.IFluidCableConnector;
import com.aki.advanced_industry.mods.industry.util.implement.IMachineConfiguration;
import com.aki.advanced_industry.api.tile.TileEntityBase;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * 液体はテレポートで運ぶ (材料にエンダーパールを使うといいかも 1個で16できるみたいな)
 * */
public abstract class TileFluidCableBase extends TileEntityBase implements IFluidCableConnector, IMachineConfiguration, IFluidHandler {
    public int MaxSendFluid;
    public int FluidReceivers = 0;
    public HashMap<EnumFacing, CableConnectionMode> facingMode = new HashMap<>();

    //
    public HashMap<EnumFacing, CableConnectionMode> renderFacingMode = new HashMap<>();
    public FluidStack StorageFluid = null;
    public int SendFluidBase = 0;
    public long Tick = 0;

    public HashSet<BlockPos> CornerLocation = new HashSet<>();
    public EnumFacing[] CableConnectionFacing = new EnumFacing[6];
    public boolean ValidInit = false;

    //basic 2000
    //advanced 6000
    //extreme 18000
    //ultimate 144000
    public TileFluidCableBase(int maxFluid) {
        this.MaxSendFluid = maxFluid;
        for(EnumFacing facing : EnumFacing.VALUES) {
            facingMode.put(facing, CableConnectionMode.NORMAL);
            renderFacingMode.put(facing, CableConnectionMode.CLOSE);
        }
    }

    @Override
    public void update() {
        super.update();
        if(!world.isRemote) {
            Tick++;
            boolean will_update = false;
            if(this.StorageFluid != null && this.StorageFluid.amount <= 0)
                this.StorageFluid = null;
            CornerLocation.clear();
            if(Tick % 5 == 0 || ValidInit) {
                this.FluidReceivers = 0;
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : facingMode.entrySet()) {
                    CableConnectionFacing[entry.getKey().getIndex()] = null;
                    renderFacingMode.replace(entry.getKey(), entry.getValue());
                    TileEntity tile = world.getTileEntity(this.getPos().add(entry.getKey().getDirectionVec()));
                    EnumFacing facing = entry.getKey();
                    switch (entry.getValue()) {
                        case NORMAL:
                            if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                this.FluidReceivers++;
                                will_update = true;
                            }
                            if (tile instanceof TileFluidCableBase && ((TileFluidCableBase) tile).facingMode.get(facing) != CableConnectionMode.CLOSE) {
                                this.CornerLocation.add(this.pos);
                                CableConnectionFacing[entry.getKey().getIndex()] = entry.getKey();
                                ((TileFluidCableBase) tile).facingMode.replace(facing.getOpposite(), CableConnectionMode.NORMAL);
                                if(this.StorageFluid != null && this.StorageFluid.amount > 0)
                                    ((TileFluidCableBase) tile).FluidReceiverCountCheck(this.getPos(), this.Tick);
                                will_update = true;
                            }
                            if(!(tile instanceof TileFluidCableBase || tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()))) {
                                renderFacingMode.replace(entry.getKey(), CableConnectionMode.CLOSE);
                                will_update = true;
                            }
                            break;
                        case PUSH:
                        case PULL:
                            if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                this.FluidReceivers++;
                                will_update = true;
                            }
                            break;
                    }
                }
                this.ValidInit = false;
            }

            CornerLocation.clear();

            if(this.FluidReceivers > 0) {
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : facingMode.entrySet()) {
                    TileEntity tile = world.getTileEntity(this.getPos().add(entry.getKey().getDirectionVec()));
                    EnumFacing facing = entry.getKey();
                    if(this.StorageFluid != null) {
                        this.SendFluidBase = this.StorageFluid.amount;
                    }
                    switch (entry.getValue()) {
                        case NORMAL:
                            if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()) && this.StorageFluid != null && this.StorageFluid.amount > 0) {
                                AddFluidStack(-tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).fill(new FluidStack(this.StorageFluid.getFluid(), this.DividedNotRemainder(Math.min(MaxSendFluid, SendFluidBase), this.FluidReceivers)), true));
                                will_update = true;
                            }
                            /*if(this.StorageFluid == null || this.StorageFluid.amount <= this.MaxSendFluid) {
                                if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                    FluidStack stack = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).drain(MaxSendFluid - (StorageFluid != null ? StorageFluid.amount : 0), true);
                                    if(stack != null && (this.StorageFluid == null || this.StorageFluid.getFluid() == stack.getFluid())) {
                                        AddFluidStack(stack, stack.amount);
                                    }
                                }
                            }*/
                            if (tile instanceof TileFluidCableBase && ((TileFluidCableBase) tile).facingMode.get(facing.getOpposite()) != CableConnectionMode.CLOSE && this.StorageFluid != null && this.StorageFluid.amount > 0) {
                                this.CornerLocation.add(this.pos);
                                ((TileFluidCableBase) tile).SendFluid(this.getPos(), this.Tick, Math.min(((TileFluidCableBase) tile).MaxSendFluid, this.MaxSendFluid));
                                will_update = true;
                            }

                            break;
                        case PUSH:
                            if(!(tile instanceof TileFluidCableBase)) {
                                if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()) && this.StorageFluid != null && this.StorageFluid.amount > 0) {
                                    AddFluidStack(-tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).fill(new FluidStack(this.StorageFluid.getFluid(), this.DividedNotRemainder(Math.min(MaxSendFluid, SendFluidBase), this.FluidReceivers)), true));
                                    will_update = true;
                                }
                            }
                            break;
                        case PULL:
                            if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                FluidStack stack = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).drain(MaxSendFluid - (StorageFluid != null ? StorageFluid.amount : 0), true);
                                if(stack != null && (this.StorageFluid == null || this.StorageFluid.getFluid() == stack.getFluid())) {
                                    AddFluidStack(stack, stack.amount);
                                    will_update = true;
                                }
                            }
                            break;
                    }
                }

            }
            if(will_update)
                this.sendUpdates();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if(!this.world.isRemote)
            this.ValidInit = true;
    }

    private void AddFluidStack(FluidStack stack, int add) {
        int size = add;
        if(this.StorageFluid != null)
            size += this.StorageFluid.amount;
        this.StorageFluid = new FluidStack(stack.getFluid(), size);
    }
    private void AddFluidStack(int add) {
        if(this.StorageFluid == null)
            return;
        this.StorageFluid = new FluidStack(this.StorageFluid.getFluid(), this.StorageFluid.amount + add);
    }

    private int DividedNotRemainder(int Input, int by) {
        int Remainder = Input % by;
        return (Input - Remainder) / by;
    }

    @Override
    public void SendFluid(BlockPos StartPos, long tick, int Max) {
        TileEntity StartTile = world.getTileEntity(StartPos);
        if (StartTile instanceof TileFluidCableBase) {
            if(!((TileFluidCableBase) StartTile).CornerLocation.contains(this.pos)) {
                ((TileFluidCableBase) StartTile).CornerLocation.add(this.pos);
                for (Map.Entry<EnumFacing, CableConnectionMode> entry : this.facingMode.entrySet()) {
                    EnumFacing facing = entry.getKey();
                    TileEntity tile = world.getTileEntity(this.getPos().add(facing.getDirectionVec()));

                    switch (entry.getValue()) {
                        case NORMAL:
                        case PUSH:
                            if (!(tile instanceof TileFluidCableBase)) {
                                if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()) && ((TileFluidCableBase) StartTile).StorageFluid != null && ((TileFluidCableBase) StartTile).StorageFluid.amount > 0) {
                                    ((TileFluidCableBase) StartTile).AddFluidStack(-tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite()).fill(new FluidStack(((TileFluidCableBase) StartTile).StorageFluid.getFluid(), this.DividedNotRemainder(Math.min(Max, ((TileFluidCableBase) StartTile).SendFluidBase), ((TileFluidCableBase) StartTile).FluidReceivers)), true));
                                }
                            }

                            if (tile instanceof TileFluidCableBase && ((TileFluidCableBase) tile).facingMode.get(facing.getOpposite()) != CableConnectionMode.CLOSE) {
                                long nextTick = ((TileFluidCableBase) tile).Tick;
                                if (nextTick != tick && nextTick != this.Tick) {//前のTickと今のTickとは次が違う場合
                                    if (((TileFluidCableBase) StartTile).StorageFluid != null && ((TileFluidCableBase) StartTile).StorageFluid.amount > 0) {
                                        ((TileFluidCableBase) tile).SendFluid(StartPos, this.Tick, Math.min(Max, ((TileFluidCableBase) tile).MaxSendFluid));
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void FluidReceiverCountCheck(BlockPos StartPos, long tick) {
        TileEntity StartTile = world.getTileEntity(StartPos);
        if(StartTile instanceof TileFluidCableBase) {
            if(!((TileFluidCableBase) StartTile).CornerLocation.contains(this.pos)) {
                ((TileFluidCableBase) StartTile).CornerLocation.add(this.pos);
                for(Map.Entry<EnumFacing, CableConnectionMode> entry : this.facingMode.entrySet()) {
                    EnumFacing facing = entry.getKey();
                    TileEntity tile = world.getTileEntity(this.getPos().add(facing.getDirectionVec()));

                    switch (entry.getValue()) {
                        case NORMAL:
                        case PUSH:
                            if(tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                                this.FluidReceivers++;
                            }

                            if (tile instanceof TileFluidCableBase) {
                                long nextTick = ((TileFluidCableBase) tile).Tick;
                                if (nextTick != tick && nextTick != this.Tick) {//前のTickと今のTickとは次が違う場合
                                    if (((TileFluidCableBase) StartTile).StorageFluid != null && ((TileFluidCableBase) StartTile).StorageFluid.amount > 0) {
                                        ((TileFluidCableBase) tile).FluidReceiverCountCheck(StartPos, this.Tick);
                                    }
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager data = super.getNetWorkData();
        for(EnumFacing facing : this.facingMode.keySet()) {
            data.addData(this.renderFacingMode.get(facing).getIndex());
        }
        return data;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        super.ReceivePacketData(dataListManager);
        for(EnumFacing facing : this.facingMode.keySet()) {
            this.renderFacingMode.replace(facing, CableConnectionMode.getCableMode(dataListManager.getDataInt()));
        }
    }

    public void ChangeFacingState(EnumFacing facing) {
        if(this.CableConnectionFacing[facing.getIndex()] == null) {
            CableConnectionMode mode = this.facingMode.get(facing);
            CableConnectionMode next = CableConnectionMode.values()[(mode.getIndex() + 1) % CableConnectionMode.values().length];
            this.facingMode.replace(facing, next);
        } else {
            if(this.facingMode.get(facing) == CableConnectionMode.NORMAL) {
                this.facingMode.replace(facing, CableConnectionMode.CLOSE);
                TileEntity tile = world.getTileEntity(this.getPos().offset(facing));
                if(tile instanceof TileFluidCableBase) {
                    ((TileFluidCableBase) tile).facingMode.replace(facing.getOpposite(), CableConnectionMode.CLOSE);
                }
            } else {
                this.facingMode.replace(facing, CableConnectionMode.NORMAL);
            }
        }

        this.sendUpdates();
    }

    @Override
    public EnumActionResult onSneakRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        if(!world.isRemote) {
            this.invalidate();
            world.destroyBlock(this.getPos(), true);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public EnumActionResult onRightClick(EntityPlayer player, EnumFacing side, RayTraceResult rayTraceResult) {
        this.ChangeFacingState(side);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.getBoolean("HasFluid")) {
            this.StorageFluid = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("Fluid"));
        }
        this.FluidReceivers = compound.getInteger("FluidReceivers");
        this.Tick = compound.getLong("Tick");
        for(EnumFacing facing : EnumFacing.VALUES) {
            this.facingMode.replace(facing, CableConnectionMode.getCableMode(compound.getInteger("CableModeIndex_" + facing.getName())));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("HasFluid", this.StorageFluid != null);
        if(this.StorageFluid != null)
            compound.setTag("Fluid", this.StorageFluid.writeToNBT(new NBTTagCompound()));
        compound.setInteger("FluidReceivers", this.FluidReceivers);
        compound.setLong("Tick", this.Tick);
        for(EnumFacing facing : EnumFacing.VALUES) {
            compound.setInteger("CableModeIndex_" + facing.getName(), this.facingMode.get(facing).getIndex());
        }
        return compound;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (this.StorageFluid != null) {
            FluidTankProperties properties = new FluidTankProperties(this.StorageFluid, this.MaxSendFluid);
            return new IFluidTankProperties[]{properties};
        }
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (this.StorageFluid != null && resource.getFluid() == this.StorageFluid.getFluid()) {
            int remaining = this.MaxSendFluid - this.StorageFluid.amount;
            int filled = Math.min(remaining - resource.amount, Math.min(this.MaxSendFluid, resource.amount));
            if (doFill) {
                this.StorageFluid.amount += filled;
            }
            return filled;
        } else {
            int Input = Math.min(resource.amount, this.MaxSendFluid);
            FluidStack stack = resource.copy();
            stack.amount = Input;
            this.StorageFluid = stack;
            return Input;
        }
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing)) && this.facingMode.get(facing) != CableConnectionMode.CLOSE;
    }

    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this.facingMode.get(facing) != CableConnectionMode.CLOSE ? (T) this : super.getCapability(capability, facing);
        //return super.getCapability(capability, facing);
    }
}
