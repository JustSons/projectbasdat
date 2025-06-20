package org.example.sekolahApp;

import org.example.sekolahApp.db.DatabaseConnection;
import org.example.sekolahApp.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        SceneManager.getInstance().setStage(primaryStage);
        primaryStage.setTitle("Aplikasi Manajemen Sekolah");
        SceneManager.getInstance().loadScene("/com/yourcompany/sekolahapp/view/Login.fxml", 800, 600);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        DatabaseConnection.closeConnection();
        super.stop();
    }

    public static void main(String[] args) {
        DatabaseConnection.getConnection();
        launch(args);
    }
}