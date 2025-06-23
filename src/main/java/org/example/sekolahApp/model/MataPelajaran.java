package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MataPelajaran {
    // Primary fields following SUBJECT table schema
    private final IntegerProperty subjectId;
    private final StringProperty subjectName;
    private final StringProperty subjectCode;
    private final StringProperty description;
    private final IntegerProperty credits;

    // Constructor following SUBJECT schema
    public MataPelajaran(int subjectId, String subjectName, String subjectCode, String description, int credits) {
        this.subjectId = new SimpleIntegerProperty(subjectId);
        this.subjectName = new SimpleStringProperty(subjectName != null ? subjectName : "");
        this.subjectCode = new SimpleStringProperty(subjectCode != null ? subjectCode : "");
        this.description = new SimpleStringProperty(description != null ? description : "");
        this.credits = new SimpleIntegerProperty(credits);
    }

    // Constructor with basic fields for backward compatibility
    public MataPelajaran(int mapelId, String kodeMapel, String namaMapel) {
        this.subjectId = new SimpleIntegerProperty(mapelId);
        this.subjectName = new SimpleStringProperty(namaMapel != null ? namaMapel : "");
        this.subjectCode = new SimpleStringProperty(kodeMapel != null ? kodeMapel : "");
        this.description = new SimpleStringProperty("");
        this.credits = new SimpleIntegerProperty(1); // Default credits
    }

    // Primary getters (following schema)
    public int getSubjectId() { return subjectId.get(); }
    public String getSubjectName() { return subjectName.get(); }
    public String getSubjectCode() { return subjectCode.get(); }
    public String getDescription() { return description.get(); }
    public int getCredits() { return credits.get(); }

    // Legacy getters for compatibility
    public int getMapelId() { return subjectId.get(); } // Alias
    public String getKodeMapel() { return subjectCode.get(); } // Alias
    public String getNamaMapel() { return subjectName.get(); } // Alias

    // Primary setters
    public void setSubjectName(String subjectName) { this.subjectName.set(subjectName); }
    public void setSubjectCode(String subjectCode) { this.subjectCode.set(subjectCode); }
    public void setDescription(String description) { this.description.set(description); }
    public void setCredits(int credits) { this.credits.set(credits); }

    // Legacy setters for compatibility
    public void setKodeMapel(String kodeMapel) { this.subjectCode.set(kodeMapel); }
    public void setNamaMapel(String namaMapel) { this.subjectName.set(namaMapel); }

    // Primary property getters (for TableView binding)
    public IntegerProperty subjectIdProperty() { return subjectId; }
    public StringProperty subjectNameProperty() { return subjectName; }
    public StringProperty subjectCodeProperty() { return subjectCode; }
    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty creditsProperty() { return credits; }

    // Legacy property getters for compatibility
    public IntegerProperty mapelIdProperty() { return subjectId; } // Alias
    public StringProperty kodeMapelProperty() { return subjectCode; } // Alias
    public StringProperty namaMapelProperty() { return subjectName; } // Alias

    @Override
    public String toString() {
        return getSubjectName(); // For ComboBox display
    }
}