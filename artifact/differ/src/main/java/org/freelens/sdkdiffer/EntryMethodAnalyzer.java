package org.freelens.sdkdiffer;

import org.freelens.data.SerializableCallGraph;
import org.freelens.data.SerializableCallGraph.CustomEdge;
import org.freelens.util.SootMethodUtil;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.*;
import java.util.stream.Collectors;

public class EntryMethodAnalyzer {
    public static Set<SootMethod> getEntryMethodsInClass(Set<SootMethod> userDefinedMethods, CallGraph callGraph) {
        Set<SootMethod> entryPoints = new HashSet<>();
        Set<String> methodsStr = userDefinedMethods.stream().<String>map(SootMethod::getSignature).collect(Collectors.toSet());

        for (Edge edge : callGraph) {
            SootMethod srcMethod = edge.src();
            SootMethod tgtMethod = edge.tgt();
            if (srcMethod == null || tgtMethod == null) {
                continue;
            }
            if (methodsStr.contains(srcMethod.getSignature()) || methodsStr.contains(tgtMethod.getSignature())) {
                if (srcMethod.getDeclaringClass().getName().contains("dummyMainClass")) {
                    entryPoints.add(tgtMethod);
                }
            }
        }
       return entryPoints;
    }


    public static Set<String> getEntryMethodsInClass(Set<SootMethod> userDefinedMethods, SerializableCallGraph callGraph) {
        Set<String> entryPoints = new HashSet<>();
        Set<String> methodsStr = userDefinedMethods.stream().<String>map(SootMethod::getSignature).collect(Collectors.toSet());

        for (CustomEdge edge : callGraph.getAllEdges()) {
            String srcMethod = edge.getSrcMethod();
            String tgtMethod = edge.getTgtMethod();
            if (srcMethod == null || tgtMethod == null) {
                continue;
            }
            if (methodsStr.contains(srcMethod) || methodsStr.contains(tgtMethod)) {
                if (srcMethod.contains("dummyMainClass")) {
                    entryPoints.add(tgtMethod);
                }
            }
        }
        return entryPoints;
    }


}
