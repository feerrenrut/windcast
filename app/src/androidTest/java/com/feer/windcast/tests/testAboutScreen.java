package com.feer.windcast.tests;

import android.content.pm.ActivityInfo;
import android.test.ActivityInstrumentationTestCase2;

import com.feer.windcast.About;
import com.feer.windcast.R;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

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

        assertEquals("About", getActivity().getActionBar().getTitle().toString());
        onView(withId(R.id.bom_attrib_imageView))
                .check(matches(isDisplayed()));
    }

    public void test_landscape_BOMAttributionShown()
    {
        launchActivity();

        assertEquals("About", getActivity().getActionBar().getTitle().toString());
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
