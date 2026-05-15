package org.freelens.sdkdiffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.io.FileWriter;
import java.io.Writer;
import java.util.stream.Collectors;

import org.freelens.data.SimpleEdge;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.Edge;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.freelens.data.CustomMethodCallGraph;
import org.freelens.data.CustomChangedMethodsGraphs;
import org.freelens.data.CustomModifiedMethodCallGraph;
import soot.util.MultiMap;

public class CallGraphJsonConverter {
    
    /**
     * Convert and save all collected call graphs to JSON files
     * 
     */
    public static void convertAndSaveAllGraphs(
            MultiMap<String, CustomMethodCallGraph> methodCallGraphMapInAddedActs,
            MultiMap<String, CustomMethodCallGraph> methodCallGraphMapInRemovedActs,
            Map<String, CustomChangedMethodsGraphs> cmsInModifiedActs,
            String outputDir) throws IOException {
        
        // Debug input sizes
        System.out.println("\nDebug - Input sizes:");
        System.out.println("Added activities: " + methodCallGraphMapInAddedActs.keySet().size());
        System.out.println("Removed activities: " + methodCallGraphMapInRemovedActs.keySet().size());
        System.out.println("Modified activities: " + cmsInModifiedActs.size());

        // Create output directory
        File baseDir = new File(outputDir);
        baseDir.mkdirs();
        
        // Convert and save added activities
        List<Map<String, Object>> addedJson = convertSimpleGraphs(methodCallGraphMapInAddedActs);
        System.out.println("\nDebug - Added activities conversion:");
        System.out.println("Number of converted methods: " + addedJson.size());
        writeJsonToFile(addedJson, new File(baseDir, "added_activities.json"));
        
        // Convert and save removed activities
        List<Map<String, Object>> removedJson = convertSimpleGraphs(methodCallGraphMapInRemovedActs);
        System.out.println("\nDebug - Removed activities conversion:");
        System.out.println("Number of converted methods: " + removedJson.size());
        writeJsonToFile(removedJson, new File(baseDir, "removed_activities.json"));
        
        // Convert and save modified activities
        List<Map<String, Object>> modifiedJson = convertChangedMethodsGraphs(cmsInModifiedActs);
        System.out.println("\nDebug - Modified activities conversion:");
        System.out.println("Number of converted activities: " + modifiedJson.size());
        writeJsonToFile(modifiedJson, new File(baseDir, "modified_activities.json"));


    }
    
    /**
     * Convert simple graphs (added or removed activities)
     */
    private static List<Map<String, Object>> convertSimpleGraphs(MultiMap<String, CustomMethodCallGraph> methodGraphs) {
        List<Map<String, Object>> allActivities = new ArrayList<>();
    
        // Sort activities for consistent output
        List<String> sortedActivities = new ArrayList<>(methodGraphs.keySet());
        Collections.sort(sortedActivities);
        
        for (String activityName : sortedActivities) {
            Map<String, Object> activityChanges = new LinkedHashMap<>();
            activityChanges.put("activity", activityName);
            
            // Convert all methods in this activity
            List<Map<String, Object>> methods = new ArrayList<>();
            for (CustomMethodCallGraph graph : methodGraphs.get(activityName)) {
                methods.add(convertToJsonFormat(graph));
            }
            
            Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("methods", methods);
            
            activityChanges.put("changes", changes);
            allActivities.add(activityChanges);
        }
        
        return allActivities;
    }
    
    /**
     * Convert a single MethodCallGraph to JSON format
     */
    private static Map<String, String> createNodeMapping(Set<String> nodes, String entryMethod) {
        // Create a sorted list of methods (excluding entry method)
        List<String> sortedMethods = new ArrayList<>(nodes);
        sortedMethods.remove(entryMethod);
        Collections.sort(sortedMethods); // Sort by method names

        // Create mapping
        Map<String, String> nodeMapping = new HashMap<>();
        Map<String, String> jsonNodes = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        
        // Always map entry method to "0"
        nodeMapping.put(entryMethod, "0");
        jsonNodes.put("0", entryMethod);
        
        // Map other nodes
        int counter = 1;
        for (String method : sortedMethods) {
            String id = String.valueOf(counter++);
            nodeMapping.put(method, id);
            jsonNodes.put(id, method);
        }
        
        return nodeMapping;
    }

    private static List<String> sortEdges(Collection<SimpleEdge> edges, Map<String, String> nodeMapping) {
        List<String> edgeStrings = new ArrayList<>();
        for (SimpleEdge edge : edges) {
            String srcId = nodeMapping.get(edge.src);
            String tgtId = nodeMapping.get(edge.tgt);
            edgeStrings.add(srcId + " -> " + tgtId);
        }
        Collections.sort(edgeStrings); // Sort by string representation (naturally sorts by srcId)
        return edgeStrings;
    }

    // For simple graphs (added/removed methods)
    private static Map<String, Object> convertToJsonFormat(CustomMethodCallGraph graph) {
        // Create sorted node mapping
        Map<String, String> nodeMapping = createNodeMapping(graph.nodes, graph.entryMethod);
        
        // Convert and sort edges
        List<String> sortedEdges = sortEdges(graph.simpleEdges, nodeMapping);
        
        // Convert SDK nodes to sorted IDs
        List<String> sdkNodeIds = new ArrayList<>();
        for (String node : graph.sdkNodes) {
            sdkNodeIds.add(nodeMapping.get(node));
        }
        Collections.sort(sdkNodeIds);

        // Build graph structure
        Map<String, Object> graphStructure = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
        graphStructure.put("entry_node", "0");
        graphStructure.put("nodes", new LinkedHashMap<>(nodeMapping));
        graphStructure.put("node_types", new LinkedHashMap<String, List<String>>() {{
            List<String> allNodes = new ArrayList<>(nodeMapping.values());
            Collections.sort(allNodes);
            put("all", allNodes);
        }});
        graphStructure.put("edges", new LinkedHashMap<String, List<String>>() {{
            put("all", sortedEdges);
        }});
        graphStructure.put("sdk_nodes", sdkNodeIds);

        // Create final structure
        Map<String, Object> methodMap = new LinkedHashMap<>();
        methodMap.put("signature", graph.entryMethod);
        methodMap.put("graph", graphStructure);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("method", methodMap);
        
        return result;
    }

    // For modified methods
    private static Map<String, Object> convertModifiedToJsonFormat(CustomModifiedMethodCallGraph diff) {
        // Create sorted node mapping
        Set<String> allNodes = new HashSet<>();
        allNodes.addAll(diff.commonNodesStr);
        allNodes.addAll(diff.addedNodesStr);
        allNodes.addAll(diff.removedNodesStr);
        Map<String, String> nodeMapping = createNodeMapping(allNodes, diff.entryMethod);
        
        // Convert and sort edges
        List<String> addedEdges = sortEdges(diff.addedEdgesStr, nodeMapping);
        List<String> removedEdges = sortEdges(diff.removedEdgesStr, nodeMapping);
        List<String> commonEdges = sortEdges(diff.commEdgesStr, nodeMapping);

        // Convert nodes to sorted IDs
        List<String> addedNodeIds = new ArrayList<>();
        List<String> removedNodeIds = new ArrayList<>();
        List<String> commonNodeIds = new ArrayList<>();
        List<String> addedSdkNodeIds = new ArrayList<>();
        List<String> removedSdkNodeIds = new ArrayList<>();
        
        for (String node : diff.addedNodesStr) {
            addedNodeIds.add(nodeMapping.get(node));
        }
        for (String node : diff.removedNodesStr) {
            removedNodeIds.add(nodeMapping.get(node));
        }
        for (String node : diff.commonNodesStr) {
            commonNodeIds.add(nodeMapping.get(node));
        }
        for (String node : diff.addedSdkNodesStr) {
            addedSdkNodeIds.add(nodeMapping.get(node));
        }
        for (String node : diff.removedSdkNodesStr) {
            removedSdkNodeIds.add(nodeMapping.get(node));
        }
        
        // Sort all ID lists
        Collections.sort(addedNodeIds);
        Collections.sort(removedNodeIds);
        Collections.sort(commonNodeIds);
        Collections.sort(addedSdkNodeIds);
        Collections.sort(removedSdkNodeIds);

        // Build graph structure
        Map<String, Object> graphStructure = new LinkedHashMap<>();
        graphStructure.put("entry_node", "0");
        graphStructure.put("nodes", new LinkedHashMap<>(nodeMapping));
        graphStructure.put("node_types", new LinkedHashMap<String, List<String>>() {{
            put("common", commonNodeIds);
            put("added", addedNodeIds);
            put("removed", removedNodeIds);
        }});
        graphStructure.put("sdk_nodes", new LinkedHashMap<String, List<String>>() {{
            put("added", addedSdkNodeIds);
            put("removed", removedSdkNodeIds);
        }});
        graphStructure.put("edges", new LinkedHashMap<String, List<String>>() {{
            put("common", commonEdges);
            put("added", addedEdges);
            put("removed", removedEdges);
        }});

        Map<String, Object> methodMap = new LinkedHashMap<>();
        methodMap.put("signature", diff.entryMethod);
        methodMap.put("graph", graphStructure);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("method", methodMap);
        
        return result;
    }
    
    /**
     * Convert changed methods graphs within modified activities
     */
    private static List<Map<String, Object>> convertChangedMethodsGraphs(
            Map<String, CustomChangedMethodsGraphs> cmsInModifiedActs) {
        
        List<Map<String, Object>> allActivities = new ArrayList<>();
        
        for (Map.Entry<String, CustomChangedMethodsGraphs> entry : cmsInModifiedActs.entrySet()) {
            String activityName = entry.getKey();
            CustomChangedMethodsGraphs changedGraphs = entry.getValue();
            
            Map<String, Object> activityChanges = new HashMap<>();
            activityChanges.put("activity", activityName);
            
            // Handle added methods
            List<Map<String, Object>> addedMethods = new ArrayList<>();
            for (CustomMethodCallGraph graph : changedGraphs.addedMethodsCallGraphs) {
                addedMethods.add(convertToJsonFormat(graph));
            }
            
            // Handle removed methods
            List<Map<String, Object>> removedMethods = new ArrayList<>();
            for (CustomMethodCallGraph graph : changedGraphs.removedMethodsCallGraphs) {
                removedMethods.add(convertToJsonFormat(graph));
            }
            
            // Handle modified methods
            List<Map<String, Object>> modifiedMethods = new ArrayList<>();
            for (CustomModifiedMethodCallGraph graph : changedGraphs.modifiedMethodsCallGraphs) {
                modifiedMethods.add(convertModifiedToJsonFormat(graph));
            }
            
            Map<String, Object> changes = new HashMap<>();
            changes.put("added_methods", addedMethods);
            changes.put("removed_methods", removedMethods);
            changes.put("modified_methods", modifiedMethods);
            
            activityChanges.put("changes", changes);
            allActivities.add(activityChanges);
        }
        
        return allActivities;
    }


    
    /**
     * Write JSON to file with pretty printing
     */
    private static void writeJsonToFile(Object data, File file) throws IOException {
        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();
            
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        }
    }
}