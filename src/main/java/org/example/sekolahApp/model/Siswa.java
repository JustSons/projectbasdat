package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Siswa {
    // Primary fields following STUDENT table schema
    private final IntegerProperty studentId;
    private final StringProperty studentName;
    private final StringProperty studentAddress;
    private final StringProperty studentGender; // 'L' or 'P' 
    private final StringProperty studentParentsPhoneNumber;
    private final StringProperty studentReligion;
    private LocalDate studentBirthDate;
    
    // Additional fields for compatibility with existing project
    private final StringProperty nis; // Keep for backward compatibility
    private final StringProperty kelasSaatIniNama; // Current class name
    private final StringProperty status; // Student status
    private final List<Nilai> daftarNilai; // Grades list

    // Constructor following STUDENT schema
    public Siswa(int studentId, String studentName, String studentAddress, String studentGender, 
                 String studentParentsPhoneNumber, String studentReligion, LocalDate studentBirthDate) {
        this.studentId = new SimpleIntegerProperty(studentId);
        this.studentName = new SimpleStringProperty(studentName != null ? studentName : "");
        this.studentAddress = new SimpleStringProperty(studentAddress != null ? studentAddress : "");
        this.studentGender = new SimpleStringProperty(studentGender != null ? studentGender : "");
        this.studentParentsPhoneNumber = new SimpleStringProperty(studentParentsPhoneNumber != null ? studentParentsPhoneNumber : "");
        this.studentReligion = new SimpleStringProperty(studentReligion != null ? studentReligion : "");
        this.studentBirthDate = studentBirthDate;
        
        // Compatibility fields
        this.nis = new SimpleStringProperty("");
        this.kelasSaatIniNama = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("ACTIVE"); // Default status
        this.daftarNilai = new ArrayList<>();
    }

    // Constructor with legacy fields for backward compatibility
    public Siswa(int id, String nis, String nama, String alamat, String jenisKelamin, String agama, 
                 LocalDate tanggalLahir, String namaOrangTua, String teleponOrangTua) {
        this.studentId = new SimpleIntegerProperty(id);
        this.studentName = new SimpleStringProperty(nama != null ? nama : "");
        this.studentAddress = new SimpleStringProperty(alamat != null ? alamat : "");
        this.studentGender = new SimpleStringProperty(jenisKelamin != null ? jenisKelamin : "");
        this.studentParentsPhoneNumber = new SimpleStringProperty(teleponOrangTua != null ? teleponOrangTua : "");
        this.studentReligion = new SimpleStringProperty(agama != null ? agama : "");
        this.studentBirthDate = tanggalLahir;
        
        // Legacy fields
        this.nis = new SimpleStringProperty(nis != null ? nis : "");
        this.kelasSaatIniNama = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("ACTIVE");
        this.daftarNilai = new ArrayList<>();
    }

    // Constructor simpel untuk menampilkan di tabel
    public Siswa(int id, String nis, String nama) {
        this.studentId = new SimpleIntegerProperty(id);
        this.studentName = new SimpleStringProperty(nama != null ? nama : "");
        this.studentAddress = new SimpleStringProperty("");
        this.studentGender = new SimpleStringProperty("");
        this.studentParentsPhoneNumber = new SimpleStringProperty("");
        this.studentReligion = new SimpleStringProperty("");
        this.studentBirthDate = null;
        
        this.nis = new SimpleStringProperty(nis != null ? nis : "");
        this.kelasSaatIniNama = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("ACTIVE");
        this.daftarNilai = new ArrayList<>();
    }

    // Primary getters (following schema)
    public int getStudentId() { return studentId.get(); }
    public String getStudentName() { return studentName.get(); }
    public String getStudentAddress() { return studentAddress.get(); }
    public String getStudentGender() { return studentGender.get(); }
    public String getStudentParentsPhoneNumber() { return studentParentsPhoneNumber.get(); }
    public String getStudentReligion() { return studentReligion.get(); }
    public LocalDate getStudentBirthDate() { return studentBirthDate; }

    // Legacy getters for compatibility
    public int getId() { return studentId.get(); }
    public String getNis() { return nis.get(); }
    public String getNama() { return studentName.get(); }
    public String getAlamat() { return studentAddress.get(); }
    public String getJenisKelamin() { return studentGender.get(); }
    public String getAgama() { return studentReligion.get(); }
    public LocalDate getTanggalLahir() { return studentBirthDate; }
    public String getNamaOrangTua() { 
        // In new schema, we only have phone number, not name
        return ""; // Return empty for compatibility
    }
    public String getTeleponOrangTua() { return studentParentsPhoneNumber.get(); }
    public String getKelasSaatIniNama() { return kelasSaatIniNama.get(); }
    public String getStatus() { return status.get(); }

    // Primary setters
    public void setStudentName(String studentName) { this.studentName.set(studentName); }
    public void setStudentAddress(String studentAddress) { this.studentAddress.set(studentAddress); }
    public void setStudentGender(String studentGender) { this.studentGender.set(studentGender); }
    public void setStudentParentsPhoneNumber(String studentParentsPhoneNumber) { this.studentParentsPhoneNumber.set(studentParentsPhoneNumber); }
    public void setStudentReligion(String studentReligion) { this.studentReligion.set(studentReligion); }
    public void setStudentBirthDate(LocalDate studentBirthDate) { this.studentBirthDate = studentBirthDate; }

    // Legacy setters for compatibility
    public void setNis(String value) { nis.set(value); }
    public void setNama(String value) { studentName.set(value); }
    public void setAlamat(String value) { studentAddress.set(value); }
    public void setJenisKelamin(String value) { studentGender.set(value); }
    public void setAgama(String value) { studentReligion.set(value); }
    public void setTanggalLahir(LocalDate value) { this.studentBirthDate = value; }
    public void setNamaOrangTua(String value) { 
        // In new schema, we don't store parent name, only phone
        // This is kept for compatibility but does nothing
    }
    public void setTeleponOrangTua(String value) { studentParentsPhoneNumber.set(value); }
    public void setKelasSaatIniNama(String value) { kelasSaatIniNama.set(value); }
    public void setStatus(String value) { status.set(value); }

    // Primary property getters (for TableView binding)
    public IntegerProperty studentIdProperty() { return studentId; }
    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty studentAddressProperty() { return studentAddress; }
    public StringProperty studentGenderProperty() { return studentGender; }
    public StringProperty studentParentsPhoneNumberProperty() { return studentParentsPhoneNumber; }
    public StringProperty studentReligionProperty() { return studentReligion; }

    // Legacy property getters for compatibility
    public IntegerProperty idProperty() { return studentId; }
    public StringProperty nisProperty() { return nis; }
    public StringProperty namaProperty() { return studentName; }
    public StringProperty kelasSaatIniNamaProperty() { return kelasSaatIniNama; }
    public StringProperty statusProperty() { return status; }

    @Override 
    public String toString() { 
        return getStudentName(); 
    }
    
    // Nilai/Grade management methods
    public List<Nilai> getDaftarNilai() { return daftarNilai; }
    
    public void setDaftarNilai(List<Nilai> daftarNilai) {
        this.daftarNilai.clear();
        this.daftarNilai.addAll(daftarNilai);
    }

    public Nilai getNilaiByMapelAndJenis(int mapelId, String jenisUjian) {
        for (Nilai n : daftarNilai) {
            if (n.getMapelId() == mapelId && n.getJenisUjian().equals(jenisUjian)) {
                return n;
            }
        }
        return null;
    }

    public void addOrUpdateNilai(Nilai nilaiBaru) {
        Nilai nilaiLama = getNilaiByMapelAndJenis(nilaiBaru.getMapelId(), nilaiBaru.getJenisUjian());
        if (nilaiLama != null) {
            nilaiLama.setNilai(nilaiBaru.getNilai());
        } else {
            daftarNilai.add(nilaiBaru);
        }
    }
}
