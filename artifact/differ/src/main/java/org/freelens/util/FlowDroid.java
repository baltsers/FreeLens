package org.freelens.util;

import java.io.File;

import org.freelens.SootConfig;
import soot.Scene;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.options.Options;

public class FlowDroid {
    public static void createCallGraph(String apkPath) {
        //SootUtil.setupSoot(SootConfig.androidJar, apkPath);
        SetupApplication application = new SetupApplication(new File(SootConfig.AndroidJarPath), new File(apkPath));
        ARSCFileParser.STRICT_MODE = false;
        application.getConfig().setMergeDexFiles(true);
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_ignore_methodsource_error(true);
        Options.v().set_allow_cg_errors(true);
        Options.v().set_whole_program(true);
        application.getSootConfig().setSootOptions(Options.v(), application.getConfig());
        application.constructCallgraph();
    }
}
