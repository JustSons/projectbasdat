// File: src/main/java/com/yourcompany/sekolahapp/model/Siswa.java
package org.example.sekolahApp.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

/**
 * Kelas Model untuk entitas Siswa.
 * Menggunakan JavaFX Properties untuk kemudahan binding ke TableView.
 */
public class Siswa {
    private final StringProperty nis;
    private final StringProperty namaSiswa;
    private final StringProperty alamat;
    private LocalDate tanggalLahir;

    public Siswa(String nis, String namaSiswa, String alamat, LocalDate tanggalLahir) {
        this.nis = new SimpleStringProperty(nis);
        this.namaSiswa = new SimpleStringProperty(namaSiswa);
        this.alamat = new SimpleStringProperty(alamat);
        this.tanggalLahir = tanggalLahir;
    }

    // Getters dan Setters untuk properties
    public String getNis() { return nis.get(); }
    public StringProperty nisProperty() { return nis; }
    public void setNis(String nis) { this.nis.set(nis); }

    public String getNamaSiswa() { return namaSiswa.get(); }
    public StringProperty namaSiswaProperty() { return namaSiswa; }
    public void setNamaSiswa(String namaSiswa) { this.namaSiswa.set(namaSiswa); }

    // ... getter dan setter lainnya ...
}