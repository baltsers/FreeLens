package org.freelens.sdkdiffer;

import java.util.HashSet;
import java.util.Set;

import org.checkerframework.checker.units.qual.C;
import org.freelens.data.CustomMethodCallGraph;
import org.freelens.data.CustomModifiedMethodCallGraph;
import org.freelens.data.MethodCallGraph;
import org.freelens.data.ModifiedMethodCallGraph;

import org.freelens.data.SimpleEdge;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class GraphDiffer {
     public static ModifiedMethodCallGraph compareGraphs(MethodCallGraph baseGraph, MethodCallGraph targetGraph) {
        Set<String> addedNodesStr = new HashSet<>(targetGraph.getNodesStr());
        addedNodesStr.removeAll(baseGraph.getNodesStr());

        Set<String> removedNodesStr = new HashSet<>(baseGraph.getNodesStr());
        removedNodesStr.removeAll(targetGraph.getNodesStr());
        
        Set<SimpleEdge> addedSimpleEdges = new HashSet<>(targetGraph.getSimpleEdges());
        addedSimpleEdges.removeAll(baseGraph.getSimpleEdges());

        Set<SimpleEdge> removedSimpleEdges = new HashSet<>(baseGraph.getSimpleEdges());
        removedSimpleEdges.removeAll(targetGraph.getSimpleEdges());
        
        // If no differences found, return null
        if (addedNodesStr.isEmpty() && removedNodesStr.isEmpty() && addedSimpleEdges.isEmpty() && removedSimpleEdges.isEmpty()) {
            return null;
        }
        
        // Create diff object with the differences
        ModifiedMethodCallGraph diff = new ModifiedMethodCallGraph(baseGraph.getEntryMethod());
        diff.addAddedEdgesStr(addedSimpleEdges);
        diff.addRemovedNodesStr(removedNodesStr);
        diff.addAddedEdgesStr(addedSimpleEdges);
        diff.addRemovedEdgesStr(removedSimpleEdges);
        
        // Add all SDK nodes for reference
        diff.addSDKNodesStr(baseGraph.getSdkNodesStr());
        diff.addSDKNodesStr(targetGraph.getSdkNodesStr());
        
        return diff;
    }

    public static CustomModifiedMethodCallGraph compareCustomGraphs(CustomMethodCallGraph baseGraph, CustomMethodCallGraph targetGraph) {
        Set<String> commonNodesStr = new HashSet<>(baseGraph.nodes);
        commonNodesStr.retainAll(targetGraph.nodes);

        Set<SimpleEdge> commonEdgesStr = new HashSet<>(baseGraph.simpleEdges);
        commonEdgesStr.retainAll(targetGraph.simpleEdges);

        Set<String> addedNodesStr = new HashSet<>(targetGraph.nodes);
        addedNodesStr.removeAll(baseGraph.nodes);

        Set<String> removedNodesStr = new HashSet<>(baseGraph.nodes);
        removedNodesStr.removeAll(targetGraph.nodes);
        
        Set<SimpleEdge> addedSimpleEdges = new HashSet<>(targetGraph.simpleEdges);
        addedSimpleEdges.removeAll(baseGraph.simpleEdges);

        Set<SimpleEdge> removedSimpleEdges = new HashSet<>(baseGraph.simpleEdges);
        removedSimpleEdges.removeAll(targetGraph.simpleEdges);

        Set<String> addedSdkNodesStr = new HashSet<>(targetGraph.sdkNodes);
        addedSdkNodesStr.removeAll(baseGraph.sdkNodes);

        Set<String> removedSdkNodesStr = new HashSet<>(baseGraph.sdkNodes);
        removedSdkNodesStr.removeAll(targetGraph.sdkNodes);
        
        // If no differences found, return null
        if (addedNodesStr.isEmpty() && removedNodesStr.isEmpty() && addedSimpleEdges.isEmpty() && removedSimpleEdges.isEmpty()) {
            return null;
        }
        
        // Create diff object with the differences
        CustomModifiedMethodCallGraph diff = new CustomModifiedMethodCallGraph(baseGraph.entryMethod);
        diff.addedNodesStr.addAll(addedNodesStr);
        diff.removedNodesStr.addAll(removedNodesStr);
        diff.addedEdgesStr.addAll(addedSimpleEdges);
        diff.removedEdgesStr.addAll(removedSimpleEdges);
        diff.sdkNodesStr.addAll(baseGraph.sdkNodes);
        diff.sdkNodesStr.addAll(targetGraph.sdkNodes);
        diff.commonNodesStr.addAll(commonNodesStr);
        diff.commEdgesStr.addAll(commonEdgesStr);
        diff.addedSdkNodesStr.addAll(addedSdkNodesStr);
        diff.removedSdkNodesStr.addAll(removedSdkNodesStr);

        return diff;
    }
    
}
