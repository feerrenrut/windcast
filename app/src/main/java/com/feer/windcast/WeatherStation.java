package com.feer.windcast;

import java.net.URL;
import java.util.Locale;

public class WeatherStation
{

    String ID;

    URL url;

    /* The name of the observation station
     */
    String Name;


    String State;

    String TimeZone;

    @Override
    public String toString()
    {
        return String.format(Locale.US, "%s (%s)", Name, State);
    }
}
