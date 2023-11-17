package com.aki.advanced_industry.mods.industry.util;

import cofh.redstoneflux.impl.EnergyStorage;
import net.minecraft.util.math.BlockPos;

public interface IEnergyCableConnector {
    //エネルギーはStartPosから取得
    void SendEnergy(BlockPos StartPos, long tick, int MaxSendEnergy);

    void EnergyReceiverCountCheck(BlockPos StartPos, long tick);
}
