package point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PointData {
    public String gridId;
    public int gridX;
    public int gridY;
    public String forecast;
    public String forecastHourly;
}
