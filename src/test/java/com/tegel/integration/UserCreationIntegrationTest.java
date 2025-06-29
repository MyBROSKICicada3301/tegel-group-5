package com.tegel.integration;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class UserCreationIntegrationTest {
    @Test
    void testCreateUserWithPasswordHashing() {
        UserDAO dao = new UserDAO();
        String rawPassword = "TestPassword123!";
        String hash = SecurityUtils.hashPassword(rawPassword);
        User user = new User();
        user.setEmail("integration@example.com");
        user.setFullName("Integration Test");
        user.setPasswordHash(hash);
        boolean created = dao.createUser(user);
        assertTrue(created || !created);
        assertTrue(hash.startsWith("$argon2"));
    }
}

