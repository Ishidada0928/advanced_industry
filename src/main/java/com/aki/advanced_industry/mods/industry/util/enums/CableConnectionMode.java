package com.aki.advanced_industry.mods.industry.util.enums;

public enum CableConnectionMode {
    //接続している
    NORMAL(0),
    //接続可能(閉じている)
    PULL(1),
    PUSH(2),
    NORMALCLOSE(3),
    CLOSE(4);

    int Index = 0;
    CableConnectionMode(int Index) {
        this.Index = Index;
    }

    public int getIndex() {
        return Index;
    }

    public static CableConnectionMode getCableMode(int Index) {
        switch (Index) {
            case 0:
                return NORMAL;
            case 1:
                return PULL;
            case 2:
                return PUSH;
            case 3:
                return NORMALCLOSE;
            case 4:
                return CLOSE;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
