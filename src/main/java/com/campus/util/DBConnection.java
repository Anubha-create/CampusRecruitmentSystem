package com.campus.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class to manage JDBC Database Connections to MySQL campus_recruitment_db.
 */
public class DBConnection {

    private static String URL = "jdbc:mysql://localhost:3306/campus_recruitment_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private static String USER = "root";
    private static String PASSWORD = "1234"; // Default password on local MySQL Server

    static {
        // Load properties from classpath if present
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                if (prop.getProperty("db.url") != null) URL = prop.getProperty("db.url");
                if (prop.getProperty("db.user") != null) USER = prop.getProperty("db.user");
                if (prop.getProperty("db.password") != null) PASSWORD = prop.getProperty("db.password");
            }
        } catch (Exception e) {
            System.out.println("[DBConnection] db.properties not found, using default credentials.");
        }

        // Allow overriding password/user via Environment variables or System Properties
        if (System.getenv("MYSQL_PASSWORD") != null) {
            PASSWORD = System.getenv("MYSQL_PASSWORD");
        }
        if (System.getProperty("db.password") != null) {
            PASSWORD = System.getProperty("db.password");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("[DBConnection] MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL Driver Class Not Found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new java.sql.Connection instance.
     * @return Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DBConnection Error] Failed to connect to MySQL database at " + URL);
            System.err.println("[DBConnection Error] Reason: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Utility helper method to safely close open Connection, PreparedStatement, or ResultSet
     */
    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
