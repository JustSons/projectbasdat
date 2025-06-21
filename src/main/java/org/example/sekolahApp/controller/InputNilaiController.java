package org.example.sekolahApp.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.example.sekolahApp.model.*; // Impor semua model
// import org.example.sekolahApp.dao.*; // Nanti Anda akan impor DAO di sini

import java.util.List;
import java.util.stream.Collectors;

public class InputNilaiController {

    // --- FXML Controls Disesuaikan ---
    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private ComboBox<MataPelajaran> mapelComboBox;

    @FXML private TableView<SiswaKelas> siswaTableView; // Tipe data TableView diubah ke SiswaKelas
    @FXML private TableColumn<SiswaKelas, String> nisColumn;
    @FXML private TableColumn<SiswaKelas, String> namaColumn;

    // Ganti nama kolom nilai di FXML Anda menjadi fx:id="uts1Column", "uas1Column", dst.
    @FXML private TableColumn<SiswaKelas, Integer> uts1Column;
    @FXML private TableColumn<SiswaKelas, Integer> uas1Column;
    @FXML private TableColumn<SiswaKelas, Integer> uts2Column;
    @FXML private TableColumn<SiswaKelas, Integer> uas2Column;

    // --- Asumsi DAO sudah ada ---
    // private final TahunAjaranDAO tahunAjaranDAO = new TahunAjaranDAO();
    // private final KelasDAO kelasDAO = new KelasDAO();
    // private final MapelDAO mapelDAO = new MapelDAO();
    // private final SiswaKelasDAO siswaKelasDAO = new SiswaKelasDAO();
    // private final NilaiDAO nilaiDAO = new NilaiDAO();

    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private final ObservableList<MataPelajaran> mapelList = FXCollections.observableArrayList();
    private final ObservableList<SiswaKelas> siswaDiKelasList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupControls();
        setupTableView();
        loadInitialData();
    }

    private void loadInitialData() {
        // GANTI DENGAN PANGGILAN DAO
        // tahunAjaranList.setAll(tahunAjaranDAO.getAllAktif());
        tahunAjaranList.setAll(new TahunAjaran(1, "2024/2025", "Aktif"));
        tahunAjaranComboBox.setItems(tahunAjaranList);
    }

    private void setupControls() {
        configureComboBox(tahunAjaranComboBox);
        configureComboBox(kelasComboBox);
        configureComboBox(mapelComboBox);

        tahunAjaranComboBox.setOnAction(e -> loadKelasByTahunAjaran());
        kelasComboBox.setOnAction(e -> loadMapelByKelas());
        mapelComboBox.setOnAction(e -> loadSiswaDanNilai());
    }

    private void loadKelasByTahunAjaran() {
        TahunAjaran selected = tahunAjaranComboBox.getValue();
        if (selected == null) return;
        // PANGGILAN DAO:
        // kelasList.setAll(kelasDAO.getByTahunAjaran(selected.getId()));
        kelasList.setAll(new Kelas(101, "Kelas X-A", selected, null));
        kelasComboBox.setItems(kelasList);
    }

    private void loadMapelByKelas() {
        // Logika untuk memuat mapel yang diajar guru di kelas tersebut
        // PANGGILAN DAO:
        // mapelList.setAll(mapelDAO.getByGuruAndKelas(loggedInGuru.getId(), kelasComboBox.getValue().getId()));
        mapelList.setAll(new MataPelajaran(201, "MAT", "Matematika"));
        mapelComboBox.setItems(mapelList);
    }

    private void loadSiswaDanNilai() {
        Kelas selectedKelas = kelasComboBox.getValue();
        MataPelajaran selectedMapel = mapelComboBox.getValue();
        if (selectedKelas == null || selectedMapel == null) return;

        // 1. Ambil daftar siswa yang terdaftar di kelas ini
        // PANGGILAN DAO:
        // List<SiswaKelas> pendaftaranSiswa = siswaKelasDAO.getSiswaKelasByKelasId(selectedKelas.getKelasId());
        // Mockup:
        Siswa siswa1 = new Siswa(1, "101", "Budi");
        Siswa siswa2 = new Siswa(2, "102", "Citra");
        List<SiswaKelas> pendaftaranSiswa = List.of(
                new SiswaKelas(501, siswa1, selectedKelas),
                new SiswaKelas(502, siswa2, selectedKelas)
        );

        // 2. Ambil semua nilai yang sudah ada untuk semua siswa di kelas ini & mapel ini
        List<Integer> siswaKelasIds = pendaftaranSiswa.stream().map(SiswaKelas::getSiswaKelasId).collect(Collectors.toList());
        // PANGGILAN DAO:
        // List<Nilai> nilaiList = nilaiDAO.getNilaiForSiswaKelas(siswaKelasIds, selectedMapel.getMapelId());

        // Mockup Nilai:
        List<Nilai> nilaiList = List.of(new Nilai(501, selectedMapel.getMapelId(), "UTS1", 85));

        // 3. Distribusikan nilai ke setiap objek siswa
        for (SiswaKelas sk : pendaftaranSiswa) {
            List<Nilai> nilaiMilikSiswa = nilaiList.stream()
                    .filter(n -> n.getSiswaKelasId() == sk.getSiswaKelasId())
                    .collect(Collectors.toList());
            sk.getSiswa().setDaftarNilai(nilaiMilikSiswa);
        }

        // 4. Tampilkan di tabel
        siswaDiKelasList.setAll(pendaftaranSiswa);
        siswaTableView.setItems(siswaDiKelasList);
    }


    private void setupTableView() {
        // Kolom NIS dan Nama sekarang mengambil data dari objek Siswa di dalam SiswaKelas
        nisColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().nisProperty());
        namaColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().namaProperty());

        configureNilaiColumn(uts1Column, "UTS1");
        configureNilaiColumn(uas1Column, "UAS1");
        configureNilaiColumn(uts2Column, "UTS2");
        configureNilaiColumn(uas2Column, "UAS2");

        siswaTableView.setEditable(true);
    }

    private void configureNilaiColumn(TableColumn<SiswaKelas, Integer> column, String jenisUjian) {
        column.setCellValueFactory(cellData -> {
            Siswa siswa = cellData.getValue().getSiswa();
            MataPelajaran mapel = mapelComboBox.getValue();
            if (mapel == null) return null;

            Nilai nilai = siswa.getNilaiByMapelAndJenis(mapel.getMapelId(), jenisUjian);
            if (nilai != null) {
                return new SimpleIntegerProperty(nilai.getNilai()).asObject();
            }
            return null;
        });

        column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        column.setOnEditCommit(event -> {
            SiswaKelas siswaKelas = event.getRowValue();
            MataPelajaran mapel = mapelComboBox.getValue();
            Integer nilaiBaru = event.getNewValue();

            if (mapel != null && nilaiBaru != null) {
                Nilai n = new Nilai(siswaKelas.getSiswaKelasId(), mapel.getMapelId(), jenisUjian, nilaiBaru);
                siswaKelas.getSiswa().addOrUpdateNilai(n);
                System.out.println("Data di memori diperbarui!");
            }
        });
    }

    @FXML
    private void handleSimpan() {
        System.out.println("--- MENYIMPAN PERUBAHAN KE DATABASE ---");
        for (SiswaKelas sk : siswaDiKelasList) {
            for (Nilai n : sk.getSiswa().getDaftarNilai()) {
                System.out.printf("Menyimpan: SiswaKelasID=%d, MapelID=%d, Jenis=%s, Nilai=%d\n",
                        n.getSiswaKelasId(), n.getMapelId(), n.getJenisUjian(), n.getNilai());
                // PANGGILAN DAO:
                // nilaiDAO.saveOrUpdateNilai(n);
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data nilai berhasil disimpan (simulasi).");
    }

    private <T> void configureComboBox(ComboBox<T> comboBox) {
        // Menggunakan toString() dari setiap model untuk tampilan
        comboBox.setConverter(new StringConverter<T>() {
            @Override public String toString(T object) { return object == null ? null : object.toString(); }
            @Override public T fromString(String string) { return null; }
        });
    }

    @FXML private void handleBack() { /* ... kode sama ... */ }
    private void showAlert(Alert.AlertType type, String title, String message) { /* ... kode sama ... */ }
}