package com.feer.windcast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;

import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class WindGraphFragment extends Fragment
{

    public WindGraphFragment() {
    }
    public WindGraphFragment(WeatherStation station) {
        mstation = station;
    }

    WeatherStation mstation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    private XYPlot plot;

    @Override
    public void onResume()
    {
        super.onResume();
        new AsyncTask<Void, Void, Boolean>()
        {
            WeatherData wd;
            String urls;

            @Override
            protected Boolean doInBackground(Void... params)
            {
                WeatherDataCache cache = new WeatherDataCache(getActivity().getResources());

                URL url;
                if(mstation != null)
                {
                    url = mstation.url;
                }
                else
                {
                    url = cache.GetWeatherStations().get(0).url;
                }
                Log.i("WindCast", "Getting data from: " + url.toString());
                wd = cache.GetWeatherDataFor(url);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result)
            {
                final TextView label = (TextView) getActivity().findViewById(R.id.label);
                if (label == null)
                {
                    throw new NullPointerException("unable to find the label");
                }

                if(wd == null)
                {
                    label.setText("Weather data is null!");
                }else
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(wd.Station.Name); sb.append('\n');
                    sb.append(wd.Station.State); sb.append('\n');

                    if(wd.ObservationData != null && !wd.ObservationData.isEmpty())
                    {
                        ObservationReading reading = wd.ObservationData.get(0);
                        sb.append(reading.LocalTime); sb.append("\n\n");
                        sb.append("Latest Wind Reading:");

                        if(reading.WindBearing != null && reading.CardinalWindDirection != null && reading.WindSpeed_KMH != null)
                        {
                            sb.append(reading.WindBearing);
                            sb.append(" (" +reading.CardinalWindDirection + " ) ");
                            sb.append(" " + reading.WindSpeed_KMH);
                        }
                    }

                    sb.append(urls);

                    label.setText(sb.toString());

                }
                // initialize our XYPlot reference:
                XYPlot  plot = (XYPlot) getActivity().findViewById(R.id.mySimpleXYPlot);
                WindGraph.SetupGraph(wd, plot, getActivity());
            }
        }.execute();



    }
}
