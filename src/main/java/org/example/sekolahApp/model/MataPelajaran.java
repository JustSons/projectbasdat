package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MataPelajaran {
    private final IntegerProperty mapelId;
    private final StringProperty kodeMapel; // Tambah ini
    private final StringProperty namaMapel;

    public MataPelajaran(int mapelId, String kodeMapel, String namaMapel) {
        this.mapelId = new SimpleIntegerProperty(mapelId);
        this.kodeMapel = new SimpleStringProperty(kodeMapel);
        this.namaMapel = new SimpleStringProperty(namaMapel);
    }

    // Constructor simplified for ComboBox display
    public MataPelajaran(int mapelId, String namaMapel) {
        this(mapelId, null, namaMapel); // kodeMapel bisa null jika hanya nama yang diperlukan
    }

    public int getMapelId() { return mapelId.get(); }
    public String getKodeMapel() { return kodeMapel.get(); }
    public String getNamaMapel() { return namaMapel.get(); }

    public IntegerProperty mapelIdProperty() { return mapelId; }
    public StringProperty kodeMapelProperty() { return kodeMapel; }
    public StringProperty namaMapelProperty() { return namaMapel; }

    @Override
    public String toString() {
        return getNamaMapel(); // Penting untuk ComboBox
    }
}