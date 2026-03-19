package controllers;

import api.CachedWeatherApiProxy;
import api.WeatherApiService;
import hourlyWeather.HourlyPeriod;
import hourlyWeather.WeatherTableEntry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import point.PointData;
import utils.IconLoader;
import hourlyWeather.HourlyEntryAdapter;
import utils.LocationManager;
import utils.ShowError;
import weather.Period;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

import static javafx.collections.FXCollections.observableArrayList;
import static utils.Parser.*;
import static utils.SwitchScene.switchScene;

public class Dashboard implements Initializable {

//    Top bar
    @FXML private ImageView appLogo;
    @FXML private ComboBox<String> locationComboBox;

//    Hero Panel
    @FXML private ImageView locationPin;
    @FXML private ImageView conditionIcon;
    @FXML private Label locationLabel;
    @FXML private Label tempLabel;
    @FXML private Label conditionLabel;
    @FXML private Label dateLabel;

//    Condition strip
    @FXML private ImageView feelsLikeIcon;
    @FXML private ImageView windIcon;
    @FXML private ImageView precipitationIcon;
    @FXML private ImageView humidityIcon;
    @FXML private ImageView dewpointIcon;

    @FXML private Label feelsLikeValue;
    @FXML private Label windValue;
    @FXML private Label precipitationValue;
    @FXML private Label humidityValue;
    @FXML private Label dewpointValue;

//    Overview
    @FXML private Label overviewText;

//    24 hour forecast
    @FXML private TableView<WeatherTableEntry> hourlyTable;
    @FXML private TableColumn<WeatherTableEntry, String> colTime;
    @FXML private TableColumn<WeatherTableEntry, String> colIcon;
    @FXML private TableColumn<WeatherTableEntry, Integer> colTemp;
    @FXML private TableColumn<WeatherTableEntry, Integer> colFeelsLike;
    @FXML private TableColumn<WeatherTableEntry, String> colCondition;
    @FXML private TableColumn<WeatherTableEntry, String> colWind;
    @FXML private TableColumn<WeatherTableEntry, String> colPrecip;

//    Status bar
    @FXML private Label statusBarLabel;

//    Internal State
    private ArrayList<HourlyPeriod> hourlyData;
    private PointData currentPointData;
    private ArrayList<Period> forecastData;
    private ShowError error;

//    Proxy
    private final WeatherApiService weatherApi = new CachedWeatherApiProxy(new api.MyWeatherAPI());

//    Initialize
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        error = new ShowError(statusBarLabel);
        loadStaticIcons();
        setupLocationComboBox();
        setupTableColumns();
        loadDefaultLocation();
    }

//    Load the static icons
    private void loadStaticIcons() {
//        Top bar
        setImage(appLogo, IconLoader.getIcon(IconLoader.APP_LOGO));
        setImage(locationPin, IconLoader.getIcon(IconLoader.LOCATION_PIN));

//        Conditions strip
        setImage(feelsLikeIcon, IconLoader.getIcon(IconLoader.FEELS_LIKE_ICON));
        setImage(windIcon, IconLoader.getIcon(IconLoader.WIND_ICON));
        setImage(precipitationIcon, IconLoader.getIcon(IconLoader.PRECIPITATION_ICON));
        setImage(humidityIcon, IconLoader.getIcon(IconLoader.HUMIDITY_ICON));
        setImage(dewpointIcon, IconLoader.getIcon(IconLoader.DEWPOINT_ICON));
    }

//    Setup the combo box
    private void setupLocationComboBox() {
        ObservableList<String> locationNames = LocationManager.getInstance().getLocationNames();
        locationComboBox.setItems(locationNames);

        if (!locationNames.isEmpty()){
            locationComboBox.getSelectionModel().selectFirst();
        }

//        Listen for location changes
        locationComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                onLocationChanged();
            }
        });
    }

//    Bind the columns to the table and setup cells
    private void setupTableColumns() {
//        Bind to the hourlyEntry class
        colTemp.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        colFeelsLike.setCellValueFactory(new PropertyValueFactory<>("feelsLike"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colWind.setCellValueFactory(new PropertyValueFactory<>("wind"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colPrecip.setCellValueFactory(new PropertyValueFactory<>("precip"));
        colIcon.setCellValueFactory(new PropertyValueFactory<>("condition"));

//        Setup the icon to change based on the condition
        colIcon.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            {
                iv.setFitWidth(24);
                iv.setFitHeight(24);
                iv.setPreserveRatio(true);
            }
            @Override
            protected void updateItem(String condition, boolean empty) {
                super.updateItem(condition, empty);
                if (empty || condition == null) {
                    setGraphic(null);
                } else {
                    iv.setImage(IconLoader.getConditionIcon(condition));
                    setGraphic(iv);
                }
                setText(null);
            }
        });

//        Setup a mini process bar for precip
        colPrecip.setCellFactory(col -> new TableCell<>() {
            private final Label valueLabel = new Label();
            private final ImageView icon = new ImageView();
            {
                icon.setFitWidth(16);
                icon.setFitHeight(16);
                icon.setPreserveRatio(true);
                icon.setImage(IconLoader.getIcon(IconLoader.PRECIPITATION_ICON));
            }
            private final HBox labelBox = new HBox(4, valueLabel, icon);
            private final ProgressBar bar = new ProgressBar(0);
            private final VBox box = new VBox(4, labelBox, bar);
            {
                box.getStyleClass().add("precipCellBox");
                labelBox.getStyleClass().add("precipLabelBox");
                bar.getStyleClass().add("precipBar");
                valueLabel.getStyleClass().add("precipValueLabel");
                VBox.setVgrow(box, Priority.ALWAYS);
                bar.setPrefWidth(50);
                bar.setPrefHeight(8);
            }
            @Override
            protected void updateItem(String precip, boolean empty) {
                super.updateItem(precip, empty);
                if (empty || precip == null) {
                    setGraphic(null);
                } else {
                    double pct = parsePrecip(precip);
                    valueLabel.setText(precip);
                    valueLabel.getStyleClass().add("cellPrecipLabel");
                    bar.setProgress(pct);
                    bar.getStyleClass().removeAll("precipBarEx", "precipBarHigh", "precipBarMed", "precipBarLow");
                    if (pct >= 0.8){
                        bar.getStyleClass().add("precipBarEx");
                    } else if (pct >= 0.5){
                        bar.getStyleClass().add("precipBarHigh");
                    } else if (pct >= 0.25){
                        bar.getStyleClass().add("precipBarMed");
                    } else{
                        bar.getStyleClass().add("precipBarLow");
                    }
                    setGraphic(box);
                }
                setText(null);
            }
        });

//        Format and setup temperature column
        colTemp.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer temp, boolean empty) {
                super.updateItem(temp, empty);
                setText(empty || temp == null ? null : temp + "°");
                getStyleClass().add("cellTempLabel");
            }
        });

//        Format and setup feels like column
        colFeelsLike.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer fl, boolean empty) {
                super.updateItem(fl, empty);
                setText(empty || fl == null ? null : fl + "°");
                getStyleClass().add("cellFeelsLikeLabel");
            }
        });

//        Set width proportions for table columns for responsiveness
        double[] columnProportions = {80, 50, 70, 70, 130, 90, 120};
        double totalProportion = Arrays.stream(columnProportions).sum();

        hourlyTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            colTime.setPrefWidth((columnProportions[0] / totalProportion) * tableWidth);
            colIcon.setPrefWidth((columnProportions[1] / totalProportion) * tableWidth);
            colTemp.setPrefWidth((columnProportions[2] / totalProportion) * tableWidth);
            colFeelsLike.setPrefWidth((columnProportions[3] / totalProportion) * tableWidth);
            colCondition.setPrefWidth((columnProportions[4] / totalProportion) * tableWidth);
            colWind.setPrefWidth((columnProportions[5] / totalProportion) * tableWidth);
            colPrecip.setPrefWidth((columnProportions[6] / totalProportion) * tableWidth);
        });

    }

//    Load data for the selected location

    private void loadDefaultLocation() {
        String first = locationComboBox.getSelectionModel().getSelectedItem();
        if (first != null) {
            LocationManager.getInstance().setCurrentLocation(first);
            updateDashboard();
        }
    }
    @FXML
    public void onLocationChanged() {
        String selected = locationComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LocationManager.getInstance().setCurrentLocation(selected);
            updateDashboard();
        }
    }

//    Call apis from MyWeatherApi
//    Update the UI based on the selected location
    private void updateDashboard() {
//        Run api calls on background thread to avoid blocking UI
        new Thread(() -> {
            try {
//                Get the PointData from LocationManager
                PointData pointData = LocationManager.getInstance().getCurrentLocation();
                if (pointData == null) {
                    error.showError("Location data not found");
                    return;
                }

                currentPointData = pointData;

//                Fetch hourly forecast from the stored API route
                ArrayList<HourlyPeriod> newHourlyData = weatherApi.getHourlyForecastFromURL(pointData.forecastHourly);
                if (newHourlyData == null || newHourlyData.isEmpty()) {
                    error.showError("Failed to fetch hourly forecast");
                    return;
                }
                hourlyData = newHourlyData;

//                Fetch forecast from the stored API route
                ArrayList<Period> newForecastData = weatherApi.getForecastFromURL(pointData.forecast);
                if (newForecastData == null || newForecastData.isEmpty()) {
                    error.showError("Failed to fetch forecast");
                    return;
                }
                forecastData = newForecastData;

//                Update UI on FX thread
                Platform.runLater(() -> populateDashboardUI());
            } catch (Exception e) {
                e.printStackTrace();
                error.showError("Error loading weather data: " + e.getMessage());
            }
        }).start();
    }

//    Populate all UI components with fetched data
    private void populateDashboardUI() {
        if (currentPointData == null || hourlyData == null || hourlyData.isEmpty() || forecastData == null || forecastData.isEmpty()) {
            return;
        }

        HourlyPeriod firstHour = hourlyData.get(0);
        Period forecast = forecastData.get(0);

//        Hero Panel
        String city = currentPointData.relativeLocation.properties.city;
        String state = currentPointData.relativeLocation.properties.state;
        locationLabel.setText(city.toUpperCase() + ", " + state);
        tempLabel.setText(String.valueOf(firstHour.temperature));
        conditionLabel.setText(firstHour.shortForecast);
        dateLabel.setText(formatDateTime());
        setImage(conditionIcon, IconLoader.getConditionIcon(extractCondition(firstHour.shortForecast)));

//        Condition Strip
        double humidity = (firstHour.relativeHumidity != null) ? firstHour.relativeHumidity.value : 50.0;
        double windSpeed = parseWindSpeed(firstHour.windSpeed);
        int feelsLikeTemp = (int) Math.round(
                utils.FeelsLikeCalculator.calculateFeelsLike(firstHour.temperature, humidity, windSpeed)
        );

        feelsLikeValue.setText(feelsLikeTemp + "°F");
        windValue.setText(firstHour.windSpeed + " " + firstHour.windDirection);
        precipitationValue.setText(formatPrecipitation(firstHour.probabilityOfPrecipitation));
        humidityValue.setText((int) humidity + "%");
        dewpointValue.setText((int) firstHour.dewpoint.value + "°F");

//        Overview Text
        overviewText.setText(forecast.detailedForecast);

//        24 hour forecast
        ArrayList<WeatherTableEntry> hourlyEntries = new ArrayList<>();
        for (HourlyPeriod hp : hourlyData) {
//            Instantiate the Adapter and add it to the list
            hourlyEntries.add(new HourlyEntryAdapter(hp));
        }
        hourlyTable.setItems(observableArrayList(hourlyEntries));

//        Status bar
        String lastUpdated = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a").format(new Date());
        statusBarLabel.setText("NWS API — Last updated: " + lastUpdated + "  ·  " + city + ", " + state);
    }

//    Navigation handlers for switching to different scenes

    @FXML
    private void handleNavigateForecast() {
        switchScene("Forecast");
    }

    @FXML
    private void handleNavigateAssistant() {
        switchScene("weatherAssistant");
    }

    @FXML
    private void handleNavigateLocations() {
        switchScene("ManageLocations");
    }

//    private helper functions
//    Set imageview null safe
    private void setImage(ImageView view, Image image) {
        if (view != null) view.setImage(image);
    }

//    Return a normalize precipitation (0.0 - 1.0) for the progress bar
    private double parsePrecip(String precip) {
        try {
            return Integer.parseInt(precip.replace("%", "")) / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}