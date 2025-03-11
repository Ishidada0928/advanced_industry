package com.aki.advanced_industry.mods.industry.util.list;

public class Triple<A, B, C> {
    private final A valueA;
    private final B valueB;
    private final C valueC;

    public Triple(A valueA, B valueB, C valueC) {
        this.valueA = valueA;
        this.valueB = valueB;
        this.valueC = valueC;
    }

    public A getvalueA() {
        return this.valueA;
    }

    public B getvalueB() {
        return this.valueB;
    }

    public C getvalueC() {
        return this.valueC;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Triple<?, ?, ?> triple = (Triple) o;
            return this.valueA.equals(triple.valueA) && (this.valueB.equals(triple.valueB) && this.valueC.equals(triple.valueC));
        } else {
            return false;
        }
    }

    public int hashCode() {
        return 31 * this.valueA.hashCode() + 31 * this.valueB.hashCode() + 31 * this.valueC.hashCode();
    }

    public String toString() {
        return "(" + this.valueA + ", " + this.valueB + ", " + this.valueC + ")";
    }
}
