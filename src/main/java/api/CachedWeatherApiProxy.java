package api;

import hourlyWeather.HourlyPeriod;
import point.PointData;
import weather.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Proxy: Control access to MyWeatherApi by adding caching
public class CachedWeatherApiProxy implements WeatherApiService{
    private final WeatherApiService realApi;

//    Cache maps: api URL -> Data
    private final Map<String, CacheEntry<ArrayList<HourlyPeriod>>> hourlyCache = new HashMap<>();
    private final Map<String, CacheEntry<ArrayList<Period>>> forecastCache = new HashMap<>();
//    Cache maps: lat,lon -> Data
    private final Map<String, CacheEntry<PointData>> pointDataCache = new HashMap<>();

//    Cache duration (15 mins)
    private static final long CACHE_DURATION = 15 * 60 * 1000;

    public CachedWeatherApiProxy(WeatherApiService realApi) {
        this.realApi = realApi;
    }

    @Override
    public PointData getPointData(double lat, double lon) {
//        Cache key is lat,lon. Only take 2 decimal places
        String key = String.format("%.2f,%.2f", lat, lon);
//        If the cache is valid, return data
        if (isCacheValid(pointDataCache, key)){
            System.out.println("Cache hit for PointData: " + key);
            return pointDataCache.get(key).data;
        }
//        Else fetch data from Api and also add to the cache
        System.out.println("Fetching fresh PointData from API for: " + key);
        PointData freshData = realApi.getPointData(lat, lon);
        if (freshData != null){
            pointDataCache.put(key, new CacheEntry<>(freshData, System.currentTimeMillis()));
        }
        return freshData;
    }

    @Override
    public ArrayList<Period> getForecastFromURL(String url){
//        If the cache is valid, return data
        if (isCacheValid(forecastCache, url)){
            System.out.println("Return cached forecast for: " + url);
            return forecastCache.get(url).data;
        }

//        Else fetch data from Api and also add to the cache
        System.out.println("Fetching fresh forecast from API for: " + url);
        ArrayList<Period> freshData = realApi.getForecastFromURL(url);
        if (freshData != null){
            forecastCache.put(url, new CacheEntry<>(freshData, System.currentTimeMillis()));
        }
        return freshData;
    }

    @Override
    public ArrayList<HourlyPeriod> getHourlyForecastFromURL(String url){
//        If the cache is valid, return data
        if (isCacheValid(hourlyCache, url)){
            System.out.println("Return cached hourly forecast for: " + url);
            return  hourlyCache.get(url).data;
        }

//        Else fetch data from Api and also add to the cache
        System.out.println("Fetching fresh hourly forecast from API for: " + url);
        ArrayList<HourlyPeriod> freshData = realApi.getHourlyForecastFromURL(url);
        if (freshData != null){
            hourlyCache.put(url, new CacheEntry<>(freshData, System.currentTimeMillis()));
        }
        return freshData;
    }

//    Return True if key in the cache and not expired
    private boolean isCacheValid(Map<String, ? extends CacheEntry<?>> cache, String key) {
        if (!cache.containsKey(key)) return false;
        CacheEntry<?> entry = cache.get(key);
        return System.currentTimeMillis() - entry.timestamp < CACHE_DURATION;
    }

//    Helper class
    private static class CacheEntry<T> {
        T data;
        long timestamp;

        public CacheEntry(T data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}

