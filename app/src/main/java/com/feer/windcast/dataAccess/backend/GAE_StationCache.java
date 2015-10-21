package com.feer.windcast.dataAccess.backend;

import com.feer.windcast.WeatherData;
import com.feer.windcast.dataAccess.LoadedWeatherStationCache;

import java.util.ArrayList;

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

    public void SetCachedStations(ArrayList<WeatherData> weatherStations) {
        mWeatherStations = weatherStations;
    }
}
