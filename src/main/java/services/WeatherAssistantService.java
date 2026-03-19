package services;

import hourlyWeather.HourlyPeriod;
import utils.FeelsLikeCalculator;
import utils.Parser;
import weather.Period;

import java.util.*;

import static utils.Parser.*;


public class WeatherAssistantService {

//    Return response from question using forecast and hourly forecast
    public String getResponse(ArrayList<Period> forecastData, ArrayList<HourlyPeriod> hourlyData, String question) {

        if (question == null || question.isBlank()) return fallback();

        String q = normalize(question);

        if (forecastData.isEmpty() && hourlyData.isEmpty()){
            return "I don't have any forecast data loaded yet. Please go back to the dashboard and select a location.";
        }

//        Answer based on priorities
//        Snow tonight
        if (isTonightQuestion(q) && isSnowQuestion(q))       return snowTonight(forecastData);
//        Rain tonight
        if (isTonightQuestion(q) && isRainQuestion(q))       return rainTonight(hourlyData);
//        Snow period
        if (isTimeSpecificQuestion(q) && isSnowQuestion(q))  return snowAtTime(forecastData, q);
//        Rain period
        if (isTimeSpecificQuestion(q) && isRainQuestion(q))  return rainAtTime(hourlyData, q);
//        snow general
        if (isSnowQuestion(q))      return snowGeneral(forecastData);
//        rain general
        if (isRainQuestion(q))      return rainGeneral(hourlyData);
//        Clothes related
        if (isClothingQuestion(q))  return clothing(forecastData, hourlyData);
//        Wind related
        if (isWindQuestion(q))      return wind(forecastData);
//        Activity related
        if (isActivityQuestion(q))  return activity(forecastData, hourlyData);
//        Feels like related
        if (isFeelsLikeQuestion(q)) return feelsLike(hourlyData);
//        Overview
        if (isOverviewQuestion(q))  return overview(forecastData, hourlyData);
//        Not fall into any of the above question types
        return fallback();
    }

//    /**
//     * Convenience method — generates a weather overview.
//     * Matches the spec signature used in the controller or dashboard.
//     */
//    public String generateOverview(ArrayList<Period> forecastData,
//                                   ArrayList<HourlyPeriod> hourlyData) {
//        return overview(forecastData, hourlyData);
//    }

//    Normalize the questions
    private String normalize(String s) {
        return s.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

//    Match the keywords to the types of question
    private boolean isRainQuestion(String q) {
        return match(q, "rain", "umbrella", "wet", "drizzle", "precipitation", "shower", "rainy");
    }

    private boolean isSnowQuestion(String q) {
        return match(q, "snow", "freeze", "blizzard", "sleet", "flurries", "snowfall", "wintry");
    }

    private boolean isClothingQuestion(String q) {
        return match(q, "wear", "jacket", "coat", "dress", "outfit", "clothes", "clothing",
                "layers", "boots", "hat", "gloves", "scarf", "what should i", "umbrella");
    }

    private boolean isWindQuestion(String q) {
        return match(q, "wind", "breezy", "gusty", "gust", "windy");
    }

    private boolean isActivityQuestion(String q) {
        return match(q, "run", "walk", "outside", "outdoor", "hike", "bike", "jog",
                "exercise", "play", "picnic", "park", "sport", "go out");
    }

    private boolean isTonightQuestion(String q) {
        return match(q, "tonight", "this evening", "this night");
    }

    private boolean isTimeSpecificQuestion(String q) {
        return match(q, "morning", "afternoon", "evening", "night", "noon", "midnight", "tonight");
    }

    private boolean isFeelsLikeQuestion(String q) {
        return match(q, "feels like", "feel like", "wind chill", "heat index",
                "how cold", "how hot", "actual feel", "feel outside");
    }

    private boolean isOverviewQuestion(String q) {
        return match(q, "today", "overview", "summary", "forecast", "weather", "week",
                "what", "how", "tell me", "give me", "current");
    }

//    Responses for rain
    private String rainGeneral(ArrayList<HourlyPeriod> hourlyData) {
        if (hourlyData.isEmpty()) {
            return "I don't have hourly data to check precipitation chances right now.";
        }
        int maxProbability = 0;
        HourlyPeriod firstHighChance = null;

//        Get the max probability of rain
//        And when is the first high chance of rain
        for (HourlyPeriod hp : hourlyData) {
            int probability = getPrecipitation(hp);
            maxProbability = Math.max(maxProbability, probability);
            if (firstHighChance == null && probability >= 50) firstHighChance = hp;
        }
//        If low probability of rain
        if (maxProbability < 20) {
            return "Rain looks unlikely today as no significant precipitation is expected. Enjoy the dry weather!";
        }
//        If there some probability of rain
        if (maxProbability < 40) {
            return "There's a slight chance of rain (" + maxProbability + "%) today, but it probably won't amount to much.";
        }
//        If there a high chance of rain
        String timing = firstHighChance != null ? "around "
                + Parser.formatTime(firstHighChance.startTime) : "at some point today";

        if (maxProbability >= 70) {
            return "Rain is very likely at " + timing + " with a " + maxProbability + "% chance. Definitely bring an umbrella.";
        }
        return "There's a " + maxProbability + "% chance of rain, most likely " + timing
                + ". Keeping an umbrella handy would be smart.";
    }
//    Response for rain tonight
    private String rainTonight(ArrayList<HourlyPeriod> hourlyData) {
//        Filter only the evening hours
        List<HourlyPeriod> evening = filterByHourRange(hourlyData, 18, 23);
        if (evening.isEmpty()) return rainGeneral(hourlyData);

        int maxProbability = 0;
        for (HourlyPeriod hp : evening) {
            maxProbability = Math.max(maxProbability, getPrecipitation(hp));
        }

        if (maxProbability < 20) return "Tonight is looking dry, so rain is unlikely this evening.";
        if (maxProbability >= 60) {
            return "Rain is likely tonight with a " + maxProbability
                    + "% chance. Bring an umbrella if you're heading out.";
        }
        return "There's a " + maxProbability
                + "% chance of rain tonight. It could go either way. A light jacket wouldn't hurt just in case.";
    }
//    Rain at a specifc range
    private String rainAtTime(ArrayList<HourlyPeriod> hourlyData, String q) {
        int[] range = timeRange(q);
        String period = timeName(q);
        List<HourlyPeriod> timeRange = filterByHourRange(hourlyData, range[0], range[1]);

        if (timeRange.isEmpty()) {
            return "I don't have hourly data for the " + period + " to check rain chances.";
        }
        int maxProbability = 0;
        for (HourlyPeriod hp : timeRange) {
            maxProbability = Math.max(maxProbability, getPrecipitation(hp));
        }

        if (maxProbability < 20)  return "Rain is unlikely this " + period + ". Looks dry!";
        if (maxProbability >= 60) {
            return "There's a " + maxProbability + "% chance of rain this " + period + ". An umbrella is a good idea.";
        }
        return "There's a " + maxProbability + "% rain chance this " + period + ". Keep an eye on it.";
    }

//    Responses for snow
//    Because snow is a rare event, no need to check the hourly data
//    Just use the general forecast data
    private String snowGeneral(ArrayList<Period> forecastData) {
        Period p = firstDaytimePeriod(forecastData);
        if (p == null) return "I don't have forecast data to check for snow.";
        return isSnowy(p, "today");
    }

    private String snowTonight(ArrayList<Period> forecastData) {
        Period night = null;
        for (Period p : forecastData) {
            if (!p.isDaytime){
                night = p;
                break;
            }
        }
        if (night == null) return snowGeneral(forecastData);
        return isSnowy(night, "tonight");
    }

    private String snowAtTime(ArrayList<Period> forecastData, String q) {
        String timeRange = timeName(q);
        boolean isNight = timeRange.equals("evening") || timeRange.equals("night");
        Period p = null;
        if (isNight) {
            for (Period p1 : forecastData) {
                if (!p1.isDaytime){
                    p = p1;
                    break;
                }
            }
        }
        else {
            p = firstDaytimePeriod(forecastData);
        }
        if (p == null) return "No forecast data available.";
        return isSnowy(p, "this " + timeRange);
    }
//    Check for snow
    private String isSnowy(Period p, String when) {
//        Combine both forecast and normalize
        String combined = ((p.shortForecast != null ? p.shortForecast : "")
                        +  " "
                        +  (p.detailedForecast != null ? p.detailedForecast : ""))
                        .toLowerCase(Locale.ENGLISH);
//        Check for snowy weather
        boolean hasSnow = match(combined, "snow", "sleet", "flurr", "wintry");
//        If there no snow
        if (!hasSnow) {
            return "No snow is expected " + when + ". " + p.shortForecast + ".";
        }
        return "Snow is possible " + when + ". " + p.shortForecast + ". Drive carefully and dress in warm layers.";
    }

//    Responses for clothes
    private String clothing(ArrayList<Period> forecastData, ArrayList<HourlyPeriod> hourlyData) {
        Period today = firstDaytimePeriod(forecastData);
        if (today == null) return "I need forecast data to give clothing advice.";
//        Get today weather info
        int temperature = today.temperature;
        double wind = Parser.parseWindSpeed(today.windSpeed);
        int precipitation = getPrecipitation(today);
        int humidity = hourlyData.isEmpty() ? 50 : getHumidity(hourlyData.get(0));
        double feelsLike = FeelsLikeCalculator.calculateFeelsLike(temperature, humidity, wind);

        String response = "";
        response += "It's " + temperature + "°F";
//        Only add feel like if the difference is more than 3
        if (Math.abs(feelsLike - temperature) >= 3) {
            response += " (feels like " + Math.round(feelsLike) + "°F)";
        }
        response += ". ";

//        Give clothes advice based on Feels like
        if (feelsLike < 15) {
            response += "Dangerously cold — heavy winter coat, thermal layers, hat, scarf, and gloves are a must. ";
        }
        else if (feelsLike < 32) {
            response += "It's freezing out — a heavy coat and warm layers are essential. ";
        }
        else if (feelsLike < 45) {
            response += "A medium-weight coat will keep you comfortable today. ";
        }
        else if (feelsLike < 55) {
            response += "A light jacket or fleece is a good call today. ";
        }
        else if (feelsLike < 68) {
            response += "Long sleeves or a light layer should do the trick. ";
        }
        else if (feelsLike < 80) {
            response += "Light, comfortable clothing is all you need today. ";
        }
        else {
            response += "It's warm — go with light, breathable clothing. ";
        }

//        Give clothes advice based on precipitation
        if (precipitation >= 60) {
            response += "Rain is likely, so bring an umbrella or waterproof jacket. ";
        }
        else if (precipitation >= 30) {
            response += "There's a chance of rain — an umbrella wouldn't hurt. ";
        }
//        Give clothes advice based on wind
        if (wind >= 30) {
            response += "Winds are strong today, so a windbreaker is a smart add.";
        }
        else if (wind >= 18) {
            response += "It'll be breezy — a wind-resistant outer layer helps.";
        }

        return response;
    }

//    Responses for wind
    private String wind(ArrayList<Period> forecastData) {
        Period today = firstDaytimePeriod(forecastData);
        if (today == null) return "No forecast data available for wind.";

        String raw   = today.windSpeed;
        double speed = Parser.parseWindSpeed(raw);
        String dir   = today.windDirection;
//        Description for different speed
        String description;
        if (speed < 5) {
            description = "winds are calm";
        }
        else if (speed < 15) {
            description = "there's a light breeze";
        }
        else if (speed < 25) {
            description = "it's moderately windy";
        }
        else if (speed < 40) {
            description = "winds are strong";
        }
        else {
            description = "winds are very strong";
        }
//        Advice for wind conditions
        String advice = "";
        if (speed >= 30) {
            advice = " A windbreaker or wind-resistant jacket is recommended.";
        }
        else if (speed >= 18) {
            advice = " Light layers will help shield you from the breeze.";
        }

        return "Today " + description + " out of the " + dir + " at " + raw + "." + advice;
    }

//    Responses for activity
    private String activity(ArrayList<Period> forecastData, ArrayList<HourlyPeriod> hourlyData) {
        Period today = firstDaytimePeriod(forecastData);
        if (today == null) return "I need forecast data to evaluate outdoor conditions.";

//        Get today weather info
        int temperature = today.temperature;
        double wind = Parser.parseWindSpeed(today.windSpeed);
        int precipitation = getPrecipitation(today);
        int humidity = hourlyData.isEmpty() ? 50 : getHumidity(hourlyData.get(0));
        double feelsLike = FeelsLikeCalculator.calculateFeelsLike(temperature, humidity, wind);

//        Create a scoring system for outdoor activities conditions
        int score = 5;
        List<String> issues = new ArrayList<>();

        if (feelsLike < 20) {
            score -= 3;
            issues.add("dangerously cold temperatures");
        }
        else if (feelsLike < 35) {
            score -= 1;
            issues.add("cold conditions");
        }

        if (feelsLike > 100) {
            score -= 3;
            issues.add("extreme heat");
        }
        else if (feelsLike > 88) {
            score -= 1;
            issues.add("high heat and humidity");
        }

        if (precipitation >= 70) {
            score -= 2;
            issues.add("heavy rain likely");
        }
        else if (precipitation >= 40) {
            score -= 1;
            issues.add("possible rain");
        }

        if (wind >= 40) {
            score -= 2;
            issues.add("very strong winds");
        }
        else if (wind >= 28) {
            score -= 1;
            issues.add("strong winds");
        }

        String issueStr = issues.isEmpty() ? null : String.join(", ", issues);

        if (score >= 4) {
            return "Conditions look great for outdoor activities today! Enjoy the " + temperature + "°F weather.";
        }
        if (score == 3) {
            return "It's a decent day to get outside."
                    + (issueStr != null ? " Just keep an eye on the " + issueStr + "." : "");
        }
        if (score == 2) {
            return "You can head out, but be prepared as there's " + issueStr + " today.";
        }
        if (score == 1) {
            return "Outdoor plans might be tricky today due to " + issueStr + ". If you go out, dress accordingly.";
        }

        return "Conditions are not great for outdoor activities today as " + issueStr + ". Consider indoor alternatives.";
    }

//    Responses for feels like
    private String feelsLike(ArrayList<HourlyPeriod> hourlyData) {
        if (hourlyData.isEmpty()){
            return "I need hourly data to calculate the feels-like temperature.";
        }

        HourlyPeriod now  = hourlyData.get(0);
        int temperature = now.temperature;
        double wind = Parser.parseWindSpeed(now.windSpeed);
        int precipitation = getPrecipitation(now);
        int humidity = hourlyData.isEmpty() ? 50 : getHumidity(hourlyData.get(0));
        double feelsLike = FeelsLikeCalculator.calculateFeelsLike(temperature, humidity, wind);

        double diff = feelsLike - temperature;
//        Explain reason for the feels like
        String reason = "";
        if (temperature <= 50 && wind > 3) {
            reason += "wind chill from " + now.windSpeed + " winds";
        }
        else if (temperature >= 80) {
            reason += "heat index from " + humidity + "% humidity";
        }
        if (reason.isBlank() || Math.abs(diff) < 2) {
            return "It's " + temperature + "°F and the feels-like temperature is very close to actual. "
                    + "No significant wind chill or heat index right now.";
        }
        String direction = diff < 0 ? "colder" : "warmer";
        return "It's " + temperature + "°F outside, but it feels "
             + Math.abs(diff) + "°F " + direction + " (" + Math.round(feelsLike) + "°F) due to " + reason + ".";
    }

//    Responses for overview
    private String overview(ArrayList<Period> forecastData, ArrayList<HourlyPeriod> hourlyData) {
        Period today = firstDaytimePeriod(forecastData);
        if (today == null) {
            return "I don't have forecast data yet. Go back to the dashboard and select a location.";
        }
        Period tonight = null;
        for (Period p : forecastData) {
            if (!p.isDaytime) {
                tonight = p;
                break;
            }
        }

        int humidity = hourlyData.isEmpty() ? 50 : getHumidity(hourlyData.get(0));
        double wind = Parser.parseWindSpeed(today.windSpeed);
        double feelsLike = FeelsLikeCalculator.calculateFeelsLike(today.temperature, humidity, wind);

        String response = "";
        response += today.shortForecast + ". High of " + today.temperature + "°F";

        if (Math.abs(feelsLike - today.temperature) >= 3) {
            response += " (feels like " + Math.round(feelsLike) + "°F)";
        }
        response += ". ";

        if (wind >= 15) {
            response += "Winds out of the " + today.windDirection + " at " + today.windSpeed + ". ";
        }

        int maxPrecipitation = 0;
        HourlyPeriod firstHighChance = null;
        for (HourlyPeriod hp : hourlyData) {
            int p = getPrecipitation(hp);
            if (p > maxPrecipitation) maxPrecipitation = p;
            if (firstHighChance == null && p >= 50) firstHighChance = hp;
        }

        if (maxPrecipitation >= 50 && firstHighChance != null) {
            response += "Rain is likely around " + Parser.formatTime(firstHighChance.startTime) + " (" + maxPrecipitation + "% chance). ";
        }
        else if (maxPrecipitation >= 30) {
            response += "A " + maxPrecipitation + "% chance of rain today. ";
        }

        if (tonight != null) {
            response += "Tonight: " + tonight.shortForecast + ", low of " + tonight.temperature + "°F. ";
        }

        response += briefClothingHint(today.temperature, feelsLike, maxPrecipitation);

        return response.trim();
    }

//    Suggest a brief clothes hint
    private String briefClothingHint(int temp, double feels, int precip) {
        String clothes;
        if (feels < 32) {
            clothes = "Dress in heavy winter layers.";
        }
        else if (feels < 45) {
            clothes = "A warm jacket is a good idea.";
        }
        else if (feels < 58) {
            clothes = "A light jacket should keep you comfortable.";
        }
        else if (feels < 75) {
            clothes = "Light layers are all you need.";
        }
        else {
            clothes = "Light, breathable clothing is the way to go.";
        }
        if (precip >= 50) {
            clothes += " Don't forget an umbrella!";
        }
        return clothes;
    }

//    Fall back if not hit any of the above type of questions
    private String fallback() {
        return "I can help with weather questions about rain, snow, clothing, wind, and outdoor plans.";
    }


//    Return hourly period data filter on range
    private List<HourlyPeriod> filterByHourRange(ArrayList<HourlyPeriod> hourlyData, int startH, int endH) {
        List<HourlyPeriod> result = new ArrayList<>();
        for (HourlyPeriod hp : hourlyData) {
            int h = getHour(hp.startTime);
            if (h >= startH && h <= endH) result.add(hp);
        }
        return result;
    }

//    Get precipitation from period
    private int getPrecipitation(Period p) {
        try {
            return p.probabilityOfPrecipitation.value;
        } catch (Exception e) {
            return 0;
        }
    }

//    Get precipitation from hourly period
    private int getPrecipitation(HourlyPeriod hp) {
        try {
            return hp.probabilityOfPrecipitation.value;
        } catch (Exception e) {
            return 0;
        }
    }

//    Get humidity, return 50 if return null
    private int getHumidity(HourlyPeriod hp) {
        try {
            return hp.relativeHumidity.value;
        } catch (Exception e) {
            return 50;
        }
    }

//    Get the first daytime period
    private Period firstDaytimePeriod(ArrayList<Period> forecastData) {
        Period dayTime = null;
        for (Period p : forecastData) {
            if (p.isDaytime) {
                dayTime = p;
                break;
            }
        }
        return dayTime;
    }

//    Match keywords
    private boolean match(String sentence, String... keywords) {
        for (String key : keywords){
            if (sentence.contains(key)) return true;
        }
        return false;
    }
}
