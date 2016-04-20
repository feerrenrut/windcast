package com.feer.windcast.testUtils;

import android.content.Context;
import android.content.SharedPreferences;

import com.feer.windcast.WindCastNavigationDrawer;
import com.feer.windcast.dataAccess.BackgroundTaskManager;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.LoadedWeatherStationCache;
import com.feer.windcast.dataAccess.StationListCacheLoader;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Reef on 11/02/2015.
 */
public class WindCastMocks {

    public final FakeWeatherStationData FakeData;
    public final LoadedWeatherStationCache LoadedCache;
    public final StationListCacheLoader.InternalCacheLoader internalCacheLoader;
    public final SharedPreferences Settings;
    public final FavouriteStationCache FavouritesCache;

    // Providers to override the default dependencies
    private final MockStationListCacheProvider mockStationListCacheProvider;
    private final MockSpecificStationWeatherDataProvider mockSpecificStationWeatherDataProvider;
    private final MockFavouriteStationCacheProvider mockFavouriteStationCacheProvider;
    
    public WindCastMocks(SharedPreferences settings) throws Exception {
        FakeData = new FakeWeatherStationData("test Station", "fav Station");

        // set up weather data cache before starting the activity.
        LoadedCache = StrictMock.create(LoadedWeatherStationCache.class);
        FavouritesCache = StrictMock.create(FavouriteStationCache.class);
        internalCacheLoader = StrictMock.create(StationListCacheLoader.InternalCacheLoader.class);

        // set up dependency injection
        mockStationListCacheProvider = new MockStationListCacheProvider(internalCacheLoader);
        mockSpecificStationWeatherDataProvider = new MockSpecificStationWeatherDataProvider();
        mockFavouriteStationCacheProvider = new MockFavouriteStationCacheProvider(FavouritesCache);

        // set call back for waiting for loaded data
        Mockito.doAnswer(createCallCacheFilledAnswer(LoadedCache))
                .when(internalCacheLoader).TriggerFillStationCache(
                any( StationListCacheLoader.CacheLoaderInterface.class ) );
        
        Mockito.doNothing().when(FavouritesCache).Initialise(
                any(Context.class), any(BackgroundTaskManager.class));

        mockSpecificStationWeatherDataProvider.SetWeatherData(null);
                
        Settings = settings;
        ClearPreferences();
        AddNavigationDrawerAlreadyOpenedPreference();
    }
    
    /// must be called after setting up each of the mock method returns
    /// or use the alternate (and less preferred) do(..).when(..)
    public void VerifyNoUnstubbedCallsOnMocks()
    {
        StrictMock.verifyNoUnstubbedInteractions(LoadedCache);
        StrictMock.verifyNoUnstubbedInteractions(FavouritesCache);
    }

    public class OnStationCacheFilledAnswer implements  Answer<Void>{
        final LoadedWeatherStationCache mCache;
        OnStationCacheFilledAnswer(LoadedWeatherStationCache cache)
        {
            mCache = cache;
        }
        
        @Override
        public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            handleOnCacheFilledRequest(
                    (StationListCacheLoader.CacheLoaderInterface)
                            invocationOnMock.getArguments()[0]);
            return null;
        }
        
        protected void handleOnCacheFilledRequest(
                StationListCacheLoader.CacheLoaderInterface callback)
        {
            callback.onComplete( mCache );
        }
    }
   
    private Answer<Void> createCallCacheFilledAnswer(final LoadedWeatherStationCache cache)
    {
        return new OnStationCacheFilledAnswer(cache);
    }
    
    public void ClearPreferences()
    {
        SharedPreferences.Editor editor =  Settings.edit();
        editor.clear();
        editor.commit();
    }

    public void AddNavigationDrawerAlreadyOpenedPreference()
    {

        SharedPreferences.Editor editor =  Settings.edit();
        editor.putBoolean(WindCastNavigationDrawer.PREFS_NAVIGATION_DRAWER_OPENED, true);
        editor.commit();
    }

    public boolean GetPreference_HasDrawerBeenOpened()
    {
        return Settings.getBoolean(WindCastNavigationDrawer.PREFS_NAVIGATION_DRAWER_OPENED, false);
    }
    
    public void JustUseMocksWithFakeData()
    {
        when(FavouritesCache.GetFavouriteURLs())
                .thenReturn(FakeData.FavURLs());
        when(LoadedCache.GetWeatherStationsFrom(anyString()))
                .thenReturn(FakeData.Stations());
        when(LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(FakeData.Stations());
        when(LoadedCache.AreAllStatesFilled())
                .thenReturn(true);
        when(LoadedCache.IsStale())
                .thenReturn(true);
        VerifyNoUnstubbedCallsOnMocks();
    }
    
}
