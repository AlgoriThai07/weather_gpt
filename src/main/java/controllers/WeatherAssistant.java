package controllers;

import api.CachedWeatherApiProxy;
import api.WeatherApiService;
import hourlyWeather.HourlyPeriod;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import point.PointData;
import services.WeatherAssistantService;
import utils.IconLoader;
import utils.LocationManager;
import weather.Period;
import utils.ShowError;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import static utils.ShowError.showError;
import static utils.SwitchScene.switchScene;

public class WeatherAssistant implements Initializable {

//    Top bar
    @FXML private ImageView  appLogo;
    @FXML private ImageView  assistantAvatar;
    @FXML private ImageView  typingAvatar;
    @FXML private Label locationLabel;
//    Chat area
    @FXML private ScrollPane chatScroll;
    @FXML private VBox chatContainer;
    @FXML private HBox typingIndicator;
//    Input bar
    @FXML private TextField  inputField;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
//    Status Bar
    @FXML private Label statusBarLabel;
//    Internal state
    private ArrayList<Period> forecastData = new ArrayList<>();
    private ArrayList<HourlyPeriod> hourlyData = new ArrayList<>();

    private final WeatherApiService weatherApi = new CachedWeatherApiProxy(new api.MyWeatherAPI());
    private final WeatherAssistantService assistant = new WeatherAssistantService();

//    Initialize
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ShowError.setStatusBarLabel(statusBarLabel);
        loadStaticIcons();
        setupInputHandlers();
        loadWeatherData();
    }

    private void loadStaticIcons() {
        setImage(appLogo, IconLoader.getIcon(IconLoader.APP_LOGO));
        setImage(assistantAvatar, IconLoader.getIcon(IconLoader.APP_LOGO));
        setImage(typingAvatar, IconLoader.getIcon(IconLoader.APP_LOGO));
    }
//    Back to dashboard handlers
    @FXML
    public void handleBackToDashboard() {
        switchScene("dashboard");
    }

//    Button handlers
    private void setupInputHandlers() {
//        Send on button click
        sendButton.setOnAction(e -> handleSend());

//        Clear conversation
        clearButton.setOnAction(e -> {
//            Remove all dynamically added bubbles except initial welcome + suggestions + typingIndicator
            chatContainer.getChildren().removeIf(node -> node.getStyleClass().contains("userRow") ||
                    (node.getStyleClass().contains("assistantRow") && node != typingIndicator && !isWelcomeRow(node)));
            inputField.clear();
            showError("Conversation cleared.");
        });

        // Quick suggestions
        chatContainer.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                hbox.getChildren().forEach(child -> {
                    if (child instanceof Button && ((Button) child).getStyleClass().contains("chipButton")) {
                        Button btn = (Button) child;
                        btn.setOnAction(e -> {
                            inputField.setText(btn.getText());
                            handleSend();
                        });
                    }
                });
            }
        });
    }

//    Send button
    private void handleSend() {
//        Get text
        String text = inputField.getText().trim();
        if (text.isBlank()) return;

        inputField.clear();
        sendButton.setDisable(true);

//        Append user bubble
        appendUserBubble(text);

//        Show typing indicator above the bottom
        showTyping(true);
        scrollToBottom();

//        Run service calls on background thread to avoid blocking UI
        new Thread(() -> {
//            Small delay so the typing indicator is visible
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignored) {}

            String response = assistant.getResponse(forecastData, hourlyData, text);
//            Show the response
            Platform.runLater(() -> {
                showTyping(false);
                appendAssistantBubble(response);
                sendButton.setDisable(false);
                scrollToBottom();
            });
        }).start();
    }

//    Chat bubbles
//    Add user bubble to the right
    private void appendUserBubble(String text) {
        String time = new SimpleDateFormat("h:mm a").format(new Date());

        Label msgLabel = new Label(text);
        msgLabel.getStyleClass().add("messageText");
        msgLabel.setWrapText(true);

        Label tsLabel = new Label(time);
        tsLabel.getStyleClass().add("timestamp");

        VBox bubble = new VBox(4, msgLabel, tsLabel);
        bubble.getStyleClass().add("userBubble");
        HBox.setMargin(bubble, new Insets(0, 0, 0, 120));

        HBox row = new HBox(bubble);
        row.getStyleClass().add("userRow");

        insertBeforeTyping(row);
    }

//    Add assistant bubble to the left
    private void appendAssistantBubble(String text) {
        String time = new SimpleDateFormat("h:mm a").format(new Date());

//        Avatar
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatarWrap");
        avatar.setMinSize(36, 36);
        avatar.setMaxSize(36, 36);
        ImageView av = new ImageView(IconLoader.getIcon(IconLoader.APP_LOGO));
        av.setFitWidth(20);
        av.setFitHeight(20);
        av.setPreserveRatio(true);
        avatar.getChildren().add(av);

//        Bubble content
        Label sender = new Label("WeatherGPT");
        sender.getStyleClass().add("senderName");

        Label msg = new Label(text);
        msg.getStyleClass().add("messageText");
        msg.setWrapText(true);

        Label ts = new Label(time);
        ts.getStyleClass().add("timestamp");

        VBox bubble = new VBox(6, sender, msg, ts);
        bubble.getStyleClass().add("assistantBubble");
        HBox.setMargin(bubble, new Insets(0, 120, 0, 0)); // cap right margin

        HBox row = new HBox(10, avatar, bubble);
        row.getStyleClass().add("assistantRow");

        insertBeforeTyping(row);
    }

//    Insert a node before the typing indicator
    private void insertBeforeTyping(javafx.scene.Node node) {
        int idx = chatContainer.getChildren().indexOf(typingIndicator);
        if (idx >= 0) {
            chatContainer.getChildren().add(idx, node);
        } else {
            chatContainer.getChildren().add(node);
        }
    }

//    Check whether welcome row
    private boolean isWelcomeRow(javafx.scene.Node node) {
        return "welcomeMessage".equals(node.getId());
    }

//    Typing Indicator
    private void showTyping(boolean visible) {
        typingIndicator.setVisible(visible);
        typingIndicator.setManaged(visible);
    }

//    Load weather data from Location Manager
//    Get forecast and hourly forecast from proxy
    private void loadWeatherData() {
//        Get the current location data
        PointData pointData = LocationManager.getInstance().getCurrentLocation();
        if (pointData == null) {
            showError("Location data not found.");
            return;
        }
        // Display location in top bar
        String city  = pointData.relativeLocation.properties.city;
        String state = pointData.relativeLocation.properties.state;
        Platform.runLater(() -> locationLabel.setText(city + ", " + state));

        // Fetch on background thread
        new Thread(() -> {
            try {
//                Fetch hourly forecast from the stored API route
                hourlyData = weatherApi.getHourlyForecastFromURL(pointData.forecastHourly);
                if (hourlyData == null || hourlyData.isEmpty()) {
                    showError("Failed to fetch hourly forecast");
                    return;
                }

//                Fetch forecast from the stored API route
                forecastData = weatherApi.getForecastFromURL(pointData.forecast);
                if (forecastData == null || forecastData.isEmpty()) {
                    showError("Failed to fetch forecast");
                    return;
                }

                Platform.runLater(() -> {
                    String updated = new SimpleDateFormat("h:mm a").format(new Date());
                    statusBarLabel.setText("NWS API — Last updated: " + updated + "  ·  " + city + ", " + state);
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("Error loading weather data: " + e.getMessage());
            }
        }).start();
    }

//    private helper functions
//    Auto scroll down to newest message
    private void scrollToBottom() {
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

//    Set imageview null safe
    private void setImage(ImageView view, Image image) {
        if (view != null) view.setImage(image);
    }
}

