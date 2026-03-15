import hourlyWeather.HourlyPeriod;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.IconLoader;
import weather.Period;
import point.PointData;

import java.util.ArrayList;

public class JavaFX extends Application {
	TextField temperature,weather;
	TextField gridX,gridY;
	TextField location;
	String locationName;

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
//		primaryStage.setTitle("I'm a professional Weather App!");
//		ArrayList<HourlyPeriod> forecast = api.MyWeatherAPI.getHourlyForecast("LOT",77,70);
//		PointData pointData = api.MyWeatherAPI.getPointData(47.62350, -122.33290);
//		if (forecast == null){
//			throw new RuntimeException("Forecast did not load");
//		}
//		temperature = new TextField();
//		weather = new TextField();
//		temperature.setText("Today's weather is: "+String.valueOf(forecast.get(0).temperature));
//		weather.setText(forecast.get(0).shortForecast);
//		if (pointData == null){
//			throw new RuntimeException("Point Data did not load");
//		}
//		gridX = new TextField();
//		gridY = new TextField();
//		gridX.setText(String.valueOf(pointData.gridX));
//		gridY.setText(String.valueOf(pointData.gridY));
//		location = new TextField();
//		locationName = String.valueOf(pointData.relativeLocation.properties.city) + ", " + String.valueOf(pointData.relativeLocation.properties.state);
//		location.setText(locationName);


//		Load the icon before loading screens
		IconLoader.preload();

		FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/FXML/dashboard.fxml"));
		Parent dashboardRoot = dashboardLoader.load();

		Scene dashboardScene = new Scene(dashboardRoot, 700,700);
				
//		Scene scene = new Scene(new VBox(temperature,weather,gridX,gridY), 700,700);
		primaryStage.setTitle("WeatherGPT");
		Image logo = new Image(getClass().getResourceAsStream("/assets/app-logo.png"));
		primaryStage.getIcons().add(logo);
		primaryStage.setScene(dashboardScene);
		primaryStage.show();
	}

}
