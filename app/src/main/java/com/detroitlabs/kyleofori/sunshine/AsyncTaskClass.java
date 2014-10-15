package com.detroitlabs.kyleofori.sunshine;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kyleofori on 10/14/14.
 */
public class AsyncTaskClass {
    public static String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_LAT = "lat";
        final String OWM_LON = "lon";
        final String OWM_COORD = "coord";
        String zoomLevel = "13z";

        JSONObject forecastJson = new JSONObject(forecastJsonStr); //this one is not in the JSON data.
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST); //this is found in the list object of the JSON data.

        JSONObject coordinates = forecastJson.getJSONObject(OWM_COORD);
        //These JSON objects are the pairs latitude: ~~~~ and longitude: ~~~~~.
        JSONObject jsonLatitude = coordinates.getJSONObject(OWM_LAT);
        JSONObject jsonLongitude = coordinates.getJSONObject(OWM_LON);
        //The following will give us those numbers that we need.
        String latitude = jsonLatitude.getString(OWM_LAT);
        String longitude = jsonLongitude.getString(OWM_LON);

        String resultString = latitude + "," + longitude + "," + zoomLevel;

        String[] resultStrs = new String[numDays];

        ArrayList<String> ultimateResultStrs = new ArrayList<String>();

        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            // KO - I'd like a check for the setting that we're on, which would multiply
            // the Celsius temperature by 1.8 and add 32 if the mode were imperial.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            boolean isImperial = prefs.getString("temperature",     //prefs.getString() has 2 parameters
                    getString(R.string.pref_temp_label)).equals("Imperial");
            if(isImperial) {
                high = convertToFahrenheit(high);
                low = convertToFahrenheit(low);
            }

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;

            ultimateResultStrs.add(resultStrs[i]);
        }

        ultimateResultStrs.add(resultString);
        String[] allInfo = new String[ultimateResultStrs.size()];
        return allInfo;
    }

    private static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private static String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */

    private static double convertToFahrenheit(double temperature) {
        temperature = 1.8*temperature + 32;
        return temperature;
    }
}
