package org.freelens.util;

import heros.solver.Pair;

import org.freelens.callpaths.*;
import org.freelens.data.CallGraphPair;
import org.freelens.data.ChangedMethods;
import org.freelens.data.ChangedMethodsGraphs;
import org.freelens.data.CustomCallGraphPair;
import org.freelens.data.CustomChangedMethodsGraphs;
import org.freelens.data.CustomMethodCallGraph;
import org.freelens.data.CustomModifiedMethodCallGraph;
import org.freelens.data.MethodCallGraph;
import org.freelens.data.ModifiedMethodCallGraph;
import org.freelens.data.SerializableCallGraph;
import org.freelens.sdkdiffer.CallGraphAnalyzer;
import org.freelens.sdkdiffer.EntryMethodAnalyzer;
import org.freelens.sdkdiffer.GraphDiffer;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.MultiMap;

import java.util.*;
import java.util.stream.Collectors;

public class SootMethodUtil {


    public static Set<String> getEntryPointsInClass(String clsName, SerializableCallGraph callGraph) {
        Set<SootMethod> userDefinedMethods = getUserDefinedMethodsInClass(clsName);
        Set<String> entryPoints = EntryMethodAnalyzer.getEntryMethodsInClass(userDefinedMethods, callGraph);
        return entryPoints;
    }

    public static Set<SootMethod> getUserDefinedMethodsInClass(String clsName) {
        SootClass cls = Scene.v().getSootClass(clsName);
        Set<SootMethod> userDefinedMethods = new HashSet<>();
        for (SootMethod method : cls.getMethods()) {
            if (!method.isConstructor() && !method.isStaticInitializer() && method.isConcrete()) {
                userDefinedMethods.add(method);
            }
        }
        return userDefinedMethods;
    }


    public static Map<String, CustomChangedMethodsGraphs> getDiffMethodsCustomGraphsInSameCls(Set<String> sameClasses,
                                                                            MultiMap<String, String> refMethodsInSameCls,
                                                                            MultiMap<String, String> tgtMethodsInSameCls) {
        Map<String, CustomChangedMethodsGraphs> cmsInModifiedActs = new HashMap<>();
        for (String clsName : sameClasses) {
            CustomChangedMethodsGraphs cmIn1Cls = SootMethodUtil.getChangedMethodsCustomGraphs(
                    refMethodsInSameCls.get(clsName),
                    tgtMethodsInSameCls.get(clsName)
            );
            if (!cmIn1Cls.isEmpty()) {
                cmsInModifiedActs.put(clsName, cmIn1Cls);
            }
        }
        return cmsInModifiedActs;
    }

    public static CustomChangedMethodsGraphs getChangedMethodsCustomGraphs(Set<String> refMethods, Set<String> tgtMethods) {
        Set<CustomMethodCallGraph> addedMethodsGraphs = new HashSet<>();
        Set<CustomMethodCallGraph> removedMethodsGraphs = new HashSet<>();
        Set<CustomModifiedMethodCallGraph> modifiedMethodsGraphs = new HashSet<>();
        // find deleted and modified
        for (String methodInRef : refMethods) {
            if (!tgtMethods.contains(methodInRef)) {
                CustomMethodCallGraph methodCallGraph = CallGraphAnalyzer.getSDKCallCustomSubgraph(methodInRef, CustomCallGraphPair.refCallGraph);
                removedMethodsGraphs.add(methodCallGraph);
            } else {
                System.out.println("Comparing: " + methodInRef + " and " + methodInRef);
                CustomModifiedMethodCallGraph modifiedMethodCallGraph = twoMethodsCallCustomGraphsEqual(methodInRef, methodInRef);
                
                if (modifiedMethodCallGraph != null) {
                    modifiedMethodsGraphs.add(modifiedMethodCallGraph);
                }
            }
        }
        // find added
        for (String methodInVj : tgtMethods) {
            if (!refMethods.contains(methodInVj)) {
                addedMethodsGraphs.add(CallGraphAnalyzer.getSDKCallCustomSubgraph(methodInVj, CustomCallGraphPair.tgtCallGraph));
            }
        }
        return new CustomChangedMethodsGraphs(addedMethodsGraphs, removedMethodsGraphs, modifiedMethodsGraphs);
    }

    private static CustomModifiedMethodCallGraph twoMethodsCallCustomGraphsEqual(String refMethod, String tgtMethod) {
        System.out.println("Comparing: " + refMethod + " and " + tgtMethod);
        CustomMethodCallGraph refMethodCallGraph = CallGraphAnalyzer.getSDKCallCustomSubgraph(refMethod, CustomCallGraphPair.refCallGraph);
        CustomMethodCallGraph tgtMethodCallGraph = CallGraphAnalyzer.getSDKCallCustomSubgraph(tgtMethod, CustomCallGraphPair.tgtCallGraph);
        return GraphDiffer.compareCustomGraphs(refMethodCallGraph, tgtMethodCallGraph);
    }


    public static Map<String, ModifiedClassCallPaths> getDiffMethodsCallPathsInSameCls(Set<String> sameActClsStr, MultiMap<String, String> refMethodsInSameActCls, MultiMap<String, String> tgtMethodsInSameActCls) {
        Map<String, ModifiedClassCallPaths> modifiedClassCallPaths = new HashMap<>();
        for (String clsName : sameActClsStr) {
            ModifiedClassCallPaths modifiedClassCallPath = SootMethodUtil.getModifiedClassCallPaths(
                    refMethodsInSameActCls.get(clsName),
                    tgtMethodsInSameActCls.get(clsName),
                    clsName
            );
            if (modifiedClassCallPath.isModified()) {
                modifiedClassCallPaths.put(clsName, modifiedClassCallPath);
            }
        }
        return modifiedClassCallPaths;

    }

    private static ModifiedClassCallPaths getModifiedClassCallPaths(Set<String> refClassMethods, Set<String> tgtClassMethods, String clsName) {
        Set<MethodCallPaths> addedMethodsPaths = new HashSet<>();
        Set<MethodCallPaths> removedMethodsPaths = new HashSet<>();
        Set<ModifiedMethodCallPaths> modifiedMethodsPaths = new HashSet<>();
        // find deleted and modified
        for (String methodInRef : refClassMethods) {
            if (!tgtClassMethods.contains(methodInRef)) {
                List<CallPath> refMethodCallPaths = PathFinder.findPathsForOneEntry(methodInRef, CustomCallGraphPair.refCallGraph);
                removedMethodsPaths.add(new MethodCallPaths(methodInRef, refMethodCallPaths));
            } else {
                //System.out.println("Comparing: " + methodInRef + " and " + methodInRef);
                ModifiedMethodCallPaths modifiedMethodCallPaths = twoMethodsCallPathsEqual(methodInRef, methodInRef);
                if (modifiedMethodCallPaths != null) {
                    modifiedMethodsPaths.add(modifiedMethodCallPaths);
                }
            }
        }
        // find added
        for (String methodInVj : tgtClassMethods) {
            if (!refClassMethods.contains(methodInVj)) {
                MethodCallPaths methodCallPaths = new MethodCallPaths(methodInVj, PathFinder.findPathsForOneEntry(methodInVj, CustomCallGraphPair.tgtCallGraph));
                addedMethodsPaths.add(methodCallPaths);
            }
        }
        return new ModifiedClassCallPaths(clsName, addedMethodsPaths, removedMethodsPaths, modifiedMethodsPaths);
    }

    private static ModifiedMethodCallPaths twoMethodsCallPathsEqual(String refMethod, String tgtMethod) {
        //System.out.println("Comparing: " + refMethod + " and " + tgtMethod);
        List<CallPath> refMethodCallPaths = PathFinder.findPathsForOneEntry(refMethod, CustomCallGraphPair.refCallGraph);
        List<CallPath> tgtMethodCallPaths = PathFinder.findPathsForOneEntry(tgtMethod, CustomCallGraphPair.tgtCallGraph);
        ChangeReport changeReport = PathAnalyzer.comparePathsOfSameEntry(refMethodCallPaths, tgtMethodCallPaths);
        if (changeReport.hasNoChanges()) {
            return null;
        }
        return new ModifiedMethodCallPaths(refMethod, changeReport);
    }
}