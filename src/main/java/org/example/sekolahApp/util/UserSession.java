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

    // Menggunakan eager initialization atau thread-safe lazy initialization jika diperlukan
    // Untuk kesederhanaan dan menghindari potensi race condition pada getInstance()
    // kita bisa menggunakan pendekatan inner static class atau langsung menginisialisasi.
    // Namun, jika kamu sudah punya 'synchronized' untuk getInstance(), kita bisa modifikasi sedikit.

    // Modified getInstance() to ensure proper initialization
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            // Jika dipanggil tanpa createSession, ini mungkin menunjukkan error
            // atau kebutuhan untuk default state.
            // Untuk memastikan session selalu diinisialisasi melalui login:
            throw new IllegalStateException("UserSession belum diinisialisasi. Pastikan login dilakukan terlebih dahulu.");
        }
        return instance;
    }

    // Modified createSession to be the *only* way to set the instance
    public static synchronized UserSession createSession(int userId, String username, String role, int referenceId, Integer waliKelasId) {
        instance = new UserSession(userId, username, role, referenceId, waliKelasId);
        System.out.println("UserSession created: " + instance.toString()); // Debugging
        return instance;
    }

    public static synchronized void cleanUserSession() {
        // Reset instance to null to clear session
        instance = null;
        System.out.println("UserSession cleaned."); // Debugging
    }

    public boolean isLoggedIn() {
        // Cek apakah instance tidak null (sesi aktif)
        return instance != null && instance.getUserId() != 0;
    }

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

    public Integer getWaliKelasId() { // Getter baru
        return waliKelasId;
    }

    // Metode helper untuk role (dari jawaban sebelumnya)
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isGuru() {
        // Seorang wali kelas juga bisa dianggap guru dalam konteks beberapa fitur guru.
        // Kamu bisa menyesuaikan logika ini:
        // return "guru".equalsIgnoreCase(role) || "wali_kelas".equalsIgnoreCase(role);
        // Atau jika 'guru' murni hanya role guru tanpa tugas wali:
        return "guru".equalsIgnoreCase(role);
    }

    public boolean isWaliKelas() { // Getter baru
        return "wali_kelas".equalsIgnoreCase(role);// Pastikan role dan ID kelasnya ada
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