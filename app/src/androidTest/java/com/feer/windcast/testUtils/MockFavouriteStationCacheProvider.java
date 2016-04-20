package com.feer.windcast.testUtils;

import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.dependencyProviders.FavouriteStationCacheProvider;

/**
 * Created by Reef on 16/03/2016.
 */
public class MockFavouriteStationCacheProvider extends FavouriteStationCacheProvider {

    static FavouriteStationCache sMockFavouriteStationCache;
    MockFavouriteStationCacheProvider(FavouriteStationCache cache){
        super();
        sInstance = this;
        sMockFavouriteStationCache = cache;
    }

    @Override
    protected FavouriteStationCache InternalCreateNewFavouriteStationAccessor() {
        return sMockFavouriteStationCache;
    }
}
