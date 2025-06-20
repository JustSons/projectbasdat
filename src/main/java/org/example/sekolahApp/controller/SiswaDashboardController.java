package org.example.sekolahApp.controller;

import javafx.fxml.FXML;
import jdk.jfr.Label;
import org.example.sekolahApp.model.Jadwal;
import org.example.sekolahApp.model.Nilai;
import org.example.sekolahApp.util.UserSession;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.text.TableView;

public class SiswaDashboardController {
    @FXML
    private Label namaSiswaLabel, nisLabel, kelasLabel;
    @FXML private TableView<Jadwal> jadwalTableView;
    @FXML private TableView<Nilai> nilaiTableView;

    @Override public void initialize(URL url, ResourceBundle rb) {
        int siswaId = UserSession.getInstance().getReferenceId();
        // Load biodata siswa, jadwal kelasnya, dan semua nilainya dari DB.
    }
}