package com.aki.advanced_industry.mods.industry.tileentities.cables.fluid;

import com.aki.advanced_industry.mods.industry.tileentities.cables.TileCableBase;
import com.aki.advanced_industry.mods.industry.util.enums.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.implement.IMachineConfiguration;
import com.aki.advanced_industry.mods.industry.util.network.cable.FluidCableManager;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

/**
 * 液体はテレポートで運ぶ (材料にエンダーパールを使うといいかも 1個で16できるみたいな)
 * このパイプで同時に運べる液体は一種類だけ。
 * ネットワーク全体でタンクの機能を持たせる(容量：それぞれのMaxSendFluidの和)。
 * -> 基本はそれぞれのパイプに平等に分配(それぞれのMaxSendFluidを考慮する)。
 *    パイプ内の液体の明るさを反映する。/
 * 液体はパイプの中心を通るようにする。
 * NORMALは Extract はしない
 * */
public class TileFluidCableBase extends TileCableBase implements IMachineConfiguration, IFluidHandler {
    private final int MaxSendFluid;
    private FluidCableManager cableManager;
    private FluidStack fluidStack = null;

    public TileFluidCableBase(int maxSendFluid) {
        super();
        this.MaxSendFluid = maxSendFluid;
        this.cableManager = new FluidCableManager(this);
        this.addCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, (facing) -> this);
    }

    @Override
    public void update() {
        super.update();
        if(!this.world.isRemote) {
            //初期化
            if(this.Tick < 0) {
                for(EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity tile = this.world.getTileEntity(this.getPos().offset(facing));
                    if(tile instanceof TileFluidCableBase) {
                        TileFluidCableBase cableBase = (TileFluidCableBase) tile;
                        cableBase.SetFacingState(facing.getOpposite(), CableConnectionMode.NORMAL);
                        this.facingMode.put(facing, CableConnectionMode.NORMAL);
                    }
                }
                this.sendUpdates();
            }

            this.Tick++;

            boolean ThisHost = this.cableManager.getHostPos() == this.pos;

            if(1000000L <= this.Tick) {
                this.Tick = 0L;
                this.LastChangeTick = 0L;
            }

            //ケーブルが途中で切れたときに新しいホストに設定。
            if((this.LastChangeTick + 2) <= this.Tick && !ThisHost) {
                this.cableManager = new FluidCableManager(this);
                ThisHost = true;
            }

            if(ThisHost) {
                this.cableManager.Reset();
                this.cableManager.PutCableTile(this, this.MaxSendFluid);
                this.cableManager.setHostTick(this.Tick);
                //Hostだけ処理する(全探索)。
                this.ScanCables();
                this.cableManager.SeparateNetworks();
                for(TileFluidCableBase cable : this.cableManager.getFluidCables().values()) {
                    cable.ScanMachines();
                }

                /*
                 * ここで送受信の処理
                 */
                this.cableManager.Update(this.world);
            }
        } else {

        }
    }

    //ここでFacingModeを適用
    public void ScanCables() {
        for(EnumFacing facing : EnumFacing.VALUES) {
            TileEntity tile = this.world.getTileEntity(this.getPos().offset(facing));
            if(tile instanceof TileFluidCableBase) {
                TileFluidCableBase cableBase = (TileFluidCableBase) tile;
                //CableManager が違う場合 CableID が一致しない
                if(((cableBase.getCableManager().getCableID() != this.cableManager.getCableID()) || (cableBase.getLastUpdateTick() != this.cableManager.getHostTick())) && this.facingMode.get(facing) == CableConnectionMode.NORMAL && cableBase.facingMode.get(facing.getOpposite()) == CableConnectionMode.NORMAL) {
                    cableBase.OverWriteCableManger(this.cableManager);
                    cableBase.ScanCables();
                }
            }
        }
    }

    //HostのCableManagerに追加
    //ここでFacingModeを適用
    public void ScanMachines() {
        for(EnumFacing facing : EnumFacing.VALUES) {
            BlockPos tile_pos = this.getPos().offset(facing);
            TileEntity tile = this.world.getTileEntity(tile_pos);
            if(!(tile instanceof TileFluidCableBase)) {
                if (this.facingMode.get(facing) != CableConnectionMode.CLOSE) {
                    FluidCableManager.CableData cableData = this.cableManager.getCableDataByIndex(this.ECM_Index);
                    if (cableData != null) {
                        boolean flag = false;

                        if (tile != null && tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite())) {
                            if (this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                this.SetFacingState(facing, CableConnectionMode.NORMAL);
                            }

                            IFluidHandler storage = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());

                            if (storage != null) {
                                flag = true;
                                for(int i = 0; i < storage.getTankProperties().length; i++) {
                                    IFluidTankProperties properties = storage.getTankProperties()[i];
                                    FluidStack stack = properties.getContents();
                                    int amount = stack != null ? stack.amount : 0;
                                    int remaining = properties.getCapacity() - amount;
                                    if (this.JudgeFluidStack(stack)) {
                                        if (amount > 0 && properties.canDrain() && this.facingMode.get(facing) != CableConnectionMode.PUSH) {
                                            cableData.AddMachineProviderPos(tile_pos, facing.getOpposite(), storage, i);
                                            break;
                                        }

                                        if (remaining > 0 && properties.canFill() && this.facingMode.get(facing) != CableConnectionMode.PULL) {
                                            cableData.AddMachineReceiverPos(tile_pos, facing.getOpposite(), storage);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (!flag && this.facingMode.get(facing) == CableConnectionMode.NORMAL) {
                            this.SetFacingState(facing, CableConnectionMode.NORMALCLOSE);
                        }
                    }
                }
            }
        }
    }

    public void OverWriteCableManger(FluidCableManager manager) {
        this.LastUpdateTick = manager.getHostTick();
        this.LastChangeTick = this.Tick;
        this.cableManager = manager;
        this.cableManager.PutCableTile(this, this.MaxSendFluid);
    }

    public FluidCableManager getCableManager() {
        return this.cableManager;
    }

    public int getMaxSendFluid() {
        return this.MaxSendFluid;
    }

    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public boolean JudgeFluidStack(@Nullable FluidStack stack) {
        return this.fluidStack == null || stack == null || this.fluidStack.getFluid().equals(stack.getFluid());
    }

    @Override
    public void ChangeFacingState(EnumFacing facing) {
        CableConnectionMode now = this.facingMode.get(facing);
        CableConnectionMode nextMode = CableConnectionMode.getCableMode((now.getIndex() + 1) % CableConnectionMode.values().length);
        TileEntity neighbor = this.world.getTileEntity(this.getPos().offset(facing));
        if(neighbor instanceof TileCableBase) {
            CableConnectionMode neighborMode = ((TileCableBase)neighbor).getFacingMode().get(facing.getOpposite());
            if(nextMode == CableConnectionMode.NORMAL) {
                neighborMode = CableConnectionMode.NORMAL;
            } else if(nextMode == CableConnectionMode.NORMALCLOSE) {
                neighborMode = CableConnectionMode.CLOSE;
                nextMode = CableConnectionMode.CLOSE;
            } else if (nextMode == CableConnectionMode.PULL || nextMode == CableConnectionMode.PUSH) {
                neighborMode = CableConnectionMode.CLOSE;
                nextMode = CableConnectionMode.CLOSE;
            }
            ((TileCableBase)neighbor).SetFacingState(facing.getOpposite(), neighborMode);
        } else {
            FluidCableManager.CableData data = this.cableManager.getCableDataByIndex(this.ECM_Index);
            if(data != null) {
                boolean has = data.hasMachineByPos(this.getPos(), facing);
                if (has) {
                    if (nextMode == CableConnectionMode.NORMALCLOSE) {
                        nextMode = CableConnectionMode.CLOSE;
                    }
                } else {
                    if (nextMode == CableConnectionMode.NORMAL || nextMode == CableConnectionMode.CLOSE) {
                        nextMode = CableConnectionMode.PULL;
                    }
                }
            }
        }

        this.SetFacingState(facing, nextMode);
        this.sendUpdates();
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[0];
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return 0;
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

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if(compound.getBoolean("HasFluid")) {
            this.fluidStack = FluidStack.loadFluidStackFromNBT(compound.getCompoundTag("FluidTag"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        boolean hasFluid = this.fluidStack != null;
        compound.setBoolean("HasFluid", hasFluid);
        if(hasFluid) {
            compound.setTag("FluidTag", this.fluidStack.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager data = super.getNetWorkData();
        boolean hasFluid = this.fluidStack != null;
        data.addDataBoolean(hasFluid);
        if(hasFluid)
            data.addDataTag(this.fluidStack.writeToNBT(new NBTTagCompound()));
        return data;
    }

    @Override
    public void ReceivePacketData(DataListManager data) {
        super.ReceivePacketData(data);
        if(data.getDataBoolean())
            this.fluidStack = FluidStack.loadFluidStackFromNBT(data.getDataTag());
    }
}
