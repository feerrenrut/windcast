package com.feer.windcast.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Handles the creation and upgrade of databases.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "windcast_db";
    private static final int DATABASE_VERSION = 1;

    private static final String FAVOURITES_TABLE_CREATE =
            "CREATE TABLE " + DBContract.FavouriteStation.TABLE_NAME + " (" +
            DBContract.FavouriteStation.COLUMN_NAME_STATION_NAME + " TEXT, " +
            DBContract.FavouriteStation.COLUMN_NAME_URL + " TEXT);";

    private static DBOpenHelper sInstance = null;

    public static DBOpenHelper Instance(Context context)
    {
        if(sInstance == null)
        {
            sInstance = new DBOpenHelper(context);
        }

        return sInstance;
    }

    private DBOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(FAVOURITES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}
