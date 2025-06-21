package org.example.sekolahApp.controller;

import javafx.scene.layout.HBox;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Staff;
import org.example.sekolahApp.model.Jadwal;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.MataPelajaran;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.UserSession;
import org.example.sekolahApp.util.SceneManager; // Pastikan ini diimpor
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.layout.GridPane;
import java.io.IOException; // Pastikan ini diimpor
import java.net.URL;
import java.sql.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;
import java.util.Optional; // Pastikan ini diimpor

public class KelolaJadwalController implements Initializable {

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
    @FXML private ComboBox<MataPelajaran> mapelComboBox;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button handleDeleteButton; // fx:id untuk tombol Hapus
    @FXML private GridPane formGridPane; // fx:id untuk GridPane form
    @FXML private HBox buttonHBox; // fx:id untuk HBox tombol Simpan/Bat

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mapelList = FXCollections.observableArrayList();
    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();

    private Jadwal selectedJadwal = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalId"));
        kelasColumn.setCellValueFactory(cellData -> cellData.getValue().getKelas().namaKelasProperty());
        hariColumn.setCellValueFactory(new PropertyValueFactory<>("hari"));
        jamMulaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamMulai"));
        jamSelesaiColumn.setCellValueFactory(new PropertyValueFactory<>("jamSelesai"));
        guruColumn.setCellValueFactory(cellData -> cellData.getValue().getGuru().namaStaffProperty());
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().getMapel().namaMapelProperty());

        // Setup ComboBoxes
        hariComboBox.setItems(FXCollections.observableArrayList("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"));

        loadTahunAjaranData();
        loadKelasData();
        loadStaffData();
        loadMapelData();
        loadJadwalData();

        setupSearchFilter();

        jadwalTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));

        // --- Kontrol Akses Berdasarkan Role ---
        UserSession session = UserSession.getInstance();
        if (session.isGuru() || session.isSiswa()) { // Guru atau Siswa hanya bisa melihat
            setFormReadOnly(true);
            saveButton.setVisible(false);
            clearButton.setVisible(false);
            handleDeleteButton.setVisible(false);
            // Nonaktifkan selection listener jika tidak ada edit
            jadwalTableView.getSelectionModel().selectedItemProperty().removeListener(
                    (observable, oldValue, newValue) -> populateForm(newValue));
            jadwalTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE); // Pastikan mode single selection
        } else { // Admin atau Wali Kelas (jika diizinkan mengedit jadwal)
            setFormReadOnly(false);
            saveButton.setVisible(true);
            clearButton.setVisible(true);
            handleDeleteButton.setVisible(true);
        }
    }

    private void setFormReadOnly(boolean readOnly) {
        kelasComboBox.setDisable(readOnly);
        hariComboBox.setDisable(readOnly);
        jamMulaiField.setEditable(!readOnly);
        jamSelesaiField.setEditable(!readOnly);
        guruComboBox.setDisable(readOnly);
        mapelComboBox.setDisable(readOnly);
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void loadKelasData() {
        kelasList.clear();
        String sql = "SELECT k.kelas_id, k.nama_kelas, k.tahun_ajaran_id, ta.tahun_ajaran, k.wali_kelas_id, s.nama_staff " +
                "FROM kelas k " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "LEFT JOIN staff s ON k.wali_kelas_id = s.staff_id " +
                "ORDER BY k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), null);
                Staff waliKelas = null;
                if (rs.getObject("wali_kelas_id") != null) {
                    waliKelas = new Staff(rs.getInt("wali_kelas_id"), rs.getString("nama_staff"));
                }
                kelasList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, waliKelas));
            }
            kelasComboBox.setItems(kelasList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void loadStaffData() {
        staffList.clear();
        String sql = "SELECT staff_id, nama_staff, jabatan FROM staff WHERE jabatan = 'Guru' OR jabatan = 'Wali Kelas' ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            guruComboBox.setItems(staffList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru/staff.");
        }
    }

    private void loadMapelData() {
        mapelList.clear();
        String sql = "SELECT mapel_id, kode_mapel, nama_mapel FROM mata_pelajaran ORDER BY nama_mapel ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapelList.add(new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel")));
            }
            mapelComboBox.setItems(mapelList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data mata pelajaran.");
        }
    }

    private void loadJadwalData() {
        jadwalList.clear();
        String sql = "SELECT j.jadwal_id, k.kelas_id, k.nama_kelas, ta.tahun_ajaran_id, ta.tahun_ajaran, " +
                "j.hari, j.jam_mulai, j.jam_selesai, s.staff_id, s.nama_staff, " +
                "mp.mapel_id, mp.kode_mapel, mp.nama_mapel " +
                "FROM jadwal j " +
                "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "JOIN staff s ON j.guru_id = s.staff_id " +
                "JOIN mata_pelajaran mp ON j.mapel_id = mp.mapel_id " +
                "ORDER BY j.hari, j.jam_mulai";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), null);
                Kelas kelas = new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), ta, null);
                Staff guru = new Staff(rs.getInt("staff_id"), rs.getString("nama_staff"));
                MataPelajaran mapel = new MataPelajaran(rs.getInt("mapel_id"), rs.getString("kode_mapel"), rs.getString("nama_mapel"));

                jadwalList.add(new Jadwal(
                        rs.getInt("jadwal_id"),
                        kelas,
                        rs.getString("hari"),
                        rs.getTime("jam_mulai").toLocalTime(),
                        rs.getTime("jam_selesai").toLocalTime(),
                        guru,
                        mapel
                ));
            }
            // Disini tidak perlu panggil setupSearchFilter lagi, cukup di initialize()
            // TableView akan otomatis terupdate karena jadwalList adalah ObservableList
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data jadwal.");
        }
    }

    // --- METODE YANG HILANG DI SINI ---

    private void setupSearchFilter() {
        FilteredList<Jadwal> filteredData = new FilteredList<>(jadwalList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(jadwal -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (jadwal.getKelas().getNamaKelas().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (jadwal.getGuru().getNamaStaff().toLowerCase().contains(lowerCaseFilter)) { // Ganti getNamaGuru jadi getNamaStaff
                    return true;
                } else if (jadwal.getMapel().getNamaMapel().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (jadwal.getHari().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
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
            mapelComboBox.setValue(jadwal.getMapel());
            saveButton.setText("Update");
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Semua field wajib diisi.");
            return;
        }

        if (selectedJadwal == null) {
            insertJadwal();
        } else {
            updateJadwal();
        }
    }

    private void insertJadwal() {
        String sql = "INSERT INTO jadwal (kelas_id, hari, jam_mulai, jam_selesai, guru_id, mapel_id) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setJadwalStatementParameters(pstmt);
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil ditambahkan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan jadwal. Pastikan tidak ada jadwal bentrok.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadJadwalData();
        clearForm();
    }

    private void updateJadwal() {
        String sql = "UPDATE jadwal SET kelas_id = ?, hari = ?, jam_mulai = ?, jam_selesai = ?, guru_id = ?, mapel_id = ? WHERE jadwal_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setJadwalStatementParameters(pstmt);
                pstmt.setInt(7, selectedJadwal.getJadwalId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil diperbarui.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui jadwal.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadJadwalData();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        Jadwal jadwalToDelete = jadwalTableView.getSelectionModel().getSelectedItem();
        if (jadwalToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih jadwal yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Jadwal:");
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus jadwal ini?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM jadwal WHERE jadwal_id = ?";
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, jadwalToDelete.getJadwalId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil dihapus.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus jadwal.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            loadJadwalData();
            clearForm();
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
        mapelComboBox.setValue(null);
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        if (kelasComboBox.getValue() == null || hariComboBox.getValue() == null ||
                jamMulaiField.getText().isEmpty() || jamSelesaiField.getText().isEmpty() ||
                guruComboBox.getValue() == null || mapelComboBox.getValue() == null) {
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
        pstmt.setInt(6, mapelComboBox.getValue().getMapelId());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}