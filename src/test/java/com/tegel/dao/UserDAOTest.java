package com.tegel.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import com.tegel.model.User;

class UserDAOTest {
    private UserDAO dao;

    @BeforeEach
    void setUp() {
        dao = new UserDAO();
    }

    @Test
    void testCreateUserWithNull() {
        assertFalse(dao.createUser(null));
    }
}

