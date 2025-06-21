/* ================================================================================ */
/* File: AlertHelper.java (KELAS BARU YANG HILANG)                                  */
/* Lokasi: src/main/java/org/example/sekolahApp/util/AlertHelper.java             */
/* ================================================================================ */
package org.example.sekolahApp.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Kelas utilitas untuk menampilkan berbagai jenis dialog Alert dengan mudah.
 */
public class AlertHelper {

    /**
     * Menampilkan dialog informasi, peringatan, atau error.
     * @param alertType Jenis alert (INFORMATION, WARNING, ERROR)
     * @param title Judul window alert
     * @param message Pesan yang ingin ditampilkan
     */
    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Tidak menggunakan header text
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Menampilkan dialog konfirmasi dengan tombol OK dan Cancel.
     * @param title Judul window konfirmasi
     * @param message Pesan pertanyaan konfirmasi
     * @return true jika pengguna menekan OK, false jika menekan Cancel atau menutup dialog.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
