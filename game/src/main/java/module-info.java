module com.arkanoid.arkanoidgame {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens game.arkanoid to javafx.fxml;
    exports game;
    opens game to javafx.fxml;
}
