package com.tegel.servlet;

import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;
import com.tegel.util.SecurityUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@MultipartConfig
@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(SignupServlet.class.getName());
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        System.out.println("servlet reach, THIS IS BIG");

        try {
            // Get parameters with null checks
            String fullName = request.getParameter("fullName");
            String nickname = request.getParameter("nickname");
            String email = request.getParameter("emailAddress");
            String password = request.getParameter("password");
            String dobString = request.getParameter("dob");
            String phone = request.getParameter("phone");
            String dietary = request.getParameter("dietary");

            System.out.println("Raw Inputs:");
            System.out.println("fullName = " + fullName);
            System.out.println("nickname = " + nickname);
            System.out.println("email = " + email);
            System.out.println("password = " + password);
            System.out.println("dob = " + dobString);
            System.out.println("phone = " + phone);
            System.out.println("dietary = " + dietary);

            if (!isValidRequiredFields(fullName, email, password, dobString, phone)) {
                System.out.println("❌ Missing required fields");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Missing required fields\"}");
                return;
            }

            // Sanitize
            try {
                fullName = SecurityUtils.sanitizeInput(fullName);
                nickname = nickname != null ? SecurityUtils.sanitizeInput(nickname) : null;
                email = SecurityUtils.sanitizeInput(email).toLowerCase().trim();
                phone = SecurityUtils.sanitizeInput(phone);
                dietary = dietary != null ? SecurityUtils.sanitizeTextArea(dietary) : null;
            } catch (SecurityException e) {
                System.out.println("❌ Security error in inputs: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid input data detected\"}");
                return;
            }

            System.out.println("✅ Inputs sanitized successfully");

            // Validation
            if (!SecurityUtils.isValidEmail(email)) {
                System.out.println("❌ Invalid email");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid email format\"}");
                return;
            }

            if (!SecurityUtils.isValidName(fullName)) {
                System.out.println("❌ Invalid name");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid name format\"}");
                return;
            }

            if (!SecurityUtils.isValidPassword(password)) {
                System.out.println("❌ Invalid password format");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter()
                        .write("{\"error\":\"Password must be at least 8 characters with uppercase, lowercase, number and special character\"}");
                return;
            }

            if (!SecurityUtils.isValidPhone(phone)) {
                System.out.println("❌ Invalid phone");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid phone number format\"}");
                return;
            }

            System.out.println("✅ All fields validated");

            if (userDAO.getUserByEmail(email) != null) {
                System.out.println("❌ Email already exists: " + email);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("{\"error\":\"User already exists\"}");
                return;
            }

            LocalDate dateOfBirth;
            try {
                dateOfBirth = LocalDate.parse(dobString);

                if (dateOfBirth.isAfter(LocalDate.now().minusYears(13))) {
                    System.out.println("❌ Too young to register");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Must be at least 13 years old\"}");
                    return;
                }

                if (dateOfBirth.isBefore(LocalDate.now().minusYears(120))) {
                    System.out.println("❌ Birth year too old");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Invalid birth date\"}");
                    return;
                }
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid date format\"}");
                return;
            }

            System.out.println("✅ DOB valid");

            String hashedPassword;
            try {
                hashedPassword = SecurityUtils.hashPassword(password);
                System.out.println("✅ Password hashed");
            } catch (Exception e) {
                System.out.println("❌ Password hashing failed");
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Registration failed\"}");
                return;
            }

            User user = new User(email, hashedPassword, phone, dateOfBirth, dietary, fullName, nickname);
            System.out.println("✅ User object created");

            if (userDAO.createUser(user)) {
                System.out.println("✅ User successfully registered");
                response.sendRedirect("login.html?success=registered");
            } else {
                System.out.println("❌ Failed to save user in DB");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Registration failed\"}");
            }

        } catch (Exception e) {
            System.out.println("❌ Unexpected error");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Server error occurred\"}");
        }
    }

    private boolean isValidRequiredFields(String fullName, String email, String password,
                                          String dob, String phone) {
        return fullName != null && !fullName.trim().isEmpty() && email != null &&
                !email.trim().isEmpty() && password != null && !password.trim().isEmpty() &&
                dob != null && !dob.trim().isEmpty() && phone != null && !phone.trim().isEmpty();
    }
}
