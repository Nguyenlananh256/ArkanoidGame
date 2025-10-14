package game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Arkanoid extends Application {
    @Override
    public void start(Stage stage) {
        final int SCREEN_WIDTH = 800;
        final int SCREEN_HEIGHT = 600;

        GameInterface gameInterface = new GameInterface();
        Scene scene = new Scene(gameInterface, SCREEN_WIDTH, SCREEN_HEIGHT);


        stage.setTitle("Arkanoid");
        stage.setScene(scene);
        stage.show();

        GameManager manager = new GameManager();

    }
}
