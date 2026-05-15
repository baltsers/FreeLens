package org.freelens.callpaths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodCallPaths {
    private final String entryMethod;

    private final List<CallPath> callPaths;

    public MethodCallPaths(String entryMethod) {
        this.entryMethod = entryMethod;
        this.callPaths = new ArrayList<>();
    }

    public MethodCallPaths(String entryMethod, List<CallPath> callPaths) {
        this.entryMethod = entryMethod;
        this.callPaths = callPaths;
    }

    public void addCallPath(CallPath callPath) {
        callPaths.add(callPath);
    }

    public String getEntryMethod() {
        return entryMethod;
    }

    public List<CallPath> getCallPaths() {
        return callPaths;
    }

    public int size() {
        return callPaths.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Entry method: ").append(entryMethod).append("\n");
        for (CallPath callPath : callPaths) {
            sb.append(callPath.toString()).append("\n");
        }
        return sb.toString();
    }



}
