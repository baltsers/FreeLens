package org.freelens.util;

import org.freelens.SootConfig;
import soot.Scene;
import soot.SootClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SootClassUtil {
    public static Set<String> getAndroidActivityAndFragmentClasses(String apkPath) {
        SootUtil.setupSoot(SootConfig.AndroidJarPath, apkPath);
        Set<String> activityClassesStr = new HashSet<>();

        for (SootClass sClass : Scene.v().getApplicationClasses()) {
            if (sClass.isPhantom()) {
                continue;
            }
            if (Scene.v().getSootClass("android.app.Activity").isPhantom()) {
                System.err.println("Error: android.app.Activity class is phantom.");
                continue;
            }
            if (sClass.isInterface()) {
                continue;
            }
            if (sClass.getName().startsWith("androidx") || sClass.getName().startsWith("android")) {
                continue;
            }
            if (Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("android.app.Activity")) 
            || Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("androidx.activity.ComponentActivity"))
            || Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("android.app.Fragment")) 
            || Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("android.support.v4.app.Fragment"))
            || Scene.v().getActiveHierarchy().isClassSubclassOf(sClass, Scene.v().getSootClass("androidx.fragment.app.Fragment"))) {
                activityClassesStr.add(sClass.getName());
            }
        }

        return activityClassesStr;
    }
}
