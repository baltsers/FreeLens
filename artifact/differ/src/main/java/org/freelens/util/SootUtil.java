package org.freelens.util;

import soot.G;
import soot.Scene;
import soot.options.Options;

import java.util.Collections;

public class SootUtil {
    private SootUtil() {
    }

    public static void setupSoot(String androidJar, String apkPath) {
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_whole_program(true);
        Options.v().set_prepend_classpath(true);
        Options.v().set_validate(true);
        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_output_format(Options.output_format_dex);
        Options.v().set_android_jars(androidJar);
        Options.v().set_process_dir(Collections.singletonList(apkPath));
        Options.v().set_ignore_resolution_errors(true);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_process_multiple_dex(true);

        Scene.v().loadNecessaryClasses();
    }
}
