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


    private String getLatAndLonFromJson(String latAndLonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LAT = "lat";
        final String OWM_LON = "lon";
        final String OWM_COORD = "coord";
        final int NUM_COORDINATES_IN_A_PAIR = 2;

        JSONObject latAndLon = new JSONObject(latAndLonStr);
        JSONObject coordinates = latAndLon.getJSONObject(OWM_COORD);
        //These JSON objects are the pairs latitude: ~~~~ and longitude: ~~~~~.
        JSONObject jsonLatitude = coordinates.getJSONObject(OWM_LAT);
        JSONObject jsonLongitude = coordinates.getJSONObject(OWM_LON);
        //The following will give us those numbers that we need.
        String latitude = jsonLatitude.getString(OWM_LAT);
        String longitude = jsonLongitude.getString(OWM_LON);

        String resultString = latitude + "," + longitude;

        return resultString;
    }


    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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

            try {

                String latAndLonStr = null;

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("geo").authority("0,0")
                        .appendQueryParameter("q", getLatAndLonFromJson(latAndLonStr) + "(Preferred Location")

//            String myUrl = builder.build().toString();
                Uri geoLocation = builder.build();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
            //    "geo:0,0?q=lat,lng(Preferred Location)";


            //Should I now make strings for geoLocation?

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
