package com.aki.advanced_industry.mods.industry.util.network.cable;

import com.aki.advanced_industry.mods.industry.tileentities.cables.fluid.TileFluidCableBase;
import com.aki.advanced_industry.mods.industry.util.list.Triple;
import com.aki.akisutils.apis.util.list.Pair;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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

public class FluidCableManager {
    private final HashMap<BlockPos, TileFluidCableBase> FluidCables = new HashMap<>();
    private final HashMap<Integer, HashSet<BlockPos>> MaxSendFluids = new HashMap<>();
    private final List<CableData> cableDataList = new ArrayList<>();
    private long CableID = 0L;
    private final BlockPos HostPos;
    private long HostTick = 0L;

    public FluidCableManager(TileFluidCableBase hostTile) {
        this.HostPos = hostTile.getPos();
        this.CableID = this.HostPos.toLong();//(GlobalCableID++);
    }

    public void setHostTick(long tick) {
        this.HostTick = tick;
    }

    public void PutCableTile(TileFluidCableBase cable, int MaxSendFluid) {
        //this.PrevFluidCables.remove(cable.getPos());
        this.FluidCables.put(cable.getPos(), cable);
        HashSet<BlockPos> posSet = this.MaxSendFluids.getOrDefault(MaxSendFluid, new HashSet<>());
        posSet.add(cable.getPos());
        this.MaxSendFluids.put(MaxSendFluid, posSet);
    }

    public HashMap<BlockPos, TileFluidCableBase> getFluidCables() {
        return this.FluidCables;
    }

    public void Reset() {
        this.FluidCables.clear();
        this.MaxSendFluids.clear();
    }

    public void Update(World world) {
        for(CableData srcCable : this.cableDataList) {
            srcCable.Update(world, this);
        }
        /*for(CableData srcCable : this.cableDataList) {
            int Provide = srcCable.CanProvideFluidAsCable();

            int AllNeedFluid = 0;
            for (CableData destCable : this.cableDataList) {
                AllNeedFluid += Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable());
            }

            if(AllNeedFluid > 0) {
                for (CableData destCable : this.cableDataList) {
                    int needByNetwork = Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable());
                    int need = Math.min(Provide * (needByNetwork / AllNeedFluid), Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable()));//割合で分配(必要なところほど多く供給)
                    if(need > 0) {
                        int storage = srcCable.ProvideFluid(world, need, false);
                        storage -= destCable.ReceiveFluid(world, storage, false);
                        if (storage > 0) {//実行されないはず...
                            srcCable.ReceiveFluid(world, storage, false);
                        }
                    }
                }
            }
        }*/
    }

    /*public int ProvideFluid(World world, CableData srcCable, int Provide, boolean doFill) {
        if(srcCable != null) {
            int AllNeedFluid = 0;
            for (CableData destCable : this.cableDataList) {
                AllNeedFluid += Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable());
            }

            Provide = Math.min(srcCable.getMaxSendFluid(), Provide);
            int first = Provide;

            if (AllNeedFluid > 0) {
                for (CableData destCable : this.cableDataList) {
                    int needByNetwork = Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable());
                    int need = Math.min(Provide * (needByNetwork / AllNeedFluid), Math.min(srcCable.getMaxSendFluidByIndex(destCable.getIndex()), destCable.NeedFluidAsCable()));//割合で分配(必要なところほど多く供給)
                    if (need > 0) {
                        Provide -= destCable.ReceiveFluid(world, Provide, doFill);
                        if (Provide <= 0) {
                            return first;
                        }
                    }
                }
            }

            return first - Provide;
        }
        return 0;
    }*/

    @Nullable
    public CableData getCableDataByIndex(int index) {
        if(!this.cableDataList.isEmpty())
            return this.cableDataList.get(index);
        return null;
    }

    public void SeparateNetworks() {
        int index = 0;
        this.cableDataList.clear();
        List<Fluid> fluids = new ArrayList<>();

        for(int MaxSendFluid : this.MaxSendFluids.keySet()) {
            List<BlockPos> blockPosList = new ArrayList<>(this.MaxSendFluids.get(MaxSendFluid));
            Set<BlockPos> WrappedCable = new HashSet<>();
            while(!blockPosList.isEmpty()) {//グループ分け
                BlockPos pos = blockPosList.get(0);
                TileFluidCableBase pos_tile = this.FluidCables.get(pos);

                FluidStack stack = pos_tile.getFluidStack();
                fluids.add(stack != null ? stack.getFluid() : null);

                CableData cableData = new CableData(pos, stack, MaxSendFluid, index);

                WrappedCable.add(pos);
                pos_tile.setECM_Index(index);
                cableData.addFluidCable(pos, pos_tile);

                this.SearchCables(fluids, MaxSendFluid, cableData, pos, WrappedCable, index);

                blockPosList.removeAll(WrappedCable);
                this.cableDataList.add(cableData);

                WrappedCable.clear();
                index++;
            }
        }

        //隣接するグループを繋ぐ(容量の違い)
        for(CableData data : this.cableDataList) {

            Fluid fluid = fluids.get(data.Index);
            data.setFluidStack(fluid != null ? new FluidStack(fluid, 0) : null);

            for(Map.Entry<Integer, Set<BlockPos>> entry : data.getNeighborBlock().entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    int ECM_Index = this.FluidCables.get(pos).getECM_Index();
                    data.addNeighbor(ECM_Index);
                }
            }
        }

        //このネットワーク(ケーブル)から容量の違うネットワークを経由したときに使うデータの準備
        for(CableData data : this.cableDataList) {
            //ECM -> MaxSendFluid
            HashMap<Integer, Integer> Index_MaxSendFluid = data.getIndex_MaxSendFluid();
            //自分自身を追加
            Index_MaxSendFluid.put(data.getIndex(), data.getMaxSendFluid());
            this.SearchCableData(data, Index_MaxSendFluid, data.getMaxSendFluid());
        }
    }

    private void SearchCables(List<Fluid> fluids, int MaxSendFluid, CableData cableData, BlockPos pos, Set<BlockPos> WrappedCable, int index) {
        for(EnumFacing facing : EnumFacing.VALUES) {
            BlockPos next = pos.offset(facing);
            TileFluidCableBase cable = this.FluidCables.get(next);
            if(cable != null) {
                //同じ液体(空)か
                //液-空 液-液 空-空
                if(cableData.JudgeCableFluidStack(cable.getFluidStack())) {
                    if(fluids.get(index) == null && cable.getFluidStack() != null) {
                        fluids.set(index, cable.getFluidStack().getFluid());
                    }
                    if (cable.getMaxSendFluid() == MaxSendFluid) {
                        if (!WrappedCable.contains(next)) {
                            WrappedCable.add(next);// 重複回避
                            cable.setECM_Index(index);

                            cableData.addFluidCable(next, cable);

                            this.SearchCables(fluids, MaxSendFluid, cableData, next, WrappedCable, index);
                        }
                    } else {//他の容量でも保存しておく。
                        cableData.addNeighborBlock(cable.getMaxSendFluid(), next);
                    }
                }
            }
        }
    }

    //全探索
    private void SearchCableData(CableData searchData, HashMap<Integer, Integer> Index_MaxSendFluid, int MaxSendFluid) {
        for(int index : searchData.getNeighbor()) {
            CableData search = this.cableDataList.get(index);
            Integer hasMaxSendFluid = Index_MaxSendFluid.getOrDefault(index, 0);
            int newMaxSendFluid = Math.max(Math.min(search.getMaxSendFluid(), MaxSendFluid), hasMaxSendFluid);
            //循環対策
            if(newMaxSendFluid != hasMaxSendFluid) {
                Index_MaxSendFluid.put(index, newMaxSendFluid);
                this.SearchCableData(search, Index_MaxSendFluid, newMaxSendFluid);
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

    /*
     * 液体の種類と容量でネットワーク化
     * */
    public static class CableData {
        private final BlockPos HostPos;
        private final int MaxSendFluid;

        private FluidStack fluidStack = null;

        //容量が異なる
        //同じ液体(空)
        //液-空 液-液 空-空
        private final HashMap<Integer, Set<BlockPos>> NeighborBlock = new HashMap<>();

        //隣接するCableData
        private final Set<Integer> Neighbor = new HashSet<>();

        //Index、送ることができる量(MaxSendFluid)
        private final HashMap<Integer, Integer> Index_MaxSendFluid = new HashMap<>();

        //同じネットワーク内のケーブル
        private final HashMap<BlockPos, TileFluidCableBase> InFluidCables = new HashMap<>();
        private final HashMap<BlockPos, Set<Pair<EnumFacing, IFluidHandler>>> MachineReceiverPos = new HashMap<>();
        private final HashMap<BlockPos, Set<Triple<EnumFacing, IFluidHandler, Integer>>> MachineProviderPos = new HashMap<>();

        private final int Index;

        public CableData(BlockPos hostPos, FluidStack stack, int maxSendFluid, int index) {
            this.HostPos = hostPos;
            this.fluidStack = stack;
            this.MaxSendFluid = maxSendFluid;
            this.Index = index;
        }

        public void addNeighborBlock(int maxSendFluid, BlockPos pos) {
            Set<BlockPos> posSet = this.NeighborBlock.getOrDefault(maxSendFluid, new HashSet<>());
            posSet.add(pos);
            this.NeighborBlock.put(maxSendFluid, posSet);
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

        public void addFluidCable(BlockPos cablePos, TileFluidCableBase cableBase) {
            this.InFluidCables.put(cablePos, cableBase);
            FluidStack stack = cableBase.getFluidStack();
            if(stack != null) {
                if (this.getFluidStack() == null) {
                    this.setFluidStack(stack.copy());
                } else {
                    this.fluidStack.amount += stack.copy().amount;
                }
            }
        }

        public HashMap<Integer, Integer> getIndex_MaxSendFluid() {
            return this.Index_MaxSendFluid;
        }

        public int getMaxSendFluidByIndex(int DestIndex) {
            return this.Index_MaxSendFluid.getOrDefault(DestIndex, this.MaxSendFluid);
        }

        @Nullable
        public FluidStack getFluidStack() {
            return this.fluidStack;
        }

        public void setFluidStack(@Nullable FluidStack fluidStack) {
            if (this.fluidStack == null) {
                this.fluidStack = fluidStack;
            }
        }

        public boolean JudgeCableFluidStack(@Nullable FluidStack stack) {
            return this.fluidStack == null || stack == null || this.fluidStack.getFluid().equals(stack.getFluid());
        }

        public void AddMachineReceiverPos(BlockPos pos, EnumFacing facing, IFluidHandler fluidHandler) {
            Set<Pair<EnumFacing, IFluidHandler>> set = this.MachineReceiverPos.getOrDefault(pos, new HashSet<>(EnumFacing.VALUES.length));
            set.add(new Pair<>(facing, fluidHandler));
            this.MachineReceiverPos.put(pos, set);
        }

        public void AddMachineProviderPos(BlockPos pos, EnumFacing facing, IFluidHandler fluidHandler, int index) {
            Set<Triple<EnumFacing, IFluidHandler, Integer>> set = this.MachineProviderPos.getOrDefault(pos, new HashSet<>(EnumFacing.VALUES.length));
            set.add(new Triple<>(facing, fluidHandler, index));
            this.MachineProviderPos.put(pos, set);
        }

        public boolean hasMachineByPos(BlockPos CablePos, EnumFacing facing) {
            BlockPos tilePos = CablePos.offset(facing);

            return false;
        }

        /**
         * 初めにネットワーク内のマシンから液体を回収する。
         * ネットワーク全体のマシンに分配
         * 余剰分をネットワーク全体のパイプに分配
         *
         */

        public void Update(World world, FluidCableManager manager) {
            int Storage = this.fluidStack != null ? this.fluidStack.amount : 0;

            for(BlockPos pos : this.MachineProviderPos.keySet()) {
                for(Triple<EnumFacing, IFluidHandler, Integer> triple : this.MachineProviderPos.get(pos)) {
                    IFluidTankProperties properties = triple.getvalueB().getTankProperties()[triple.getvalueC()];
                    if(properties.getContents() != null) {
                        FluidStack stack = properties.getContents();
                        IFluidHandler handler = triple.getvalueB();
                        FluidStack drainStack = new FluidStack(stack.getFluid(), Math.min(Math.min(this.getMaxSendFluid(), stack.amount), (this.getMaxCapacity() - Storage)));
                        if(Storage == 0) {
                            this.setFluidStack(stack.copy());
                            FluidStack drainedStack = handler.drain(drainStack, true);
                            if(drainedStack != null) {
                                Storage = drainedStack.amount;
                            }
                        } else {
                            if (this.fluidStack.getFluid() == stack.getFluid()) {
                                FluidStack drainedStack = handler.drain(drainStack, true);
                                if (drainedStack != null) {
                                    Storage += drainedStack.amount;
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("index: " + this.getIndex() + ", Storage: " + Storage);

            //HashMap<Integer, Integer>
            for(int index : this.getIndex_MaxSendFluid().keySet()) {
                int maxSend = this.getMaxSendFluidByIndex(index);
                CableData cableData = manager.getCableDataByIndex(index);

            }
        }

        public BlockPos getHostPos() {
            return this.HostPos;
        }

        public int getMaxSendFluid() {
            return this.MaxSendFluid;
        }

        public int getMaxCapacity() {
            return this.getMaxSendFluid() * this.InFluidCables.size();
        }

        public int getNeedFluid() {
            return this.getMaxCapacity() - (this.getFluidStack() != null ? this.getFluidStack().amount : 0);
        }

        public int getIndex() {
            return this.Index;
        }
    }
}