package com.detroitlabs.kyleofori.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
//        if (id == R.id.action_see_preferred_location) {
//            Intent i = new Intent();
//            i.setAction(i.ACTION_VIEW);
////            i.putExtra(i.EXTRA_TEXT, ___); //the string that goes here should be the coordinates...or the Uri.
//
//            //Verify that the intent will resolve to an activity.
//            if (i.resolveActivity(getPackageManager()) != null) {
//                startActivity(i);
//            }
//            else {
//                Log.e("MainActivity", "We can't open any internet"); //I'm sure this shouldn't be hard-coded.
//            }
//        }

        if (id == R.id.action_see_preferred_location) {

            //10/17 I'm removing out the try-catch until I get the use of JSON anything back.
            String latAndLonStr = null;

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority("www.google.com")
                    .appendPath("maps");
//                        .appendPath("@" + getLatAndLonFromJson(latAndLonStr));

//            String myUrl = builder.build().toString();
            Uri uri = builder.build();
            openWebPage(uri);
        }
        return super.onOptionsItemSelected(item);
    }

    public void openWebPage(Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}