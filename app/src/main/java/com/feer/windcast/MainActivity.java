package com.feer.windcast;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements WeatherStationFragment.OnWeatherStationFragmentInteractionListener
{

    private DrawerLayout mDrawerLayout;
    private String[] mDrawerOptions;
    private ListView mDrawerList;
    private WeatherStationFragment mStationsFragment;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerOptions = getResources().getStringArray(R.array.drawer_options);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.windcast_drawer);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerOptions);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        if (savedInstanceState == null) {
            mStationsFragment = new WeatherStationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, mStationsFragment).commit();
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
            if(itemText.equals("All"))
            {
                mStationsFragment.ShowAllStations();
                setTitle(getString(R.string.app_name));
            }
            else
            {
                mStationsFragment.ShowOnlyStationsInState(itemText);
                setTitle(String.format(getString(R.string.wind_stations_in), itemText));
            }


            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
}

