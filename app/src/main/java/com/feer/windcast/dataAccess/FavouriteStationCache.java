package com.feer.windcast.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.feer.windcast.AWeatherStation;
import com.feer.windcast.dataAccess.DBContract.DBContract2;

import java.util.ArrayList;


/**
 * A class to interact with the Favourite Station table
 */
public class FavouriteStationCache
{
    private Context mContext = null;
    private BackgroundTaskManager mTaskManager = null;
    private ArrayList<String> mFavUrls = null; // only populated once GetFavouritesFromDB is called
    private boolean mInitialised = false;

    /**
     * Initialises the FavouriteStationCache so calls to all other functions
     * can be made synchronously.
     * This must not be called from the UI thread!
     */
    public synchronized void Initialise(Context context, BackgroundTaskManager taskManager)
    {
        mContext = context;
        mTaskManager = taskManager;
        mFavUrls = GetFavouritesFromDB();
        mInitialised = true;
    }

    synchronized
    public void AddFavouriteStation(final AWeatherStation station)
    {
        CheckInitialisation();

        if(!mFavUrls.contains(station.GetURL())) // do add the same station twice!
        {
            mFavUrls.add(station.GetURL());

            mTaskManager.RunInBackground(
                    new BackgroundTask<Void>() {
                        @Override
                        public Void DoInBackground() {
                            AddFavouriteStationToDB(station);
                            return null;
                        }
                    }
            );
        }
    }

    synchronized
    public void RemoveFavouriteStation(final AWeatherStation station)
    {
        CheckInitialisation();

        if(mFavUrls.contains(station.GetURL())) // only remove if it is there
        {
            mFavUrls.remove(station.GetURL());

            mTaskManager.RunInBackground(
                    new BackgroundTask<Void>() {
                        @Override
                        public Void DoInBackground() {
                            RemoveFavouriteStationFromDB(station);
                            return null;
                        }
                    }
            );
        }
    }

    synchronized
    public ArrayList<String> GetFavouriteURLs()
    {
        CheckInitialisation();
        return mFavUrls;
    }

    private void CheckInitialisation() {
        if(!mInitialised)
        {
            throw new IllegalStateException("Accessing Favourites cache before Initialise() was called or complete!!");
        }
    }

    private synchronized void AddFavouriteStationToDB(AWeatherStation station)
    {
        station.IsFavourite = true;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract2.FavouriteStation.COLUMN_NAME_STATION_NAME, station.GetName());
        values.put(DBContract2.FavouriteStation.COLUMN_NAME_URL, station.GetURL());
        db.insert(
                DBContract2.FavouriteStation.TABLE_NAME,
                "null",
                values
                 );
    }

    private synchronized void RemoveFavouriteStationFromDB(AWeatherStation station)
    {
        station.IsFavourite = false;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        String selection = DBContract2.FavouriteStation.COLUMN_NAME_URL + " = ?";
        String[] selectionArgs = {station.GetURL()};
        db.delete(DBContract2.FavouriteStation.TABLE_NAME, selection, selectionArgs);
    }

    private synchronized ArrayList<String> GetFavouritesFromDB()
    {
        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getReadableDatabase();
        Cursor c = db.query(
                DBContract2.FavouriteStation.TABLE_NAME,
                new String[]{DBContract2.FavouriteStation.COLUMN_NAME_URL},
                null, // selection
                null, // selection args
                null, // group by
                null, // having
                null  // order by
                           );
        ArrayList<String> favs = new ArrayList<String>();

        while(c.moveToNext())
        {
            favs.add(
                    c.getString(
                            c.getColumnIndexOrThrow(
                                    DBContract2.FavouriteStation.COLUMN_NAME_URL)));
        }
        return favs;
    }
}
