package rulai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;

public class JsonToMapParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 从JSON文件读取Map
    public static Map<String, Object> jsonToMap(String filePath) throws Exception {
        return objectMapper.readValue(new File(filePath),
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
    }

    // 从Map生成JSON字符串
    public static String mapToJson(Map<String, Object> map) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    }
}