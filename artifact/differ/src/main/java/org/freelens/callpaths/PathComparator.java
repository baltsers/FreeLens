package org.freelens.callpaths;

import java.util.Collections;
import java.util.List;

public class PathComparator {
    public static boolean comparePaths(List<SymbolizedCallPath> paths1, List<SymbolizedCallPath> paths2) {
        // First check sizes
        if (paths1.size() != paths2.size()) {
            return false;
        }

        // Sort both lists
        Collections.sort(paths1);
        Collections.sort(paths2);

        // Compare each element
        return paths1.equals(paths2);
    }
}
