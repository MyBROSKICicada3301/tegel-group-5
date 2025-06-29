package com.tegel.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {
    @Test
    void testHashPasswordNotNull() {
        String hash = SecurityUtils.hashPassword("TestPassword123!");
        assertNotNull(hash);
    }

    @Test
    void testIsValidEmail() {
        assertTrue(SecurityUtils.isValidEmail("test@example.com"));
        assertFalse(SecurityUtils.isValidEmail("bademail"));
    }
}

