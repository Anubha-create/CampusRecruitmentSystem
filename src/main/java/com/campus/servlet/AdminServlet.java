package com.campus.servlet;

import com.campus.dao.ApplicationDAO;
import com.campus.model.Application;
import com.campus.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling Admin / Placement Officer dashboard statistics and reports.
 */
@WebServlet("/api/admin")
public class AdminServlet extends HttpServlet {
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession(false);
        User user = session != null ? (User) session.getAttribute("user") : null;

        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized. Placement Officer access required.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print(jsonResponse);
            return;
        }

        String action = request.getParameter("action");

        if ("stats".equalsIgnoreCase(action) || action == null) {
            Map<String, Integer> stats = applicationDAO.getPlacementStats();
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("stats", gson.toJsonTree(stats));
            response.setStatus(HttpServletResponse.SC_OK);
        } else if ("applications".equalsIgnoreCase(action)) {
            String company = request.getParameter("company");
            String role = request.getParameter("role");
            String student = request.getParameter("student");
            String status = request.getParameter("status");

            List<Application> apps = applicationDAO.getAllApplications(company, role, student, status);
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("applications", gson.toJsonTree(apps));
            response.setStatus(HttpServletResponse.SC_OK);
        }

        out.print(jsonResponse);
        out.flush();
    }
}
