package com.aki.advanced_industry.packet;

import com.aki.mcutils.APICore.DataManage.DataListManager;

public interface IPacketTileData {
    public DataListManager getNetWorkData();

    public void ReceivePacketData(DataListManager dataListManager);
}
