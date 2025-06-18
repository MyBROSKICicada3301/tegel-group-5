package com.tegel.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/check-session")
public class CheckSessionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        PrintWriter out = response.getWriter();

        if (session != null && session.getAttribute("userId") != null) {
            // User is logged in
            out.print(
                    "{\"loggedIn\": true, \"role\": \"" + session.getAttribute("userRole") + "\"}");
        } else {
            // User is not logged in
            out.print("{\"loggedIn\": false}");
        }

        out.flush();
    }
}
