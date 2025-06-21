package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.Siswa;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class KelolaKelulusanController implements Initializable {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private TableView<Siswa> siswaTableView;
    @FXML private TableColumn<Siswa, String> nisColumn;
    @FXML private TableColumn<Siswa, String> namaColumn;
    @FXML private TableColumn<Siswa, String> statusColumn;
    @FXML private Button luluskanButton;
    @FXML private Button aktifkanButton;
    @FXML private Button pindahButton;

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("DEBUG: Halaman Kelola Kelulusan sedang diinisialisasi...");
        setupTableColumns();
        loadTahunAjaranData();

        tahunAjaranComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("DEBUG: Tahun Ajaran dipilih: " + newVal.getTahunAjaran() + " (ID: " + newVal.getTahunAjaranId() + ")");
                loadKelasByTahunAjaran(newVal.getTahunAjaranId());
            }
        });

        kelasComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("DEBUG: Kelas dipilih: " + newVal.getNamaKelas() + " (ID: " + newVal.getKelasId() + ")");
                loadSiswaData(newVal.getKelasId());
            } else {
                siswaList.clear();
            }
        });

        if (!tahunAjaranList.isEmpty()) {
            Optional<TahunAjaran> activeYear = tahunAjaranList.stream()
                    .filter(ta -> "aktif".equalsIgnoreCase(ta.getStatus()))
                    .findFirst();

            if (activeYear.isPresent()) {
                System.out.println("DEBUG: Ditemukan tahun ajaran aktif, memilih: " + activeYear.get().getTahunAjaran());
                tahunAjaranComboBox.setValue(activeYear.get());
            } else {
                System.out.println("DEBUG: Tidak ada tahun ajaran aktif, memilih item pertama.");
                tahunAjaranComboBox.getSelectionModel().selectFirst();
            }
        } else {
            System.out.println("DEBUG: Daftar tahun ajaran kosong.");
        }
    }

    private void setupTableColumns() {
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        siswaTableView.setItems(siswaList);
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran DESC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("status")));
            }
            tahunAjaranComboBox.setItems(tahunAjaranList);
            System.out.println("DEBUG: Berhasil memuat " + tahunAjaranList.size() + " tahun ajaran.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void loadKelasByTahunAjaran(int tahunAjaranId) {
        System.out.println("DEBUG: Memuat kelas untuk tahun_ajaran_id: " + tahunAjaranId);
        kelasList.clear();
        kelasComboBox.getSelectionModel().clearSelection();
        String sql = "SELECT kelas_id, nama_kelas FROM kelas WHERE tahun_ajaran_id = ? ORDER BY nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tahunAjaranId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                kelasList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas")));
            }
            kelasComboBox.setItems(kelasList);

            System.out.println("DEBUG: Ditemukan " + kelasList.size() + " kelas.");
            if (kelasList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Informasi", "Tidak ada kelas yang ditemukan untuk tahun ajaran yang dipilih.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void loadSiswaData(int kelasId) {
        System.out.println("DEBUG: Memuat siswa untuk kelas_id: " + kelasId);
        siswaList.clear();
        String sql = "SELECT s.siswa_id, s.nis, s.nama_siswa, s.status FROM siswa s JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id WHERE sk.kelas_id = ? ORDER BY s.nama_siswa ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Siswa siswa = new Siswa(rs.getInt("siswa_id"), rs.getString("nis"), rs.getString("nama_siswa"));
                siswa.setStatus(rs.getString("status"));
                siswaList.add(siswa);
            }
            System.out.println("DEBUG: Ditemukan " + siswaList.size() + " siswa.");

            // PERBAIKAN: Menambahkan notifikasi jika daftar siswa kosong
            if (siswaList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Data Kosong", "Tidak ada siswa yang terdaftar di kelas '" + kelasComboBox.getValue().getNamaKelas() + "'.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa.");
        }
    }

    private void updateStatusSiswa(int siswaId, String newStatus, String namaSiswa) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Apakah Anda yakin ingin mengubah status " + namaSiswa + " menjadi '" + newStatus + "'?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Konfirmasi Perubahan Status");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "UPDATE siswa SET status = ? WHERE siswa_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setInt(2, siswaId);
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status siswa berhasil diubah.");
                if (kelasComboBox.getValue() != null) {
                    loadSiswaData(kelasComboBox.getValue().getKelasId());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengupdate status siswa.");
            }
        }
    }

    private Siswa getSelectedSiswa() {
        Siswa selectedSiswa = siswaTableView.getSelectionModel().getSelectedItem();
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih siswa dari tabel terlebih dahulu.");
        }
        return selectedSiswa;
    }

    @FXML private void handleLuluskanSiswa() {
        Siswa siswa = getSelectedSiswa();
        if (siswa != null) {
            updateStatusSiswa(siswa.getId(), "Lulus", siswa.getNama());
        }
    }

    @FXML private void handleAktifkanSiswa() {
        Siswa siswa = getSelectedSiswa();
        if (siswa != null) {
            updateStatusSiswa(siswa.getId(), "Aktif", siswa.getNama());
        }
    }

    @FXML private void handlePindahSiswa() {
        Siswa siswa = getSelectedSiswa();
        if (siswa != null) {
            updateStatusSiswa(siswa.getId(), "Pindah", siswa.getNama());
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}