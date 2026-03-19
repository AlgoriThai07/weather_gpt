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
    private PointData currentLocation;


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

        point.PointGeometry chicagoGeometry = new point.PointGeometry();
        chicagoGeometry.type = "Point";
        chicagoGeometry.coordinates = new java.util.ArrayList<>();
        // Chicago's default lat/lon
        chicagoGeometry.coordinates.add(-87.62);
        chicagoGeometry.coordinates.add(41.87);

        RelativeLocation chicagoRelativeLocation = new RelativeLocation();
        chicagoRelativeLocation.properties = chicagoLocation;
        chicagoRelativeLocation.geometry = chicagoGeometry;

        PointData chicagoData = new PointData();
        chicagoData.relativeLocation = chicagoRelativeLocation;
        chicagoData.gridId = "LOT";
        chicagoData.gridX = 76;
        chicagoData.gridY = 73;
        chicagoData.forecast = "https://api.weather.gov/gridpoints/LOT/76,73/forecast";
        chicagoData.forecastHourly = "https://api.weather.gov/gridpoints/LOT/76,73/forecast/hourly";

        addLocation(chicagoData);
        this.currentLocation = chicagoData;
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

    public PointData getLocationFromName(String locationName){
        int index = locationNames.indexOf(locationName);
        if (index >= 0) {
            return locations.get(index);
        }
        return null;
    }

    public PointData getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String locationName) {
        PointData currentLocation = getLocationFromName(locationName);
        if (currentLocation != null) {
            this.currentLocation = currentLocation;
        }
    }
}
