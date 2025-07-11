package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Staff;
import org.example.sekolahApp.util.PasswordUtil; // Perlu untuk membuat password hash
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaStaffController implements Initializable {

    @FXML private TableView<Staff> staffTableView;
    @FXML private TableColumn<Staff, Integer> idColumn;
    @FXML private TableColumn<Staff, String> nipColumn;
    @FXML private TableColumn<Staff, String> namaStaffColumn;
    @FXML private TableColumn<Staff, String> jabatanColumn;
    @FXML private TableColumn<Staff, String> teleponColumn;

    @FXML private TextField searchField;
    @FXML private TextField nipField;
    @FXML private TextField namaStaffField;
    @FXML private ComboBox<String> jabatanComboBox;
    @FXML private TextField emailField;
    @FXML private TextField teleponField;
    @FXML private TextArea alamatArea;
    @FXML private DatePicker tanggalLahirPicker;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private Staff selectedStaff = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        nipColumn.setCellValueFactory(new PropertyValueFactory<>("nip"));
        namaStaffColumn.setCellValueFactory(new PropertyValueFactory<>("namaStaff"));
        jabatanColumn.setCellValueFactory(new PropertyValueFactory<>("jabatan"));
        teleponColumn.setCellValueFactory(new PropertyValueFactory<>("nomorTelepon"));

        // Setup Jabatan ComboBox
        jabatanComboBox.setItems(FXCollections.observableArrayList("Administrator", "Guru", "Wali Kelas")); // Sesuaikan jabatan yang ada

        loadStaffData();
        setupSearchFilter();

        staffTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadStaffData() {
        staffList.clear();
        String sql = "SELECT * FROM staff ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Ambil tanggal lahir
                Date sqlDate = rs.getDate("tanggal_lahir");
                LocalDate tanggalLahir = (sqlDate != null) ? sqlDate.toLocalDate() : null; // Tangani NULL

                staffList.add(new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("nip"),
                        rs.getString("nama_staff"),
                        rs.getString("jabatan"),
                        rs.getString("email"),
                        rs.getString("nomor_telepon"),
                        rs.getString("alamat"),
                        tanggalLahir // Gunakan variabel yang sudah ditangani NULL
                ));
            }
            setupSearchFilter();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data staff.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<Staff> filteredData = new FilteredList<>(staffList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(staff -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (staff.getNamaStaff().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (staff.getNip() != null && staff.getNip().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (staff.getJabatan().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Staff> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(staffTableView.comparatorProperty());
        staffTableView.setItems(sortedData);
    }

    private void populateForm(Staff staff) {
        selectedStaff = staff;
        if (staff != null) {
            nipField.setText(staff.getNip());
            namaStaffField.setText(staff.getNamaStaff());
            jabatanComboBox.setValue(staff.getJabatan());
            emailField.setText(staff.getEmail());
            teleponField.setText(staff.getNomorTelepon());
            alamatArea.setText(staff.getAlamat());
            tanggalLahirPicker.setValue(staff.getTanggalLahir());
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

        if (selectedStaff == null) {
            insertStaff();
        } else {
            updateStaff();
        }
    }

    private void insertStaff() {
        // PERBAIKAN: Hapus 'staff_id' dari daftar kolom. Biarkan database yang mengisinya.
        String sql = "INSERT INTO staff (nip, nama_staff, jabatan, email, nomor_telepon, alamat, tanggal_lahir) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStaffStatementParameters(pstmt); // Ini akan mengisi 7 parameter
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data staff berhasil ditambahkan.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan staff. NIP mungkin sudah ada.");
        }
        loadStaffData();
        clearForm();
    }

    private void updateStaff() {
        String sql = "UPDATE staff SET nip = ?, nama_staff = ?, jabatan = ?, email = ?, nomor_telepon = ?, alamat = ?, tanggal_lahir = ? WHERE staff_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setStaffStatementParameters(pstmt);
            pstmt.setInt(8, selectedStaff.getStaffId()); // staff_id ada di parameter ke-8 untuk WHERE clause
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data staff berhasil diperbarui.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data staff.");
        }
        loadStaffData();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        Staff staffToDelete = staffTableView.getSelectionModel().getSelectedItem();
        if (staffToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih staff yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Staff: " + staffToDelete.getNamaStaff());
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus data staff ini beserta akun loginnya (jika ada)? Aksi ini tidak dapat dibatalkan.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteUserSQL = "DELETE FROM users WHERE role IN ('guru', 'wali_kelas', 'admin') AND reference_id = ?";
            String deleteStaffSQL = "DELETE FROM staff WHERE staff_id = ?";

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Mulai transaksi

                // Hapus dari tabel users dulu
                try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSQL)) {
                    pstmtUser.setInt(1, staffToDelete.getStaffId());
                    pstmtUser.executeUpdate();
                }

                // Hapus dari tabel staff
                try (PreparedStatement pstmtStaff = conn.prepareStatement(deleteStaffSQL)) {
                    pstmtStaff.setInt(1, staffToDelete.getStaffId());
                    pstmtStaff.executeUpdate();
                }

                conn.commit(); // Selesaikan transaksi jika semua berhasil
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Staff berhasil dihapus.");

            } catch (SQLException e) {
                // Jika ada error, batalkan semua perubahan
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus staff.");
            }
            loadStaffData();
            clearForm();
        }
    }

    @FXML
    private void handleCreateUser() {
        Staff selectedStaffUser = staffTableView.getSelectionModel().getSelectedItem();
        if (selectedStaffUser == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih staff untuk membuat akun login.");
            return;
        }

        if (selectedStaffUser.getTanggalLahir() == null) {
            showAlert(Alert.AlertType.ERROR, "Data Tidak Lengkap", "Tanggal lahir staff wajib diisi untuk membuat password awal.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Buat Akun Login");
        confirmAlert.setHeaderText("Buat akun login untuk " + selectedStaffUser.getNamaStaff() + "?");
        confirmAlert.setContentText("Username akan diatur sebagai NIP staff (jika ada), dan password awal adalah tanggal lahir (ddMMyyyy). Lanjutkan?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String username = (selectedStaffUser.getNip() != null && !selectedStaffUser.getNip().isEmpty()) ?
                    selectedStaffUser.getNip() : selectedStaffUser.getNamaStaff().replace(" ", "").toLowerCase();
            String rawPassword = selectedStaffUser.getTanggalLahir().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
            String hashedPassword = PasswordUtil.hashPassword(rawPassword);

            String role;
            switch (selectedStaffUser.getJabatan().toLowerCase()) {
                case "administrator": role = "admin"; break;
                case "guru": role = "guru"; break;
                case "wali kelas": role = "wali_kelas"; break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Jabatan Tidak Valid", "Jabatan staff tidak memiliki role login yang ditentukan.");
                    return;
            }

            if (isUserExists(username)) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Akun login untuk username '" + username + "' sudah ada.");
                return;
            }

            String insertUserSQL = "INSERT INTO users (username, password_hash, role, reference_id) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, role);
                pstmt.setInt(4, selectedStaffUser.getStaffId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun login berhasil dibuat:\nUsername: " + username + "\nPassword Awal: " + rawPassword);
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal membuat akun login.");
            }
        }
    }

    private boolean isUserExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
        staffTableView.getSelectionModel().clearSelection();
        selectedStaff = null;
        nipField.clear();
        namaStaffField.clear();
        jabatanComboBox.setValue(null);
        emailField.clear();
        teleponField.clear();
        alamatArea.clear();
        tanggalLahirPicker.setValue(null);
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        return !namaStaffField.getText().isEmpty() &&
                jabatanComboBox.getValue() != null &&
                tanggalLahirPicker.getValue() != null;
    }

    private void setStaffStatementParameters(PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, nipField.getText().isEmpty() ? null : nipField.getText());
        pstmt.setString(2, namaStaffField.getText());
        pstmt.setString(3, jabatanComboBox.getValue());
        pstmt.setString(4, emailField.getText().isEmpty() ? null : emailField.getText());
        pstmt.setString(5, teleponField.getText().isEmpty() ? null : teleponField.getText());
        pstmt.setString(6, alamatArea.getText().isEmpty() ? null : alamatArea.getText());
        pstmt.setDate(7, Date.valueOf(tanggalLahirPicker.getValue()));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
