package hourlyWeather;

import utils.FeelsLikeCalculator;

import static utils.Parser.*;

//This is the Adapter class
//It implements the WeatherTableEntry interface and wraps the Hourly Period class
public class HourlyEntryAdapter implements WeatherTableEntry{
    private String time;
    private Integer temperature;
    private Integer feelsLike;
    private String condition;
    private String wind;
    private String precip;

//    Transform and format API data for the UI
    public HourlyEntryAdapter(HourlyPeriod period) {
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
    @Override
    public String getTime() {
        return time;
    }

    @Override
    public Integer getTemperature() {
        return temperature;
    }

    @Override
    public Integer getFeelsLike() {
        return feelsLike;
    }

    @Override
    public String getCondition() {
        return condition;
    }

    @Override
    public String getWind() {
        return wind;
    }

    @Override
    public String getPrecip() {
        return precip;
    }
}