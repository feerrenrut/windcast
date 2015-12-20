package com.feer.windcast;

import com.feer.windcast.backend.windcastdata.model.LatestReading;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

/**
 * Created by Reef on 23/10/2015.
 */
public class GAE_ObservationReading implements IObservationReading, IWindObservation{
    private final LatestReading reading;
    private Date localTime;
    public GAE_ObservationReading(LatestReading reading){
        this.reading = reading;
    }


    final static DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.ordinalDateTime().withOffsetParsed();
    final static float KMH_TO_KNOT = 0.539957f;

    @Override
    public Date getLocalTime() {
        if(localTime==null && reading.getLocalTime() != null){
            localTime = DATE_TIME_FORMATTER
                    .parseDateTime(reading.getLocalTime())
                    .toDate();
        }
        return localTime;
    }

    @Override
    public IWindObservation getWind_Observation() {
        return this;
    }

    @Override
    public String getCardinalWindDirection() {
        return reading.getCardinalWindDirection();
    }

    @Override
    public Float getWindBearing() {
        return ObservationReader.ConvertCardinalCharsToBearing(getCardinalWindDirection());
    }

    @Override
    public Integer getWindSpeed_KMH() {
        return reading.getWindSpeedKMH();
    }

    @Override
    public Integer getWindSpeed_KN() {
        return (int) (KMH_TO_KNOT * getWindSpeed_KMH());
    }

    @Override
    public Integer getWindGustSpeed_KMH() {
        return 0;
    }
}
