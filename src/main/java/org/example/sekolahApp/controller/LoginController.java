package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.PasswordUtil;
import org.example.sekolahApp.util.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    protected void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText(); // Ini password mentah dari input
        String hashedPassword = PasswordUtil.hashPassword(password); // Hash password input

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username dan password tidak boleh kosong.");
            return;
        }

        // Ambil semua data yang diperlukan: user_id, role, reference_id, dan cari wali_kelas_id jika guru/wali_kelas
        String sql = "SELECT user_id, role, reference_id FROM users WHERE username = ? AND password_hash = ?";

        Connection conn = null; // Deklarasi di luar try
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword); // Gunakan hashedpassword);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int userId = rs.getInt("user_id");
                        String role = rs.getString("role");
                        int referenceId = rs.getInt("reference_id");
                        Integer waliKelasId = null; // Default null

                        // Jika user adalah guru atau wali_kelas, cek apakah dia wali kelas
                        if ("guru".equalsIgnoreCase(role) || "wali_kelas".equalsIgnoreCase(role)) {
                            String checkWaliKelasSql = "SELECT kelas_id FROM kelas WHERE wali_kelas_id = ?";
                            try (PreparedStatement pstmtWali = conn.prepareStatement(checkWaliKelasSql)) {
                                pstmtWali.setInt(1, referenceId); // reference_id user guru/wali_kelas adalah staff_id
                                try (ResultSet rsWali = pstmtWali.executeQuery()) {
                                    if (rsWali.next()) {
                                        waliKelasId = rsWali.getInt("kelas_id"); // Dapatkan ID kelas yang diwali
                                        // Jika ditemukan, pastikan role di session adalah 'wali_kelas'
                                        // agar UI bisa merespons
                                        role = "wali_kelas"; // Override role menjadi wali_kelas jika dia memang wali
                                    }
                                }
                            }
                        }

                        statusLabel.setText("Login berhasil sebagai " + role);
                        // Buat session dengan informasi waliKelasId
                        // BARIS BARU YANG BENAR:
                        UserSession.createSession(userId, username, role, referenceId, waliKelasId);
                        navigateToDashboard(role);

                    } else {
                        showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
                        statusLabel.setText("Login gagal.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal terhubung ke database.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "Gagal memuat halaman dashboard.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void navigateToDashboard(String role) throws IOException {
        // Logika navigasi ini akan lebih baik jika ada satu dashboard controller untuk semua role
        // yang kemudian memuat konten dinamis berdasarkan role, atau mengarahkan ke dashboard khusus.
        // Untuk saat ini, kita bisa arahkan ke admin dashboard dan di sana menonaktifkan menu.
        // Atau buat dashboard khusus guru/wali kelas jika UX-nya sangat berbeda.

        // Jika ada dashboard khusus:
        if ("siswa".equalsIgnoreCase(role)) {
            // SceneManager.getInstance().loadScene("/org/example.sekolahApp/view/SiswaDashboard.fxml", 800, 600);
            showAlert(Alert.AlertType.INFORMATION, "Info", "Dashboard Siswa belum diimplementasikan.");
        } else if ("guru".equalsIgnoreCase(role) || "wali_kelas".equalsIgnoreCase(role)) {
            // SceneManager.getInstance().loadScene("/org/example.sekolahApp/view/GuruWaliKelasDashboard.fxml", 1024, 768);
            // Untuk sementara, tetap arahkan ke admin dashboard dan sesuaikan menu di sana
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml");
        } else { // Admin
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}