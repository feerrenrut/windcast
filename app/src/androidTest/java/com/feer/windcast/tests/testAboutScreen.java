package com.feer.windcast.tests;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.About;
import com.feer.windcast.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Reef on 12/10/2014.
 */
public class testAboutScreen extends ActivityInstrumentationTestCase2<About>
{
    public testAboutScreen()
    {
        super(About.class);
    }

    // Activity is not created until get activity is called
    private void launchActivity()
    {
        getActivity();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void test_noAction_BOMAttributionShown()
    {
        launchActivity();

        onView( withText("About")).check(matches(isDisplayed()));
        onView(withId(R.id.bom_attrib_imageView))
                .check(matches(isDisplayed()));
    }

    public void test_landscape_BOMAttributionShown()
    {
        launchActivity();

        onView( withText("About")).check(matches(isDisplayed()));
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        onView(withId(R.id.bom_attrib_imageView))
                .check(matches(isDisplayed()));
    }

    /* Ideally we would test that we can launch a browser to the website... or test that the correct
    intent was launched. This is apparently not possible:
    http://stackoverflow.com/questions/21855540/android-espresso-web-browser

    public void test_clickBomAttribution_launchesBomWebsite()
    { } */

}
