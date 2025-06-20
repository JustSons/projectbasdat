package org.example.sekolahApp.controller;

// ... KELOLA STAFF, KELAS, MAPEL CONTROLLERS ...
// Polanya SANGAT MIRIP dengan KelolaSiswaController.
// - FXML untuk form dan table
// - Controller dengan loadData(), populateForm(), handleSave(), handleDelete()
// - Model yang sesuai (Staff.java, Kelas.java, dll)


// PembagianKelasController.java (Contoh Fitur Transaksi)

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import org.example.sekolahApp.model.Siswa;
import org.example.sekolahApp.model.Kelas;

import java.net.URL;
import java.util.ResourceBundle;

public class PembagianKelasController implements Initializable {
    @FXML
    private ComboBox<Kelas> kelasComboBox;
    @FXML private ListView<Siswa> siswaTersediaListView;
    @FXML private ListView<Siswa> siswaDiKelasListView;
    // ... (buttons)

    private ObservableList<Kelas> kelasList = FXCollections.observableArrayList();
    private ObservableList<Siswa> siswaTersediaList = FXCollections.observableArrayList();
    private ObservableList<Siswa> siswaDiKelasList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load semua kelas ke ComboBox
        // ...
        // Tambahkan listener ke ComboBox untuk memuat siswa saat kelas dipilih
        kelasComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                loadSiswaDiKelas(newV.getId());
                loadSiswaBelumPunyaKelas();
            }
        });
    }

    private void loadSiswaDiKelas(int kelasId) { /* ... Ambil data dari tabel siswa_kelas ... */ }
    private void loadSiswaBelumPunyaKelas() { /* ... Ambil siswa yang tidak ada di tabel siswa_kelas ... */ }

    @FXML private void handlePindahKeKelas() {
        // Pindahkan siswa dari list 'tersedia' ke 'di kelas'
        Siswa selected = siswaTersediaListView.getSelectionModel().getSelectedItem();
        // ... (logika pindah item antar list)
    }

    @FXML private void handleKeluarkanDariKelas() { /* ... Logika sebaliknya ... */ }

    @FXML private void handleSimpanPerubahan() {
        // 1. Hapus semua entri siswa_kelas untuk kelas yang dipilih.
        // 2. Insert semua siswa yang ada di `siswaDiKelasListView` ke tabel `siswa_kelas`.
        // Lakukan ini dalam satu transaksi database (conn.setAutoCommit(false), commit, rollback).
    }
}

