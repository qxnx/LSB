module com.example.lsbwjfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.lsbwjfx to javafx.fxml;
    exports com.example.lsbwjfx;
}