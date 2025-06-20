package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Mapel {
    private final IntegerProperty id;
    private final StringProperty kodeMapel;
    private final StringProperty namaMapel;
    public Mapel(int id, String kode, String nama) {
        this.id = new SimpleIntegerProperty(id);
        this.kodeMapel = new SimpleStringProperty(kode);
        this.namaMapel = new SimpleStringProperty(nama);
    }
    // Getters
    public int getId() { return id.get(); }
    public String getNamaMapel() { return namaMapel.get(); }
    @Override public String toString() { return getNamaMapel(); }
}
