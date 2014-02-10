package com.feer.windcast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;

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
                    try
                    {
                        URL url = new URL("http://www.bom.gov.au/fwo/IDW60801/IDW60801.94603.json");
                        URLConnection ucon = url.openConnection();
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);

                        wd = ObservationReader.ReadJsonStream(is);

                    } catch (MalformedURLException e)
                    {
                        Log.e("DATA", e.getMessage());
                    } catch (IOException e)
                    {
                        Log.e("DATA", e.getMessage());
                    }
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


                            ArrayList<Number> windSpeeds = new ArrayList<Number>(wd.ObservationData.size());
                            ArrayList<Number> readingTimes = new ArrayList<Number>(wd.ObservationData.size());
                            ListIterator windSpeedItr = windSpeeds.listIterator();
                            ListIterator readingTimesItr = readingTimes.listIterator();
                            for(ObservationReading reading1 : wd.ObservationData)
                            {
                                Number val = reading1.WindSpeed_KMH != null ?
                                        reading1.WindSpeed_KMH : 0;

                                windSpeedItr.add(val);

                                readingTimesItr.add(reading1.LocalTime.getTime());
                            }
                            Collections.reverse(windSpeeds);
                            Collections.reverse(readingTimes);

                            // initialize our XYPlot reference:
                            plot = (XYPlot) getActivity().findViewById(R.id.mySimpleXYPlot);
                            plot.setTitle("Wind Speed at "+wd.WeatherStationName);

                            // Create a couple arrays of y-values to plot:
                            Number[] series1Numbers = windSpeeds.toArray(new Number[windSpeeds.size()]);
                            Number[] series1XLabels = readingTimes.toArray(new Number[readingTimes.size()]);

                            // Turn the above arrays into XYSeries':
                            XYSeries series1 = new SimpleXYSeries(
                                    Arrays.asList(series1XLabels),
                                    Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                                    "");                             // Set the display title of the series

                            plot.setDomainLabel("Time");
                            plot.setDomainValueFormat(new DecimalFormat("0"));

                            plot.setDomainValueFormat(new Format() {

                                // create a simple date format that draws on the year portion of our timestamp.
                                // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
                                // for a full description of SimpleDateFormat.
                                private SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm");

                                @Override
                                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                                    // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                                    // we multiply our timestamp by 1000:
                                    long timestamp = ((Number) obj).longValue() * 1000;
                                    Date date = new Date(timestamp);
                                    return dateFormat.format(date, toAppendTo, pos);
                                }

                                @Override
                                public Object parseObject(String source, ParsePosition pos) {
                                    return null;

                                }
                            });

                            // Create a formatter to use for drawing a series using LineAndPointRenderer
                            // and configure it from xml:
                            LineAndPointFormatter series1Format = new LineAndPointFormatter();
                            series1Format.setPointLabelFormatter(new PointLabelFormatter());
                            series1Format.configure(getActivity().getApplicationContext(),
                                    R.xml.line_point_formatter_with_plf1);



                            // add a new series' to the xyplot:
                            plot.addSeries(series1, series1Format);
                            plot.getLegendWidget().setVisible(false);

                            // reduce the number of range labels
                            plot.setTicksPerRangeLabel(3);
                            plot.getGraphWidget().setDomainLabelOrientation(-45);
                        }
                    }
                }
            }.execute();



        }
    }

}
