package com.arkanoid;

import com.arkanoid.GameEngine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ArkanoidGame extends Application {
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        GameEngine gameEngine = new GameEngine(WINDOW_WIDTH, WINDOW_HEIGHT);
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

    public static void main(String[] args) {
        launch(args);
    }
}
