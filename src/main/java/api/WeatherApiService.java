package api;

import hourlyWeather.HourlyPeriod;
import point.PointData;
import weather.Period;

import java.util.ArrayList;

// Same calls on both proxy and myWeatherAPI
public interface WeatherApiService {
    PointData getPointData(double lat, double lon);
    ArrayList<Period> getForecastFromURL(String url);
    ArrayList<HourlyPeriod> getHourlyForecastFromURL(String url);
}
