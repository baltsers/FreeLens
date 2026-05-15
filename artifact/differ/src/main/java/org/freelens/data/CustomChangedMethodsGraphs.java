package org.freelens.data;

import java.util.Set;

public class CustomChangedMethodsGraphs {
    public Set<CustomMethodCallGraph> addedMethodsCallGraphs;
    public Set<CustomMethodCallGraph> removedMethodsCallGraphs;
    public Set<CustomModifiedMethodCallGraph> modifiedMethodsCallGraphs;

    public CustomChangedMethodsGraphs(Set<CustomMethodCallGraph> addedMethodsCallGraphs,
                                      Set<CustomMethodCallGraph> removedMethodsCallGraphs,
                                      Set<CustomModifiedMethodCallGraph> modifiedMethodsCallGraphs){
        this.addedMethodsCallGraphs = addedMethodsCallGraphs;
        this.removedMethodsCallGraphs = removedMethodsCallGraphs;
        this.modifiedMethodsCallGraphs = modifiedMethodsCallGraphs;
    }

    public boolean isEmpty() {
        return this.addedMethodsCallGraphs.isEmpty()
                && this.removedMethodsCallGraphs.isEmpty()
                && this.modifiedMethodsCallGraphs.isEmpty();
    }
}
