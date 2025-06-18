package com.tegel.util;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;

@WebFilter("/*") // filter all requests
public class JwtAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI();

        // Public paths, these pages can be accessed without login
        if (path.endsWith("login.html") || path.endsWith("signup.html") ||
                path.contains("/api/public/") || path.endsWith(".css") || path.endsWith(".js")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // get the cookie
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null && JwtUtil.isTokenValid(token)) {
            // valid token, continue
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            // no valid token, redirect to login
            response.sendRedirect("login.html");
        }
    }
}
