package com.campus.servlet;

import com.campus.dao.StudentDAO;
import com.campus.model.Student;
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
 * Servlet handling Student Profile fetching, updating, and search functionality.
 */
@WebServlet("/api/student")
public class StudentServlet extends HttpServlet {
    private final StudentDAO studentDAO = new StudentDAO();
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
        if ("me".equalsIgnoreCase(action) || action == null) {
            // Get logged-in student profile
            Student student = studentDAO.getStudentByUserId(user.getUserId());
            if (student != null) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("student", gson.toJsonTree(student));
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Student profile not found.");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } else if ("list".equalsIgnoreCase(action) || "search".equalsIgnoreCase(action)) {
            // Admin or Recruiter listing/searching students
            String query = request.getParameter("query");
            List<Student> students = studentDAO.searchStudents(query);
            jsonResponse.addProperty("status", "success");
            jsonResponse.add("students", gson.toJsonTree(students));
            response.setStatus(HttpServletResponse.SC_OK);
        } else if ("get".equalsIgnoreCase(action)) {
            int studentId = Integer.parseInt(request.getParameter("id"));
            Student s = studentDAO.getStudentByStudentId(studentId);
            if (s != null) {
                jsonResponse.addProperty("status", "success");
                jsonResponse.add("student", gson.toJsonTree(s));
            } else {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Student not found.");
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
            jsonResponse.addProperty("message", "Unauthorized. Please log in.");
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

            if ("update".equalsIgnoreCase(action)) {
                Student existing = studentDAO.getStudentByUserId(user.getUserId());
                if (existing == null && !"ADMIN".equals(user.getRole())) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Student profile not found.");
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(jsonResponse);
                    return;
                }

                int studentId = reqObj.has("studentId") ? reqObj.get("studentId").getAsInt() : (existing != null ? existing.getStudentId() : 0);
                int targetUserId = existing != null ? existing.getUserId() : user.getUserId();
                String name = reqObj.has("name") ? reqObj.get("name").getAsString() : user.getName();
                String branch = reqObj.has("branch") ? reqObj.get("branch").getAsString() : (existing != null ? existing.getBranch() : "");
                double cgpa = reqObj.has("cgpa") ? reqObj.get("cgpa").getAsDouble() : (existing != null ? existing.getCgpa() : 0.0);
                int gradYear = reqObj.has("graduationYear") ? reqObj.get("graduationYear").getAsInt() : (existing != null ? existing.getGraduationYear() : 2026);
                int backlogs = reqObj.has("activeBacklogs") ? reqObj.get("activeBacklogs").getAsInt() : (existing != null ? existing.getActiveBacklogs() : 0);
                String skills = reqObj.has("skills") ? reqObj.get("skills").getAsString() : (existing != null ? existing.getSkills() : "");

                Student updatedStudent = new Student(studentId, targetUserId, branch, cgpa, gradYear, backlogs, skills);
                updatedStudent.setName(name);

                boolean success = studentDAO.updateStudentProfile(updatedStudent);
                if (success) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Student profile updated successfully!");
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to update profile.");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else if ("delete".equalsIgnoreCase(action) && "ADMIN".equalsIgnoreCase(user.getRole())) {
                int studentId = reqObj.get("studentId").getAsInt();
                boolean deleted = studentDAO.deleteStudent(studentId);
                if (deleted) {
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Student deleted successfully.");
                } else {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Failed to delete student.");
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
