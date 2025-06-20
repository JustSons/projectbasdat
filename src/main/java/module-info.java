module org.example.sekolahApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.jfr;


    opens org.example.sekolahApp to javafx.fxml;
    exports org.example.sekolahApp;
    exports org.example.sekolahApp.controller;
    opens org.example.sekolahApp.controller to javafx.fxml;
}