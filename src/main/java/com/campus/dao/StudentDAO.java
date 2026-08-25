package com.campus.dao;

import com.campus.model.Student;
import com.campus.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student profile management and candidate search.
 */
public class StudentDAO {

    /**
     * Adds a student profile associated with a user ID.
     */
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (user_id, branch, cgpa, graduation_year, active_backlogs, skills) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, student.getUserId());
            pstmt.setString(2, student.getBranch());
            pstmt.setDouble(3, student.getCgpa());
            pstmt.setInt(4, student.getGraduationYear());
            pstmt.setInt(5, student.getActiveBacklogs());
            pstmt.setString(6, student.getSkills());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StudentDAO Error] Add student failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt, conn);
        }
        return false;
    }

    /**
     * Retrieves student profile by User ID (including joined user details).
     */
    public Student getStudentByUserId(int userId) {
        String sql = "SELECT s.*, u.name, u.email FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO Error] Get student by user ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Retrieves student profile by Student ID.
     */
    public Student getStudentByStudentId(int studentId) {
        String sql = "SELECT s.*, u.name, u.email FROM students s JOIN users u ON s.user_id = u.user_id WHERE s.student_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractStudentFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO Error] Get student by student ID failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return null;
    }

    /**
     * Updates student profile details and user name.
     */
    public boolean updateStudentProfile(Student student) {
        String updateStudentSql = "UPDATE students SET branch = ?, cgpa = ?, graduation_year = ?, active_backlogs = ?, skills = ? WHERE student_id = ?";
        String updateUserSql = "UPDATE users SET name = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            pstmt1 = conn.prepareStatement(updateStudentSql);
            pstmt1.setString(1, student.getBranch());
            pstmt1.setDouble(2, student.getCgpa());
            pstmt1.setInt(3, student.getGraduationYear());
            pstmt1.setInt(4, student.getActiveBacklogs());
            pstmt1.setString(5, student.getSkills());
            pstmt1.setInt(6, student.getStudentId());
            pstmt1.executeUpdate();

            pstmt2 = conn.prepareStatement(updateUserSql);
            pstmt2.setString(1, student.getName());
            pstmt2.setInt(2, student.getUserId());
            pstmt2.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("[StudentDAO Error] Update student failed: " + e.getMessage());
        } finally {
            DBConnection.close(pstmt1, pstmt2, conn);
        }
        return false;
    }

    /**
     * Retrieves all registered students.
     */
    public List<Student> getAllStudents() {
        return searchStudents("");
    }

    /**
     * Searches students by name, email, branch, or skills.
     */
    public List<Student> searchStudents(String query) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, u.name, u.email FROM students s JOIN users u ON s.user_id = u.user_id " +
                     "WHERE u.name LIKE ? OR u.email LIKE ? OR s.branch LIKE ? OR s.skills LIKE ? " +
                     "ORDER BY s.student_id DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            String term = "%" + (query == null ? "" : query.trim()) + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            pstmt.setString(4, term);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StudentDAO Error] Search students failed: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, conn);
        }
        return list;
    }

    /**
     * Deletes student profile and underlying user account.
     */
    public boolean deleteStudent(int studentId) {
        Student student = getStudentByStudentId(studentId);
        if (student == null) return false;

        UserDAO userDAO = new UserDAO();
        return userDAO.deleteUser(student.getUserId());
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setBranch(rs.getString("branch"));
        s.setCgpa(rs.getDouble("cgpa"));
        s.setGraduationYear(rs.getInt("graduation_year"));
        s.setActiveBacklogs(rs.getInt("active_backlogs"));
        s.setSkills(rs.getString("skills"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        return s;
    }
}
