package org.example.sekolahApp.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.*; // Pastikan semua model diimpor
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;
import java.time.LocalDate;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputNilaiController {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private ComboBox<MataPelajaran> mapelComboBox;
    @FXML private TableView<SiswaKelas> siswaTableView;
    @FXML private TableColumn<SiswaKelas, String> nisColumn;
    @FXML private TableColumn<SiswaKelas, String> namaColumn;
    @FXML private TableColumn<SiswaKelas, Integer> uts1Column;
    @FXML private TableColumn<SiswaKelas, Integer> uas1Column;
    @FXML private TableColumn<SiswaKelas, Integer> uts2Column;
    @FXML private TableColumn<SiswaKelas, Integer> uas2Column;

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mapelList = FXCollections.observableArrayList();
    private final ObservableList<SiswaKelas> siswaDiKelasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupControls();
        setupTableView();
        loadInitialData();
    }

    // GANTI metode lama Anda dengan versi yang sudah diperbaiki ini.
    private void loadInitialData() {
        // Selalu pastikan list kosong sebelum diisi ulang untuk menghindari duplikat
        tahunAjaranList.clear();

        // PERBAIKAN: Hapus "WHERE status = 'aktif'" untuk mengambil SEMUA tahun ajaran.
        // Diurutkan berdasarkan tahun terbaru.
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Gunakan loop 'while' untuk memastikan semua hasil query dimuat, bukan hanya satu.
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tahunAjaranComboBox.setItems(tahunAjaranList);

        // Otomatis pilih tahun ajaran yang aktif sebagai default jika ada.
        if (!tahunAjaranList.isEmpty()) {
            TahunAjaran defaultSelection = tahunAjaranList.stream()
                    .findFirst()
                    .orElse(tahunAjaranList.get(0)); // Jika tidak ada yang aktif, pilih yang paling atas

            tahunAjaranComboBox.getSelectionModel().select(defaultSelection);
            loadKelasByTahunAjaran(); // Langsung muat kelas untuk pilihan default
        }
    }

    private void setupControls() {
        configureComboBox(tahunAjaranComboBox);
        configureComboBox(kelasComboBox);
        configureComboBox(mapelComboBox);

        // Aksi ketika pilihan berubah
        kelasComboBox.setOnAction(e -> loadMapelByKelas());
        mapelComboBox.setOnAction(e -> loadSiswaDanNilai());
    }

    // GANTI metode lama Anda dengan versi yang sudah diperbaiki ini.
    private void loadKelasByTahunAjaran() {
        TahunAjaran selectedTahun = tahunAjaranComboBox.getValue();
        if (selectedTahun == null) return;

        // Kosongkan semua list turunan sebelum diisi ulang
        kelasList.clear();
        mapelList.clear();
        siswaDiKelasList.clear();

        // Query ini diubah agar HANYA memuat kelas yang memiliki jadwal untuk guru yang login
        // Ini menyelesaikan masalah kelas baru dan mapel baru yang tidak muncul
        String sql = "SELECT DISTINCT k.kelas_id, k.nama_kelas FROM jadwal j " +
                "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                "WHERE j.guru_id = ? AND k.tahun_ajaran_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, UserSession.getInstance().getReferenceId());
            pstmt.setInt(2, selectedTahun.getTahunAjaranId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                kelasList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas yang Anda ajar.");
        }

        // Set item ke ComboBox
        kelasComboBox.setItems(kelasList);

        // Otomatis pilih item pertama jika ada, lalu picu pemuatan mapel
        if (!kelasList.isEmpty()){
            kelasComboBox.getSelectionModel().selectFirst();
            // Memanggil loadMapelByKelas() secara manual setelah kelas dimuat
            loadMapelByKelas();

        }
    }

    private void loadMapelByKelas() {
        Kelas selectedKelas = kelasComboBox.getValue();
        if (selectedKelas == null) return;

        mapelList.clear();
        // Muat mapel yang diajar oleh guru yang login di kelas yang dipilih
        String sql = "SELECT mp.mapel_id, mp.kode_mapel, mp.nama_mapel FROM jadwal j " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "WHERE j.guru_id = ? AND j.kelas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, UserSession.getInstance().getReferenceId());
            pstmt.setInt(2, selectedKelas.getKelasId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mapelList.add(new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        mapelComboBox.setItems(mapelList);
    }

    private void loadSiswaDanNilai() {
        Kelas selectedKelas = kelasComboBox.getValue();
        MataPelajaran selectedMapel = mapelComboBox.getValue();
        if (selectedKelas == null || selectedMapel == null) {
            siswaDiKelasList.clear(); // Kosongkan tabel jika tidak ada mapel/kelas
            return;
        }

        // 1. Ambil daftar siswa dari tabel siswa_kelas
        List<SiswaKelas> pendaftaranSiswa = new ArrayList<>();
        String sqlSiswa = "SELECT sk.siswa_kelas_id, s.* " +
                "FROM siswa_kelas sk " +
                "JOIN siswa s ON sk.siswa_id = s.siswa_id WHERE sk.kelas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSiswa)) {
            pstmt.setInt(1, selectedKelas.getKelasId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Siswa siswa = new Siswa(rs.getInt("siswa_id"),
                        rs.getString("nis"),
                        rs.getString("nama_siswa"),
                        rs.getString("alamat"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("agama"),
                        rs.getDate("tanggal_lahir").toLocalDate(),
                        rs.getString("nama_orang_tua"),
                        rs.getString("telepon_orang_tua")
                );
                pendaftaranSiswa.add(new SiswaKelas(rs.getInt("siswa_kelas_id"), siswa, selectedKelas));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (pendaftaranSiswa.isEmpty()) {
            siswaDiKelasList.clear();
            return;
        }

        // 2. Ambil semua nilai untuk semua siswa di kelas ini & mapel ini dalam satu query
        List<Integer> siswaKelasIds = pendaftaranSiswa.stream().map(SiswaKelas::getSiswaKelasId).collect(Collectors.toList());
        String placeholders = siswaKelasIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sqlNilai = "SELECT siswa_kelas_id, jenis_ujian, nilai FROM nilai WHERE mapel_id = ? AND siswa_kelas_id IN (" + placeholders + ")";

        List<Nilai> semuaNilai = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlNilai)) {
            pstmt.setInt(1, selectedMapel.getMapelId());
            for (int i = 0; i < siswaKelasIds.size(); i++) {
                pstmt.setInt(i + 2, siswaKelasIds.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                semuaNilai.add(new Nilai(rs.getInt("siswa_kelas_id"), selectedMapel.getMapelId(), rs.getString("jenis_ujian"), rs.getInt("nilai")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Distribusikan nilai ke setiap objek siswa
        for (SiswaKelas sk : pendaftaranSiswa) {
            List<Nilai> nilaiMilikSiswa = semuaNilai.stream()
                    .filter(n -> n.getSiswaKelasId() == sk.getSiswaKelasId())
                    .collect(Collectors.toList());
            sk.getSiswa().setDaftarNilai(nilaiMilikSiswa);
        }

        // 4. Tampilkan di tabel
        siswaDiKelasList.setAll(pendaftaranSiswa);
    }

    private void setupTableView() {
        siswaTableView.setItems(siswaDiKelasList);
        siswaTableView.setEditable(true);

        // --- NAMA PROPERTI DIPERBAIKI ---
        nisColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().nisProperty());
        namaColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().namaProperty()); // Diubah dari namaProperty()

        // Konfigurasi kolom nilai tidak berubah
        configureNilaiColumn(uts1Column, "UTS1");
        configureNilaiColumn(uas1Column, "UAS1");
        configureNilaiColumn(uts2Column, "UTS2");
        configureNilaiColumn(uas2Column, "UAS2");
    }

    private void configureNilaiColumn(TableColumn<SiswaKelas, Integer> column, String jenisUjian) {
        // Mengambil nilai dari dalam objek Siswa
        column.setCellValueFactory(cellData -> {
            Siswa siswa = cellData.getValue().getSiswa();
            MataPelajaran mapel = mapelComboBox.getValue();
            if (mapel == null) return null;

            Nilai nilai = siswa.getNilaiByMapelAndJenis(mapel.getMapelId(), jenisUjian);
            if (nilai != null) {
                return new SimpleIntegerProperty(nilai.getNilai()).asObject();
            }
            return null; // Tampilkan kosong jika belum ada nilai
        });

        // Membuat cell menjadi TextField saat diedit
        column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // Aksi saat selesai mengedit
        column.setOnEditCommit(event -> {
            SiswaKelas siswaKelas = event.getRowValue();
            MataPelajaran mapel = mapelComboBox.getValue();
            Integer nilaiBaru = event.getNewValue();

            if (mapel != null && nilaiBaru != null) {
                // Buat objek nilai baru dan perbarui data di memori (di dalam objek Siswa)
                Nilai n = new Nilai(siswaKelas.getSiswaKelasId(), mapel.getMapelId(), jenisUjian, nilaiBaru);
                siswaKelas.getSiswa().addOrUpdateNilai(n);
                System.out.printf("Memori diperbarui: Siswa %s, Jenis %s, Nilai %d\n", siswaKelas.getSiswa().getNama(), jenisUjian, nilaiBaru);
            }
        });
    }

    @FXML
    private void handleSimpan() {
        MataPelajaran selectedMapel = mapelComboBox.getValue();
        if (selectedMapel == null) {
            showAlert(Alert.AlertType.WARNING, "Gagal", "Pilih mata pelajaran terlebih dahulu.");
            return;
        }

        String checkSql = "SELECT nilai_id FROM nilai WHERE siswa_kelas_id = ? AND mapel_id = ? AND jenis_ujian = ?";
        String insertSql = "INSERT INTO nilai (siswa_kelas_id, mapel_id, jenis_ujian, nilai) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE nilai SET nilai = ? WHERE nilai_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Mulai transaksi

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                for (SiswaKelas sk : siswaDiKelasList) {
                    for (Nilai n : sk.getSiswa().getDaftarNilai()) {
                        // Cek apakah nilai sudah ada di DB
                        checkStmt.setInt(1, n.getSiswaKelasId());
                        checkStmt.setInt(2, n.getMapelId());
                        checkStmt.setString(3, n.getJenisUjian());
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) { // Jika ada, UPDATE
                            int nilaiId = rs.getInt("nilai_id");
                            updateStmt.setInt(1, n.getNilai());
                            updateStmt.setInt(2, nilaiId);
                            updateStmt.addBatch();
                        } else { // Jika tidak ada, INSERT
                            insertStmt.setInt(1, n.getSiswaKelasId());
                            insertStmt.setInt(2, n.getMapelId());
                            insertStmt.setString(3, n.getJenisUjian());
                            insertStmt.setInt(4, n.getNilai());
                            insertStmt.addBatch();
                        }
                    }
                }
                insertStmt.executeBatch();
                updateStmt.executeBatch();
                conn.commit(); // Selesaikan transaksi
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Semua perubahan nilai telah disimpan ke database.");

            } catch (SQLException e) {
                conn.rollback(); // Batalkan transaksi jika ada error
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan saat menyimpan nilai.");
        }
    }

    // Helper untuk konfigurasi ComboBox
    private <T> void configureComboBox(ComboBox<T> comboBox) {
        comboBox.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? null : object.toString();
            }
            @Override
            public T fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/GuruDashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}