package com.arkanoid;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ArkanoidGame extends Application {
    private static final int WINDOW_WIDTH = 780;
    private static final int WINDOW_HEIGHT = 600;

    private GameEngine gameEngine;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        gameEngine = new GameEngine(WINDOW_WIDTH, WINDOW_HEIGHT);
        root.getChildren().add(gameEngine.getCanvas());

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.setOnKeyPressed(gameEngine::handleKeyPress);
        scene.setOnKeyReleased(gameEngine::handleKeyRelease);

        primaryStage.setTitle("Arkanoid - OOP Game");
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