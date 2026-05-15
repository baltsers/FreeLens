package org.freelens.sdkdiffer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.freelens.data.CustomMethodCallGraph;
import org.freelens.data.MethodCallGraph;
import org.freelens.data.SDKMethods;
import org.freelens.data.SerializableCallGraph;
import org.freelens.data.SimpleEdge;
import org.freelens.data.SerializableCallGraph.CustomEdge;

import com.google.common.collect.Iterators;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class CallGraphAnalyzer {
    public static int MAX_DEPTH = 2;

    public static MethodCallGraph getSDKCallSubgraph(SootMethod entryMethod, CallGraph callGraph) {
        System.out.println("\nStarting subgraph collection for: " + entryMethod.getSignature());
        MethodCallGraph subgraph = new MethodCallGraph(entryMethod);
        Set<SootMethod> visited = new HashSet<>();
        collectSubgraph(entryMethod, callGraph, subgraph, visited, 0);
        System.out.println("Completed subgraph collection. Nodes: " + subgraph.getNodes().size() + 
                          ", Edges: " + subgraph.getEdges().size() + 
                          ", SDK Nodes: " + subgraph.getSDKNodes().size());
        return subgraph;
    }

    public static CustomMethodCallGraph getSDKCallCustomSubgraph(String entryMethod, SerializableCallGraph refCallGraph) {
        System.out.println("\nStarting subgraph collection for: " + entryMethod);
        CustomMethodCallGraph subgraph = new CustomMethodCallGraph(entryMethod);
        Set<String> visited = new HashSet<>();
        collectCustomSubgraph(entryMethod, refCallGraph, subgraph, visited, 0);
        System.out.println("Completed subgraph collection. Nodes: " + subgraph.nodes.size() + 
        ", Edges: " + subgraph.simpleEdges.size() + 
        ", SDK Nodes: " + subgraph.sdkNodes.size());
        return subgraph;
    }

    public static void collectCustomSubgraph(
            String currentMethod,
            SerializableCallGraph callGraph,
            CustomMethodCallGraph subgraph,
            Set<String> visited,
            int depth) {
        
        //Check depth limit
        if (depth > MAX_DEPTH) {
            return;
        }

        if (SDKMethods.isJavaLibraryMethod(currentMethod)) {
            subgraph.sdkNodes.add(currentMethod);
            return;
        }

        // If current method is an initializer method and has no outgoing edges, stop here
        if (SDKMethods.isInitializerMethod(currentMethod) &&
            Iterators.size(callGraph.edgesOutOf(currentMethod)) == 0) {
            return;
        }

        // Check if already visited
        if (visited.contains(currentMethod)) {
            //System.out.println(indent + "Already visited, skipping: " + currentMethod);
            return;
        }

        visited.add(currentMethod);
        subgraph.nodes.add(currentMethod);

        // If current method is SDK method, mark it and stop here
        if (SDKMethods.isSDKMethod(currentMethod)) {
            subgraph.sdkNodes.add(currentMethod);
            return;
        }

        // Process outgoing edges
        Iterator<CustomEdge> outEdges = callGraph.edgesOutOf(currentMethod);

        while (outEdges.hasNext()) {
            CustomEdge edge = outEdges.next();
            String target = edge.getTgtMethod();

            if (SDKMethods.isJavaLibraryMethod(target)) {
                continue;
            }

            subgraph.simpleEdges.add(new SimpleEdge(currentMethod, target));
            subgraph.nodes.add(target);
            collectCustomSubgraph(target, callGraph, subgraph, visited, depth + 1);
        }
    }

    private static void collectSubgraph(
            SootMethod currentMethod,
            CallGraph callGraph,
            MethodCallGraph subgraph,
            Set<SootMethod> visited,
            int depth) {
        
        // Debug current state
        String indent = "  ".repeat(depth);  // Visual indentation based on depth
        System.out.println(indent + "Visiting depth " + depth + ": " + currentMethod.getSignature());
        
        // Check depth limit
        if (depth >= MAX_DEPTH) {
            System.out.println("\n!!! MAX DEPTH REACHED !!!");
            System.out.println("At method: " + currentMethod.getSignature());
            System.out.println("Current depth: " + depth);
            System.out.println("Max allowed depth: " + MAX_DEPTH);
            System.out.println("Call chain will be truncated here");
            return;
        }
        
        // Check if already visited
        if (visited.contains(currentMethod)) {
            System.out.println(indent + "Already visited, skipping: " + currentMethod.getSignature());
            return;
        }

        visited.add(currentMethod);
        subgraph.addNode(currentMethod);
        subgraph.addNodeStr(currentMethod.getSignature());

        // If current method is SDK method, mark it and stop here
        if (SDKMethods.isSDKMethod(currentMethod.getSignature())) {
            System.out.println(indent + "Found SDK method, stopping here: " + currentMethod.getSignature());
            subgraph.addSDKNode(currentMethod);
            subgraph.addSDKNodeStr(currentMethod.getSignature());
            return;
        }

        // Process outgoing edges
        Iterator<Edge> outEdges = callGraph.edgesOutOf(currentMethod);
        int edgeCount = 0;
        
        while (outEdges.hasNext()) {
            Edge edge = outEdges.next();
            SootMethod target = edge.getTgt().method();
            edgeCount++;
            
            System.out.println(indent + "Processing edge " + edgeCount + 
                             " at depth " + depth + ": " + 
                             currentMethod.getName() + " -> " + target.getName());
            
            subgraph.addEdge(edge);
            subgraph.addSimpleEdge(new SimpleEdge(currentMethod.getSignature(), target.getSignature()));
            collectSubgraph(target, callGraph, subgraph, visited, depth + 1);
        }
        
        if (edgeCount == 0) {
            System.out.println(indent + "No outgoing edges from: " + currentMethod.getSignature());
        }
    }


}
