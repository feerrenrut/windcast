package com.feer.windcast.dataAccess;

public abstract class BackgroundTask<ResultType>
{
    public abstract ResultType DoInBackground();
    public void OnPostExecute(ResultType result)
    {}
}
