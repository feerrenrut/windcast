package com.feer.windcast.tests;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.testUtils.WindCastMocks;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.openDrawer;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testSearchIcon extends ActivityInstrumentationTestCase2<MainActivity>
{
    public testSearchIcon()
    {
        super(MainActivity.class);
    }

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }
    
    WindCastMocks Mocks = null;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
    }

    public void test_NoStations_searchIconNotShown()
    {
        // depends on
        // testTitleBar.test_initialTitleBar_NoStations_isWindcast();

        Mocks.FakeData.HasStations(0).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        onView(withId(R.id.search)).check(doesNotExist());
    }

    public void test_withStationsButNoFavourites_onAllStationsView_searchIconShown()
    {
        // depends on 
        // testTitleBar.test_initialTitleBar_NoFavourites_isWindcast();
        Mocks.FakeData.HasStations(10).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        onView(withId(R.id.search)).check(matches(isDisplayed()));
    }

    public void test_withStationsButNoFavourites_onFavouritesView_searchIconNotShown()
    {
        // depends on
        // testTitleBar.test_initialTitleBar_NoFavourites_isWindcast();
        Mocks.FakeData.HasStations(10).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        openDrawer(R.id.drawer_layout);
        onView(withText(R.string.favourites)).perform(click());

        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.favourite_stations);

        onView( withText(expectedTitle)).check(matches(isDisplayed()));

        onView(withId(R.id.search)).check(doesNotExist());
    }

    public void test_withStationsAndFavourites_onFavouritesView_searchIconShown()
    {
        // depends on
        // testTitleBar.test_initialTitleBar_withFavourites_isFavourites();
        Mocks.FakeData.HasStations(10).HasFavourites(3);
        Mocks.JustUseMocksWithFakeData();
        
        launchActivity();

        onView(withId(R.id.search)).check(matches(isDisplayed()));
    }


    public void test_withStationsAndFavourites_onFavouritesView_afterRotate_SearchAvailable()
    {
        test_withStationsAndFavourites_onFavouritesView_searchIconShown();
        
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        onView(withId(R.id.search)).check(matches(isDisplayed()));
    }
}
