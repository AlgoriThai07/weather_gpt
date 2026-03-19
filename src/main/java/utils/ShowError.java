package utils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ShowError {
    private final Label statusBarLabel;

    public ShowError(Label statusBarLabel) {
        this.statusBarLabel = statusBarLabel;
    }
    //    Show error messages in the status bar
    public void showError(String message) {
        if (statusBarLabel != null) {
            Platform.runLater(() -> statusBarLabel.setText("Error: " + message));
        } else{
            System.out.println("Error: " + message);
        }
    }
}
