package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class GuruDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label namaGuruLabel;
    @FXML private Label nipLabel;
    @FXML private Label jabatanLabel;

    // Tabel Jadwal Mengajar
    @FXML private TableView<JadwalGuruView> jadwalTableView;
    @FXML private TableColumn<JadwalGuruView, String> kelasColumn;
    @FXML private TableColumn<JadwalGuruView, String> hariColumn;
    @FXML private TableColumn<JadwalGuruView, String> jamMulaiColumn;
    @FXML private TableColumn<JadwalGuruView, String> jamSelesaiColumn;
    @FXML private TableColumn<JadwalGuruView, String> mapelColumn;
    @FXML private TableColumn<JadwalGuruView, String> tahunAjaranColumn;

    private final ObservableList<JadwalGuruView> jadwalList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cek apakah ada user yang login dan role guru
        if (!UserSession.getInstance().isLoggedIn() || 
            (!UserSession.getInstance().isGuru() && !UserSession.getInstance().isWaliKelas())) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Halaman ini hanya untuk guru.");
            return;
        }

        int staffId = UserSession.getInstance().getReferenceId();

        // Setup kolom tabel
        setupTableColumns();

        // Memuat data
        loadBiodataGuru(staffId);
        loadJadwalMengajar(staffId);
    }

    private void setupTableColumns() {
        kelasColumn.setCellValueFactory(new PropertyValueFactory<>("namaKelas"));
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        jamSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        tahunAjaranColumn.setCellValueFactory(new PropertyValueFactory<>("tahunAjaran"));

        jadwalTableView.setItems(jadwalList);
    }

    private void loadBiodataGuru(int staffId) {
        String sql = "SELECT nama_staff, nip, jabatan FROM staff WHERE staff_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String namaGuru = rs.getString("nama_staff");
                String nip = rs.getString("nip");
                String jabatan = rs.getString("jabatan");

                welcomeLabel.setText("Selamat Datang, " + namaGuru);
                namaGuruLabel.setText(namaGuru);
                nipLabel.setText("NIP: " + (nip != null ? nip : "-"));
                jabatanLabel.setText("Jabatan: " + jabatan);
            } else {
                welcomeLabel.setText("Data guru tidak ditemukan");
                showAlert(Alert.AlertType.WARNING, "Data Tidak Ditemukan", "Data guru tidak ditemukan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat biodata guru.");
        }
    }

    private void loadJadwalMengajar(int staffId) {
        jadwalList.clear();
        
        String sql = "SELECT k.nama_kelas, j.hari, j.jam_mulai, j.jam_selesai, " +
                     "mp.nama_mapel, ta.tahun_ajaran " +
                     "FROM jadwal j " +
                     "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                     "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                     "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                     "WHERE j.guru_id = ? " +
                     "ORDER BY ta.tahun_ajaran DESC, " +
                     "CASE j.hari " +
                     "    WHEN 'Senin' THEN 1 " +
                     "    WHEN 'Selasa' THEN 2 " +
                     "    WHEN 'Rabu' THEN 3 " +
                     "    WHEN 'Kamis' THEN 4 " +
                     "    WHEN 'Jumat' THEN 5 " +
                     "    WHEN 'Sabtu' THEN 6 " +
                     "    WHEN 'Minggu' THEN 7 " +
                     "    ELSE 8 END, j.jam_mulai";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String namaKelas = rs.getString("nama_kelas");
                String hari = rs.getString("hari");
                LocalTime jamMulai = rs.getTime("jam_mulai").toLocalTime();
                LocalTime jamSelesai = rs.getTime("jam_selesai").toLocalTime();
                String namaMapel = rs.getString("nama_mapel");
                String tahunAjaran = rs.getString("tahun_ajaran");

                jadwalList.add(new JadwalGuruView(namaKelas, hari, jamMulai.toString(), 
                                                jamSelesai.toString(), namaMapel, tahunAjaran));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat jadwal mengajar.");
        }
    }

    @FXML
    private void handleInputNilai() {
        // Arahkan ke halaman input nilai
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/InputNilai.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman input nilai.");
        }
    }

    @FXML
    private void handleCetakRapor() {
        // Jika guru adalah wali kelas, boleh cetak rapor
        if (UserSession.getInstance().isWaliKelas()) {
            try {
                SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/CetakRapor.fxml");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman cetak rapor.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Akses Ditolak", "Fitur cetak rapor hanya untuk wali kelas.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().cleanUserSession();
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/Login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal logout.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class untuk view jadwal guru
    public static class JadwalGuruView {
        private final String namaKelas;
        private final String hari;
        private final String jamMulai;
        private final String jamSelesai;
        private final String namaMapel;
        private final String tahunAjaran;

        public JadwalGuruView(String namaKelas, String hari, String jamMulai, String jamSelesai, 
                            String namaMapel, String tahunAjaran) {
            this.namaKelas = namaKelas;
            this.hari = hari;
            this.jamMulai = jamMulai;
            this.jamSelesai = jamSelesai;
            this.namaMapel = namaMapel;
            this.tahunAjaran = tahunAjaran;
        }

        public String getNamaKelas() { return namaKelas; }
        public String getHari() { return hari; }
        public String getJamMulai() { return jamMulai; }
        public String getJamSelesai() { return jamSelesai; }
        public String getNamaMapel() { return namaMapel; }
        public String getTahunAjaran() { return tahunAjaran; }
    }
}
