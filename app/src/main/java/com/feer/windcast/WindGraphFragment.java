package com.feer.windcast;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.feer.windcast.WeatherStation.WeatherStationBuilder;
import com.feer.windcast.dataAccess.WeatherDataCache;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
/**
 * A placeholder fragment containing a simple view.
 */
public class WindGraphFragment extends Fragment
{
    private static final String TAG = "WindGraphFragment";

    private static final String PARAM_KEY_STATION_URL = "param_weatherStation_URL";
    private static final String PARAM_KEY_STATION_NAME = "param_weatherStation_NAME";
    private static final String PARAM_KEY_STATION_STATE = "param_weatherStation_STATE";


    /*This is required so the fragment can be instantiated when restoring its activity's state*/
    public WindGraphFragment() {
    }

    public static WindGraphFragment newInstance(WeatherStation station)
    {
        Bundle args = new Bundle();
        addInstanceStateToBundle(args, station);

        WindGraphFragment f = new WindGraphFragment();
        f.setArguments(args);
        return f;
    }

    private WeatherStation mStation = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if(savedInstanceState != null)
        {
            readBundle(savedInstanceState);
        }
        else if(this.getArguments() != null)
        {
            readBundle(this.getArguments());
        }

        return rootView;
    }

    private void readBundle(Bundle savedInstanceState)
    {
        Log.v(TAG, "Reading bundle");

        String urlString = savedInstanceState.getString(PARAM_KEY_STATION_URL);
        String nameString = savedInstanceState.getString(PARAM_KEY_STATION_NAME);
        String stateString = savedInstanceState.getString(PARAM_KEY_STATION_STATE);

        if(urlString != null)
        {
            Log.v(TAG, String.format("Previous Name (%s) and URL (%s)", nameString, urlString));

            try
            {
                WeatherStationBuilder builder = new WeatherStation.WeatherStationBuilder();
                builder.WithName(nameString);
                builder.WithURL(new URL(urlString));
                builder.WithState(WeatherStation.States.valueOf(stateString));
                mStation = builder.Build();
            } catch (MalformedURLException e)
            {
                Log.e("WindCast","url not valid: " + urlString + "\n\n" + e.toString());
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        populateUI();
    }

    static private String FormatStationName(WeatherStation station)
    {
        return station.GetName() + ", " + station.GetLongStateName();
    }

    private void populateUI()
    {
        final Activity act = getActivity();
        final XYPlot  plot = (XYPlot) act.findViewById(R.id.mySimpleXYPlot);
        WindGraph.FormatGraph(plot, act);

        final TextView stationNameLabel = (TextView) act.findViewById(R.id.stationNameLabel);
        stationNameLabel.setText(FormatStationName(mStation));

        new AsyncTask<Void, Void, Boolean>()
        {
            WeatherData wd;

            @Override
            protected Boolean doInBackground(Void... params)
            {
                if(mStation == null)
                {
                    Log.w(TAG, "No weather station!");
                    return false;
                }

                WeatherDataCache cache = WeatherDataCache.GetWeatherDataCache();
                Log.i("WindCast", "Getting data from: " + mStation.GetURL().toString());
                wd = cache.GetWeatherDataFor(mStation);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                Activity act = getActivity();
                // if the user hits back before this callback returns act could be null.
                // In this case we exit early.
                if (act == null) return;

                final TextView stationNameLabel = (TextView) act.findViewById(R.id.stationNameLabel);

                final TextView readingTime = (TextView) act.findViewById(R.id.readingTimeLabel);
                if(wd == null || result == false)
                {
                    readingTime.setText("Weather data not available");
                    return;
                }

                stationNameLabel.setText(FormatStationName(wd.Station));

                if(wd.ObservationData != null && !wd.ObservationData.isEmpty())
                {
                    ObservationReading reading = wd.ObservationData.get(0);

                    DateFormat localDate = android.text.format.DateFormat
                            .getDateFormat(act.getApplicationContext());

                    DateFormat localTime = android.text.format.DateFormat
                            .getTimeFormat(act.getApplicationContext());

                    readingTime.setText(
                            localTime.format(reading.LocalTime) + ' ' + localDate.format(reading.LocalTime)
                            + "\n"                                                    );

                    if(reading.WindBearing != null && reading.CardinalWindDirection != null && reading.WindSpeed_KMH != null)
                    {
                        final TextView speedLabel = (TextView)act.findViewById(R.id.latestReadingLabel);

                        if(!reading.CardinalWindDirection.equals("calm") && reading.WindSpeed_KMH > 0)
                        {
                            speedLabel.setText(reading.WindSpeed_KMH + " Km/H from " + getDirectionWordsFromChars(reading.CardinalWindDirection));
                        }
                        else
                        {
                            speedLabel.setText("Calm conditions");
                        }
                    }
                }
                else
                {
                    readingTime.setText(act.getString(R.string.no_readings));
                }

                WindGraph.SetupGraph(wd, plot, act);
                plot.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    private static String getDirectionWordsFromChars(String cardinalChars)
    {
        StringBuilder sb = new StringBuilder();
        for(char c : cardinalChars.toCharArray())
        {
            sb.append(getCardinalWordFromChar(c)).append(' ');
        }
        return sb.toString().trim();
    }

    private static String getCardinalWordFromChar(char cardinalChar)
    {
        switch (cardinalChar)
        {
            case 'n' : return "North";
            case 's' : return "South";
            case 'w' : return "West";
            case 'e' : return "East";
        }
        return "";
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if(mStation != null)
        {
            addInstanceStateToBundle(outState, mStation);
        }

    }

    static private void addInstanceStateToBundle(Bundle outState, WeatherStation station)
    {
        outState.putString(PARAM_KEY_STATION_URL, station.GetURL().toString());
        outState.putString(PARAM_KEY_STATION_NAME, station.GetName());
        outState.putString(PARAM_KEY_STATION_STATE, station.GetStateAbbreviated());
    }
}
