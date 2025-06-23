package org.example.sekolahApp.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Schema Adapter for handling differences between old and new database schemas.
 * 
 * OLD SCHEMA (Indonesian): siswa, kelas, staff, mata_pelajaran, jadwal, nilai, tahun_ajaran
 * NEW SCHEMA (English): STUDENT, CLASS, STAFF, SUBJECT, SCHEDULE, GRADE, SCHOOL_YEAR
 */
public class SchemaAdapter {
    
    private static final Map<String, String> OLD_TO_NEW_TABLES = new HashMap<>();
    private static final Map<String, String> NEW_TO_OLD_TABLES = new HashMap<>();
    
    private static final Map<String, Map<String, String>> OLD_TO_NEW_COLUMNS = new HashMap<>();
    private static final Map<String, Map<String, String>> NEW_TO_OLD_COLUMNS = new HashMap<>();
    
    static {
        initializeTableMappings();
        initializeColumnMappings();
    }
    
    private static void initializeTableMappings() {
        // Old to New table mappings
        OLD_TO_NEW_TABLES.put("siswa", "STUDENT");
        OLD_TO_NEW_TABLES.put("kelas", "CLASS");
        OLD_TO_NEW_TABLES.put("staff", "STAFF");
        OLD_TO_NEW_TABLES.put("mata_pelajaran", "SUBJECT");
        OLD_TO_NEW_TABLES.put("jadwal", "SCHEDULE");
        OLD_TO_NEW_TABLES.put("nilai", "GRADE");
        OLD_TO_NEW_TABLES.put("tahun_ajaran", "SCHOOL_YEAR");
        OLD_TO_NEW_TABLES.put("siswa_kelas", "STUDENT_HISTORY");
        OLD_TO_NEW_TABLES.put("users", "users"); // Keep as is for authentication
        
        // New to Old table mappings (reverse)
        for (Map.Entry<String, String> entry : OLD_TO_NEW_TABLES.entrySet()) {
            NEW_TO_OLD_TABLES.put(entry.getValue(), entry.getKey());
        }
    }
    
    private static void initializeColumnMappings() {
        // STUDENT table column mappings
        Map<String, String> studentColumns = new HashMap<>();
        studentColumns.put("siswa_id", "student_id");
        studentColumns.put("nis", "nis"); // Keep for compatibility (not in new schema)
        studentColumns.put("nama_siswa", "student_name");
        studentColumns.put("alamat", "student_address");
        studentColumns.put("jenis_kelamin", "student_gender");
        studentColumns.put("agama", "student_religion");
        studentColumns.put("tanggal_lahir", "student_birth_date");
        studentColumns.put("telepon_orang_tua", "student_parents_phone_number");
        OLD_TO_NEW_COLUMNS.put("siswa", studentColumns);
        
        // CLASS table column mappings
        Map<String, String> classColumns = new HashMap<>();
        classColumns.put("kelas_id", "class_id");
        classColumns.put("nama_kelas", "class_name");
        classColumns.put("wali_kelas_id", "wali_kelas_id");
        OLD_TO_NEW_COLUMNS.put("kelas", classColumns);
        
        // STAFF table column mappings
        Map<String, String> staffColumns = new HashMap<>();
        staffColumns.put("staff_id", "staff_id");
        staffColumns.put("nama_staff", "staff_name");
        staffColumns.put("nip", "nip"); // Keep for compatibility
        staffColumns.put("jabatan", "jabatan"); // Keep for compatibility
        staffColumns.put("email", "staff_email");
        staffColumns.put("nomor_telepon", "staff_phone_number");
        staffColumns.put("alamat", "staff_address");
        staffColumns.put("tanggal_lahir", "staff_hire_date");
        OLD_TO_NEW_COLUMNS.put("staff", staffColumns);
        
        // SUBJECT table column mappings
        Map<String, String> subjectColumns = new HashMap<>();
        subjectColumns.put("mapel_id", "subject_id");
        subjectColumns.put("kode_mapel", "subject_code");
        subjectColumns.put("nama_mapel", "subject_name");
        OLD_TO_NEW_COLUMNS.put("mata_pelajaran", subjectColumns);
        
        // SCHEDULE table column mappings
        Map<String, String> scheduleColumns = new HashMap<>();
        scheduleColumns.put("jadwal_id", "schedule_id");
        scheduleColumns.put("kelas_id", "class_id");
        scheduleColumns.put("mapel_id", "subject_id");
        scheduleColumns.put("guru_id", "teacher_id");
        scheduleColumns.put("tahun_ajaran_id", "school_year_id");
        scheduleColumns.put("hari", "day");
        scheduleColumns.put("jam_mulai", "time_start");
        scheduleColumns.put("jam_selesai", "time_end");
        scheduleColumns.put("ruangan", "room");
        OLD_TO_NEW_COLUMNS.put("jadwal", scheduleColumns);
        
        // GRADE table column mappings
        Map<String, String> gradeColumns = new HashMap<>();
        gradeColumns.put("nilai_id", "grade_id");
        gradeColumns.put("siswa_kelas_id", "student_subject_id"); // Changed relationship
        gradeColumns.put("jenis_ujian", "assessment_type");
        gradeColumns.put("nilai", "grade");
        OLD_TO_NEW_COLUMNS.put("nilai", gradeColumns);
        
        // SCHOOL_YEAR table column mappings
        Map<String, String> schoolYearColumns = new HashMap<>();
        schoolYearColumns.put("tahun_ajaran_id", "school_year_id");
        schoolYearColumns.put("tahun_ajaran", "school_year");
        schoolYearColumns.put("status", "is_active"); // Convert "aktif"/"tidak_aktif" to boolean
        OLD_TO_NEW_COLUMNS.put("tahun_ajaran", schoolYearColumns);
        
        // Create reverse mappings
        for (Map.Entry<String, Map<String, String>> tableEntry : OLD_TO_NEW_COLUMNS.entrySet()) {
            String tableName = tableEntry.getKey();
            String newTableName = OLD_TO_NEW_TABLES.get(tableName);
            
            Map<String, String> reverseColumns = new HashMap<>();
            for (Map.Entry<String, String> columnEntry : tableEntry.getValue().entrySet()) {
                reverseColumns.put(columnEntry.getValue(), columnEntry.getKey());
            }
            NEW_TO_OLD_COLUMNS.put(newTableName, reverseColumns);
        }
    }
    
    /**
     * Convert table name from old schema to new schema
     */
    public static String getNewTableName(String oldTableName) {
        return OLD_TO_NEW_TABLES.getOrDefault(oldTableName, oldTableName);
    }
    
    /**
     * Convert table name from new schema to old schema
     */
    public static String getOldTableName(String newTableName) {
        return NEW_TO_OLD_TABLES.getOrDefault(newTableName, newTableName);
    }
    
    /**
     * Convert column name from old schema to new schema for specific table
     */
    public static String getNewColumnName(String oldTableName, String oldColumnName) {
        Map<String, String> columnMap = OLD_TO_NEW_COLUMNS.get(oldTableName);
        if (columnMap != null) {
            return columnMap.getOrDefault(oldColumnName, oldColumnName);
        }
        return oldColumnName;
    }
    
    /**
     * Convert column name from new schema to old schema for specific table
     */
    public static String getOldColumnName(String newTableName, String newColumnName) {
        Map<String, String> columnMap = NEW_TO_OLD_COLUMNS.get(newTableName);
        if (columnMap != null) {
            return columnMap.getOrDefault(newColumnName, newColumnName);
        }
        return newColumnName;
    }
    
    /**
     * Adapt SQL query from old schema to new schema
     */
    public static String adaptQueryToNewSchema(String oldQuery) {
        String adaptedQuery = oldQuery;
        
        // Replace table names
        for (Map.Entry<String, String> entry : OLD_TO_NEW_TABLES.entrySet()) {
            adaptedQuery = adaptedQuery.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        
        // Additional specific replacements for column names would go here
        // This is a basic implementation - more sophisticated parsing might be needed
        
        return adaptedQuery;
    }
    
    /**
     * Adapt SQL query from new schema to old schema
     */
    public static String adaptQueryToOldSchema(String newQuery) {
        String adaptedQuery = newQuery;
        
        // Replace table names
        for (Map.Entry<String, String> entry : NEW_TO_OLD_TABLES.entrySet()) {
            adaptedQuery = adaptedQuery.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        
        return adaptedQuery;
    }
    
    /**
     * Get appropriate table name based on current schema
     */
    public static String getTableName(String baseName) {
        String currentSchema = DatabaseConnection.detectSchema();
        
        if ("NEW".equals(currentSchema)) {
            return getNewTableName(baseName);
        } else {
            return baseName; // Use old name as default
        }
    }
} 