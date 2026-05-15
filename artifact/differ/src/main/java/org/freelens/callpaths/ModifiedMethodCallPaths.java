package org.freelens.callpaths;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ModifiedMethodCallPaths {
    @Getter
    private String entryMethod;

    @Setter
    @Getter
    private ChangeReport changeReport;


    public ModifiedMethodCallPaths(String entryMethod, ChangeReport changeReport) {
        this.entryMethod = entryMethod;
        this.changeReport = changeReport;

    }

    public ModifiedMethodCallPaths(String entryMethod) {
        this.entryMethod = entryMethod;
    }




}
