package org.example.sekolahApp.controller;

import com.yourcompany.sekolahapp.db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText(); // Seharusnya di-hash sebelum dibandingkan

        Connection conn = DatabaseConnection.getConnection();
        // Ganti query ini dengan logic yang lebih aman (prepared statement dan password hashing)
        String sql = "SELECT role FROM users WHERE username = ? AND password_hash = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // TODO: Hash password input sebelum membandingkan

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                statusLabel.setText("Login berhasil sebagai " + role);
                // TODO: Pindah ke scene yang sesuai dengan role (Admin/Guru/Siswa)
                // Contoh: SceneManager.getInstance().loadScene(role + "_dashboard.fxml");
            } else {
                statusLabel.setText("Username atau password salah.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error: Gagal terhubung ke database.");
        }
    }
}