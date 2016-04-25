package com.feer.windcast.dataAccess.dependencyProviders;

import android.util.Log;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;

/**
 * Created by Reef on 16/03/2016.
 */
public class SpecificStationWeatherDataProvider {
    private static String TAG = "WeatherDataProvider";

    protected static SpecificStationWeatherDataProvider sInstance = null;
    protected static SpecificStationWeatherDataProvider getInstance() {
        if(sInstance == null ) {
            sInstance = new SpecificStationWeatherDataProvider();
        }
        return sInstance;
    }

    /* Performs http request, should be called using AsyncTask
     */
    public static WeatherData GetWeatherDataFor(WeatherStation station) {
        return getInstance().InternalGetWeatherDataFor(station);
    }

    protected WeatherData InternalGetWeatherDataFor(WeatherStation station) {
        WeatherData wd = null;
        try {
            ObservationReader obs = new ObservationReader(station);
            wd = obs.GetWeatherData();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return wd;
    }
}
