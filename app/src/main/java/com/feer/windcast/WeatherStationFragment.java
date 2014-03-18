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
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;

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
    private OnFragmentInteractionListener mListener;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<WeatherStation> mAdapter;


    WeatherDataCache mCache;

    private EditText mSearchInput;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WeatherStationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();

        mAdapter = new ArrayAdapter<WeatherStation>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, stations);

        new AsyncTask<Void, Void, ArrayList<WeatherStation>>()
        {

            @Override
            protected ArrayList<WeatherStation> doInBackground(Void... params)
            {
                return mCache.GetWeatherStations();
            }

            @Override
            protected void onPostExecute(ArrayList<WeatherStation> cacheStations)
            {
                stations.addAll(cacheStations);
                mAdapter.notifyDataSetChanged();
                Log.i(TAG, "Finished adding new stations.");
            }
        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherstation, container, false);

        // Set the adapter
        /*
          The fragment's ListView/GridView.
        */
        AbsListView mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        InitializeSearchBox(view);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;


            mCache = new WeatherDataCache(activity.getResources());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        WeatherStation station = mAdapter.getItem(position);
        WeatherStationSelected(station);
    }

    private void WeatherStationSelected(WeatherStation station)
    {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(station);
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
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {

        public void onFragmentInteraction(WeatherStation station);
    }

}
