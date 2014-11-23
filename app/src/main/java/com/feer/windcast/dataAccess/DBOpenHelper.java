package com.feer.windcast.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.feer.windcast.dataAccess.DBContract.FavouriteStation;

/**
 * Handles the creation and upgrade of databases.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "windcast_db";
    private static final int DATABASE_VERSION = 2;

    private static final String FAVOURITES_TABLE_CREATE =
            "CREATE TABLE " + FavouriteStation.TABLE_NAME + " (" +
            FavouriteStation.COLUMN_NAME_STATION_NAME + " TEXT, " +
            FavouriteStation.COLUMN_NAME_URL + " TEXT);";

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
        if(oldVersion == 1)
        {
            fixDuplicateFavourites(db);
        }
    }

    public void fixDuplicateFavourites(SQLiteDatabase db)
    {
        String q =
                "delete from " + FavouriteStation.TABLE_NAME + ' ' +
                "where rowid not in " +
                '(' +
                   "select min(rowid) from " + FavouriteStation.TABLE_NAME + ' ' +
                   "group by " + FavouriteStation.COLUMN_NAME_URL +
                ')';

        db.execSQL(q);
    }
}
