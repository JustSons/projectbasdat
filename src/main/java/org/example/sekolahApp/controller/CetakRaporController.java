package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.util.SceneManager;
import org.example.sekolahApp.util.UserSession;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CetakRaporController {

    @FXML private Label infoKelasLabel;
    @FXML private ComboBox<Siswa> siswaComboBox;
    @FXML private TextArea raporTextArea;
    @FXML private Button cetakButton;

    private int kelasId = -1;
    private String namaKelas;
    private String tahunAjaran;

    private final ObservableList<Siswa> siswaList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureSiswaComboBox();
        loadDataWaliKelas();
        if (kelasId != -1) {
            loadSiswaDiKelas();
        } else {
            raporTextArea.setText("Gagal memuat data. Anda mungkin tidak ditugaskan sebagai wali kelas.");
            siswaComboBox.setDisable(true);
            cetakButton.setDisable(true);
        }
    }

    private void loadDataWaliKelas() {
        int staffId = UserSession.getInstance().getReferenceId();
        String sql = "SELECT k.kelas_id, k.nama_kelas, ta.tahun_ajaran " +
                "FROM kelas k " +
                "JOIN tahun_ajaran ta ON k.tahun_ajaran_id = ta.tahun_ajaran_id " +
                "WHERE k.wali_kelas_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.kelasId = rs.getInt("kelas_id");
                this.namaKelas = rs.getString("nama_kelas");
                this.tahunAjaran = rs.getString("tahun_ajaran");
                infoKelasLabel.setText("Kelas: " + namaKelas + " (" + tahunAjaran + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat info kelas.");
        }
    }

    private void loadSiswaDiKelas() {
        siswaList.clear();
        String sql = "SELECT s.siswa_id, s.nis, s.nama_siswa FROM siswa s " +
                "JOIN siswa_kelas sk ON s.siswa_id = sk.siswa_id " +
                "WHERE sk.kelas_id = ? ORDER BY s.nama_siswa";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, kelasId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                siswaList.add(new Siswa(rs.getInt("siswa_id"), rs.getString("nis"), rs.getString("nama_siswa")));
            }
            siswaComboBox.setItems(siswaList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa.");
        }
    }

    private void configureSiswaComboBox() {
        siswaComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Siswa siswa) {
                return siswa == null ? null : siswa.getNama() + " (" + siswa.getNis() + ")";
            }

            @Override
            public Siswa fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    private void handlePilihSiswa() {
        Siswa selectedSiswa = siswaComboBox.getValue();
        if (selectedSiswa == null) {
            raporTextArea.clear();
            return;
        }
        // Tampilkan preview di TextArea
        raporTextArea.setText(generateRaporText(selectedSiswa));
    }

    private String generateRaporText(Siswa siswa) {
        StringBuilder sb = new StringBuilder();
        List<NilaiRapor> daftarNilai = getNilaiSiswa(siswa.getId());

        sb.append("===============================================================\n");
        sb.append("                         RAPOR SISWA\n");
        sb.append("===============================================================\n\n");
        sb.append(String.format("Nama Siswa    : %s\n", siswa.getNama()));
        sb.append(String.format("NIS           : %s\n", siswa.getNis()));
        sb.append(String.format("Kelas         : %s\n", this.namaKelas));
        sb.append(String.format("Tahun Ajaran  : %s\n", this.tahunAjaran));
        sb.append("\n---------------------------------------------------------------\n");
        sb.append(String.format("| %-30s | %-5s | %-5s | %-5s | %-5s |\n", "Mata Pelajaran", "UTS 1", "UAS 1", "UTS 2", "UAS 2"));
        sb.append("---------------------------------------------------------------\n");

        Map<String, List<NilaiRapor>> nilaiByMapel = daftarNilai.stream()
                .collect(Collectors.groupingBy(NilaiRapor::getNamaMapel));

        for (String namaMapel : nilaiByMapel.keySet()) {
            List<NilaiRapor> nilaiMapel = nilaiByMapel.get(namaMapel);
            String uts1 = nilaiMapel.stream().filter(n -> "UTS1".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uas1 = nilaiMapel.stream().filter(n -> "UAS1".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uts2 = nilaiMapel.stream().filter(n -> "UTS2".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uas2 = nilaiMapel.stream().filter(n -> "UAS2".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");

            sb.append(String.format("| %-30s | %-5s | %-5s | %-5s | %-5s |\n", namaMapel, uts1, uas1, uts2, uas2));
        }
        sb.append("---------------------------------------------------------------\n\n");
        sb.append("Catatan Wali Kelas:\n");
        sb.append("...............................................................\n");
        sb.append("...............................................................\n\n\n");
        sb.append("Mengetahui,                                 Orang Tua/Wali,\n");
        sb.append("Wali Kelas,\n\n\n\n");
        sb.append("____________________                        ____________________\n");

        return sb.toString();
    }

    @FXML
    private void handleCetakPDF() {
        Siswa selectedSiswa = siswaComboBox.getValue();
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih seorang siswa terlebih dahulu.");
            return;
        }

        // 1. Buka File Chooser untuk memilih lokasi penyimpanan
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rapor PDF");
        fileChooser.setInitialFileName("Rapor_" + selectedSiswa.getNama().replace(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(raporTextArea.getScene().getWindow());

        if (file == null) {
            return; // Pengguna membatalkan dialog
        }

        // 2. Buat Dokumen PDF
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 3. Tulis konten ke PDF
            drawRaporContent(contentStream, selectedSiswa);

            contentStream.close();
            document.save(file);

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Rapor berhasil disimpan sebagai PDF di:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuat file PDF.");
        }
    }

    private void drawRaporContent(PDPageContentStream contentStream, Siswa siswa) throws IOException {
        final float margin = 50;
        final float yStart = PDRectangle.A4.getUpperRightY() - margin;
        final float width = PDRectangle.A4.getWidth() - 2 * margin;
        float currentY = yStart;
        final float lineHeight = 15;

        // Ambil data nilai
        List<NilaiRapor> daftarNilai = getNilaiSiswa(siswa.getId());
        Map<String, List<NilaiRapor>> nilaiByMapel = daftarNilai.stream()
                .collect(Collectors.groupingBy(NilaiRapor::getNamaMapel));

        // Tulis Judul
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
        contentStream.newLineAtOffset((width / 2) - 50, currentY);
        contentStream.showText("RAPOR SISWA");
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Tulis Biodata
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Nama Siswa    : " + siswa.getNama());
        currentY -= lineHeight;
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("NIS           : " + siswa.getNis());
        currentY -= lineHeight;
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Kelas         : " + this.namaKelas);
        currentY -= lineHeight;
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("Tahun Ajaran  : " + this.tahunAjaran);
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Tulis Header Tabel Nilai (gunakan font monospaced agar lurus)
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 10);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("-----------------------------------------------------------------");
        currentY -= lineHeight;
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText(String.format("| %-30s | %-5s | %-5s | %-5s | %-5s |", "Mata Pelajaran", "UTS 1", "UAS 1", "UTS 2", "UAS 2"));
        currentY -= lineHeight;
        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("-----------------------------------------------------------------");
        currentY -= lineHeight;

        // Tulis isi tabel
        for (String namaMapel : nilaiByMapel.keySet()) {
            List<NilaiRapor> nilaiMapel = nilaiByMapel.get(namaMapel);
            String uts1 = nilaiMapel.stream().filter(n -> "UTS1".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uas1 = nilaiMapel.stream().filter(n -> "UAS1".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uts2 = nilaiMapel.stream().filter(n -> "UTS2".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");
            String uas2 = nilaiMapel.stream().filter(n -> "UAS2".equals(n.getJenisUjian())).findFirst().map(n -> String.valueOf(n.getNilai())).orElse("-");

            contentStream.newLineAtOffset(0, -lineHeight);
            contentStream.showText(String.format("| %-30s | %-5s | %-5s | %-5s | %-5s |", namaMapel, uts1, uas1, uts2, uas2));
            currentY -= lineHeight;
        }

        contentStream.newLineAtOffset(0, -lineHeight);
        contentStream.showText("-----------------------------------------------------------------");
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Tulis Catatan dan Tanda Tangan
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Catatan Wali Kelas:");
        currentY -= lineHeight * 4; // Beri spasi untuk catatan
        contentStream.newLineAtOffset(0, -lineHeight * 4);
        contentStream.showText("Mengetahui,");
        contentStream.newLineAtOffset(width/2, 0); // Pindah ke kanan
        contentStream.showText("Orang Tua/Wali,");
        currentY -= lineHeight;
        contentStream.newLineAtOffset(-(width/2), -lineHeight); // Kembali ke kiri
        contentStream.showText("Wali Kelas,");
        currentY -= lineHeight * 4;
        contentStream.newLineAtOffset(0, -lineHeight*4);
        contentStream.showText("____________________");
        contentStream.newLineAtOffset(width/2, 0);
        contentStream.showText("____________________");
        contentStream.endText();
    }

    private List<NilaiRapor> getNilaiSiswa(int siswaId) {
        List<NilaiRapor> hasil = new ArrayList<>();
        String sql = "SELECT mp.nama_mapel, n.jenis_ujian, n.nilai " +
                "FROM nilai n " +
                "JOIN siswa_kelas sk ON n.siswa_kelas_id = sk.siswa_kelas_id " +
                "JOIN mata_pelajaran mp ON n.mapel_id = mp.mapel_id " +
                "WHERE sk.siswa_id = ? AND sk.kelas_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, siswaId);
            pstmt.setInt(2, this.kelasId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                hasil.add(new NilaiRapor(rs.getString("nama_mapel"), rs.getString("jenis_ujian"), rs.getInt("nilai")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasil;
    }

    @FXML
    private void handleKembali() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/WaliKelasDashboard.fxml");
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

    // --- Inner classes untuk menampung data ---
    private static class Siswa {
        private final int id;
        private final String nis;
        private final String nama;

        public Siswa(int id, String nis, String nama) {
            this.id = id;
            this.nis = nis;
            this.nama = nama;
        }
        public int getId() { return id; }
        public String getNis() { return nis; }
        public String getNama() { return nama; }
    }

    private static class NilaiRapor {
        private final String namaMapel;
        private final String jenisUjian;
        private final int nilai;

        public NilaiRapor(String namaMapel, String jenisUjian, int nilai) {
            this.namaMapel = namaMapel;
            this.jenisUjian = jenisUjian;
            this.nilai = nilai;
        }
        public String getNamaMapel() { return namaMapel; }
        public String getJenisUjian() { return jenisUjian; }
        public int getNilai() { return nilai; }
    }
}