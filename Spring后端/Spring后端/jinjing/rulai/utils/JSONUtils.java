package rulai.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JSONUtils {
    @Autowired
    private ObjectMapper objectMapper;

    public String toJSONString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public <T> T fromJSONString(String string, Class<T> clazz) throws JsonProcessingException {
        return  objectMapper.readValue(string, clazz);
    }
}
