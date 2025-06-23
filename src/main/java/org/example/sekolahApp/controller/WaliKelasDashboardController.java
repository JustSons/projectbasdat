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
import java.util.ResourceBundle;

public class WaliKelasDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label namaWaliLabel;
    @FXML private Label kelasWaliLabel;
    @FXML private Label jumlahSiswaLabel;

    // Tabel Daftar Siswa di Kelas
    @FXML private TableView<SiswaWaliView> siswaTableView;
    @FXML private TableColumn<SiswaWaliView, String> nisColumn;
    @FXML private TableColumn<SiswaWaliView, String> namaColumn;
    @FXML private TableColumn<SiswaWaliView, String> jenisKelaminColumn;
    @FXML private TableColumn<SiswaWaliView, String> statusColumn;

    // Tabel Jadwal Kelas
    @FXML private TableView<JadwalKelasView> jadwalTableView;
    @FXML private TableColumn<JadwalKelasView, String> hariColumn;
    @FXML private TableColumn<JadwalKelasView, String> jamColumn;
    @FXML private TableColumn<JadwalKelasView, String> mapelColumn;
    @FXML private TableColumn<JadwalKelasView, String> guruColumn;

    private final ObservableList<SiswaWaliView> siswaList = FXCollections.observableArrayList();
    private final ObservableList<JadwalKelasView> jadwalList = FXCollections.observableArrayList();

    private int kelasId = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cek apakah user adalah wali kelas
        if (!UserSession.getInstance().isLoggedIn() || !UserSession.getInstance().isWaliKelas()) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Halaman ini hanya untuk wali kelas.");
            return;
        }

        setupTableColumns();
        loadWaliKelasInfo();
        
        if (kelasId != -1) {
            loadSiswaDiKelas();
            loadJadwalKelas();
        }
    }

    private void setupTableColumns() {
        // Setup kolom tabel siswa
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        jenisKelaminColumn.setCellValueFactory(new PropertyValueFactory<>("jenisKelamin"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        siswaTableView.setItems(siswaList);

        // Setup kolom tabel jadwal
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamColumn.setCellValueFactory(new PropertyValueFactory<>("jam"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapel"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("guru"));
        jadwalTableView.setItems(jadwalList);
    }

    private void loadWaliKelasInfo() {
        int staffId = UserSession.getInstance().getReferenceId();

        String sql = "SELECT s.nama_staff, k.kelas_id, k.nama_kelas, ta.tahun_ajaran " +
                     "FROM staff s " +
                     "JOIN kelas k ON s.staff_id = k.wali_kelas_id " +
                     "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                     "WHERE s.staff_id = ? AND ta.status = 'aktif'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String namaWali = rs.getString("nama_staff");
                kelasId = rs.getInt("kelas_id");
                String namaKelas = rs.getString("nama_kelas");
                String tahunAjaran = rs.getString("tahun_ajaran");

                welcomeLabel.setText("Selamat Datang, Wali Kelas " + namaWali);
                namaWaliLabel.setText(namaWali);
                kelasWaliLabel.setText(namaKelas + " (" + tahunAjaran + ")");

                // Hitung jumlah siswa
                loadJumlahSiswa();

            } else {
                welcomeLabel.setText("Anda belum ditugaskan sebagai wali kelas");
                showAlert(Alert.AlertType.INFORMATION, "Info", "Anda belum ditugaskan sebagai wali kelas dari kelas manapun.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat informasi wali kelas.");
        }
    }

    private void loadJumlahSiswa() {
        if (kelasId == -1) return;

        String sql = "SELECT COUNT(*) as jumlah FROM siswa_kelas WHERE kelas_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int jumlah = rs.getInt("jumlah");
                jumlahSiswaLabel.setText(String.valueOf(jumlah) + " siswa");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSiswaDiKelas() {
        siswaList.clear();

        String sql = "SELECT s.nis, s.nama_siswa, s.jenis_kelamin, s.status " +
                     "FROM siswa s " +
                     "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                     "WHERE sk.kelas_id = ? " +
                     "ORDER BY s.nama_siswa";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String nis = rs.getString("nis");
                String nama = rs.getString("nama_siswa");
                String jenisKelamin = rs.getString("jenis_kelamin");
                String status = rs.getString("status");

                // Translate jenis kelamin
                String jenisKelaminDisplay = "Laki-laki".equals(jenisKelamin) ? "Laki-laki" : "Perempuan";

                siswaList.add(new SiswaWaliView(nis, nama, jenisKelaminDisplay, status != null ? status : "Aktif"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa.");
        }
    }

    private void loadJadwalKelas() {
        jadwalList.clear();

        String sql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, mp.nama_mapel, s.nama_staff " +
                     "FROM jadwal j " +
                     "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                     "JOIN staff s ON j.guru_id = s.staff_id " +
                     "WHERE j.kelas_id = ? " +
                     "ORDER BY CASE j.hari " +
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

            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String hari = rs.getString("hari");
                String jamMulai = rs.getTime("jam_mulai").toLocalTime().toString();
                String jamSelesai = rs.getTime("jam_selesai").toLocalTime().toString();
                String mapel = rs.getString("nama_mapel");
                String guru = rs.getString("nama_staff");

                String jam = jamMulai + " - " + jamSelesai;
                jadwalList.add(new JadwalKelasView(hari, jam, mapel, guru));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat jadwal kelas.");
        }
    }

    @FXML
    private void handleCetakRapor() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/CetakRapor.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman cetak rapor.");
        }
    }

    @FXML
    private void handleInputNilai() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/InputNilai.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman input nilai.");
        }
    }

    @FXML
    private void handleKelolaJadwal() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/KelolaJadwal.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka halaman kelola jadwal.");
        }
    }

    @FXML
    private void handleRefresh() {
        // Refresh semua data
        loadWaliKelasInfo();
        if (kelasId != -1) {
            loadSiswaDiKelas();
            loadJadwalKelas();
        }
        showAlert(Alert.AlertType.INFORMATION, "Info", "Data berhasil diperbarui.");
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

    // Inner classes untuk view
    public static class SiswaWaliView {
        private final String nis;
        private final String nama;
        private final String jenisKelamin;
        private final String status;

        public SiswaWaliView(String nis, String nama, String jenisKelamin, String status) {
            this.nis = nis;
            this.nama = nama;
            this.jenisKelamin = jenisKelamin;
            this.status = status;
        }

        public String getNis() { return nis; }
        public String getNama() { return nama; }
        public String getJenisKelamin() { return jenisKelamin; }
        public String getStatus() { return status; }
    }

    public static class JadwalKelasView {
        private final String hari;
        private final String jam;
        private final String mapel;
        private final String guru;

        public JadwalKelasView(String hari, String jam, String mapel, String guru) {
            this.hari = hari;
            this.jam = jam;
            this.mapel = mapel;
            this.guru = guru;
        }

        public String getHari() { return hari; }
        public String getJam() { return jam; }
        public String getMapel() { return mapel; }
        public String getGuru() { return guru; }
    }
}