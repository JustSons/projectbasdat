package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.MasterNamaKelas;
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

    @FXML private TableView<MasterNamaKelas> masterKelasTableView;
    @FXML private TableColumn<MasterNamaKelas, Integer> idColumn;
    @FXML private TableColumn<MasterNamaKelas, String> namaKelasColumn;
    @FXML private TextField searchField;
    @FXML private TextField namaKelasField;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<MasterNamaKelas> masterKelasList = FXCollections.observableArrayList();
    private MasterNamaKelas selectedMasterKelas = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("masterId"));
        namaKelasColumn.setCellValueFactory(new PropertyValueFactory<>("namaKelasTemplate"));

        loadMasterKelasData();
        setupSearchFilter();

        masterKelasTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadMasterKelasData() {
        masterKelasList.clear();
        String sql = "SELECT master_id, nama_kelas_template FROM master_nama_kelas ORDER BY nama_kelas_template ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                masterKelasList.add(new MasterNamaKelas(rs.getInt("master_id"), rs.getString("nama_kelas_template")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data master nama kelas.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<MasterNamaKelas> filteredData = new FilteredList<>(masterKelasList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(masterKelas -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return masterKelas.getNamaKelasTemplate().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        SortedList<MasterNamaKelas> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(masterKelasTableView.comparatorProperty());
        masterKelasTableView.setItems(sortedData);
    }

    private void populateForm(MasterNamaKelas masterKelas) {
        selectedMasterKelas = masterKelas;
        if (masterKelas != null) {
            namaKelasField.setText(masterKelas.getNamaKelasTemplate());
            saveButton.setText("Update");
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        if (namaKelasField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Nama kelas tidak boleh kosong.");
            return;
        }
        if (selectedMasterKelas == null) {
            insertMasterKelas();
        } else {
            updateMasterKelas();
        }
    }

    private void insertMasterKelas() {
        String sql = "INSERT INTO master_nama_kelas (nama_kelas_template) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, namaKelasField.getText().trim());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nama kelas template berhasil ditambahkan.");
            loadMasterKelasData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan. Nama kelas mungkin sudah ada.");
        }
    }

    private void updateMasterKelas() {
        String sql = "UPDATE master_nama_kelas SET nama_kelas_template = ? WHERE master_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, namaKelasField.getText().trim());
            pstmt.setInt(2, selectedMasterKelas.getMasterId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nama kelas template berhasil diperbarui.");
            loadMasterKelasData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data.");
        }
    }

    @FXML
    private void handleDelete() {
        MasterNamaKelas toDelete = masterKelasTableView.getSelectionModel().getSelectedItem();
        if (toDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih nama kelas yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Menghapus template ini tidak akan menghapus kelas yang sudah dibuat di menu Pembagian Kelas. Lanjutkan?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM master_nama_kelas WHERE master_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, toDelete.getMasterId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nama kelas template berhasil dihapus.");
                loadMasterKelasData();
                clearForm();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data.");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            // Pastikan path ke admin_dashboard.fxml sudah benar
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
        masterKelasTableView.getSelectionModel().clearSelection();
        selectedMasterKelas = null;
        namaKelasField.clear();
        saveButton.setText("Simpan");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}