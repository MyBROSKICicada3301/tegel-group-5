package com.tegel.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class AnnouncementDAOTest {
    private AnnouncementDAO dao;

    @BeforeEach
    void setUp() {
        dao = new AnnouncementDAO();
    }

    @Test
    void testGetPublicAnnouncements() {
        List<?> announcements = dao.getPublicAnnouncements();
        assertNotNull(announcements);
    }
}

