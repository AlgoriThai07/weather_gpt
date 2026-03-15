package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hourlyWeather.HourlyPeriod;
import hourlyWeather.HourlyRoot;
import point.PointData;
import point.PointRoot;
import weather.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

@SuppressWarnings("DefaultPackage")
public class MyWeatherAPI extends WeatherAPI {
    public static ArrayList<HourlyPeriod> getHourlyForecast(String region, int gridx, int gridy){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.gov/gridpoints/"+region+"/"+String.valueOf(gridx)+","+String.valueOf(gridy)+"/forecast/hourly"))
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response == null){
            System.err.println("Failed to get response");
            return null;
        }
        HourlyRoot r = getHourlyObject(response.body());
        if(r == null){
            System.err.println("Failed to parse JSon");
            return null;
        }
        ArrayList<HourlyPeriod> allPeriods = r.properties.periods;
        ArrayList<HourlyPeriod> limitedPeriods = new ArrayList<>();
        for (int i = 0; i < Math.min(24, allPeriods.size()); i++) {
            limitedPeriods.add(allPeriods.get(i));
        }
        return limitedPeriods;
    }
    public static HourlyRoot getHourlyObject(String json){
        ObjectMapper om = new ObjectMapper();
        HourlyRoot toRet = null;
        try {
            toRet = om.readValue(json, HourlyRoot.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toRet;
    }
    public static PointData getPointData(double lat, double lon){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.weather.gov/points/"+String.valueOf(lat)+","+String.valueOf(lon)))
                .header("User-Agent", "MyWeatherApp/1.0 (thaiviet0703@gmail.com)")
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response == null){
            System.err.println("Failed to get response");
            return null;
        }
        PointRoot r = getPointObject(response.body());
        if(r == null){
            System.err.println("Failed to parse JSon");
            return null;
        }
        return r.properties;

    }
    public static PointRoot getPointObject(String json){
        ObjectMapper om = new ObjectMapper();
        PointRoot toRet = null;
        try {
            toRet = om.readValue(json, PointRoot.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toRet;
    }
    public static ArrayList<Period> getForecastFromURL(String url){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "MyWeatherApp/1.0 (thaiviet0703@gmail.com)")
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response == null){
            System.err.println("Failed to get response");
            return null;
        }
        Root r = getObject(response.body());
        if(r == null){
            System.err.println("Failed to parse JSon");
            return null;
        }
        return r.properties.periods;
    }
    public static ArrayList<HourlyPeriod> getHourlyForecastFromURL(String url){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "MyWeatherApp/1.0 (thaiviet0703@gmail.com)")
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response == null){
            System.err.println("Failed to get response");
            return null;
        }
        HourlyRoot r = getHourlyObject(response.body());
        if(r == null){
            System.err.println("Failed to parse JSon");
            return null;
        }
        ArrayList<HourlyPeriod> allPeriods = r.properties.periods;
        ArrayList<HourlyPeriod> limitedPeriods = new ArrayList<>();
        for (int i = 0; i < Math.min(24, allPeriods.size()); i++) {
            limitedPeriods.add(allPeriods.get(i));
        }
        return limitedPeriods;
    }
}
