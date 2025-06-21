package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private final StringProperty kelasSaatIniNama;
    private final StringProperty status; // Pastikan field ini ada
     private final List<Nilai> daftarNilai;

    // Constructor lengkap untuk saat membuat atau mengedit data siswa secara penuh
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
        this.kelasSaatIniNama = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("Aktif"); // Default status
        this.daftarNilai = new ArrayList<>();
    }

    // Constructor simpel untuk menampilkan di tabel (seperti di Kelola Kelulusan)
    public Siswa(int id, String nis, String nama) {
        this.id = new SimpleIntegerProperty(id);
        this.nis = new SimpleStringProperty(nis);
        this.nama = new SimpleStringProperty(nama);
        // Atribut lain diisi nilai default agar tidak error
        this.alamat = new SimpleStringProperty(null);
        this.jenisKelamin = new SimpleStringProperty(null);
        this.agama = new SimpleStringProperty(null);
        this.tanggalLahir = null;
        this.namaOrangTua = new SimpleStringProperty(null);
        this.teleponOrangTua = new SimpleStringProperty(null);
        this.kelasSaatIniNama = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("Aktif");
        this.daftarNilai = new ArrayList<>();
    }


    // --- Getters ---
    public int getId() { return id.get(); }
    public String getNis() { return nis.get(); }
    public String getNama() { return nama.get(); }
    public String getAlamat() { return alamat.get(); }
    public String getJenisKelamin() { return jenisKelamin.get(); }
    public String getAgama() { return agama.get(); }
    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public String getNamaOrangTua() { return namaOrangTua.get(); }
    public String getTeleponOrangTua() { return teleponOrangTua.get(); }
    public String getKelasSaatIniNama() { return kelasSaatIniNama.get(); }
    public String getStatus() { return status.get(); }


    // --- Setters ---
    public void setNis(String value) { nis.set(value); }
    public void setNama(String value) { nama.set(value); }
    public void setAlamat(String value) { alamat.set(value); }
    public void setJenisKelamin(String value) { jenisKelamin.set(value); }
    public void setAgama(String value) { agama.set(value); }
    public void setTanggalLahir(LocalDate value) { this.tanggalLahir = value; }
    public void setNamaOrangTua(String value) { namaOrangTua.set(value); }
    public void setTeleponOrangTua(String value) { teleponOrangTua.set(value); }
    public void setKelasSaatIniNama(String value) { kelasSaatIniNama.set(value); }
    public void setStatus(String value) { status.set(value); }


    // --- Property Getters (Sangat Penting untuk TableView) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty nisProperty() { return nis; }
    public StringProperty namaProperty() { return nama; }
    public StringProperty kelasSaatIniNamaProperty() { return kelasSaatIniNama; }
    public StringProperty statusProperty() { return status; } // <--- METHOD PENTING YANG HILANG

    @Override public String toString() { return getNama(); }
    public List<Nilai> getDaftarNilai() { return daftarNilai; }
    public Nilai getNilaiByKonteks(String jenisUjian, String tahunAjaran, int semester) {
        for (Nilai n : daftarNilai) {
            if (n.getJenisUjian().equals(jenisUjian) &&
                    n.getTahunAjaran().equals(tahunAjaran) &&
                    n.getSemester() == semester) {
                return n;
            }
        }
        return null;
    }

    public void addOrUpdateNilai(Nilai nilaiBaru) {
        Nilai nilaiLama = getNilaiByKonteks(
                nilaiBaru.getJenisUjian(),
                nilaiBaru.getTahunAjaran(),
                nilaiBaru.getSemester()
        );

        if (nilaiLama != null) {
            daftarNilai.remove(nilaiLama);
        }
        daftarNilai.add(nilaiBaru);
    }
}
