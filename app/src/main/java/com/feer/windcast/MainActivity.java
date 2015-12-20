package com.feer.windcast;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements WeatherStationFragment.OnWeatherStationFragmentInteractionListener
{

    private static final String TAG = "MainActivity";
    private static final String STATIONS_FRAG_TAG = "stationsFrag";
    private CharSequence mTitle;

    private WindCastNavigationDrawer mDrawerToggle;
    private WeatherStationFragment mWeatherStationFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = super.getTitle();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new WindCastNavigationDrawer(this, new WindCastNavigationDrawer.NavigationDrawerInteraction() {
            @Override
            public void OnAllStatesClicked() {
                ReplaceStationListFragment(
                        WeatherStationFragment.StationsToShow.All,
                        getResources().getString(R.string.app_name)
                );
            }

            @Override
            public void OnFavouriteStationsClicked() {
                ReplaceStationListFragment(
                        WeatherStationFragment.StationsToShow.Favourites,
                        getResources().getString(R.string.favourite_stations)
                );
            }

            @Override
            public void OnStateClicked(WeatherStationFragment.StationsToShow stateClicked) {
                ReplaceStationListFragment(
                        stateClicked,
                        getResources().getString(R.string.wind_stations_in, stateClicked.toString())
                );
            }
        });

        mWeatherStationFrag = (WeatherStationFragment)getSupportFragmentManager().findFragmentByTag(STATIONS_FRAG_TAG);

        if(mWeatherStationFrag == null) {
            Log.v(TAG, "Recreating weather station fragment");
            mWeatherStationFrag = new WeatherStationFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, mWeatherStationFrag, STATIONS_FRAG_TAG).commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public void onWeatherStationSelected(AWeatherStation station)
    {
        startActivity(ShowGraphActivity.getShowGraphForStationIntent(this, station));
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private void ReplaceStationListFragment(WeatherStationFragment.StationsToShow filter, String title)
    {
        Log.i(TAG, "Replace station list fragment: " + filter.toString());

        mWeatherStationFrag = WeatherStationFragment.newInstance(filter);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, mWeatherStationFrag, STATIONS_FRAG_TAG).commit();
        setTitle(title);
    }


}

