package org.example.sekolahApp.util;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String username;
    private String role;
    private int referenceId; // ID di tabel staff atau siswa

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void createSession(int userId, String username, String role, int referenceId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.referenceId = referenceId;
    }

    public void cleanUserSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
        this.referenceId = 0;
    }

    // Getters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public int getReferenceId() { return referenceId; }
}