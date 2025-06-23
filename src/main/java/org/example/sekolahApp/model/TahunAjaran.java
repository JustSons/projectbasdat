package org.example.sekolahApp.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class TahunAjaran {
    // Primary fields following SCHOOL_YEAR table schema
    private final IntegerProperty schoolYearId;
    private final StringProperty schoolYear; // e.g., "2023/2024"
    private LocalDate startDate;
    private LocalDate endDate;
    private final BooleanProperty isActive;
    
    // Legacy fields for compatibility
    private final StringProperty status; // "aktif" or "tidak_aktif" for legacy compatibility

    // Constructor following SCHOOL_YEAR schema
    public TahunAjaran(int schoolYearId, String schoolYear, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.schoolYear = new SimpleStringProperty(schoolYear != null ? schoolYear : "");
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = new SimpleBooleanProperty(isActive);
        
        // Set legacy status based on isActive
        this.status = new SimpleStringProperty(isActive ? "aktif" : "tidak_aktif");
    }

    // Constructor with legacy fields for backward compatibility
    public TahunAjaran(int tahunAjaranId, String tahunAjaran, String status) {
        this.schoolYearId = new SimpleIntegerProperty(tahunAjaranId);
        this.schoolYear = new SimpleStringProperty(tahunAjaran != null ? tahunAjaran : "");
        this.startDate = null;
        this.endDate = null;
        
        // Set isActive based on legacy status
        boolean active = "aktif".equalsIgnoreCase(status);
        this.isActive = new SimpleBooleanProperty(active);
        this.status = new SimpleStringProperty(status != null ? status : "tidak_aktif");
    }

    // Primary getters (following schema)
    public int getSchoolYearId() { return schoolYearId.get(); }
    public String getSchoolYear() { return schoolYear.get(); }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean getIsActive() { return isActive.get(); }

    // Legacy getters for compatibility
    public int getTahunAjaranId() { return schoolYearId.get(); } // Alias
    public String getTahunAjaran() { return schoolYear.get(); } // Alias
    public String getStatus() { return status.get(); }

    // Primary setters
    public void setSchoolYear(String schoolYear) { this.schoolYear.set(schoolYear); }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setIsActive(boolean isActive) { 
        this.isActive.set(isActive);
        // Update legacy status accordingly
        this.status.set(isActive ? "aktif" : "tidak_aktif");
    }

    // Legacy setters for compatibility
    public void setTahunAjaran(String tahunAjaran) { this.schoolYear.set(tahunAjaran); }
    public void setStatus(String status) { 
        this.status.set(status);
        // Update isActive accordingly
        this.isActive.set("aktif".equalsIgnoreCase(status));
    }

    // Primary property getters (for TableView binding)
    public IntegerProperty schoolYearIdProperty() { return schoolYearId; }
    public StringProperty schoolYearProperty() { return schoolYear; }
    public BooleanProperty isActiveProperty() { return isActive; }

    // Legacy property getters for compatibility
    public IntegerProperty tahunAjaranIdProperty() { return schoolYearId; } // Alias
    public StringProperty tahunAjaranProperty() { return schoolYear; } // Alias
    public StringProperty statusProperty() { return status; }

    @Override
    public String toString() {
        return getSchoolYear(); // For ComboBox display
    }
}