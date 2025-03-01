package com.aki.advanced_industry.api.packet;

import com.aki.advanced_industry.api.tile.TileEntityBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTileDataRequest  implements IMessage {

    public BlockPos pos;

    public int dimension = 0;

    public PacketTileDataRequest() {
    }

    public PacketTileDataRequest(TileEntityBase tile) {
        this(tile.getPos(), tile.getWorld().provider.getDimension());
    }

    public PacketTileDataRequest(BlockPos pos, int dim) {
        this.pos = pos;
        this.dimension = dim;
    }

    @Override
    public void toBytes(ByteBuf dataStream) {
        dataStream.writeLong(this.pos.toLong());
        dataStream.writeInt(this.dimension);
    }

    @Override
    public void fromBytes(ByteBuf dataStream) {
        this.pos = BlockPos.fromLong(dataStream.readLong());
        this.dimension = dataStream.readInt();
    }

    public static class HandlerServer implements IMessageHandler<PacketTileDataRequest, PacketTileData> {

        @Override
        public PacketTileData onMessage(PacketTileDataRequest message, MessageContext context) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);
            TileEntity tile = world.getTileEntity(message.pos);
            if (tile instanceof TileEntityBase) {
                return new PacketTileData((TileEntityBase) tile);//((IPacketTileData) tile).ReceivePacketData(message.parameters);
            } else return null;
        }
    }
}
