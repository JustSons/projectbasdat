package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Staff {
    private final IntegerProperty id;
    private final StringProperty nip;
    private final StringProperty nama;
    private final StringProperty jabatan;
    public Staff(int id, String nip, String nama, String jabatan) {
        this.id = new SimpleIntegerProperty(id);
        this.nip = new SimpleStringProperty(nip);
        this.nama = new SimpleStringProperty(nama);
        this.jabatan = new SimpleStringProperty(jabatan);
    }
    // Getters dan Setters
    public int getId() { return id.get(); }
    public String getNama() { return nama.get(); }
    public String getJabatan() { return jabatan.get(); }
    @Override public String toString() { return getNama(); }
}
