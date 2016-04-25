package com.feer.windcast.testUtils;

import com.feer.windcast.dataAccess.StationListCacheLoader;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.dataAccess.dependencyProviders.StationListCacheProvider;

import java.util.UUID;

/**
 * Created by Reef on 16/03/2016.
 */
public class MockStationListCacheProvider extends StationListCacheProvider {

    static StationListCacheLoader.InternalCacheLoader sMockStationListCacheLoader;

    MockStationListCacheProvider(StationListCacheLoader.InternalCacheLoader loader){
        super();
        // set the static / singleton variables
        sInstance = this;

        // set the mock returned by internal create for the internalCacheLoader.
        sMockStationListCacheLoader = loader;

        // create a fresh weather data cache each run so no state is carried over.
        sWeatherDataCache = new WeatherDataCache();
    }

    @Override
    protected StationListCacheLoader InternalCreateStationListCacheLoader(){
        // dont mock creation of StationListCacheLoader, it does not need to be replaced for tests.
        return super.InternalCreateStationListCacheLoader();
    }

    @Override
    protected StationListCacheLoader.InternalCacheLoader InternalCreateInternalCacheLoader(UUID uuid){
        return sMockStationListCacheLoader;
    }

}
