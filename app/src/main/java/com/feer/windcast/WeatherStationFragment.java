package com.feer.windcast;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;

import com.feer.windcast.database.FavouriteStation;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the callbacks
 * interface.
 */
public class WeatherStationFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static final String TAG = "WeatherStationFragment" ;
    private static final String PARAM_STATIONS_TO_SHOW = "param_stations_to_show";
    private OnWeatherStationFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private WeatherStationArrayAdapter mAdapter;
    private  StationsToShow mShowOnlyStations = StationsToShow.All;


    WeatherDataCache mCache;

    private EditText mSearchInput;
    private FavouriteStation mFavs = null;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherStationFragment() {
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
        WA;
    }

    public static WeatherStationFragment NewWeatherStation(StationsToShow showstations)
    {

        Bundle bundle = new Bundle();
        WeatherStationFragment frag = new WeatherStationFragment();
        frag.writeBundle(bundle, showstations);
        frag.setArguments(bundle);
        return frag;
    }

    private void readBundle(Bundle savedInstanceState)
    {
        Log.v(TAG, "Reading bundle");

        int enumAsInt = savedInstanceState.getInt(
                PARAM_STATIONS_TO_SHOW, StationsToShow.All.ordinal());

        mShowOnlyStations = StationsToShow.values()[enumAsInt];
    }
    private static void writeBundle(Bundle saveInstanceState, StationsToShow showStations)
    {
        Log.v(TAG, "writing bundle");
        saveInstanceState.putInt(PARAM_STATIONS_TO_SHOW, showStations.ordinal());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) readBundle(savedInstanceState);
        else if (getArguments() != null) readBundle(getArguments());

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherstation, container, false);

        mFavs = new FavouriteStation(getActivity());

        WeatherStationArrayAdapter.OnFavouriteChangedListener handleFavChange = new WeatherStationArrayAdapter.OnFavouriteChangedListener()
        {
            @Override
            public void OnFavouriteChanged(final WeatherStation station)
            {
                if(station.IsFavourite) {
                    mFavs.AddFavouriteStationAsync(station);
                } else {
                    mFavs.RemoveFavouriteStationAsync(station);
                }
            }
        };

        mAdapter = new WeatherStationArrayAdapter(
                getActivity(),
                R.layout.weather_station_list_item,
                new ArrayList<WeatherStation>(),
                handleFavChange);

        AbsListView listView = (AbsListView) view.findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);

        new AsyncTask<Void, Void, ArrayList<WeatherStation>>()
        {

            @Override
            protected ArrayList<WeatherStation> doInBackground(Void... params)
            {
                if(mShowOnlyStations == StationsToShow.All || mShowOnlyStations == StationsToShow.Favourites)
                {
                    return mCache.GetWeatherStationsFromAllStates();
                }
                else
                {
                    return mCache.GetWeatherStationsFrom(mShowOnlyStations.toString());
                }
            }

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            protected void onPostExecute(final ArrayList<WeatherStation> cacheStations)
            {
                if(mFavs!=null)
                {
                    mFavs.GetFavouritesAsync(
                            new FavouriteStation.AsyncComplete<ArrayList<WeatherStation>>()
                            {
                                @Override
                                public void OnAsyncComplete(ArrayList<WeatherStation> favs)
                                {
                                    ArrayList<WeatherStation> useStations = cacheStations;
                                    SetFavStations(useStations, favs);
                                    if (mShowOnlyStations == StationsToShow.Favourites)
                                    {
                                        SetFavStations(useStations, favs);
                                        useStations = FilterToOnlyFavs(useStations);
                                    }
                                    SetStationList(useStations);

                                    Log.i(TAG, "Finished adding new stations.");
                                }
                            }
                                            );
                }
            }
        }.execute();

        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(this);
        InitializeSearchBox(view);

        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        mAdapter = null;
        mSearchInput = null;
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


            mCache = WeatherDataCache.GetWeatherDataCache();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnWeatherStationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        mFavs.WaitForTasksComplete();
        mFavs = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        WeatherStation station = mAdapter.getItem(position);
        Log.i(TAG, String.format("Selected station: %s", station.Name));
        try
        {
            WeatherStationSelected(station);
        } catch (Exception e)
        {
            Log.e(TAG, "Exception when selecting the station",e);
        }
    }

    private void WeatherStationSelected(WeatherStation station) throws Exception
    {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onWeatherStationSelected(station);
        }
        else
        {
            throw new Exception("Weather station selected, but there is no listener set.");
        }
    }

    private void SetFavStations(final Collection<WeatherStation> fullListOfStations, final Collection<WeatherStation> favStations)
    {
        for(WeatherStation station : fullListOfStations)
        {
            station.IsFavourite = (favStations.contains(station));
        }
    }

    private ArrayList<WeatherStation> FilterToOnlyFavs(final ArrayList<WeatherStation> fullListOfStations)
    {
        ArrayList<WeatherStation> onlyFavs = new ArrayList<WeatherStation>();

        for(WeatherStation station : fullListOfStations)
        {
            if(station.IsFavourite)
            {
                onlyFavs.add(station);
            }
        }
        return onlyFavs;
    }

    private void SetStationList(ArrayList<WeatherStation> listStations)
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
        }
    }


    /**
     * Initializes the search box for filtering the list of weather stations
     */
    private void InitializeSearchBox(View view)
    {
        mSearchInput = (EditText)view.findViewById(R.id.weather_station_search_box);

        mSearchInput.addTextChangedListener(
                new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                    {
                        mAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
                    {
                    }

                    @Override
                    public void afterTextChanged(Editable editable)
                    {
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
