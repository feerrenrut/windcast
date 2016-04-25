package com.feer.windcast.dataAccess.dependencyProviders;

import com.feer.windcast.dataAccess.FavouriteStationCache;

/**
 * Created by Reef on 16/03/2016.
 */
public class FavouriteStationCacheProvider {

    protected static FavouriteStationCacheProvider sInstance = null;
    protected static FavouriteStationCacheProvider getInstance() {
        if(sInstance == null ) {
            sInstance = new FavouriteStationCacheProvider();
        }
        return sInstance;
    }

    public static FavouriteStationCache CreateNewFavouriteStationAccessor() {
        return getInstance().InternalCreateNewFavouriteStationAccessor();
    }

    protected FavouriteStationCache InternalCreateNewFavouriteStationAccessor() {
        return new FavouriteStationCache();
    }
}
