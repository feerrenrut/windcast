package com.feer.windcast;

import java.net.URL;
import java.util.Locale;

public class WeatherStation implements Comparable
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

    @Override
    public int compareTo(Object o)
    {
        try
        {
            WeatherStation other = (WeatherStation)o;
            return Name.compareTo(other.Name);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
}
