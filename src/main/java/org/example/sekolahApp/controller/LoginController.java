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
        String password_hash = passwordField.getText();
        String hashedPassword = PasswordUtil.hashPassword(password_hash);

        if (username.isEmpty() || password_hash.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username dan password tidak boleh kosong.");
            return;
        }

        // TODO: Implement password hashing. Untuk saat ini, password disimpan sebagai plain text.
        // Dalam produksi, password harus di-hash. Contoh: String hashedPassword = hashFunction(password);
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password_hash); // Seharusnya `hashedPassword`

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                statusLabel.setText("Login berhasil sebagai " + role);
                UserSession.getInstance().createSession(rs.getInt("user_id"), username, role, rs.getInt("reference_id"));
                navigateToDashboard(role);

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username atau password salah.");
                statusLabel.setText("Login gagal.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal terhubung ke database.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "System Error", "Gagal memuat halaman dashboard.");
        }
    }

    private void navigateToDashboard(String role) throws IOException {
        switch (role.toLowerCase()) {
            case "admin":
                SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml", 800, 600);
                break;
            case "guru":
                // SceneManager.getInstance().loadScene(".../GuruDashboard.fxml");
                showAlert(Alert.AlertType.INFORMATION, "Info", "Dashboard Guru belum diimplementasikan.");
                break;
            case "siswa":
                // SceneManager.getInstance().loadScene(".../SiswaDashboard.fxml");
                showAlert(Alert.AlertType.INFORMATION, "Info", "Dashboard Siswa belum diimplementasikan.");
                break;
            default:
                showAlert(Alert.AlertType.WARNING, "Peran Tidak Dikenal", "Peran pengguna tidak valid.");
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