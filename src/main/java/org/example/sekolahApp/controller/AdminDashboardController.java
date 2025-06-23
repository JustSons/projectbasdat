package org.example.sekolahApp.controller;

import org.example.sekolahApp.util.SceneManager;
import javafx.event.ActionEvent;
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

    // --- FXML Declarations ---
    @FXML private Button kelolaSiswaButton;
    @FXML private Button kelolaStaffButton;
    @FXML private Button pembagianKelasButton;
    @FXML private Button kelolaJadwalButton;
    @FXML private Button kelolaKelasButton;
    @FXML private Button kelolaTahunAjaranButton;
    @FXML private Button kelolaMapelButton;
    @FXML private Button naikKelasButton;
    @FXML private Button kelolaKelulusanButton;
    @FXML private Button cetakRaporButton;
    @FXML private Button masukkanNilaiButton;
    @FXML private Button logoutButton;
    @FXML private Label welcomeLabel;
    @FXML private Label menuTitleLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession session = UserSession.getInstance();

        String welcomeText = "Selamat Datang";
        if (session.isAdmin()) welcomeText += ", Admin!";
        else if (session.isGuru()) welcomeText += ", Guru " + session.getUsername() + "!";
        else if (session.isWaliKelas()) welcomeText += ", Wali Kelas " + session.getUsername() + "!";
        else if (session.isSiswa()) welcomeText += ", Siswa " + session.getUsername() + "!";
        else welcomeText += "!";
        welcomeLabel.setText(welcomeText);

        if (menuTitleLabel != null) menuTitleLabel.setText("Menu Utama");

        setVisibleForRole(session);
    }

    private void setVisibleForRole(UserSession session) {
        boolean isAdmin = session.isAdmin();
        boolean isGuru = session.isGuru();
        boolean isWaliKelas = session.isWaliKelas();


        kelolaSiswaButton.setVisible(false);
        kelolaStaffButton.setVisible(false);
        pembagianKelasButton.setVisible(false);
        kelolaJadwalButton.setVisible(false);
        kelolaKelasButton.setVisible(false);
        kelolaTahunAjaranButton.setVisible(false); // Sembunyikan secara default
        cetakRaporButton.setVisible(false);
        masukkanNilaiButton.setVisible(false);
        kelolaMapelButton.setVisible(false);
        naikKelasButton.setVisible(false);
        kelolaKelulusanButton.setVisible(false);

        if (isAdmin) {
            kelolaSiswaButton.setVisible(true);
            kelolaStaffButton.setVisible(true);
            pembagianKelasButton.setVisible(true);
            kelolaJadwalButton.setVisible(true);
            kelolaKelasButton.setVisible(true);
            kelolaTahunAjaranButton.setVisible(true);
            kelolaKelulusanButton.setVisible(true);
            naikKelasButton.setVisible(true);
            kelolaMapelButton.setVisible(true); // Admin bisa kelola tahun ajaran
        } else if (isGuru) {
            // Arahkan guru ke dashboard khusus guru
            try {
                SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/GuruDashboard.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Load Error", "Gagal membuka dashboard guru.");
            }
        } else if (isWaliKelas) {
            // Arahkan wali kelas ke dashboard khusus guru/wali kelas
            try {
                SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/GuruDashboard.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Load Error", "Gagal membuka dashboard wali kelas.");
            }
        }

    }

    // --- Event Handlers untuk Setiap Tombol ---

    @FXML
    private void handleKelolaKelulusan(ActionEvent event) {
        loadScene("/org/example/sekolahApp/view/KelolaKelulusan.fxml", "Kelola Kelulusan");
    }

    @FXML
    private void handleNaikKelasButton(ActionEvent event) {
        loadScene("/org/example/sekolahApp/view/ProsesNaikKelas.fxml", "Proses Naik Kelas");
    }

    @FXML
    private void handleKelolaMapelButton(ActionEvent event) {
        loadScene("/org/example/sekolahApp/view/kelola_matapelajaran.fxml", "Kelola Mata Pelajaran");
    }

    @FXML private void handleKelolaSiswa() { loadScene("/org/example/sekolahApp/view/KelolaSiswa.fxml", "Kelola Siswa"); }
    @FXML private void handleKelolaStaff() { loadScene("/org/example/sekolahApp/view/KelolaStaff.fxml", "Kelola Staff"); }
    @FXML private void handlePembagianKelas() { loadScene("/org/example/sekolahApp/view/PembagianKelas.fxml", "Pembagian Kelas"); }
    @FXML private void handleKelolaJadwal() { loadScene("/org/example/sekolahApp/view/KelolaJadwal.fxml", "Kelola Jadwal"); }
    @FXML private void handleKelolaKelas() { loadScene("/org/example/sekolahApp/view/KelolaKelas.fxml", "Kelola Kelas"); }
    @FXML private void handleKelolaTahunAjaran() { loadScene("/org/example/sekolahApp/view/KelolaTahunAjaran.fxml", "Kelola Tahun Ajaran"); }
    @FXML private void handleMasukkanNilai() { loadScene("/org/example/sekolahApp/view/InputNilai.fxml", "Input Nilai"); }
    @FXML private void handleCetakRapor() { loadScene("/org/example/sekolahApp/view/CetakRapor.fxml", "Cetak Rapor"); }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().cleanUserSession();
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/Login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper untuk memuat scene
    private void loadScene(String fxmlPath, String menuName) {
        try {
            SceneManager.getInstance().loadScene(fxmlPath);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Load Error", "Gagal membuka menu " + menuName + ".");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
