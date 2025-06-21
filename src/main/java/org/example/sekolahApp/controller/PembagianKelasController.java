package org.example.sekolahApp.controller;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.model.*; // Import semua model
import org.example.sekolahApp.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class PembagianKelasController implements Initializable {

    // FXML Declarations
    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<MasterNamaKelas> masterNamaKelasComboBox;
    @FXML private ComboBox<Staff> waliKelasComboBox;
    @FXML private TableView<Siswa> siswaTersediaTableView;
    @FXML private TableColumn<Siswa, String> nisTersediaColumn;
    @FXML private TableColumn<Siswa, String> namaTersediaColumn;
    @FXML private Label labelDaftarKelas;
    @FXML private TableView<Kelas> kelasTableView;
    @FXML private TableColumn<Kelas, String> namaKelasAssignedColumn;
    @FXML private TableColumn<Kelas, String> waliKelasAssignedColumn;
    @FXML private Label labelSiswaDiKelas;
    @FXML private TableView<Siswa> siswaDiKelasTableView;
    @FXML private TableColumn<Siswa, String> nisDiKelasColumn;
    @FXML private TableColumn<Siswa, String> namaDiKelasColumn;

    // ObservableList Declarations
    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<MasterNamaKelas> masterNamaKelasList = FXCollections.observableArrayList();
    private final ObservableList<Staff> staffList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaTersediaList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaDiKelasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadInitialDataForComboBoxes();
        setupListeners();

        if (!tahunAjaranList.isEmpty()) {
            tahunAjaranComboBox.getSelectionModel().selectFirst();
        }
    }

    private void setupTableColumns() {
        // Tabel Kelas (yang sudah jadi)
        namaKelasAssignedColumn.setCellValueFactory(new PropertyValueFactory<>("namaKelas"));
        waliKelasAssignedColumn.setCellValueFactory(cell -> {
            Staff wali = cell.getValue().getWaliKelas();
            return wali != null ? wali.namaStaffProperty() : new javafx.beans.property.SimpleStringProperty("Belum Diatur");
        });

        // Tabel Siswa
        nisTersediaColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaTersediaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        nisDiKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaDiKelasColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));

        // Set items to tables
        kelasTableView.setItems(kelasList);
        siswaTersediaTableView.setItems(siswaTersediaList);
        siswaDiKelasTableView.setItems(siswaDiKelasList);
    }

    private void setupListeners() {
        tahunAjaranComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                labelDaftarKelas.setText("Daftar Kelas pada Tahun Ajaran: " + newVal.getTahunAjaran());
                loadKelasData(); // Memuat kelas yang ada untuk tahun ajaran ini
                loadSiswaTersedia(); // Memuat siswa yang tersedia untuk tahun ajaran ini
                siswaDiKelasList.clear(); // Kosongkan tabel siswa di kelas
                labelSiswaDiKelas.setText("Siswa di Kelas: (Pilih kelas di atas)");
            }
        });

        kelasTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                labelSiswaDiKelas.setText("Siswa di Kelas: " + newVal.getNamaKelas());
                loadSiswaDiKelas(); // Memuat siswa yang ada di kelas yang dipilih
            } else {
                labelSiswaDiKelas.setText("Siswa di Kelas: (Pilih kelas di atas)");
                siswaDiKelasList.clear();
            }
        });
    }

    // --- DATA LOADING METHODS ---

    private void loadInitialDataForComboBoxes() {
        loadTahunAjaranData();
        loadMasterNamaKelasData();
        loadWaliKelasData();
    }

    private void loadTahunAjaranData() {
        tahunAjaranList.clear();
        String sql = "SELECT tahun_ajaran_id, tahun_ajaran, status FROM tahun_ajaran ORDER BY tahun_ajaran DESC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tahunAjaranList.add(new TahunAjaran(rs.getInt("tahun_ajaran_id"), rs.getString("tahun_ajaran"), rs.getString("status")));
            }
            tahunAjaranComboBox.setItems(tahunAjaranList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data tahun ajaran.");
        }
    }

    private void loadMasterNamaKelasData() {
        masterNamaKelasList.clear();
        String sql = "SELECT master_id, nama_kelas_template FROM master_nama_kelas ORDER BY nama_kelas_template ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                masterNamaKelasList.add(new MasterNamaKelas(rs.getInt("master_id"), rs.getString("nama_kelas_template")));
            }
            masterNamaKelasComboBox.setItems(masterNamaKelasList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data master nama kelas.");
        }
    }

    private void loadWaliKelasData() {
        staffList.clear();
        String sql = "SELECT staff_id, nama_staff FROM staff WHERE jabatan IN ('Guru', 'Wali Kelas') ORDER BY nama_staff ASC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                staffList.add(new Staff(rs.getInt("staff_id"), rs.getString("nama_staff")));
            }
            waliKelasComboBox.setItems(staffList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data guru/wali kelas.");
        }
    }

    private void loadKelasData() {
        kelasList.clear();
        TahunAjaran selectedTA = tahunAjaranComboBox.getValue();
        if (selectedTA == null) return;

        String sql = "SELECT k.kelas_id, k.nama_kelas, k.tahun_ajaran_id, k.wali_kelas_id, s.nama_staff " +
                "FROM kelas k " +
                "LEFT JOIN staff s ON k.wali_kelas_id = s.staff_id " +
                "WHERE k.tahun_ajaran_id = ? ORDER BY k.nama_kelas ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedTA.getTahunAjaranId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Staff wali = rs.getString("nama_staff") != null ? new Staff(rs.getInt("wali_kelas_id"), rs.getString("nama_staff")) : null;
                Kelas k = new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), selectedTA, wali);
                kelasList.add(k);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data kelas.");
        }
    }

    private void loadSiswaTersedia() {
        siswaTersediaList.clear();
        TahunAjaran selectedTA = tahunAjaranComboBox.getValue();
        if (selectedTA == null) return;

        String sql = "SELECT s.siswa_id, s.nis, s.nama_siswa FROM siswa s " +
                "WHERE s.siswa_id NOT IN (" +
                "  SELECT sk.siswa_id FROM siswa_kelas sk " +
                "  JOIN kelas k ON sk.kelas_id = k.kelas_id " +
                "  WHERE k.tahun_ajaran_id = ?" +
                ") ORDER BY s.nama_siswa ASC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, selectedTA.getTahunAjaranId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                siswaTersediaList.add(new Siswa(rs.getInt("siswa_id"), rs.getString("nis"), rs.getString("nama_siswa")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa tersedia.");
        }
    }

    private void loadSiswaDiKelas(){
        siswaDiKelasList.clear();
        Kelas selectedKelas = kelasTableView.getSelectionModel().getSelectedItem();
        if(selectedKelas == null) return;

        String sql = "SELECT s.siswa_id, s.nis, s.nama_siswa FROM siswa s " +
                "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                "WHERE sk.kelas_id = ? ORDER BY s.nama_siswa ASC";
        try(Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, selectedKelas.getKelasId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()){
                siswaDiKelasList.add(new Siswa(rs.getInt("siswa_id"), rs.getString("nis"), rs.getString("nama_siswa")));
            }
        } catch(SQLException e){
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat siswa di kelas.");
        }
    }

    // --- ACTIONS ---

    @FXML
    private void handleSaveKelas() {
        TahunAjaran ta = tahunAjaranComboBox.getValue();
        MasterNamaKelas master = masterNamaKelasComboBox.getValue();

        if (ta == null || master == null) {
            showAlert(Alert.AlertType.WARNING, "Validasi Gagal", "Tahun Ajaran dan Nama Kelas wajib dipilih.");
            return;
        }

        Staff wali = waliKelasComboBox.getValue();
        String sql = "INSERT INTO kelas (nama_kelas, tahun_ajaran_id, wali_kelas_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, master.getNamaKelasTemplate());
            pstmt.setInt(2, ta.getTahunAjaranId());
            if (wali != null) {
                pstmt.setInt(3, wali.getStaffId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas berhasil dibuat.");
            loadKelasData(); // Refresh tabel kelas
            masterNamaKelasComboBox.setValue(null);
            waliKelasComboBox.setValue(null);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan. Kelas dengan nama tersebut mungkin sudah ada di tahun ajaran ini.");
        }
    }

    @FXML
    private void handleDeleteKelas() {
        Kelas toDelete = kelasTableView.getSelectionModel().getSelectedItem();
        if (toDelete == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih kelas dari tabel yang ingin dihapus.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Menghapus kelas ini juga akan menghapus data siswa di dalamnya (dari tabel siswa_kelas). Lanjutkan?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Konfirmasi Hapus");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM kelas WHERE kelas_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, toDelete.getKelasId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas berhasil dihapus.");
                loadKelasData();
                loadSiswaTersedia();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data.");
            }
        }
    }

    @FXML
    private void handleAddSiswa() {
        Siswa siswa = siswaTersediaTableView.getSelectionModel().getSelectedItem();
        Kelas kelas = kelasTableView.getSelectionModel().getSelectedItem();
        if (siswa == null || kelas == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Tidak Lengkap", "Pilih siswa dari tabel kiri dan kelas tujuan dari tabel kanan.");
            return;
        }

        String sql = "INSERT INTO siswa_kelas (siswa_id, kelas_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siswa.getId());
            pstmt.setInt(2, kelas.getKelasId());
            pstmt.executeUpdate();
            loadSiswaTersedia();
            loadSiswaDiKelas();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan siswa ke kelas.");
        }
    }

    @FXML
    private void handleRemoveSiswa() {
        Siswa siswa = siswaDiKelasTableView.getSelectionModel().getSelectedItem();
        Kelas kelas = kelasTableView.getSelectionModel().getSelectedItem();
        if (siswa == null || kelas == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih siswa dari tabel kelas untuk dikeluarkan.");
            return;
        }

        String sql = "DELETE FROM siswa_kelas WHERE siswa_id = ? AND kelas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siswa.getId());
            pstmt.setInt(2, kelas.getKelasId());
            pstmt.executeUpdate();
            loadSiswaTersedia();
            loadSiswaDiKelas();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengeluarkan siswa.");
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
