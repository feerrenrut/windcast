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

    private WeatherStation mStation = new WeatherStation();

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

        if(urlString != null)
        {
            Log.v(TAG, String.format("Previous Name (%s) and URL (%s)", nameString, urlString));

            mStation = new WeatherStation();
            try
            {
                mStation.url = new URL(urlString);
                mStation.Name = nameString;
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

    private void populateUI()
    {

        final Activity act = getActivity();
        final XYPlot  plot = (XYPlot) act.findViewById(R.id.mySimpleXYPlot);
        WindGraph.FormatGraph(plot, act);
        new AsyncTask<Void, Void, Boolean>()
        {
            WeatherData wd;

            @Override
            protected Boolean doInBackground(Void... params)
            {
                WeatherDataCache cache = WeatherDataCache.GetWeatherDataCache();

                URL url;
                if(mStation != null)
                {
                    url = mStation.url;
                }
                else
                {
                    Log.w(TAG, "No weather station set using first one!");
                    //TODO let the user know in a nice way!!
                    return false;
                }
                Log.i("WindCast", "Getting data from: " + url.toString());
                wd = cache.GetWeatherDataFor(url);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                Activity act = getActivity();
                // if the user hits back before this callback returns act could be null.
                // In this case we exit early.
                if (act == null) return;

                //TODO check the result, let the user know if we don't know what data to show them.
                final TextView stationNameLabel = (TextView) act.findViewById(R.id.stationNameLabel);

                if(wd == null)
                {
                    stationNameLabel.setText("Weather data is null!");
                }else
                {
                    stationNameLabel.setText(wd.Station.Name + ", " + wd.Station.State);

                    if(wd.ObservationData != null && !wd.ObservationData.isEmpty())
                    {
                        ObservationReading reading = wd.ObservationData.get(0);

                        DateFormat localDate = android.text.format.DateFormat
                                .getDateFormat(act.getApplicationContext());

                        DateFormat localTime = android.text.format.DateFormat
                                .getTimeFormat(act.getApplicationContext());

                        ((TextView) act.findViewById(R.id.readingTimeLabel)).setText(
                                localTime.format(reading.LocalTime) + ' ' + localDate.format(reading.LocalTime)
                                + "\n"                                                    );

                        if(reading.WindBearing != null && reading.CardinalWindDirection != null && reading.WindSpeed_KMH != null)
                        {
                            TextView speedLabel = (TextView)act.findViewById(R.id.latestReadingLabel);

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
                        ((TextView)act.findViewById(R.id.latestReadingLabel)).setText(act.getString(R.string.no_readings));
                    }

                }
                WindGraph.SetupGraph(wd, plot, act);
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
        outState.putString(PARAM_KEY_STATION_URL, station.url.toString());
        outState.putString(PARAM_KEY_STATION_NAME, station.Name);
    }
}
