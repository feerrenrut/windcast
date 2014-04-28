package com.feer.windcast.tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.closeDrawer;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.openDrawer;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;
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
        //launch activity
        Activity act = getActivity();

        openDrawer(R.id.windcast_drawer);
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(isDisplayed()));

        closeDrawer(R.id.windcast_drawer);
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(not(isDisplayed())));
    }

    public void test_selectWA_closesDrawer()
    {
        //launch activity
        Activity act = getActivity();

        openDrawer(R.id.windcast_drawer);

        onView(withText("WA")).perform(click());
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(not(isDisplayed())));
    }

    public void test_selectWA_titleShowsSelection()
    {
        //launch activity
        Activity act = getActivity();
        final String STATE_TO_SELECT = "WA";

        openDrawer(R.id.windcast_drawer);
        onView(withText(STATE_TO_SELECT)).perform(click());

        assertEquals("Wind Stations in WA", act.getActionBar().getTitle().toString());
    }

    public void test_selectWA_showsWAStations()
    {
        when(mCache.GetWeatherStationsFromAllStates()).thenReturn(mFakeStations.GetAllStations());
        when(mCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.GetSingleStation());

        //launch activity
        Activity act = getActivity();

        final String STATE_TO_CLICK = "WA";

        onView(withId(android.R.id.list))
                .check(matches(adapterHasCount(equalTo(mFakeStations.MAX_NUM_OF_ALL_STATIONS))));

        openDrawer(R.id.windcast_drawer);
        onView(withText(STATE_TO_CLICK)).perform(click());

        verify(mCache, times(1)).GetWeatherStationsFrom(STATE_TO_CLICK);
        onView(withId(android.R.id.list)).check(matches(adapterHasCount(equalTo(1))));
    }
}
