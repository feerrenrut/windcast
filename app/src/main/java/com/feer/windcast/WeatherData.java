package com.feer.windcast;

import java.util.List;
import java.util.Locale;

/**
 * Describes weather data for a particular location
 */
public class WeatherData implements Comparable
{
    public WeatherStation Station;

    /* The observation data for this location */
    public List<ObservationReading> ObservationData;

    @Override
    public String toString()
    {
        return Station.toString();
    }

    @Override
    public int compareTo(Object o)
    {
        try
        {
            WeatherData other = (WeatherData)o;
            return compareTo(other);
        }
        catch(Exception e)
        {
            return -1;
        }
    }

    private int compareTo(WeatherData other)
    {
        return Station.compareTo(other.Station);
    }
    
    @Override
    public boolean equals(Object object)
    {
        boolean sameSame = false;

        if (object != null && object instanceof WeatherData)
        {
            WeatherData other = (WeatherData) object;
            sameSame =
                    this.compareTo(other) == 0 &&
                            this.ObservationData.equals(other.ObservationData);
        }
        return sameSame;
    }

    @Override
    public int hashCode()
    {
        if(mHashCode ==0)
        {
            mHashCode = 45;

            mHashCode = 3 * mHashCode + Station.hashCode();
            mHashCode = 4 * mHashCode + ObservationData.hashCode();
        }
        return mHashCode;
    }

    private int mHashCode =0;
}

