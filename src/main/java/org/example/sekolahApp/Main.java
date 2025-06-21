package org.example.sekolahApp;

import javafx.stage.StageStyle;
import org.example.sekolahApp.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager.getInstance().setStage(primaryStage);
        primaryStage.setTitle("Aplikasi Manajemen Sekolah");

        // Memuat FXML dari folder resources
        String fxmlPath = "/org/example/sekolahApp/view/Login.fxml";
        SceneManager.getInstance().loadScene(fxmlPath); // Pastikan ukuran ini relevan
        primaryStage.setMaximized(true);
        primaryStage.initStyle(StageStyle.UTILITY);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Hapus baris ini karena DatabaseConnection.closeConnection() sudah tidak ada
        // DatabaseConnection.closeConnection(); // Ini yang menyebabkan error "Cannot resolve method"

        // Pesan ini masih relevan karena koneksi individual akan ditutup oleh try-with-resources.
        // Jika tidak ada Connection Pool, tidak ada "pool" yang perlu ditutup secara eksplisit di sini.
        System.out.println("Aplikasi ditutup."); // Pesan lebih tepat
        super.stop();
    }

    public static void main(String[] args) {
        // Hapus baris ini: DatabaseConnection.getConnection();
        // Alasan:
        // 1. Sekarang getConnection() melempar SQLException, dan main method tidak bisa mendeklarasikan throws SQLException.
        // 2. Dengan pola yang baru, tidak ada gunanya membuat koneksi di sini lalu langsung membuangnya.
        //    Koneksi akan dibuat sesuai kebutuhan di dalam controller menggunakan try-with-resources.
        // Pesan "Memastikan koneksi database dibuat saat aplikasi pertama kali berjalan"
        // sudah tidak relevan dengan implementasi getConnection() yang baru (membuat koneksi baru setiap kali).

        launch(args);
    }
}