package com.feer.windcast;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements WeatherStationFragment.OnWeatherStationFragmentInteractionListener
{

    private static final String TAG = "MainActivity";
    private static final String STATIONS_FRAG_TAG = "stationsFrag";
    private static final String GRAPH_FRAG_TAG = "graphFrag";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mAustralianStates;
    private ListView mDrawerStatesList;
    private LinearLayout mDrawerContents;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerContents = (LinearLayout) findViewById(R.id.drawer_contents);

        mDrawerStatesList = (ListView) findViewById(R.id.drawer_states_list);
        mTitle = super.getTitle();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            @Override
            public void onDrawerClosed(View drawerView)
            {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mAustralianStates = getResources().getStringArray(R.array.AustralianStates);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mAustralianStates
        );

        mDrawerStatesList.setAdapter(adapter);
        mDrawerStatesList.setOnItemClickListener(new DrawerItemClickListener());

        class ReplaceStationsOnClick implements View.OnClickListener
        {
            final WeatherStationFragment.StationsToShow mStationsToShow;
            final String mNewTitle;

            ReplaceStationsOnClick(WeatherStationFragment.StationsToShow showStations, String newTitle)
            {
                mStationsToShow = showStations;
                mNewTitle = newTitle;
            }

            @Override
            public void onClick(View v)
            {
                v.setSelected(true);
                ReplaceStationListFragment(
                        mStationsToShow,
                        mNewTitle);
            }
        }

        ReplaceStationsOnClick favsClicked = new ReplaceStationsOnClick(
                WeatherStationFragment.StationsToShow.Favourites,
                getResources().getString(R.string.favourite_stations));
        findViewById(R.id.drawer_favs_layout).setOnClickListener(favsClicked);

        ReplaceStationsOnClick allClicked = new ReplaceStationsOnClick(
                WeatherStationFragment.StationsToShow.All,
                getResources().getString(R.string.app_name));
        findViewById(R.id.drawer_all_layout).setOnClickListener(allClicked);

        final ImageView statesImage = (ImageView) findViewById(R.id.drawer_states_image);
        View.OnClickListener toggleStatesArray = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mDrawerStatesList.getVisibility() == View.VISIBLE) {
                    mDrawerStatesList.setVisibility(View.INVISIBLE);
                    statesImage.setImageResource(R.drawable.down_arrow);
                }
                else {
                    mDrawerStatesList.setVisibility(View.VISIBLE);
                    statesImage.setImageResource(R.drawable.up_arrow);
                }
            }
        };

        findViewById(R.id.drawer_states_layout).setOnClickListener(toggleStatesArray);
        mDrawerStatesList.setVisibility(View.INVISIBLE);

        if(getSupportFragmentManager().findFragmentByTag(STATIONS_FRAG_TAG) == null)
        {
            Log.i(TAG, "no pre-existing GRAPH_FRAG_TAG, recreating weather station fragment");
            WeatherStationFragment stationsFragment = new WeatherStationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, stationsFragment, STATIONS_FRAG_TAG).commit();
        }
        else
        {
            Log.i(TAG, "Pre-existing GRAPH_FRAG_TAG, not recreating weather station fragment");
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
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onWeatherStationSelected(WeatherStation station)
    {
        //Todo this should probably launch a new activity.
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.content_frame, WindGraphFragment.newInstance(station), GRAPH_FRAG_TAG);
        trans.addToBackStack(null);
        trans.commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    private void ReplaceStationListFragment(WeatherStationFragment.StationsToShow filter, String title)
    {
        mDrawerLayout.closeDrawer(mDrawerContents);
        FragmentManager fm = getSupportFragmentManager();
        int numBackStacks = fm.getBackStackEntryCount();
        Log.i(TAG, String.format("Number of transitions in back stack: %s", numBackStacks));

        Fragment oldStationsFrag = fm.findFragmentByTag(STATIONS_FRAG_TAG);
        if(oldStationsFrag != null)
        {
            Log.i(TAG, "Found old stations fragment. Removing last transition. Expect that the number of transitions in the back stack was 1.");
            fm.popBackStack();
        }

        WeatherStationFragment stationsFragment = WeatherStationFragment.NewWeatherStation(filter);

        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, stationsFragment, STATIONS_FRAG_TAG).commit();
        setTitle(title);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            String itemText = mAustralianStates[position];
            Log.i(TAG, String.format("Selecting drawer item: %s", itemText));

            mDrawerStatesList.setItemChecked(position, true);

            ReplaceStationListFragment(
                    WeatherStationFragment.StationsToShow.valueOf(itemText),
                    String.format(getString(R.string.wind_stations_in), itemText));
        }
    }
}

