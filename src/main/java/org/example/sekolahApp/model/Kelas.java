package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kelas {
    private final IntegerProperty id;
    private final StringProperty namaKelas;
    private final IntegerProperty tahunAjaranId;
    private final StringProperty namaWaliKelas;
    public Kelas(int id, String nama, int tahunAjaranId, String namaWaliKelas) {
        this.id = new SimpleIntegerProperty(id);
        this.namaKelas = new SimpleStringProperty(nama);
        this.tahunAjaranId = new SimpleIntegerProperty(tahunAjaranId);
        this.namaWaliKelas = new SimpleStringProperty(namaWaliKelas);
    }
    // Getters
    public int getId() { return id.get(); }
    public String getNamaKelas() { return namaKelas.get(); }
    @Override public String toString() { return getNamaKelas(); }
}