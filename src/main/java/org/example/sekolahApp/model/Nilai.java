package org.example.sekolahApp.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Nilai {
    // Primary fields following GRADE table schema
    private final IntegerProperty gradeId;
    private final IntegerProperty studentSubjectId; // Foreign key to STUDENT_SUBJECT
    private final DoubleProperty grade; // DECIMAL(5,2) for grades like 85.50
    private final StringProperty assessmentType; // 'TUGAS', 'UJIAN', 'UTS', 'UAS', 'QUIZ', 'PRAKTIKUM'
    private LocalDate assessmentDate;
    private final StringProperty description;
    private final DoubleProperty weight; // Weight for grade calculation (e.g., 0.20 for 20%)
    
    // Legacy fields for backward compatibility
    private final IntegerProperty siswaKelasId; // For legacy compatibility
    private final IntegerProperty mapelId; // For legacy compatibility

    // Constructor following GRADE schema
    public Nilai(int gradeId, int studentSubjectId, double grade, String assessmentType, 
                LocalDate assessmentDate, String description, double weight) {
        this.gradeId = new SimpleIntegerProperty(gradeId);
        this.studentSubjectId = new SimpleIntegerProperty(studentSubjectId);
        this.grade = new SimpleDoubleProperty(grade);
        this.assessmentType = new SimpleStringProperty(assessmentType != null ? assessmentType : "");
        this.assessmentDate = assessmentDate;
        this.description = new SimpleStringProperty(description != null ? description : "");
        this.weight = new SimpleDoubleProperty(weight);
        
        // Legacy fields (set to 0 as they're not used in new schema)
        this.siswaKelasId = new SimpleIntegerProperty(0);
        this.mapelId = new SimpleIntegerProperty(0);
    }

    // Legacy constructor for backward compatibility
    public Nilai(int nilaiId, int siswaKelasId, int mapelId, String jenisUjian, int nilai) {
        this.gradeId = new SimpleIntegerProperty(nilaiId);
        this.studentSubjectId = new SimpleIntegerProperty(0); // Default value
        this.grade = new SimpleDoubleProperty(nilai); // Convert int to double
        this.assessmentType = new SimpleStringProperty(jenisUjian != null ? jenisUjian : "");
        this.assessmentDate = null;
        this.description = new SimpleStringProperty("");
        this.weight = new SimpleDoubleProperty(1.0); // Default weight
        
        // Legacy fields
        this.siswaKelasId = new SimpleIntegerProperty(siswaKelasId);
        this.mapelId = new SimpleIntegerProperty(mapelId);
    }

    // Primary getters (following schema)
    public int getGradeId() { return gradeId.get(); }
    public int getStudentSubjectId() { return studentSubjectId.get(); }
    public double getGrade() { return grade.get(); }
    public String getAssessmentType() { return assessmentType.get(); }
    public LocalDate getAssessmentDate() { return assessmentDate; }
    public String getDescription() { return description.get(); }
    public double getWeight() { return weight.get(); }

    // Legacy getters for compatibility
    public int getNilaiId() { return gradeId.get(); } // Alias
    public int getSiswaKelasId() { return siswaKelasId.get(); }
    public int getMapelId() { return mapelId.get(); }
    public String getJenisUjian() { return assessmentType.get(); } // Alias
    public int getNilai() { return (int) grade.get(); } // Convert double to int for legacy

    // Primary setters
    public void setStudentSubjectId(int studentSubjectId) { this.studentSubjectId.set(studentSubjectId); }
    public void setGrade(double grade) { this.grade.set(grade); }
    public void setAssessmentType(String assessmentType) { this.assessmentType.set(assessmentType); }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
    public void setDescription(String description) { this.description.set(description); }
    public void setWeight(double weight) { this.weight.set(weight); }

    // Legacy setters for compatibility
    public void setSiswaKelasId(int siswaKelasId) { this.siswaKelasId.set(siswaKelasId); }
    public void setMapelId(int mapelId) { this.mapelId.set(mapelId); }
    public void setJenisUjian(String jenisUjian) { this.assessmentType.set(jenisUjian); }
    public void setNilai(int nilai) { this.grade.set(nilai); } // Convert int to double

    // Primary property getters (for TableView binding)
    public IntegerProperty gradeIdProperty() { return gradeId; }
    public IntegerProperty studentSubjectIdProperty() { return studentSubjectId; }
    public DoubleProperty gradeProperty() { return grade; }
    public StringProperty assessmentTypeProperty() { return assessmentType; }
    public StringProperty descriptionProperty() { return description; }
    public DoubleProperty weightProperty() { return weight; }

    // Legacy property getters for compatibility
    public IntegerProperty nilaiIdProperty() { return gradeId; } // Alias
    public IntegerProperty siswaKelasIdProperty() { return siswaKelasId; }
    public IntegerProperty mapelIdProperty() { return mapelId; }

    @Override
    public String toString() {
        return getAssessmentType() + ": " + getGrade();
    }
}