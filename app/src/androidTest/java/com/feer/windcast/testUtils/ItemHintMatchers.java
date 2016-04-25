package com.feer.windcast.testUtils;

import android.content.res.Resources;
import android.view.View;
import android.widget.EditText;

import android.support.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.is;

public final class ItemHintMatchers
{

    public static Matcher<View> withItemHint(String hintText) {
        // use preconditions to fail fast when a test is creating an invalid matcher.
        checkArgument(!(hintText.equals(null)));
        return withItemHint(is(hintText));
    }

    public static Matcher<View> withItemHint(final int resourceId)
    {
        return new BoundedMatcher<View, EditText>(EditText.class) {
            private String resourceName = null;
            private String expectedText = null;

            @Override
            public void describeTo(Description description) {
                description.appendText("with string from resource id: ");
                description.appendValue(resourceId);
                if (null != resourceName) {
                    description.appendText("[");
                    description.appendText(resourceName);
                    description.appendText("]");
                }
                if (null != expectedText) {
                    description.appendText(" value: ");
                    description.appendText(expectedText);
                }
            }

            @Override
            public boolean matchesSafely(EditText editText) {
                if (null == expectedText) {
                    try {
                        expectedText = editText.getResources().getString(resourceId);
                        resourceName = editText.getResources().getResourceEntryName(resourceId);
                    } catch (Resources.NotFoundException ignored) {
                        /* view could be from a context unaware of the resource id. */
                    }
                }
                if (null != expectedText) {
                    return expectedText.equals(editText.getHint().toString());
                } else {
                    return false;
                }
            }
        };
    }

    public static Matcher<View> withItemHint(final Matcher<String> matcherText) {
        // use preconditions to fail fast when a test is creating an invalid matcher.
        checkNotNull(matcherText);
        return new BoundedMatcher<View, EditText>(EditText.class) {

            @Override
            public void describeTo(Description description) {
                description.appendText("with item hint: " + matcherText);
            }

            @Override
            protected boolean matchesSafely(EditText editTextField) {
                return matcherText.matches(editTextField.getHint().toString());
            }
        };
    }

}
