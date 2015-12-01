package com.vlasovartem.pmdb.utils.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * LocalDateSerializer
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {
    @Override
    public void serialize(LocalDate localDate, JsonGenerator jgen, SerializerProvider serializerProvider) throws
            IOException, JsonProcessingException {
        jgen.writeString(localDate.format(DateTimeFormatter.ofPattern("d MMM uuuu", Locale.getDefault())));
    }

    @Override
    public Class<LocalDate> handledType() {
        return LocalDate.class;
    }
}
