package com.aki.advanced_industry.api.packet;

import com.aki.akisutils.apis.data.manager.DataListManager;

public interface IPacketTileData {
    public DataListManager getNetWorkData();

    public void ReceivePacketData(DataListManager dataListManager);
}
