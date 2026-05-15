package org.freelens.util;

public class StringUtil {
    public static String getNormalizedJimpleStatement(String jimpleStatement) {
        jimpleStatement = jimpleStatement.replaceAll("\\$\\$Lambda\\$[a-zA-Z0-9\\-\\$]+", "LAMBDA_CLASS");
        jimpleStatement = jimpleStatement.replaceAll("\\d{7,}", "RESOURCE_ID");
        jimpleStatement = jimpleStatement.replaceAll("-LAMBDA_CLASS[_a-zA-Z0-9\\-]*", "LAMBDA_CLASS");
        return jimpleStatement;
    }
}
