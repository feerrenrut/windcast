package com.feer.windcast.tests;

import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.AWeatherStation;
import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherData;
import com.feer.windcast.testUtils.WindCastMocks;

import java.net.MalformedURLException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class testStarInteraction extends ActivityInstrumentationTestCase2<MainActivity>
{

    public testStarInteraction()
    {
        super(MainActivity.class);
    }

    private WindCastMocks Mocks;
    private final int drawerID = R.id.drawer_layout;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
        
        Mocks.FakeData.HasStations(0).HasFavourites(0);
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.FakeData.Stations());
        when(Mocks.LoadedCache.GetWeatherStationsFrom(anyString()))
                .thenReturn(Mocks.FakeData.Stations());
    }

    public void test_ClickGrayStar_CallsAddFav() throws MalformedURLException
    {
        final int EXPECTED_NUM_STATIONS = 11;
        Mocks.FakeData.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(0);
        
        doNothing().when(Mocks.FavouritesCache)
                .AddFavouriteStation(any(AWeatherStation.class));
        doNothing().when(Mocks.FavouritesCache)
                .RemoveFavouriteStation(any(AWeatherStation.class));
        
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        AWeatherStation newFav = Mocks.FakeData.Stations().get(4).Station;
        assertTrue(
                "Station not yet clicked, it should not be a favourite!",
                newFav.IsFavourite == false);

        onView(allOf(
                        withParent(withChild(withText(newFav.toString()))),
                        withId(R.id.is_favourite_checkbox)))
                .perform(click());

        verify(Mocks.FavouritesCache).AddFavouriteStation(newFav);
        assertTrue(
                "Station clicked, it should now be a favourite!",
                newFav.IsFavourite == true);
    }

    public void test_ClickBlueStar_CallsRemoveFav() throws MalformedURLException
    {
        final int EXPECTED_NUM_STATIONS = 11;
        Mocks.FakeData.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(EXPECTED_NUM_STATIONS);
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.FakeData.Stations());
        when(Mocks.LoadedCache.GetWeatherStationsFrom(anyString()))
                .thenReturn(Mocks.FakeData.Stations());
        when(Mocks.LoadedCache.AreAllStatesFilled())
                .thenReturn(true);
        when(Mocks.LoadedCache.IsStale())
                .thenReturn(true);
        when(Mocks.FavouritesCache.GetFavouriteURLs())
                .thenReturn(Mocks.FakeData.FavURLs());
        doNothing().when(Mocks.FavouritesCache)
                .AddFavouriteStation(any(AWeatherStation.class));
        doNothing().when(Mocks.FavouritesCache)
                .RemoveFavouriteStation(any(AWeatherStation.class));
        
        AWeatherStation oldFav = null;
        for(WeatherData data : Mocks.FakeData.Stations())
        {
            if(data.Station.IsFavourite)
            {
                oldFav = data.Station;
                break;
            }
        }
        
        checkNotNull(oldFav);

        Mocks.VerifyNoUnstubbedCallsOnMocks();
        launchActivity();

        onView(allOf(
                        withParent(withChild(withText(oldFav.toString()))),
                        withId(R.id.is_favourite_checkbox)))
                .perform(click());

        verify(Mocks.FavouritesCache).RemoveFavouriteStation(oldFav);
        assertTrue(
                "Station clicked, it should now no longer be a favourite!",
                oldFav.IsFavourite == false);
    }
}
