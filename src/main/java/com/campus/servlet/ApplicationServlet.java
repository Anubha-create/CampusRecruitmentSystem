package com.campus.servlet;

import com.campus.dao.ApplicationDAO;
import com.campus.dao.CompanyDAO;
import com.campus.dao.JobDriveDAO;
import com.campus.dao.StudentDAO;
import com.campus.model.Application;
import com.campus.model.Company;
import com.campus.model.JobDrive;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.util.EligibilityChecker;
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
 * Servlet handling job applications, candidate tracking, and status pipeline updates.
 */
@WebServlet("/api/application")
public class ApplicationServlet extends HttpServlet {
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final JobDriveDAO jobDriveDAO = new JobDriveDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
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

        if (user == null) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized. Please log in.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print(jsonResponse);
            return;
        }

        String action = request.getParameter("action");

        if ("history".equalsIgnoreCase(action) || "student".equalsIgnoreCase(action)) {
            // Student application history
            Student student = studentDAO.getStudentByUserId(user.getUserId());
            if (student != null) {
                List<Application> history = applicationDAO.getApplicationsByStudent(student.getStudentId());
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("applications", gson.toJsonTree(history));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Student profile not found.");
            }
        } else if ("job".equalsIgnoreCase(action)) {
            // Recruiter viewing applicants for a specific job drive
            int jobId = Integer.parseInt(request.getParameter("jobId"));
            List<Application> applicants = applicationDAO.getApplicationsByJob(jobId);
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("applications", gson.toJsonTree(applicants));
            response.setStatus(HttpServletResponse.SC_OK);
        } else if ("company".equalsIgnoreCase(action)) {
            // Recruiter viewing all applicants for their company drives
            Company company = companyDAO.getCompanyByUserId(user.getUserId());
            if (company != null) {
                List<Application> applicants = applicationDAO.getApplicationsByCompany(company.getCompanyId());
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("applications", gson.toJsonTree(applicants));
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Company profile not found.");
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
            String action = reqObj.has("action") ? reqObj.get("action").getAsString() : "apply";

            if ("apply".equalsIgnoreCase(action)) {
                if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Only students can apply for job drives.");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(jsonResponse);
                    return;
                }

                int jobId = reqObj.get("jobId").getAsInt();
                Student student = studentDAO.getStudentByUserId(user.getUserId());
                if (student == null) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Student profile not found.");
                    out.print(jsonResponse);
                    return;
                }

                JobDrive drive = jobDriveDAO.getJobDriveById(jobId);
                if (drive == null) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Job Drive not found.");
                    out.print(jsonResponse);
                    return;
                }

                // Check duplicate application
                if (applicationDAO.hasApplied(student.getStudentId(), jobId)) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "You have already applied for this job drive.");
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print(jsonResponse);
                    return;
                }

                // Server-side Automatic Eligibility Engine Verification
                EligibilityChecker.EligibilityResult eligibility = EligibilityChecker.checkEligibility(student, drive);
                if (!eligibility.isEligible()) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Cannot apply: " + eligibility.getReasonSummary());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(jsonResponse);
                    return;
                }

                boolean applied = applicationDAO.applyForJob(student.getStudentId(), jobId);
                if (applied) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Application submitted successfully!");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to submit application.");
                }

            } else if ("updateStatus".equalsIgnoreCase(action)) {
                // Recruiter or Admin updating application pipeline status
                if (!"COMPANY".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Unauthorized to update candidate status.");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(jsonResponse);
                    return;
                }

                int applicationId = reqObj.get("applicationId").getAsInt();
                String status = reqObj.get("status").getAsString();

                boolean updated = applicationDAO.updateApplicationStatus(applicationId, status);
                if (updated) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Application status updated to '" + status + "'.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to update application status.");
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
