package com.feer.windcast.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.feer.windcast.dataAccess.DBContract.DBContract1;
import com.feer.windcast.dataAccess.DBContract.DBContract2;

/**
 * Handles the creation and upgrade of databases.
 */
public class DBOpenHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "windcast_db";
    private static final int DATABASE_VERSION = 2;

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
        db.execSQL(DBContract2.FavouriteStation.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        int currentVersion = oldVersion;
        while(currentVersion != newVersion) {
            switch (currentVersion) {
                case 1:
                    fixDuplicateFavourites(db);
            }
            ++currentVersion;
        }  
        
    }

    /*
    In DB v1 there duplicates of favourites were created. Use DB v1 values to fix.
     */
    private void fixDuplicateFavourites(SQLiteDatabase db)
    {
        final String q =
                "delete from " + DBContract1.FavouriteStation.TABLE_NAME + ' ' +
                "where rowid not in " +
                '(' +
                   "select min(rowid) from " + DBContract1.FavouriteStation.TABLE_NAME + ' ' +
                   "group by " + DBContract1.FavouriteStation.COLUMN_NAME_URL +
                ')';

        db.execSQL(q);
    }
}
