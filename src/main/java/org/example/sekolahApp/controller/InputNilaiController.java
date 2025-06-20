package org.example.sekolahApp.controller;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.Mapel;
import org.example.sekolahApp.model.Nilai;

import java.net.URL;
import java.util.ResourceBundle;

// InputNilaiController.java (Fitur Guru)
public class InputNilaiController implements Initializable {
    @FXML
    private ComboBox<Kelas> kelasAjarComboBox;
    @FXML private ComboBox<Mapel> mapelAjarComboBox;
    @FXML private TableView<Nilai> nilaiTableView;
    // ... (kolom tabel: Nama Siswa, Jenis Ujian, Nilai)

    @Override public void initialize(URL url, ResourceBundle rb) {
        // Load kelas & mapel yang diajar oleh guru yang login
        // (Gunakan UserSession.getInstance().getReferenceId() untuk mendapatkan ID guru)
        // ...
    }
    // ... (logika untuk memuat siswa saat kelas & mapel dipilih)
    // ... (logika untuk menyimpan/update nilai di tabel)
}