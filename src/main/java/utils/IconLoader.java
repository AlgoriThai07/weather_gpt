package utils;

import javafx.scene.image.Image;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class IconLoader {

//    Base path
    private static final String BASE = "/assets/";
//    Image Cache
    private static final Map<String, Image> cache = new HashMap<>();
//    Logo constants
    public static final String APP_LOGO             = "app-logo.png";
    public static final String LOCATION_PIN         = "location-pin.png";
    public static final String FEELS_LIKE_ICON   = "feels-like.png";
    public static final String DEWPOINT_ICON = "dewpoint.png";
    public static final String WIND_ICON = "wind.png";
    public static final String PRECIPITATION_ICON        = "precipitation.png";
    public static final String HUMIDITY_ICON = "humidity.png";

//    Weather conditions -> icon file name
    private static final Map<String, String> CONDITION_ICONS = Map.of(
    "Cloudy",        "cloudy.png",
    "Rain",          "rain.png",
    "Thunderstorms", "thunderstorms.png",
    "Mostly Cloudy", "mostly-cloudy.png",
    "Partly Cloudy", "partly-cloudy.png",
    "Sunny",         "sunny.png",
    "Snow",          "snow.png"
);

//    Private constructor - never called
    private IconLoader() {}

//    Return a cached image of the filename
    public static Image getIcon(String filename) {
//        If cache has image, return
//        Else, get from assets
        return cache.computeIfAbsent(filename, f -> {
            URL url = IconLoader.class.getResource(BASE + f);

            if (url == null) {
                System.err.println("Icon Loader: Cannot find icon " + BASE + f);
                return null;
            }
//            Set the background loading = true, large images don't block FX thread
            return new Image(url.toExternalForm(), true);
        });
    }

//    Return a cached image of the condition
//    If another condition, return cloudy icon
    public static Image getConditionIcon(String condition) {
        String filename = CONDITION_ICONS.getOrDefault(condition, "cloudy.png");
        return getIcon(filename);
    }

//    Preload the cache with the necessary icons
    public static void preload() {
        getIcon(APP_LOGO);
        getIcon(LOCATION_PIN);
        getIcon(FEELS_LIKE_ICON);
        getIcon(DEWPOINT_ICON);
        getIcon(WIND_ICON);
        getIcon(PRECIPITATION_ICON);
        getIcon(HUMIDITY_ICON);

        // Preload all condition icons
        for (String filename : CONDITION_ICONS.values()) {
            getIcon(filename);
        }

        System.out.println("Icon Loader: Preloaded icons");
    }
    public static void clearCache() {
        cache.clear();
    }
}