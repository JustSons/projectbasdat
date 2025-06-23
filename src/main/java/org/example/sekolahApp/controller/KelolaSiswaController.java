package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Siswa;
import org.example.sekolahApp.util.PasswordUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaSiswaController implements Initializable {

    @FXML private TableView<Siswa> siswaTableView;
    @FXML private TableColumn<Siswa, Integer> idColumn;
    @FXML private TableColumn<Siswa, String> nisColumn;
    @FXML private TableColumn<Siswa, String> namaColumn;

    @FXML private TextField searchField;
    @FXML private TextField nisField;
    @FXML private TextField namaField;
    @FXML private DatePicker tanggalLahirPicker;
    @FXML private ComboBox<String> jenisKelaminComboBox;
    @FXML private TextField agamaField;
    @FXML private TextArea alamatArea;
    @FXML private TextField namaOrangTuaField;
    @FXML private TextField teleponOrangTuaField;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<Siswa> siswaList = FXCollections.observableArrayList();
    private Siswa selectedSiswa = null;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup kolom tabel
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));

        // Setup ComboBox jenis kelamin
        jenisKelaminComboBox.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));

        // Muat data siswa dari database
        loadSiswaData();

        // Setup filter pencarian
        setupSearchFilter();

        // Tambahkan listener untuk mendeteksi pilihan di tabel
        siswaTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadSiswaData() {
        siswaList.clear();
        String sql = "SELECT * FROM siswa ORDER BY nama_siswa ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = 0; // Untuk menghitung data yang dimuat
            while (rs.next()) {
                siswaList.add(new Siswa(
                        rs.getInt("siswa_id"),
                        rs.getString("nis"),
                        rs.getString("nama_siswa"), // Pastikan nama kolom di DB persis "nama_siswa"
                        rs.getString("alamat"),
                        rs.getString("jenis_kelamin"),
                        rs.getString("agama"),
                        rs.getDate("tanggal_lahir").toLocalDate(),
                        rs.getString("nama_orang_tua"),
                        rs.getString("telepon_orang_tua")
                ));
                count++;
            }
            System.out.println("DEBUG: loadSiswaData() selesai. Total siswa yang dimuat ke siswaList: " + count);
            System.out.println("DEBUG: Ukuran siswaList setelah dimuat: " + siswaList.size());

            // Pastikan setupSearchFilter() dipanggil ulang setelah data baru dimuat
            setupSearchFilter(); // Ini akan memperbarui FilteredList/SortedList
            // Jika ini belum ada di akhir loadSiswaData(), tambahkan!

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa.");
            System.err.println("DEBUG: Terjadi SQLException saat memuat data siswa: " + e.getMessage());
        }
    }

    private void setupSearchFilter() {
        FilteredList<Siswa> filteredData = new FilteredList<>(siswaList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(siswa -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (siswa.getNama().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (siswa.getNis().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Siswa> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(siswaTableView.comparatorProperty());
        siswaTableView.setItems(sortedData); // Pastikan ini selalu dipanggil
    }

    private void populateForm(Siswa siswa) {
        selectedSiswa = siswa;
        if (siswa != null) {
            nisField.setText(siswa.getNis());
            namaField.setText(siswa.getNama());
            tanggalLahirPicker.setValue(siswa.getTanggalLahir());
            jenisKelaminComboBox.setValue(siswa.getJenisKelamin());
            agamaField.setText(siswa.getAgama());
            alamatArea.setText(siswa.getAlamat());
            namaOrangTuaField.setText(siswa.getNamaOrangTua());
            teleponOrangTuaField.setText(siswa.getTeleponOrangTua());
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

        if (selectedSiswa == null) {
            // Mode Tambah
            insertSiswa();
        } else {
            // Mode Update
            updateSiswa();
        }
    }

    private void insertSiswa() {
        String insertSiswaSQL = "INSERT INTO siswa (nis, nama_siswa, alamat, jenis_kelamin, agama, tanggal_lahir, nama_orang_tua, telepon_orang_tua) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertUserSQL = "INSERT INTO users (username, password_hash, role, reference_id) VALUES (?, ?, 'siswa', ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            // Matikan auto-commit untuk transaksi
            conn.setAutoCommit(false);

            // 1. Insert ke tabel siswa
            int newSiswaId;
            try (PreparedStatement pstmtSiswa = conn.prepareStatement(insertSiswaSQL, Statement.RETURN_GENERATED_KEYS)) {
                setSiswaStatementParameters(pstmtSiswa);
                pstmtSiswa.executeUpdate();

                // Ambil ID siswa yang baru dibuat
                try (ResultSet generatedKeys = pstmtSiswa.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newSiswaId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Gagal membuat siswa, tidak mendapatkan ID.");
                    }
                }
            }

            // 2. Insert ke tabel users
            try (PreparedStatement pstmtUser = conn.prepareStatement(insertUserSQL)) {
                String siswaUsername = nisField.getText();
                String siswaRawPassword = tanggalLahirPicker.getValue().format(DateTimeFormatter.ofPattern("ddMMyyyy"));
                String siswaHashedPassword = PasswordUtil.hashPassword(siswaRawPassword); // <--- TAMBAHKAN HASHING DI SINI!

                pstmtUser.setString(1, "s" + siswaUsername);
                pstmtUser.setString(2, siswaHashedPassword); // Gunakan yang sudah di-hash
                pstmtUser.setInt(3, newSiswaId);
                pstmtUser.executeUpdate();
            }

            // Jika semua berhasil, commit transaksi
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data siswa dan akun login berhasil ditambahkan.");

        } catch (SQLException e) {
            // Jika ada error, rollback transaksi
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan siswa. NIS mungkin sudah ada.");
        } finally {
            // Kembalikan ke mode auto-commit
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        loadSiswaData();
        clearForm();
    }

    private void updateSiswa() {
        String sql = "UPDATE siswa SET nis = ?, nama_siswa = ?, alamat = ?, jenis_kelamin = ?, agama = ?, tanggal_lahir = ?, nama_orang_tua = ?, telepon_orang_tua = ? WHERE siswa_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setSiswaStatementParameters(pstmt);
            pstmt.setInt(9, selectedSiswa.getId());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data siswa berhasil diperbarui.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data siswa.");
        }
        loadSiswaData();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        Siswa siswaToDelete = siswaTableView.getSelectionModel().getSelectedItem();
        if (siswaToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih siswa yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Siswa: " + siswaToDelete.getNama());
        confirmAlert.setContentText("Apakah Anda yakin ingin menghapus data siswa ini beserta akun loginnya? Aksi ini tidak dapat dibatalkan.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deleteUserSQL = "DELETE FROM users WHERE role = 'siswa' AND reference_id = ?";
            String deleteSiswaSQL = "DELETE FROM siswa WHERE siswa_id = ?";

            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Hapus dari tabel users dulu
                try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSQL)) {
                    pstmtUser.setInt(1, siswaToDelete.getId());
                    pstmtUser.executeUpdate();
                }

                // Hapus dari tabel siswa
                try (PreparedStatement pstmtSiswa = conn.prepareStatement(deleteSiswaSQL)) {
                    pstmtSiswa.setInt(1, siswaToDelete.getId());
                    pstmtSiswa.executeUpdate();
                }

                conn.commit();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Siswa berhasil dihapus.");

            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus siswa.");
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            loadSiswaData();
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
        siswaTableView.getSelectionModel().clearSelection();
        selectedSiswa = null;
        nisField.clear();
        namaField.clear();
        tanggalLahirPicker.setValue(null);
        jenisKelaminComboBox.setValue(null);
        agamaField.clear();
        alamatArea.clear();
        namaOrangTuaField.clear();
        teleponOrangTuaField.clear();
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        return !nisField.getText().isEmpty() &&
                !namaField.getText().isEmpty() &&
                tanggalLahirPicker.getValue() != null &&
                jenisKelaminComboBox.getValue() != null;
    }

    private void setSiswaStatementParameters(PreparedStatement pstmt) throws SQLException {
        pstmt.setString(1, nisField.getText());
        pstmt.setString(2, namaField.getText());
        pstmt.setString(3, alamatArea.getText());
        pstmt.setString(4, jenisKelaminComboBox.getValue());
        pstmt.setString(5, agamaField.getText());
        pstmt.setDate(6, Date.valueOf(tanggalLahirPicker.getValue()));
        pstmt.setString(7, namaOrangTuaField.getText());
        pstmt.setString(8, teleponOrangTuaField.getText());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}