package org.zuzuk.ui.services;

import android.app.Service;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;

/**
 * Created by Gavriil Sitnikov on 03/10/2014.
 * Base service that can execute requests
 */
public abstract class BaseRequestExecutorService extends Service {
    private SpiceManager remoteSpiceManager;
    private SpiceManager localSpiceManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (getApplicationContext() instanceof SpiceManagerProvider) {
            localSpiceManager = ((SpiceManagerProvider) getApplicationContext()).createLocalSpiceManager();
            remoteSpiceManager = ((SpiceManagerProvider) getApplicationContext()).createRemoteSpiceManager();
        } else
            throw new RuntimeException("To use TaskExecutorHelper your Application class should implement SpiceManagerProvider");

        remoteSpiceManager.start(this);
        localSpiceManager.start(this);
    }

    @Override
    public void onDestroy() {
        remoteSpiceManager.shouldStop();
        localSpiceManager.shouldStop();
        super.onDestroy();
    }

    /* Executes request in background */
    public <T> void executeRequest(RemoteRequest<T> request,
                                   RequestListener<T> requestListener) {
        remoteSpiceManager.execute(request, requestListener);
    }

    /* Executes wrapped request in background */
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        remoteSpiceManager.execute(requestWrapper.getPreparedRequest(), requestWrapper);
    }

    /* Executes local task in background */
    public void executeTask(LocalTask task) {
        executeTask(task, null);
    }

    /* Executes task in background */
    public <T> void executeTask(Task<T> task,
                                RequestListener<T> requestListener) {
        localSpiceManager.execute(task, requestListener);
    }
}