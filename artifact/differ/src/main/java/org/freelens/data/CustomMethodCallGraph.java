package org.freelens.data;

import java.util.HashSet;
import java.util.Set;

public class CustomMethodCallGraph {
    public String entryMethod;
    public Set<String> nodes;
    public Set<SimpleEdge> simpleEdges;
    public Set<String> sdkNodes;

    public CustomMethodCallGraph(String entryMethod) {
        this.entryMethod = entryMethod;
        this.nodes = new HashSet<>();
        this.simpleEdges = new HashSet<>();
        this.sdkNodes = new HashSet<>();
    }

    
}
