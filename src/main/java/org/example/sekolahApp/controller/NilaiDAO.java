package org.example.sekolahApp.controller; // <-- PERUBAHAN DI SINI

import org.example.sekolahApp.model.Nilai;
import org.example.sekolahApp.db.DatabaseConnection; // Pastikan path ini benar
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NilaiDAO {
    public void saveOrUpdateNilai(Nilai nilai) throws SQLException {
        String sql = "INSERT INTO nilai (siswa_kelas_id, mapel_id, jenis_ujian, nilai) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nilai = VALUES(nilai)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nilai.getSiswaKelasId());
            pstmt.setInt(2, nilai.getMapelId());
            pstmt.setString(3, nilai.getJenisUjian());
            pstmt.setInt(4, nilai.getNilai());
            pstmt.executeUpdate();
        }
    }

    public List<Nilai> getNilaiForSiswaKelas(List<Integer> siswaKelasIds, int mapelId) throws SQLException {
        List<Nilai> list = new ArrayList<>();
        if (siswaKelasIds.isEmpty()) return list;

        String placeholders = siswaKelasIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT nilai_id, siswa_kelas_id, mapel_id, jenis_ujian, nilai FROM nilai WHERE mapel_id = ? AND siswa_kelas_id IN (" + placeholders + ")";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, mapelId);
            int index = 2;
            for (Integer id : siswaKelasIds) {
                pstmt.setInt(index++, id);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Nilai n = new Nilai(rs.getInt("siswa_kelas_id"), rs.getInt("mapel_id"), rs.getString("jenis_ujian"), rs.getInt("nilai"));
                n.setNilaiId(rs.getInt("nilai_id"));
                list.add(n);
            }
        }
        return list;
    }
}