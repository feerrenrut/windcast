package com.feer.windcast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class WeatherStation implements Comparable
{

    String ID;

    public URL url;

    /* The name of the observation station
     */
    public String Name;

    public boolean IsFavourite = false;


    String State;

    String TimeZone;

    public WeatherStation(String name, String dataUrl) throws MalformedURLException
    {
        Name = name;
        url = new URL(dataUrl);
    }

    public WeatherStation()
    {}

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
            return compareTo(other);
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private int compareTo(WeatherStation other)
    {
        return Name.compareTo(other.Name);
    }

    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof WeatherStation)
        {
            WeatherStation other = (WeatherStation) object;
            sameSame =
                    this.compareTo(other) == 0 &&
                    this.url.toString().equals(other.url.toString());
        }

        return sameSame;
    }

    @Override
    public int hashCode()
    {
        if(mHashCode ==0)
        {
            mHashCode = 42;

            mHashCode = 3 * mHashCode + toString().hashCode();
            mHashCode = 3 * mHashCode + url.hashCode();
        }
        return mHashCode;
    }

    private int mHashCode =0;
}
