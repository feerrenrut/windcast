package com.feer.windcast.tests;

import android.app.Activity;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.MainActivity;
import com.feer.windcast.R;
import com.feer.windcast.testUtils.WindCastMocks;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * http://youtu.be/uHoB0KzQGRg?t=54s
 * see https://code.google.com/p/android-test-kit/wiki/EspressoStartGuide
 */
public class testTitleBar extends ActivityInstrumentationTestCase2<MainActivity>
{
    public WindCastMocks Mocks;

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    public testTitleBar()
    {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        Mocks = new WindCastMocks(
                PreferenceManager.getDefaultSharedPreferences(
                        this.getInstrumentation().getTargetContext()));
    }

    public void test_initialTitleBar_NoStations_isWindcast()
    {
        Mocks.FakeData.HasStations(0).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        
        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.app_name);
        onView( withText(expectedTitle)).check(matches(isDisplayed()));
    }

    public void test_initialTitleBar_NoFavourites_isWindcast()
    {
        Mocks.FakeData.HasStations(10).HasFavourites(0);
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        
        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.app_name);
        onView( withText(expectedTitle)).check(matches(isDisplayed()));
    }

    public void test_initialTitleBar_withFavourites_isFavourites()
    {
        Mocks.FakeData.HasStations(10).HasFavourites(3);
        Mocks.JustUseMocksWithFakeData();
        launchActivity();
        
        Activity act = getActivity();
        final String expectedTitle = act.getResources().getString(R.string.favourite_stations);
        onView( withText(expectedTitle)).check(matches(isDisplayed()));
    }
}
