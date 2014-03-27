package com.feer.windcast;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements WeatherStationFragment.OnWeatherStationFragmentInteractionListener
{

    private static final String TAG = "MainActivity";
    private static final String STATIONS_FRAG_TAG = "stationsFrag";
    private DrawerLayout mDrawerLayout;
    private String[] mDrawerOptions;
    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerOptions = getResources().getStringArray(R.array.drawer_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.windcast_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        mDrawer = (LinearLayout) findViewById(R.id.left_drawer);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerOptions);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        Log.i(TAG, "no saved instance state, recreating weather station fragment");
        WeatherStationFragment stationsFragment = new WeatherStationFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content_frame, stationsFragment, STATIONS_FRAG_TAG).commit();
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onWeatherStationSelected(WeatherStation station)
    {
        //Todo this should probably launch a new activity.
        // the graph shows up as transparent over the top of the list. Back action exist the app!
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.content_frame, new WindGraphFragment(station));
        trans.addToBackStack(null);
        trans.commit();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id)
        {
            String itemText = mDrawerOptions[position];
            Log.i(TAG, String.format("Selecting drawer item: %s", itemText));

            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawer);

            FragmentManager fm = getSupportFragmentManager();
            int numBackStacks = fm.getBackStackEntryCount();
            Log.i(TAG, String.format("Number of transitions in back stack: %s", numBackStacks));

            Fragment oldStationsFrag = fm.findFragmentByTag(STATIONS_FRAG_TAG);
            if(oldStationsFrag != null)
            {
                Log.i(TAG, "Found old stations fragment. Removing last transition. Expect that the number of transitions in the back stack was 1.");
                fm.popBackStack();
            }

            WeatherStationFragment stationsFragment = new WeatherStationFragment();

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_frame, stationsFragment, STATIONS_FRAG_TAG).commit();

            if(itemText.equals("All"))
            {
                stationsFragment.ShowAllStations();
                setTitle(getString(R.string.app_name));
            }
            else
            {
                stationsFragment.ShowOnlyStationsInState(itemText);
                setTitle(String.format(getString(R.string.wind_stations_in), itemText));
            }
        }
    }
}

