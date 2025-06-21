package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Staff {
    private final IntegerProperty staffId;
    private final StringProperty nip;
    private final StringProperty namaStaff;
    private final StringProperty jabatan;
    private final StringProperty email;
    private final StringProperty nomorTelepon;
    private final StringProperty alamat;
    private LocalDate tanggalLahir;

    public Staff(int staffId, String nip, String namaStaff, String jabatan, String email, String nomorTelepon, String alamat, LocalDate tanggalLahir) {
        this.staffId = new SimpleIntegerProperty(staffId);
        this.nip = (nip != null) ? new SimpleStringProperty(nip) : new SimpleStringProperty("");
        this.namaStaff = new SimpleStringProperty(namaStaff);
        this.jabatan = (jabatan != null) ? new SimpleStringProperty(jabatan) : new SimpleStringProperty("");
        this.email = (email != null) ? new SimpleStringProperty(email) : new SimpleStringProperty("");
        this.nomorTelepon = (nomorTelepon != null) ? new SimpleStringProperty(nomorTelepon) : new SimpleStringProperty("");
        this.alamat = (alamat != null) ? new SimpleStringProperty(alamat) : new SimpleStringProperty("");
        this.tanggalLahir = tanggalLahir;
    }

    // Constructor simplified for ComboBox display
    public Staff(int staffId, String namaStaff) {
        this(staffId, null, namaStaff, null, null, null, null, null);
    }

    // Getters
    public int getStaffId() { return staffId.get(); }
    public String getNip() { return nip.get(); }
    public String getNamaStaff() { return namaStaff.get(); }
    public String getJabatan() { return jabatan.get(); }
    public String getEmail() { return email.get(); }
    public String getNomorTelepon() { return nomorTelepon.get(); }
    public String getAlamat() { return alamat.get(); }
    public LocalDate getTanggalLahir() { return tanggalLahir; }

    // Property Getters
    public IntegerProperty staffIdProperty() { return staffId; }
    public StringProperty nipProperty() { return nip; }
    public StringProperty namaStaffProperty() { return namaStaff; }
    public StringProperty jabatanProperty() { return jabatan; }
    public StringProperty emailProperty() { return email; }
    public StringProperty nomorTeleponProperty() { return nomorTelepon; }
    public StringProperty alamatProperty() { return alamat; }

    @Override
    public String toString() {
        return getNamaStaff(); // Penting untuk ComboBox
    }
}