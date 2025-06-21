package org.example.sekolahApp.controller;

import org.example.sekolahApp.model.MataPelajaran;
import org.example.sekolahApp.db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MataPelajaranDAO {
    // Metode ini mengambil semua mata pelajaran.
    // Logika untuk filter mapel per guru bisa ditambahkan di query jika perlu.
    public List<MataPelajaran> getAll() throws SQLException {
        List<MataPelajaran> list = new ArrayList<>();
        String sql = "SELECT id, kode_mapel, nama_mapel FROM mata_pelajaran";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new MataPelajaran(
                        rs.getInt("id"),
                        rs.getString("kode_mapel"),
                        rs.getString("nama_mapel")
                ));
            }
        }
        return list;
    }
}