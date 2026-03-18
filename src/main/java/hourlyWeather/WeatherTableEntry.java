package hourlyWeather;


// What the JavaFX TableView expects to see
public interface WeatherTableEntry {
    String getTime();
    Integer getTemperature();
    Integer getFeelsLike();
    String getCondition();
    String getWind();
    String getPrecip();
}
