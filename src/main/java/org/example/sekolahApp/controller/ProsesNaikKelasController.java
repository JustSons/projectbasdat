package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProsesNaikKelasController implements Initializable {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private Label tahunAjaranTujuanLabel;
    @FXML private Label statusMessageLabel; // Label baru untuk pesan status
    @FXML private Button prosesButton;
    @FXML private TextArea logTextArea;

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private TahunAjaran tahunAjaranTujuan = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadTahunAjaranData();
        tahunAjaranComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTahunAjaranTujuan(newVal);
            }
        });
        // Nonaktifkan tombol proses secara default
        prosesButton.setDisable(true);
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran FROM tahun_ajaran ORDER BY tahun_ajaran DESC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran")));
            }
            tahunAjaranComboBox.setItems(tahunAjaranList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void updateTahunAjaranTujuan(TahunAjaran selectedTa) {
        String[] parts = selectedTa.getTahunAjaran().split("/");
        if (parts.length != 2) {
            tahunAjaranTujuanLabel.setText("- Format Salah -");
            statusMessageLabel.setText("Format tahun ajaran sumber tidak valid.");
            prosesButton.setDisable(true);
            tahunAjaranTujuan = null;
            return;
        }
        String nextTaStr = (Integer.parseInt(parts[0]) + 1) + "/" + (Integer.parseInt(parts[1]) + 1);

        Optional<TahunAjaran> nextTaOpt = tahunAjaranList.stream()
                .filter(ta -> ta.getTahunAjaran().equals(nextTaStr))
                .findFirst();

        if (nextTaOpt.isPresent()) {
            tahunAjaranTujuan = nextTaOpt.get();
            tahunAjaranTujuanLabel.setText(tahunAjaranTujuan.getTahunAjaran());
            statusMessageLabel.setText("Tahun ajaran tujuan ditemukan. Siap untuk proses.");
            statusMessageLabel.setTextFill(Color.GREEN);
            prosesButton.setDisable(false); // Aktifkan tombol
        } else {
            tahunAjaranTujuanLabel.setText("- Tidak Ditemukan -");
            statusMessageLabel.setText("Error: Tahun ajaran " + nextTaStr + " belum ada. Buat di menu 'Kelola Tahun Ajaran'.");
            statusMessageLabel.setTextFill(Color.RED);
            prosesButton.setDisable(true); // Nonaktifkan tombol
            tahunAjaranTujuan = null;
        }
    }

    @FXML
    private void handleNaikKelas() {
        TahunAjaran currentTa = tahunAjaranComboBox.getValue();
        // Validasi ulang, meskipun tombol sudah dinonaktifkan
        if (currentTa == null || tahunAjaranTujuan == null) {
            showAlert(Alert.AlertType.ERROR, "Proses Diblokir", "Tahun ajaran sumber atau tujuan tidak valid. Tidak bisa melanjutkan.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Konfirmasi Kenaikan Kelas");
        confirmAlert.setHeaderText("Anda akan memproses kenaikan kelas dari " + currentTa.getTahunAjaran() + " ke " + tahunAjaranTujuan.getTahunAjaran() + ".");
        confirmAlert.setContentText("Aksi ini tidak dapat dibatalkan. Lanjutkan?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            prosesKenaikanKelasDiDatabase(currentTa.getTahunAjaranId(), tahunAjaranTujuan.getTahunAjaranId());
        }
    }

    private void prosesKenaikanKelasDiDatabase(int currentTaId, int nextTaId) {
        // (Sisa kode untuk proses ini tetap sama seperti sebelumnya)
        // ...
    }

    private int getNextKelasId(Connection conn, String currentClassName, int nextTaId) throws SQLException {
        // (Sisa kode untuk proses ini tetap sama seperti sebelumnya)
        // ...
        return -1;
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml");
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