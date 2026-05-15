package org.freelens.sdkdiffer;

import heros.solver.Pair;
import org.freelens.callpaths.*;
import org.freelens.data.CallGraphPair;
import org.freelens.data.ChangedMethods;
import org.freelens.data.ChangedMethodsGraphs;
import org.freelens.data.CustomCallGraphPair;
import org.freelens.data.CustomChangedMethodsGraphs;
import org.freelens.data.CustomMethodCallGraph;
import org.freelens.data.MethodCallGraph;
import org.freelens.data.SDKMethods;
import org.freelens.data.SerializableCallGraph;
import org.freelens.data.SerializableCallGraph.CustomEdge;
import org.freelens.util.*;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.util.HashMultiMap;
import soot.util.MultiMap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class SDKDiffer {
    private static String refAppPath;
    private static String tgtAppPath;
    private static String outputDir;


    private static MultiMap<String, String> getEntryMethodsInAllClasses(Set<String> actClsStrs, SerializableCallGraph callGraph) {
        MultiMap<String, String> methodsInAllClasses = new HashMultiMap<>();
        for (String actClsStr : actClsStrs) {
            methodsInAllClasses.putAll(actClsStr, SootMethodUtil.getEntryPointsInClass(actClsStr, callGraph));
        }
        return methodsInAllClasses;
    }


    private static MultiMap<String, CustomMethodCallGraph> getMethodCallCustomGraphsInAllClasses(MultiMap<String, String> actClsStrs, SerializableCallGraph callGraph) {
        MultiMap<String, CustomMethodCallGraph> methodCallGraphsInAllClasses = new HashMultiMap<>();
        for (String actClsStr : actClsStrs.keySet()) {
            Set<String> methods = actClsStrs.get(actClsStr);
            for (String method : methods) {
                CustomMethodCallGraph methodCallGraph = CallGraphAnalyzer.getSDKCallCustomSubgraph(method, callGraph);
                methodCallGraphsInAllClasses.put(actClsStr, methodCallGraph);
            }
        }
        return methodCallGraphsInAllClasses;
    }

    private static void runSDKCallGraph(MultiMap<String, String> refMethodsInSameActCls, MultiMap<String, String> tgtMethodsInSameActCls,
                                        MultiMap<String, String> msInDeletedActs,
                                        MultiMap<String, String> msInAddedActs,
                                        Set<String> sameActClsStr, Set<String> addedActClsStr, Set<String> removedActClsStr) throws XmlPullParserException, IOException {
        //System.out.println("Analyzing call paths in modified activities...");

        Map<String, CustomChangedMethodsGraphs> cmsInModifiedActs = SootMethodUtil.getDiffMethodsCustomGraphsInSameCls(sameActClsStr, refMethodsInSameActCls, tgtMethodsInSameActCls);

        System.out.println("Added activities: " + addedActClsStr.size());
        System.out.println("Removed activities: " + removedActClsStr.size());
        System.out.println("Modified activities: " + cmsInModifiedActs.size());

        //System.out.println("Get sub-graphs for methods in added activities...");
        MultiMap<String, CustomMethodCallGraph> methodCallGraphMapInAddedActs = getMethodCallCustomGraphsInAllClasses(msInAddedActs, CustomCallGraphPair.tgtCallGraph);
        //System.out.println("Get sub-graphs for methods in removed activities...");
        MultiMap<String, CustomMethodCallGraph> methodCallGraphMapInRemovedActs = getMethodCallCustomGraphsInAllClasses(msInDeletedActs, CustomCallGraphPair.refCallGraph);

        outputDir = outputDir + AndroidUtil.getPackageName(refAppPath);
        CallGraphJsonConverter.convertAndSaveAllGraphs(methodCallGraphMapInAddedActs, methodCallGraphMapInRemovedActs, cmsInModifiedActs, outputDir);

    }

    public static void run(String refAppPath, 
     String tgtAppPath, 
     String refCustomCallGraphPath,
     String tgtCustomCallGraphPath,
     boolean runCallPathAnalysis,
     String outputDir) throws XmlPullParserException, IOException {
        assert AndroidUtil.getPackageName(refAppPath).equals(AndroidUtil.getPackageName(tgtAppPath));
        SDKDiffer.refAppPath = refAppPath;
        SDKDiffer.tgtAppPath = tgtAppPath;
        SDKDiffer.outputDir = outputDir;
        //System.out.println("Loading custom call graphs...");
        SerializableCallGraph refCustomCallGraph;
        SerializableCallGraph tgtCustomCallGraph;
        try {
            refCustomCallGraph = SerializableCallGraph.deserialize(refCustomCallGraphPath);
            tgtCustomCallGraph = SerializableCallGraph.deserialize(tgtCustomCallGraphPath);
        } catch (Exception e) {
            return;
        }
        SDKMethods.loadSDKMethods("android_sdk_packages_sorted.list");

        Set<String> tgtAppClsNames = SootClassUtil.getAndroidActivityAndFragmentClasses(tgtAppPath);
        Set<String> refAppClsNames = SootClassUtil.getAndroidActivityAndFragmentClasses(refAppPath);

        Set<String> sameActClsStr = new HashSet<>(tgtAppClsNames); // Activity classes whose name are unchanged
        Set<String> addedActClsStr = new HashSet<>(tgtAppClsNames); // new activity classes
        Set<String> removedActClsStr = new HashSet<>(refAppClsNames); // removed activity classes
        sameActClsStr.retainAll(refAppClsNames);
        addedActClsStr.removeAll(sameActClsStr);
        removedActClsStr.removeAll(sameActClsStr);

        CustomCallGraphPair.tgtCallGraph = tgtCustomCallGraph;
        CustomCallGraphPair.refCallGraph = refCustomCallGraph;
        MultiMap<String, String> refMethodsInSameActCls = getEntryMethodsInAllClasses(sameActClsStr, CustomCallGraphPair.refCallGraph);
        MultiMap<String, String> msInDeletedActs = getEntryMethodsInAllClasses(removedActClsStr, CustomCallGraphPair.refCallGraph);

        MultiMap<String, String> tgtMethodsInSameActCls = getEntryMethodsInAllClasses(sameActClsStr, CustomCallGraphPair.tgtCallGraph);
        MultiMap<String, String> msInAddedActs = getEntryMethodsInAllClasses(addedActClsStr, CustomCallGraphPair.tgtCallGraph);

        if (runCallPathAnalysis) {
            runCallPath(refMethodsInSameActCls, tgtMethodsInSameActCls, msInDeletedActs, msInAddedActs, sameActClsStr, addedActClsStr, removedActClsStr);
        } else {
            runSDKCallGraph(refMethodsInSameActCls, tgtMethodsInSameActCls, msInDeletedActs, msInAddedActs, sameActClsStr, addedActClsStr, removedActClsStr);
    }
}

    private static void runCallPath(MultiMap<String, String> refMethodsInSameActCls, MultiMap<String, String> tgtMethodsInSameActCls,
                                    MultiMap<String, String> msInDeletedActs,
                                    MultiMap<String, String> msInAddedActs,
                                    Set<String> sameActClsStr, Set<String> addedActClsStr, Set<String> removedActClsStr) throws XmlPullParserException, IOException {
        //System.out.println("Analyzing call paths in modified activities...");

        Map<String, ModifiedClassCallPaths> cmsInModifiedActs = SootMethodUtil.getDiffMethodsCallPathsInSameCls(sameActClsStr, refMethodsInSameActCls, tgtMethodsInSameActCls);

        System.out.println("Added activities: " + addedActClsStr.size());
        System.out.println("Removed activities: " + removedActClsStr.size());
        System.out.println("Modified activities: " + cmsInModifiedActs.size());

//        Map<String, List<SymbolizedCallPath>> addedClsAndCallPaths = new HashMap<>();
//        for (String actClsStr : addedActClsStr) {
//            List<SymbolizedCallPath> callPaths = PathFinder.findPathsForAllEntries(actClsStr, CustomCallGraphPair.tgtCallGraph);
//            addedClsAndCallPaths.put(actClsStr, callPaths);
//        }
//        Map<String, List<SymbolizedCallPath>> removedClsAndCallPaths = new HashMap<>();
//        for (String actClsStr : removedActClsStr) {
//            List<SymbolizedCallPath> callPaths = PathFinder.findPathsForAllEntries(actClsStr, CustomCallGraphPair.refCallGraph);
//            removedClsAndCallPaths.put(actClsStr, callPaths);
//        }
//        Map<String, Integer> addedClsAndNumOfPaths = new HashMap<>();
//        for (String actClsStr : addedClsAndCallPaths.keySet()) {
//            addedClsAndNumOfPaths.put(actClsStr, addedClsAndCallPaths.get(actClsStr).size());
//        }
//        Map<String, Integer> removedClsAndNumOfPaths = new HashMap<>();
//        for (String actClsStr : removedClsAndCallPaths.keySet()) {
//            removedClsAndNumOfPaths.put(actClsStr, removedClsAndCallPaths.get(actClsStr).size());
//        }

//        int totalAdded = addedClsAndCallPaths.size();
//        int totalRemoved = removedClsAndCallPaths.size();
//        int matched = 0;

        // Find number matched classes
//        for (String actClsStr : addedClsAndNumOfPaths.keySet()) {
//            int addedNumOfPaths = addedClsAndNumOfPaths.get(actClsStr);
//            //System.out.println(actClsStr + " has " + addedNumOfPaths + " paths; ");
//            for (String actClsStr2 : removedClsAndNumOfPaths.keySet()) {
//                int removedNumOfPaths = removedClsAndNumOfPaths.get(actClsStr2);
//                //System.out.println("\t- " + actClsStr2 + " has " + removedNumOfPaths + " paths");
//                if (addedNumOfPaths == removedNumOfPaths && addedNumOfPaths > 0) {
//                    List<SymbolizedCallPath> paths1 = addedClsAndCallPaths.get(actClsStr);
//                    List<SymbolizedCallPath> paths2 = removedClsAndCallPaths.get(actClsStr2);
//                    System.out.println("Found potential matched classes: " + actClsStr + " and " + actClsStr2);
//                    if (PathComparator.comparePaths(paths1, paths2)) {
//                        System.out.println("- Matched classes: " + actClsStr + " and " + actClsStr2);
//                        matched++;
//                        System.out.printf(
//                                "Matched classes (%d/%d): %s and %s%n",
//                                matched,
//                                Math.min(totalAdded, totalRemoved),
//                                actClsStr,
//                                actClsStr2
//                        );
//                    }
//                }
//            }
//        }

        //System.out.println("Get sub-graphs for methods in added activities...");
        //MultiMap<String, MethodCallPaths> methodCallGraphMapInAddedActs = getMethodCallGraphsInAllClasses(msInAddedActs, CallGraphPair.tgtCallGraph);
        //System.out.println("Get sub-graphs for methods in removed activities...");
        //MultiMap<String, MethodCallPaths> methodCallGraphMapInRemovedActs = getMethodCallGraphsInAllClasses(msInDeletedActs, CallGraphPair.refCallGraph);

        ChangeResults results = new ChangeResults();
        for (String modifiedActCls : cmsInModifiedActs.keySet()) {
            ModifiedClassCallPaths modifiedClassCallPaths = cmsInModifiedActs.get(modifiedActCls);
            results.addModifiedClass(modifiedActCls, modifiedClassCallPaths);
        }

        AddedRemovedMethodsResults addedRemovedMethodsResults = new AddedRemovedMethodsResults();
        for (String addedActCls : msInAddedActs.keySet()) {
            Set<String> addedMethods = msInAddedActs.get(addedActCls);
            addedRemovedMethodsResults.addAddedMethods(addedMethods);
        }

        for (String removedActCls : msInDeletedActs.keySet()) {
            Set<String> removedMethods = msInDeletedActs.get(removedActCls);
            addedRemovedMethodsResults.addRemovedMethods(removedMethods);
        }

        String addedRemovedMethodsOutputFilePath = outputDir + File.separator + AndroidUtil.getPackageName(refAppPath) + "_added_removed_methods.json";
        String outputFilePath = outputDir + File.separator + AndroidUtil.getPackageName(refAppPath) + "_modified_callpaths.json";
        JsonUtils.saveToJson(results, outputFilePath);
        JsonUtils.saveToJson(addedRemovedMethodsResults, addedRemovedMethodsOutputFilePath);



    }
}
