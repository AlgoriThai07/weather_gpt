package utils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import static utils.ShowError.showError;

public class SwitchScene {
    private static Scene currentScene;
    private static Stage primaryStage;

    public static void setCurrentScene(Scene scene) {
        currentScene = scene;
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlName) {
        if (primaryStage == null || currentScene == null) {
            System.err.println("Primary stage or current scene is null");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(SwitchScene.class.getResource("/FXML/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root);
            currentScene = newScene;
            primaryStage.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load scene: " + fxmlName);
        }
    }
}
