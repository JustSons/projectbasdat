//package org.example.sekolahApp.controller;
//
//import org.example.sekolahApp.model.Siswa;
//import org.example.sekolahApp.model.SiswaKelas;
//import org.example.sekolahApp.db.DatabaseConnection;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SiswaKelasDAO {
//    // Metode ini mengambil semua siswa yang terdaftar di sebuah kelas
//    public List<SiswaKelas> getSiswaInKelas(int kelasId) throws SQLException {
//        List<SiswaKelas> list = new ArrayList<>();
//        // Query ini menggabungkan siswa_kelas dengan siswa untuk mendapatkan detail nama dan nis
//        String sql = "SELECT sk.id as sk_id, s.id as s_id, s.nis, s.nama " +
//                "FROM siswa_kelas sk JOIN siswa s ON sk.siswa_id = s.id " +
//                "WHERE sk.kelas_id = ?";
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setInt(1, kelasId);
//            ResultSet rs = pstmt.executeQuery();
//
//            while(rs.next()) {
//                // Buat objek Siswa
//                Siswa siswa = new Siswa(
//                        rs.getInt("s_id"),
//                        rs.getString("nis"),
//                        rs.getString("nama")
//                );
//
//                // Buat objek SiswaKelas yang menampung id pendaftaran dan objek Siswa
//                list.add(new SiswaKelas(
//                        rs.getInt("sk_id"),
//                        siswa,
//                        null // Objek Kelas bisa di-pass dari controller jika perlu, di sini tidak
//                ));
//            }
//        }
//        return list;
//    }
//}