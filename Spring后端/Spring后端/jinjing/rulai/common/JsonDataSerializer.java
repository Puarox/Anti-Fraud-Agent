package rulai.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class JsonDataSerializer extends JsonSerializer<String> {
    
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeRawValue(value);
        }
    }
}