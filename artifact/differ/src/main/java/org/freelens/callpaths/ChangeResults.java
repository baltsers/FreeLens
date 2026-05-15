package org.freelens.callpaths;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.HashMap;
import java.util.Map;

@JsonRootName(value = "modified_activities")
public class ChangeResults {
    @JsonProperty
    private Map<String, ModifiedClassCallPaths> modifiedActivities;

    public ChangeResults() {
        this.modifiedActivities = new HashMap<>();
    }

    public void addModifiedClass(String className, ModifiedClassCallPaths modifiedClass) {
        modifiedActivities.put(className, modifiedClass);
    }

}
