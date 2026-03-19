package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import point.PointData;
import weather.Period;
import utils.LocationManager;
import java.util.ArrayList;

// This controller handles the "Manage Locations" screen where users can add or remove cities.
public class ManageLocations {

    @FXML private VBox root;
    @FXML private ListView<String> locationsListView;
    @FXML private TextField nameInput;
    @FXML private TextField latInput;
    @FXML private TextField lonInput;
    @FXML private Label statusLabel;
    @FXML private Label locationCountLabel;

    @FXML
    // handle back button
    private void handleBack(ActionEvent event) {
        try {
            // Load main screen
            Parent dashboard = FXMLLoader.load(getClass().getResource("/FXML/dashboard.fxml"));
            Stage stage = (Stage) root.getScene().getWindow();
            stage.getScene().setRoot(dashboard);
            stage.sizeToScene();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Load the saved cities into the list and update count label
        locationsListView.setItems(LocationManager.getInstance().getLocationNames());
        updateCountLabel();

        // Set up the rows
        locationsListView.setCellFactory(listView -> new LocationCell());
    }

    // Helper method to update the number of saved locations
    private void updateCountLabel() {
        int count = LocationManager.getInstance().getLocationNames().size();
        locationCountLabel.setText(count + " location" + (count == 1 ? "" : "s") + " saved");
    }

    // Handle add location button
    @FXML
    private void handleAddLocation(ActionEvent event) {
        // Read and remove unnecessary character from input
        String latStr = latInput.getText().trim();
        String lonStr = lonInput.getText().trim();

        // Stop if the input is empty
        if (latStr.isEmpty() || lonStr.isEmpty()) {
            setStatus("Please enter both Latitude and Longitude.", "red");
            return;
        }

        try {
            // Convert to double
            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);
            setStatus("Contacting NWS API...", "blue");

            new Thread(() -> {
                // Call the API
                api.MyWeatherAPI weatherApi = new api.MyWeatherAPI();
                PointData newLocationData = weatherApi.getPointData(lat, lon);
                Platform.runLater(() -> {
                    // Check if the API finds a US location
                    if (newLocationData != null && newLocationData.relativeLocation != null) {
                        String newName = newLocationData.relativeLocation.properties.city + ", " + newLocationData.relativeLocation.properties.state;

                        // Use the user typed input
                        if (!nameInput.getText().trim().isEmpty()) {
                            newName = nameInput.getText().trim();
                        }

                        // Handle the same city
                        if (LocationManager.getInstance().getLocationNames().contains(newName)) {
                            setStatus("This city is already in your list!", "red");
                            return;
                        }

                        // Save new location
                        LocationManager.getInstance().addLocation(newLocationData);
                        setStatus("Location added successfully!", "green");

                        // Clear text boxes for the first time
                        nameInput.clear();
                        latInput.clear();
                        lonInput.clear();

                        // Add new city to the list
                        updateCountLabel();
                        locationsListView.setItems(null);
                        locationsListView.setItems(LocationManager.getInstance().getLocationNames());
                        locationsListView.refresh();
                    } else {
                        setStatus("Invalid coordinates or outside the US", "red");
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            // Handle when user types letters
            setStatus("Latitude and Longitude must be numbers.", "red");
        }
    }

    // Change status message and color
    private void setStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    // build custom rows with icons and buttons
    private class LocationCell extends ListCell<String> {
        @Override
        // Call this every time it draws a row
        protected void updateItem(String cityName, boolean empty) {
            super.updateItem(cityName, empty);

            // If the row is empty, hide everything
            if (empty || cityName == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            // Get data for this city
            PointData pd = LocationManager.getInstance().getLocationFromName(cityName);

            // Left side: icon, name, coordinates
            HBox nameAndPin = new HBox(8, createIcon("/assets/location-pin.png"), createLabel(cityName, 15, "#1A365D", true));
            nameAndPin.setAlignment(Pos.CENTER_LEFT);
            Label coordsLbl = createLabel(getCoordinatesString(pd), 11, "#999999", false);
            VBox textBox = new VBox(3, nameAndPin, coordsLbl);

            // Blank space
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Right side: temp and delete
            Label tempLbl = createLabel("--°", 16, "#1A365D", true);
            HBox tempBox = new HBox(6, createIcon("/assets/feels-like.png"), tempLbl);
            tempBox.setAlignment(Pos.CENTER);

            // Get weather in background
            fetchRealTemperature(pd, tempLbl);

            Button deleteBtn = createDeleteButton(cityName);

            // Put it all in one row
            HBox row = new HBox(15, textBox, spacer, tempBox, deleteBtn);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-padding: 15 20; -fx-background-color: white; -fx-border-color: #F0F4F8; -fx-border-width: 0 0 2 0;");

            setGraphic(row);
            setText(null);
        }

        // Creates a label with a specific size, color
        private Label createLabel(String text, int size, String color, boolean isBold) {
            Label lbl = new Label(text);
            String weight = isBold ? "bold" : "normal";
            lbl.setStyle(String.format("-fx-font-size: %d; -fx-text-fill: %s; -fx-font-weight: %s;", size, color, weight));
            return lbl;
        }

        // Load a small icon
        private ImageView createIcon(String path) {
            ImageView icon = new ImageView();
            try {
                icon.setImage(new Image(getClass().getResourceAsStream(path)));
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);
            } catch (Exception e) { }
            return icon;
        }

        // Get lat/lon and formats them
        private String getCoordinatesString(PointData pd) {
            if (pd != null && pd.relativeLocation != null && pd.relativeLocation.geometry != null) {
                double lon = pd.relativeLocation.geometry.coordinates.get(0);
                double lat = pd.relativeLocation.geometry.coordinates.get(1);
                return String.format("Lat: %.2f   Lon: %.2f", lat, lon);
            }
            return "Lat: --   Lon: --";
        }

        // call API to get current temp
        private void fetchRealTemperature(PointData pd, Label tempLbl) {
            if (pd != null && pd.forecast != null) {
                new Thread(() -> {
                    try {
                        api.MyWeatherAPI weatherApi = new api.MyWeatherAPI();
                        ArrayList<Period> forecastData = weatherApi.getForecastFromURL(pd.forecast);
                        if (forecastData != null && !forecastData.isEmpty()) {
                            String realTemp = forecastData.get(0).temperature + "°";
                            // Update screen
                            Platform.runLater(() -> tempLbl.setText(realTemp));
                        }
                    } catch (Exception e) {}
                }).start();
            }
        }

        // Handle delete button
        private Button createDeleteButton(String cityName) {
            Button btn = new Button("×");
            btn.setStyle("-fx-background-color: #FDF0F0; -fx-text-fill: #E74C3C; -fx-border-color: #FADBD8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 2 8; -fx-cursor: hand;");

            btn.setOnAction(e -> {
                // Last location cannot be deleted
                if (LocationManager.getInstance().getLocationNames().size() > 1) {
                    LocationManager.getInstance().removeLocation(cityName);
                    updateCountLabel();
                    locationsListView.setItems(LocationManager.getInstance().getLocationNames());
                    setStatus("Removed " + cityName, "green");
                } else {
                    setStatus("At least one location must remain.", "red");
                }
            });
            return btn;
        }
    }
}