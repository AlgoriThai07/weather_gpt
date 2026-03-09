package hourlyWeather;

import weather.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyRoot{
    public String type;
    public Geometry geometry;
//    New class
    public HourlyProperties properties;
}
