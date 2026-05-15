package org.freelens.data;

import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;

public class MethodCallGraph {
    private SootMethod entryMethod;
    private Set<SootMethod> nodes;

    private Set<String> nodesStr;

    private Set<Edge> edges;

    private Set<SimpleEdge> simpleEdges;

    private Set<SootMethod> sdkNodes;

    private Set<String> sdkNodesStr;


    public void setEntryMethod(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
    }

    public void setNodes(Set<SootMethod> nodes) {
        this.nodes = nodes;
    }

    public Set<String> getNodesStr() {
        return nodesStr;
    }

    public void setNodesStr(Set<String> nodesStr) {
        this.nodesStr = nodesStr;
    }

    public void setEdges(Set<Edge> edges) {
        this.edges = edges;
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

    public MethodCallGraph(SootMethod entryMethod) {
        this.entryMethod = entryMethod;
        this.nodes = new HashSet<>();
        this.edges = new HashSet<>();
        this.sdkNodes = new HashSet<>();
        this.sdkNodesStr = new HashSet<>();
        this.nodesStr = new HashSet<>();
        this.simpleEdges = new HashSet<>();
    }

    public void addSimpleEdge(SimpleEdge simpleEdge) {
        simpleEdges.add(simpleEdge);
    }

    public void addNode(SootMethod node) {
        nodes.add(node);
    }

    public void addSDKNode(SootMethod sdkNode) {
        sdkNodes.add(sdkNode);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }
    
    public SootMethod getEntryMethod() {
        return entryMethod;
    }

    public Set<SootMethod> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Set<SootMethod> getSDKNodes() {
        return sdkNodes;
    }

    public void addSDKNodeStr(String sdkNodeStr) {
        sdkNodesStr.add(sdkNodeStr);
    }

    public void addNodeStr(String nodeStr) {
        nodesStr.add(nodeStr);
    }

    public Set<SimpleEdge> getSimpleEdges() {
        return simpleEdges;
    }



}
