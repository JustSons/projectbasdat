package org.example.sekolahApp.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class RekapNilaiView {
    private final SiswaKelas siswaKelas;
    private final ObjectProperty<Integer> ujian1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> ujian2 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> uts1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> uas1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> ujian3 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> ujian4 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> uts2 = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> uas2 = new SimpleObjectProperty<>();

    public RekapNilaiView(SiswaKelas siswaKelas) { this.siswaKelas = siswaKelas; }
    public SiswaKelas getSiswaKelas() { return siswaKelas; }
    public Siswa getSiswa() { return siswaKelas.getSiswa(); }

    public void setNilaiByJenis(String jenisUjian, Integer nilai) {
        switch (jenisUjian) {
            case "Ujian 1": ujian1.set(nilai); break;
            case "Ujian 2": ujian2.set(nilai); break;
            case "UTS1":    uts1.set(nilai); break;
            case "UAS1":    uas1.set(nilai); break;
            case "Ujian 3": ujian3.set(nilai); break;
            case "Ujian 4": ujian4.set(nilai); break;
            case "UTS2":    uts2.set(nilai); break;
            case "UAS2":    uas2.set(nilai); break;
        }
    }
    public ObjectProperty<Integer> ujian1Property() { return ujian1; }
    public ObjectProperty<Integer> ujian2Property() { return ujian2; }
    public ObjectProperty<Integer> uts1Property() { return uts1; }
    public ObjectProperty<Integer> uas1Property() { return uas1; }
    public ObjectProperty<Integer> ujian3Property() { return ujian3; }
    public ObjectProperty<Integer> ujian4Property() { return ujian4; }
    public ObjectProperty<Integer> uts2Property() { return uts2; }
    public ObjectProperty<Integer> uas2Property() { return uas2; }
}