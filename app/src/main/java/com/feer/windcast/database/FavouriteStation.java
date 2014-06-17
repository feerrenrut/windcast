package com.feer.windcast.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.feer.windcast.WeatherStation;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.feer.windcast.database.DBContract.FavouriteStation.COLUMN_NAME_STATION_NAME;
import static com.feer.windcast.database.DBContract.FavouriteStation.COLUMN_NAME_URL;

/**
 * A class to interact with the Favourite Station table
 */
public class FavouriteStation
{
    private final Context mContext;
    private final ArrayList<AsyncTask> mTasks;

    public FavouriteStation(Context context)
    {
        mContext = context;
        mTasks = new ArrayList<AsyncTask>();
    }

    public interface AsyncComplete<Result>
    {
        public void OnAsyncComplete(Result result);
    }

    public void AddFavouriteStationAsync(final WeatherStation station)
    {
        final AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                AddFavouriteStation(station);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                mTasks.remove(this);
            }
        };

        mTasks.add(t);
        t.execute();
    }

    public void RemoveFavouriteStationAsync(final WeatherStation station)
    {
        final AsyncTask<Void, Void, Void> t = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                RemoveFavouriteStation(station);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                mTasks.remove(this);
            }
        };

        mTasks.add(t);
        t.execute();
    }

    public void GetFavouritesAsync(final AsyncComplete<ArrayList<WeatherStation>> onComplete)
    {
        final AsyncTask<Void, Void, ArrayList<WeatherStation>> t = new AsyncTask<Void, Void, ArrayList<WeatherStation>>()
        {
            @Override
            protected ArrayList<WeatherStation> doInBackground(Void... params)
            {
                return GetFavourites();
            }

            @Override
            protected void onPostExecute(ArrayList<WeatherStation> favs)
            {
                onComplete.OnAsyncComplete(favs);
                mTasks.remove(this);
            }
        };

        mTasks.add(t);
        t.execute();
    }

    public synchronized void WaitForTasksComplete()
    {
        for(AsyncTask t : mTasks)
        {
            try
            {
                t.get(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e)
            {
                Log.e("WindCast", "waiting for task, got InterruptedException: " + e.toString());
            } catch (ExecutionException e)
            {
                Log.e("WindCast", "waiting for task, got ExecutionException: " + e.toString());
            } catch (TimeoutException e)
            {
                Log.e("WindCast", "waiting for task, got TimeoutException: " + e.toString());
            }
        }
    }

    private synchronized void AddFavouriteStation(WeatherStation station)
    {
        station.IsFavourite = true;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_STATION_NAME, station.Name); //todo i dont think that name is unique, it may need to be combined with state?
        values.put(COLUMN_NAME_URL, station.url.toString());
        db.insert(
                DBContract.FavouriteStation.TABLE_NAME,
                "null", values
                 );
    }

    private synchronized void RemoveFavouriteStation(WeatherStation station)
    {
        station.IsFavourite = false;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        String selection = COLUMN_NAME_STATION_NAME + " LIKE ?";
        String[] selectionArgs = { station.Name };
        db.delete(DBContract.FavouriteStation.TABLE_NAME, selection, selectionArgs);
    }

    private synchronized ArrayList<WeatherStation> GetFavourites()
    {
        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getReadableDatabase();
        Cursor c = db.query(
                DBContract.FavouriteStation.TABLE_NAME,
                new String[]{COLUMN_NAME_STATION_NAME, COLUMN_NAME_URL},
                null, null, null, null, null
                           );
        ArrayList<WeatherStation> favs = new ArrayList<WeatherStation>();

        while(c.moveToNext())
        {
            try
            {
                WeatherStation station = new WeatherStation(
                        c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_STATION_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_URL)));
                station.IsFavourite = true;
                favs.add(station);
            } catch (MalformedURLException e)
            {
                Log.e("WindCast",
                        "MalformedURLException while getting favourites from DB. For station: "
                                + c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_URL)));
            }
        }
        return favs;
    }
}
