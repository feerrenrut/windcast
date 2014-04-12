package com.feer.windcast;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.androidplot.ui.SeriesRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.feer.windcast.graphing.WindDirectionPointRenderer;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class WindGraph
{
    private static final int MAX_READINGS_TO_SHOW = 10;

    public static void SetupGraph(WeatherData wd, XYPlot plot, Activity act)
    {
        int numObs = wd.ObservationData.size();
        ArrayList<Integer> windSpeeds = new ArrayList<Integer>(numObs);
        final ArrayList<Date> readingTimes = new ArrayList<Date>(numObs);
        ArrayList<Float> windDirections = new ArrayList<Float>(numObs);

        for(ObservationReading reading1 : wd.ObservationData)
        {
            Integer val = reading1.WindSpeed_KMH != null ?
                    reading1.WindSpeed_KMH : 0;

            windSpeeds.add(val);
            readingTimes.add(reading1.LocalTime);
            windDirections.add(reading1.WindBearing);
        };

        Collections.reverse(windSpeeds);
        Collections.reverse(readingTimes);
        Collections.reverse(windDirections);

        plot.setTitle("Wind Speed at " + wd.Station.Name);

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                windSpeeds,          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "");                             // Set the display title of the series


        if(numObs > MAX_READINGS_TO_SHOW)
        {
            List<Integer> sublist = windSpeeds.subList(windSpeeds.size() - MAX_READINGS_TO_SHOW, windSpeeds.size());
            SetGraphBoundaries(sublist, plot, numObs, true);
        }
        else
        {
            SetGraphBoundaries(windSpeeds, plot, numObs, false);
        }

        plot.setDomainValueFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm");
            private int domainTick = 0;

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
            {
                domainTick++;

                if(domainTick % 2 == 0)
                {
                    long index = Math.round( ((Number)obj).doubleValue() ) ;

                    if(index < readingTimes.size())
                    {
                        Date date = readingTimes.get((int)index);
                        return dateFormat.format(date, toAppendTo, pos);
                    }
                }

                return new StringBuffer();
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        Resources res = act.getResources();
        final Bitmap windArrow = BitmapFactory.decodeResource(res, R.drawable.wind_dir_arrow);

        LineAndPointFormatter formatter = new LineAndPointFormatter()
        {
            @Override
            public SeriesRenderer getRendererInstance(XYPlot xyPlot)
            {
                return new WindDirectionPointRenderer<LineAndPointFormatter>(xyPlot, windArrow);
            }

            @Override
            public Class<? extends SeriesRenderer> getRendererClass()
            {
                return WindDirectionPointRenderer.class;
            }
        };

        PointLabelFormatter labelFormatter = new PointLabelFormatter();
        formatter.setPointLabelFormatter(labelFormatter);
        formatter.configure(
                act.getApplicationContext(),
                R.xml.line_point_formatter_with_plf1
                           );

        labelFormatter.vOffset -= 4.f;

        // add a new series' to the xyplot:
        plot.addSeries(series1, formatter);
        plot.getLegendWidget().setVisible(false);
        WindDirectionPointRenderer renderer = (WindDirectionPointRenderer) plot.getRenderer(WindDirectionPointRenderer.class);
        renderer.SetWindDirections(windDirections);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
    }

    private static void SetGraphBoundaries(List<Integer> windSpeedsList, XYPlot plot, int numObjs, boolean usingSublist)
    {
        final float DOMAIN_STEP = 0.5f;
        final float RANGE_BUFFER = 0.1f;//10% buffer from the top and bottom of the graph edge for clear display of graph points
        final int lastReadingIndex = numObjs -1;
        final int numberOfReadingsToShow = Math.min(MAX_READINGS_TO_SHOW, lastReadingIndex);
        final int firstReadingIndex = lastReadingIndex - numberOfReadingsToShow;

        plot.setDomainStepValue(DOMAIN_STEP);
        plot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);

        float maxValue = Collections.max(windSpeedsList).floatValue();
        float minValue = Collections.min(windSpeedsList).floatValue();
        float rangeOffset = (maxValue - minValue) * RANGE_BUFFER;

        plot.setRangeBoundaries(minValue - rangeOffset , maxValue + rangeOffset, BoundaryMode.FIXED);

        if(usingSublist)
        {
            plot.setDomainBoundaries((float)firstReadingIndex + DOMAIN_STEP, (float)lastReadingIndex + DOMAIN_STEP, BoundaryMode.FIXED);
        }
        else
        {
            plot.setDomainBoundaries((float)firstReadingIndex - DOMAIN_STEP, (float)lastReadingIndex + DOMAIN_STEP, BoundaryMode.FIXED);
        }
    }
}
