package com.campus.dao;

import com.campus.model.JobDrive;
import com.campus.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for managing placement Job Drives and recruitment criteria.
 */
public class JobDriveDAO {

    /**
     * Creates a new job drive.
     */
    public boolean addJobDrive(JobDrive jobDrive) {
        String sql = "INSERT INTO job_drives (company_id, job_role, package, minimum_cgpa, eligible_branches, maximum_backlogs, required_skills, job_description, application_deadline) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, jobDrive.getCompanyId());
            pstmt.setString(2, jobDrive.getJobRole());
            pstmt.setString(3, jobDrive.getPackageAmount());
            pstmt.setDouble(4, jobDrive.getMinimumCgpa());
            pstmt.setString(5, jobDrive.getEligibleBranches());
            pstmt.setInt(6, jobDrive.getMaximumBacklogs());
            pstmt.setString(7, jobDrive.getRequiredSkills());
            pstmt.setString(8, jobDrive.getJobDescription());
            pstmt.setDate(9, jobDrive.getApplicationDeadline());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Add job drive failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Retrieves Job Drive details by job ID.
     */
    public JobDrive getJobDriveById(int jobId) {
        String sql = "SELECT j.*, c.company_name FROM job_drives j JOIN companies c ON j.company_id = c.company_id WHERE j.job_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, jobId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractJobDriveFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Get job drive by ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Retrieves all job drives for a specific company ID.
     */
    public List<JobDrive> getJobDrivesByCompanyId(int companyId) {
        List<JobDrive> list = new ArrayList<>();
        String sql = "SELECT j.*, c.company_name FROM job_drives j JOIN companies c ON j.company_id = c.company_id " +
                     "WHERE j.company_id = ? ORDER BY j.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, companyId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractJobDriveFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Get job drives by company ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Retrieves all job drives in the system.
     */
    public List<JobDrive> getAllJobDrives() {
        List<JobDrive> list = new ArrayList<>();
        String sql = "SELECT j.*, c.company_name FROM job_drives j JOIN companies c ON j.company_id = c.company_id " +
                     "ORDER BY j.created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractJobDriveFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Get all job drives failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Updates an existing job drive.
     */
    public boolean updateJobDrive(JobDrive jobDrive) {
        String sql = "UPDATE job_drives SET job_role = ?, package = ?, minimum_cgpa = ?, eligible_branches = ?, " +
                     "maximum_backlogs = ?, required_skills = ?, job_description = ?, application_deadline = ? " +
                     "WHERE job_id = ? AND company_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, jobDrive.getJobRole());
            pstmt.setString(2, jobDrive.getPackageAmount());
            pstmt.setDouble(3, jobDrive.getMinimumCgpa());
            pstmt.setString(4, jobDrive.getEligibleBranches());
            pstmt.setInt(5, jobDrive.getMaximumBacklogs());
            pstmt.setString(6, jobDrive.getRequiredSkills());
            pstmt.setString(7, jobDrive.getJobDescription());
            pstmt.setDate(8, jobDrive.getApplicationDeadline());
            pstmt.setInt(9, jobDrive.getJobId());
            pstmt.setInt(10, jobDrive.getCompanyId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Update job drive failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Deletes a job drive.
     */
    public boolean deleteJobDrive(int jobId) {
        String sql = "DELETE FROM job_drives WHERE job_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, jobId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[JobDriveDAO Error] Delete job drive failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    private JobDrive extractJobDriveFromResultSet(ResultSet rs) throws SQLException {
        JobDrive j = new JobDrive();
        j.setJobId(rs.getInt("job_id"));
        j.setCompanyId(rs.getInt("company_id"));
        j.setJobRole(rs.getString("job_role"));
        j.setPackageAmount(rs.getString("package"));
        j.setMinimumCgpa(rs.getDouble("minimum_cgpa"));
        j.setEligibleBranches(rs.getString("eligible_branches"));
        j.setMaximumBacklogs(rs.getInt("maximum_backlogs"));
        j.setRequiredSkills(rs.getString("required_skills"));
        j.setJobDescription(rs.getString("job_description"));
        j.setApplicationDeadline(rs.getDate("application_deadline"));
        j.setCreatedAt(rs.getTimestamp("created_at"));
        j.setCompanyName(rs.getString("company_name"));
        return j;
    }
}
