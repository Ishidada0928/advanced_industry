package com.aki.advanced_industry.mods.industry.util.implement;

import net.minecraft.util.math.BlockPos;

public interface IFluidCableConnector {
    void SendFluid(BlockPos StartPos, long tick, int Max);

    //すべてのパイプに均等に液体を注ぐため(Setで)
    void FluidReceiverCountCheck(BlockPos StartPos, long tick);
}
