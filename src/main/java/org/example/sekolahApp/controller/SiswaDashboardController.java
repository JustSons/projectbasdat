package org.example.sekolahApp.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SiswaDashboardController implements Initializable {

    // Biodata Labels
    @FXML private Label welcomeLabel;
    @FXML private Label nisLabel;
    @FXML private Label namaLabel; // Disesuaikan dengan FXML
    @FXML private Label kelasLabel;
    @FXML private Label alamatLabel;
    @FXML private Label jenisKelaminLabel;
    @FXML private Label agamaLabel;
    @FXML private Label tanggalLahirLabel;

    // Jadwal Table & Columns
    @FXML private TableView<JadwalView> jadwalTableView;
    @FXML private TableColumn<JadwalView, String> hariColumn;
    @FXML private TableColumn<JadwalView, String> jamColumn;
    @FXML private TableColumn<JadwalView, String> mataPelajaranColumn;
    @FXML private TableColumn<JadwalView, String> guruColumn;

    // Nilai Table & Columns
    @FXML private TableView<NilaiView> nilaiTableView;
    @FXML private TableColumn<NilaiView, String> mataPelajaranNilaiColumn;
    @FXML private TableColumn<NilaiView, String> jenisNilaiColumn;
    @FXML private TableColumn<NilaiView, Integer> nilaiColumn;
    @FXML private TableColumn<NilaiView, String> tanggalColumn;

    private final ObservableList<JadwalView> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<NilaiView> nilaiList = FXCollections.observableArrayList();
    private int siswaId;
    private Integer kelasId; // Gunakan Integer agar bisa null

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (!UserSession.getInstance().isLoggedIn() || !UserSession.getInstance().isSiswa()) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Halaman ini hanya untuk siswa.");
            return;
        }

        this.siswaId = UserSession.getInstance().getReferenceId();
        setupTableColumns();
        loadAllData();
    }

    private void setupTableColumns() {
        // Kolom Tabel Jadwal
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamColumn.setCellValueFactory(new PropertyValueFactory<>("jam"));
        mataPelajaranColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        guruColumn.setCellValueFactory(new PropertyValueFactory<>("namaGuru"));
        jadwalTableView.setItems(jadwalList);

        // Kolom Tabel Nilai
        mataPelajaranNilaiColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        jenisNilaiColumn.setCellValueFactory(new PropertyValueFactory<>("jenisUjian"));
        nilaiColumn.setCellValueFactory(new PropertyValueFactory<>("nilai"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        nilaiTableView.setItems(nilaiList);
    }

    private void loadAllData() {
        loadBiodataSiswa(this.siswaId);
        if (this.kelasId != null) { // Hanya muat jadwal & nilai jika siswa punya kelas
            loadJadwalSiswa(this.kelasId);
            loadNilaiSiswa(this.siswaId);
        }
    }

    private void loadBiodataSiswa(int currentSiswaId) {
        String sql = "SELECT s.nama_siswa, s.nis, s.alamat, s.jenis_kelamin, s.agama, s.tanggal_lahir, k.kelas_id, k.nama_kelas " +
                "FROM siswa s " +
                "LEFT JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                "LEFT JOIN kelas k ON sk.kelas_id = k.kelas_id " +
                "LEFT JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "WHERE s.siswa_id = ? AND (ta.status IS NULL OR ta.status = 'aktif')";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentSiswaId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Ambil kelas_id, bisa jadi null jika siswa belum masuk kelas
                this.kelasId = (Integer) rs.getObject("kelas_id");

                welcomeLabel.setText("Selamat Datang, " + rs.getString("nama_siswa"));
                namaLabel.setText(rs.getString("nama_siswa"));
                nisLabel.setText(rs.getString("nis"));
                kelasLabel.setText(rs.getString("nama_kelas") != null ? rs.getString("nama_kelas") : "Belum Masuk Kelas Aktif");
                alamatLabel.setText(rs.getString("alamat"));
                jenisKelaminLabel.setText(rs.getString("jenis_kelamin"));
                agamaLabel.setText(rs.getString("agama"));
                LocalDate tglLahir = rs.getDate("tanggal_lahir").toLocalDate();
                tanggalLahirLabel.setText(tglLahir.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat biodata siswa.");
        }
    }

    private void loadJadwalSiswa(int currentKelasId) {
        jadwalList.clear();
        String sql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, mp.nama_mapel, s.nama_staff " +
                "FROM jadwal j " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "JOIN staff s ON j.guru_id = s.staff_id " +
                "WHERE j.kelas_id = ? " +
                "ORDER BY CASE j.hari WHEN 'Senin' THEN 1 WHEN 'Selasa' THEN 2 WHEN 'Rabu' THEN 3 WHEN 'Kamis' THEN 4 WHEN 'Jumat' THEN 5 WHEN 'Sabtu' THEN 6 ELSE 7 END, j.jam_mulai";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentKelasId);
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            while (rs.next()) {
                LocalTime jamMulai = rs.getTime("jam_mulai").toLocalTime();
                LocalTime jamSelesai = rs.getTime("jam_selesai").toLocalTime();
                jadwalList.add(new JadwalView(
                        rs.getString("hari"),
                        jamMulai.format(timeFormatter) + " - " + jamSelesai.format(timeFormatter),
                        rs.getString("nama_mapel"),
                        rs.getString("nama_staff")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat jadwal pelajaran.");
        }
    }

    private void loadNilaiSiswa(int currentSiswaId) {
        nilaiList.clear();
        String sql = "SELECT mp.nama_mapel, n.jenis_ujian, n.nilai " +
                "FROM nilai n " +
                "JOIN mata_pelajaran mp ON n.mapel_id = mp.mapel_id " +
                "JOIN siswa_kelas sk ON n.siswa_kelas_id = sk.siswa_kelas_id " +
                "WHERE sk.siswa_id = ? ORDER BY mp.nama_mapel, n.jenis_ujian";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentSiswaId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                nilaiList.add(new NilaiView(
                        rs.getString("nama_mapel"),
                        rs.getString("jenis_ujian"),
                        rs.getInt("nilai"),
                        "-")); // Kolom tanggal di-hardcode karena tidak ada di DB
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data nilai.");
        }
    }

    // --- METODE BARU YANG ANDA BUTUHKAN ---
    @FXML
    private void handleRefresh() {
        showAlert(Alert.AlertType.INFORMATION, "Refresh", "Memuat ulang data...");
        loadAllData();
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.cleanUserSession();
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

    // Inner class untuk view jadwal
    public static class JadwalView {
        private final SimpleStringProperty hari;
        private final SimpleStringProperty jam;
        private final SimpleStringProperty namaMapel;
        private final SimpleStringProperty namaGuru;

        public JadwalView(String hari, String jam, String namaMapel, String namaGuru) {
            this.hari = new SimpleStringProperty(hari);
            this.jam = new SimpleStringProperty(jam);
            this.namaMapel = new SimpleStringProperty(namaMapel);
            this.namaGuru = new SimpleStringProperty(namaGuru);
        }

        public String getHari() { return hari.get(); }
        public String getJam() { return jam.get(); }
        public String getNamaMapel() { return namaMapel.get(); }
        public String getNamaGuru() { return namaGuru.get(); }
    }

    // Inner class untuk view nilai
    public static class NilaiView {
        private final SimpleStringProperty namaMapel;
        private final SimpleStringProperty jenisUjian;
        private final SimpleIntegerProperty nilai;
        private final SimpleStringProperty tanggal;

        public NilaiView(String namaMapel, String jenisUjian, int nilai, String tanggal) {
            this.namaMapel = new SimpleStringProperty(namaMapel);
            this.jenisUjian = new SimpleStringProperty(jenisUjian);
            this.nilai = new SimpleIntegerProperty(nilai);
            this.tanggal = new SimpleStringProperty(tanggal);
        }

        public String getNamaMapel() { return namaMapel.get(); }
        public String getJenisUjian() { return jenisUjian.get(); }
        public int getNilai() { return nilai.get(); }
        public String getTanggal() { return tanggal.get(); }
    }
}