package com.arkanoid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ArkanoidGame extends Application {

    private GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        gameEngine = new GameEngine();
        root.getChildren().add(gameEngine.getCanvas());

        Scene scene = new Scene(root, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        scene.setOnKeyPressed(gameEngine::handleKeyPress);
        scene.setOnKeyReleased(gameEngine::handleKeyRelease);

        primaryStage.setTitle(GameConstants.GAME_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        gameEngine.start();
    }

    @Override
    public void stop() {
        if (gameEngine != null) {
            gameEngine.disposeAudio();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
