package com.feer.windcast;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;

import com.feer.windcast.dataAccess.BackgroundTaskManager;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.LoadedWeatherStationCache;
import com.feer.windcast.dataAccess.WeatherDataCache;

import java.util.ArrayList;
import java.util.Collection;

import static android.support.v4.app.ActivityCompat.invalidateOptionsMenu;
import static com.feer.windcast.EmptyDataError.*;
import static com.feer.windcast.EmptyDataError.EmptyTextState.LoadingData;
import static com.feer.windcast.EmptyDataError.EmptyTextState.NoFavourites;
import static com.feer.windcast.EmptyDataError.EmptyTextState.NoInternetAccess;
import static com.feer.windcast.EmptyDataError.EmptyTextState.NoResultsAfterFilter;
import static com.feer.windcast.EmptyDataError.EmptyTextState.NoStationsAvailable;

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the callbacks
 * interface.
 */
public class WeatherStationFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG = "WeatherStationFragment" ;
    private static final String PARAM_STATIONS_TO_SHOW = "param_stations_to_show";
    private OnWeatherStationFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView with
     * Views.
     */
    private WeatherStationArrayAdapter mAdapter;
    private  StationsToShow mShowOnlyStations = null; //set to favs if some exist else all

    private BackgroundTaskManager mTaskManager  = null;

    private EditText mSearchInput;
    private FavouriteStationCache mFavs = null;
    private boolean mStationsExist = false;
    private TextView mEmptyView = null;


    private EmptyDataError mEmptyTextEnum = new EmptyDataError(new OnEmptyListReasonChanged() {
        @Override
        public void onEmptyListReasonChanged(EmptyTextState newReason) {
            WeatherStationFragment.this.SetEmptyTextViewContents(newReason);
        }
    });

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherStationFragment() {
        mTaskManager = BackgroundTaskManager.GetTaskManager();
    }

    public static enum StationsToShow
    {
        All,
        Favourites,
        ACT,
        NSW,
        NT,
        QLD,
        SA,
        TAS,
        VIC,
        WA
    }

    public static WeatherStationFragment newInstance(StationsToShow showstations)
    {
        Bundle bundle = new Bundle();
        WeatherStationFragment.writeBundle(bundle, showstations);

        WeatherStationFragment frag = new WeatherStationFragment();
        frag.setArguments(bundle);
        return frag;
    }

    private void readBundle(Bundle savedInstanceState)
    {
        int enumAsInt = savedInstanceState.getInt(
                PARAM_STATIONS_TO_SHOW, StationsToShow.All.ordinal());

        mShowOnlyStations = StationsToShow.values()[enumAsInt];
    }
    private static void writeBundle(Bundle saveInstanceState, StationsToShow showStations)
    {
        if(showStations != null)
        {
            saveInstanceState.putInt(PARAM_STATIONS_TO_SHOW, showStations.ordinal());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        if(savedInstanceState != null) readBundle(savedInstanceState);
        else if (getArguments() != null) readBundle(getArguments());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(savedInstanceState != null) readBundle(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_weatherstation_list, container, false);

        WeatherStationArrayAdapter.OnFavouriteChangedListener handleFavChange = new WeatherStationArrayAdapter.OnFavouriteChangedListener()
        {
            @Override
            public void OnFavouriteChanged(final WeatherStation station)
            {
                if(station.IsFavourite) {
                    mFavs.AddFavouriteStation(station);
                } else {
                    mFavs.RemoveFavouriteStation(station);
                }
            }
        };



        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String unitTypePrefValue = preferences.getString(SettingsActivity.PREF_KEY_WIND_SPEED_UNIT, "0");
        boolean useKmh = SettingsActivity.WindSpeedUnitPref.GetUnitTypeFromValue(Integer.parseInt(unitTypePrefValue)) == SettingsActivity.WindSpeedUnitPref.UnitType.kmh;

        mAdapter = new WeatherStationArrayAdapter(
                getActivity(),
                R.layout.weather_station_list_item,
                new ArrayList<WeatherData>(),
                handleFavChange,
                useKmh);

        AbsListView listView = (AbsListView) view.findViewById(android.R.id.list);
        mEmptyView = (TextView) view.findViewById(android.R.id.empty); // default text is loading_station_list
        listView.setEmptyView(mEmptyView);
        listView.setAdapter(mAdapter);
        mEmptyTextEnum.AddEmptyListReason(LoadingData);

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);
        InitializeSearchBox(view);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.stations_list_menu, menu);
        if( !mStationsExist)
        {
            menu.removeItem(R.id.search);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.search:
                ToggleStationFilter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void ToggleStationFilter() {
        if(mSearchInput != null)
        {
            switch (mSearchInput.getVisibility())
            {
                case View.GONE:
                    mSearchInput.setVisibility(View.VISIBLE);
                    break;
                case View.VISIBLE:
                    mSearchInput.setText("");
                    mSearchInput.setVisibility(View.GONE);
                    mSearchInput.clearFocus();
                    break;

            }
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        mAdapter = null;
        mSearchInput = null;
        mEmptyView = null;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        writeBundle(outState, mShowOnlyStations);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnWeatherStationFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnWeatherStationFragmentInteractionListener");
        }

        mFavs = WeatherDataCache.GetInstance().CreateNewFavouriteStationAccessor();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) readBundle(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        abstract class FillStation extends AsyncTask<Void, Void, ArrayList<WeatherData>> implements WeatherDataCache.NotifyWhenStationCacheFilled {}

        WeatherDataCache.GetInstance().OnStationCacheFilled(new FillStation() {
            @Override
            public void OnCacheFilled(LoadedWeatherStationCache fullCache) {
                weatherCache = fullCache;
                execute();
            }

            public LoadedWeatherStationCache weatherCache = null;

            @Override
            protected ArrayList<WeatherData> doInBackground(Void... params) {
                Context con;
                if (getActivity() == null || (con = getActivity().getApplicationContext()) == null || weatherCache == null)
                    return null;

                mFavs.Initialise(con, mTaskManager);
                if (mShowOnlyStations == null || mShowOnlyStations == StationsToShow.All || mShowOnlyStations == StationsToShow.Favourites) {
                    return weatherCache.GetWeatherStationsFromAllStates();
                } else {
                    return weatherCache.GetWeatherStationsFrom(mShowOnlyStations.toString());
                }
            }

            @Override
            protected void onPostExecute(final ArrayList<WeatherData> cacheStations) {
                Activity act = getActivity();
                if (act == null) return;

                if (mShowOnlyStations == null) {
                    mShowOnlyStations = (mFavs == null || mFavs.GetFavouriteURLs().isEmpty()) ? StationsToShow.All : StationsToShow.Favourites;
                }

                if (mFavs != null && cacheStations != null) {
                    ArrayList<String> favs = mFavs.GetFavouriteURLs();
                    ArrayList<WeatherData> useStations = cacheStations;
                    SetFavStations(useStations, favs);
                    
                    if (mShowOnlyStations == StationsToShow.Favourites) {
                        useStations = FilterToOnlyFavs(useStations);
                        if (useStations.isEmpty()) {
                            mEmptyTextEnum.AddEmptyListReason(NoFavourites);
                        }
                        getActivity().setTitle(R.string.favourite_stations);
                    } else if (useStations.isEmpty()) {
                        mEmptyTextEnum.AddEmptyListReason(NoStationsAvailable);
                    }

                    SetStationList(useStations);
                    Log.i(TAG, "Finished adding new stations.");
                } else {
                    Boolean favsNull = mFavs == null;
                    Boolean cacheStationsNull = cacheStations == null;
                    mEmptyTextEnum.AddEmptyListReason(NoInternetAccess);

                    Log.i(TAG,
                            "Could not add new stations." +
                                    " mFavs is null: " + favsNull.toString() +
                                    " cacheStations is null: " + cacheStationsNull.toString());
                }
                mEmptyTextEnum.RemoveEmptyListReason(LoadingData);
                invalidateOptionsMenu(act);
            }
        });
    }

    private void SetEmptyTextViewContents(EmptyTextState reason) {
        Activity act;
        Resources res;
        if(mEmptyView != null && (act = getActivity()) != null && (res = act.getResources()) != null) {

            switch (reason) {
                case NoInternetAccess:
                    mEmptyView.setText(res.getText(R.string.no_internet_access));
                    break;
                case NoStationsAvailable:
                    mEmptyView.setText(res.getText(R.string.no_stations_available));
                    break;
                case LoadingData:
                    mEmptyView.setText(res.getText(R.string.loading_station_list));
                    break;
                case NoResultsAfterFilter:
                    mEmptyView.setText(R.string.filter_results_no_stations_found);
                    break;
                case NoFavourites:
                    mEmptyView.setText(R.string.no_favourites);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mTaskManager.WaitForTasksToComplete();
        mFavs = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        WeatherStationArrayAdapter adapter;
        if ( (adapter = mAdapter) != null || (adapter = (WeatherStationArrayAdapter) parent.getAdapter()) != null) {
            WeatherStation station = adapter.getItem(position).Station;
            Log.i(TAG, String.format("Selected station: %s", station.GetName()));
            try {
                WeatherStationSelected(station);
            } catch (Exception e) {
                Log.e(TAG, "Exception when selecting the station", e);
            }
        }
    }

    private void WeatherStationSelected(WeatherStation station) throws Exception
    {
        if (null != mListener) {
            mSearchInput.clearFocus();
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onWeatherStationSelected(station);
        }
        else
        {
            throw new Exception("Weather station selected, but there is no listener set.");
        }
    }

    private void SetFavStations(final Collection<WeatherData> fullListOfStations, final Collection<String> favStations)
    {
        for(WeatherData station : fullListOfStations)
        {
            station.Station.IsFavourite = (favStations.contains(station.Station.GetURL().toString()));
        }
    }

    private ArrayList<WeatherData> FilterToOnlyFavs(final ArrayList<WeatherData> fullListOfStations)
    {
        ArrayList<WeatherData> onlyFavs = new ArrayList<WeatherData>();

        for(WeatherData station : fullListOfStations)
        {
            if(station.Station.IsFavourite)
            {
                onlyFavs.add(station);
            }
        }
        return onlyFavs;
    }

    private void SetStationList(ArrayList<WeatherData> listStations)
    {
        if (mAdapter != null)
        {
            mAdapter.clear();
            mAdapter.addAll(listStations);

            // this has to be done to refresh the filter on mAdapter.
            // Otherwise the previously set filter will persist, and the list of objects returned by
            // the adapter will not change!
            if (mSearchInput != null) mSearchInput.setText("");
            mAdapter.notifyDataSetChanged();

            mStationsExist = !listStations.isEmpty();
        }
    }


    /**
     * Initializes the search box for filtering the list of weather stations
     */
    private void InitializeSearchBox(View view)
    {
        mSearchInput = (EditText)view.findViewById(R.id.weather_station_search_box);
        mSearchInput.setFocusable(true);
        mSearchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        mAdapter.getFilter().filter(charSequence);
                        if(charSequence.length() > 0)
                        {
                            mEmptyTextEnum.AddEmptyListReason(NoResultsAfterFilter);
                        }
                        else
                        {
                            mEmptyTextEnum.RemoveEmptyListReason(NoResultsAfterFilter);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }
                    @Override
                    public void afterTextChanged(Editable editable) { }
                }
        );
        mSearchInput.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (v == mSearchInput && !hasFocus) {
                            //close keyboard
                            Context context = getActivity().getApplicationContext();
                            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(
                                    InputMethodManager.SHOW_IMPLICIT, 0);
                        }
                    }
                }
        );
    }

    /**
    * Interface to allow actions to occur outside of this
    * fragment, when the user interacts with this fragment
    */
    public interface OnWeatherStationFragmentInteractionListener
    {
        public void onWeatherStationSelected(WeatherStation station);
    }

}
