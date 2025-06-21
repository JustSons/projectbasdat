package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Jadwal;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.MataPelajaran;
import org.example.sekolahApp.model.Staff;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaJadwalController implements Initializable {

    // --- FXML Declarations ---
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
    @FXML private GridPane formGridPane; // Pastikan fx:id ini ada di FXML

    // --- ObservableList Declarations (No Duplicates) ---
    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mataPelajaranList = FXCollections.observableArrayList();

    private Jadwal selectedJadwal = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        setupComboBoxes();
        loadInitialData();
        setupSearchFilter();

        jadwalTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));

        UserSession session = UserSession.getInstance();
        if (session.isGuru() || session.isSiswa()) {
            setFormReadOnly(true);
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalId"));
        kelasColumn.setCellValueFactory(cellData -> cellData.getValue().getKelas().namaKelasProperty());
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        jamSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        guruColumn.setCellValueFactory(cellData -> cellData.getValue().getGuru().namaStaffProperty());
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().getMapel().namaMapelProperty());
    }

    private void setupComboBoxes() {
        hariComboBox.setItems(FXCollections.observableArrayList("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"));
    }

    private void loadInitialData() {
        loadKelasData();
        loadStaffData();
        loadMataPelajaranData(); // Hanya satu pemanggilan yang benar
        loadJadwalData();
    }

    private void setFormReadOnly(boolean readOnly) {
        // Disable seluruh form, dan atur visibilitas tombol
        formGridPane.setDisable(readOnly);
        saveButton.setVisible(!readOnly);
        clearButton.setVisible(!readOnly);
        handleDeleteButton.setVisible(!readOnly);
    }

    private void loadKelasData() {
        kelasList.clear();
        String sql = "SELECT k.kelas_id, k.nama_kelas, k.tahun_ajaran_id, ta.tahun_ajaran " +
                "FROM kelas k JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "WHERE ta.status = 'aktif' ORDER BY k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), "aktif");
                // Constructor Kelas butuh objek Staff, kita bisa berikan null jika tidak diperlukan di sini
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
                // Constructor Staff yang simpel (hanya id dan nama)
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            guruComboBox.setItems(staffList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru/staff.");
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
        String sql = "SELECT j.jadwal_id, k.kelas_id, k.nama_kelas, ta.tahun_ajaran_id, ta.tahun_ajaran, ta.status, " +
                "j.hari, j.jam_mulai, j.jam_selesai, s.staff_id, s.nama_staff, " +
                "mp.mapel_id, mp.kode_mapel, mp.nama_mapel " +
                "FROM jadwal j " +
                "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "JOIN staff s ON j.guru_id = s.staff_id " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "ORDER BY j.hari, j.jam_mulai";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("status"));
                Kelas kelas = new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, null);
                Staff guru = new Staff(rs.getInt("staff_id"), rs.getString("nama_staff"));
                MataPelajaran mapel = new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel"));
                jadwalList.add(new Jadwal(rs.getInt("jadwal_id"), kelas, rs.getString("hari"), rs.getTime("jam_mulai").toLocalTime(), rs.getTime("jam_selesai").toLocalTime(), guru, mapel));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data jadwal.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<Jadwal> filteredData = new FilteredList<>(jadwalList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(jadwal -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (jadwal.getKelas().getNamaKelas().toLowerCase().contains(lowerCaseFilter)) return true;
                if (jadwal.getGuru().getNamaStaff().toLowerCase().contains(lowerCaseFilter)) return true;
                if (jadwal.getMapel().getNamaMapel().toLowerCase().contains(lowerCaseFilter)) return true;
                if (jadwal.getHari().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<Jadwal> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(jadwalTableView.comparatorProperty());
        jadwalTableView.setItems(sortedData);
    }

    private void populateForm(Jadwal jadwal) {
        selectedJadwal = jadwal;
        if (jadwal != null) {
            kelasComboBox.setValue(jadwal.getKelas());
            hariComboBox.setValue(jadwal.getHari());
            jamMulaiField.setText(jadwal.getJamMulai().toString());
            jamSelesaiField.setText(jadwal.getJamSelesai().toString());
            guruComboBox.setValue(jadwal.getGuru());
            mataPelajaranComboBox.setValue(jadwal.getMapel());
            saveButton.setText("Update");
        } else {
            clearForm();
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

    private void insertJadwal() {
        String sql = "INSERT INTO jadwal (kelas_id, hari, jam_mulai, jam_selesai, guru_id, mapel_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setJadwalStatementParameters(pstmt);
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil ditambahkan.");
            loadJadwalData();
            clearForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan jadwal. Pastikan tidak ada jadwal bentrok.");
        }
    }

    private void updateJadwal() {
        String sql = "UPDATE jadwal SET kelas_id = ?, hari = ?, jam_mulai = ?, jam_selesai = ?, guru_id = ?, mapel_id = ? WHERE jadwal_id = ?";
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

    @FXML
    private void handleDelete() {
        Jadwal jadwalToDelete = jadwalTableView.getSelectionModel().getSelectedItem();
        if (jadwalToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih jadwal yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Apakah Anda yakin ingin menghapus jadwal ini?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM jadwal WHERE jadwal_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, jadwalToDelete.getJadwalId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil dihapus.");
                loadJadwalData();
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus jadwal.");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        jadwalTableView.getSelectionModel().clearSelection();
        selectedJadwal = null;
        kelasComboBox.setValue(null);
        hariComboBox.setValue(null);
        jamMulaiField.clear();
        jamSelesaiField.clear();
        guruComboBox.setValue(null);
        mataPelajaranComboBox.setValue(null);
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        if (kelasComboBox.getValue() == null || hariComboBox.getValue() == null ||
                jamMulaiField.getText().isEmpty() || jamSelesaiField.getText().isEmpty() ||
                guruComboBox.getValue() == null || mataPelajaranComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Semua field wajib diisi.");
            return false;
        }
        try {
            LocalTime.parse(jamMulaiField.getText());
            LocalTime.parse(jamSelesaiField.getText());
            return true;
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Format Waktu Salah", "Gunakan format HH:MM untuk jam (contoh: 08:00).");
            return false;
        }
    }

    private void setJadwalStatementParameters(PreparedStatement pstmt) throws SQLException {
        pstmt.setInt(1, kelasComboBox.getValue().getKelasId());
        pstmt.setString(2, hariComboBox.getValue());
        pstmt.setTime(3, Time.valueOf(LocalTime.parse(jamMulaiField.getText())));
        pstmt.setTime(4, Time.valueOf(LocalTime.parse(jamSelesaiField.getText())));
        pstmt.setInt(5, guruComboBox.getValue().getStaffId());
        pstmt.setInt(6, mataPelajaranComboBox.getValue().getMapelId());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}