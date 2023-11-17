package com.aki.advanced_industry.tile.handler;

import com.aki.advanced_industry.tile.TileEntityBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.List;

public class TileEntityChunkLoadingHandler implements ForgeChunkManager.LoadingCallback {
    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for(ForgeChunkManager.Ticket ticket : tickets) {
            int x = ticket.getModData().getInteger("xCoord");
            int y = ticket.getModData().getInteger("yCoord");
            int z = ticket.getModData().getInteger("zCoord");
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if(te instanceof TileEntityBase && ((TileEntityBase) te).DoChunkLoading()) {
                ((TileEntityBase) te).forceChunkLoading(ticket);
            }
        }
    }
}
