package com.tegel.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.tegel.model.Newsletter;

class NewsletterDAOTest {
    private NewsletterDAO dao;

    @BeforeEach
    void setUp() {
        dao = new NewsletterDAO();
    }

    @Test
    void testCreateNewsletterWithNull() {
        assertThrows(NullPointerException.class, () -> dao.createNewsletter(null));
    }
}

