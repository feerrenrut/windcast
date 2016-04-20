package com.feer.windcast.tests;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.test.espresso.contrib.DrawerActions;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.AWeatherStation;
import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherData;
import com.feer.windcast.WeatherStation;
import com.feer.windcast.testUtils.WindCastMocks;

import org.hamcrest.Matchers;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
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
    }


    public void test_Drawer_openClose()
    {
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        openDrawer(drawerID);
        onView(withId(R.id.drawer_states))
                .check(matches(isDisplayed()));

        closeDrawer(drawerID);
        onView(withId(R.id.drawer_states))
                .check(matches(not(isDisplayed())));
    }

    public void test_drawerOpen_ExpandStates_hasExpectedContents()
    {
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        
        openDrawer(drawerID);
        
        onView(withId(R.id.drawer_states))
                .perform(click())
                .check(matches(withText(R.string.states)));

        CharSequence[] stateList = getActivity().getResources().getTextArray(R.array.AustralianStates);

        onView(withId(R.id.drawer_states_list))
                .check(matches(adapterHasCount(equalTo(stateList.length))));

        for(int i = 0; i < stateList.length; ++i)
        {
            onData(instanceOf(String.class))
                    .inAdapterView(withId(R.id.drawer_states_list))
                    .atPosition(i)
                    .check(matches(withText(stateList[i].toString())));
        }
    }

    public void test_drawerOpen_hasExpectedContents()
    {
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        openDrawer(drawerID);

        onView(withId(R.id.drawer_favs))
                .check(matches(allOf(
                    withText(R.string.favourites),
                    isCompletelyDisplayed() )));
        onView(withId(R.id.drawer_favs_image))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.drawer_allStations))
                .check(matches(allOf(
                        withText(R.string.all),
                        isCompletelyDisplayed())));
        onView(withId(R.id.drawer_all_image))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.drawer_states))
                .check(matches(allOf(
                        withText(R.string.states),
                        isCompletelyDisplayed())));
        onView(withId(R.id.drawer_states_image))
                .check(matches(isCompletelyDisplayed()));

        onView(withId(R.id.drawer_states_list))
                .check(matches(not(isDisplayed())));
    }


    public void test_selectWA_closesDrawer()
    {
        Mocks.JustUseMocksWithFakeData();
        launchActivity();

        openDrawer(drawerID);
        onView(withId(R.id.drawer_states)).check(matches(isCompletelyDisplayed()));
        onView(withId(R.id.drawer_states)).perform(click());

        onView(withText("WA")).perform(click());
        onView(withId(drawerID)).check(matches(isClosed()));
    }

    private void openDrawer(int drawerID) {
        onView(withId(drawerID)).perform(DrawerActions.open());
    }

    private void closeDrawer(int drawerID) {
        onView(withId(drawerID)).perform(DrawerActions.close());
    }

    public void test_selectWA_titleShowsSelection()
    {
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        final Activity act = getActivity();

        openDrawer(drawerID);
        onView(withId(R.id.drawer_states)).perform(click());
        onView(withText("WA")).perform(click());

        onView( withText("Wind Stations in WA")).check(matches(isDisplayed()));

        openDrawer(drawerID);
        onView(withText("All")).perform(click());

        final String expectedTitle = act.getResources().getString(R.string.app_name);
        onView( withText(expectedTitle)).check(matches(isDisplayed()));
    }

    public void test_selectWA_showsWAStations()
    {
        final int EXPECTED_NUM_STATIONS = 11;
        final String STATE_TO_CLICK = "WA";
        
        Mocks.FakeData.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(0);

        ArrayList<WeatherData> oneState = new ArrayList<WeatherData>(
                Mocks.FakeData.Stations().subList(0, 1));

        when(Mocks.FavouritesCache.GetFavouriteURLs())
                .thenReturn(Mocks.FakeData.FavURLs());
        when(Mocks.LoadedCache.GetWeatherStationsFromAllStates())
                .thenReturn(Mocks.FakeData.Stations());
        when(Mocks.LoadedCache.GetWeatherStationsFrom(STATE_TO_CLICK))
                .thenReturn(oneState);
        when(Mocks.LoadedCache.AreAllStatesFilled())
                .thenReturn(true);
        when(Mocks.LoadedCache.IsStale())
                .thenReturn(true);
        
        Mocks.VerifyNoUnstubbedCallsOnMocks();
        launchActivity();

        openDrawer(drawerID);
        onView(withText("All")).perform(click());

        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));
        openDrawer(drawerID);
        onView(withId(R.id.drawer_states)).perform(click());
        onView(withText(STATE_TO_CLICK)).perform(click());

        verify(Mocks.LoadedCache, times(1)).GetWeatherStationsFrom(STATE_TO_CLICK);
        onView(withId(android.R.id.list)).check(matches(adapterHasCount(equalTo(1))));
    }

    // Test stats:
    // Legitimate bugs found: 1
    public void test_withFilter_selectState_FilterCleared()
    {
        Mocks.FakeData.HasStations(11).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();

        final Integer EXPECTED_NUM_STATIONS = Mocks.FakeData.Stations().size();
        final String STATE_TO_CLICK = "WA";

        launchActivity();

        onView(withId(android.R.id.list))
                .check(matches(
                        adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));

        onView(withId(R.id.search)).perform(click());
        AWeatherStation searchStation = Mocks.FakeData.Stations().get(2).Station;

        String searchTerm = searchStation.GetName();
        onView(withId(R.id.weather_station_search_box))
                .perform(typeText(searchTerm));

        onView(withId(android.R.id.list))
                .check(matches(adapterHasCount(Matchers.equalTo(1))));

        openDrawer(drawerID);
        onView(withId(R.id.drawer_states)).perform(click());
        
        onView(withText(STATE_TO_CLICK)).perform(click());
        
        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));

        onView(withId(R.id.weather_station_search_box))
                .check(matches(withText("")));
    }

    public void test_SelectFavourites_ShowsOnlyFavourites() throws MalformedURLException
    {
        final int EXPECTED_NUM_STATIONS = 11;
        Mocks.FakeData.HasStations(EXPECTED_NUM_STATIONS).HasFavourites(EXPECTED_NUM_STATIONS);

        doNothing().when(Mocks.FavouritesCache)
                .AddFavouriteStation(any(WeatherStation.class));
        doNothing().when(Mocks.FavouritesCache)
                .RemoveFavouriteStation(any(WeatherStation.class));
        
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        openDrawer(drawerID);
        final String FAVOURITES_TEXT = "Favourites";
        onView(withText(FAVOURITES_TEXT)).perform(click());

        onView(withId(android.R.id.list))
                .check(matches(
                    adapterHasCount(equalTo(EXPECTED_NUM_STATIONS))));

        onView(withId(R.id.weather_station_search_box))
                .check(matches(withText("")));
    }

    public void test_onStartDrawerOpen()
    {
        Mocks.ClearPreferences();
        Mocks.JustUseMocksWithFakeData();
        launchActivity();

        onView(withId(R.id.drawer_states))
                .check(matches(isDisplayed()));

        onView(withId(drawerID)).check(matches(isOpen()));
    }

    public void test_afterManualDrawerOpen_onStartDrawerNotOpen()
    {
        Mocks.ClearPreferences();
        Mocks.AddNavigationDrawerAlreadyOpenedPreference();
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        onView(withId(R.id.drawer_states))
                .check(matches(not(isDisplayed())));
    }

    public void test_manuallyOpeningDrawer_setsPreference()
    {
        Mocks.ClearPreferences();
        Mocks.JustUseMocksWithFakeData();
        launchActivity();

        onView(withId(drawerID)).check(matches(isOpen()));
        closeDrawer(drawerID);
        assertFalse(
                "Drawer not manually opened, preference should not yet exist",
                Mocks.GetPreference_HasDrawerBeenOpened());

        // this seems like a hack, calling open drawer alone does not result in the onDrawerOpen
        // callback being called. However swipeRight does not wait for the drawer to open!
        swipeRight();
        openDrawer(drawerID);

        onView(withId(drawerID)).check(matches(isOpen()));
        assertTrue(
                "Drawer has been manually opened, preference should now exist",
                Mocks.GetPreference_HasDrawerBeenOpened());
    }
}
