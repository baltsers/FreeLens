package org.freelens.data;

import heros.solver.Pair;
import soot.SootMethod;

import java.util.Set;

public class ChangedMethods {
    private Set<SootMethod> addedMethods;
    private Set<SootMethod> modifiedMethodsRef;
    private Set<SootMethod> modifiedMethodsTgt;
    private Set<SootMethod> removedMethods;

    private Set<Pair<SootMethod, SootMethod>> modifiedMethodPairs;

    public ChangedMethods(Set<SootMethod> addedMethods,
                          Set<SootMethod> modifiedMethodsRef,
                          Set<SootMethod> modifiedMethodsTgt,
                          Set<SootMethod> removedMethods){
        this.addedMethods = addedMethods;
        this.removedMethods = removedMethods;
        this.modifiedMethodsRef = modifiedMethodsRef;
        this.modifiedMethodsTgt = modifiedMethodsTgt;
    }

    public int size() {
        return this.addedMethods.size() + this.modifiedMethodsRef.size() + this.removedMethods.size();
    }

    public Set<SootMethod> getAddedMethods() {
        return this.addedMethods;
    }

    public Set<SootMethod> getModifiedMethodsRef() {
        return this.modifiedMethodsRef;
    }

    public Set<SootMethod> getModifiedMethodsTgt() {
        return this.modifiedMethodsTgt;
    }

    public Set<SootMethod> getRemovedMethods() {
        return this.removedMethods;
    }

    public Set<Pair<SootMethod, SootMethod>> getModifiedMethodPairs() {
        return this.modifiedMethodPairs;
    }

    public void setModifiedMethodPairs(Set<Pair<SootMethod, SootMethod>> modifiedMethodPairs) {
        this.modifiedMethodPairs = modifiedMethodPairs;
    }

    public boolean isEmpty() {
        return this.addedMethods.isEmpty()
                && this.modifiedMethodsRef.isEmpty()
                && this.removedMethods.isEmpty();
    }
}
