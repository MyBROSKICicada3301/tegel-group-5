package com.tegel.util;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityUtilsTest {

    @Test
    public void testIsValidRole() {
        Assertions.assertTrue(SecurityUtils.isValidRole("admin"));
        Assertions.assertTrue(SecurityUtils.isValidRole("user"));
        Assertions.assertTrue(SecurityUtils.isValidRole("member"));
        Assertions.assertFalse(SecurityUtils.isValidRole("guest"));
        Assertions.assertFalse(SecurityUtils.isValidRole("")); 
        Assertions.assertFalse(SecurityUtils.isValidRole(null));
    }

    @Test
    public void testIsValidLocation() {
        assertTrue(SecurityUtils.isValidLocation("Valid"));
        assertTrue(SecurityUtils.isValidLocation(null)); 
        assertFalse(SecurityUtils.isValidLocation("DROP TABLE users;"));
    }


    @Test
    public void testIsValidUsername() {
        Assertions.assertTrue(SecurityUtils.isValidUsername("validUser123"));
        Assertions.assertTrue(SecurityUtils.isValidUsername("user_name"));
        Assertions.assertFalse(SecurityUtils.isValidUsername(""));
        Assertions.assertFalse(SecurityUtils.isValidUsername(null));
        Assertions.assertFalse(SecurityUtils.isValidUsername("invalid user"));
        Assertions.assertFalse(SecurityUtils.isValidUsername("user!@#"));
    }

    @Test
    public void testIsValidEmail() {
        Assertions.assertTrue(SecurityUtils.isValidEmail("user12@gmail.com"));
        Assertions.assertFalse(SecurityUtils.isValidEmail("user12gmail.com"));
        Assertions.assertFalse(SecurityUtils.isValidEmail("user12@gmailcom"));
        Assertions.assertFalse(SecurityUtils.isValidEmail("user12@.com"));
        Assertions.assertFalse(SecurityUtils.isValidEmail("user12@com"));
        Assertions.assertFalse(SecurityUtils.isValidEmail(null));
    }
}
