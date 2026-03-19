package utils;

import weather.ProbabilityOfPrecipitation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Parser {
    private Parser() {}

//    Return precipitation in value + % (80%) format
    public static String formatPrecipitation(ProbabilityOfPrecipitation pop) {
        if (pop == null) return "0%";
        try {
            int percentage = pop.value;
            return percentage + "%";
        } catch (Exception e) {
            return "0%";
        }
    }

//    Return the "h:mm AM/PM" format
    public static String formatTime(java.util.Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        return sdf.format(date);
    }

    public static String formatDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d 'at' h:mm a");
        return sdf.format(new Date());
    }

//  Transform the windspeed from string to a double value
//  to calculate feels like temp
    public static double parseWindSpeed(String windString) {
        if (windString == null || windString.isEmpty()) return 0.0;
        try {
            // Extract the numeric part before " mph"
            String[] parts = windString.split(" ");
            return Double.parseDouble(parts[0]);
        } catch (Exception e) {
            return 0.0;
        }
    }

//    Extract condition from short forecast
//    Map to one of the standardized value
    public static String extractCondition(String shortForecast) {
        if (shortForecast == null) {
            return "Cloudy";
        }

        String forecast = shortForecast.toUpperCase();

        if (forecast.contains("THUNDERSTORM")) {
            return "Thunderstorms";
        }
        if (forecast.contains("SNOW")) {
            return "Snow";
        }
        if (forecast.contains("RAIN") || forecast.contains("SHOWERS")) {
            return "Rain";
        }
        if (forecast.contains("SUNNY") || forecast.contains("CLEAR")) {
            return "Sunny";
        }
        if (forecast.contains("PARTLY") && forecast.contains("CLOUDY")) {
            return "Partly Cloudy";
        }
        if (forecast.contains("MOSTLY") && forecast.contains("CLOUDY")) {
            return "Mostly Cloudy";
        }
        if (forecast.contains("CLOUDY")) {
            return "Cloudy";
        }
//     Default fallback
        return "Cloudy";
    }
//    Map time name
    public static String timeName(String q) {
        if (q.contains("morning")) return "morning";
        if (q.contains("noon")) return "noon";
        if (q.contains("afternoon")) return "afternoon";
        if (q.contains("evening") || q.contains("tonight")) return "evening";
        if (q.contains("night")) return "night";
        return "day";
    }
//    Map time range
    public static int[] timeRange(String q) {
        if (q.contains("morning")) return new int[]{5,  11};
        if (q.contains("noon")) return new int[]{11, 13};
        if (q.contains("afternoon")) return new int[]{12, 17};
        if (q.contains("evening") || q.contains("tonight")) return new int[]{18, 21};
        if (q.contains("night") || q.contains("midnight")) return new int[]{21, 23};
        return new int[]{0, 23};
    }
//    Get hour
    public static int getHour(Date date) {
        if (date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }
}
