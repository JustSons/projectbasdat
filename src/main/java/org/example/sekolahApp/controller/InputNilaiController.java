package org.example.sekolahApp.controller;

import org.example.sekolahApp.util.SceneManager;
import javafx.fxml.FXML;
import java.io.IOException;

public class InputNilaiController {
    @FXML
    private void handleBack() {
        try {
            SceneManager.getInstance().loadScene("/org/example/sekolahApp/view/admin_dashboard.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}