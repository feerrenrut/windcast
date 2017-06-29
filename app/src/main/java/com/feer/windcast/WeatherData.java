package com.feer.windcast;

import java.util.List;

/**
 * Describes weather data for a particular location
 */
public class WeatherData implements Comparable
{
    public AWeatherStation Station;

    /* The observation data for this location */
    public List<IObservationReading> ObservationData;

    /* The latest reading for this location */
    private IObservationReading latestReading;

    public IObservationReading getLatestReading() {
        if(latestReading == null && ObservationData != null && !ObservationData.isEmpty()){
            latestReading = ObservationData.get(0);
        }
        return latestReading;
    }

    public void setLatestReading(IObservationReading latestReading) {
        this.latestReading = latestReading;
    }

    /* A description of the source of the data
     */
    public String Source;

    private boolean mUpdateFailed;
    public boolean isUpdateFailed() {
        return mUpdateFailed;
    }
    public void setUpdateFailed(boolean updateFailed) {
        this.mUpdateFailed = updateFailed;
    }

    private boolean mIsStaleData;
    public boolean isStaleData() {
        return mIsStaleData;
    }
    public void setStaleData(boolean isStaleData) {
        this.mIsStaleData = isStaleData;
    }

    private boolean mUsingFileCacheData;
    public boolean isUsingFileCacheData() {
        return mUsingFileCacheData;
    }
    public void setUsingFileCacheData(boolean usingFileCacheData) {
        this.mUsingFileCacheData = usingFileCacheData;
    }

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

    private int mHashCode =0;
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
}

