package com.feer.windcast;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.PositionMetrics;
import com.androidplot.ui.SeriesRenderer;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
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

import static com.androidplot.Plot.BorderStyle.NONE;

/**
 *
 */
public class WindGraph
{
    private static final int MAX_READINGS_TO_SHOW = 10;

    public static void FormatGraph(XYPlot plot)
    {
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setRangeLabelVerticalOffset(-10);

        plot.getLayoutManager().remove(plot.getLegendWidget());
        plot.getLayoutManager().remove(plot.getTitleWidget());
        plot.getLayoutManager().remove(plot.getDomainLabelWidget());
        plot.setBorderStyle(NONE, 0.0f, 0.0f);
        plot.setMarkupEnabled(false);

        final int offWhite = Color.parseColor("#ffececec");

        plot.setPlotMargins(0, 0, 0, 0);
        plot.setPlotPadding(0, 0, 0, 0);

        plot.getGraphWidget().setSize(new SizeMetrics(0, SizeLayoutType.FILL, 0, SizeLayoutType.FILL));

        plot.getRangeLabelWidget().getLabelPaint().setColor(Color.BLACK);
        plot.getRangeLabelWidget().setMarginLeft(20.0f);

        plot.getGraphWidget().setPositionMetrics(
                new PositionMetrics(
                        0.0f, XLayoutStyle.ABSOLUTE_FROM_LEFT,
                        0.0f, YLayoutStyle.ABSOLUTE_FROM_TOP,
                        AnchorPosition.LEFT_TOP));

        plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.BLACK);
        plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        plot.getGraphWidget().setDomainLabelVerticalOffset(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);

        plot.getGraphWidget().getGridBackgroundPaint().setColor(offWhite); // sets the colour of teh part of the graph behind the grid
        plot.getGraphWidget().getBackgroundPaint().setColor(offWhite); // sets the colour of the part of the graph where the axis labels are
    }

    public static void SetupGraph(WeatherData wd, XYPlot plot, Activity act, final SettingsActivity.WindSpeedUnitPref.UnitType unitType)
    {
        plot.clear();
        int numObs = wd.ObservationData.size();
        ArrayList<Integer> windSpeeds = new ArrayList<Integer>(numObs);
        final ArrayList<Date> readingTimes = new ArrayList<Date>(numObs);
        ArrayList<Float> windDirections = new ArrayList<Float>(numObs);

        for(ObservationReading reading1 : wd.ObservationData)
        {
            Integer val;

            if(unitType == SettingsActivity.WindSpeedUnitPref.UnitType.kmh) {
                val = reading1.WindSpeed_KMH != null ?
                        reading1.WindSpeed_KMH : 0;
            }
            else
            {
                val = reading1.WindSpeed_KN != null ?
                        reading1.WindSpeed_KN : 0;
            }

            windSpeeds.add(val);
            readingTimes.add(reading1.LocalTime);
            windDirections.add(reading1.WindBearing);
        }

        Collections.reverse(windSpeeds);
        Collections.reverse(readingTimes);
        Collections.reverse(windDirections);

        plot.setTitle("Wind Speed at " + wd.Station.GetName());

        plot.getRangeLabelWidget().setText( unitType == SettingsActivity.WindSpeedUnitPref.UnitType.kmh ? "km/h" : "kn");

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
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos)
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
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;
            }
        });
        Resources res = act.getResources();
        final Bitmap windArrow = BitmapFactory.decodeResource(res, R.drawable.wind_dir_arrow);
        final Bitmap calmIcon = BitmapFactory.decodeResource(res, R.drawable.calm);

        LineAndPointFormatter formatter = new LineAndPointFormatter()
        {
            @Override
            public SeriesRenderer getRendererInstance(XYPlot xyPlot)
            {
                return new WindDirectionPointRenderer<LineAndPointFormatter>(xyPlot, windArrow, calmIcon);
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

        labelFormatter.hOffset = 0.f;
        labelFormatter.vOffset -= 10.f;

        // add a new series' to the xyplot:
        plot.addSeries(series1, formatter);

        WindDirectionPointRenderer renderer = (WindDirectionPointRenderer) plot.getRenderer(WindDirectionPointRenderer.class);
        renderer.SetWindDirections(windDirections);

        plot.getGraphWidget().setPositionMetrics(
                new PositionMetrics(
                        0.0f, XLayoutStyle.ABSOLUTE_FROM_LEFT,
                        0.0f, YLayoutStyle.ABSOLUTE_FROM_TOP,
                        AnchorPosition.LEFT_TOP));
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
