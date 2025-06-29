package com.tegel.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;

class DatabaseManagerTest {
    @Test
    void testGetConnection() {
        assertDoesNotThrow(() -> {
            Connection conn = DatabaseManager.getConnection();
            assertNotNull(conn);
            conn.close();
        });
    }
}

