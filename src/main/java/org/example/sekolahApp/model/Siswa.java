package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class Siswa {
    private final IntegerProperty id;
    private final StringProperty nis;
    private final StringProperty nama;
    private final StringProperty alamat;
    private final StringProperty jenisKelamin;
    private final StringProperty agama;
    private LocalDate tanggalLahir;
    private final StringProperty namaOrangTua;
    private final StringProperty teleponOrangTua;

    public Siswa(int id, String nis, String nama, String alamat, String jenisKelamin, String agama, LocalDate tanggalLahir, String namaOrangTua, String teleponOrangTua) {
        this.id = new SimpleIntegerProperty(id);
        this.nis = new SimpleStringProperty(nis);
        this.nama = new SimpleStringProperty(nama);
        this.alamat = new SimpleStringProperty(alamat);
        this.jenisKelamin = new SimpleStringProperty(jenisKelamin);
        this.agama = new SimpleStringProperty(agama);
        this.tanggalLahir = tanggalLahir;
        this.namaOrangTua = new SimpleStringProperty(namaOrangTua);
        this.teleponOrangTua = new SimpleStringProperty(teleponOrangTua);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getNis() { return nis.get(); }
    public String getNama() { return nama.get(); }
    public String getAlamat() { return alamat.get(); }
    public String getJenisKelamin() { return jenisKelamin.get(); }
    public String getAgama() { return agama.get(); }
    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public String getNamaOrangTua() { return namaOrangTua.get(); }
    public String getTeleponOrangTua() { return teleponOrangTua.get(); }
    @Override public String toString() { return getNama(); }


    // Setters
    public void setNis(String value) { nis.set(value); }
    public void setNama(String value) { nama.set(value); }
    public void setAlamat(String value) { alamat.set(value); }
    public void setJenisKelamin(String value) { jenisKelamin.set(value); }
    public void setAgama(String value) { agama.set(value); }
    public void setTanggalLahir(LocalDate value) { this.tanggalLahir = value; }
    public void setNamaOrangTua(String value) { namaOrangTua.set(value); }
    public void setTeleponOrangTua(String value) { teleponOrangTua.set(value); }

    // Property Getters (untuk TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty nisProperty() { return nis; }
    public StringProperty namaProperty() { return nama; }
}