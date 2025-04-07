package backend.academy.scrapper.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.List;

@Converter(autoApply = true)
public class ListToJsonConverter implements AttributeConverter<List<Object>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Object> objects) {
        if (objects == null) return null;
        try {
            return objectMapper.writeValueAsString(objects);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<Object> convertToEntityAttribute(String s) {
        if (s == null) return null;
        try {
            return objectMapper.readValue(s, new TypeReference<List<Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
