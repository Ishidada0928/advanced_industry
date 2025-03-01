package com.aki.advanced_industry.api.packet;

import com.aki.mcutils.APICore.DataManage.DataListManager;

public interface IPacketTileGuiUpdate {
    public void ReceiveGUIUpdatePacketData(DataListManager dataListManager);
}