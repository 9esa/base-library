package org.zuzuk.tasks.base;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.local.LocalTask;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async task executing
 */
public interface TaskExecutor {

    /* Executes task in foreground */
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener);

    /* Executes local task in foreground */
    public void executeTask(LocalTask task);

    /* Executes task in background */
    public <T> void executeTaskBackground(Task<T> task,
                                RequestListener<T> requestListener);

    /* Executes local task in background */
    public void executeTaskBackground(LocalTask task);
}