package com.detroitlabs.kyleofori.sunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kyleofori on 10/15/14.
 */
public class WeatherReport {

    //Constants.
    private final String[] weatherOutput;
    private final int numDays;

//    private final Context context;
//    private final SharedPreferences sharedPrefs;

    //private ForecastFragment forecastFragment;


    //Constructor. numDays could be useful for defining how many days you want on screen.
    public WeatherReport(String[] weatherOutput, int numDays) {

        this.weatherOutput = weatherOutput;
        this.numDays = numDays;
//        this.context = context;
//        sharedPrefs = context.getSharedPreferences(context.getString(R.string.pref_temp_label), Context.MODE_PRIVATE); //This should probably mean something
    }

    //"static factory method" or "builder" called FROMJSON. IT'S CALLED FROMJSON. This method returns a WeatherReport.
    public static WeatherReport fromJson(String forecastJsonStr, int numDays){

        WeatherReport weatherOutput = null;

        try {
            String[] weatherDataFromJson = getWeatherDataFromJson(forecastJsonStr, numDays);
            weatherOutput = new WeatherReport(weatherDataFromJson, numDays);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return weatherOutput; //This will be a parameter for the constructor above.
    }

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

        JSONObject forecastJson = new JSONObject(forecastJsonStr); //this one is not in the JSON data.
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST); //this is found in the list object of the JSON data.

        String[] resultStrs = new String[numDays];
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


            //WORKING ON THIS TEMPERATURE METHOD 2014-10-16
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext);
//
//            boolean isImperial = prefs.getString("temperature",     //prefs.getString() has 2 parameters
//                    mContext.getString(R.string.pref_temp_label)).equals("Imperial");


            //WOULD MAKING A NEW FORECAST FRAGMENT WORK?
//            ForecastFragment forecastFragment = new ForecastFragment();
//            if (forecastFragment.) {
//            Context mContext = forecastFragment.getActivity();
//
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
//
//

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }


    private static String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(ForecastFragment.getActivity());

        String unitType = sharedPrefs.getString(
                "temperature",
                        "metric"); //I shouldn't have to hard code this.

//        boolean isImperial = sharedPrefs.getString("temperature",     //prefs.getString() has 2 parameters
//                getString(R.string.pref_temp_label)).equals("Imperial");
//
        if (unitType.equals("imperial")) { //Shouldn't have to hard code this either, but getString() not working

            high = convertToFahrenheit(high);
            low = convertToFahrenheit(low);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public static String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    public static double convertToFahrenheit(double temperature) {
        temperature = 1.8 * temperature + 32;
        return temperature;
    }

}
