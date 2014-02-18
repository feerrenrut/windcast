package com.feer.windcast;

import android.app.Activity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;

/**
 *
 */
public class WindGraph
{
    public static void SetupGraph(WeatherData wd, XYPlot plot, Activity act)
    {
        int numObs = wd.ObservationData.size();
        ArrayList<Number> windSpeeds = new ArrayList<Number>(numObs);
        final ArrayList<Date> readingTimes = new ArrayList<Date>(numObs);

        ListIterator windSpeedItr = windSpeeds.listIterator();
        ListIterator readingTimesItr = readingTimes.listIterator();
        for(ObservationReading reading1 : wd.ObservationData)
        {
            Number val = reading1.WindSpeed_KMH != null ?
                    reading1.WindSpeed_KMH : 0;

            windSpeedItr.add(val);
            readingTimesItr.add(reading1.LocalTime);
        };

        Collections.reverse(windSpeeds);
        Collections.reverse(readingTimes);

        plot.setTitle("Wind Speed at " + wd.Station.Name);

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                windSpeeds,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "");                             // Set the display title of the series


        plot.setDomainBoundaries(numObs-11, numObs-1, BoundaryMode.FIXED);
        plot.setDomainStepValue(1.0);
        plot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);

        plot.setDomainValueFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                long index = Math.round( ((Number)obj).doubleValue() ) ;
                Date date = readingTimes.get((int)index);
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
        series1Format.configure(act.getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.getLegendWidget().setVisible(false);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
    }
}
