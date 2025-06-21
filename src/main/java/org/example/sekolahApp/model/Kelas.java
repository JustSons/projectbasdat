package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kelas {
    private final IntegerProperty kelasId;
    private final StringProperty namaKelas;
    private final ObjectProperty<TahunAjaran> tahunAjaran; // Reference to TahunAjaran object
    private final ObjectProperty<Staff> waliKelas; // Reference to Staff object for wali kelas

    public Kelas(int kelasId, String namaKelas, TahunAjaran tahunAjaran, Staff waliKelas) {
        this.kelasId = new SimpleIntegerProperty(kelasId);
        this.namaKelas = new SimpleStringProperty(namaKelas);
        this.tahunAjaran = new SimpleObjectProperty<>(tahunAjaran);
        this.waliKelas = new SimpleObjectProperty<>(waliKelas);
    }

    // Constructor simpel, hanya untuk ID dan Nama
    public Kelas(int kelasId, String namaKelas) {
        this.kelasId = new SimpleIntegerProperty(kelasId);
        this.namaKelas = new SimpleStringProperty(namaKelas);
        // Atribut lain bisa di-set ke null agar tidak error
        this.tahunAjaran = new SimpleObjectProperty<>(null);
        this.waliKelas = new SimpleObjectProperty<>(null);
    }

    // Constructor simplified if loading from DB and only need IDs
    // Ini mungkin tidak terlalu dibutuhkan lagi karena kita memuat objek penuh
    // public Kelas(int kelasId, String namaKelas, int tahunAjaranId, String tahunAjaranStr, Integer waliKelasId, String waliKelasNama) {
    //     this(kelasId, namaKelas,
    //          new TahunAjaran(tahunAjaranId, tahunAjaranStr, null),
    //          (waliKelasId != null && waliKelasNama != null) ? new Staff(waliKelasId, waliKelasNama) : null
    //     );
    // }


    // Getters
    public int getKelasId() { return kelasId.get(); }
    public String getNamaKelas() { return namaKelas.get(); }
    public TahunAjaran getTahunAjaran() { return tahunAjaran.get(); }
    public Staff getWaliKelas() { return waliKelas.get(); }

    // Property Getters
    public IntegerProperty kelasIdProperty() { return kelasId; }
    public StringProperty namaKelasProperty() { return namaKelas; }
    public ObjectProperty<TahunAjaran> tahunAjaranProperty() { return tahunAjaran; }
    public ObjectProperty<Staff> waliKelasProperty() { return waliKelas; }


    @Override
    public String toString() {
        // PERBAIKAN: Cek apakah tahunAjaran tidak null sebelum digunakan
        if (getTahunAjaran() != null && getTahunAjaran().getTahunAjaran() != null) {
            // Jika lengkap, tampilkan nama dan tahun ajaran
            return getNamaKelas() + " (" + getTahunAjaran().getTahunAjaran() + ")";
        } else {
            // Jika tidak lengkap (misal: dari halaman Kelola Kelulusan),
            // cukup kembalikan nama kelasnya saja.
            return getNamaKelas();
        }
    }
}