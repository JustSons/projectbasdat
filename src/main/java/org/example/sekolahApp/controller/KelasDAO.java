package org.example.sekolahApp.controller;

import org.example.sekolahApp.model.Kelas;
import org.example.sekolahApp.model.TahunAjaran;
import org.example.sekolahApp.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KelasDAO {
    public List<Kelas> getByTahunAjaranId(int tahunAjaranId) throws SQLException {
        List<Kelas> list = new ArrayList<>();
        // Query ini menggabungkan tabel kelas dan tahun_ajaran untuk mendapatkan objek TahunAjaran lengkap
        String sql = "SELECT k.id, k.nama_kelas, t.id as ta_id, t.tahun_ajaran, t.status FROM kelas k " +
                "JOIN tahun_ajaran t ON k.tahun_ajaran_id = t.id WHERE k.tahun_ajaran_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tahunAjaranId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Buat objek TahunAjaran dari hasil join
                TahunAjaran ta = new TahunAjaran(
                        rs.getInt("ta_id"),
                        rs.getString("tahun_ajaran"),
                        rs.getString("status")
                );

                // Buat objek Kelas dan masukkan objek TahunAjaran ke dalamnya
                list.add(new Kelas(
                        rs.getInt("id"),
                        rs.getString("nama_kelas"),
                        ta,
                        null // Objek WaliKelas (Staff) bisa di-load terpisah jika dibutuhkan
                ));
            }
        }
        return list;
    }
}