package com.feer.windcast.testUtils;

import android.content.Context;
import android.content.SharedPreferences;

import com.feer.windcast.WeatherStation;
import com.feer.windcast.WindCastNavigationDrawer;
import com.feer.windcast.dataAccess.BackgroundTaskManager;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.LoadedWeatherStationCache;
import com.feer.windcast.dataAccess.WeatherDataCache;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by Reef on 11/02/2015.
 */
public class WindCastMocks {


    public final FakeWeatherStationData Fakes;
    public final LoadedWeatherStationCache LoadedCache;
    public final WeatherDataCache DataCache;
    public final SharedPreferences Settings;
    public final FavouriteStationCache FavouritesCache;
    
    public WindCastMocks(SharedPreferences settings) throws Exception {
        Fakes = new FakeWeatherStationData("test Station", "fav Station");

        // set up weather data cache before starting the activity.
        LoadedCache = StrictMock.create(LoadedWeatherStationCache.class);
        DataCache = StrictMock.create(WeatherDataCache.class);
        FavouritesCache = StrictMock.create(FavouriteStationCache.class);

        WeatherDataCache.SetWeatherDataCache(DataCache); //set weather data cache singleton
        
        Mockito.doAnswer(createCallCacheFilledAnswer(LoadedCache)) // set call back for waiting for
                .when(DataCache).OnStationCacheFilled(                    // loaded data
                isA(WeatherDataCache.NotifyWhenStationCacheFilled.class));
        
        Mockito.doNothing().when(FavouritesCache).Initialise(
                any(Context.class), any(BackgroundTaskManager.class));

        Mockito.doReturn(null)
                .when(DataCache).GetWeatherDataFor(
                isA(WeatherStation.class));
                
        Settings = settings;
        ClearPreferences();
        AddNavigationDrawerAlreadyOpenedPreference();
    }
    
    /// must be called after setting up each of the mock method returns
    /// or use the alternate (and less preferred) do(..).when(..)
    public void VerifyNoUnstubbedCallsOnMocks()
    {
        StrictMock.verifyNoUnstubbedInteractions(LoadedCache);
        StrictMock.verifyNoUnstubbedInteractions(DataCache);
        StrictMock.verifyNoUnstubbedInteractions(FavouritesCache);
    }
   
    private Answer<Void> createCallCacheFilledAnswer(final LoadedWeatherStationCache cache)
    {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                ((WeatherDataCache.NotifyWhenStationCacheFilled)invocationOnMock.getArguments()[0])
                        .OnCacheFilled(cache);
                return null;
            }
        };
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
        when(DataCache.CreateNewFavouriteStationAccessor())
                .thenReturn(FavouritesCache);
        when(FavouritesCache.GetFavouriteURLs())
                .thenReturn(Fakes.FavURLs());
        when(LoadedCache.GetWeatherStationsFrom( anyString()))
                .thenReturn(Fakes.Stations());
        when(LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Fakes.Stations());
        VerifyNoUnstubbedCallsOnMocks();
    }
    
}
