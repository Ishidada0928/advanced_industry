package com.aki.advanced_industry.mods.industry.tileentities.cables.energy;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.api.IEnergyStorage;
import com.aki.advanced_industry.mods.industry.tileentities.cables.TileCableBase;
import com.aki.advanced_industry.mods.industry.util.enums.CableConnectionMode;
import com.aki.advanced_industry.mods.industry.util.enums.EnergyImplementType;
import com.aki.advanced_industry.mods.industry.util.implement.IMachineConfiguration;
import com.aki.advanced_industry.mods.industry.util.network.cable.EnergyCableManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.CapabilityEnergy;

public abstract class TileEnergyCableBase extends TileCableBase implements IMachineConfiguration {
    private final int MaxSendEnergy;
    private EnergyCableManager cableManager;

    public TileEnergyCableBase(int maxSendEnergy) {
        super();
        this.MaxSendEnergy = maxSendEnergy;
        this.cableManager = new EnergyCableManager(this);
        this.addCapability(CapabilityEnergy.ENERGY, (facing) -> this);
    }

    @Override
    public void update() {
        super.update();
        if(!this.world.isRemote) {
            //初期化
            if(this.Tick < 0) {
                for(EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity tile = this.world.getTileEntity(this.getPos().offset(facing));
                    if(tile instanceof TileEnergyCableBase) {
                        TileEnergyCableBase cableBase = (TileEnergyCableBase) tile;
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
                this.cableManager = new EnergyCableManager(this);
                ThisHost = true;
            }

            if(ThisHost) {
                this.cableManager.Reset();
                this.cableManager.PutCableTile(this, this.MaxSendEnergy);
                this.cableManager.setHostTick(this.Tick);
                //Hostだけ処理する(全探索)。
                this.ScanCables();
                this.cableManager.SeparateNetworks();
                for(TileEnergyCableBase cable : this.cableManager.getEnergyCables().values()) {
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
            if(tile instanceof TileEnergyCableBase) {
                TileEnergyCableBase cableBase = (TileEnergyCableBase) tile;
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
            if(!(tile instanceof TileEnergyCableBase)) {
                if (this.facingMode.get(facing) != CableConnectionMode.CLOSE) {
                    EnergyCableManager.CableData cableData = this.cableManager.getCableDataByIndex(this.ECM_Index);
                    if (cableData != null) {
                        if (tile != null && tile.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                            boolean flag = false;
                            net.minecraftforge.energy.IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                            if (storage != null) {
                                int canExtract = storage.extractEnergy(storage.getEnergyStored(), true);
                                int needReceive = storage.receiveEnergy(storage.getMaxEnergyStored() - storage.getEnergyStored(), true);

                                if (canExtract > 0 && storage.canExtract() && this.facingMode.get(facing) != CableConnectionMode.PUSH) {
                                    flag = true;
                                    cableData.AddMachineProviderPos(tile_pos, facing.getOpposite(), EnergyImplementType.FORGEIENERGYSTORAGEEXTRACT);
                                    cableData.IncreaseProvideEnergy(canExtract);
                                }
                                if (needReceive > 0 && storage.canReceive() && this.facingMode.get(facing) != CableConnectionMode.PULL) {
                                    flag = true;
                                    cableData.AddMachineReceiverPos(tile_pos, facing.getOpposite(), EnergyImplementType.FORGEIENERGYSTORAGERECEIVE);
                                    cableData.IncreaseNeedEnergy(needReceive);
                                }
                            }

                            if (flag && this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                this.SetFacingState(facing, CableConnectionMode.NORMAL);
                            }
                        } else if (tile instanceof IEnergyStorage) {
                            boolean flag = false;
                            IEnergyStorage storage = ((IEnergyStorage) tile);
                            int canExtract = storage.extractEnergy(storage.getEnergyStored(), true);
                            int needReceive = storage.receiveEnergy(storage.getMaxEnergyStored() - storage.getEnergyStored(), true);

                            if (canExtract > 0 && this.facingMode.get(facing) != CableConnectionMode.PUSH) {
                                flag = true;
                                cableData.AddMachineProviderPos(tile_pos, facing.getOpposite(), EnergyImplementType.RFIENERGYSTORAGE);
                                cableData.IncreaseProvideEnergy(canExtract);
                            }

                            if (needReceive > 0 && this.facingMode.get(facing) != CableConnectionMode.PULL) {
                                flag = true;
                                cableData.AddMachineReceiverPos(tile_pos, facing.getOpposite(), EnergyImplementType.RFIENERGYSTORAGE);
                                cableData.IncreaseNeedEnergy(needReceive);
                            }

                            if (flag && this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                this.SetFacingState(facing, CableConnectionMode.NORMAL);
                            }
                        } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                            boolean flag = false;
                            net.minecraftforge.energy.IEnergyStorage storage = ((net.minecraftforge.energy.IEnergyStorage) tile);
                            int canExtract = storage.extractEnergy(storage.getEnergyStored(), true);
                            int needReceive = storage.receiveEnergy(storage.getMaxEnergyStored() - storage.getEnergyStored(), true);
                            if (canExtract > 0 && storage.canExtract() && this.facingMode.get(facing) != CableConnectionMode.PUSH) {
                                flag = true;
                                cableData.AddMachineProviderPos(tile_pos, facing.getOpposite(), EnergyImplementType.FORGEIENERGYSTORAGEEXTRACT);
                                cableData.IncreaseProvideEnergy(canExtract);
                            }
                            if (needReceive > 0 && storage.canReceive() && this.facingMode.get(facing) != CableConnectionMode.PULL) {
                                flag = true;
                                cableData.AddMachineReceiverPos(tile_pos, facing.getOpposite(), EnergyImplementType.FORGEIENERGYSTORAGERECEIVE);
                                cableData.IncreaseNeedEnergy(needReceive);
                            }

                            if (flag && this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                this.SetFacingState(facing, CableConnectionMode.NORMAL);
                            }
                        } else {
                            boolean flag = false;
                            if (tile instanceof IEnergyReceiver && this.facingMode.get(facing) != CableConnectionMode.PULL) {
                                IEnergyReceiver receiver = ((IEnergyReceiver) tile);
                                if (receiver.canConnectEnergy(facing.getOpposite())) {
                                    if (this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                        this.SetFacingState(facing, CableConnectionMode.NORMAL);
                                    }
                                    flag = true;
                                    int needReceive = receiver.getMaxEnergyStored(facing.getOpposite()) - receiver.getEnergyStored(facing.getOpposite());
                                    if (needReceive > 0) {
                                        cableData.AddMachineReceiverPos(tile_pos, facing.getOpposite(), EnergyImplementType.RFIENERGYRECEIVER);
                                        cableData.IncreaseNeedEnergy(receiver.receiveEnergy(facing.getOpposite(), needReceive, true));
                                    }
                                }
                            }

                            if (tile instanceof IEnergyProvider && this.facingMode.get(facing) != CableConnectionMode.PUSH) {
                                IEnergyProvider provider = ((IEnergyProvider) tile);
                                if (provider.canConnectEnergy(facing.getOpposite())) {
                                    if (this.facingMode.get(facing) == CableConnectionMode.NORMALCLOSE) {
                                        this.SetFacingState(facing, CableConnectionMode.NORMAL);
                                    }
                                    flag = true;
                                    int canExtract = provider.extractEnergy(facing.getOpposite(), provider.getEnergyStored(facing.getOpposite()), true);
                                    if (canExtract > 0) {
                                        cableData.AddMachineProviderPos(tile_pos, facing.getOpposite(), EnergyImplementType.RFIENERGYPROVIDER);
                                        cableData.IncreaseProvideEnergy(canExtract);
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
    }

    public void OverWriteCableManger(EnergyCableManager manager) {
        this.LastUpdateTick = manager.getHostTick();
        this.LastChangeTick = this.Tick;
        this.cableManager = manager;
        this.cableManager.PutCableTile(this, this.MaxSendEnergy);
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return this.facingMode.get(enumFacing) != CableConnectionMode.CLOSE;
    }

    //消費した電力を返す。
    @Override
    public int receiveEnergy(EnumFacing enumFacing, int i, boolean b) {
        if (this.facingMode.get(enumFacing) == CableConnectionMode.NORMAL || this.facingMode.get(enumFacing) == CableConnectionMode.PULL) {
            EnergyCableManager.CableData data = this.cableManager.getCableDataByIndex(this.ECM_Index);
            if(data != null) {
                EnergyImplementType type = data.getEnergyImplementTypeByPos(this.getPos(), enumFacing);
                if (type != EnergyImplementType.RFIENERGYPROVIDER && type != EnergyImplementType.RFIENERGYSTORAGE && type != EnergyImplementType.FORGEIENERGYSTORAGEEXTRACT) {
                    return this.cableManager.ProvideEnergy(this.world, data, i, b);
                }
            }
        }
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    //消費した電力を返す。
    @Override
    public int receiveEnergy(int i, boolean b) {
        return this.cableManager.ProvideEnergy(this.world, this.cableManager.getCableDataByIndex(this.ECM_Index), i, b);
    }

    //
    @Override
    public int extractEnergy(int i, boolean b) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        return this.MaxSendEnergy;
    }

    @Override
    public int getMaxEnergyStored() {
        return this.MaxSendEnergy;
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    public int getMaxSendEnergy() {
        return this.MaxSendEnergy;
    }

    public EnergyCableManager getCableManager() {
        return this.cableManager;
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
            EnergyCableManager.CableData data = this.cableManager.getCableDataByIndex(this.ECM_Index);
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
}
