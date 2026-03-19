import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.IconLoader;
import utils.SwitchScene;
public class JavaFX extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
//		Load the icon before loading screens
		IconLoader.preload();

		FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/FXML/dashboard.fxml"));
		Parent dashboardRoot = dashboardLoader.load();

		Scene dashboardScene = new Scene(dashboardRoot, 800,700);

		SwitchScene.setCurrentScene(dashboardScene);
		SwitchScene.setPrimaryStage(primaryStage);

		primaryStage.setTitle("WeatherGPT");
		Image logo = new Image(getClass().getResourceAsStream("/assets/app-logo.png"));
		primaryStage.getIcons().add(logo);
		primaryStage.setScene(dashboardScene);
		primaryStage.show();
	}

}
