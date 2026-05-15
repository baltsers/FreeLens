package org.freelens.data;

import java.util.Set;

public class ChangedMethodsGraphs {
    private Set<MethodCallGraph> addedMethodsCallGraphs;
    private Set<MethodCallGraph> removedMethodsCallGraphs;
    private Set<ModifiedMethodCallGraph> modifiedMethodsCallGraphs;

    public ChangedMethodsGraphs(Set<MethodCallGraph> addedMethodsCallGraphs,
                                 Set<MethodCallGraph> removedMethodsCallGraphs,
                                 Set<ModifiedMethodCallGraph> modifiedMethodsCallGraphs){
        this.addedMethodsCallGraphs = addedMethodsCallGraphs;
        this.removedMethodsCallGraphs = removedMethodsCallGraphs;
        this.modifiedMethodsCallGraphs = modifiedMethodsCallGraphs;
    }

    public Set<MethodCallGraph> getAddedMethodsCallGraphs() {
        return this.addedMethodsCallGraphs;
    }

    public Set<MethodCallGraph> getRemovedMethodsCallGraphs() {
        return this.removedMethodsCallGraphs;
    }

    public Set<ModifiedMethodCallGraph> getModifiedMethodsCallGraphs() {
        return this.modifiedMethodsCallGraphs;
    }

    public boolean isEmpty() {
        return this.addedMethodsCallGraphs.isEmpty()
                && this.removedMethodsCallGraphs.isEmpty()
                && this.modifiedMethodsCallGraphs.isEmpty();
    }

    
    
}
