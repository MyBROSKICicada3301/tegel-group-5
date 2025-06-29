package com.tegel.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {
    @Test
    void testInfoDoesNotThrow() {
        assertDoesNotThrow(() -> Logger.info("TestSource", "Test message"));
    }
}

