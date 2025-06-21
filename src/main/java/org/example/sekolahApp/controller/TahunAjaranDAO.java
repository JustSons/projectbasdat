//package org.example.sekolahApp.controller; // <-- PERUBAHAN DI SINI
//
//import org.example.sekolahApp.model.TahunAjaran;
//import org.example.sekolahApp.db.DatabaseConnection; // Pastikan path ini benar
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class TahunAjaranDAO {
//    public List<TahunAjaran> getAllAktif() throws SQLException {
//        List<TahunAjaran> list = new ArrayList<>();
//        String sql = "SELECT id, tahun_ajaran, status FROM tahun_ajaran WHERE status = 'Aktif'";
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql);
//             ResultSet rs = pstmt.executeQuery()) {
//
//            while (rs.next()) {
//                list.add(new TahunAjaran(rs.getInt("id"), rs.getString("tahun_ajaran"), rs.getString("status")));
//            }
//        }
//        return list;
//    }
//}