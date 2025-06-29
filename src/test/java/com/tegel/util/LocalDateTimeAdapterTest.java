package com.tegel.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.time.LocalDateTime;

class LocalDateTimeAdapterTest {
    private LocalDateTimeAdapter adapter = new LocalDateTimeAdapter();

    @Test
    void testSerializeDeserialize() {
        LocalDateTime now = LocalDateTime.now();
        JsonElement json = adapter.serialize(now, null, null);
        LocalDateTime parsed = adapter.deserialize(json, null, null);
        assertEquals(now.toLocalDate(), parsed.toLocalDate());
    }
}

