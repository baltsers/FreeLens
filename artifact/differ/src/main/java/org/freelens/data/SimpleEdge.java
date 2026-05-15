package org.freelens.data;

public class SimpleEdge {
    public String src;
    public String tgt;

    public SimpleEdge(String src, String tgt) {
        this.src = src;
        this.tgt = tgt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleEdge)) {
            return false;
        }
        SimpleEdge edge = (SimpleEdge) obj;
        return src.equals(edge.src) && tgt.equals(edge.tgt);
    }

    @Override
    public int hashCode() {
        return src.hashCode() + tgt.hashCode();
    }

    @Override
    public String toString() {
        return src + " -> " + tgt;
    }
}
