package com.feer.windcast.testUtils;

import com.feer.windcast.WeatherStation;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.google.android.apps.common.testing.testrunner.util.Checks.checkNotNull;
import static java.lang.String.format;

/**
 *
 */
public class FakeWeatherStationData
{
    private ArrayList<WeatherStation> mStations;

    public FakeWeatherStationData(String stationNameBase) throws Exception
    {
        checkNotNull(stationNameBase);
        final String urlFormat = "http://WindCastTestData.com/%s.json";
        final String nameFormat = "%s%d";

        final int numToAdd = 11;
        mStations = new ArrayList<WeatherStation>(numToAdd);
        try
        {
            for(int stationIndex = 0; stationIndex < numToAdd; ++stationIndex)
            {
                final String stationName = format(nameFormat, stationNameBase, stationIndex);
                final String stationUrl = format(urlFormat, stationNameBase);
                mStations.add(new WeatherStation(stationName, stationUrl));
            }
        } catch (MalformedURLException e)
        {
            throw new Exception("Unable to create fake weatherStation Data.", e);
        }
    }

    public ArrayList<WeatherStation> GetSingleStation()
    {
        ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>(1);
        stations.add(mStations.get(0));
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
