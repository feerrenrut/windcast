package com.feer.windcast.backend;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class LatestReading{

    final private static DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.ordinalDateTime().withOffsetParsed();

    public static DateTime getLocalTime(LatestReading r) {
        final String lt = r.getLocalTime();
        if(lt != null) {
            return DATE_TIME_FORMATTER.parseDateTime(lt);
        }
        return null;
    }

    public static LatestReading setLocalTime(LatestReading r, DateTime d){
        r.setLocalTime( d.toString(DATE_TIME_FORMATTER) );
        return r;
    }

    /* Local time at the place of reading
     */
    private String LocalTime;

    /* The cardinal direction (n,s,e,w) the wind is coming from eg nnw
    */
    private String CardinalWindDirection;

    /* The speed of the wind in KiloMeters per Hour
     */
    private Integer WindSpeed_KMH;


    public String getLocalTime() {
        return LocalTime;
    }

    public void setLocalTime(String localTime) {
        LocalTime = localTime;
    }

    public String getCardinalWindDirection() {
        return CardinalWindDirection;
    }

    public void setCardinalWindDirection(String cardinalWindDirection) {
        CardinalWindDirection = cardinalWindDirection;
    }

    public Integer getWindSpeed_KMH() {
        return WindSpeed_KMH;
    }

    public void setWindSpeed_KMH(Integer windSpeed_KMH) {
        WindSpeed_KMH = windSpeed_KMH;
    }
}
