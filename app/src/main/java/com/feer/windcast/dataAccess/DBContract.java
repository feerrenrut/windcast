package com.feer.windcast.dataAccess;

import android.provider.BaseColumns;

/**
 * Specifies the contract with the database, this is the formal definition of the database schema
 */
public final class DBContract
{
    DBContract(){}

    public static abstract class FavouriteStation implements BaseColumns
    {
        public static final String TABLE_NAME = "favouriteStations";
        public static final String COLUMN_NAME_STATION_NAME = "name";
        public static final String COLUMN_NAME_URL = "url";
    }
}
