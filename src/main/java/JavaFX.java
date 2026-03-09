import hourlyWeather.HourlyPeriod;
import javafx.application.Application;

import javafx.scene.Scene;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import weather.Period;
import point.PointData;

import java.util.ArrayList;

public class JavaFX extends Application {
	TextField temperature,weather;
	TextField gridX,gridY;

	public static void main(String[] args) {
		launch(args);
	}

	//feel free to remove the starter code from this method
	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("I'm a professional Weather App!");
		ArrayList<HourlyPeriod> forecast = MyWeatherAPI.getHourlyForecast("LOT",77,70);
		PointData pointData = MyWeatherAPI.getPointData(47.62350, -122.33290);
		if (forecast == null){
			throw new RuntimeException("Forecast did not load");
		}
		temperature = new TextField();
		weather = new TextField();
		temperature.setText("Today's weather is: "+String.valueOf(forecast.get(0).temperature));
		weather.setText(forecast.get(0).shortForecast);
		if (pointData == null){
			throw new RuntimeException("Point Data did not load");
		}
		gridX = new TextField();
		gridY = new TextField();
		gridX.setText(String.valueOf(pointData.gridX));
		gridY.setText(String.valueOf(pointData.gridY));
		
		
		
				
		Scene scene = new Scene(new VBox(temperature,weather,gridX,gridY), 700,700);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
