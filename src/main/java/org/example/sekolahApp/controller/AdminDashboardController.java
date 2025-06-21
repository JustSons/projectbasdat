package org.example.sekolahApp.controller;

import org.example.sekolahApp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.example.sekolahApp.util.UserSession;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private void handleKelolaSiswa() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaSiswa.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Siswa.");
        }
    }

    @FXML private void handleKelolaStaff() {
        // SceneManager.getInstance().loadScene(".../KelolaStaff.fxml");
    }

    @FXML private void handlePembagianKelas() {
        // SceneManager.getInstance().loadScene(".../PembagianKelas.fxml");
    }

    @FXML
    private void handleKelolaGuru() {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Kelola Guru belum tersedia.");
    }

    @FXML
    private void handleKelolaKelas() {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fitur Kelola Kelas belum tersedia.");
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().cleanUserSession();
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/Login.fxml", 500, 400);
        } catch (IOException e) {
            e.printStackTrace();
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