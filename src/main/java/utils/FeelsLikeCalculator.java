package utils;

//Calculate feels like temperature based on NWS standards
public final class FeelsLikeCalculator {

    private FeelsLikeCalculator() {}

//    Temperature in F
//    relative humidity (0-100)
//    wind in MPH
    public static double calculateFeelsLike(double temperature, double relativeHumidity, double windSpeedMph) {
//        Winter logic: Wind chills when temp <= 50 and wind > 3 mph
        if (temperature <= 50 && windSpeedMph > 3) {
            return 35.74 + (0.6215 * temperature)
                    - (35.75 * Math.pow(windSpeedMph, 0.16))
                    + (0.4275 * temperature * Math.pow(windSpeedMph, 0.16));
        }

//       Summer Logic: Heat Index when temp >= 80
        if (temperature >= 80) {
//            Simple Heat Index formula
            double heatIndex = 0.5 * (temperature + 61.0 + ((temperature - 68.0) * 1.2) + (relativeHumidity * 0.094));

            // If the SHI is >= 80, use the full Rothfusz regression
            if (heatIndex >= 80) {
                heatIndex = -42.379 + 2.04901523 * temperature + 10.14333127 * relativeHumidity
                        - 0.22475541 * temperature * relativeHumidity
                        - 0.00683783 * temperature * temperature
                        - 0.05481717 * relativeHumidity * relativeHumidity
                        + 0.00122874 * temperature * temperature * relativeHumidity
                        + 0.00085282 * temperature * relativeHumidity * relativeHumidity
                        - 0.00000199 * temperature * temperature * relativeHumidity * relativeHumidity;
                if (temperature >= 80 && temperature <= 112 && relativeHumidity < 13) {
                    double adj = ((13.0 - relativeHumidity) / 4.0) * Math.sqrt((17.0 - Math.abs(temperature - 95.0)) / 17.0);
                    heatIndex -= adj;
                }
                else if (temperature >= 80 && temperature <= 87 && relativeHumidity > 85) {
                    double adj = ((relativeHumidity - 85.0) / 10.0) * ((87.0 - temperature) / 5.0);
                    heatIndex += adj;
                }
            }
            return heatIndex;
        }

//        Default logic: If the temp is between 51 - 79, return actual temp
        return temperature;
    }
}