package com.campus.servlet;

import com.campus.dao.ApplicationDAO;
import com.campus.dao.CompanyDAO;
import com.campus.dao.JobDriveDAO;
import com.campus.dao.StudentDAO;
import com.campus.model.Company;
import com.campus.model.JobDrive;
import com.campus.model.Student;
import com.campus.model.User;
import com.campus.util.EligibilityChecker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.sql.Date;
import java.util.List;

/**
 * Servlet managing Job Drives creation, details, search, and automatic eligibility evaluation.
 */
@WebServlet("/api/job-drive")
public class JobDriveServlet extends HttpServlet {
    private final JobDriveDAO jobDriveDAO = new JobDriveDAO();
    private final CompanyDAO companyDAO = new CompanyDAO();
    private final StudentDAO studentDAO = new StudentDAO();
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

        String action = request.getParameter("action");
        String jobIdStr = request.getParameter("id");

        if ("get".equalsIgnoreCase(action) && jobIdStr != null) {
            int jobId = Integer.parseInt(jobIdStr);
            JobDrive drive = jobDriveDAO.getJobDriveById(jobId);
            if (drive != null) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("jobDrive", gson.toJsonTree(drive));

                // If logged-in user is a student, attach automatic eligibility evaluation and application status
                if (user != null && "STUDENT".equalsIgnoreCase(user.getRole())) {
                    Student student = studentDAO.getStudentByUserId(user.getUserId());
                    if (student != null) {
                        EligibilityChecker.EligibilityResult result = EligibilityChecker.checkEligibility(student, drive);
                        jsonResponse.addProperty("isEligible", result.isEligible());
                        jsonResponse.addProperty("eligibilityReason", result.getReasonSummary());
                        jsonResponse.addProperty("hasApplied", applicationDAO.hasApplied(student.getStudentId(), jobId));
                    }
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Job Drive not found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if ("company".equalsIgnoreCase(action)) {
            // Get drives belonging to logged-in recruiter company
            if (user == null || !"COMPANY".equalsIgnoreCase(user.getRole())) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unauthorized.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(jsonResponse);
                return;
            }
            Company company = companyDAO.getCompanyByUserId(user.getUserId());
            if (company != null) {
                List<JobDrive> drives = jobDriveDAO.getJobDrivesByCompanyId(company.getCompanyId());
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("jobDrives", gson.toJsonTree(drives));
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Company profile not found.");
            }
        } else {
            // List all job drives for students, visitors, or admin
            List<JobDrive> drives = jobDriveDAO.getAllJobDrives();
            jsonResponse.addProperty("status", "success");

            // Build list with student eligibility if student logged in
            Student loggedStudent = (user != null && "STUDENT".equalsIgnoreCase(user.getRole())) ? 
                    studentDAO.getStudentByUserId(user.getUserId()) : null;

            JsonArray array = new JsonArray();
            for (JobDrive d : drives) {
                JsonObject obj = gson.toJsonTree(d).getAsJsonObject();
                if (loggedStudent != null) {
                    EligibilityChecker.EligibilityResult result = EligibilityChecker.checkEligibility(loggedStudent, d);
                    obj.addProperty("isEligible", result.isEligible());
                    obj.addProperty("eligibilityReason", result.getReasonSummary());
                    obj.addProperty("hasApplied", applicationDAO.hasApplied(loggedStudent.getStudentId(), d.getJobId()));
                }
                array.add(obj);
            }
            jsonResponse.add("jobDrives", array);
            response.setStatus(HttpServletResponse.SC_OK);
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

        if (user == null || (!"COMPANY".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole()))) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unauthorized. Only recruiters and placement officers can post or manage job drives.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
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
            String action = reqObj.has("action") ? reqObj.get("action").getAsString() : "create";

            if ("create".equalsIgnoreCase(action)) {
                int companyId = 0;
                if ("COMPANY".equalsIgnoreCase(user.getRole())) {
                    Company c = companyDAO.getCompanyByUserId(user.getUserId());
                    if (c == null) {
                        jsonResponse.addProperty("status", "error");
                        jsonResponse.addProperty("message", "Company profile not found.");
                        out.print(jsonResponse);
                        return;
                    }
                    companyId = c.getCompanyId();
                } else if (reqObj.has("companyId")) {
                    companyId = reqObj.get("companyId").getAsInt();
                }

                String jobRole = reqObj.get("jobRole").getAsString();
                String pkg = reqObj.get("package").getAsString();
                double minCgpa = reqObj.get("minimumCgpa").getAsDouble();
                String eligibleBranches = reqObj.get("eligibleBranches").getAsString();
                int maxBacklogs = reqObj.get("maximumBacklogs").getAsInt();
                String skills = reqObj.get("requiredSkills").getAsString();
                String desc = reqObj.get("jobDescription").getAsString();
                Date deadline = Date.valueOf(reqObj.get("applicationDeadline").getAsString());

                JobDrive drive = new JobDrive(0, companyId, jobRole, pkg, minCgpa, eligibleBranches, maxBacklogs, skills, desc, deadline, null);
                boolean success = jobDriveDAO.addJobDrive(drive);

                if (success) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Job Drive created successfully!");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to create job drive.");
                }
            } else if ("update".equalsIgnoreCase(action)) {
                int jobId = reqObj.get("jobId").getAsInt();
                JobDrive drive = jobDriveDAO.getJobDriveById(jobId);
                if (drive == null) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Job drive not found.");
                    out.print(jsonResponse);
                    return;
                }

                drive.setJobRole(reqObj.get("jobRole").getAsString());
                drive.setPackageAmount(reqObj.get("package").getAsString());
                drive.setMinimumCgpa(reqObj.get("minimumCgpa").getAsDouble());
                drive.setEligibleBranches(reqObj.get("eligibleBranches").getAsString());
                drive.setMaximumBacklogs(reqObj.get("maximumBacklogs").getAsInt());
                drive.setRequiredSkills(reqObj.get("requiredSkills").getAsString());
                drive.setJobDescription(reqObj.get("jobDescription").getAsString());
                drive.setApplicationDeadline(Date.valueOf(reqObj.get("applicationDeadline").getAsString()));

                boolean updated = jobDriveDAO.updateJobDrive(drive);
                if (updated) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Job drive updated successfully.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to update job drive.");
                }
            } else if ("delete".equalsIgnoreCase(action)) {
                int jobId = reqObj.get("jobId").getAsInt();
                boolean deleted = jobDriveDAO.deleteJobDrive(jobId);
                if (deleted) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Job drive deleted.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to delete job drive.");
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
