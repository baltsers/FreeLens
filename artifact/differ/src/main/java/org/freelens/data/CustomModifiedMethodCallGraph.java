package org.freelens.data;

import java.util.HashSet;
import java.util.Set;


public class CustomModifiedMethodCallGraph {
    public String entryMethod;
    public Set<String> commonNodesStr;
    public Set<String> addedNodesStr;     
    public Set<String> removedNodesStr;   
    public Set<SimpleEdge> addedEdgesStr;  
    public Set<SimpleEdge> removedEdgesStr;
    public Set<SimpleEdge> commEdgesStr;  
    public Set<String> sdkNodesStr;
    public Set<String> addedSdkNodesStr;
    public Set<String> removedSdkNodesStr;

    public CustomModifiedMethodCallGraph(String entryMethod) {
        this.entryMethod = entryMethod;
        this.addedNodesStr = new HashSet<>();
        this.removedNodesStr = new HashSet<>();
        this.addedEdgesStr = new HashSet<>();
        this.removedEdgesStr = new HashSet<>();
        this.sdkNodesStr = new HashSet<>();
        this.commonNodesStr = new HashSet<>();
        this.commEdgesStr = new HashSet<>();
        this.addedSdkNodesStr = new HashSet<>();
        this.removedSdkNodesStr = new HashSet<>();
    }
    
}
