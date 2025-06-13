module org.example.limbad {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.limbad to javafx.fxml;
    exports org.example.limbad;
}