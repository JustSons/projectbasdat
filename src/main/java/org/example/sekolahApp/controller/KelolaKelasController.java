package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.Staff;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaKelasController implements Initializable {

    @FXML private TableView<Kelas> kelasTableView;
    @FXML private TableColumn<Kelas, Integer> idColumn;
    @FXML private TableColumn<Kelas, String> tahunAjaranColumn; // Akan menampilkan string tahun ajaran
    @FXML private TableColumn<Kelas, String> namaKelasColumn;
    @FXML private TableColumn<Kelas, String> waliKelasColumn; // Akan menampilkan nama staff wali kelas

    @FXML private TextField searchField;
    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private TextField namaKelasField;
    @FXML private ComboBox<Staff> waliKelasComboBox;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList(); // Untuk wali kelas
    private Kelas selectedKelas = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("kelasId"));
        tahunAjaranColumn.setCellValueFactory(cellData -> cellData.getValue().getTahunAjaran().tahunAjaranProperty());
        namaKelasColumn.setCellValueFactory(new PropertyValueFactory<>("namaKelas"));
        waliKelasColumn.setCellValueFactory(cellData -> {
            Staff wali = cellData.getValue().getWaliKelas();
            return (wali != null) ? wali.namaStaffProperty() : new javafx.beans.property.SimpleStringProperty("Belum Ada");
        });

        // Load data for ComboBoxes
        loadTahunAjaranData();
        loadStaffData();

        // Load Kelas data
        loadKelasData();
        setupSearchFilter();

        kelasTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran DESC";
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

    private void loadStaffData() {
        staffList.clear();
        String sql = "SELECT staff_id, nama_staff FROM staff WHERE jabatan = 'Guru' OR jabatan = 'Wali Kelas' ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            waliKelasComboBox.setItems(FXCollections.observableArrayList(staffList)); // Buat salinan agar bisa ditambahkan null
            waliKelasComboBox.getItems().add(0, null); // Tambahkan opsi "Belum Ada" (null) di awal
            waliKelasComboBox.setButtonCell(new ListCell<Staff>() { // Custom cell untuk menampilkan null sebagai "Pilih Wali Kelas"
                @Override
                protected void updateItem(Staff item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item == null ? "Pilih Wali Kelas (Opsional)" : item.getNamaStaff());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data staff (guru).");
        }
    }

    private void loadKelasData() {
        kelasList.clear();
        String sql = "SELECT k.kelas_id, k.nama_kelas, k.tahun_ajaran_id, ta.tahun_ajaran, ta.status AS ta_status, k.wali_kelas_id, s.nama_staff " +
                "FROM kelas k " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "LEFT JOIN staff s ON k.wali_kelas_id = s.staff_id " +
                "ORDER BY ta.tahun_ajaran DESC, k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TahunAjaran ta = new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("ta_status"));
                Staff waliKelas = null;
                if (rs.getObject("wali_kelas_id") != null) {
                    waliKelas = new Staff(rs.getInt("wali_kelas_id"), rs.getString("nama_staff"));
                }
                kelasList.add(new Kelas(
                        rs.getInt("kelas_id"),
                        rs.getString("nama_kelas"),
                        ta,
                        waliKelas
                ));
            }
            setupSearchFilter(); // Re-apply filter
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<Kelas> filteredData = new FilteredList<>(kelasList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(kelas -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (kelas.getNamaKelas().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (kelas.getTahunAjaran().getTahunAjaran().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (kelas.getWaliKelas() != null && kelas.getWaliKelas().getNamaStaff().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Kelas> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(kelasTableView.comparatorProperty());
        kelasTableView.setItems(sortedData);
    }

    private void populateForm(Kelas kelas) {
        selectedKelas = kelas;
        if (kelas != null) {
            tahunAjaranComboBox.setValue(kelas.getTahunAjaran());
            namaKelasField.setText(kelas.getNamaKelas());
            waliKelasComboBox.setValue(kelas.getWaliKelas()); // Set objek Staff
            saveButton.setText("Update");
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Semua field wajib diisi dan format benar.");
            return;
        }

        if (selectedKelas == null) {
            insertKelas();
        } else {
            updateKelas();
        }
    }

    private void insertKelas() {
        String sql = "INSERT INTO kelas (nama_kelas, tahun_ajaran_id, wali_kelas_id) VALUES (?, ?, ?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, namaKelasField.getText());
                pstmt.setInt(2, tahunAjaranComboBox.getValue().getTahunAjaranId());
                if (waliKelasComboBox.getValue() != null) {
                    pstmt.setInt(3, waliKelasComboBox.getValue().getStaffId());
                } else {
                    pstmt.setNull(3, Types.INTEGER); // Set NULL jika tidak ada wali kelas
                }
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas berhasil ditambahkan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan Kelas. Nama Kelas mungkin sudah ada di Tahun Ajaran ini.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadKelasData();
        clearForm();
    }

    private void updateKelas() {
        String sql = "UPDATE kelas SET nama_kelas = ?, tahun_ajaran_id = ?, wali_kelas_id = ? WHERE kelas_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, namaKelasField.getText());
                pstmt.setInt(2, tahunAjaranComboBox.getValue().getTahunAjaranId());
                if (waliKelasComboBox.getValue() != null) {
                    pstmt.setInt(3, waliKelasComboBox.getValue().getStaffId());
                } else {
                    pstmt.setNull(3, Types.INTEGER); // Set NULL jika tidak ada wali kelas
                }
                pstmt.setInt(4, selectedKelas.getKelasId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas berhasil diperbarui.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui Kelas. Nama Kelas mungkin sudah ada di Tahun Ajaran ini.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadKelasData();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        Kelas kelasToDelete = kelasTableView.getSelectionModel().getSelectedItem();
        if (kelasToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih Kelas yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Kelas: " + kelasToDelete.getNamaKelas() + " (" + kelasToDelete.getTahunAjaran().getTahunAjaran() + ")");
        confirmAlert.setContentText("Menghapus Kelas ini juga akan menghapus semua Jadwal dan Alokasi Siswa (siswa_kelas) yang terhubung dengannya. Apakah Anda yakin?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM kelas WHERE kelas_id = ?";
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, kelasToDelete.getKelasId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas berhasil dihapus.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus Kelas. Pastikan tidak ada siswa di kelas ini atau jadwal yang masih terhubung.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            loadKelasData();
            clearForm();
        }
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml", 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    private void clearForm() {
        kelasTableView.getSelectionModel().clearSelection();
        selectedKelas = null;
        tahunAjaranComboBox.setValue(null);
        namaKelasField.clear();
        waliKelasComboBox.setValue(null);
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        String namaKelas = namaKelasField.getText();
        TahunAjaran tahunAjaran = tahunAjaranComboBox.getValue();

        if (namaKelas.isEmpty() || tahunAjaran == null) {
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}