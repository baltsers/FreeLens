package org.freelens;



import org.freelens.sdkdiffer.CallGraphAnalyzer;
import org.freelens.sdkdiffer.SDKDiffer;
import org.freelens.util.SerializationUtil;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Main implements Runnable{
    @Option(names = {"--ref-apk"}, description = "Path to the apk file of the reference version", required = true)
    private String refVersionApkPath;

    @Option(names = {"--tgt-apk"}, description = "Path to the apk file of the target version", required = false)
    private String tgtVersionApkPath;

    @Option(names = {"--save-cg"}, description = "Save the call graph", required = false)
    private boolean saveCG = false;

    @Option(names = {"--ref-ser-cg"}, description = "Path to the reference call graph", required = false)
    private String refSerializedCg;

    @Option(names = {"--tgt-ser-cg"}, description = "Path to the target call graph", required = false)
    private String tgtSerializedCg;

    @Option(names = ("--max-depth"), description = "Maximum depth of the call graph", required = false)
    private int maxDepth = 2;

    @Option(names = ("--call-path"), description = "Run call path analysis", required = false)
    boolean runCallPathAnalysis = true;
    @Option(names = {"--output-dir"}, description = "Path to the output directory", required = true)
    private String outputDir;

    @Override
    public void run() {
       try {
           if (saveCG) {
               SerializationUtil.serializeCallGraph(refVersionApkPath, outputDir);
               return;
           }
           if (tgtVersionApkPath == null) {
               throw new IllegalArgumentException("Target apk path is required");
           }
           CallGraphAnalyzer.MAX_DEPTH = maxDepth;
           SDKDiffer.run(refVersionApkPath, tgtVersionApkPath, refSerializedCg, tgtSerializedCg, runCallPathAnalysis,outputDir);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    public static void main(String[] args) {
        CommandLine.run(new Main(), args);
    }

}
