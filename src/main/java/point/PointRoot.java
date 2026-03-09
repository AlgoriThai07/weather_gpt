package point;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PointRoot {
    public String type;
    public PointGeometry geometry;
    public PointData properties;
}
