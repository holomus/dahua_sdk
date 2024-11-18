package org.example.dahuasdk.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class UnixTimestampSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Convert LocalDateTime to Unix timestamp in seconds
        gen.writeNumber(value.toEpochSecond(ZoneOffset.UTC));
    }
}
