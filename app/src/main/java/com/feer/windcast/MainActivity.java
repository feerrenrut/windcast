package com.feer.windcast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

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

                @Override
                protected Boolean doInBackground(Void... params)
                {
                    wd = new WeatherDataCache(getActivity().getResources()).GetWeatherData();
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
                        sb.append(wd.WeatherStationName); sb.append('\n');
                        sb.append(wd.State); sb.append('\n');

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

                            label.setText(sb.toString());
                        }
                    }
                    // initialize our XYPlot reference:
                    XYPlot  plot = (XYPlot) getActivity().findViewById(R.id.mySimpleXYPlot);
                    WindGraph.SetupGraph(wd, plot, getActivity());
                }
            }.execute();



        }
    }

}
