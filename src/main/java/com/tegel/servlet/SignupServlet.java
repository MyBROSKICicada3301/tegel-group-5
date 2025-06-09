package com.tegel.servlet;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

import com.tegel.dao.UserDAO;
import com.tegel.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String fullName = request.getParameter("fullName");
        String nickname = request.getParameter("nickname");
        String email = request.getParameter("emailAddress");
        String password = request.getParameter("password");
        String dobString = request.getParameter("dob");
        String phone = request.getParameter("phone");
        String dietary = request.getParameter("dietary");
        
        try {
            // Check if user already exists
            if (userDAO.getUserByEmail(email) != null) {
                response.sendRedirect("signup.html?error=exists");
                return;
            }
            
            String hashedPassword = hashPassword(password);
            LocalDate dateOfBirth = LocalDate.parse(dobString);
            
            User user = new User(email, hashedPassword, phone, dateOfBirth, 
                               dietary, fullName, nickname);
            
            if (userDAO.createUser(user)) {
                response.sendRedirect("login.html?success=registered");
            } else {
                response.sendRedirect("signup.html?error=failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signup.html?error=server");
        }
    }
    
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
