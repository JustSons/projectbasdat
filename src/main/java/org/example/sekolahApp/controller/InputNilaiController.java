package org.example.sekolahApp.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.example.sekolahApp.model.*;
import org.example.sekolahApp.*; // <-- HAPUS BARIS INI

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class InputNilaiController {

    @FXML private ComboBox<TahunAjaran> tahunAjaranComboBox;
    @FXML private ComboBox<Kelas> kelasComboBox;
    @FXML private ComboBox<MataPelajaran> mapelComboBox;
    @FXML private TableView<RekapNilaiView> siswaTableView;
    @FXML private TableColumn<RekapNilaiView, String> nisColumn;
    @FXML private TableColumn<RekapNilaiView, String> namaColumn;
    @FXML private TableColumn<RekapNilaiView, Integer> ujian1Column;
    @FXML private TableColumn<RekapNilaiView, Integer> ujian2Column;
    @FXML private TableColumn<RekapNilaiView, Integer> uts1Column;
    @FXML private TableColumn<RekapNilaiView, Integer> uas1Column;
    @FXML private TableColumn<RekapNilaiView, Integer> ujian3Column;
    @FXML private TableColumn<RekapNilaiView, Integer> ujian4Column;
    @FXML private TableColumn<RekapNilaiView, Integer> uts2Column;
    @FXML private TableColumn<RekapNilaiView, Integer> uas2Column;

    private final TahunAjaranDAO tahunAjaranDAO = new TahunAjaranDAO();
    private final KelasDAO kelasDAO = new KelasDAO();
    private final MataPelajaranDAO mapelDAO = new MataPelajaranDAO();
    private final SiswaKelasDAO siswaKelasDAO = new SiswaKelasDAO();
    private final NilaiDAO nilaiDAO = new NilaiDAO();

    private final ObservableList<RekapNilaiView> rekapNilaiViewList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupControls();
        setupTableView();
        loadInitialData();
    }

    private void loadInitialData() {
        try {
            tahunAjaranComboBox.setItems(FXCollections.observableArrayList(tahunAjaranDAO.getAllAktif()));
        } catch (SQLException e) { handleDbError(e); }
    }

    private void setupControls() {
        configureComboBox(tahunAjaranComboBox);
        configureComboBox(kelasComboBox);
        configureComboBox(mapelComboBox);
        tahunAjaranComboBox.setOnAction(e -> loadKelasByTahunAjaran());
        kelasComboBox.setOnAction(e -> loadMapel());
        mapelComboBox.setOnAction(e -> loadSiswaDanNilai());
    }

    private void loadKelasByTahunAjaran() {
        TahunAjaran selected = tahunAjaranComboBox.getValue();
        if (selected == null) return;
        try {
            kelasComboBox.setItems(FXCollections.observableArrayList(kelasDAO.getByTahunAjaranId(selected.getTahunAjaranId())));
        } catch (SQLException e) { handleDbError(e); }
    }

    private void loadMapel() {
        try {
            mapelComboBox.setItems(FXCollections.observableArrayList(mapelDAO.getAll()));
        } catch (SQLException e) { handleDbError(e); }
    }

    private void loadSiswaDanNilai() {
        Kelas selectedKelas = kelasComboBox.getValue();
        MataPelajaran selectedMapel = mapelComboBox.getValue();
        if (selectedKelas == null || selectedMapel == null) return;

        try {
            List<SiswaKelas> pendaftaranSiswa = siswaKelasDAO.getSiswaInKelas(selectedKelas.getKelasId());
            rekapNilaiViewList.clear();

            if (pendaftaranSiswa.isEmpty()) {
                siswaTableView.setItems(rekapNilaiViewList);
                return;
            }

            List<Integer> siswaKelasIds = pendaftaranSiswa.stream().map(SiswaKelas::getSiswaKelasId).collect(Collectors.toList());
            List<Nilai> semuaNilai = nilaiDAO.getNilaiForSiswaKelas(siswaKelasIds, selectedMapel.getMapelId());

            for (SiswaKelas sk : pendaftaranSiswa) {
                RekapNilaiView rekapView = new RekapNilaiView(sk);
                semuaNilai.stream()
                        .filter(n -> n.getSiswaKelasId() == sk.getSiswaKelasId())
                        .forEach(n -> rekapView.setNilaiByJenis(n.getJenisUjian(), n.getNilai()));
                rekapNilaiViewList.add(rekapView);
            }

            siswaTableView.setItems(rekapNilaiViewList);
        } catch (SQLException e) {
            handleDbError(e);
        }
    }

    private void setupTableView() {
        nisColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().nisProperty());
        namaColumn.setCellValueFactory(cellData -> cellData.getValue().getSiswa().namaProperty());

        configureNilaiColumn(ujian1Column, "Ujian 1", "ujian1");
        configureNilaiColumn(ujian2Column, "Ujian 2", "ujian2");
        configureNilaiColumn(uts1Column, "UTS1", "uts1");
        configureNilaiColumn(uas1Column, "UAS1", "uas1");
        configureNilaiColumn(ujian3Column, "Ujian 3", "ujian3");
        configureNilaiColumn(ujian4Column, "Ujian 4", "ujian4");
        configureNilaiColumn(uts2Column, "UTS2", "uts2");
        configureNilaiColumn(uas2Column, "UAS2", "uas2");

        siswaTableView.setEditable(true);
    }

    private void configureNilaiColumn(TableColumn<RekapNilaiView, Integer> column, String jenisUjian, String propertyName) {
        column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(propertyName));
        column.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        column.setOnEditCommit(event -> {
            RekapNilaiView rekapView = event.getRowValue();
            MataPelajaran mapel = mapelComboBox.getValue();
            Integer nilaiBaru = event.getNewValue();

            if (mapel != null) {
                Nilai nilaiToSave = new Nilai(rekapView.getSiswaKelas().getSiswaKelasId(), mapel.getMapelId(), jenisUjian, nilaiBaru);
                try {
                    nilaiDAO.saveOrUpdateNilai(nilaiToSave);
                    rekapView.setNilaiByJenis(jenisUjian, nilaiBaru);
                } catch (SQLException e) {
                    handleDbError(e);
                    event.getTableView().refresh();
                }
            }
        });
    }

    @FXML
    private void handleSimpan() {
        showAlert(Alert.AlertType.INFORMATION, "Refresh Data", "Menyegarkan data dari database.");
        loadSiswaDanNilai();
    }

    private <T> void configureComboBox(ComboBox<T> comboBox) {
        comboBox.setConverter(new StringConverter<>() {
            @Override public String toString(T object) { return object == null ? null : object.toString(); }
            @Override public T fromString(String string) { return null; }
        });
    }

    private void handleDbError(SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Error Database", "Terjadi kesalahan pada database: " + e.getMessage());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void handleBack() { /* Implementasi navigasi Anda */ }
}