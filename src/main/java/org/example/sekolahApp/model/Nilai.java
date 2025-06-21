package org.example.sekolahApp.model;

public class Nilai {
    private final String namaMapel;
    private final String jenisUjian;
    private final int nilai;

    public Nilai(String namaMapel, String jenisUjian, int nilai) {
        this.namaMapel = namaMapel;
        this.jenisUjian = jenisUjian;
        this.nilai = nilai;
    }

    // Buat semua getter (getNamaMapel(), getJenisUjian(), dst.)
    public String getNamaMapel() { return namaMapel; }
    public String getJenisUjian() { return jenisUjian; }
    public int getNilai() { return nilai; }
}