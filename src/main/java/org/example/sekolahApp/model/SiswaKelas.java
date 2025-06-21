package org.example.sekolahApp.model;

public class SiswaKelas {
    private final int siswaKelasId;
    private final Siswa siswa;
    private final Kelas kelas;

    public SiswaKelas(int siswaKelasId, Siswa siswa, Kelas kelas) {
        this.siswaKelasId = siswaKelasId;
        this.siswa = siswa;
        this.kelas = kelas;
    }

    public int getSiswaKelasId() { return siswaKelasId; }
    public Siswa getSiswa() { return siswa; }
    public Kelas getKelas() { return kelas; }
}