package com.feer.windcast.dataAccess.dependencyProviders;
import com.feer.windcast.dataAccess.StationListCacheLoader;
import com.feer.windcast.dataAccess.StationListCacheLoader.InternalCacheLoader;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.dataAccess.WindCastApiWeatherStationLoader;

import java.util.UUID;

/**
 * Created by Reef on 16/03/2016.
 */
public class StationListCacheProvider {

    protected static StationListCacheProvider sInstance = null;
    protected static StationListCacheProvider getInstance() {
        if(sInstance == null ) {
            sInstance = new StationListCacheProvider();
        }
        return sInstance;
    }

    protected static WeatherDataCache sWeatherDataCache = null;
    public static WeatherDataCache GetWeatherDataCacheInstance()  {
        if(sWeatherDataCache == null) {
            sWeatherDataCache = new WeatherDataCache();
        }
        return sWeatherDataCache;
    }

    public static StationListCacheLoader CreateStationListCacheLoader() {
        return getInstance().InternalCreateStationListCacheLoader();
    }
    protected StationListCacheLoader InternalCreateStationListCacheLoader() {
        return new StationListCacheLoader();
    }

    public static InternalCacheLoader CreateInternalCacheLoader(UUID userId) {
        return getInstance().InternalCreateInternalCacheLoader(userId);
    }

    protected InternalCacheLoader InternalCreateInternalCacheLoader(UUID userId) {
        return new WindCastApiWeatherStationLoader(userId);
    }
}
