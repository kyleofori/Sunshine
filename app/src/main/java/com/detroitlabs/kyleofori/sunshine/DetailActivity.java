package com.detroitlabs.kyleofori.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }


    private String[] getLatAndLonFromJson (String latAndLonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LAT = "lat";
        final String OWM_LON = "lon";
        final String OWM_COORD = "coord";
        final int NUM_COORDINATES_IN_A_PAIR = 2;

        JSONObject latAndLon = new JSONObject(latAndLonStr);
        JSONObject coordinates = latAndLon.getJSONObject(OWM_COORD);
        JSONObject latitude = coordinates.getJSONObject(OWM_LAT);
        JSONObject longitude = coordinates.getJSONObject(OWM_LON);


        String[] coordinates = new String[NUM_COORDINATES_IN_A_PAIR];
        for (int i = 0; i < coordinatesArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String latCoordinate;
            String lonCoordinate;

            // Get the JSON object representing the day
            JSONObject latitude = weatherArray.getJSONObject(i);
            JSONObject longitude =

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
        }

        return resultStrs;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.pref_general.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_see_preferred_location) {



            Uri.Builder builder = new Uri.Builder();
            builder.scheme("geo").authority("0,0")
                    .appendQueryParameter("q", "lat,lng"+"(Preferred Location")

//            String myUrl = builder.build().toString();
            Uri geoLocation = builder.build();

            "geo:0,0?q=lat,lng(Preferred Location)";


            //Should I now make strings for geoLocation?
            public void showMap(Uri geoLocation) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            Intent intent = getActivity().getIntent();

            //BELOW IS WHAT I TRIED FIRST. I later learned that it'd be better to name the view that
            //already exists in fragment_detail.pref_general and change the text from DetailActivity.class than
            //to try and make a whole new TextView.
            //The aforementioned view has now been named detail_text.
//        TextView textView = new TextView(this); //The parameter for a TextView is Context.
//        textView.setText(message);
//        setContentView(textView); //to get the message on the screen.


            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            if(intent != null && intent.hasExtra(ForecastFragment.EXTRA_MESSAGE)) {
                String message = intent.getStringExtra(ForecastFragment.EXTRA_MESSAGE);
                TextView textView = (TextView) rootView.findViewById(R.id.detail_text);
                textView.setText(message);
            }
            return rootView;
        }
    }
}
