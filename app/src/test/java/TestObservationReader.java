package com.feer.windcast;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.widget.TextView;

import com.feer.windcast.ObservationReader;
import com.feer.windcast.WeatherData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@RunWith(RobolectricTestRunner.class)
public class TestObservationReader {

    @Test
    public void testInstantiation() throws FileNotFoundException, IOException{

        String current = new java.io.File(".").getCanonicalPath();
        ass
        final String testDataFileName = "data\\observationData.txt";
        InputStream testStream = new FileInputStream(testDataFileName);

        WeatherData wd = ObservationReader.ReadJsonStream(testStream);

        assertEquals("Badgingarra", wd.WeatherStationName);
    }
}