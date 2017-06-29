package com.feer.windcast.dataAccess.backend;

import android.util.Log;

import com.feer.windcast.WeatherData;
import com.feer.windcast.dataAccess.LoadedWeatherStationCache;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Reef on 20/10/2015.
 */
public class GAE_StationCache implements LoadedWeatherStationCache {

    ArrayList<WeatherData> mWeatherStations;

    @Override
    public ArrayList<WeatherData> GetWeatherStationsFrom(String state) {
        ArrayList<WeatherData> forState = new ArrayList<>();
        for (WeatherData d : mWeatherStations) {
            if(d.Station.GetStateAbbreviated().equals(state)){
                forState.add(d);
            }
        }
        return forState;
    }

    @Override
    public ArrayList<WeatherData> GetWeatherStationsFromAllStates() {
        return mWeatherStations;
    }

    @Override
    public boolean AreAllStatesFilled() {
        return mWeatherStations != null;
    }

    Long mCacheExpiryTime = null;
    @Override
    public boolean IsStale() {
        final long currentTime = new Date().getTime();
        return mCacheExpiryTime != null &&
                mCacheExpiryTime < currentTime ;
    }

    final long cacheTimeout = 15L * 60 * 1000; // 15 min;
    public void SetCachedStations(ArrayList<WeatherData> weatherStations, Date latestReadingTime) {
        mWeatherStations = weatherStations;
        mCacheExpiryTime = latestReadingTime.getTime() + cacheTimeout;
        Log.i("GAEstationcache", "Cache updated, latest reading: " + latestReadingTime.toLocaleString() );
    }
}
