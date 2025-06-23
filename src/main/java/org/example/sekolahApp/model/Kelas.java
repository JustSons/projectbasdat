package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Kelas {
    // Primary fields following CLASS table schema
    private final IntegerProperty classId;
    private final StringProperty className;
    private final IntegerProperty waliKelasId; // Foreign key to WALI_KELAS table
    
    // Additional fields for display purposes
    private final StringProperty waliKelasName; // For displaying wali kelas name
    private final StringProperty tahunAjaranNama; // For backward compatibility

    // Constructor following CLASS schema
    public Kelas(int classId, String className, Integer waliKelasId) {
        this.classId = new SimpleIntegerProperty(classId);
        this.className = new SimpleStringProperty(className != null ? className : "");
        this.waliKelasId = new SimpleIntegerProperty(waliKelasId != null ? waliKelasId : 0);
        
        // Additional fields for display
        this.waliKelasName = new SimpleStringProperty("");
        this.tahunAjaranNama = new SimpleStringProperty("");
    }

    // Constructor with additional display fields
    public Kelas(int classId, String className, Integer waliKelasId, String waliKelasName) {
        this.classId = new SimpleIntegerProperty(classId);
        this.className = new SimpleStringProperty(className != null ? className : "");
        this.waliKelasId = new SimpleIntegerProperty(waliKelasId != null ? waliKelasId : 0);
        this.waliKelasName = new SimpleStringProperty(waliKelasName != null ? waliKelasName : "");
        this.tahunAjaranNama = new SimpleStringProperty("");
    }

    // Legacy constructor for backward compatibility
    public Kelas(int kelasId, String namaKelas, int tahunAjaranId, String tahunAjaranNama, Integer waliKelasId, String waliKelasName) {
        this.classId = new SimpleIntegerProperty(kelasId);
        this.className = new SimpleStringProperty(namaKelas != null ? namaKelas : "");
        this.waliKelasId = new SimpleIntegerProperty(waliKelasId != null ? waliKelasId : 0);
        this.waliKelasName = new SimpleStringProperty(waliKelasName != null ? waliKelasName : "");
        this.tahunAjaranNama = new SimpleStringProperty(tahunAjaranNama != null ? tahunAjaranNama : "");
    }

    // Primary getters (following schema)
    public int getClassId() { return classId.get(); }
    public String getClassName() { return className.get(); }
    public int getWaliKelasId() { return waliKelasId.get(); }

    // Additional getters for display
    public String getWaliKelasName() { return waliKelasName.get(); }
    public String getTahunAjaranNama() { return tahunAjaranNama.get(); }

    // Legacy getters for compatibility
    public int getKelasId() { return classId.get(); } // Alias
    public String getNamaKelas() { return className.get(); } // Alias

    // Primary setters
    public void setClassName(String className) { this.className.set(className); }
    public void setWaliKelasId(int waliKelasId) { this.waliKelasId.set(waliKelasId); }

    // Additional setters for display
    public void setWaliKelasName(String waliKelasName) { this.waliKelasName.set(waliKelasName); }
    public void setTahunAjaranNama(String tahunAjaranNama) { this.tahunAjaranNama.set(tahunAjaranNama); }

    // Legacy setters for compatibility
    public void setNamaKelas(String namaKelas) { this.className.set(namaKelas); }

    // Primary property getters (for TableView binding)
    public IntegerProperty classIdProperty() { return classId; }
    public StringProperty classNameProperty() { return className; }
    public IntegerProperty waliKelasIdProperty() { return waliKelasId; }

    // Additional property getters for display
    public StringProperty waliKelasNameProperty() { return waliKelasName; }
    public StringProperty tahunAjaranNamaProperty() { return tahunAjaranNama; }

    // Legacy property getters for compatibility
    public IntegerProperty kelasIdProperty() { return classId; } // Alias
    public StringProperty namaKelasProperty() { return className; } // Alias

    @Override
    public String toString() {
        return getClassName(); // For ComboBox display
    }
}