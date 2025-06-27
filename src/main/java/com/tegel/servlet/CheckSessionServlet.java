package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet to check if a user session is active and return user details if logged in.
 */
@WebServlet("/check-session")
public class CheckSessionServlet extends HttpServlet {

    /**
     * Handles GET requests to check the session status.
     * Returns JSON indicating whether the user is logged in and their userId and role if applicable.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null && session.getAttribute("userId") != null) {
            // User is logged in - include userId in response
            out.print("{\"loggedIn\": true, \"userId\": " + session.getAttribute("userId") +
                              ", \"role\": \"" + session.getAttribute("role") + "\"}");
        } else {
            // User is not logged in
            out.print("{\"loggedIn\": false}");
        }

        out.flush();
    }
}
