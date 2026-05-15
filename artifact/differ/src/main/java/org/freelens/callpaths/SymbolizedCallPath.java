package org.freelens.callpaths;

import lombok.Getter;

public class SymbolizedCallPath implements IPath, Comparable<SymbolizedCallPath>{
    @Getter
    private final String sdkMethod;
    @Getter
    private final int depth;

    public SymbolizedCallPath(String sdkMethod, int depth) {
        this.sdkMethod = sdkMethod;
        this.depth = depth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SymbolizedCallPath other) {
            return this.sdkMethod.equals(other.sdkMethod) && this.depth == other.depth;
        }
        return false;
    }

    @Override
    public int compareTo(SymbolizedCallPath other) {
        // First compare by SDK method
        int sdkCompare = this.sdkMethod.compareTo(other.sdkMethod);
        if (sdkCompare != 0) {
            return sdkCompare;
        }
        // If SDK methods are equal, compare by depth
        return Integer.compare(this.depth, other.depth);
    }



}
