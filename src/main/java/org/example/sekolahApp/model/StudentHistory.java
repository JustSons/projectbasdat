package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class StudentHistory {
    // Fields following STUDENT_HISTORY table schema
    private final IntegerProperty studentHistoryId;
    private final IntegerProperty studentId;
    private final IntegerProperty classId;
    private final IntegerProperty schoolYearId;
    private LocalDate enrollmentDate;
    private final StringProperty status; // 'ACTIVE', 'GRADUATED', 'TRANSFERRED', 'DROPPED'
    
    // Additional fields for display purposes
    private final StringProperty studentName;
    private final StringProperty className;
    private final StringProperty schoolYear;

    // Primary constructor following STUDENT_HISTORY schema
    public StudentHistory(int studentHistoryId, int studentId, int classId, int schoolYearId, 
                         LocalDate enrollmentDate, String status) {
        this.studentHistoryId = new SimpleIntegerProperty(studentHistoryId);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.classId = new SimpleIntegerProperty(classId);
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.enrollmentDate = enrollmentDate;
        this.status = new SimpleStringProperty(status != null ? status : "ACTIVE");
        
        // Initialize display fields
        this.studentName = new SimpleStringProperty("");
        this.className = new SimpleStringProperty("");
        this.schoolYear = new SimpleStringProperty("");
    }

    // Constructor with display fields
    public StudentHistory(int studentHistoryId, int studentId, int classId, int schoolYearId, 
                         LocalDate enrollmentDate, String status, String studentName, 
                         String className, String schoolYear) {
        this.studentHistoryId = new SimpleIntegerProperty(studentHistoryId);
        this.studentId = new SimpleIntegerProperty(studentId);
        this.classId = new SimpleIntegerProperty(classId);
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.enrollmentDate = enrollmentDate;
        this.status = new SimpleStringProperty(status != null ? status : "ACTIVE");
        
        // Set display fields
        this.studentName = new SimpleStringProperty(studentName != null ? studentName : "");
        this.className = new SimpleStringProperty(className != null ? className : "");
        this.schoolYear = new SimpleStringProperty(schoolYear != null ? schoolYear : "");
    }

    // Getters
    public int getStudentHistoryId() { return studentHistoryId.get(); }
    public int getStudentId() { return studentId.get(); }
    public int getClassId() { return classId.get(); }
    public int getSchoolYearId() { return schoolYearId.get(); }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public String getStatus() { return status.get(); }
    
    // Display getters
    public String getStudentName() { return studentName.get(); }
    public String getClassName() { return className.get(); }
    public String getSchoolYear() { return schoolYear.get(); }

    // Setters
    public void setStudentId(int studentId) { this.studentId.set(studentId); }
    public void setClassId(int classId) { this.classId.set(classId); }
    public void setSchoolYearId(int schoolYearId) { this.schoolYearId.set(schoolYearId); }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public void setStatus(String status) { this.status.set(status); }
    
    // Display setters
    public void setStudentName(String studentName) { this.studentName.set(studentName); }
    public void setClassName(String className) { this.className.set(className); }
    public void setSchoolYear(String schoolYear) { this.schoolYear.set(schoolYear); }

    // Property getters (for TableView binding)
    public IntegerProperty studentHistoryIdProperty() { return studentHistoryId; }
    public IntegerProperty studentIdProperty() { return studentId; }
    public IntegerProperty classIdProperty() { return classId; }
    public IntegerProperty schoolYearIdProperty() { return schoolYearId; }
    public StringProperty statusProperty() { return status; }
    
    // Display property getters
    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty classNameProperty() { return className; }
    public StringProperty schoolYearProperty() { return schoolYear; }

    @Override
    public String toString() {
        return getStudentName() + " - " + getClassName() + " (" + getSchoolYear() + ")";
    }
} 