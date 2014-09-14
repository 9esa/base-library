package org.zuzuk.tasks.base;

import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.local.LocalTask;

/**
 * Created by Gavriil Sitnikov on 07/14.
 * Interface that supply async task executing
 */
public interface TaskExecutor {

    /* Executes task */
    public <T> void executeTask(Task<T> task, RequestListener<T> requestListener);

    /* Executes local task */
    public void executeTask(LocalTask task);
}