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


    // Constructor following SCHOOL_YEAR schema
    public TahunAjaran(int schoolYearId, String schoolYear, LocalDate startDate, LocalDate endDate, boolean isActive) {
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.schoolYear = new SimpleStringProperty(schoolYear != null ? schoolYear : "");
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Constructor with legacy fields for backward compatibility
    public TahunAjaran(int tahunAjaranId, String tahunAjaran) {
        this.schoolYearId = new SimpleIntegerProperty(tahunAjaranId);
        this.schoolYear = new SimpleStringProperty(tahunAjaran != null ? tahunAjaran : "");
        this.startDate = null;
        this.endDate = null;
    }

    // Primary getters (following schema)
    public int getSchoolYearId() { return schoolYearId.get(); }
    public String getSchoolYear() { return schoolYear.get(); }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    // Legacy getters for compatibility
    public int getTahunAjaranId() { return schoolYearId.get(); } // Alias
    public String getTahunAjaran() { return schoolYear.get(); } // Alias

    // Primary setters
    public void setSchoolYear(String schoolYear) { this.schoolYear.set(schoolYear); }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }


    // Legacy setters for compatibility
    public void setTahunAjaran(String tahunAjaran) { this.schoolYear.set(tahunAjaran); }

    // Primary property getters (for TableView binding)
    public IntegerProperty schoolYearIdProperty() { return schoolYearId; }
    public StringProperty schoolYearProperty() { return schoolYear; }

    // Legacy property getters for compatibility
    public IntegerProperty tahunAjaranIdProperty() { return schoolYearId; } // Alias
    public StringProperty tahunAjaranProperty() { return schoolYear; } // Alias

    @Override
    public String toString() {
        return getSchoolYear(); // For ComboBox display
    }
}