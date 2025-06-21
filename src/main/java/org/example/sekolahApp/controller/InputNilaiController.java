package org.example.sekolahApp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.example.sekolahApp.model.Nilai;
import org.example.sekolahApp.model.Siswa;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.util.SceneManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InputNilaiController {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<Integer> semesterComboBox;
    @FXML private ComboBox<String> mapelComboBox;

    @FXML private TableView<Siswa> siswaTableView;
    @FXML private TableColumn<Siswa, String> nisColumn;
    @FXML private TableColumn<Siswa, String> namaColumn;
    @FXML private TableColumn<Siswa, Integer> ujian1Column;
    @FXML private TableColumn<Siswa, Integer> ujian2Column;
    @FXML private TableColumn<Siswa, Integer> utsColumn;

    private final ObservableList<Siswa> masterSiswaList = FXCollections.observableArrayList();
    private final List<String> mataPelajaranGuru = new ArrayList<>();
    private final ObservableList<TahunAjaran> tahunAjaranList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupMockData();
        setupControls();
        setupTableView();
    }

    private void setupMockData() {
        tahunAjaranList.add(new TahunAjaran(1, "2024/2025", "Aktif"));
        tahunAjaranList.add(new TahunAjaran(2, "2025/2026", "Tidak Aktif"));

        Siswa siswa1 = new Siswa(1, "101", "Budi Santoso");
        Siswa siswa2 = new Siswa(2, "102", "Citra Lestari");

        siswa1.addOrUpdateNilai(new Nilai(1, "Matematika", "UTS", 80, "2024/2025", 1));
        siswa1.addOrUpdateNilai(new Nilai(1, "Fisika", "UTS", 75, "2024/2025", 1));
        siswa1.addOrUpdateNilai(new Nilai(1, "Matematika", "UTS", 88, "2025/2026", 1));

        masterSiswaList.addAll(siswa1, siswa2);

        mataPelajaranGuru.add("Matematika");
        mataPelajaranGuru.add("Fisika");
    }

    private void setupControls() {
        tahunAjaranComboBox.setItems(tahunAjaranList);

        tahunAjaranComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TahunAjaran object) {
                return object == null ? "" : object.getTahunAjaran();
            }

            @Override
            public TahunAjaran fromString(String string) {
                return null;
            }
        });

        semesterComboBox.setItems(FXCollections.observableArrayList(1, 2));
        mapelComboBox.setItems(FXCollections.observableArrayList(mataPelajaranGuru));

        tahunAjaranComboBox.setOnAction(e -> loadSiswaData());
        semesterComboBox.setOnAction(e -> loadSiswaData());
        mapelComboBox.setOnAction(e -> loadSiswaData());
    }

    private void loadSiswaData() {
        if (tahunAjaranComboBox.getValue() != null && semesterComboBox.getValue() != null && mapelComboBox.getValue() != null) {
            siswaTableView.setItems(masterSiswaList);
            siswaTableView.refresh();
        } else {
            siswaTableView.getItems().clear();
        }
    }

    private void setupTableView() {
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));

        configureNilaiColumn(ujian1Column, "Ujian 1");
        configureNilaiColumn(ujian2Column, "Ujian 2");
        configureNilaiColumn(utsColumn, "UTS");

        siswaTableView.setEditable(true);
    }

    private void configureNilaiColumn(TableColumn<Siswa, Integer> column, String jenisUjian) {
        column.setCellValueFactory(cellData -> {
            TahunAjaran ta = tahunAjaranComboBox.getValue();
            Integer semester = semesterComboBox.getValue();

            if (ta == null || semester == null) return null;

            String tahunAjaranStr = ta.getTahunAjaran();
            Siswa siswa = cellData.getValue();
            Nilai nilai = siswa.getNilaiByKonteks(jenisUjian, tahunAjaranStr, semester);

            if (nilai != null) {
                return new javafx.beans.property.SimpleIntegerProperty(nilai.getNilai()).asObject();
            }
            return null;
        });

        column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        column.setOnEditCommit(event -> {
            TahunAjaran ta = tahunAjaranComboBox.getValue();
            Integer semester = semesterComboBox.getValue();
            String mapel = mapelComboBox.getValue();

            if (ta == null || semester == null || mapel == null) {
                showAlert(Alert.AlertType.WARNING, "Konteks Tidak Lengkap", "Harap pilih Tahun Ajaran, Semester, dan Mata Pelajaran.");
                siswaTableView.refresh();
                return;
            }

            String tahunAjaranStr = ta.getTahunAjaran();
            Siswa siswa = event.getRowValue();
            Integer nilaiBaru = event.getNewValue();

            if (nilaiBaru != null) {
                Nilai n = new Nilai(siswa.getId(), mapel, jenisUjian, nilaiBaru, tahunAjaranStr, semester);
                siswa.addOrUpdateNilai(n);
                System.out.println("Nilai DENGAN KONTEKS diperbarui untuk " + siswa.getNama() + " -> " + jenisUjian + " (" + tahunAjaranStr + " Sem-" + semester + "): " + nilaiBaru);
            }
        });
    }

    @FXML
    private void handleSimpan() {
        System.out.println("\n--- SIMULASI SIMPAN KE DATABASE ---");
        for(Siswa s : masterSiswaList) {
            System.out.println("Histori Nilai untuk: " + s.getNama());
            if(s.getDaftarNilai().isEmpty()) {
                System.out.println("  (Tidak ada data nilai)");
            } else {
                for(Nilai n : s.getDaftarNilai()) {
                    System.out.printf("  - TA: %s, Sem: %d, Mapel: %s, Ujian: %s, Nilai: %d\n",
                            n.getTahunAjaran(), n.getSemester(), n.getNamaMapel(), n.getJenisUjian(), n.getNilai());
                }
            }
        }
        System.out.println("---------------------------------");
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data nilai berhasil disimpan (simulasi). Cek konsol untuk melihat semua histori nilai.");
    }

    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/guru_dashboard.fxml");
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