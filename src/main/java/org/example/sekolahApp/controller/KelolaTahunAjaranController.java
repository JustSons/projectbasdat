package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
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

public class KelolaTahunAjaranController implements Initializable {

    @FXML private TableView<TahunAjaran> tahunAjaranTableView;
    @FXML private TableColumn<TahunAjaran, Integer> idColumn;
    @FXML private TableColumn<TahunAjaran, String> tahunAjaranColumn;

    @FXML private TextField searchField;
    @FXML private TextField tahunAjaranField;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private TahunAjaran selectedTahunAjaran = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setup Table Columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("tahunAjaranId"));
        tahunAjaranColumn.setCellValueFactory(new PropertyValueFactory<>("tahunAjaran"));


        loadTahunAjaranData();
        setupSearchFilter();

        tahunAjaranTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran FROM tahun_ajaran ORDER BY tahun_ajaran DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(
                        rs.getInt("tahun_ajaran_id"),
                        rs.getString("tahun_ajaran")
                ));
            }
            // Penting: re-bind items to TableView after loading
            setupSearchFilter(); // Ini akan memperbarui FilteredList/SortedList yang terhubung ke TableView
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<TahunAjaran> filteredData = new FilteredList<>(tahunAjaranList, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(ta -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (ta.getTahunAjaran().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<TahunAjaran> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tahunAjaranTableView.comparatorProperty());
        tahunAjaranTableView.setItems(sortedData);
    }

    private void populateForm(TahunAjaran ta) {
        selectedTahunAjaran = ta;
        if (ta != null) {
            tahunAjaranField.setText(ta.getTahunAjaran());
            saveButton.setText("Update");
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        if (!isFormValid()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Semua field wajib diisi dan format tahun ajaran benar.");
            return;
        }

        if (selectedTahunAjaran == null) {
            insertTahunAjaran();
        } else {
            updateTahunAjaran();
        }
    }

    private void insertTahunAjaran() {
        String sql = "INSERT INTO tahun_ajaran (tahun_ajaran) VALUES (?)";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tahunAjaranField.getText());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tahun Ajaran berhasil ditambahkan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan Tahun Ajaran. Mungkin Tahun Ajaran sudah ada.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadTahunAjaranData();
        clearForm();
    }

    private void updateTahunAjaran() {
        String sql = "UPDATE tahun_ajaran SET tahun_ajaran = ? WHERE tahun_ajaran_id = ?";
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tahunAjaranField.getText());
                pstmt.setInt(2, selectedTahunAjaran.getTahunAjaranId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tahun Ajaran berhasil diperbarui.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui Tahun Ajaran.");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        loadTahunAjaranData();
        clearForm();
    }

    @FXML
    private void handleDelete() {
        TahunAjaran taToDelete = tahunAjaranTableView.getSelectionModel().getSelectedItem();
        if (taToDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih Tahun Ajaran yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Hapus");
        confirmAlert.setHeaderText("Hapus Tahun Ajaran: " + taToDelete.getTahunAjaran());
        confirmAlert.setContentText("Menghapus Tahun Ajaran ini juga akan menghapus semua Kelas yang terhubung dengannya. Apakah Anda yakin?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM tahun_ajaran WHERE tahun_ajaran_id = ?";
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, taToDelete.getTahunAjaranId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Tahun Ajaran berhasil dihapus.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus Tahun Ajaran. Pastikan tidak ada kelas atau jadwal yang masih terhubung.");
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            loadTahunAjaranData();
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
        tahunAjaranTableView.getSelectionModel().clearSelection();
        selectedTahunAjaran = null;
        tahunAjaranField.clear();
        saveButton.setText("Simpan");
    }

    private boolean isFormValid() {
        String taText = tahunAjaranField.getText();

        // Validasi format tahun ajaran: YYYY/YYYY (misal 2024/2025)
        if (!taText.matches("\\d{4}/\\d{4}")) {
            showAlert(Alert.AlertType.WARNING, "Format Salah", "Format Tahun Ajaran harus YYYY/YYYY (contoh: 2024/2025).");
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