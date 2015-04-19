package com.feer.windcast.testUtils;

import android.util.Log;

import com.feer.windcast.WeatherData;
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
    private final ArrayList<WeatherData> mStations;
    private final ArrayList<WeatherData> mFavouriteStations;
    private final String mStationNameBase;
    private final String mFavouriteNameBase;

    public FakeWeatherStationData(String stationNameBase, String favouriteNameBase) throws Exception
    { 
        checkNotNull(stationNameBase);
        checkNotNull(favouriteNameBase);
        mStationNameBase=stationNameBase;
        mFavouriteNameBase=favouriteNameBase;
        mStations = new ArrayList<WeatherData>();
        mFavouriteStations = new ArrayList<WeatherData>();
    }
    
    private void CreateMoreStations(int numToAdd, boolean areFavs, ArrayList<WeatherData> stationList)  {
        final String urlFormat = "http://WindCastTestData.com/%s%d.json";
        final String nameFormat = "%s%d";


        final String nameBase = areFavs ? mFavouriteNameBase : mStationNameBase;
        final String urlStationType = areFavs ? "fav" : "station";

        final int firstNewStationIndex = stationList.size();
        final int lastNewStationIndex = firstNewStationIndex + numToAdd;
        try
        {
            for(int stationIndex = firstNewStationIndex; stationIndex < lastNewStationIndex; ++stationIndex)
            {
                WeatherStation.WeatherStationBuilder builder = new WeatherStation.WeatherStationBuilder();
                builder.WithName(format(nameFormat, nameBase, stationIndex));
                builder.WithURL(new URL(format(urlFormat, urlStationType, stationIndex)));
                builder.WithState(WA);
                builder.IsFavourite(areFavs);
                
                WeatherData data = new WeatherData();
                data.Station = builder.Build();

                stationList.add(data);
            }
        } catch (MalformedURLException e)
        {
            Log.e("FakeWeatherStationData", "Unable to create fake weatherStation Data.", e);
        } 
    }
    
    private void EnsureStationExists(int index)
    {
        final int necessarySize = index +1;
        if(necessarySize > mStations.size())
        {
            CreateMoreStations(necessarySize - mStations.size(), false, mStations);
        }
    }
    
    private void EnsureFavouriteStationExists(int index)
    {
        final int necessarySize = index +1;
        if(necessarySize > mFavouriteStations.size())
        {
            CreateMoreStations(necessarySize - mFavouriteStations.size(), true, mFavouriteStations);
        }
    }
    
    public FakeWeatherStationData HasStations(int numberOfStations)
    {
        EnsureStationExists(numberOfStations-1);
        return this;
    }
    public FakeWeatherStationData HasFavourites(int numberOfStations)
    {
        EnsureFavouriteStationExists(numberOfStations-1);
        return this;
    }
    
    public ArrayList<WeatherData> Stations()
    {
        ArrayList<WeatherData> s = new ArrayList<WeatherData>(mStations);
        s.addAll(mFavouriteStations);
        return s;
    }
    
    public ArrayList<String> FavURLs()
    {
        ArrayList<String> s = new ArrayList<String>();
        for(WeatherData data : mFavouriteStations)
        {
            s.add(data.Station.GetURL().toString());
        }
        return s;
    }
}
