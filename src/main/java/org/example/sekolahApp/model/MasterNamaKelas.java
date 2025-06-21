package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class untuk tabel 'master_nama_kelas'.
 * Tabel ini hanya berfungsi sebagai template atau cetakan untuk nama-nama kelas.
 * Ini adalah "bahan baku" yang akan dikelola oleh menu "Kelola Kelas".
 */
public class MasterNamaKelas {
    private final IntegerProperty masterId;
    private final StringProperty namaKelasTemplate;

    public MasterNamaKelas(int masterId, String namaKelasTemplate) {
        this.masterId = new SimpleIntegerProperty(masterId);
        this.namaKelasTemplate = new SimpleStringProperty(namaKelasTemplate);
    }

    // Property Getters (diperlukan oleh JavaFX TableView)
    public IntegerProperty masterIdProperty() {
        return masterId;
    }

    public StringProperty namaKelasTemplateProperty() {
        return namaKelasTemplate;
    }

    // Standard Getters (metode get biasa)
    public int getMasterId() {
        return masterId.get();
    }

    public String getNamaKelasTemplate() {
        return namaKelasTemplate.get();
    }

    /**
     * Override toString() sangat penting agar nama kelas bisa tampil dengan benar
     * di dalam ComboBox tanpa perlu membuat cell factory khusus.
     */
    @Override
    public String toString() {
        return getNamaKelasTemplate();
    }
}