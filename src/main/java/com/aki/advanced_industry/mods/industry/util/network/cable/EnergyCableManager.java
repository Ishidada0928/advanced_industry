package com.aki.advanced_industry.mods.industry.util.network.cable;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.api.IEnergyStorage;
import com.aki.advanced_industry.mods.industry.tileentities.cables.energy.TileEnergyCableBase;
import com.aki.advanced_industry.mods.industry.util.enums.EnergyImplementType;
import com.aki.mcutils.APICore.Utils.list.Pair;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.*;

/*
* ケーブルをネットワークとして見る。
* 各TickでOnUpdateが実行されるとき、初めのブロックをホストとする。
* ホストから全探索を行い、途中のブロックのCableManagerをホストのものに上書きする。
* [上書きするとき、元のCableManagerからそのブロックを削除する]
* このとき、ホストのTick[2つ]とID[BlockPos]を各ブロックに保存する。-> 循環を防ぐ
* [Hostが壊されたときは、ホスト以外のブロックのOnUpdateで更新されるTickと、
* 保存時のTickが異なるので、そのブロックが新しいホストになる(以前のHostを消す)]
* [ケーブルが破壊されたときは、ホストからデータを消し、ホストから全探索。]
* */

//間のケーブルの容量と経路を考える...
//愚直にやると遠回りする...

public class EnergyCableManager {
    private final HashMap<BlockPos, TileEnergyCableBase> EnergyCables = new HashMap<>();
    private final HashMap<Integer, HashSet<BlockPos>> MaxSendEnergies = new HashMap<>();
    private final List<CableData> cableDataList = new ArrayList<>();
    private long CableID = 0L;
    private final BlockPos HostPos;
    private long HostTick = 0L;

    public EnergyCableManager(TileEnergyCableBase hostTile) {
        this.HostPos = hostTile.getPos();
        this.CableID = this.HostPos.toLong();//(GlobalCableID++);
        //this.PutCableTile(hostTile, hostTile.getMaxSendEnergy());
    }

    public void setHostTick(long tick) {
        this.HostTick = tick;
    }

    public void PutCableTile(TileEnergyCableBase cable, int MaxSendEnergy) {
        //this.PrevEnergyCables.remove(cable.getPos());
        this.EnergyCables.put(cable.getPos(), cable);
        HashSet<BlockPos> posSet = this.MaxSendEnergies.getOrDefault(MaxSendEnergy, new HashSet<>());
        posSet.add(cable.getPos());
        this.MaxSendEnergies.put(MaxSendEnergy, posSet);
    }

    public HashMap<BlockPos, TileEnergyCableBase> getEnergyCables() {
        return this.EnergyCables;
    }

    public void Reset() {
        this.EnergyCables.clear();
        this.MaxSendEnergies.clear();
    }

    public void Update(World world) {
        for(CableData srcCable : this.cableDataList) {
            int Provide = srcCable.CanProvideEnergyAsCable();

            int AllNeedEnergy = 0;
            for (CableData destCable : this.cableDataList) {
                AllNeedEnergy += Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable());
            }

            if(AllNeedEnergy > 0) {
                for (CableData destCable : this.cableDataList) {
                    int needByNetwork = Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable());
                    int need = Math.min(Provide * (needByNetwork / AllNeedEnergy), Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable()));//割合で分配(必要なところほど多く供給)
                    if(need > 0) {
                        int storage = srcCable.ProvideEnergy(world, need, false);
                        storage -= destCable.ReceiveEnergy(world, storage, false);
                        if (storage > 0) {//実行されないはず...
                            srcCable.ReceiveEnergy(world, storage, false);
                        }
                    }
                }
            }
        }
    }

    public int ProvideEnergy(World world, CableData srcCable, int Provide, boolean simulation) {
        if(srcCable != null) {
            int AllNeedEnergy = 0;
            for (CableData destCable : this.cableDataList) {
                AllNeedEnergy += Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable());
            }

            Provide = Math.min(srcCable.getMaxSendEnergy(), Provide);
            int first = Provide;

            if (AllNeedEnergy > 0) {
                for (CableData destCable : this.cableDataList) {
                    int needByNetwork = Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable());
                    int need = Math.min(Provide * (needByNetwork / AllNeedEnergy), Math.min(srcCable.getMaxSendEnergyByIndex(destCable.getIndex()), destCable.NeedEnergyAsCable()));//割合で分配(必要なところほど多く供給)
                    if (need > 0) {
                        Provide -= destCable.ReceiveEnergy(world, Provide, simulation);
                        if (Provide <= 0) {
                            return first;
                        }
                    }
                }
            }

            return first - Provide;
        }
        return 0;
    }

    @Nullable
    public CableData getCableDataByIndex(int index) {
        if(!this.cableDataList.isEmpty())
            return this.cableDataList.get(index);
        return null;
    }

    public void SeparateNetworks() {
        int index = 0;
        this.cableDataList.clear();

        for(int MaxSendEnergy : this.MaxSendEnergies.keySet()) {
            List<BlockPos> blockPosList = new ArrayList<>(this.MaxSendEnergies.get(MaxSendEnergy));
            Set<BlockPos> WrappedCable = new HashSet<>();
            while(!blockPosList.isEmpty()) {//グループ分け
                BlockPos pos = blockPosList.get(0);
                TileEnergyCableBase pos_tile = this.EnergyCables.get(pos);
                CableData cableData = new CableData(pos, MaxSendEnergy, index);

                WrappedCable.add(pos);
                pos_tile.setECM_Index(index);
                this.SearchCables(MaxSendEnergy, cableData, pos, WrappedCable, index);
                blockPosList.removeAll(WrappedCable);

                this.cableDataList.add(cableData);

                WrappedCable.clear();
                index++;
            }
        }
        
        //隣接するグループを繋ぐ(容量の違い)
        for(CableData data : this.cableDataList) {
            for(Map.Entry<Integer, Set<BlockPos>> entry : data.getNeighborBlock().entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    int ECM_Index = this.EnergyCables.get(pos).getECM_Index();
                    data.addNeighbor(ECM_Index);
                }
            }
        }

        //このネットワーク(ケーブル)から容量の違うネットワークを経由したときに使うデータの準備
        for(CableData data : this.cableDataList) {
            //ECM -> MaxSendEnergy
            HashMap<Integer, Integer> Index_MaxSendEnergy = data.getIndex_MaxSendEnergy();
            //自分自身を追加
            Index_MaxSendEnergy.put(data.getIndex(), data.getMaxSendEnergy());
            this.SearchCableData(data, Index_MaxSendEnergy, data.getMaxSendEnergy());
        }
    }

    private void SearchCables(int MaxSendEnergy, CableData cableData, BlockPos pos, Set<BlockPos> WrappedCable, int index) {
        for(EnumFacing facing : EnumFacing.VALUES) {
            BlockPos next = pos.offset(facing);
            TileEnergyCableBase cable = this.EnergyCables.get(next);
            if(cable != null) {
                if(cable.getMaxSendEnergy() == MaxSendEnergy) {
                    if(!WrappedCable.contains(next)) {
                        WrappedCable.add(next);// 重複回避
                        cable.setECM_Index(index);
                        this.SearchCables(MaxSendEnergy, cableData, next, WrappedCable, index);
                    }
                } else {//他の容量でも保存しておく。
                    cableData.addNeighborBlock(cable.getMaxSendEnergy(), next);
                }
            }
        }
    }

    //全探索
    private void SearchCableData(CableData searchData, HashMap<Integer, Integer> Index_MaxSendEnergy, int MaxSendEnergy) {
        for(int index : searchData.getNeighbor()) {
            CableData search = this.cableDataList.get(index);
            Integer hasMaxSendEnergy = Index_MaxSendEnergy.getOrDefault(index, 0);
            int newMaxSendEnergy = Math.max(Math.min(search.getMaxSendEnergy(), MaxSendEnergy), hasMaxSendEnergy);
            //循環対策
            if(newMaxSendEnergy != hasMaxSendEnergy) {
                Index_MaxSendEnergy.put(index, newMaxSendEnergy);
                this.SearchCableData(search, Index_MaxSendEnergy, newMaxSendEnergy);
            }
        }
    }

    public long getHostTick() {
        return this.HostTick;
    }

    public BlockPos getHostPos() {
        return this.HostPos;
    }

    public long getCableID() {
        return this.CableID;
    }

    public class CableData {
        private final BlockPos HostPos;
        private final int MaxSendEnergy;

        //隣接するMaxSendEnergyが違うもの
        private final HashMap<Integer, Set<BlockPos>> NeighborBlock = new HashMap<>();

        //隣接するCableData
        private final Set<Integer> Neighbor = new HashSet<>();

        //Index、送ることができる量(MaxSendEnergy)
        private final HashMap<Integer, Integer> Index_MaxSendEnergy = new HashMap<>();
        private final HashMap<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> MachineReceiverPos = new HashMap<>();
        private final HashMap<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> MachineProviderPos = new HashMap<>();
        private final int Index;
        private int NeedReceiveEnergy = 0;
        private int CanProvideEnergy = 0;
        public CableData(BlockPos hostPos, int maxSendEnergy, int index) {
            this.HostPos = hostPos;
            this.MaxSendEnergy = maxSendEnergy;
            this.Index = index;
        }

        public void addNeighborBlock(int maxSendEnergy, BlockPos pos) {
            Set<BlockPos> posSet = this.NeighborBlock.getOrDefault(maxSendEnergy, new HashSet<>());
            posSet.add(pos);
            this.NeighborBlock.put(maxSendEnergy, posSet);
        }

        public HashMap<Integer, Set<BlockPos>> getNeighborBlock() {
            return this.NeighborBlock;
        }

        public void addNeighbor(int index) {
            this.Neighbor.add(index);
        }

        public Set<Integer> getNeighbor() {
            return this.Neighbor;
        }

        public HashMap<Integer, Integer> getIndex_MaxSendEnergy() {
            return this.Index_MaxSendEnergy;
        }

        public int getMaxSendEnergyByIndex(int DestIndex) {
            return this.Index_MaxSendEnergy.getOrDefault(DestIndex, this.MaxSendEnergy);
        }

        public void AddMachineReceiverPos(BlockPos pos, EnumFacing facing, EnergyImplementType type) {
            Set<Pair<EnumFacing, EnergyImplementType>> set = this.MachineReceiverPos.getOrDefault(pos, new HashSet<>(EnumFacing.VALUES.length * 5));
            set.add(new Pair<>(facing, type));
            this.MachineReceiverPos.put(pos, set);
        }

        public void AddMachineProviderPos(BlockPos pos, EnumFacing facing, EnergyImplementType type) {
            Set<Pair<EnumFacing, EnergyImplementType>> set = this.MachineProviderPos.getOrDefault(pos, new HashSet<>(EnumFacing.VALUES.length * 5));
            set.add(new Pair<>(facing, type));
            this.MachineProviderPos.put(pos, set);
        }

        public boolean hasMachineByPos(BlockPos CablePos, EnumFacing facing) {
            BlockPos tilePos = CablePos.offset(facing);
            Set<Pair<EnumFacing, EnergyImplementType>> set = this.MachineReceiverPos.get(tilePos);
            if(set != null) {
                for(Pair<EnumFacing, EnergyImplementType> pair : set) {
                    if(pair.getKey() == facing.getOpposite()) {
                        return true;
                    }
                }
            }

            set = this.MachineProviderPos.get(tilePos);
            if(set != null) {
                for(Pair<EnumFacing, EnergyImplementType> pair : set) {
                    if(pair.getKey() == facing.getOpposite()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Nullable
        public EnergyImplementType getEnergyImplementTypeByPos(BlockPos CablePos, EnumFacing facing) {
            BlockPos tilePos = CablePos.offset(facing);
            Set<Pair<EnumFacing, EnergyImplementType>> set = this.MachineReceiverPos.get(tilePos);
            if(set != null) {
                for(Pair<EnumFacing, EnergyImplementType> pair : set) {
                    if(pair.getKey() == facing.getOpposite()) {
                        return pair.getValue();
                    }
                }
            }

            set = this.MachineProviderPos.get(tilePos);
            if(set != null) {
                for(Pair<EnumFacing, EnergyImplementType> pair : set) {
                    if(pair.getKey() == facing.getOpposite()) {
                        return pair.getValue();
                    }
                }
            }

            return null;
        }

        public void IncreaseNeedEnergy(int needReceiveEnergy) {
            this.NeedReceiveEnergy += needReceiveEnergy;
        }

        public void IncreaseProvideEnergy(int canProvideEnergy) {
            this.CanProvideEnergy += canProvideEnergy;
        }

        //このケーブルのネットワーク内が受け取ることができるエネルギー
        public int NeedEnergyAsCable() {
            return Math.min(this.NeedReceiveEnergy, this.MaxSendEnergy);
        }

        //このケーブルのネットワーク内外で送ることができるエネルギー
        public int CanProvideEnergyAsCable() {
            return Math.min(this.CanProvideEnergy, this.MaxSendEnergy);
        }

        //このケーブルのネットワークが受け取ったエネルギー
        //マシンに供給
        public int ReceiveEnergy(World world, int receiveEnergy, boolean simulation) {
            int prev = 0;
            int receive = Math.min(this.MaxSendEnergy, receiveEnergy);
            int first = receive;

            while(receive > 0 && (receive != prev)) {
                prev = receive;

                if(this.MachineReceiverPos.isEmpty()) {
                    break;
                }

                int receivers = this.MachineReceiverPos.size();
                if(receive >= receivers) {//割ったときに >= 1.0
                    int provide = Math.floorDiv(receive, receivers);
                    for(Map.Entry<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> entry : (simulation ? new HashMap<>(this.MachineReceiverPos) : this.MachineReceiverPos).entrySet()) {
                        TileEntity tile = world.getTileEntity(entry.getKey());
                        Iterator<Pair<EnumFacing, EnergyImplementType>> iter = entry.getValue().iterator();
                        if(tile != null) {
                            while (iter.hasNext()) {
                                Pair<EnumFacing, EnergyImplementType> pair = iter.next();
                                EnumFacing facing = pair.getKey();
                                switch (pair.getValue()) {
                                    case RFIENERGYSTORAGE:
                                        if (tile instanceof IEnergyStorage) {
                                            IEnergyStorage storage = ((IEnergyStorage) tile);
                                            receive -= storage.receiveEnergy(provide, simulation);
                                            if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGEEXTRACT:
                                    case RFIENERGYPROVIDER:
                                        break;
                                    case RFIENERGYRECEIVER:
                                        if (tile instanceof IEnergyReceiver) {
                                            IEnergyReceiver receiver = ((IEnergyReceiver) tile);
                                            receive -= receiver.receiveEnergy(facing, provide, simulation);
                                            if (receiver.getMaxEnergyStored(facing) == receiver.getEnergyStored(facing)) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGERECEIVE:
                                        if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
                                            net.minecraftforge.energy.IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, facing);
                                            if (storage != null) {
                                                receive -= storage.receiveEnergy(provide, simulation);
                                                if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                    iter.remove();
                                                }
                                            }
                                        } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                            net.minecraftforge.energy.IEnergyStorage storage = ((net.minecraftforge.energy.IEnergyStorage) tile);
                                            receive -= storage.receiveEnergy(provide, simulation);
                                            if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                } else {//割ったときに < 1.0
                    for(Map.Entry<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> entry : (simulation ? new HashMap<>(this.MachineReceiverPos) : this.MachineReceiverPos).entrySet()) {
                        TileEntity tile = world.getTileEntity(entry.getKey());
                        Iterator<Pair<EnumFacing, EnergyImplementType>> iter = entry.getValue().iterator();
                        if (tile != null) {
                            while (iter.hasNext()) {
                                Pair<EnumFacing, EnergyImplementType> pair = iter.next();
                                EnumFacing facing = pair.getKey();
                                switch (pair.getValue()) {
                                    case RFIENERGYSTORAGE:
                                        if (tile instanceof IEnergyStorage) {
                                            IEnergyStorage storage = ((IEnergyStorage) tile);
                                            receive -= storage.receiveEnergy(receive, simulation);
                                            if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGEEXTRACT:
                                    case RFIENERGYPROVIDER:
                                        break;
                                    case RFIENERGYRECEIVER:
                                        if (tile instanceof IEnergyReceiver) {
                                            IEnergyReceiver receiver = ((IEnergyReceiver) tile);
                                            receive -= receiver.receiveEnergy(facing, receive, simulation);
                                            if (receiver.getMaxEnergyStored(facing) == receiver.getEnergyStored(facing)) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGERECEIVE:
                                        if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
                                            net.minecraftforge.energy.IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, facing);
                                            if (storage != null) {
                                                receive -= storage.receiveEnergy(receive, simulation);
                                                if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                    iter.remove();
                                                }
                                            }
                                        } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                            net.minecraftforge.energy.IEnergyStorage storage = ((net.minecraftforge.energy.IEnergyStorage) tile);
                                            receive -= storage.receiveEnergy(receive, simulation);
                                            if (storage.getMaxEnergyStored() == storage.getEnergyStored()) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                }

                                if (receive <= 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            return first - receive;
        }

        //このケーブルのネットワークが送電するエネルギー
        //マシンから取得
        public int ProvideEnergy(World world, int provideEnergy, boolean simulation) {
            int sum = 0;
            int prev = -1;
            int provide = Math.min(provideEnergy, this.MaxSendEnergy);

            while (sum < provide && (prev != sum)) {
                prev = sum;

                if(this.MachineProviderPos.isEmpty()) {
                    break;
                }

                int providers = this.MachineProviderPos.size();
                if(provide >= providers) {
                    int receive = Math.floorDiv(provide, providers);
                    for(Map.Entry<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> entry : (simulation ? new HashMap<>(this.MachineProviderPos) : this.MachineProviderPos).entrySet()) {
                        TileEntity tile = world.getTileEntity(entry.getKey());
                        Iterator<Pair<EnumFacing, EnergyImplementType>> iter = entry.getValue().iterator();
                        if (tile != null) {
                            while (iter.hasNext()) {
                                Pair<EnumFacing, EnergyImplementType> pair = iter.next();
                                EnumFacing facing = pair.getKey();
                                switch (pair.getValue()) {
                                    case RFIENERGYSTORAGE:
                                        if (tile instanceof IEnergyStorage) {
                                            IEnergyStorage storage = ((IEnergyStorage) tile);
                                            sum += storage.extractEnergy(receive, simulation);
                                            if (storage.getEnergyStored() == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case RFIENERGYPROVIDER:
                                        if (tile instanceof IEnergyProvider) {
                                            IEnergyProvider provider = ((IEnergyProvider) tile);
                                            sum += provider.extractEnergy(facing, receive, simulation);
                                            if (provider.getEnergyStored(facing) == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGERECEIVE:
                                    case RFIENERGYRECEIVER:
                                        break;
                                    case FORGEIENERGYSTORAGEEXTRACT:
                                        if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
                                            net.minecraftforge.energy.IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, facing);
                                            if(storage != null) {
                                                sum += storage.extractEnergy(receive, simulation);
                                                if (storage.getEnergyStored() == 0) {
                                                    iter.remove();
                                                }
                                            }
                                        } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                            net.minecraftforge.energy.IEnergyStorage storage = ((net.minecraftforge.energy.IEnergyStorage) tile);
                                            sum += storage.extractEnergy(receive, simulation);
                                            if (storage.getEnergyStored() == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                }
                            }
                        }
                    }
                } else {
                    for (Map.Entry<BlockPos, Set<Pair<EnumFacing, EnergyImplementType>>> entry : (simulation ? new HashMap<>(this.MachineProviderPos) : this.MachineProviderPos).entrySet()) {
                        TileEntity tile = world.getTileEntity(entry.getKey());
                        Iterator<Pair<EnumFacing, EnergyImplementType>> iter = entry.getValue().iterator();
                        if(tile != null) {
                            while (iter.hasNext()) {
                                Pair<EnumFacing, EnergyImplementType> pair = iter.next();
                                EnumFacing facing = pair.getKey();
                                switch (pair.getValue()) {
                                    case RFIENERGYSTORAGE:
                                        if (tile instanceof IEnergyStorage) {
                                            IEnergyStorage storage = ((IEnergyStorage) tile);
                                            sum += storage.extractEnergy(provide, simulation);
                                            if (storage.getEnergyStored() == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case RFIENERGYPROVIDER:
                                        if (tile instanceof IEnergyProvider) {
                                            IEnergyProvider provider = ((IEnergyProvider) tile);
                                            sum += provider.extractEnergy(facing, provide, simulation);
                                            if (provider.getEnergyStored(facing) == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                    case FORGEIENERGYSTORAGERECEIVE:
                                    case RFIENERGYRECEIVER:
                                        break;
                                    case FORGEIENERGYSTORAGEEXTRACT:
                                        if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
                                            net.minecraftforge.energy.IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, facing);
                                            if (storage != null) {
                                                System.out.println("Provide2");
                                                sum += storage.extractEnergy(provide, simulation);
                                                if (storage.getEnergyStored() == 0) {
                                                    iter.remove();
                                                }
                                            }
                                        } else if (tile instanceof net.minecraftforge.energy.IEnergyStorage) {
                                            net.minecraftforge.energy.IEnergyStorage storage = ((net.minecraftforge.energy.IEnergyStorage) tile);
                                            sum += storage.extractEnergy(provide, simulation);
                                            if (storage.getEnergyStored() == 0) {
                                                iter.remove();
                                            }
                                        }
                                        break;
                                }

                                if (sum == provide) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return sum;
        }

        public BlockPos getHostPos() {
            return this.HostPos;
        }

        public int getMaxSendEnergy() {
            return this.MaxSendEnergy;
        }

        public int getIndex() {
            return this.Index;
        }
    }
}