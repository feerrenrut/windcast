package com.feer.windcast.dataAccess.DBContract;

import android.provider.BaseColumns;

/**
 * Specifies the contract with the database, this is the formal definition of the database schema
 */
public final class DBContract2
{
    DBContract2(){}

    public static abstract class FavouriteStation implements BaseColumns
    {
        public static final String TABLE_NAME = "favouriteStations";
        public static final String COLUMN_NAME_STATION_NAME = "name"; // type: TEXT
        public static final String COLUMN_NAME_URL = "url"; // type: TEXT

        public static final String TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_STATION_NAME + " TEXT, " +
                        COLUMN_NAME_URL + " TEXT);";
    }
}
