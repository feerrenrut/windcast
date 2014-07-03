package com.feer.windcast.dataAccess;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


abstract class BackgroundTask<ResultType>
{
    public abstract ResultType DoInBackground();
    public void OnPostExecute(ResultType result)
    {}
}
/**
 *
 */
public class BackgroundTaskManager
{
    private final ArrayList<BackgroundAsyncTask> mTasks;
    private final BackGroundTaskRemover mRemover;

    public BackgroundTaskManager()
    {
        mTasks = new ArrayList<BackgroundAsyncTask>();
        mRemover = new BackGroundTaskRemover();
    }

    public void RunInBackground(BackgroundTask task)
    {
        BackgroundAsyncTask t = new BackgroundAsyncTask(mRemover, task);
        mTasks.add(t);
        t.execute();
    }

    public void WaitForTasksToComplete()
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

    private class BackGroundTaskRemover
    {
        void RemoveTask(BackgroundAsyncTask task)
        {
            mTasks.remove(task);
        }
    }

    private  class BackgroundAsyncTask<ResultType> extends AsyncTask<Object, Void, ResultType>
    {
        final private BackGroundTaskRemover mRemover;
        final private BackgroundTask<ResultType> mTask;

        public BackgroundAsyncTask(BackGroundTaskRemover mRemover, BackgroundTask<ResultType> mTask)
        {
            this.mRemover = mRemover;
            this.mTask = mTask;
        }

        @Override
        protected ResultType doInBackground(Object... params)
        {
            return mTask.DoInBackground();
        }

        @Override
        protected void onPostExecute(ResultType result)
        {
            mRemover.RemoveTask(this);
            mTask.OnPostExecute(result);
        }
    }
}
