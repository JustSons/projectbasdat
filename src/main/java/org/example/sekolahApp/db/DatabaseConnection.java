package org.example.sekolahApp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Hapus komentar tentang Singleton, karena kita tidak lagi mengimplementasikannya sebagai Singleton untuk koneksi tunggal
public class DatabaseConnection {
    // Ganti dengan detail koneksi database Anda
    private static final String URL = "jdbc:postgresql://localhost:5432/project_bd";
    private static final String USER = "postgres"; // User default pgAdmin
    private static final String PASSWORD = "postgres"; // Ganti dengan password Anda

    // Hapus baris ini: private static Connection connection = null;
    // Hapus juga private constructor DatabaseConnection() {}

    /**
     * Mendapatkan instance koneksi database yang baru.
     * Setiap kali method ini dipanggil, koneksi baru akan dibuat.
     * @return Objek Connection yang aktif.
     * @throws SQLException Jika terjadi kesalahan saat membuat koneksi.
     */
    public static Connection getConnection() throws SQLException { // Tambahkan throws SQLException
        // Load driver JDBC PostgreSQL (ini hanya perlu dilakukan sekali, tapi aman jika di sini)
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL tidak ditemukan! Pastikan JAR JDBC ada di classpath.");
            throw new SQLException("Driver JDBC tidak ditemukan", e); // Re-throw sebagai SQLException
        }
        // Buat dan kembalikan koneksi baru setiap kali method ini dipanggil
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Koneksi ke PostgreSQL berhasil dibuat."); // Sesuaikan pesan
        return conn;
    }

    /**
     * Metode ini TIDAK LAGI DIBUTUHKAN jika menggunakan try-with-resources
     * dan getConnection() mengembalikan koneksi baru setiap kali.
     * Jika Anda tetap ingin menggunakannya untuk hal lain, biarkan saja,
     * tapi jangan memanggilnya untuk koneksi yang diambil dengan getConnection() di controller.
     */
    // public static void closeConnection() {
    //     // Baris ini tidak lagi relevan dalam konteks getConnection() yang baru.
    //     // Koneksi ditutup secara otomatis oleh try-with-resources.
    // }
}