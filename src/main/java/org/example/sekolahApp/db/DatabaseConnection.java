// File: src/main/java/com/yourcompany/sekolahapp/db/DatabaseConnection.java
package org.example.sekolahApp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Kelas Singleton untuk mengelola koneksi ke database PostgreSQL.
 */
public class DatabaseConnection {
    // Ganti dengan detail koneksi database Anda
    private static final String URL = "jdbc:postgresql://localhost:5432/project_bd";
    private static final String USER = "postgres"; // User default pgAdmin
    private static final String PASSWORD = "1234"; // Ganti dengan password Anda

    private static Connection connection = null;

    // Private constructor untuk mencegah instansiasi dari luar
    private DatabaseConnection() {}

    /**
     * Mendapatkan instance koneksi database.
     * @return Objek Connection yang aktif.
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load driver JDBC PostgreSQL
                Class.forName("org.postgresql.Driver");
                // Buat koneksi
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi ke PostgreSQL berhasil!");
            } catch (SQLException e) {
                System.err.println("Koneksi gagal: " + e.getMessage());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                System.err.println("Driver PostgreSQL tidak ditemukan!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Menutup koneksi database.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Koneksi ditutup.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}