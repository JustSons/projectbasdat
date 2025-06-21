package org.example.sekolahApp.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;
    private SceneManager() {}
    public static synchronized SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }
    public void setStage(Stage stage) { this.stage = stage; }
    public void loadScene(String fxmlPath) throws IOException {
        URL url = getClass().getResource(fxmlPath);
        if (url == null) throw new IOException("Cannot find FXML: " + fxmlPath);
        Parent root = FXMLLoader.load(url);
        if (stage.getScene() == null) {
            stage.setScene(new Scene(root));
        } else {
            stage.getScene().setRoot(root);
        }
    }
}