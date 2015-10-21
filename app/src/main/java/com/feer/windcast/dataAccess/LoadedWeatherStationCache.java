package com.feer.windcast.dataAccess;

import com.feer.windcast.WeatherData;

import java.util.ArrayList;

public interface LoadedWeatherStationCache
{
    ArrayList<WeatherData> GetWeatherStationsFrom(String state);
    ArrayList<WeatherData> GetWeatherStationsFromAllStates();
    boolean AreAllStatesFilled();
}
