package com.example.kursinis.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LocalDateTimeAdapter implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

    // Match MySQL DATETIME format: 2025-11-29 21:37:40.000000
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.SSSSSS]").withLocale(Locale.ENGLISH);

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String dateTimeString = json.getAsString();
        // Handle both with and without microseconds
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
        // MySQL expects format: 2025-11-29 21:37:40.000000
        // We'll output with .000000 for microseconds even if they're zero
        String formatted = formatter.format(localDateTime);
        // Ensure we always have 6 digits for microseconds
        if (!formatted.contains(".")) {
            formatted += ".000000";
        }
        return new JsonPrimitive(formatted);
    }
}
