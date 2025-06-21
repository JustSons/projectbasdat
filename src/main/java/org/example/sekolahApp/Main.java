package org.example.sekolahApp;

import org.example.sekolahApp.db.DatabaseConnection;
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
        SceneManager.getInstance().loadScene(fxmlPath, 600, 400);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DatabaseConnection.closeConnection();
        System.out.println("Aplikasi ditutup, koneksi database terputus.");
        super.stop();
    }

    public static void main(String[] args) {
        // Memastikan koneksi database dibuat saat aplikasi pertama kali berjalan
        DatabaseConnection.getConnection();
        launch(args);
    }
}