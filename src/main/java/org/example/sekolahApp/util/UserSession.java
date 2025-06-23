package org.example.sekolahApp.util;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String username;
    private String role;
    private int referenceId; // staff_id atau siswa_id
    private Integer waliKelasId; // ID kelas yang diwali oleh guru ini (bisa null)

    // Private constructor untuk Singleton
    private UserSession(int userId, String username, String role, int referenceId, Integer waliKelasId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.referenceId = referenceId;
        this.waliKelasId = waliKelasId;
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserSession belum diinisialisasi. Pastikan login dilakukan terlebih dahulu.");
        }
        return instance;
    }

    public static synchronized UserSession createSession(int userId, String username, String role, int referenceId, Integer waliKelasId) {
        instance = new UserSession(userId, username, role, referenceId, waliKelasId);
        System.out.println("UserSession created: " + instance.toString()); // Debugging
        return instance;
    }

    public static synchronized void cleanUserSession() {
        instance = null;
        System.out.println("UserSession cleaned."); // Debugging
    }

    public boolean isLoggedIn() {
        return instance != null && instance.getUserId() != 0;
    }

    // --- METODE YANG HILANG DITAMBAHKAN DI SINI ---
    public String getDashboardFxml() {
        if (role == null) {
            return "/org/example/sekolahApp/view/Login.fxml"; // Fallback
        }
        switch (role) {
            case "admin":
            case "guru":
            case "wali_kelas":
                return "/org/example/sekolahApp/view/admin_dashboard.fxml";
            case "siswa":
                return "/org/example/sekolahApp/view/SiswaDashboard.fxml";
            default:
                return "/org/example/sekolahApp/view/Login.fxml";
        }
    }
    // ---------------------------------------------


    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public Integer getWaliKelasId() {
        return waliKelasId;
    }

    // Metode helper untuk role
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isGuru() {
        // Wali kelas juga seorang guru
        return "guru".equalsIgnoreCase(role) || "wali_kelas".equalsIgnoreCase(role);
    }

    public boolean isWaliKelas() {
        return "wali_kelas".equalsIgnoreCase(role);
    }

    public boolean isSiswa() {
        return "siswa".equalsIgnoreCase(role);
    }



    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", referenceId=" + referenceId +
                ", waliKelasId=" + waliKelasId +
                '}';
    }
}