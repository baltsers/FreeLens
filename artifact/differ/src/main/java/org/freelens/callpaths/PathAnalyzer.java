package org.freelens.callpaths;

import com.google.common.collect.Sets;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

public class PathAnalyzer {

    private static Pair<List<CallPath>, List<CallPath>> findDiffPathInObfuscatedCallPaths(List<CallPath> v1Paths, List<CallPath> v2Paths) {
        // Sort paths by intermediate methods char length
        Map<Integer, List<CallPath>> v1PathsByLength = v1Paths.stream()
                .collect(Collectors.groupingBy(CallPath::intermediateMethodsCharLength));
        Map<Integer, List<CallPath>> v2PathsByLength = v2Paths.stream()
                .collect(Collectors.groupingBy(CallPath::intermediateMethodsCharLength));


        List<CallPath> v1Diffs = new ArrayList<>();
        List<CallPath> v2Diffs = new ArrayList<>();

        // Find lengths that exist in v1 but not in v2
        for (Integer length : v1PathsByLength.keySet()) {
            if (!v2PathsByLength.containsKey(length)) {
                v1Diffs.addAll(v1PathsByLength.get(length));
            }
        }

        // Find lengths that exist in v2 but not in v1
        for (Integer length : v2PathsByLength.keySet()) {
            if (!v1PathsByLength.containsKey(length)) {
                v2Diffs.addAll(v2PathsByLength.get(length));
            }
        }

        return Pair.of(v1Diffs, v2Diffs);
    }



    public static ChangeReport comparePathsOfSameEntry(List<CallPath> v1Paths, List<CallPath> v2Paths) {
        ChangeReport report = new ChangeReport();

        // Group paths by SDK method and depth
        Map<Pair<String, Integer>, List<CallPath>> v1Grouped = groupBySDKAndDepth(v1Paths);
        Map<Pair<String, Integer>, List<CallPath>> v2Grouped = groupBySDKAndDepth(v2Paths);

        for (Map.Entry<Pair<String, Integer>, List<CallPath>> v1Entry : v1Grouped.entrySet()) {
            Pair<String, Integer> key = v1Entry.getKey();
            List<CallPath> v1PathsForKey = v1Entry.getValue();
            List<CallPath> v2PathsForKey = v2Grouped.get(key);

            if (v2PathsForKey == null) {
                // This SDK method + depth combination doesn't exist in v2
                report.getRemovedPaths().addAll(v1PathsForKey);
                continue;
            }

            // Compare sizes
            if (v1PathsForKey.size() != v2PathsForKey.size()) {
                // Try to find strictly equal pairs
                if (!findStrictlyEqualPath(v1PathsForKey, v2PathsForKey)) {
                    // No strictly equal pairs found - likely obfuscated
                    // Find the extra path among obfuscated paths
                    Pair<List<CallPath>, List<CallPath>> diffs =
                            findDiffPathInObfuscatedCallPaths(v1PathsForKey, v2PathsForKey);

                    report.getPotentialChanges().add(new ChangeReport.PotentialChange(
                            v1PathsForKey,
                            v2PathsForKey,
                            key.getFirst(),
                            key.getSecond()
                    ));

                    if (!diffs.getFirst().isEmpty() || !diffs.getSecond().isEmpty()) {
                        // Found likely different paths based on length
                        report.getRemovedPaths().addAll(diffs.getFirst());
                        report.getAddedPaths().addAll(diffs.getSecond());

//                    } else {
//                        // Still can't determine differences, mark as potential change
//                        report.getPotentialChanges().add(new ChangeReport.PotentialChange(
//                                v1PathsForKey,
//                                v2PathsForKey,
//                                key.getFirst(),
//                                key.getSecond()
//                        ));
                    }
                }
            }

            // Remove processed entries
            v2Grouped.remove(key);
        }

        // Any remaining entries in v2Grouped are new paths
        for (List<CallPath> remainingV2Paths : v2Grouped.values()) {
            report.getAddedPaths().addAll(remainingV2Paths);
        }

        return report;
    }

    private static boolean findStrictlyEqualPath(List<CallPath> v1Paths, List<CallPath> v2Paths) {
        for (CallPath v1Path : v1Paths) {
            for (CallPath v2Path : v2Paths) {
                if (v1Path.strictlyEquals(v2Path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<Pair<String, Integer>, List<CallPath>> groupBySDKAndDepth(List<CallPath> paths) {
        Map<Pair<String, Integer>, List<CallPath>> grouped = new HashMap<>();
        for (CallPath path : paths) {
            Pair<String, Integer> key = Pair.of(path.getSdkMethod(), path.getDepth());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(path);
        }
        return grouped;
    }
}
