package org.freelens.data;

import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class ModifiedMethodCallGraph {


    public SootMethod entryMethod;
    public Set<SootMethod> addedNodes;     // Nodes in target but not in base
    public Set<String> addedNodesStr;     // Nodes in target but not in base
    public Set<SootMethod> removedNodes;   // Nodes in base but not in target
    public Set<String> removedNodesStr;   // Nodes in base but not in target

    public Set<Edge> addedEdges;           // Edges in target but not in base
    public Set<SimpleEdge> addedEdgesStr;  // Edges in target but not in base
    public Set<Edge> removedEdges;         // Edges in base but not in target

    public Set<SimpleEdge> removedEdgesStr;         // Edges in base but not in target

    public Set<SimpleEdge> getRemovedEdgesStr() {
        return removedEdgesStr;
    }

    public void setRemovedEdgesStr(Set<SimpleEdge> removedEdgesStr) {
        this.removedEdgesStr = removedEdgesStr;
    }

    public Set<SootMethod> sdkNodes;       // SDK nodes for reference

    public Set<String> sdkNodesStr;       // SDK nodes for reference

    public ModifiedMethodCallGraph(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
        this.addedNodes = new HashSet<>();
        this.removedNodes = new HashSet<>();
        this.addedEdges = new HashSet<>();
        this.removedEdges = new HashSet<>();
        this.sdkNodes = new HashSet<>();
        this.addedNodesStr = new HashSet<>();
        this.removedNodesStr = new HashSet<>();
        this.addedEdgesStr = new HashSet<>();
        this.sdkNodesStr = new HashSet<>();
    }

    public void setEntryMethod(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
    }

    public void setAddedNodes(Set<SootMethod> addedNodes) {
        this.addedNodes = addedNodes;
    }

    public Set<String> getAddedNodesStr() {
        return addedNodesStr;
    }

    public void setAddedNodesStr(Set<String> addedNodesStr) {
        this.addedNodesStr = addedNodesStr;
    }

    public void setRemovedNodes(Set<SootMethod> removedNodes) {
        this.removedNodes = removedNodes;
    }

    public Set<String> getRemovedNodesStr() {
        return removedNodesStr;
    }

    public void setRemovedNodesStr(Set<String> removedNodesStr) {
        this.removedNodesStr = removedNodesStr;
    }

    public void setAddedEdges(Set<Edge> addedEdges) {
        this.addedEdges = addedEdges;
    }

    public Set<SimpleEdge> getAddedEdgesStr() {
        return addedEdgesStr;
    }

    public void setAddedEdgesStr(Set<SimpleEdge> addedEdgesStr) {
        this.addedEdgesStr = addedEdgesStr;
    }

    public void setRemovedEdges(Set<Edge> removedEdges) {
        this.removedEdges = removedEdges;
    }

    public Set<SootMethod> getSdkNodes() {
        return sdkNodes;
    }

    public void setSdkNodes(Set<SootMethod> sdkNodes) {
        this.sdkNodes = sdkNodes;
    }

    public Set<String> getSdkNodesStr() {
        return sdkNodesStr;
    }

    public void setSdkNodesStr(Set<String> sdkNodesStr) {
        this.sdkNodesStr = sdkNodesStr;
    }

    public Set<SootMethod> getAddedNodes() {
        return addedNodes;
    }

    public Set<SootMethod> getRemovedNodes() {
        return removedNodes;
    }

    public Set<Edge> getAddedEdges() {
        return addedEdges;
    }

    public Set<Edge> getRemovedEdges() {
        return removedEdges;
    }

    public Set<SootMethod> getSDKNodes() {
        return sdkNodes;
    }

    public SootMethod getEntryMethod() {
        return entryMethod;
    }

    public void addAddedNodes(Set<SootMethod> nodes) {
        this.addedNodes.addAll(nodes);
    }

    public void addAddedNodesStr(Set<String> nodes) {
        this.addedNodesStr.addAll(nodes);
    }
    
    public void addRemovedNodes(Set<SootMethod> nodes) {
        this.removedNodes.addAll(nodes);
    }

    public void addRemovedNodesStr(Set<String> nodes) {
        this.removedNodesStr.addAll(nodes);
    }
    
    public void addAddedEdges(Set<Edge> edges) {
        this.addedEdges.addAll(edges);
    }

    public void addAddedEdgesStr(Set<SimpleEdge> edges) {
        this.addedEdgesStr.addAll(edges);
    }
    
    public void addRemovedEdges(Set<Edge> edges) {
        this.removedEdges.addAll(edges);
    }

    public void addRemovedEdgesStr(Set<SimpleEdge> edges) {
        this.removedEdgesStr.addAll(edges);
    }
    
    public void addSDKNodes(Set<SootMethod> nodes) {
        this.sdkNodes.addAll(nodes);
    }

    public void addSDKNodesStr(Set<String> nodes) {
        this.sdkNodesStr.addAll(nodes);
    }
    
    public boolean isEmpty() {
        return addedNodesStr.isEmpty() && removedNodesStr.isEmpty() && addedEdgesStr.isEmpty() && removedEdgesStr.isEmpty();
    }



    
}
