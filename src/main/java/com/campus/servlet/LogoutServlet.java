package com.campus.servlet;

import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet invalidating active user sessions.
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        String isAjax = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(isAjax) || request.getContentType() != null && request.getContentType().contains("application/json")) {
            response.setContentType("application/json");
            JsonObject res = new JsonObject();
            res.addProperty("status", "success");
            res.addProperty("message", "Logged out successfully.");
            res.addProperty("redirectUrl", "login.html");
            response.getWriter().print(res);
        } else {
            response.sendRedirect("login.html");
        }
    }
}
