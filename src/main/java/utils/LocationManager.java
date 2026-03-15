package utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import point.Location;
import point.PointData;
import point.RelativeLocation;

public class LocationManager {
    private static LocationManager instance;
    private final ObservableList<String> locationNames;
    private final ObservableList<PointData> locations;


    private LocationManager() {
        locationNames = FXCollections.observableArrayList();
        locations = FXCollections.observableArrayList();
        initDefaultLocation();
    }

    public static LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    private void initDefaultLocation(){
        Location chicagoLocation = new Location();
        chicagoLocation.city = "Chicago";
        chicagoLocation.state = "IL";

        RelativeLocation chicagoRelativeLocation = new RelativeLocation();
        chicagoRelativeLocation.properties = chicagoLocation;

        PointData chicagoData = new PointData();
        chicagoData.relativeLocation = chicagoRelativeLocation;
        chicagoData.gridId = "LOT";
        chicagoData.gridX = 76;
        chicagoData.gridY = 73;
        chicagoData.forecast = "https://api.weather.gov/gridpoints/LOT/76,73/forecast";
        chicagoData.forecastHourly = "https://api.weather.gov/gridpoints/LOT/76,73/forecast/hourly";

        addLocation(chicagoData);
    }

    public ObservableList<String> getLocationNames() {
        return locationNames;
    }

    public ObservableList<PointData> getLocations() {
        return locations;
    }

    public void addLocation(PointData pointData) {
        if (pointData != null && pointData.relativeLocation != null) {
            String locationName = pointData.relativeLocation.properties.city + ", " + pointData.relativeLocation.properties.state;
            if (!locationNames.contains(locationName)) {
                locationNames.add(locationName);
                locations.add(pointData);
            }
        }
    }

    public void removeLocation(String locationName) {
        int index = locationNames.indexOf(locationName);
        if (index >= 0) {
            locationNames.remove(index);
            locations.remove(index);
        }
    }

    public PointData getCurrentLocation(String locationName){
        int index = locationNames.indexOf(locationName);
        if (index >= 0) {
            return locations.get(index);
        }
        return null;
    }
}
