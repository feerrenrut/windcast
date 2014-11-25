package com.feer.windcast;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.net.URL;


public class ShowGraphActivity extends ActionBarActivity {

    private static final String KEY_STATION_NAME = "StationName";
    private static final String KEY_STATION_URL = "StationURL";
    private static final String KEY_STATION_STATE = "StationState";

    static Intent getShowGraphForStationIntent(Context context, WeatherStation station)
    {
        Intent graphIntent = new Intent(context, ShowGraphActivity.class);
        graphIntent.putExtra(KEY_STATION_NAME, station.GetName());
        graphIntent.putExtra(KEY_STATION_URL, station.GetURL().toString());
        graphIntent.putExtra(KEY_STATION_STATE, station.GetStateAbbreviated());

        return graphIntent;
    }

    private static WeatherStation getStationFromIntent(Intent intent) throws MalformedURLException {

        return new WeatherStation.WeatherStationBuilder()
                .WithName(intent.getStringExtra(KEY_STATION_NAME))
                .WithURL(new URL(intent.getStringExtra(KEY_STATION_URL)))
                .WithState(WeatherStation.States.valueOf(intent.getStringExtra(KEY_STATION_STATE)))
                .Build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_graph);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            WindGraphFragment graphFragment = WindGraphFragment.newInstance(
                    getStationFromIntent(getIntent()));

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_frame, graphFragment).commit();
            setTitle(R.string.app_name);
        } catch (MalformedURLException e) {
            Log.e("ShowGraph", "Error creating fragment from bundle", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
         switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
