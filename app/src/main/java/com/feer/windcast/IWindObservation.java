package com.feer.windcast;

/**
 * Created by Reef on 23/10/2015.
 */
public interface IWindObservation {
    String getCardinalWindDirection();

    Float getWindBearing();

    Integer getWindSpeed_KMH();

    Integer getWindSpeed_KN();

    Integer getWindGustSpeed_KMH();
}
