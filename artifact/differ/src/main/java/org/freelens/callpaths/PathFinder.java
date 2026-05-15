package org.freelens.callpaths;

import org.freelens.data.SDKMethods;
import org.freelens.data.SerializableCallGraph;
import org.freelens.util.SootMethodUtil;
import soot.SootMethod;

import java.util.*;

import static org.freelens.util.SootMethodUtil.getUserDefinedMethodsInClass;

public class PathFinder {

    public static int maxDepth = 5;

    private PathFinder() {
    }

    public static List<CallPath> findPathsForOneEntry(String entryMethod, SerializableCallGraph callGraph) {
        List<CallPath> paths = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        dfs(callGraph, entryMethod, visited, currentPath, paths);
        return paths;
    }

    public static List<SymbolizedCallPath> findPathsForAllEntries(String classname, SerializableCallGraph callGraph) {
        Set<SootMethod> userDefinedMethods = SootMethodUtil.getUserDefinedMethodsInClass(classname);
        List<SymbolizedCallPath> allPaths = new ArrayList<>();
        for (SootMethod userDefinedMethod : userDefinedMethods) {
            String entryMethod = userDefinedMethod.getSignature();
            List<SymbolizedCallPath> paths = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            List<String> currentPath = new ArrayList<>();
            dfsSymbolized(callGraph, entryMethod, visited, currentPath, paths);
            allPaths.addAll(paths);
        }
        return allPaths;
    }

    private static void dfsSymbolized(SerializableCallGraph callGraph,
                     String currentMethod,
                     Set<String> visited,
                     List<String> currentPath,
                     List<SymbolizedCallPath> paths) {
        if (currentPath.size() > maxDepth) {
            return;
        }


        // If we hit an SDK method, create a path
        if (SDKMethods.isSDKMethod(currentMethod) && !currentPath.isEmpty()) {
            paths.add(new SymbolizedCallPath(currentMethod, currentPath.size() + 1));
            return;
        }

        // Cycle detection
        if (visited.contains(currentMethod)) {
            return;
        }

        // Add current method to path and visited set
        visited.add(currentMethod);
        currentPath.add(currentMethod);

        // Explore outgoing edges
        Iterator<SerializableCallGraph.CustomEdge> outEdges = callGraph.edgesOutOf(currentMethod);
        while (outEdges.hasNext()) {
            SerializableCallGraph.CustomEdge edge = outEdges.next();
            List<String> newPath = new ArrayList<>(currentPath);
            dfsSymbolized(callGraph, edge.getTgtMethod(), new HashSet<>(visited), newPath, paths);
        }

        // Backtrack
        visited.remove(currentMethod);
        currentPath.remove(currentPath.size() - 1);

    }



    private static void dfs(SerializableCallGraph callGraph,
                     String currentMethod,
                     Set<String> visited,
                     List<String> currentPath,
                     List<CallPath> paths) {


        if (currentPath.size() > maxDepth) {
            return;
        }


        // If we hit an SDK method, create a path
        if (SDKMethods.isSDKMethod(currentMethod) && !currentPath.isEmpty()) {
            List<String> intermediates = currentPath.subList(1, currentPath.size());
            paths.add(new CallPath(currentPath.get(0), currentMethod, intermediates));
            return;
        }

        // Cycle detection
        if (visited.contains(currentMethod)) {
            return;
        }

        // Add current method to path and visited set
        visited.add(currentMethod);
        currentPath.add(currentMethod);

        // Explore outgoing edges
        Iterator<SerializableCallGraph.CustomEdge> outEdges = callGraph.edgesOutOf(currentMethod);
        while (outEdges.hasNext()) {
            SerializableCallGraph.CustomEdge edge = outEdges.next();
            List<String> newPath = new ArrayList<>(currentPath);
            dfs(callGraph, edge.getTgtMethod(), new HashSet<>(visited), newPath, paths);
        }

        // Backtrack
        visited.remove(currentMethod);
        currentPath.remove(currentPath.size() - 1);
    }
}
