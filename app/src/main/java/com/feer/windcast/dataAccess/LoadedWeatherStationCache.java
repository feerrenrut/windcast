package com.feer.windcast.dataAccess;

import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

import java.util.ArrayList;

public interface LoadedWeatherStationCache
{
    public ArrayList<WeatherData> GetWeatherStationsFrom(String state);
    public ArrayList<WeatherData> GetWeatherStationsFromAllStates();
}
