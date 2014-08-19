package com.feer.windcast.testUtils;

import com.feer.windcast.WeatherStation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.feer.windcast.WeatherStation.States.*;
import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;
import static java.lang.String.format;

/**
 *
 */
public class FakeWeatherStationData
{
    private ArrayList<WeatherStation> mStations;
    public static final int MAX_NUM_OF_ALL_STATIONS = 11;

    public FakeWeatherStationData(String stationNameBase) throws Exception
    {
        checkNotNull(stationNameBase);
        final String urlFormat = "http://WindCastTestData.com/%d.json";
        final String nameFormat = "%s%d";

        final int numToAdd = MAX_NUM_OF_ALL_STATIONS;
        mStations = new ArrayList<WeatherStation>(numToAdd);
        try
        {
            for(int stationIndex = 0; stationIndex < numToAdd; ++stationIndex)
            {
                WeatherStation.WeatherStationBuilder builder = new WeatherStation.WeatherStationBuilder();
                builder.WithName(format(nameFormat, stationNameBase, stationIndex));
                builder.WithURL(new URL(format(urlFormat, stationIndex)));
                builder.WithState(WA);

                mStations.add(builder.Build());
            }
        } catch (MalformedURLException e)
        {
            throw new Exception("Unable to create fake weatherStation Data.", e);
        }
    }

    public ArrayList<WeatherStation> GetSingleStation(int index)
    {
        ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>(1);
        stations.add(mStations.get(index));
        return stations;
    }

    public ArrayList<WeatherStation> EmptyStationList()
    {
        return new ArrayList<WeatherStation>();
    }

    public ArrayList<WeatherStation> GetAllStations()
    {
        return mStations;
    }
}
