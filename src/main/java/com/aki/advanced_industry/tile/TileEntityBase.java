package com.aki.advanced_industry.tile;

import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.redstoneflux.api.IEnergyStorage;
import cofh.redstoneflux.impl.EnergyStorage;
import com.aki.advanced_industry.AdvancedIndustryCore;
import com.aki.advanced_industry.packet.IPacketTileData;
import com.aki.advanced_industry.packet.IPacketTileGuiUpdate;
import com.aki.advanced_industry.packet.PacketTileData;
import com.aki.advanced_industry.packet.PacketTileDataRequest;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityBase extends TileEntity implements ITickable, IPacketTileData, IPacketTileGuiUpdate, IEnergyStorage, IEnergyReceiver {
    public long lastChangeTime = 0;
    public ForgeChunkManager.Ticket chunkTicket = null;

    public EnergyStorage energyStorage = new EnergyStorage(0);

    public TileEntityBase() {

    }

    public TileEntityBase(int MaxEnergyStorage) {
        energyStorage = new EnergyStorage(MaxEnergyStorage);
    }

    public void sendUpdates() {
        SendPacketUpdate();
        world.markBlockRangeForRenderUpdate(pos, pos);
        world.notifyBlockUpdate(pos, getState(), getState(), 3);
        world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
        markDirty();
    }

    @Override
    public void markDirty() {
        if (hasWorld() && world.isBlockLoaded(getPos())) { // we need the loaded check to make sure we don't trigger a chunk load while the chunk is loaded
            world.markChunkDirty(pos, this);
            IBlockState state = world.getBlockState(pos);
            if (state.hasComparatorInputOverride()) {
                world.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
    }

    //
    // Sends an update packet to all players who have this TileEntity loaded. Needed because inventory changes are not synced in a timely manner unless the player
    // has the GUI open. And sometimes the rendering needs the inventory...
    //
    public void forceUpdatePlayers() {
        if (!(world instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer) world;
        PlayerChunkMap playerManager = worldServer.getPlayerChunkMap();
        SPacketUpdateTileEntity updatePacket = null;

        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        for (EntityPlayer playerObj : world.playerEntities) {
            if (playerObj instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) playerObj;

                if (playerManager.isPlayerWatchingChunk(player, chunkX, chunkZ)) {
                    if (updatePacket == null) {
                        updatePacket = getUpdatePacket();
                        if (updatePacket == null) {
                            return;
                        }
                    }
                    try {
                        player.connection.sendPacket(updatePacket);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    @Override
    protected void setWorldCreate(@Nonnull World worldIn) {
        // Forge gives us our World earlier than vanilla. No idea why it doesn't get put into #world but is ignored by default.
        // Anyway, this is helpful while reading our nbt, so let's use it.
        setWorld(worldIn);
    }

    private IBlockState getState() {
        return world.getBlockState(pos);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound = this.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        //super.onDataPacket(net, pkt);
        this.readFromNBT(pkt.getNbtCompound());//handleUpdateTag(pkt.getNbtCompound());
        //this.sendUpdates();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.lastChangeTime = compound.getLong("LastChangeTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("LastChangeTime", this.lastChangeTime);
        return super.writeToNBT(compound);
    }

    @Override
    public void update() {
        if(!world.isRemote) {
            this.forceUpdatePlayers();
        }

        if (chunkTicket != null) {
            chunkTicket.getModData().setInteger("xCoord", this.getPos().getX());
            chunkTicket.getModData().setInteger("yCoord", this.getPos().getY());
            chunkTicket.getModData().setInteger("zCoord", this.getPos().getZ());
        }
    }

    @Override
    public void validate() {
        super.validate();
        if(!getWorld().isRemote && chunkTicket == null) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(AdvancedIndustryCore.INSTANCE, getWorld(), ForgeChunkManager.Type.NORMAL);
            //ticket.setChunkListDepth();
            if(ticket != null) {
                forceChunkLoading(ticket);
            }
        }
    }

    @Override
    public void invalidate() {
        if (chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
        }
        this.stopChunkLoading();
        super.invalidate();
    }

    public void forceChunkLoading(ForgeChunkManager.Ticket ticket) {
        stopChunkLoading();
        this.chunkTicket = ticket;
        List<ChunkPos> chunks = getChunkPos();
        for (ChunkPos pos : chunks) {
            if (!this.chunkTicket.getChunkList().contains(pos)) {
                ForgeChunkManager.forceChunk(this.chunkTicket, pos);
                //this.world.getChunkProvider().getLoadedChunk(pos.x, pos.z);
            }
        }
    }

    public List<ChunkPos> getChunkPos() {
        List<ChunkPos> poses = new ArrayList<>();
        ChunkPos minChunkPos = new ChunkPos(this.getPos().add(-16,0,-16));
        ChunkPos maxChunkPos = new ChunkPos(this.getPos().add(16,0,16));
        for (int x = minChunkPos.x; x <= maxChunkPos.x; x++) {
            for (int z = minChunkPos.z; z <= maxChunkPos.z; z++) {
                poses.add(new ChunkPos(x, z));
            }
        }
        poses.add(new ChunkPos(this.getPos()));
        return poses;
    }

    public void unforceChunkLoading() {
        for(Object obj : chunkTicket.getChunkList()) {
            ChunkPos coord = (ChunkPos) obj;
            ForgeChunkManager.unforceChunk(chunkTicket, coord);
        }
    }

    public void stopChunkLoading() {
        if(chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
            chunkTicket = null;
        }
    }


    /**
     * false==チャンクをロードしつづけない。
     * true==チャンクをロードしつづける
     */
    public boolean DoChunkLoading() {
        return false;
    }


    public boolean getBreakOK(IBlockState state, BlockPos Target) {
        return state.getMaterial() != Material.AIR && getBreakBlock(state, Target) && !(state.getBlock() instanceof IFluidBlock || state.getBlock() instanceof BlockLiquid || state.getMaterial().isLiquid());
    }

    public boolean getBreakBlock(IBlockState stateBase, BlockPos pos1) {
        float i = stateBase.getBlockHardness(world, pos1);
        return (i >= 0.0f);
    }

    @Override
    public DataListManager getNetWorkData() {
        DataListManager dataListManager = new DataListManager();
        dataListManager.addData(lastChangeTime = world.getTotalWorldTime());
        return dataListManager;
    }

    @Override
    public void ReceivePacketData(DataListManager dataListManager) {
        this.lastChangeTime = dataListManager.getDataLong();
    }

    @Override
    public void onLoad() {
        //super.onLoad();
        if(world.isRemote) {
            AdvancedIndustryCore.wrapper.sendToServer(new PacketTileDataRequest(this));
        }
    }

    public void SendPacketUpdate() {
        this.lastChangeTime = this.world.getTotalWorldTime();
        AdvancedIndustryCore.wrapper.sendToAllAround(new PacketTileData(TileEntityBase.this), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        double range = 1024;
        return new AxisAlignedBB(-range, -range, -range, range, range, range);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public void ReceiveGUIUpdatePacketData(DataListManager dataListManager) {

    }

    @Override
    public int receiveEnergy(EnumFacing enumFacing, int i, boolean b) {
        this.sendUpdates();
        return energyStorage.receiveEnergy(i, b);
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {

        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return false;
    }

    @Override
    public int receiveEnergy(int i, boolean b) {
        this.sendUpdates();
        return energyStorage.receiveEnergy(i, b);
    }

    @Override
    public int extractEnergy(int i, boolean b) {
        return energyStorage.extractEnergy(i, b);
    }

    @Override
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }
}
