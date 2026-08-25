package com.campus.servlet;

import com.campus.dao.UserDAO;
import com.campus.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet handling user authentication and role-based session initialization.
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            // Read JSON body request
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);
            String email = jsonRequest != null && jsonRequest.has("email") ? jsonRequest.get("email").getAsString() : request.getParameter("email");
            String password = jsonRequest != null && jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : request.getParameter("password");

            if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Email and password are required.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse);
                return;
            }

            User user = userDAO.loginUser(email.trim(), password.trim());

            if (user != null) {
                // Initialize user session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userRole", user.getRole());
                session.setAttribute("userName", user.getName());

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Login successful!");
                jsonResponse.addProperty("role", user.getRole());
                jsonResponse.addProperty("name", user.getName());
                jsonResponse.addProperty("userId", user.getUserId());

                // Set target dashboard redirect URL based on role
                switch (user.getRole().toUpperCase()) {
                    case "STUDENT":
                        jsonResponse.addProperty("redirectUrl", "student-dashboard.html");
                        break;
                    case "COMPANY":
                        jsonResponse.addProperty("redirectUrl", "company-dashboard.html");
                        break;
                    case "ADMIN":
                        jsonResponse.addProperty("redirectUrl", "admin-dashboard.html");
                        break;
                    default:
                        jsonResponse.addProperty("redirectUrl", "index.html");
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid email or password.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Server error during login: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        out.print(jsonResponse);
        out.flush();
    }
}
