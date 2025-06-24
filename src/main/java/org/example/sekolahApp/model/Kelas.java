package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kelas {
    private final int kelasId;
    private final StringProperty namaKelas;
    private final TahunAjaran tahunAjaran;
    private final Staff waliKelas;

    // Ini adalah konstruktor utama Anda yang sudah ada
    public Kelas(int kelasId, String namaKelas, TahunAjaran tahunAjaran, Staff waliKelas) {
        this.kelasId = kelasId;
        this.namaKelas = new SimpleStringProperty(namaKelas);
        this.tahunAjaran = tahunAjaran;
        this.waliKelas = waliKelas;
    }
    public Kelas(int kelasId, String namaKelas) {
        this(kelasId, namaKelas, new TahunAjaran(0, ""), null); // Memberi objek default agar tidak null
    }


    // --- Sisa kode di file ini biarkan seperti semula ---

    public int getKelasId() {
        return kelasId;
    }

    public String getNamaKelas() {
        return namaKelas.get();
    }

    public StringProperty namaKelasProperty() {
        return namaKelas;
    }

    public TahunAjaran getTahunAjaran() {
        return tahunAjaran;
    }

    public Staff getWaliKelas() {
        return waliKelas;
    }

    @Override
    public String toString() {
        // Jika tahun ajaran ada, tampilkan. Jika tidak, hanya nama kelas.
        if (getTahunAjaran() != null && !getTahunAjaran().getTahunAjaran().isEmpty()) {
            return getNamaKelas() + " (" + getTahunAjaran().getTahunAjaran() + ")";
        }
        return getNamaKelas();
    }
}