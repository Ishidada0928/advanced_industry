package com.aki.advanced_industry.mods.industry.util;

public enum CableConnectionMode {
    NORMAL(0),
    PULL(1),
    PUSH(2),
    CLOSE(3);

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
                return CLOSE;
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
