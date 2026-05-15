package org.freelens.callpaths;

import lombok.Getter;

import java.util.Set;

public class ModifiedClassCallPaths {

    @Getter
    private String className;
    @Getter
    private Set<ModifiedMethodCallPaths> modifiedMethodCallPaths;
//    @Getter
//    private Set<MethodCallPaths> addedMethodCallPaths;
//    @Getter
//    private Set<MethodCallPaths> removedMethodCallPaths;

    public ModifiedClassCallPaths(String className, Set<MethodCallPaths> addedMethodCallPaths,
                                  Set<MethodCallPaths> removedMethodCallPaths,
                                  Set<ModifiedMethodCallPaths> modifiedMethodCallPaths) {
        this.className = className;
//        this.addedMethodCallPaths = addedMethodCallPaths;
//        this.removedMethodCallPaths = removedMethodCallPaths;
        this.modifiedMethodCallPaths = modifiedMethodCallPaths;
    }

    public boolean isModified() {
//        return !addedMethodCallPaths.isEmpty() || !removedMethodCallPaths.isEmpty() || !modifiedMethodCallPaths.isEmpty();
        return !modifiedMethodCallPaths.isEmpty();
    }



}
