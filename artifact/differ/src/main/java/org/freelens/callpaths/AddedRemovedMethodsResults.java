package org.freelens.callpaths;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

public class AddedRemovedMethodsResults {
    @JsonProperty
    private Set<String> addedMethods;

    @JsonProperty
    private Set<String> removedMethods;

    public AddedRemovedMethodsResults() {
        this.addedMethods = new HashSet<>();
        this.removedMethods = new HashSet<>();
    }

    public AddedRemovedMethodsResults(Set<String> addedMethods, Set<String> removedMethods) {
        this.addedMethods = addedMethods;
        this.removedMethods = removedMethods;
    }


    public  void addAddedMethods(Set<String> addedMethods) {
        this.addedMethods.addAll(addedMethods);
    }

    public void addRemovedMethods(Set<String> removedMethods) {
        this.removedMethods.addAll(removedMethods);
    }
}
