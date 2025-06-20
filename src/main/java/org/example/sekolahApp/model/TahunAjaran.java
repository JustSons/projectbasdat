package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TahunAjaran {
    private final IntegerProperty id;
    private final StringProperty tahunAjaran;
    public TahunAjaran(int id, String tahun) {
        this.id = new SimpleIntegerProperty(id);
        this.tahunAjaran = new SimpleStringProperty(tahun);
    }
    // Getters
    public int getId() { return id.get(); }
    public String getTahunAjaran() { return tahunAjaran.get(); }
    @Override public String toString() { return getTahunAjaran(); }
}