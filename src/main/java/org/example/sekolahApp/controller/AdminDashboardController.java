package org.example.sekolahApp.controller;

import org.example.sekolahApp.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.sekolahApp.util.UserSession;
import javafx.fxml.Initializable;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Button kelolaSiswaButton;
    @FXML private Button kelolaStaffButton;
    @FXML private Button pembagianKelasButton;
    @FXML private Button kelolaJadwalButton;
    @FXML private Button kelolaKelasButton; // Tombol untuk Kelola Kelas (Master)
    @FXML private Button kelolaTahunAjaranButton; // Tombol baru untuk Kelola Tahun Ajaran
    @FXML private Button cetakRaporButton;
    @FXML private Button masukkanNilaiButton;
    @FXML private Label welcomeLabel;
    @FXML private Label menuTitleLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession session = UserSession.getInstance();

        // Personalisasi Pesan Selamat Datang
        String welcomeText = "Selamat Datang";
        if (session.isAdmin()) {
            welcomeText += ", Admin!";
        } else if (session.isGuru()) {
            welcomeText += ", Guru " + session.getUsername() + "!";
        } else if (session.isWaliKelas()) {
            welcomeText += ", Wali Kelas " + session.getUsername() + "!";
        } else if (session.isSiswa()) {
            welcomeText += ", Siswa " + session.getUsername() + "!";
        } else {
            welcomeText += "!";
        }
        welcomeLabel.setText(welcomeText);

        if (menuTitleLabel != null) {
            menuTitleLabel.setText("Menu Utama");
        }

        // Kontrol Visibilitas Tombol
        kelolaSiswaButton.setVisible(false);
        kelolaStaffButton.setVisible(false);
        pembagianKelasButton.setVisible(false);
        kelolaJadwalButton.setVisible(false);
        kelolaKelasButton.setVisible(false);
        kelolaTahunAjaranButton.setVisible(false); // Sembunyikan secara default
        cetakRaporButton.setVisible(false);
        masukkanNilaiButton.setVisible(false);

        if (session.isAdmin()) {
            kelolaSiswaButton.setVisible(true);
            kelolaStaffButton.setVisible(true);
            pembagianKelasButton.setVisible(true);
            kelolaJadwalButton.setVisible(true);
            kelolaKelasButton.setVisible(true);
            kelolaTahunAjaranButton.setVisible(true); // Admin bisa kelola tahun ajaran
        } else if (session.isGuru()) {
            kelolaJadwalButton.setVisible(true);
            masukkanNilaiButton.setVisible(true);
        } else if (session.isWaliKelas()) {
            kelolaJadwalButton.setVisible(true);
            masukkanNilaiButton.setVisible(true);
            cetakRaporButton.setVisible(true);
        }
    }

    @FXML
    private void handleKelolaSiswa() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaSiswa.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Siswa.");
        }
    }

    @FXML
    private void handleKelolaStaff() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaStaff.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Staff.");
        }
    }

    @FXML
    private void handlePembagianKelas() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/PembagianKelas.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Pembagian Kelas.");
        }
    }

    @FXML
    private void handleKelolaJadwal() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaJadwal.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Jadwal.");
        }
    }

    @FXML
    private void handleKelolaKelas() { // Fungsi untuk tombol Kelola Kelas (Master)
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaKelas.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Kelas.");
        }
    }

    @FXML
    private void handleKelolaTahunAjaran() { // Fungsi baru untuk tombol Kelola Tahun Ajaran
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaTahunAjaran.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Kelola Tahun Ajaran.");
        }
    }

    @FXML
    private void handleMasukkanNilai() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/InputNilai.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Input Nilai.");
        }
    }

    @FXML
    private void handleCetakRapor() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/CetakRapor.fxml", 1024, 768);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka menu Cetak Rapor.");
        }
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