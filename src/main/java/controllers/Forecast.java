package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.LocationManager;
import point.PointData;
import weather.Period;
import utils.IconLoader;

// Real-time date
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import java.util.ArrayList;

import static utils.Parser.*;

// Controller for forecast scene
public class Forecast {
    @FXML private VBox root;

    // Handle back button
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load the dashboard scene to go back
            Parent dashboard = FXMLLoader.load(getClass().getResource("/FXML/dashboard.fxml"));
            Stage stage = (Stage) root.getScene().getWindow();
            // Swap current scene
            stage.getScene().setRoot(dashboard);

            // Shrink/grow to fit the new FXML
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Day 1 variables
    @FXML private Label day1Label;
    @FXML private Label day1Date;
    @FXML private ImageView day1Icon;
    @FXML private Label day1Condition;
    @FXML private Label day1HighTemp;
    @FXML private Label day1LowTemp;
    @FXML private Label day1Wind;
    @FXML private Label day1Precip;
    @FXML private ProgressBar day1PrecipBar;
    @FXML private VBox day1TopBox;

    // Day 2 variables
    @FXML private Label day2Label;
    @FXML private Label day2Date;
    @FXML private ImageView day2Icon;
    @FXML private Label day2Condition;
    @FXML private Label day2HighTemp;
    @FXML private Label day2LowTemp;
    @FXML private Label day2Wind;
    @FXML private Label day2Precip;
    @FXML private ProgressBar day2PrecipBar;
    @FXML private VBox day2TopBox;

    // Day 3 variables
    @FXML private Label day3Label;
    @FXML private Label day3Date;
    @FXML private ImageView day3Icon;
    @FXML private Label day3Condition;
    @FXML private Label day3HighTemp;
    @FXML private Label day3LowTemp;
    @FXML private Label day3Wind;
    @FXML private Label day3Precip;
    @FXML private ProgressBar day3PrecipBar;
    @FXML private VBox day3TopBox;

    // Status bar
    @FXML private Label statusBarLabel;

    // Current city
    @FXML private Label locationLabel;

    // Load the data for each card
    @FXML
    public void initialize() {
        // Run the API call in a background thread
        new Thread(() -> {
            try {
                String city = LocationManager.getInstance().getLocationNames().get(0);
                PointData location = LocationManager.getInstance().getLocationFromName(city);

                //Fetch the forecast list from the NWS API
                api.MyWeatherAPI weatherApi = new api.MyWeatherAPI();
                ArrayList<Period> forecastData = weatherApi.getForecastFromURL(location.forecast);

                Platform.runLater(() -> {
                    // Skip to the first day time period as sometimes the API starts with "Tonight"
                    int startIndex = 0;
                    while (startIndex < forecastData.size() &&
                            (forecastData.get(startIndex).name.contains("Night") ||
                                    forecastData.get(startIndex).name.contains("Tonight") ||
                                    forecastData.get(startIndex).name.contains("Overnight"))) {
                        startIndex++;
                    }

                    // Day and night pairs for 3 days
                    Period day1 = forecastData.get(startIndex);
                    Period night1 = forecastData.get(startIndex + 1);
                    Period day2 = forecastData.get(startIndex + 2);
                    Period night2 = forecastData.get(startIndex + 3);
                    Period day3 = forecastData.get(startIndex + 4);
                    Period night3 = forecastData.get(startIndex + 5);

                    // Set up the Dates
                    LocalDate today = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d");
                    day1Date.setText(today.format(formatter));
                    day2Date.setText(today.plusDays(1).format(formatter));
                    day3Date.setText(today.plusDays(2).format(formatter));

                    // Card 1
                    // Get the first word of the day
                    String firstName = day1.name.split(" ")[0];
                    if (firstName.equals("This")) {
                        day1Label.setText("Today");
                    } else {
                        day1Label.setText(firstName);
                    }
                    day1Condition.setText(day1.shortForecast);
                    day1Icon.setImage(IconLoader.getConditionIcon(extractCondition(day1.shortForecast)));
                    day1HighTemp.setText(day1.temperature + "°");
                    day1LowTemp.setText("/ " + night1.temperature + "°F");
                    day1Wind.setText(day1.windSpeed + " " + day1.windDirection);
                    // Set the background color based on the weather
                    day1TopBox.setStyle("-fx-background-color: " + getBoxColor(day1.shortForecast) + "; -fx-background-radius: 12 12 0 0; -fx-padding: 20;");

                    day1Precip.setText(formatPrecipitation(day1.probabilityOfPrecipitation));
                    day1PrecipBar.setProgress(parsePrecip(day1Precip.getText()));

                    // Card 2
                    day2Label.setText(day2.name.split(" ")[0]);
                    day2Condition.setText(day2.shortForecast);
                    day2Icon.setImage(IconLoader.getConditionIcon(extractCondition(day2.shortForecast)));
                    day2HighTemp.setText(day2.temperature + "°");
                    day2LowTemp.setText("/ " + night2.temperature + "°F");
                    day2Wind.setText(day2.windSpeed + " " + day2.windDirection);
                    day2TopBox.setStyle("-fx-background-color: " + getBoxColor(day2.shortForecast) + "; -fx-background-radius: 12 12 0 0; -fx-padding: 20;");
                    day2Precip.setText(formatPrecipitation(day2.probabilityOfPrecipitation));
                    day2PrecipBar.setProgress(parsePrecip(day2Precip.getText()));

                    // Card 3
                    day3Label.setText(day3.name.split(" ")[0]);
                    day3Condition.setText(day3.shortForecast);
                    day3Icon.setImage(IconLoader.getConditionIcon(extractCondition(day3.shortForecast)));
                    day3HighTemp.setText(day3.temperature + "°");
                    day3LowTemp.setText("/ " + night3.temperature + "°F");
                    day3Wind.setText(day3.windSpeed + " " + day3.windDirection);
                    day3TopBox.setStyle("-fx-background-color: " + getBoxColor(day3.shortForecast) + "; -fx-background-radius: 12 12 0 0; -fx-padding: 20;");
                    day3Precip.setText(formatPrecipitation(day3.probabilityOfPrecipitation));
                    day3PrecipBar.setProgress(parsePrecip(day3Precip.getText()));

                    // NWS API update
                    String lastUpdated = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a").format(new Date());
                    statusBarLabel.setText("NWS API — Last updated: " + lastUpdated + "  ·  " + city);

                    locationLabel.setText("Location: " + city);
                });
            } catch (Exception e) {
                System.out.println("Could not load weather: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // Set background color depending on the weather
    private String getBoxColor(String weatherStr) {
        String w = weatherStr.toLowerCase();
        if (w.contains("rain") || w.contains("storm") || w.contains("snow") || w.contains("shower")) {
            return "#1565C0"; // Dark blue for wet/stormy weather
        } else if (w.contains("cloud") || w.contains("overcast")) {
            return "#2986CC"; // Light blue for cloudy weather
        } else {
            return "#E67E22"; // Orange for sunny/clear weather
        }
    }

    // Helper function to convert a string to a double for the progress bar
    private double parsePrecip(String precip) {
        try {
            return Integer.parseInt(precip.replace("%", "")) / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}


