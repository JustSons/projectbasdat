package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TahunAjaran {
    private final IntegerProperty tahunAjaranId;
    private final StringProperty tahunAjaran;
    private final StringProperty status;

    public TahunAjaran(int tahunAjaranId, String tahunAjaran, String status) {
        this.tahunAjaranId = new SimpleIntegerProperty(tahunAjaranId);
        this.tahunAjaran = new SimpleStringProperty(tahunAjaran);
        this.status = new SimpleStringProperty(status);
    }

    public int getTahunAjaranId() { return tahunAjaranId.get(); }
    public String getTahunAjaran() { return tahunAjaran.get(); }
    public String getStatus() { return status.get(); }

    public IntegerProperty tahunAjaranIdProperty() { return tahunAjaranId; }
    public StringProperty tahunAjaranProperty() { return tahunAjaran; }
    public StringProperty statusProperty() { return status; }

    @Override
    public String toString() {
        return getTahunAjaran();
    }
}