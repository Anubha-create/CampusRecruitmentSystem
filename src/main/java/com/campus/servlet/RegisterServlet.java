package com.campus.servlet;

import com.campus.dao.CompanyDAO;
import com.campus.dao.StudentDAO;
import com.campus.dao.UserDAO;
import com.campus.model.Company;
import com.campus.model.Student;
import com.campus.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet handling Student and Company registration workflows.
 */
@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonObject reqObj = gson.fromJson(sb.toString(), JsonObject.class);
            if (reqObj == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid registration request payload.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse);
                return;
            }

            String role = reqObj.has("role") ? reqObj.get("role").getAsString().toUpperCase() : "STUDENT";
            String email = reqObj.has("email") ? reqObj.get("email").getAsString().trim() : "";
            String password = reqObj.has("password") ? reqObj.get("password").getAsString().trim() : "";
            String name = reqObj.has("name") ? reqObj.get("name").getAsString().trim() : "";

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Name, email, and password are required.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(jsonResponse);
                return;
            }

            if (userDAO.isEmailRegistered(email)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "This email address is already registered.");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print(jsonResponse);
                return;
            }

            User user = new User(name, email, password, role);
            int userId = userDAO.registerUser(user);

            if (userId <= 0) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to create user account.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print(jsonResponse);
                return;
            }

            boolean profileSuccess = false;
            if ("STUDENT".equals(role)) {
                String branch = reqObj.has("branch") ? reqObj.get("branch").getAsString().trim() : "CSE";
                double cgpa = reqObj.has("cgpa") ? reqObj.get("cgpa").getAsDouble() : 0.0;
                int gradYear = reqObj.has("graduationYear") ? reqObj.get("graduationYear").getAsInt() : 2026;
                int backlogs = reqObj.has("activeBacklogs") ? reqObj.get("activeBacklogs").getAsInt() : 0;
                String skills = reqObj.has("skills") ? reqObj.get("skills").getAsString().trim() : "";

                Student student = new Student(0, userId, branch, cgpa, gradYear, backlogs, skills);
                profileSuccess = studentDAO.addStudent(student);
            } else if ("COMPANY".equals(role)) {
                String companyName = reqObj.has("companyName") ? reqObj.get("companyName").getAsString().trim() : name;
                String desc = reqObj.has("companyDescription") ? reqObj.get("companyDescription").getAsString().trim() : "";

                Company company = new Company(0, userId, companyName, desc);
                profileSuccess = companyDAO.addCompany(company);
            }

            if (profileSuccess) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Registration successful! You can now log in.");
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                // Rollback created user account
                userDAO.deleteUser(userId);
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Failed to save profile details.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Server error during registration: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        out.print(jsonResponse);
        out.flush();
    }
}
