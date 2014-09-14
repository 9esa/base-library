package org.zuzuk.dataproviding.requests.local.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import org.zuzuk.dataproviding.requests.base.Task;
import org.zuzuk.dataproviding.requests.base.ResultListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Loader that should be used for local loading data or direct remote requesting
 */
public class LocalLoader<T> extends AsyncTaskLoader<T> implements LoaderManager.LoaderCallbacks<T> {
    private final Task<T> task;
    private final ResultListener<T> resultListener;
    private Exception exception;

    public LocalLoader(Context context, Task<T> task, ResultListener<T> resultListener) {
        super(context);
        this.task = task;
        this.resultListener = resultListener;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        exception = null;
        forceLoad();
    }

    @Override
    public T loadInBackground() {
        try {
            return task.execute();
        } catch (Exception e) {
            exception = e;
            return null;
        }
    }

    @Override
    public void deliverResult(T data) {
        super.deliverResult(data);

        if (exception == null) {
            resultListener.onSuccess(data);
        } else {
            resultListener.onFailure(exception);
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    public Loader<T> onCreateLoader(int id, Bundle args) {
        return this;
    }

    @Override
    public void onLoadFinished(Loader<T> loader, T data) {
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
    }
}
