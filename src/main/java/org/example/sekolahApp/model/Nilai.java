package org.example.sekolahApp.model;

public class Nilai {
    private Integer nilaiId;
    private final int siswaKelasId;
    private final int mapelId;
    private final String jenisUjian;
    private int nilai;

    public Nilai(int siswaKelasId, int mapelId, String jenisUjian, int nilai) {
        this.siswaKelasId = siswaKelasId;
        this.mapelId = mapelId;
        this.jenisUjian = jenisUjian;
        this.nilai = nilai;
    }

    // Getters & Setters
    public Integer getNilaiId() { return nilaiId; }
    public void setNilaiId(Integer nilaiId) { this.nilaiId = nilaiId; }
    public int getSiswaKelasId() { return siswaKelasId; }
    public int getMapelId() { return mapelId; }
    public String getJenisUjian() { return jenisUjian; }
    public int getNilai() { return nilai; }
    public void setNilai(int nilai) { this.nilai = nilai; }
}