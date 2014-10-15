package com.detroitlabs.kyleofori.sunshine;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kyleofori on 10/14/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName(); //needs to match name of class
    private Context mContext;

    public interface WeatherFetchedListener {
        public void weatherReceived(String[] weatherData);
    }


    private WeatherFetchedListener onWeatherFetchedListener;



    public FetchWeatherTask() {
        super();
    }

    public FetchWeatherTask(Context context) {
        super();
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
        onWeatherFetchedListener.weatherReceived(strings);

    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     */

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        ArrayList<String> resultsArrayList = new ArrayList<String>();

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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            boolean isImperial = prefs.getString("temperature",     //prefs.getString() has 2 parameters
                    mContext.getString(R.string.pref_temp_label)).equals("Imperial");
            if (isImperial) {
                high = convertToFahrenheit(high);
                low = convertToFahrenheit(low);
            }

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }



         /* Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */

    public double convertToFahrenheit(double temperature) {
        temperature = 1.8 * temperature + 32;
        return temperature;
    }

    @Override
    protected String[] doInBackground(String... postcode) {
        if (postcode.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String[] gWDFJ = null;


        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast

            //I THINK THE URIBUILDER WILL GO HERE.

            //After talking to Bryan Kelly
//                Uri.Builder myUriBuilder = new Uri.Builder();
//                myUriBuilder.appendPath("?q=Detroit");
//                myUriBuilder.appendPath("&mode=json");
//                myUriBuilder.appendPath("&units=metric");
//                myUriBuilder.appendPath("&cnt=7");
//                myUriBuilder.build();

            //after checking StackOverflow
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q", postcode[0])
                    .appendQueryParameter("mode", "json")
                    .appendQueryParameter("units", "metric")
                    .appendQueryParameter("cnt", "7");

            String myUrl = builder.build().toString();

            URL url = new URL(myUrl);
//                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=Detroit&mode=json&units=metric&cnt=7");

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            forecastJsonStr = buffer.toString();

            gWDFJ = getWeatherDataFromJson(forecastJsonStr, 7);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return gWDFJ; //onPostExecute() knows what I will return from this.
    }

    public void setOnWeatherFetched(WeatherFetchedListener onWeatherFetchedListener) {
        this.onWeatherFetchedListener = onWeatherFetchedListener;
    }
}