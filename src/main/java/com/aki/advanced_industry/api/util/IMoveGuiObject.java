package com.aki.advanced_industry.api.util;

import com.aki.akisutils.apis.util.list.Pair;

public interface IMoveGuiObject {
    boolean IsMoval(int mouseX, int mouseY);

    void Move(int mouseX, int mouseY);

    void Reset();

    //Render・当たり判定処理。
    Pair<Integer, Integer> getXY();
}
