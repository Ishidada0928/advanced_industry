package com.aki.advanced_industry.api.packet;

import com.aki.akisutils.apis.data.manager.DataListManager;

public interface IPacketTileGuiUpdate {
    public void ReceiveGUIUpdatePacketData(DataListManager dataListManager);
}