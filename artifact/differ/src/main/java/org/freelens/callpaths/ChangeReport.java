package org.freelens.callpaths;

import lombok.Data;

import java.util.*;

@Data
public class ChangeReport {
    private final List<CallPath> addedPaths = new ArrayList<>();
    private final List<CallPath> removedPaths = new ArrayList<>();
//    private final Map<String, List<String>> structuralChanges = new HashMap<>();
//    private final Map<String, List<String>> sdkUsageChanges = new HashMap<>();
    private final List<PotentialChange> potentialChanges = new ArrayList<>();


    @Data
    public static class PotentialChange {
        private final List<CallPath> v1Paths;
        private final List<CallPath> v2Paths;
        private final String sdkMethod;
        private final int depth;
    }

    public boolean hasNoChanges() {
        return addedPaths.isEmpty() && removedPaths.isEmpty() && potentialChanges.isEmpty();
    }
}
