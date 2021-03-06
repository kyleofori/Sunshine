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
import java.util.Date;

/**
 * Created by kyleofori on 10/8/14.
 */
public class ForecastFragment extends Fragment {

    public static final String EXTRA_MESSAGE = "com.detroitlabs.kyleofori.sunshine.MESSAGE";

    private ArrayAdapter<String> mForecastAdapter;

    private ListView mListView;

    public ForecastFragment () {
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
        if(id == R.id.refresh) {
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
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        //Changing the following from weatherTask.execute("Detroit") because I want
        //the local preferences to be found.
        //When I need a context, if I'm in a Fragment, apparently I must use getActivity().
//ATTEMPT 1            String name = getString(R.xml.pref_general);
//ATTEMPT 2            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
/*ATTEMPT 3*/          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName(); //needs to match name of class

        public FetchWeatherTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            mForecastAdapter.clear();
            mForecastAdapter.addAll(strings);
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */


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

                gWDFJ = AsyncTaskClass.getWeatherDataFromJson(forecastJsonStr, 7);

            }
            catch (JSONException e){
                e.printStackTrace();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
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

    }
}
