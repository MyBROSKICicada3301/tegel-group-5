package com.tegel.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.tegel.model.Event;

class EventDAOTest {
    private EventDAO dao;

    @BeforeEach
    void setUp() {
        dao = new EventDAO();
    }

    @Test
    void testCreateEventWithNull() {
        assertFalse(dao.createEvent(null));
    }
}

