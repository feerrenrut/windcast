package com.feer.windcast.tests;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.WeatherDataCache;
import com.feer.windcast.testUtils.FakeWeatherStationData;

import static com.feer.windcast.testUtils.AdapterMatchers.adapterHasCount;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.typeText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.closeDrawer;
import static com.google.android.apps.common.testing.ui.espresso.contrib.DrawerActions.openDrawer;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.hasSibling;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isAssignableFrom;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
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
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(isDisplayed()));

        closeDrawer(drawerID);
        onView(allOf(withText(R.string.states), hasSibling(withId(R.id.left_drawer_list))))
                .check(matches(not(isDisplayed())));
    }

    public void test_drawerOpen_hasExpectedContents()
    {
        launchActivity();
        openDrawer(drawerID);

        onView(allOf(isAssignableFrom(TextView.class), hasSibling(withId(R.id.left_drawer_list))))
                        .check(matches(withText(R.string.states)));

        CharSequence[] stateList = getActivity().getResources().getTextArray(R.array.drawer_options);

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
        when(mCache.GetWeatherStationsFrom(anyString())).thenReturn(mFakeStations.GetSingleStation());

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
}
