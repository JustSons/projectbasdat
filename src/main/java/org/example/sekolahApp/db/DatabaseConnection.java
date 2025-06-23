package org.example.sekolahApp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager
 * 
 * SCHEMA MIGRATION NOTES:
 * - Current project uses Indonesian table names (siswa, kelas, staff, etc.)
 * - New schema uses English table names (STUDENT, CLASS, STAFF, etc.)
 * - This class is prepared to handle both schemas during migration
 */
public class DatabaseConnection {
    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/project_bd";
    private static final String USER = "postgres"; // User default pgAdmin
    private static final String PASSWORD = "12345"; // Ganti dengan password Anda

    /**
     * Get a new database connection instance.
     * Each call creates a new connection to avoid connection sharing issues.
     * 
     * @return Active Connection object
     * @throws SQLException If connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found! Ensure JAR is in classpath.");
            throw new SQLException("JDBC Driver not found", e);
        }
        
        // Create and return new connection
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("PostgreSQL connection established successfully.");
        return conn;
    }

    /**
     * Check if a table exists in the database.
     * Useful for detecting which schema is being used.
     * 
     * @param tableName Name of the table to check
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(String tableName) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT 1 FROM information_schema.tables WHERE table_name = ? LIMIT 1";
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tableName.toLowerCase());
                try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Detect which schema is currently being used.
     * 
     * @return "NEW" if new schema (STUDENT, CLASS, etc.), "OLD" if old schema (siswa, kelas, etc.)
     */
    public static String detectSchema() {
        if (tableExists("STUDENT") && tableExists("CLASS") && tableExists("STAFF")) {
            return "NEW";
        } else if (tableExists("siswa") && tableExists("kelas") && tableExists("staff")) {
            return "OLD";
        } else {
            return "UNKNOWN";
        }
    }
}