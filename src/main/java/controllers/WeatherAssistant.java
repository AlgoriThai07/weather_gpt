package controllers;

import javafx.fxml.FXML;

import static utils.SwitchScene.switchScene;

public class WeatherAssistant {
    @FXML
    public void handleBackToDashboard() {
        switchScene("dashboard");
    }
}
