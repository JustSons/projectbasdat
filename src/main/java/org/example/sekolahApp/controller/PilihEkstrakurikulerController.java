package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Extracurricular;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class PilihEkstrakurikulerController {

    @FXML private TableView<Extracurricular> ekskulTersediaTableView;
    @FXML private TableColumn<Extracurricular, String> namaTersediaColumn;
    @FXML private TableColumn<Extracurricular, String> hariTersediaColumn;
    @FXML private TableColumn<Extracurricular, String> jamTersediaColumn;
    @FXML private TableColumn<Extracurricular, String> tempatTersediaColumn;

    private final ObservableList<Extracurricular> ekskulTersediaList = FXCollections.observableArrayList();
    private int siswaId;

    @FXML
    public void initialize() {
        this.siswaId = UserSession.getInstance().getReferenceId();
        setupTable();
        loadEkskulTersedia();
    }

    private void setupTable() {
        namaTersediaColumn.setCellValueFactory(new PropertyValueFactory<>("extracurricularName"));
        hariTersediaColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        jamTersediaColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimeFormatted()));
        tempatTersediaColumn.setCellValueFactory(new PropertyValueFactory<>("extracurricularPlace"));
        ekskulTersediaTableView.setItems(ekskulTersediaList);
    }

    private void loadEkskulTersedia() {
        ekskulTersediaList.clear();
        String sql = "SELECT * FROM extracurricular ORDER BY extracurricular_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ekskulTersediaList.add(new Extracurricular(
                        rs.getInt("extracurricular_id"),
                        rs.getString("extracurricular_name"),
                        rs.getString("day"),
                        rs.getTime("time") != null ? rs.getTime("time").toLocalTime() : null,
                        rs.getString("extracurricular_place")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar ekstrakurikuler.");
        }
    }

    @FXML
    private void handleDaftar() {
        Extracurricular selectedEkskul = ekskulTersediaTableView.getSelectionModel().getSelectedItem();

        if (selectedEkskul == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Silakan pilih ekstrakurikuler yang ingin diikuti.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Anda yakin ingin mendaftar untuk " + selectedEkskul.getExtracurricularName() + "?");
        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // 1. Cek apakah sudah terdaftar
                String checkSql = "SELECT COUNT(*) FROM extracurricular_student WHERE student_id = ? AND extracurricular_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, this.siswaId);
                    checkStmt.setInt(2, selectedEkskul.getExtracurricularId());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Sudah Terdaftar", "Anda sudah terdaftar di ekstrakurikuler ini.");
                        return;
                    }
                }

                // 2. Jika belum, daftarkan
                String insertSql = "INSERT INTO extracurricular_student (student_id, extracurricular_id) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, this.siswaId);
                    insertStmt.setInt(2, selectedEkskul.getExtracurricularId());
                    int affectedRows = insertStmt.executeUpdate();
                    if (affectedRows > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Pendaftaran Berhasil", "Anda berhasil terdaftar di " + selectedEkskul.getExtracurricularName() + ".");
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Terjadi kesalahan saat proses pendaftaran.");
            }
        }
    }

    @FXML
    private void handleKembali() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/SiswaDashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}