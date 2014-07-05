package com.feer.windcast.tests;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.dataAccess.FavouriteStationCache;
import com.feer.windcast.dataAccess.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.closeDrawer;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.openDrawer;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withChild;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withParent;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class testDrawerStationFragmentInteraction extends ActivityInstrumentationTestCase2<MainActivity>
{
    public testDrawerStationFragmentInteraction()
    {
        super(MainActivity.class);
    }

    private FakeWeatherStationData mFakeStations;
    private WeatherDataCache mCache;
    private final int drawerID = R.id.windcast_drawer;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mFakeStations = new FakeWeatherStationData("test Station");

        // set up weather data cache before starting the activity.
        mCache = mock(WeatherDataCache.class);
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.EmptyStationList());
        when(mCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.EmptyStationList());
        WeatherDataCache.SetsWeatherDataCache(mCache);
    }

    public void test_Drawer_openClose()
    {
        launchActivity();

        openDrawer(drawerID);
        onView(withId(R.id.drawer_states))
                .check(matches(isDisplayed()));

        closeDrawer(drawerID);
        onView(withId(R.id.drawer_states))
                .check(matches(not(isDisplayed())));
    }

    public void test_drawerOpen_hasExpectedContents()
    {
        launchActivity();
        openDrawer(drawerID);

        onView(withId(R.id.drawer_states))
                        .check(matches(withText(R.string.states)));

        CharSequence[] stateList = getActivity().getResources().getTextArray(R.array.AustralianStates);

        onView(withId(R.id.left_drawer_list))
                .check(matches(adapterHasCount(equalTo(stateList.length))));

        for(int i = 0; i < stateList.length; ++i)
        {
            onData(instanceOf(String.class))
                    .inAdapterView(withId(R.id.left_drawer_list))
                    .atPosition(i)
                    .check(matches(withText(stateList[i].toString())));
        }
    }

    public void test_selectWA_closesDrawer()
    {
        launchActivity();

        openDrawer(drawerID);

        onView(withText("WA")).perform(click());
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(not(isDisplayed())));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void test_selectWA_titleShowsSelection()
    {
        launchActivity();
        final Activity act = getActivity();

        openDrawer(drawerID);
        onView(withText("WA")).perform(click());

        assertEquals("Wind Stations in WA", act.getActionBar().getTitle().toString());

        openDrawer(drawerID);
        onView(withText("All")).perform(click());

        final String appName = act.getResources().getString(R.string.app_name);
        assertEquals(appName, act.getActionBar().getTitle().toString());
    }

    public void test_selectWA_showsWAStations()
    {
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());
        when(mCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.GetSingleStation(0));

        launchActivity();

        final String STATE_TO_CLICK = "WA";

        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(FakeWeatherStationData.MAX_NUM_OF_ALL_STATIONS))));

        openDrawer(drawerID);
        onView(withText(STATE_TO_CLICK)).perform(click());

        verify(mCache, times(1)).GetWeatherStationsFrom(STATE_TO_CLICK);
        onView(withId(android.R.id.list)).check(matches(adapterHasCount(equalTo(1))));
    }

    public void test_withFilter_selectState_FilterCleared()
    {
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());
        when(mCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.GetAllStations());
        launchActivity();

        onView(withId(R.id.weather_station_search_box))
                .perform(typeText("Station3"));
        onView(withId(android.R.id.list))
                .check(matches(adapterHasCount(equalTo(1))));

        openDrawer(drawerID);
        final String STATE_TO_CLICK = "WA";
        onView(withText(STATE_TO_CLICK)).perform(click());
        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(FakeWeatherStationData.MAX_NUM_OF_ALL_STATIONS))));

        onView(withId(R.id.weather_station_search_box))
                .check(matches(withText("")));
    }

    public void test_SelectFavourites_ShowsOnlyFavourites() throws MalformedURLException
    {
        FavouriteStationCache mFavs;
        mFavs = mock(FavouriteStationCache.class);
        when(mCache.CreateNewFavouriteStationAccessor()).thenReturn(mFavs);
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());


        final ArrayList<WeatherStation> favStationList = new ArrayList<WeatherStation>();
        ArrayList<WeatherStation> allStationList = mFakeStations.GetAllStations();
        WeatherStation copy = allStationList.get(5);
        favStationList.add(new WeatherStation(copy.Name, copy.url.toString()));
        copy = allStationList.get(2);
        favStationList.add(new WeatherStation(copy.Name, copy.url.toString()));

        when(mFavs.GetFavourites()).thenReturn(favStationList);

        launchActivity();

        openDrawer(drawerID);
        final String FAVOURITES_TEXT = "Favourites";
        onView(withText(FAVOURITES_TEXT)).perform(click());

        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(2))));

        onView(withId(R.id.weather_station_search_box))
                .check(matches(withText("")));
    }

    public void test_ClickBlackStar_CallsAddFav() throws MalformedURLException
    {
        FavouriteStationCache mFavs;
        mFavs = mock(FavouriteStationCache.class);
        when(mCache.CreateNewFavouriteStationAccessor()).thenReturn(mFavs);
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());

        when(mFavs.GetFavourites()).thenReturn(mFakeStations.EmptyStationList());

        launchActivity();

        WeatherStation newFav = mFakeStations.GetAllStations().get(4);
        assertTrue(
                "Station not yet clicked, it should not be a favourite!",
                newFav.IsFavourite == false);

        onView(allOf(
                        withParent(withChild(withText(newFav.toString()))),
                        withId(R.id.image)))
                .perform(click());

        verify(mFavs).AddFavouriteStation(newFav);
        assertTrue(
                "Station clicked, it should now be a favourite!",
                newFav.IsFavourite == true);
    }

    public void test_ClickYellowStar_CallsRemoveFav() throws MalformedURLException
    {
        FavouriteStationCache mFavs;
        mFavs = mock(FavouriteStationCache.class);
        when(mCache.CreateNewFavouriteStationAccessor()).thenReturn(mFavs);
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());

        final int STATION_INDEX = 4;
        WeatherStation oldFav = mFakeStations.GetAllStations().get(STATION_INDEX);
        oldFav.IsFavourite = true;
        when(mFavs.GetFavourites()).thenReturn(mFakeStations.GetSingleStation(STATION_INDEX));

        launchActivity();

        onView(allOf(
                        withParent(withChild(withText(oldFav.toString()))),
                        withId(R.id.image)))
                .perform(click());

        verify(mFavs).RemoveFavouriteStation(oldFav);
        assertTrue(
                "Station clicked, it should now no longer be a favourite!",
                oldFav.IsFavourite == false);
    }
}
