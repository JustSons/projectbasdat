package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CetakRaporController implements Initializable {

    @FXML private ComboBox<KelasView> kelasComboBox;
    @FXML private ComboBox<SiswaView> siswaComboBox;
    @FXML private TableView<NilaiRaporView> nilaiTableView;
    @FXML private TableColumn<NilaiRaporView, String> mapelColumn;
    @FXML private TableColumn<NilaiRaporView, String> uts1Column;
    @FXML private TableColumn<NilaiRaporView, String> uas1Column;
    @FXML private TableColumn<NilaiRaporView, String> uts2Column;
    @FXML private TableColumn<NilaiRaporView, String> uas2Column;
    @FXML private Label siswaInfoLabel;
    @FXML private Button cetakButton;

    private final ObservableList<KelasView> kelasList = FXCollections.observableArrayList();
    private final ObservableList<SiswaView> siswaList = FXCollections.observableArrayList();
    private final ObservableList<NilaiRaporView> nilaiList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cek apakah user adalah wali kelas
        if (!UserSession.getInstance().isLoggedIn() || !UserSession.getInstance().isWaliKelas()) {
            showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Halaman ini hanya untuk wali kelas.");
            return;
        }

        setupTableColumns();
        loadKelasList();
        setupEventHandlers();
    }

    private void setupTableColumns() {
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("namaMapel"));
        uts1Column.setCellValueFactory(new PropertyValueFactory<>("uts1"));
        uas1Column.setCellValueFactory(new PropertyValueFactory<>("uas1"));
        uts2Column.setCellValueFactory(new PropertyValueFactory<>("uts2"));
        uas2Column.setCellValueFactory(new PropertyValueFactory<>("uas2"));

        nilaiTableView.setItems(nilaiList);
    }

    private void setupEventHandlers() {
        kelasComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadSiswaByKelas(newVal.getKelasId());
            }
        });

        siswaComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNilaiSiswa(newVal.getSiswaId());
                updateSiswaInfo(newVal);
            }
        });
    }

    private void loadKelasList() {
        kelasList.clear();
        
        // Hanya load kelas yang diwali oleh user yang login
        int waliKelasId = UserSession.getInstance().getReferenceId();
        
        String sql = "SELECT k.kelas_id, k.nama_kelas, ta.tahun_ajaran " +
                     "FROM kelas k " +
                     "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                     "WHERE k.wali_kelas_id = ? " +
                     "ORDER BY ta.tahun_ajaran DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, waliKelasId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                kelasList.add(new KelasView(
                    rs.getInt("kelas_id"),
                    rs.getString("nama_kelas") + " (" + rs.getString("tahun_ajaran") + ")"
                ));
            }
            
            kelasComboBox.setItems(kelasList);
            
            if (kelasList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Info", "Anda belum menjadi wali kelas dari kelas manapun.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar kelas.");
        }
    }

    private void loadSiswaByKelas(int kelasId) {
        siswaList.clear();
        siswaComboBox.setValue(null);
        nilaiList.clear();
        siswaInfoLabel.setText("");

        String sql = "SELECT s.siswa_id, s.nama_siswa, s.nis " +
                     "FROM siswa s " +
                     "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                     "WHERE sk.kelas_id = ? " +
                     "ORDER BY s.nama_siswa";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                siswaList.add(new SiswaView(
                    rs.getInt("siswa_id"),
                    rs.getString("nama_siswa") + " - " + rs.getString("nis"),
                    rs.getString("nama_siswa"),
                    rs.getString("nis")
                ));
            }
            
            siswaComboBox.setItems(siswaList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa.");
        }
    }

    private void loadNilaiSiswa(int siswaId) {
        nilaiList.clear();

        String sql = "SELECT mp.nama_mapel, n.jenis_ujian, n.nilai " +
                     "FROM nilai n " +
                     "JOIN mata_pelajaran mp ON n.mapel_id = mp.mapel_id " +
                     "JOIN siswa_kelas sk ON n.siswa_kelas_id = sk.siswa_kelas_id " +
                     "WHERE sk.siswa_id = ? " +
                     "ORDER BY mp.nama_mapel";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, siswaId);
            ResultSet rs = pstmt.executeQuery();

            // Group nilai by mata pelajaran
            List<String> mapelList = new ArrayList<>();
            List<NilaiRaporView> tempNilai = new ArrayList<>();

            while (rs.next()) {
                String namaMapel = rs.getString("nama_mapel");
                String jenisUjian = rs.getString("jenis_ujian");
                int nilai = rs.getInt("nilai");

                // Cari atau buat entry untuk mata pelajaran ini
                NilaiRaporView nilaiRapor = tempNilai.stream()
                    .filter(n -> n.getNamaMapel().equals(namaMapel))
                    .findFirst()
                    .orElse(null);

                if (nilaiRapor == null) {
                    nilaiRapor = new NilaiRaporView(namaMapel);
                    tempNilai.add(nilaiRapor);
                }

                // Set nilai berdasarkan jenis ujian
                switch (jenisUjian.toUpperCase()) {
                    case "UTS1":
                        nilaiRapor.setUts1(String.valueOf(nilai));
                        break;
                    case "UAS1":
                        nilaiRapor.setUas1(String.valueOf(nilai));
                        break;
                    case "UTS2":
                        nilaiRapor.setUts2(String.valueOf(nilai));
                        break;
                    case "UAS2":
                        nilaiRapor.setUas2(String.valueOf(nilai));
                        break;
                }
            }

            nilaiList.addAll(tempNilai);
            cetakButton.setDisable(nilaiList.isEmpty());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat nilai siswa.");
        }
    }

    private void updateSiswaInfo(SiswaView siswa) {
        siswaInfoLabel.setText("Rapor untuk: " + siswa.getNamaSiswa() + " (NIS: " + siswa.getNis() + ")");
    }

    @FXML
    private void handleCetakRapor() {
        SiswaView selectedSiswa = siswaComboBox.getValue();
        KelasView selectedKelas = kelasComboBox.getValue();

        if (selectedSiswa == null || selectedKelas == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Tidak Lengkap", "Pilih kelas dan siswa terlebih dahulu.");
            return;
        }

        if (nilaiList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Data", "Tidak ada nilai untuk dicetak.");
            return;
        }

        // File chooser untuk save rapor
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rapor");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("Rapor_" + selectedSiswa.getNis() + ".txt");

        Stage stage = (Stage) cetakButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            generateRapor(file, selectedSiswa, selectedKelas);
        }
    }

    private void generateRapor(File file, SiswaView siswa, KelasView kelas) {
        try (FileWriter writer = new FileWriter(file)) {
            // Header rapor
            writer.write("==========================================\n");
            writer.write("           RAPOR SISWA\n");
            writer.write("==========================================\n\n");
            
            writer.write("Nama Siswa  : " + siswa.getNamaSiswa() + "\n");
            writer.write("NIS         : " + siswa.getNis() + "\n");
            writer.write("Kelas       : " + kelas.getNamaKelas() + "\n");
            writer.write("Tanggal     : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n\n");
            
            writer.write("==========================================\n");
            writer.write("              NILAI\n");
            writer.write("==========================================\n\n");
            
            // Table header
            writer.write(String.format("%-25s %-8s %-8s %-8s %-8s\n", 
                "MATA PELAJARAN", "UTS1", "UAS1", "UTS2", "UAS2"));
            writer.write("----------------------------------------------------------\n");
            
            // Table content
            for (NilaiRaporView nilai : nilaiList) {
                writer.write(String.format("%-25s %-8s %-8s %-8s %-8s\n",
                    nilai.getNamaMapel(),
                    nilai.getUts1(),
                    nilai.getUas1(),
                    nilai.getUts2(),
                    nilai.getUas2()
                ));
            }
            
            writer.write("\n\n");
            writer.write("Catatan: Rapor ini tidak menghitung nilai akhir,\n");
            writer.write("hanya menampilkan nilai per ujian.\n\n");
            
            writer.write("Wali Kelas,\n\n\n");
            writer.write("_____________________\n");
            
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Rapor berhasil disimpan ke: " + file.getAbsolutePath());
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan rapor: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/GuruDashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal kembali ke dashboard.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner classes untuk view
    public static class KelasView {
        private final int kelasId;
        private final String namaKelas;

        public KelasView(int kelasId, String namaKelas) {
            this.kelasId = kelasId;
            this.namaKelas = namaKelas;
        }

        public int getKelasId() { return kelasId; }
        public String getNamaKelas() { return namaKelas; }

        @Override
        public String toString() { return namaKelas; }
    }

    public static class SiswaView {
        private final int siswaId;
        private final String displayName;
        private final String namaSiswa;
        private final String nis;

        public SiswaView(int siswaId, String displayName, String namaSiswa, String nis) {
            this.siswaId = siswaId;
            this.displayName = displayName;
            this.namaSiswa = namaSiswa;
            this.nis = nis;
        }

        public int getSiswaId() { return siswaId; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getNis() { return nis; }

        @Override
        public String toString() { return displayName; }
    }

    public static class NilaiRaporView {
        private final String namaMapel;
        private String uts1 = "-";
        private String uas1 = "-";
        private String uts2 = "-";
        private String uas2 = "-";

        public NilaiRaporView(String namaMapel) {
            this.namaMapel = namaMapel;
        }

        public String getNamaMapel() { return namaMapel; }
        public String getUts1() { return uts1; }
        public String getUas1() { return uas1; }
        public String getUts2() { return uts2; }
        public String getUas2() { return uas2; }

        public void setUts1(String uts1) { this.uts1 = uts1; }
        public void setUas1(String uas1) { this.uas1 = uas1; }
        public void setUts2(String uts2) { this.uts2 = uts2; }
        public void setUas2(String uas2) { this.uas2 = uas2; }
    }
}