module org.example.sekolahApp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jdk.jfr;
    requires org.postgresql.jdbc;

    opens org.example.sekolahApp.model to javafx.base; // <--- INI SOLUSINYA!

    // ---------------------------

    opens org.example.sekolahApp to javafx.fxml;
    exports org.example.sekolahApp;
    exports org.example.sekolahApp.controller;
    exports org.example.sekolahApp.model;
    opens org.example.sekolahApp.controller to javafx.fxml;
}