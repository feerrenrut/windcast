package com.feer.windcast.dataAccess;

import com.feer.windcast.WeatherStation;

import java.util.ArrayList;

public interface LoadedWeatherStationCache
{
    public ArrayList<WeatherStation> GetWeatherStationsFrom(String state);
    public ArrayList<WeatherStation> GetWeatherStationsFromAllStates();
}
