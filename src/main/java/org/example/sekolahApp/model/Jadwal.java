package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalTime;

public class Jadwal {
    // Primary fields following SCHEDULE table schema
    private final IntegerProperty scheduleId;
    private final IntegerProperty classId;
    private final IntegerProperty subjectId;
    private final IntegerProperty teacherId;
    private final IntegerProperty schoolYearId;
    private final StringProperty day; // ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', ...)
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private final StringProperty room;
    
    // Additional display fields
    private final StringProperty className;
    private final StringProperty subjectName;
    private final StringProperty teacherName;
    private final StringProperty schoolYear;

    // Constructor following SCHEDULE schema
    public Jadwal(int scheduleId, int classId, int subjectId, int teacherId, int schoolYearId, 
                  String day, LocalTime timeStart, LocalTime timeEnd, String room) {
        this.scheduleId = new SimpleIntegerProperty(scheduleId);
        this.classId = new SimpleIntegerProperty(classId);
        this.subjectId = new SimpleIntegerProperty(subjectId);
        this.teacherId = new SimpleIntegerProperty(teacherId);
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.day = new SimpleStringProperty(day != null ? day : "");
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.room = new SimpleStringProperty(room != null ? room : "");
        
        // Initialize display fields
        this.className = new SimpleStringProperty("");
        this.subjectName = new SimpleStringProperty("");
        this.teacherName = new SimpleStringProperty("");
        this.schoolYear = new SimpleStringProperty("");
    }

    // Constructor with display fields
    public Jadwal(int scheduleId, int classId, int subjectId, int teacherId, int schoolYearId, 
                  String day, LocalTime timeStart, LocalTime timeEnd, String room,
                  String className, String subjectName, String teacherName, String schoolYear) {
        this.scheduleId = new SimpleIntegerProperty(scheduleId);
        this.classId = new SimpleIntegerProperty(classId);
        this.subjectId = new SimpleIntegerProperty(subjectId);
        this.teacherId = new SimpleIntegerProperty(teacherId);
        this.schoolYearId = new SimpleIntegerProperty(schoolYearId);
        this.day = new SimpleStringProperty(day != null ? day : "");
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.room = new SimpleStringProperty(room != null ? room : "");
        
        // Set display fields
        this.className = new SimpleStringProperty(className != null ? className : "");
        this.subjectName = new SimpleStringProperty(subjectName != null ? subjectName : "");
        this.teacherName = new SimpleStringProperty(teacherName != null ? teacherName : "");
        this.schoolYear = new SimpleStringProperty(schoolYear != null ? schoolYear : "");
    }

    // Legacy constructor for backward compatibility
    public Jadwal(int jadwalId, String hari, LocalTime jamMulai, LocalTime jamSelesai, String namaKelas, String namaMapel, String namaGuru) {
        this.scheduleId = new SimpleIntegerProperty(jadwalId);
        this.classId = new SimpleIntegerProperty(0); // Default value
        this.subjectId = new SimpleIntegerProperty(0); // Default value
        this.teacherId = new SimpleIntegerProperty(0); // Default value
        this.schoolYearId = new SimpleIntegerProperty(0); // Default value
        this.day = new SimpleStringProperty(hari != null ? hari : "");
        this.timeStart = jamMulai;
        this.timeEnd = jamSelesai;
        this.room = new SimpleStringProperty("");
        
        // Set display fields from legacy data
        this.className = new SimpleStringProperty(namaKelas != null ? namaKelas : "");
        this.subjectName = new SimpleStringProperty(namaMapel != null ? namaMapel : "");
        this.teacherName = new SimpleStringProperty(namaGuru != null ? namaGuru : "");
        this.schoolYear = new SimpleStringProperty("");
    }

    // Primary getters (following schema)
    public int getScheduleId() { return scheduleId.get(); }
    public int getClassId() { return classId.get(); }
    public int getSubjectId() { return subjectId.get(); }
    public int getTeacherId() { return teacherId.get(); }
    public int getSchoolYearId() { return schoolYearId.get(); }
    public String getDay() { return day.get(); }
    public LocalTime getTimeStart() { return timeStart; }
    public LocalTime getTimeEnd() { return timeEnd; }
    public String getRoom() { return room.get(); }

    // Display getters
    public String getClassName() { return className.get(); }
    public String getSubjectName() { return subjectName.get(); }
    public String getTeacherName() { return teacherName.get(); }
    public String getSchoolYear() { return schoolYear.get(); }

    // Legacy getters for compatibility
    public int getJadwalId() { return scheduleId.get(); } // Alias
    public String getHari() { return day.get(); } // Alias
    public LocalTime getJamMulai() { return timeStart; } // Alias
    public LocalTime getJamSelesai() { return timeEnd; } // Alias
    public String getNamaKelas() { return className.get(); } // Alias
    public String getNamaMapel() { return subjectName.get(); } // Alias
    public String getNamaGuru() { return teacherName.get(); } // Alias

    // Primary setters
    public void setClassId(int classId) { this.classId.set(classId); }
    public void setSubjectId(int subjectId) { this.subjectId.set(subjectId); }
    public void setTeacherId(int teacherId) { this.teacherId.set(teacherId); }
    public void setSchoolYearId(int schoolYearId) { this.schoolYearId.set(schoolYearId); }
    public void setDay(String day) { this.day.set(day); }
    public void setTimeStart(LocalTime timeStart) { this.timeStart = timeStart; }
    public void setTimeEnd(LocalTime timeEnd) { this.timeEnd = timeEnd; }
    public void setRoom(String room) { this.room.set(room); }

    // Display setters
    public void setClassName(String className) { this.className.set(className); }
    public void setSubjectName(String subjectName) { this.subjectName.set(subjectName); }
    public void setTeacherName(String teacherName) { this.teacherName.set(teacherName); }
    public void setSchoolYear(String schoolYear) { this.schoolYear.set(schoolYear); }

    // Legacy setters for compatibility
    public void setHari(String hari) { this.day.set(hari); }
    public void setJamMulai(LocalTime jamMulai) { this.timeStart = jamMulai; }
    public void setJamSelesai(LocalTime jamSelesai) { this.timeEnd = jamSelesai; }

    // Primary property getters (for TableView binding)
    public IntegerProperty scheduleIdProperty() { return scheduleId; }
    public IntegerProperty classIdProperty() { return classId; }
    public IntegerProperty subjectIdProperty() { return subjectId; }
    public IntegerProperty teacherIdProperty() { return teacherId; }
    public IntegerProperty schoolYearIdProperty() { return schoolYearId; }
    public StringProperty dayProperty() { return day; }
    public StringProperty roomProperty() { return room; }

    // Display property getters
    public StringProperty classNameProperty() { return className; }
    public StringProperty subjectNameProperty() { return subjectName; }
    public StringProperty teacherNameProperty() { return teacherName; }
    public StringProperty schoolYearProperty() { return schoolYear; }

    // Legacy property getters for compatibility
    public IntegerProperty jadwalIdProperty() { return scheduleId; } // Alias
    public StringProperty hariProperty() { return day; } // Alias

    @Override
    public String toString() {
        return getDay() + " " + getTimeStart() + "-" + getTimeEnd() + " " + getSubjectName();
    }
}