package org.zuzuk.dataproviding.requests.local.base;

import android.support.v4.content.Loader;

import org.zuzuk.dataproviding.requests.base.Task;
import org.zuzuk.dataproviding.requests.base.ResultListener;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async task executing
 */
public interface TaskExecutor {

    /* Executes task */
    public <T> Loader<T> executeTask(Task<T> task, ResultListener<T> resultListener);
}