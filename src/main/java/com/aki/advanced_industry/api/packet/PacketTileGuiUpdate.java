package com.aki.advanced_industry.api.packet;

import com.aki.akisutils.apis.data.manager.DataListManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTileGuiUpdate implements IMessageHandler<PacketTileGuiUpdate.TileEntityGUIMessage, IMessage> {

    @Override
    public IMessage onMessage(TileEntityGUIMessage message, MessageContext context) {
        World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);
        TileEntity tile = world.getTileEntity(message.pos);
        if(tile instanceof IPacketTileGuiUpdate) {
            ((IPacketTileGuiUpdate)tile).ReceiveGUIUpdatePacketData(message.parameters);
        }
        return null;
    }

    public static class TileEntityGUIMessage implements IMessage {

        public BlockPos pos;

        public DataListManager parameters = new DataListManager();

        public int dimension = 0;

        public TileEntityGUIMessage() {
        }

        public TileEntityGUIMessage(TileEntity tile, DataListManager params) {
            this(tile.getPos(), params, tile.getWorld().provider.getDimension());
        }

        public TileEntityGUIMessage(BlockPos pos, DataListManager params, int dim) {
            this.pos = pos;
            parameters = params;
            this.dimension = dim;
        }

        @Override
        public void toBytes(ByteBuf dataStream) {
            dataStream.writeLong(this.pos.toLong());
            dataStream.writeInt(this.dimension);
            parameters.writeData(dataStream);
        }

        @Override
        public void fromBytes(ByteBuf dataStream) {
            this.pos = BlockPos.fromLong(dataStream.readLong());
            this.dimension = dataStream.readInt();
            this.parameters.readData(dataStream);
        }
    }
}