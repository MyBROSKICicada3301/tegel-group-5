package com.tegel.servlet;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.tegel.dao.UserDAO;
import com.tegel.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // Check admin authorization
        if (!"admin".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        response.setContentType("application/json");

        if ("/users".equals(pathInfo)) {
            List<User> users = userDAO.getAllUsers();
            response.getWriter().write(gson.toJson(users));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // Check admin authorization
        if (!"admin".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if ("/updateRole".equals(pathInfo)) {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String newRole = request.getParameter("newRole");

            boolean success = userDAO.updateUserRole(userId, newRole);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":" + success + "}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");

        // Check admin authorization
        if (!"admin".equals(userRole)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/user/")) {
            int userId = Integer.parseInt(pathInfo.substring(6));
            boolean success = userDAO.deleteUser(userId);

            response.setContentType("application/json");
            response.getWriter().write("{\"success\":" + success + "}");
        }
    }
}
