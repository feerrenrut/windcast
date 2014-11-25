package com.feer.windcast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by Reef on 24/11/2014.
 */
public class WindCastNavigationDrawer extends ActionBarDrawerToggle {


    public static final String PREFS_NAVIGATION_DRAWER_OPENED = "navigation drawer opened";
    private static final String TAG = "NavigationDrawer";

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerStatesList;
    private LinearLayout mDrawerContents;
    private String[] mAustralianStates;
    private NavigationDrawerInteraction mNavInteraction;

    private Activity mAct;

    public WindCastNavigationDrawer(Activity act, NavigationDrawerInteraction navInteraction)
    {
        super(act, (DrawerLayout) act.findViewById(R.id.drawer_layout), R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close);

        mAct = act;
        mNavInteraction = navInteraction;
        mDrawerLayout = (DrawerLayout) mAct.findViewById(R.id.drawer_layout);
        mDrawerContents = (LinearLayout) mAct.findViewById(R.id.drawer_contents);
        mDrawerStatesList = (ListView) mAct.findViewById(R.id.drawer_states_list);

        mAustralianStates = mAct.getResources().getStringArray(R.array.AustralianStates);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mAct,
                R.layout.drawer_list_item, mAustralianStates
        );
        mDrawerStatesList.setAdapter(adapter);
        mDrawerLayout.setDrawerListener(this);

        SetupClickListeners();

        // hide list of states by default
        mDrawerStatesList.setVisibility(View.INVISIBLE);

        // open drawer if it has not yet been opened manually
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAct);
        final boolean hasDrawerBeenOpened = sp.getBoolean(PREFS_NAVIGATION_DRAWER_OPENED, false);
        if(!hasDrawerBeenOpened) {
            mDrawerLayout.openDrawer(mDrawerContents);
        }

    }

    @Override
    public void onDrawerClosed(View drawerView)
    {
        super.onDrawerClosed(drawerView);
        mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
    }

    /** Called when a drawer has settled in a completely open state. */
    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        mAct.invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAct);

        SharedPreferences.Editor e = sp.edit();

        e.putBoolean(PREFS_NAVIGATION_DRAWER_OPENED, true);
        e.commit();
    }

    private void SetupClickListeners() {
        // handle clicking on a state
        mDrawerStatesList.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id)
            {
                String itemText = mAustralianStates[position];
                Log.i(TAG, String.format("Selecting drawer item: %s", itemText));
                mDrawerStatesList.setItemChecked(position, true);
                mNavInteraction.OnStateClicked(WeatherStationFragment.StationsToShow.valueOf(itemText));

                mDrawerLayout.closeDrawer(mDrawerContents);
            }
        });

        // handle clicking on Favourite stations
        mAct.findViewById(R.id.drawer_favs_layout).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                mNavInteraction.OnFavouriteStationsClicked();

                mDrawerLayout.closeDrawer(mDrawerContents);
            }
        });

        // handle clicking on All stations
        mAct.findViewById(R.id.drawer_all_layout).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(true);
                mNavInteraction.OnAllStatesClicked();

                mDrawerLayout.closeDrawer(mDrawerContents);
            }
        });

        // swap down/up arrow depending on whether states list is shown or not
        final ImageView statesImage = (ImageView) mAct.findViewById(R.id.drawer_states_image);
        mAct.findViewById(R.id.drawer_states_layout).setOnClickListener( new View.OnClickListener()
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
        } );
    }

    public interface NavigationDrawerInteraction
    {
        void OnAllStatesClicked();
        void OnFavouriteStationsClicked();
        void OnStateClicked(WeatherStationFragment.StationsToShow stateClicked);
    }
}
