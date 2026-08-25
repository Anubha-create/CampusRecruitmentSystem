package com.campus.dao;

import com.campus.model.Company;
import com.campus.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Company profile management.
 */
public class CompanyDAO {

    /**
     * Adds a company profile for a user ID.
     */
    public boolean addCompany(Company company) {
        String sql = "INSERT INTO companies (user_id, company_name, company_description) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, company.getUserId());
            pstmt.setString(2, company.getCompanyName());
            pstmt.setString(3, company.getCompanyDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CompanyDAO Error] Add company failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Retrieves company profile by User ID.
     */
    public Company getCompanyByUserId(int userId) {
        String sql = "SELECT c.*, u.email FROM companies c JOIN users u ON c.user_id = u.user_id WHERE c.user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractCompanyFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CompanyDAO Error] Get company by user ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Retrieves company profile by Company ID.
     */
    public Company getCompanyByCompanyId(int companyId) {
        String sql = "SELECT c.*, u.email FROM companies c JOIN users u ON c.user_id = u.user_id WHERE c.company_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, companyId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractCompanyFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CompanyDAO Error] Get company by ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Updates company name and description.
     */
    public boolean updateCompany(Company company) {
        String sql = "UPDATE companies SET company_name = ?, company_description = ? WHERE company_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, company.getCompanyName());
            pstmt.setString(2, company.getCompanyDescription());
            pstmt.setInt(3, company.getCompanyId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CompanyDAO Error] Update company failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Retrieves all company profiles.
     */
    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<>();
        String sql = "SELECT c.*, u.email FROM companies c JOIN users u ON c.user_id = u.user_id ORDER BY c.company_name ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(extractCompanyFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CompanyDAO Error] Get all companies failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Deletes company profile and user account.
     */
    public boolean deleteCompany(int companyId) {
        Company company = getCompanyByCompanyId(companyId);
        if (company == null) return false;

        UserDAO userDAO = new UserDAO();
        return userDAO.deleteUser(company.getUserId());
    }

    private Company extractCompanyFromResultSet(ResultSet rs) throws SQLException {
        Company c = new Company();
        c.setCompanyId(rs.getInt("company_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setCompanyName(rs.getString("company_name"));
        c.setCompanyDescription(rs.getString("company_description"));
        c.setEmail(rs.getString("email"));
        return c;
    }
}
