package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.*;
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

public class SiswaDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label namaSiswaLabel;
    @FXML private Label nisLabel;
    @FXML private Label kelasLabel;
    @FXML private Label alamatLabel;
    @FXML private Label jenisKelaminLabel;
    @FXML private Label agamaLabel;
    @FXML private Label tanggalLahirLabel;

    // Tabel Jadwal
    @FXML private TableView<JadwalView> jadwalTableView;
    @FXML private TableColumn<JadwalView, String> hariColumn;
    @FXML private TableColumn<JadwalView, String> jamMulaiColumn;
    @FXML private TableColumn<JadwalView, String> jamSelesaiColumn;
    @FXML private TableColumn<JadwalView, String> mapelJadwalColumn;
    @FXML private TableColumn<JadwalView, String> guruColumn;

    // Tabel Nilai
    @FXML private TableView<NilaiView> nilaiTableView;
    @FXML private TableColumn<NilaiView, String> mapelNilaiColumn;
    @FXML private TableColumn<NilaiView, String> jenisUjianColumn;
    @FXML private TableColumn<NilaiView, Integer> nilaiColumn;
    @FXML private TableColumn<NilaiView, String> tanggalColumn;

    private final ObservableList<JadwalView> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<NilaiView> nilaiList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cek apakah ada user yang login dan role siswa
        if (!UserSession.getInstance().isLoggedIn() || !UserSession.getInstance().isSiswa()) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Halaman ini hanya untuk siswa.");
            return;
        }

        int siswaId = UserSession.getInstance().getReferenceId();

        // Setup kolom tabel sebelum memuat data
        setupTableColumns();

        // Memuat semua data yang dibutuhkan
        loadBiodataSiswa(siswaId);
        loadJadwalSiswa(siswaId);
        loadNilaiSiswa(siswaId);
    }

    private void setupTableColumns() {
        // Kolom Tabel Jadwal
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        jamSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        mapelJadwalColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("namaGuru"));

        // Kolom Tabel Nilai
        mapelNilaiColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        jenisUjianColumn.setCellValueFactory(new PropertyValueFactory<>("jenisUjian"));
        nilaiColumn.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        // Set items
        jadwalTableView.setItems(jadwalList);
        nilaiTableView.setItems(nilaiList);
    }

    private void loadBiodataSiswa(int siswaId) {
        // Query sesuai dengan schema database yang ada
        String sql = "SELECT s.nama_siswa, s.nis, s.alamat, s.jenis_kelamin, " +
                     "s.agama, s.tanggal_lahir, k.nama_kelas, ta.tahun_ajaran " +
                     "FROM siswa s " +
                     "LEFT JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                     "LEFT JOIN kelas k ON sk.kelas_id = k.kelas_id " +
                     "LEFT JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                     "WHERE s.siswa_id = ? AND (ta.status = 'aktif' OR ta.status IS NULL) " +
                     "ORDER BY ta.tahun_ajaran_id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String namaSiswa = rs.getString("nama_siswa");
                String nis = rs.getString("nis");
                String alamat = rs.getString("alamat");
                String jenisKelamin = rs.getString("jenis_kelamin");
                String agama = rs.getString("agama");
                String tanggalLahir = rs.getDate("tanggal_lahir") != null ? 
                                    rs.getDate("tanggal_lahir").toString() : "-";
                String namaKelas = rs.getString("nama_kelas");
                String tahunAjaran = rs.getString("tahun_ajaran");

                // Set teks pada Label
                welcomeLabel.setText("Selamat Datang, " + namaSiswa);
                namaSiswaLabel.setText(namaSiswa);
                nisLabel.setText("NIS: " + nis);
                kelasLabel.setText(namaKelas != null ? namaKelas + " (" + tahunAjaran + ")" : "Belum ada kelas");
                alamatLabel.setText(alamat != null ? alamat : "-");
                jenisKelaminLabel.setText("Laki-laki".equals(jenisKelamin) ? "Laki-laki" : "Perempuan");
                agamaLabel.setText(agama != null ? agama : "-");
                tanggalLahirLabel.setText(tanggalLahir);
            } else {
                welcomeLabel.setText("Data siswa tidak ditemukan");
                showAlert(Alert.AlertType.WARNING, "Data Tidak Ditemukan", "Data siswa tidak ditemukan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat biodata siswa.");
        }
    }

    private void loadJadwalSiswa(int siswaId) {
        jadwalList.clear();
        
        // Query sesuai schema database yang ada
        String sql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, mp.nama_mapel, s.nama_staff " +
                     "FROM jadwal j " +
                     "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                     "JOIN staff s ON j.guru_id = s.staff_id " +
                     "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                     "JOIN siswa_kelas sk ON k.kelas_id = sk.kelas_id " +
                     "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                     "WHERE sk.siswa_id = ? AND ta.status = 'aktif' " +
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

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String hari = rs.getString("hari");
                LocalTime jamMulai = rs.getTime("jam_mulai").toLocalTime();
                LocalTime jamSelesai = rs.getTime("jam_selesai").toLocalTime();
                String namaMapel = rs.getString("nama_mapel");
                String namaGuru = rs.getString("nama_staff");

                jadwalList.add(new JadwalView(hari, jamMulai.toString(), jamSelesai.toString(), namaMapel, namaGuru));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat jadwal siswa.");
        }
    }

    private void loadNilaiSiswa(int siswaId) {
        nilaiList.clear();
        
        // Query sesuai schema database yang ada
        String sql = "SELECT mp.nama_mapel, n.jenis_ujian, n.nilai " +
                     "FROM nilai n " +
                     "JOIN mata_pelajaran mp ON n.mapel_id = mp.mapel_id " +
                     "JOIN siswa_kelas sk ON n.siswa_kelas_id = sk.siswa_kelas_id " +
                     "WHERE sk.siswa_id = ? " +
                     "ORDER BY mp.nama_mapel, n.jenis_ujian";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String namaMapel = rs.getString("nama_mapel");
                String jenisUjian = rs.getString("jenis_ujian");
                int nilai = rs.getInt("nilai");
                String tanggal = "-"; // Data tanggal tidak ada di schema lama

                nilaiList.add(new NilaiView(namaMapel, jenisUjian, nilai, tanggal));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat nilai siswa.");
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

    // Inner classes untuk view
    public static class JadwalView {
        private final String hari;
        private final String jamMulai;
        private final String jamSelesai;
        private final String namaMapel;
        private final String namaGuru;

        public JadwalView(String hari, String jamMulai, String jamSelesai, String namaMapel, String namaGuru) {
            this.hari = hari;
            this.jamMulai = jamMulai;
            this.jamSelesai = jamSelesai;
            this.namaMapel = namaMapel;
            this.namaGuru = namaGuru;
        }

        public String getHari() { return hari; }
        public String getJamMulai() { return jamMulai; }
        public String getJamSelesai() { return jamSelesai; }
        public String getNamaMapel() { return namaMapel; }
        public String getNamaGuru() { return namaGuru; }
    }

    public static class NilaiView {
        private final String namaMapel;
        private final String jenisUjian;
        private final int nilai;
        private final String tanggal;

        public NilaiView(String namaMapel, String jenisUjian, int nilai, String tanggal) {
            this.namaMapel = namaMapel;
            this.jenisUjian = jenisUjian;
            this.nilai = nilai;
            this.tanggal = tanggal;
        }

        public String getNamaMapel() { return namaMapel; }
        public String getJenisUjian() { return jenisUjian; }
        public int getNilai() { return nilai; }
        public String getTanggal() { return tanggal; }
    }
}
