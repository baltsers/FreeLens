package org.freelens.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SDKMethods {
    private static Set<String> sdkMethodSignatures = new HashSet<>();

    public static void loadSDKMethods(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sdkMethodSignatures.add(line.trim());
            }
        }
    }

    public static boolean isSDKMethod(String methodSignature) {
        for (String sdkSignature : sdkMethodSignatures) {
            if (methodSignature.startsWith(sdkSignature)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInitializerMethod(String methodSignature) {
        return methodSignature.contains("<init") || 
                methodSignature.contains("<clinit");
    }

    public static boolean isJavaLibraryMethod(String methodSignature) {
        return methodSignature.contains("<java.") || 
                methodSignature.contains("<javax.") || 
                methodSignature.contains("<sun.") || 
                methodSignature.contains("<com.sun.");
    }
}
