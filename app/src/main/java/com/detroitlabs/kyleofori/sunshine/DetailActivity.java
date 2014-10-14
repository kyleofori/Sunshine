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
                builder.scheme("http").authority("www.google.com")
                        .appendPath("maps")
                        .appendPath("@" + getLatAndLonFromJson(latAndLonStr));

//            String myUrl = builder.build().toString();
                Uri uri = builder.build();
                openWebPage(uri);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void openWebPage(Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
