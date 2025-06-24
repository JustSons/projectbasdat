package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Jadwal;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.MataPelajaran;
import org.example.sekolahApp.model.Staff;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;

import java.io.IOException;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class KelolaJadwalController {

    @FXML private TableView<Jadwal> jadwalTableView;
    @FXML private TableColumn<Jadwal, Integer> idColumn;
    @FXML private TableColumn<Jadwal, String> kelasColumn;
    @FXML private TableColumn<Jadwal, String> hariColumn;
    @FXML private TableColumn<Jadwal, LocalTime> jamMulaiColumn;
    @FXML private TableColumn<Jadwal, LocalTime> jamSelesaiColumn;
    @FXML private TableColumn<Jadwal, String> guruColumn;
    @FXML private TableColumn<Jadwal, String> mapelColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private ComboBox<String> hariComboBox;
    @FXML private TextField jamMulaiField;
    @FXML private TextField jamSelesaiField;
    @FXML private ComboBox<Staff> guruComboBox;
    @FXML private ComboBox<MataPelajaran> mataPelajaranComboBox;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button handleDeleteButton;

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mataPelajaranList = FXCollections.observableArrayList();
    private Jadwal selectedJadwal = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadInitialData();
        setupSearchFilter();
        setupForm();
    }

    private void setupForm() {
        hariComboBox.setItems(FXCollections.observableArrayList("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"));

        jadwalTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> populateForm(newVal));

        // Atur visibilitas dan fungsionalitas berdasarkan peran pengguna
        UserSession session = UserSession.getInstance();
        boolean isAdmin = session.isAdmin();
        saveButton.setVisible(isAdmin);
        clearButton.setVisible(isAdmin);
        handleDeleteButton.setVisible(isAdmin);
        // Form di-disable jika bukan admin
        kelasComboBox.setDisable(!isAdmin);
        hariComboBox.setDisable(!isAdmin);
        jamMulaiField.setEditable(isAdmin);
        jamSelesaiField.setEditable(isAdmin);
        guruComboBox.setDisable(!isAdmin);
        mataPelajaranComboBox.setDisable(!isAdmin);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalId"));
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        jamSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        // Mengambil data dari objek relasi
        kelasColumn.setCellValueFactory(cellData -> cellData.getValue().getKelas().namaKelasProperty());
        guruColumn.setCellValueFactory(cellData -> cellData.getValue().getGuru().namaStaffProperty());
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().getMapel().namaMapelProperty());
    }

    private void loadInitialData() {
        loadKelasData();
        loadStaffData();
        loadMataPelajaranData();
        loadJadwalData();
    }

    private void loadKelasData() {
        kelasList.clear();
        String sql = "SELECT k.kelas_id, k.nama_kelas, ta.tahun_ajaran_id, ta.tahun_ajaran " +
                "FROM kelas k JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "ORDER BY k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"));
                kelasList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, null));
            }
            kelasComboBox.setItems(kelasList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void loadStaffData() {
        staffList.clear();
        String sql = "SELECT staff_id, nama_staff FROM staff WHERE jabatan = 'Guru' OR jabatan = 'Wali Kelas' ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            guruComboBox.setItems(staffList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru.");
        }
    }

    private void loadMataPelajaranData() {
        mataPelajaranList.clear();
        String sql = "SELECT mapel_id, kode_mapel, nama_mapel FROM mata_pelajaran ORDER BY nama_mapel ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mataPelajaranList.add(new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel")));
            }
            mataPelajaranComboBox.setItems(mataPelajaranList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data mata pelajaran.");
        }
    }

    private void loadJadwalData() {
        jadwalList.clear();
        String sql = "SELECT j.jadwal_id, j.hari, j.jam_mulai, j.jam_selesai, " +
                "k.kelas_id, k.nama_kelas, ta.tahun_ajaran_id, ta.tahun_ajaran, " +
                "s.staff_id, s.nama_staff, " +
                "mp.mapel_id, mp.kode_mapel, mp.nama_mapel " +
                "FROM jadwal j " +
                "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "JOIN staff s ON j.guru_id = s.staff_id " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "ORDER BY j.hari, j.jam_mulai";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"));
                Kelas kelas = new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, null);
                Staff guru = new Staff(rs.getInt("staff_id"), rs.getString("nama_staff"));
                MataPelajaran mapel = new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel"));
                jadwalList.add(new Jadwal(rs.getInt("jadwal_id"), rs.getString("hari"), rs.getTime("jam_mulai").toLocalTime(), rs.getTime("jam_selesai").toLocalTime(), kelas, mapel, guru));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data jadwal.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<Jadwal> filteredData = new FilteredList<>(jadwalList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(jadwal -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                if (jadwal.getKelas().getNamaKelas().toLowerCase().contains(lowerCaseFilter)) return true;
                if (jadwal.getGuru().getNamaStaff().toLowerCase().contains(lowerCaseFilter)) return true;
                if (jadwal.getMapel().getNamaMapel().toLowerCase().contains(lowerCaseFilter)) return true;
                return jadwal.getHari().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<Jadwal> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(jadwalTableView.comparatorProperty());
        jadwalTableView.setItems(sortedData);
    }

    private void populateForm(Jadwal jadwal) {
        selectedJadwal = jadwal;
        if (jadwal == null) {
            clearForm();
        } else {
            // Cocokkan objek di ComboBox berdasarkan ID, bukan referensi objek
            kelasComboBox.setValue(kelasList.stream().filter(k -> k.getKelasId() == jadwal.getKelas().getKelasId()).findFirst().orElse(null));
            guruComboBox.setValue(staffList.stream().filter(s -> s.getStaffId() == jadwal.getGuru().getStaffId()).findFirst().orElse(null));
            mataPelajaranComboBox.setValue(mataPelajaranList.stream().filter(m -> m.getMapelId() == jadwal.getMapel().getMapelId()).findFirst().orElse(null));

            hariComboBox.setValue(jadwal.getHari());
            jamMulaiField.setText(jadwal.getJamMulai().toString());
            jamSelesaiField.setText(jadwal.getJamSelesai().toString());
            saveButton.setText("Update");
        }
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) return;

        if (selectedJadwal == null) {
            insertJadwal();
        } else {
            updateJadwal();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedJadwal == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih jadwal yang akan dihapus.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus jadwal ini?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM jadwal WHERE jadwal_id = ?";
                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedJadwal.getJadwalId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil dihapus.");
                    loadJadwalData();
                    clearForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus jadwal.");
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void insertJadwal() {
        String sql = "INSERT INTO jadwal (kelas_id, mapel_id, guru_id, hari, jam_mulai, jam_selesai) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setJadwalStatementParameters(pstmt);
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil ditambahkan.");
            loadJadwalData();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan jadwal.");
        }
    }

    private void updateJadwal() {
        String sql = "UPDATE jadwal SET kelas_id=?, mapel_id=?, guru_id=?, hari=?, jam_mulai=?, jam_selesai=? WHERE jadwal_id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setJadwalStatementParameters(pstmt);
            pstmt.setInt(7, selectedJadwal.getJadwalId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil diperbarui.");
            loadJadwalData();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui jadwal.");
        }
    }

    // GANTI metode lama Anda dengan versi yang sudah diperbaiki ini.
    private void setJadwalStatementParameters(PreparedStatement pstmt) throws SQLException {
        // Ambil nilai dari setiap komponen form
        Kelas kelas = kelasComboBox.getValue();
        MataPelajaran mapel = mataPelajaranComboBox.getValue();
        Staff guru = guruComboBox.getValue();
        String hari = hariComboBox.getValue();

        // PERBAIKAN UTAMA: Konversi teks waktu menggunakan LocalTime.parse() terlebih dahulu
        LocalTime jamMulai = LocalTime.parse(jamMulaiField.getText());
        LocalTime jamSelesai = LocalTime.parse(jamSelesaiField.getText());

        // Set parameter ke statement SQL
        pstmt.setInt(1, kelas.getKelasId());
        pstmt.setInt(2, mapel.getMapelId());
        pstmt.setInt(3, guru.getStaffId());
        pstmt.setString(4, hari);
        pstmt.setTime(5, Time.valueOf(jamMulai));
        pstmt.setTime(6, Time.valueOf(jamSelesai));
    }

    private void clearForm() {
        selectedJadwal = null;
        jadwalTableView.getSelectionModel().clearSelection();
        kelasComboBox.setValue(null);
        hariComboBox.setValue(null);
        jamMulaiField.clear();
        jamSelesaiField.clear();
        guruComboBox.setValue(null);
        mataPelajaranComboBox.setValue(null);
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        String errorMessage = "";
        if (kelasComboBox.getValue() == null) errorMessage += "Kelas harus dipilih.\n";
        if (hariComboBox.getValue() == null) errorMessage += "Hari harus dipilih.\n";
        if (guruComboBox.getValue() == null) errorMessage += "Guru harus dipilih.\n";
        if (mataPelajaranComboBox.getValue() == null) errorMessage += "Mata pelajaran harus dipilih.\n";
        if (jamMulaiField.getText().isBlank()) errorMessage += "Jam mulai tidak boleh kosong.\n";
        if (jamSelesaiField.getText().isBlank()) errorMessage += "Jam selesai tidak boleh kosong.\n";

        try {
            LocalTime.parse(jamMulaiField.getText());
            LocalTime.parse(jamSelesaiField.getText());
        } catch (DateTimeParseException e) {
            errorMessage += "Format waktu salah. Gunakan HH:MM (contoh: 08:00).\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", errorMessage);
            return false;
        }
    }

    @FXML
    private void handleBack() throws IOException {
        SceneManager.getInstance().loadScene(UserSession.getInstance().getDashboardFxml());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}