package org.example.sekolahApp.model;

public class Jadwal {
    private final String hari;
    private final String jamMulai;
    private final String jamSelesai;
    private final String namaMapel;
    private final String namaGuru;

    public Jadwal(String hari, String jamMulai, String jamSelesai, String namaMapel, String namaGuru) {
        this.hari = hari;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.namaMapel = namaMapel;
        this.namaGuru = namaGuru;
    }

    // Buat semua getter (getHari(), getJamMulai(), dst.)
    public String getHari() { return hari; }
    public String getJamMulai() { return jamMulai; }
    public String getJamSelesai() { return jamSelesai; }
    public String getNamaMapel() { return namaMapel; }
    public String getNamaGuru() { return namaGuru; }
}