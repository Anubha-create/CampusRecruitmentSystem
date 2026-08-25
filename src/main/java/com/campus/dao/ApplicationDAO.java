package com.campus.dao;

import com.campus.model.Application;
import com.campus.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for handling student job applications, status updates, and stats reporting.
 */
public class ApplicationDAO {

    /**
     * Submits a student job application.
     */
    public boolean applyForJob(int studentId, int jobId) {
        String sql = "INSERT INTO applications (student_id, job_id, application_status) VALUES (?, ?, 'Applied')";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, jobId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Apply for job failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Checks if a student has already applied for a specific job drive.
     */
    public boolean hasApplied(int studentId, int jobId) {
        String sql = "SELECT application_id FROM applications WHERE student_id = ? AND job_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, jobId);
            rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Duplicate check failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return false;
    }

    /**
     * Retrieves application history for a specific student.
     */
    public List<Application> getApplicationsByStudent(int studentId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, j.job_role, j.package, c.company_name " +
                     "FROM applications a " +
                     "JOIN job_drives j ON a.job_id = j.job_id " +
                     "JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE a.student_id = ? ORDER BY a.application_date DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Application app = new Application();
                app.setApplicationId(rs.getInt("application_id"));
                app.setStudentId(rs.getInt("student_id"));
                app.setJobId(rs.getInt("job_id"));
                app.setApplicationDate(rs.getTimestamp("application_date"));
                app.setApplicationStatus(rs.getString("application_status"));
                app.setJobRole(rs.getString("job_role"));
                app.setPackageAmount(rs.getString("package"));
                app.setCompanyName(rs.getString("company_name"));
                list.add(app);
            }
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Get student applications failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all applications submitted for a specific job drive.
     */
    public List<Application> getApplicationsByJob(int jobId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as student_name, u.email as student_email, s.branch, s.cgpa, s.active_backlogs, " +
                     "j.job_role, j.package, c.company_name " +
                     "FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "JOIN job_drives j ON a.job_id = j.job_id " +
                     "JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE a.job_id = ? ORDER BY a.application_date DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, jobId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractDetailedApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Get job applications failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all applications submitted across all job drives owned by a specific company.
     */
    public List<Application> getApplicationsByCompany(int companyId) {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, u.name as student_name, u.email as student_email, s.branch, s.cgpa, s.active_backlogs, " +
                     "j.job_role, j.package, c.company_name " +
                     "FROM applications a " +
                     "JOIN students s ON a.student_id = s.student_id " +
                     "JOIN users u ON s.user_id = u.user_id " +
                     "JOIN job_drives j ON a.job_id = j.job_id " +
                     "JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE c.company_id = ? ORDER BY a.application_date DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, companyId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractDetailedApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Get company applications failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all applications with multi-field search & filtering (Admin Dashboard).
     */
    public List<Application> getAllApplications(String companyFilter, String roleFilter, String studentFilter, String statusFilter) {
        List<Application> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, u.name as student_name, u.email as student_email, s.branch, s.cgpa, s.active_backlogs, " +
            "j.job_role, j.package, c.company_name " +
            "FROM applications a " +
            "JOIN students s ON a.student_id = s.student_id " +
            "JOIN users u ON s.user_id = u.user_id " +
            "JOIN job_drives j ON a.job_id = j.job_id " +
            "JOIN companies c ON j.company_id = c.company_id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (companyFilter != null && !companyFilter.trim().isEmpty()) {
            sql.append("AND c.company_name LIKE ? ");
            params.add("%" + companyFilter.trim() + "%");
        }
        if (roleFilter != null && !roleFilter.trim().isEmpty()) {
            sql.append("AND j.job_role LIKE ? ");
            params.add("%" + roleFilter.trim() + "%");
        }
        if (studentFilter != null && !studentFilter.trim().isEmpty()) {
            sql.append("AND (u.name LIKE ? OR u.email LIKE ?) ");
            params.add("%" + studentFilter.trim() + "%");
            params.add("%" + studentFilter.trim() + "%");
        }
        if (statusFilter != null && !statusFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append("AND a.application_status = ? ");
            params.add(statusFilter.trim());
        }

        sql.append("ORDER BY a.application_date DESC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractDetailedApplicationFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Filter all applications failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Updates candidate application pipeline status ('Applied', 'Shortlisted', 'Aptitude Test', 'Interview', 'Selected', 'Rejected').
     */
    public boolean updateApplicationStatus(int applicationId, String newStatus) {
        String sql = "UPDATE applications SET application_status = ? WHERE application_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, applicationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Update application status failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Generates summary statistics map for Admin & Placement Officer dashboard.
     */
    public Map<String, Integer> getPlacementStats() {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
            if (rs.next()) stats.put("totalStudents", rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM companies");
            if (rs.next()) stats.put("totalCompanies", rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM job_drives");
            if (rs.next()) stats.put("totalJobDrives", rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM applications");
            if (rs.next()) stats.put("totalApplications", rs.getInt(1));
            rs.close();

            rs = stmt.executeQuery("SELECT COUNT(*) FROM applications WHERE application_status = 'Selected'");
            if (rs.next()) stats.put("totalSelectedStudents", rs.getInt(1));
            rs.close();

        } catch (SQLException e) {
            System.err.println("[ApplicationDAO Error] Get placement stats failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, stmt, conn);
        }
        return stats;
    }

    private Application extractDetailedApplicationFromResultSet(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getInt("application_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setJobId(rs.getInt("job_id"));
        a.setApplicationDate(rs.getTimestamp("application_date"));
        a.setApplicationStatus(rs.getString("application_status"));

        a.setStudentName(rs.getString("student_name"));
        a.setStudentEmail(rs.getString("student_email"));
        a.setStudentBranch(rs.getString("branch"));
        a.setStudentCgpa(rs.getDouble("cgpa"));
        a.setStudentBacklogs(rs.getInt("active_backlogs"));

        a.setJobRole(rs.getString("job_role"));
        a.setPackageAmount(rs.getString("package"));
        a.setCompanyName(rs.getString("company_name"));
        return a;
    }
}
