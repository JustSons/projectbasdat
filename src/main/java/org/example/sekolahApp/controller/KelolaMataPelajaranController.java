package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.MataPelajaran;
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

public class KelolaMataPelajaranController implements Initializable {

    @FXML private TableView<MataPelajaran> mapelTableView;
    @FXML private TableColumn<MataPelajaran, String> kodeMapelColumn;
    @FXML private TableColumn<MataPelajaran, String> namaMapelColumn;
    @FXML private TextField searchField;
    @FXML private TextField kodeMapelField;
    @FXML private TextField namaMapelField;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    private final ObservableList<MataPelajaran> mapelList = FXCollections.observableArrayList();
    private MataPelajaran selectedMapel = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Pastikan nama properti di model MataPelajaran Anda adalah "kodeMapel" dan "namaMapel"
        kodeMapelColumn.setCellValueFactory(new PropertyValueFactory<>("kodeMapel"));
        namaMapelColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));

        loadMataPelajaranData();
        setupSearchFilter();

        mapelTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateForm(newValue));
    }

    private void loadMataPelajaranData() {
        mapelList.clear();
        String sql = "SELECT * FROM mata_pelajaran ORDER BY nama_mapel ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mapelList.add(new MataPelajaran(
                        rs.getInt("mapel_id"),
                        rs.getString("kode_mapel"),
                        rs.getString("nama_mapel")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data mata pelajaran.");
        }
    }

    private void setupSearchFilter() {
        FilteredList<MataPelajaran> filteredData = new FilteredList<>(mapelList, b -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(mapel -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (mapel.getKodeMapel().toLowerCase().contains(lowerCaseFilter)) return true;
                if (mapel.getNamaMapel().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });

        SortedList<MataPelajaran> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(mapelTableView.comparatorProperty());
        mapelTableView.setItems(sortedData);
    }

    private void populateForm(MataPelajaran mapel) {
        selectedMapel = mapel;
        if (mapel != null) {
            kodeMapelField.setText(mapel.getKodeMapel());
            namaMapelField.setText(mapel.getNamaMapel());
            saveButton.setText("Update");
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        if (kodeMapelField.getText().trim().isEmpty() || namaMapelField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Kode dan Nama Mata Pelajaran tidak boleh kosong.");
            return;
        }
        if (selectedMapel == null) {
            insertMataPelajaran();
        } else {
            updateMataPelajaran();
        }
    }

    private void insertMataPelajaran() {
        String sql = "INSERT INTO mata_pelajaran (kode_mapel, nama_mapel) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeMapelField.getText().trim());
            pstmt.setString(2, namaMapelField.getText().trim());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Mata pelajaran berhasil ditambahkan.");
            loadMataPelajaranData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan. Kode mata pelajaran mungkin sudah ada.");
        }
    }

    private void updateMataPelajaran() {
        String sql = "UPDATE mata_pelajaran SET kode_mapel = ?, nama_mapel = ? WHERE mapel_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kodeMapelField.getText().trim());
            pstmt.setString(2, namaMapelField.getText().trim());
            pstmt.setInt(3, selectedMapel.getMapelId());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Mata pelajaran berhasil diperbarui.");
            loadMataPelajaranData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data.");
        }
    }

    @FXML
    private void handleDelete() {
        MataPelajaran toDelete = mapelTableView.getSelectionModel().getSelectedItem();
        if (toDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Pilihan", "Pilih mata pelajaran yang akan dihapus.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Apakah Anda yakin ingin menghapus " + toDelete.getNamaMapel() + "?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM mata_pelajaran WHERE mapel_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, toDelete.getMapelId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Mata pelajaran berhasil dihapus.");
                loadMataPelajaranData();
                clearForm();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data. Mungkin mapel ini masih digunakan di jadwal.");
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
        mapelTableView.getSelectionModel().clearSelection();
        selectedMapel = null;
        kodeMapelField.clear();
        namaMapelField.clear();
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
