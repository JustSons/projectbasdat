package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalTime;

public class Jadwal {
    private final IntegerProperty jadwalId;
    private final StringProperty hari;
    private LocalTime jamMulai;
    private LocalTime jamSelesai;

    // Relasi ke objek lain
    private Kelas kelas;
    private MataPelajaran mapel;
    private Staff guru;

    public Jadwal(int jadwalId, String hari, LocalTime jamMulai, LocalTime jamSelesai, Kelas kelas, MataPelajaran mapel, Staff guru) {
        this.jadwalId = new SimpleIntegerProperty(jadwalId);
        this.hari = new SimpleStringProperty(hari);
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesai;
        this.kelas = kelas;
        this.mapel = mapel;
        this.guru = guru;
    }

    // Getters
    public int getJadwalId() { return jadwalId.get(); }
    public String getHari() { return hari.get(); }
    public LocalTime getJamMulai() { return jamMulai; }
    public LocalTime getJamSelesai() { return jamSelesai; }
    public Kelas getKelas() { return kelas; }
    public MataPelajaran getMapel() { return mapel; }
    public Staff getGuru() { return guru; }

    // Property Getters untuk TableView
    public IntegerProperty jadwalIdProperty() { return jadwalId; }
    public StringProperty hariProperty() { return hari; }
}