package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Staff; // Ganti Guru jadi Staff
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.Siswa;
import org.example.sekolahApp.model.TahunAjaran; // Tambahkan import TahunAjaran
import org.example.sekolahApp.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PembagianKelasController implements Initializable {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox; // Menggunakan TahunAjaran object
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private ComboBox<Staff> waliKelasComboBox; // Menggunakan Staff object

    @FXML private TableView<Siswa> siswaBelumKelasTableView;
    @FXML private TableColumn<Siswa, Integer> siswaIdBelumKelasColumn;
    @FXML private TableColumn<Siswa, String> siswaNisBelumKelasColumn;
    @FXML private TableColumn<Siswa, String> siswaNamaBelumKelasColumn;

    @FXML private TableView<Siswa> siswaDiKelasTableView;
    @FXML private TableColumn<Siswa, Integer> siswaIdDiKelasColumn;
    @FXML private TableColumn<Siswa, String> siswaNisDiKelasColumn;
    @FXML private TableColumn<Siswa, String> siswaNamaDiKelasColumn;
    @FXML private TableColumn<Siswa, String> siswaKelasSaatIniColumn; // Untuk menampilkan nama kelas siswa saat ini

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList(); // Ganti guruList
    private final ObservableList<Siswa> siswaBelumKelasList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaDiKelasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Table Columns
        siswaIdBelumKelasColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        siswaNisBelumKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        siswaNamaBelumKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        // Memastikan nama properti di model Siswa adalah "kelasSaatIniNama"
        siswaKelasSaatIniColumn.setCellValueFactory(new PropertyValueFactory<>("kelasSaatIniNama"));


        siswaIdDiKelasColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        siswaNisDiKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        siswaNamaDiKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        // Memastikan nama properti di model Siswa adalah "kelasSaatIniNama"
        siswaKelasSaatIniColumn.setCellValueFactory(new PropertyValueFactory<>("kelasSaatIniNama"));


        // Load data for ComboBoxes
        loadTahunAjaranData();
        loadKelasData(); // Memuat kelas berdasarkan tahun ajaran yang dipilih
        loadStaffData(); // Memuat staff (guru)

        // Add listeners
        tahunAjaranComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadKelasData(); // Reload kelas setiap tahun ajaran berubah
            loadSiswaData(); // Reload siswa setiap tahun ajaran berubah
        });
        kelasComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadSiswaData()); // Reload siswa setiap kelas berubah

        // Initial load
        tahunAjaranComboBox.getSelectionModel().selectFirst(); // Pilih tahun ajaran pertama secara default
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran DESC"; // Terbaru di atas
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("status")));
            }
            tahunAjaranComboBox.setItems(tahunAjaranList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void loadKelasData() {
        kelasList.clear();
        TahunAjaran selectedTA = tahunAjaranComboBox.getValue();
        if (selectedTA == null) return; // Jangan load kelas jika tahun ajaran belum dipilih

        String sql = "SELECT k.kelas_id, k.nama_kelas, k.tahun_ajaran_id, ta.tahun_ajaran, k.wali_kelas_id, s.nama_staff " +
                "FROM kelas k " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "LEFT JOIN staff s ON k.wali_kelas_id = s.staff_id " +
                "WHERE k.tahun_ajaran_id = ? " + // Filter berdasarkan tahun ajaran yang dipilih
                "ORDER BY k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedTA.getTahunAjaranId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), null);
                    Staff waliKelas = null;
                    if (rs.getObject("wali_kelas_id") != null) {
                        waliKelas = new Staff(rs.getInt("wali_kelas_id"), rs.getString("nama_staff"));
                    }
                    kelasList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, waliKelas));
                }
            }
            kelasComboBox.setItems(kelasList);
            // Set wali kelas yang terpilih jika ada kelas yang dipilih
            if (kelasComboBox.getValue() != null && kelasComboBox.getValue().getWaliKelas() != null) {
                waliKelasComboBox.setValue(kelasComboBox.getValue().getWaliKelas());
            } else {
                waliKelasComboBox.setValue(null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void loadStaffData() { // Ganti nama method dari loadGuruData
        staffList.clear();
        String sql = "SELECT staff_id, nama_staff, jabatan FROM staff WHERE jabatan = 'Guru' OR jabatan = 'Wali Kelas' ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            waliKelasComboBox.setItems(staffList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru/staff.");
        }
    }

    private void loadSiswaData() {
        siswaBelumKelasList.clear();
        siswaDiKelasList.clear();

        TahunAjaran selectedTA = tahunAjaranComboBox.getValue();
        Kelas selectedKelas = kelasComboBox.getValue();

        if (selectedTA == null) return; // Jangan load jika tahun ajaran belum dipilih

        Connection conn = null; // Deklarasi di luar try-with-resources
        try {
            conn = DatabaseConnection.getConnection();

            // Query untuk semua siswa yang belum memiliki kelas untuk tahun ajaran yang dipilih (atau kelasnya tidak di kelas terpilih)
            // Ini akan mengambil siswa yang benar-benar belum terdaftar di kelas manapun untuk TA ini,
            // ATAU siswa yang sudah terdaftar di kelas lain untuk TA ini.
            String sqlBelumKelas =
                    "SELECT s.siswa_id, s.nis, s.nama_siswa, s.alamat, s.jenis_kelamin, s.agama, s.tanggal_lahir, s.nama_orang_tua, s.telepon_orang_tua, " +
                            "k_current.nama_kelas AS current_nama_kelas " + // Tambah ini
                            "FROM siswa s " +
                            "LEFT JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                            "LEFT JOIN kelas k_assigned ON sk.kelas_id = k_assigned.kelas_id AND k_assigned.tahun_ajaran_id = ? " + // Join ke kelas untuk TA yang dipilih
                            "LEFT JOIN kelas k_current ON sk.kelas_id = k_current.kelas_id " + // Join lagi untuk nama kelas saat ini
                            "WHERE k_assigned.kelas_id IS NULL OR k_assigned.kelas_id != ? " + // Siswa belum di kelas terpilih untuk TA ini
                            "ORDER BY s.nama_siswa ASC";


            // Query untuk siswa yang SUDAH di kelas yang dipilih untuk tahun ajaran ini
            String sqlDiKelas =
                    "SELECT s.siswa_id, s.nis, s.nama_siswa, s.alamat, s.jenis_kelamin, s.agama, s.tanggal_lahir, s.nama_orang_tua, s.telepon_orang_tua, " +
                            "k_assigned.nama_kelas AS current_nama_kelas " + // Alias ini untuk kolom kelasSaatIniNama
                            "FROM siswa s " +
                            "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                            "JOIN kelas k_assigned ON sk.kelas_id = k_assigned.kelas_id " +
                            "WHERE k_assigned.tahun_ajaran_id = ? AND k_assigned.kelas_id = ? " + // Filter berdasarkan tahun ajaran dan kelas terpilih
                            "ORDER BY s.nama_siswa ASC";

            if (selectedKelas != null) {
                // Load siswa yang belum di kelas ini
                try (PreparedStatement pstmt = conn.prepareStatement(sqlBelumKelas)) {
                    pstmt.setInt(1, selectedTA.getTahunAjaranId()); // Parameter untuk k_assigned.tahun_ajaran_id
                    pstmt.setInt(2, selectedKelas.getKelasId());    // Parameter untuk k_assigned.kelas_id
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Siswa s = createSiswaFromResultSet(rs);
                            // Set nama kelas saat ini jika ada (dari current_nama_kelas alias)
                            String currentKelasNama = rs.getString("current_nama_kelas");
                            if (currentKelasNama != null) {
                                s.setKelasSaatIniNama(currentKelasNama);
                            }
                            siswaBelumKelasList.add(s);
                        }
                    }
                }

                // Load siswa yang sudah di kelas ini
                try (PreparedStatement pstmt = conn.prepareStatement(sqlDiKelas)) {
                    pstmt.setInt(1, selectedTA.getTahunAjaranId()); // Parameter untuk k_assigned.tahun_ajaran_id
                    pstmt.setInt(2, selectedKelas.getKelasId());    // Parameter untuk k_assigned.kelas_id
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Siswa s = createSiswaFromResultSet(rs);
                            s.setKelasSaatIniNama(rs.getString("current_nama_kelas")); // Pasti ada di sini
                            siswaDiKelasList.add(s);
                        }
                    }
                }
            } else {
                // Jika tidak ada kelas yang dipilih, tampilkan semua siswa yang belum memiliki kelas sama sekali
                // untuk tahun ajaran ini.
                String sqlAllSiswaWithoutClass =
                        "SELECT s.siswa_id, s.nis, s.nama_siswa, s.alamat, s.jenis_kelamin, s.agama, s.tanggal_lahir, s.nama_orang_tua, s.telepon_orang_tua " +
                                "FROM siswa s " +
                                "LEFT JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                                "LEFT JOIN kelas k ON sk.kelas_id = k.kelas_id AND k.tahun_ajaran_id = ? " + // Penting untuk filter TA
                                "WHERE sk.siswa_id IS NULL " + // Hanya siswa yang belum punya entri di siswa_kelas untuk TA ini
                                "ORDER BY s.nama_siswa ASC";

                try (PreparedStatement pstmt = conn.prepareStatement(sqlAllSiswaWithoutClass)) {
                    pstmt.setInt(1, selectedTA.getTahunAjaranId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            siswaBelumKelasList.add(createSiswaFromResultSet(rs));
                        }
                    }
                }
            }
            siswaBelumKelasTableView.setItems(siswaBelumKelasList);
            siswaDiKelasTableView.setItems(siswaDiKelasList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa untuk pembagian kelas.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Helper method to create Siswa object from ResultSet (to avoid repetition)
    private Siswa createSiswaFromResultSet(ResultSet rs) throws SQLException {
        return new Siswa(
                rs.getInt("siswa_id"),
                rs.getString("nis"),
                rs.getString("nama_siswa"),
                rs.getString("alamat"),
                rs.getString("jenis_kelamin"),
                rs.getString("agama"),
                rs.getDate("tanggal_lahir").toLocalDate(),
                rs.getString("nama_orang_tua"),
                rs.getString("telepon_orang_tua")
        );
    }


    @FXML
    private void handleSetWaliKelas() {
        Kelas selectedKelas = kelasComboBox.getValue();
        Staff selectedWaliKelas = waliKelasComboBox.getValue(); // Gunakan Staff object

        if (selectedKelas == null || selectedWaliKelas == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih Kelas dan Guru untuk Wali Kelas.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Wali Kelas");
        confirmAlert.setHeaderText("Set Wali Kelas untuk " + selectedKelas.getNamaKelas() + " (" + selectedKelas.getTahunAjaran().getTahunAjaran() + ")");
        confirmAlert.setContentText("Apakah Anda yakin ingin menetapkan " + selectedWaliKelas.getNamaStaff() + " sebagai wali kelas?"); // Gunakan namaStaff

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "UPDATE kelas SET wali_kelas_id = ? WHERE kelas_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedWaliKelas.getStaffId()); // Gunakan getStaffId()
                pstmt.setInt(2, selectedKelas.getKelasId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Wali kelas berhasil diatur.");
                loadKelasData(); // Refresh kelas data to show new wali kelas
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengatur wali kelas.");
            }
        }
    }

    @FXML
    private void handleAddSiswaToClass() {
        Siswa selectedSiswa = siswaBelumKelasTableView.getSelectionModel().getSelectedItem();
        Kelas selectedKelas = kelasComboBox.getValue();
        TahunAjaran selectedTA = tahunAjaranComboBox.getValue(); // Menggunakan TahunAjaran object

        if (selectedSiswa == null || selectedKelas == null || selectedTA == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih siswa, kelas, dan tahun ajaran.");
            return;
        }

        // Cek apakah siswa sudah di kelas lain untuk tahun ajaran ini
        // (sudah dilakukan di SQL kueri siswaBelumKelas, tapi validasi di sini lebih user-friendly)
        if (selectedSiswa.getKelasSaatIniNama() != null && !selectedSiswa.getKelasSaatIniNama().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Siswa ini (" + selectedSiswa.getNama() + ") sudah terdaftar di kelas " + selectedSiswa.getKelasSaatIniNama() + " untuk tahun ajaran " + selectedTA.getTahunAjaran() + ".");
            return;
        }


        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Penambahan Siswa");
        confirmAlert.setHeaderText("Tambahkan " + selectedSiswa.getNama() + " ke kelas " + selectedKelas.getNamaKelas() + " (" + selectedTA.getTahunAjaran() + ")?");
        confirmAlert.setContentText("Apakah Anda yakin?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "INSERT INTO siswa_kelas (siswa_id, kelas_id) VALUES (?, ?)"; // Tidak ada tahun_ajaran di tabel siswa_kelas
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedSiswa.getId());
                    pstmt.setInt(2, selectedKelas.getKelasId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Siswa berhasil ditambahkan ke kelas.");
                    loadSiswaData(); // Refresh both tables
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan siswa ke kelas. Mungkin siswa sudah ada di kelas ini untuk tahun ajaran yang sama.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @FXML
    private void handleRemoveSiswaFromClass() {
        Siswa selectedSiswa = siswaDiKelasTableView.getSelectionModel().getSelectedItem();
        Kelas selectedKelas = kelasComboBox.getValue(); // Kelas yang sedang aktif di ComboBox
        TahunAjaran selectedTA = tahunAjaranComboBox.getValue();

        if (selectedSiswa == null || selectedKelas == null || selectedTA == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih siswa di kelas ini dan pastikan kelas serta tahun ajaran terpilih.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Pengeluaran Siswa");
        confirmAlert.setHeaderText("Keluarkan " + selectedSiswa.getNama() + " dari kelas " + selectedKelas.getNamaKelas() + " (" + selectedTA.getTahunAjaran() + ")?");
        confirmAlert.setContentText("Aksi ini akan menghapus siswa dari kelas ini untuk tahun ajaran yang dipilih.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM siswa_kelas WHERE siswa_id = ? AND kelas_id = ?"; // Tidak ada tahun_ajaran di tabel siswa_kelas
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedSiswa.getId());
                    pstmt.setInt(2, selectedKelas.getKelasId());
                    // Tidak ada pstmt.setString(3, tahunAjaran); karena tabel siswa_kelas tidak punya kolom tahun_ajaran
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Siswa berhasil dikeluarkan dari kelas.");
                    loadSiswaData(); // Refresh both tables
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengeluarkan siswa dari kelas.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @FXML
    private void handleNaikKelas() {
        TahunAjaran currentTahunAjaranObj = tahunAjaranComboBox.getValue();
        if (currentTahunAjaranObj == null || currentTahunAjaranObj.getTahunAjaran().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tahun ajaran saat ini terlebih dahulu.");
            return;
        }
        String currentTahunAjaran = currentTahunAjaranObj.getTahunAjaran();

        // Logic untuk menentukan tahun ajaran baru (contoh: 2024/2025 -> 2025/2026)
        String[] parts = currentTahunAjaran.split("/");
        if (parts.length != 2) {
            showAlert(Alert.AlertType.ERROR, "Format Tahun Ajaran Salah", "Format harus XXXX/YYYY (contoh: 2024/2025).");
            return;
        }
        int startYear = Integer.parseInt(parts[0]);
        int endYear = Integer.parseInt(parts[1]);
        String nextTahunAjaranStr = (startYear + 1) + "/" + (endYear + 1);

        // Cari ID Tahun Ajaran berikutnya
        Optional<TahunAjaran> nextTahunAjaranObj = tahunAjaranList.stream()
                .filter(ta -> ta.getTahunAjaran().equals(nextTahunAjaranStr))
                .findFirst();

        if (nextTahunAjaranObj.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Tahun Ajaran Berikutnya Tidak Ditemukan", "Tahun ajaran " + nextTahunAjaranStr + " belum terdaftar. Harap tambahkan terlebih dahulu.");
            return;
        }
        int nextTahunAjaranId = nextTahunAjaranObj.get().getTahunAjaranId();


        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Naik Kelas");
        confirmAlert.setHeaderText("Naik Kelas Siswa dari " + currentTahunAjaran + " ke " + nextTahunAjaranStr);
        confirmAlert.setContentText("Aksi ini akan menaikkan semua siswa aktif ke kelas yang diasumsikan sebagai kelas 'naik' untuk tahun ajaran " + nextTahunAjaranStr + ".\n" +
                "1. Siswa yang saat ini di kelas akhir (misal XII) akan dianggap lulus.\n" +
                "2. Proses ini akan menghapus alokasi siswa ke kelas di tahun ajaran " + currentTahunAjaran + " dan membuat alokasi baru di " + nextTahunAjaranStr + ".\n" +
                "Lanjutkan?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false); // Mulai transaksi

                // 1. Dapatkan semua siswa yang saat ini terdaftar di tahun ajaran ini
                String selectSiswaKelasSQL = "SELECT sk.siswa_id, k.nama_kelas, sk.kelas_id, k.tahun_ajaran_id " +
                        "FROM siswa_kelas sk " +
                        "JOIN kelas k ON sk.kelas_id = k.kelas_id " +
                        "WHERE k.tahun_ajaran_id = ?";
                List<int[]> siswaToPromote = new ArrayList<>(); // Store {siswa_id, kelas_id_saat_ini}
                try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSiswaKelasSQL)) {
                    pstmtSelect.setInt(1, currentTahunAjaranObj.getTahunAjaranId());
                    try (ResultSet rs = pstmtSelect.executeQuery()) {
                        while (rs.next()) {
                            int siswaId = rs.getInt("siswa_id");
                            String namaKelasSaatIni = rs.getString("nama_kelas");
                            int kelasIdSaatIni = rs.getInt("kelas_id");

                            int nextKelasId = getNextKelasId(namaKelasSaatIni, nextTahunAjaranId, conn);

                            if (nextKelasId != -1) { // Jika ada kelas berikutnya
                                siswaToPromote.add(new int[]{siswaId, kelasIdSaatIni, nextKelasId});
                            } else {
                                // Jika tidak ada kelas berikutnya (misal: sudah kelas 12/lulus),
                                // kita akan menghapus mereka dari siswa_kelas untuk tahun ajaran ini
                                // Ini akan ditangani oleh DELETE global di bawah jika tidak dipromosikan
                                System.out.println("Siswa " + siswaId + " (" + namaKelasSaatIni + ") diasumsikan lulus/keluar.");
                            }
                        }
                    }
                }

                // 2. Hapus semua alokasi siswa dari tahun ajaran saat ini
                String deleteAllCurrentYearClassesSQL = "DELETE FROM siswa_kelas WHERE kelas_id IN (SELECT kelas_id FROM kelas WHERE tahun_ajaran_id = ?)";
                try (PreparedStatement pstmtDeleteAll = conn.prepareStatement(deleteAllCurrentYearClassesSQL)) {
                    pstmtDeleteAll.setInt(1, currentTahunAjaranObj.getTahunAjaranId());
                    pstmtDeleteAll.executeUpdate();
                }

                // 3. Masukkan siswa ke kelas baru untuk tahun ajaran berikutnya
                String insertNewClassSQL = "INSERT INTO siswa_kelas (siswa_id, kelas_id) VALUES (?, ?)"; // Tidak ada kolom tahun_ajaran_id di siswa_kelas
                try (PreparedStatement pstmtInsert = conn.prepareStatement(insertNewClassSQL)) {
                    for (int[] promo : siswaToPromote) {
                        pstmtInsert.setInt(1, promo[0]); // siswa_id
                        pstmtInsert.setInt(2, promo[2]); // next_kelas_id
                        pstmtInsert.addBatch(); // Tambahkan ke batch
                    }
                    pstmtInsert.executeBatch(); // Eksekusi semua insert dalam batch
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Proses naik kelas berhasil untuk tahun ajaran " + currentTahunAjaran + " ke " + nextTahunAjaranStr + ".");
                loadSiswaData(); // Refresh tables

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal melakukan proses naik kelas.");
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Helper method to get the ID of the next class for the next academic year
    // THIS IS A SIMPLIFIED LOGIC. YOU MAY NEED MORE SOPHISTICATED MAPPING.
    private int getNextKelasId(String currentClassName, int nextTahunAjaranId, Connection conn) throws SQLException {
        // Contoh sederhana: X -> XI, XI -> XII, VII -> VIII, VIII -> IX, dst.
        // Asumsi nama kelas konsisten (e.g., 'X IPA 1' akan menjadi 'XI IPA 1')
        String nextClassNamePrefix = "";

        if (currentClassName.toLowerCase().contains("vii")) { // Kelas 7
            nextClassNamePrefix = currentClassName.replace("VII", "VIII");
        } else if (currentClassName.toLowerCase().contains("viii")) { // Kelas 8
            nextClassNamePrefix = currentClassName.replace("VIII", "IX");
        } else if (currentClassName.toLowerCase().contains("ix")) { // Kelas 9
            nextClassNamePrefix = currentClassName.replace("IX", "X");
        } else if (currentClassName.toLowerCase().contains("x ")) { // Kelas 10 (perhatikan spasi untuk menghindari XII)
            nextClassNamePrefix = currentClassName.replace("X ", "XI ");
        } else if (currentClassName.toLowerCase().contains("xi")) { // Kelas 11
            nextClassNamePrefix = currentClassName.replace("XI", "XII");
        } else {
            return -1; // Tidak ada kelas berikutnya atau sudah kelas akhir (misal XII)
        }

        String sql = "SELECT kelas_id FROM kelas WHERE nama_kelas = ? AND tahun_ajaran_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nextClassNamePrefix); // Mencari nama kelas yang persis sama
            pstmt.setInt(2, nextTahunAjaranId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("kelas_id");
                }
            }
        }
        return -1; // Tidak ditemukan kelas berikutnya dengan nama dan TA yang cocok
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml", 800, 600);
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