package org.freelens.util;

import org.xmlpull.v1.XmlPullParserException;
import soot.jimple.infoflow.android.manifest.ProcessManifest;


import java.io.IOException;

public class AndroidUtil {
    private AndroidUtil() {
    }

    public static String getPackageName(String apkPath) throws XmlPullParserException, IOException {
        try (ProcessManifest manifest = new ProcessManifest(apkPath)) {
            return manifest.getPackageName();
        }
    }

    public static String getVersionName (String apkPath) throws XmlPullParserException, IOException {
        try (ProcessManifest manifest = new ProcessManifest(apkPath)) {
            return manifest.getVersionName();
        }
    }

}
