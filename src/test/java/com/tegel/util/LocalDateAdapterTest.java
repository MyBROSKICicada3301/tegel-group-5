package com.tegel.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import java.time.LocalDate;

class LocalDateAdapterTest {
    private LocalDateAdapter adapter = new LocalDateAdapter();

    @Test
    void testSerializeDeserialize() {
        LocalDate today = LocalDate.now();
        JsonElement json = adapter.serialize(today, null, null);
        LocalDate parsed = adapter.deserialize(json, null, null);
        assertEquals(today, parsed);
    }
}

