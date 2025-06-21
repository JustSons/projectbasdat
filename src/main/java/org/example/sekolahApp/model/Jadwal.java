package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalTime;

public class Jadwal {
    private final IntegerProperty jadwalId;
    private final ObjectProperty<Kelas> kelas; // Objek Kelas (dengan TahunAjaran di dalamnya)
    private final StringProperty hari;
    private final ObjectProperty<LocalTime> jamMulai;
    private final ObjectProperty<LocalTime> jamSelesai;
    private final ObjectProperty<Staff> guru; // Menggunakan Staff, bukan Guru
    private final ObjectProperty<MataPelajaran> mapel; // Objek MataPelajaran

    public Jadwal(int jadwalId, Kelas kelas, String hari, LocalTime jamMulai, LocalTime jamSelesai, Staff guru, MataPelajaran mapel) {
        this.jadwalId = new SimpleIntegerProperty(jadwalId);
        this.kelas = new SimpleObjectProperty<>(kelas);
        this.hari = new SimpleStringProperty(hari);
        this.jamMulai = new SimpleObjectProperty<>(jamMulai);
        this.jamSelesai = new SimpleObjectProperty<>(jamSelesai);
        this.guru = new SimpleObjectProperty<>(guru);
        this.mapel = new SimpleObjectProperty<>(mapel);
    }

    // Getters
    public int getJadwalId() { return jadwalId.get(); }
    public Kelas getKelas() { return kelas.get(); }
    public String getHari() { return hari.get(); }
    public LocalTime getJamMulai() { return jamMulai.get(); }
    public LocalTime getJamSelesai() { return jamSelesai.get(); }
    public Staff getGuru() { return guru.get(); } // Mengembalikan Staff
    public MataPelajaran getMapel() { return mapel.get(); }

    // Property Getters (untuk TableView)
    public IntegerProperty jadwalIdProperty() { return jadwalId; }
    public ObjectProperty<Kelas> kelasProperty() { return kelas; }
    public StringProperty hariProperty() { return hari; }
    public ObjectProperty<LocalTime> jamMulaiProperty() { return jamMulai; }
    public ObjectProperty<LocalTime> jamSelesaiProperty() { return jamSelesai; }
    public ObjectProperty<Staff> guruProperty() { return guru; } // Mengembalikan ObjectProperty<Staff>
    public ObjectProperty<MataPelajaran> mapelProperty() { return mapel; }

    @Override
    public String toString() {
        return getMapel().getNamaMapel() + " (" + getHari() + ", " + getJamMulai() + "-" + getJamSelesai() + ") - " + getKelas().getNamaKelas();
    }
}