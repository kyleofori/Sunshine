package com.detroitlabs.kyleofori.sunshine;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Date;

/**
 * Created by kyleofori on 10/8/14.
 */
public class ForecastFragment extends Fragment implements FetchWeatherTask.WeatherFetchedListener {

    public static final String EXTRA_MESSAGE = "com.detroitlabs.kyleofori.sunshine.MESSAGE";

    private ArrayAdapter<String> mForecastAdapter;

    private ListView mListView;

    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu); //gMI's parameters are the menu pref_general and the menu passed in.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);

        //I found the method below in the docs.
//        switch (item.getItemId()) {
//            case R.id.refresh:
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast, //What View will be inflated for that element in array
                R.id.list_item_forecast_textview, //which View within the layout does the element of the array bind to
                new ArrayList<String>());

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


//                //even though there's no long here, we have to implement the whole method since
//                //onItemClick is an interface.
//       !!!         Context context = getActivity();
//                //Contexts are mostly used to load and access resources.
//
//                //Use getActivity() to get the context when you're
//                //in a class that extends Activity (like this one! ForecastFragment extends Fragment,
//                //which doesn't actually extend Activity but is closely associated with Activity...)
//                //CONTEXTS ARE USUALLY EITHER AN ACTIVITY OR AN APPLICATION.
//       !!!         CharSequence text = mForecastAdapter.getItem(i);  //mForecastAdapter is an instance
//                //of an AdapterView. AdapterViews are Views, meaning they fill they screen with
//                //something(s) the user sees. As a View, it can contain other Views. We are making
//                //those sub-Views clickable, to display a toast. To figure out what text should be
//                //shown in the toast, we use the "getItem()" method.
//                int duration = Toast.LENGTH_SHORT;
//
//       !!!         Toast toast = Toast.makeText(context, text, duration);
//       !!!         toast.show();


                Intent intent = new Intent(getActivity(), DetailActivity.class);
                String message = mForecastAdapter.getItem(i);
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);
            }
        }); //So all this is REALLY just part of the parameters of setOnItemClickListener.


        return rootView;
    }

    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        weatherTask.setOnWeatherFetched(this);
        //Changing the following from weatherTask.execute("Detroit") because I want
        //the local preferences to be found.
        //When I need a context, if I'm in a Fragment, apparently I must use getActivity().
//ATTEMPT 1            String name = getString(R.xml.pref_general);
//ATTEMPT 2            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
/*ATTEMPT 3*/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

//            prefs.getString(R.xml.pr)
///*ATTEMPT 4*/                     getActivity().getSharedPreferences(prefs, Context.MODE_PRIVATE);
//            weatherTask.execute(prefs.getString("location", "48214"));  //With help from B. Zabor

//Finally, redone after looking at the answer.
        weatherTask.execute(prefs.getString("location",
                getString(R.string.pref_location_default)));

        //The answer suggests:
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String location = prefs.getString(getString(R.string.pref_location_key),
//                    getString(R.string.pref_location_default));
//            weatherTask.execute(location);

        boolean isImperial = prefs.getString("temperature",     //prefs.getString() has 2 parameters
                mContext.getString(R.string.pref_temp_label)).equals("Imperial");
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void weatherReceived(String[] weatherData) {
        Log.v("Forecast Fragment", "weather received");
        mForecastAdapter.clear();
        for (String x: weatherData) {
         mForecastAdapter.add(x);
        };
    }
}