package org.freelens.data;
import java.io.*;
import java.util.*;


import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.CallGraph;


/**
 * Serializable wrapper for FlowDroid call graph
 * Maintains API compatibility while enabling serialization
 */
public class SerializableCallGraph implements Serializable {
    private static final long serialVersionUID = 1L;

    // Store edges in adjacency list format
    private Map<String, Set<CustomEdge>> outgoingEdges;
    private Map<String, Set<CustomEdge>> incomingEdges;
    private Set<CustomEdge> allEdges;

    public SerializableCallGraph() {
        outgoingEdges = new HashMap<>();
        incomingEdges = new HashMap<>();
        allEdges = new HashSet<>();
    }

    /**
     * Custom edge class that's serializable
     */
    public static class CustomEdge implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String srcMethod;
        private final String tgtMethod;

        public CustomEdge(String srcMethod, String tgtMethod) {
            this.srcMethod = srcMethod;
            this.tgtMethod = tgtMethod;

        }

        public String getSrcMethod() {
            return srcMethod;
        }

        public String getTgtMethod() {
            return tgtMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomEdge that = (CustomEdge) o;
            return Objects.equals(srcMethod, that.srcMethod) &&
                    Objects.equals(tgtMethod, that.tgtMethod);
        }

        @Override
        public int hashCode() {
            return Objects.hash(srcMethod, tgtMethod);
        }

        @Override
        public String toString() {
            return srcMethod + " -> " + tgtMethod;
        }
    }

    /**
     * Convert FlowDroid Edge to our CustomEdge
     */
    private CustomEdge convertEdge(Edge edge) {
        return new CustomEdge(
                edge.getSrc().method().getSignature(),
                edge.getTgt().method().getSignature()
                );
    }

    /**
     * Add an edge to the graph
     */
    public void addEdge(CustomEdge edge) {
        allEdges.add(edge);

        outgoingEdges.computeIfAbsent(edge.getSrcMethod(), k -> new HashSet<>()).add(edge);
        incomingEdges.computeIfAbsent(edge.getTgtMethod(), k -> new HashSet<>()).add(edge);
    }

    /**
     * Import edges from FlowDroid call graph
     */
    public void importFromFlowDroid(CallGraph callGraph) {
        Iterator<Edge> edges = callGraph.iterator();
        while (edges.hasNext()) {
            Edge edge = edges.next();
            if (edge.getSrc() == null || edge.getTgt() == null) {
                continue;
            }
            addEdge(convertEdge(edge));
        }
    }

    /**
     * Get iterator for outgoing edges (FlowDroid API compatible)
     */
    public Iterator<CustomEdge> edgesOutOf(String method) {
        Set<CustomEdge> edges = outgoingEdges.get(method);
        return edges != null ? edges.iterator() : Collections.emptyIterator();
    }

    /**
     * Get iterator for incoming edges
     */
    public Iterator<CustomEdge> edgesInto(String method) {
        Set<CustomEdge> edges = incomingEdges.get(method);
        return edges != null ? edges.iterator() : Collections.emptyIterator();
    }

    /**
     * Get all edges
     */
    public Set<CustomEdge> getAllEdges() {
        return Collections.unmodifiableSet(allEdges);
    }

    /**
     * Serialize the call graph to a file
     */
    public void serialize(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        }
    }

    /**
     * Deserialize the call graph from a file
     */
    public static SerializableCallGraph deserialize(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (SerializableCallGraph) in.readObject();
        }
    }

//    public static void main(String[] args) {
//        String apkPath = "com.dfa.hubzilla_android_46.apk";
//        createCallGraph(apkPath);
//        CallGraph callGraph = Scene.v().getCallGraph();
//        SerializableCallGraph serializableCallGraph = new SerializableCallGraph();
//        serializableCallGraph.importFromFlowDroid(callGraph);
//        for (Edge edge : callGraph) {
//            if (edge.getSrc() == null) {
//                continue;
//            }
//            System.out.println(edge.getSrc().method().getSignature() + " -> " + edge.getTgt().method().getSignature());
//        }
//        System.out.println("Serialized call graph to callgraph.ser");
//        for (CustomEdge edge : serializableCallGraph.getAllEdges()) {
//            System.out.println(edge.getSrcMethod() + " -> " + edge.getTgtMethod());
//        }
//
//    }
}