package org.freelens.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.freelens.callpaths.AddedRemovedMethodsResults;
import org.freelens.callpaths.ChangeResults;

import java.io.File;
import java.io.IOException;

public class JsonUtils {
    public static void saveToJson(ChangeResults results, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), results);
    }

    public static void saveToJson(AddedRemovedMethodsResults results, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(filePath), results);
    }

    public static ChangeResults loadFromJson(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), ChangeResults.class);
    }
}
