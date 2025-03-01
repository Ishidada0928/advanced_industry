package com.aki.advanced_industry.api.packet;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import com.aki.mcutils.APICore.DataManage.DataListManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTileData  implements IMessage {

    public BlockPos pos;

    public DataListManager parameters = new DataListManager();

    public int dimension = 0;

    public PacketTileData() {
    }

    public PacketTileData(TileEntityBase tile) {
        this(tile.getPos(), tile.getNetWorkData(), tile.getWorld().provider.getDimension());
    }

    public PacketTileData(TileEntity tile, DataListManager params) {
        this(tile.getPos(), params, tile.getWorld().provider.getDimension());
    }

    public PacketTileData(BlockPos pos, DataListManager params, int dim) {
        this.pos = pos;
        parameters = params;
        this.dimension = dim;
    }

    @Override
    public void toBytes(ByteBuf dataStream) {
        dataStream.writeLong(this.pos.toLong());
        dataStream.writeInt(this.dimension);
        parameters.writeDatas(dataStream);
    }

    @Override
    public void fromBytes(ByteBuf dataStream) {
        this.pos = BlockPos.fromLong(dataStream.readLong());
        this.dimension = dataStream.readInt();
        this.parameters.readDatas(dataStream);
    }

    public static class HandlerClient implements IMessageHandler<PacketTileData, IMessage> {

        @Override
        public IMessage onMessage(PacketTileData message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                World world = Minecraft.getMinecraft().world;
                TileEntity tile = world.getTileEntity(message.pos);
                if (tile instanceof IPacketTileData) {
                    ((IPacketTileData) tile).ReceivePacketData(message.parameters);
                }
            });
            return null;
        }
    }
}