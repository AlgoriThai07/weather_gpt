package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SwitchScene {
    private static Scene currentScene;
    private static Stage primaryStage;
    private static final ShowError error = new ShowError(null);

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
            double width = primaryStage.getWidth();
            double height = primaryStage.getHeight();
            FXMLLoader loader = new FXMLLoader(SwitchScene.class.getResource("/FXML/" + fxmlName + ".fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root, width, height);
            currentScene = newScene;
            primaryStage.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
            error.showError("Failed to load scene: " + fxmlName);
        }
    }
}
