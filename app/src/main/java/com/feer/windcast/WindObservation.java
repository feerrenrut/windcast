package com.feer.windcast;

public class WindObservation implements IWindObservation {
    /* The cardinal direction (n,s,e,w) the wind is coming from eg nnw
     */
    private String CardinalWindDirection;

    /* The compass bearing the wind is coming from
    *  0 = North, 90 = East, 180 = South, 270 = West
    */
    private Float WindBearing;

    /* The speed of the wind in KiloMeters per Hour
     */
    private Integer WindSpeed_KMH;

    /* The speed of the wind in knots
    */
    private Integer WindSpeed_KN;

    /* The wind gust speed in KiloMeters per Hour
     */
    private Integer WindGustSpeed_KMH;

    @Override
    public String getCardinalWindDirection() {
        return CardinalWindDirection;
    }

    public void setCardinalWindDirection(String cardinalWindDirection) {
        CardinalWindDirection = cardinalWindDirection;
    }

    @Override
    public Float getWindBearing() {
        return WindBearing;
    }

    public void setWindBearing(Float windBearing) {
        WindBearing = windBearing;
    }

    @Override
    public Integer getWindSpeed_KMH() {
        return WindSpeed_KMH;
    }

    public void setWindSpeed_KMH(Integer windSpeed_KMH) {
        WindSpeed_KMH = windSpeed_KMH;
    }

    @Override
    public Integer getWindSpeed_KN() {
        return WindSpeed_KN;
    }

    public void setWindSpeed_KN(Integer windSpeed_KN) {
        WindSpeed_KN = windSpeed_KN;
    }

    @Override
    public Integer getWindGustSpeed_KMH() {
        return WindGustSpeed_KMH;
    }

    public void setWindGustSpeed_KMH(Integer windGustSpeed_KMH) {
        WindGustSpeed_KMH = windGustSpeed_KMH;
    }
}
