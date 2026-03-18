package utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ShowError {
    @FXML
    private static Label statusBarLabel;

    public static void setStatusBarLabel(Label statusBarLabel) {
        ShowError.statusBarLabel = statusBarLabel;
    }
    //    Show error messages in the status bar
    public static void showError(String message) {
        if (statusBarLabel != null) {
            Platform.runLater(() -> statusBarLabel.setText("Error: " + message));
        } else{
            System.out.println("Error: " + message);
        }
    }
}
