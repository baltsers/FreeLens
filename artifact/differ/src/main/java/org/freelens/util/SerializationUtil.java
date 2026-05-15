package org.freelens.util;

import org.freelens.data.SerializableCallGraph;
import org.xmlpull.v1.XmlPullParserException;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.io.File;
import java.io.IOException;

public class SerializationUtil {
    public static void serializeCallGraph(String apkPath, String outputDir) throws IOException, XmlPullParserException {
        SerializableCallGraph customCallGraph = new SerializableCallGraph();
        String packageName = AndroidUtil.getPackageName(apkPath);
        String versionName = AndroidUtil.getVersionName(apkPath);
        FlowDroid.createCallGraph(apkPath);
        CallGraph flowDroidCallGraph = Scene.v().getCallGraph();
        customCallGraph.importFromFlowDroid(flowDroidCallGraph);
        String outputFilePath = outputDir + File.separator + packageName + "-" + versionName + ".ser";
        customCallGraph.serialize(outputFilePath);
    }
}
