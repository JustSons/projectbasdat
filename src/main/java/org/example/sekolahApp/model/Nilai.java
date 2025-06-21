package org.example.sekolahApp.model;

public class Nilai {
    private final int idSiswa;
    private final String namaMapel;
    private final String jenisUjian;
    private final int nilai;
    private final String tahunAjaran;
    private final int semester;

    public Nilai(int idSiswa, String namaMapel, String jenisUjian, int nilai, String tahunAjaran, int semester) {
        this.idSiswa = idSiswa;
        this.namaMapel = namaMapel;
        this.jenisUjian = jenisUjian;
        this.nilai = nilai;
        this.tahunAjaran = tahunAjaran;
        this.semester = semester;
    }

    // Getters
    public int getIdSiswa() { return idSiswa; }
    public String getNamaMapel() { return namaMapel; }
    public String getJenisUjian() { return jenisUjian; }
    public int getNilai() { return nilai; }
    public String getTahunAjaran() { return tahunAjaran; }
    public int getSemester() { return semester; }
}