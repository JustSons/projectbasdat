package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // <-- IMPORT YANG PENTING
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Jadwal; // Pastikan Anda sudah membuat model ini
import org.example.sekolahApp.model.Nilai;   // Pastikan Anda sudah membuat model ini
import org.example.sekolahApp.util.UserSession;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

// Tambahkan "implements Initializable" di sini
public class SiswaDashboardController implements Initializable {

    @FXML private Label namaSiswaLabel;
    @FXML private Label nisLabel;
    @FXML private Label kelasLabel;
    @FXML private Label welcomeLabel; // Label sambutan di atas

    // Tabel Jadwal
    @FXML private TableView<Jadwal> jadwalTableView;
    @FXML private TableColumn<Jadwal, String> hariColumn;
    @FXML private TableColumn<Jadwal, String> jamMulaiColumn;
    @FXML private TableColumn<Jadwal, String> jamSelesaiColumn;
    @FXML private TableColumn<Jadwal, String> mapelJadwalColumn;
    @FXML private TableColumn<Jadwal, String> guruColumn;

    // Tabel Nilai
    @FXML private TableView<Nilai> nilaiTableView;
    @FXML private TableColumn<Nilai, String> mapelNilaiColumn;
    @FXML private TableColumn<Nilai, String> jenisUjianColumn;
    @FXML private TableColumn<Nilai, Integer> nilaiColumn;

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<Nilai> nilaiList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cek apakah ada user yang login
        if (!UserSession.getInstance().isLoggedIn()) {
            welcomeLabel.setText("Error: Tidak ada sesi pengguna.");
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
    }

    private void loadBiodataSiswa(int siswaId) {
        // Query untuk mengambil biodata siswa dan nama kelasnya
        String sql = "SELECT s.nama_siswa, s.nis, k.nama_kelas " +
                "FROM siswa s " +
                "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                "JOIN kelas k ON sk.kelas_id = k.kelas_id " +
                "WHERE s.siswa_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String namaSiswa = rs.getString("nama_siswa");
                String nis = rs.getString("nis");
                String namaKelas = rs.getString("nama_kelas");

                // Set teks pada Label
                welcomeLabel.setText("Selamat Datang, " + namaSiswa);
                namaSiswaLabel.setText(namaSiswa);
                nisLabel.setText(nis);
                kelasLabel.setText(namaKelas);
            } else {
                // Jika siswa belum dimasukkan ke kelas manapun
                namaSiswaLabel.setText("Siswa belum terdaftar di kelas manapun.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadJadwalSiswa(int siswaId) {
        jadwalList.clear();
        // Query kompleks untuk mengambil jadwal berdasarkan kelas siswa
        String sql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, mp.nama_mapel, st.nama_staff " +
                "FROM jadwal j " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "JOIN staff st ON j.guru_id = st.staff_id " +
                "WHERE j.kelas_id = (SELECT kelas_id FROM siswa_kelas WHERE siswa_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                jadwalList.add(new Jadwal(
                        rs.getString("hari"),
                        rs.getTime("jam_mulai").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        rs.getTime("jam_selesai").toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        rs.getString("nama_mapel"),
                        rs.getString("nama_staff")
                ));
            }
            jadwalTableView.setItems(jadwalList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadNilaiSiswa(int siswaId) {
        nilaiList.clear();
        // Query untuk mengambil semua nilai siswa
        String sql = "SELECT mp.nama_mapel, n.jenis_ujian, n.nilai " +
                "FROM nilai n " +
                "JOIN mata_pelajaran mp ON n.mapel_id = mp.mapel_id " +
                "JOIN siswa_kelas sk ON n.siswa_kelas_id = sk.siswa_kelas_id " +
                "WHERE sk.siswa_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                nilaiList.add(new Nilai(
                        rs.getString("nama_mapel"),
                        rs.getString("jenis_ujian"),
                        rs.getInt("nilai")
                ));
            }
            nilaiTableView.setItems(nilaiList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // Anda juga perlu membuat kelas model Jadwal.java dan Nilai.java
    // Contoh ada di bawah jika Anda belum membuatnya
}
