package hourlyWeather;

import utils.FeelsLikeCalculator;

import static utils.Parser.*;

//Class for displaying in the 24h forecast
public class HourlyEntry {
    private String time;
    private Integer temperature;
    private Integer feelsLike;
    private String condition;
    private String wind;
    private String precip;

//    Transform and format API data for the UI
    public HourlyEntry(HourlyPeriod period) {
//        Format time as "3:00 PM"
        this.time = formatTime(period.startTime);

//        Temperature
        this.temperature = period.temperature;

//        Calculate feels Like temperature
        double humidity = (period.relativeHumidity != null) ? period.relativeHumidity.value : 50.0;
        double windSpeed = parseWindSpeed(period.windSpeed);
        this.feelsLike = (int) Math.round(FeelsLikeCalculator.calculateFeelsLike(this.temperature, humidity, windSpeed));

        // Extract condition from shortForecast (e.g., "Cloudy", "Rain", "Thunderstorms")
        this.condition = extractCondition(period.shortForecast);

        // Format wind: "12 mph NW"
        this.wind = period.windSpeed;

        // Extract precip probability
        this.precip = formatPrecipitation(period.probabilityOfPrecipitation);
    }

    public String getTime() {
        return time;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public Integer getFeelsLike() {
        return feelsLike;
    }

    public String getCondition() {
        return condition;
    }

    public String getWind() {
        return wind;
    }

    public String getPrecip() {
        return precip;
    }
}