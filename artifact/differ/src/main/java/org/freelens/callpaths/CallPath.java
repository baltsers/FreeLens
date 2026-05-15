package org.freelens.callpaths;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CallPath implements IPath {
    // Getters
    @Getter
    private final String entryMethod;
    @Getter
    private final String sdkMethod;
    @Getter
    private final int depth;
    private final List<String> intermediateMethods; // Store original method names

    public CallPath(String entryMethod, String sdkMethod, List<String> intermediateMethods) {
        this.entryMethod = entryMethod;
        this.sdkMethod = sdkMethod;
        this.intermediateMethods = new ArrayList<>(intermediateMethods);
        this.depth = intermediateMethods.size();
    }

    public List<String> getIntermediateMethods() {
        return new ArrayList<>(intermediateMethods);
    }

    // For comparison and storage in HashSet/HashMap
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallPath other = (CallPath) o;
        return depth == other.depth &&
                Objects.equals(entryMethod, other.entryMethod) &&
                Objects.equals(sdkMethod, other.sdkMethod);
    }

    public boolean strictlyEquals(CallPath other) {
        return depth == other.depth &&
                Objects.equals(entryMethod, other.entryMethod) &&
                Objects.equals(sdkMethod, other.sdkMethod) &&
                intermediateMethods.equals(other.intermediateMethods);
    }

    public int callPathCharLength() {
        return entryMethod.length() + sdkMethod.length() + intermediateMethods.stream().mapToInt(String::length).sum();
    }

    public int intermediateMethodsCharLength() {
        return intermediateMethods.stream().mapToInt(String::length).sum();
    }

    @Override
    public int hashCode() {
        return Objects.hash(entryMethod, sdkMethod, depth);
    }

    // For debugging and logging
    public String toStringWithIntermediates() {
        return String.format("%s -> %s -> %s (depth: %d)",
                entryMethod,
                intermediateMethods.toString(),
                sdkMethod,
                depth);
    }

    // For abstracted representation (with U)
    public String toAbstractString() {
        StringBuilder sb = new StringBuilder(entryMethod);
        for (int i = 0; i < depth; i++) {
            sb.append(" -> U");
        }
        sb.append(" -> ").append(sdkMethod);
        return sb.toString();
    }
}
