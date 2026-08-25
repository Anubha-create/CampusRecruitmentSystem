package com.campus.servlet;

import com.campus.dao.CompanyDAO;
import com.campus.dao.UserDAO;
import com.campus.model.Company;
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
import java.util.List;

/**
 * Servlet for managing Company profile data and recruiter listings.
 */
@WebServlet("/api/company")
public class CompanyServlet extends HttpServlet {
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        String action = request.getParameter("action");

        if ("list".equalsIgnoreCase(action) || action == null) {
            List<Company> companies = companyDAO.getAllCompanies();
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("companies", gson.toJsonTree(companies));
            response.setStatus(HttpServletResponse.SC_OK);
        } else if ("me".equalsIgnoreCase(action)) {
            HttpSession session = request.getSession(false);
            User user = session != null ? (User) session.getAttribute("user") : null;
            if (user == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unauthorized.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(jsonResponse);
                return;
            }

            Company company = companyDAO.getCompanyByUserId(user.getUserId());
            if (company != null) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("company", gson.toJsonTree(company));
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Company profile not found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        out.print(jsonResponse);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(jsonResponse);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonObject reqObj = gson.fromJson(sb.toString(), JsonObject.class);
            String action = reqObj.has("action") ? reqObj.get("action").getAsString() : "update";

            if ("add".equalsIgnoreCase(action) && "ADMIN".equalsIgnoreCase(user.getRole())) {
                String companyName = reqObj.get("companyName").getAsString();
                String email = reqObj.get("email").getAsString();
                String password = reqObj.has("password") ? reqObj.get("password").getAsString() : "company123";
                String desc = reqObj.has("companyDescription") ? reqObj.get("companyDescription").getAsString() : "";

                if (userDAO.isEmailRegistered(email)) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Email already registered.");
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(jsonResponse);
                    return;
                }

                User newUser = new User(companyName, email, password, "COMPANY");
                int newUserId = userDAO.registerUser(newUser);
                if (newUserId > 0) {
                    Company newCompany = new Company(0, newUserId, companyName, desc);
                    boolean added = companyDAO.addCompany(newCompany);
                    if (added) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.addProperty("message", "Company created successfully.");
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        userDAO.deleteUser(newUserId);
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Failed to add company details.");
                    }
                }
            } else if ("update".equalsIgnoreCase(action)) {
                int companyId = reqObj.get("companyId").getAsInt();
                String companyName = reqObj.get("companyName").getAsString();
                String desc = reqObj.get("companyDescription").getAsString();

                Company c = new Company(companyId, 0, companyName, desc);
                boolean updated = companyDAO.updateCompany(c);
                if (updated) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Company profile updated.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to update company.");
                }
            } else if ("delete".equalsIgnoreCase(action) && "ADMIN".equalsIgnoreCase(user.getRole())) {
                int companyId = reqObj.get("companyId").getAsInt();
                boolean deleted = companyDAO.deleteCompany(companyId);
                if (deleted) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Company deleted.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to delete company.");
                }
            }
        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Server error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        out.print(jsonResponse);
        out.flush();
    }
}
