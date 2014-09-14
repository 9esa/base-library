package org.zuzuk.tasks;

import android.content.Context;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.request.listener.RequestListener;

import org.zuzuk.tasks.base.Task;
import org.zuzuk.tasks.base.TaskExecutor;
import org.zuzuk.tasks.local.LocalTask;
import org.zuzuk.tasks.remote.base.RemoteRequest;
import org.zuzuk.tasks.remote.base.RequestExecutor;
import org.zuzuk.tasks.remote.base.RequestWrapper;
import org.zuzuk.tasks.remote.base.SpiceManagerProvider;
import org.zuzuk.utils.Ln;

/**
 * Created by Gavriil Sitnikov on 14/09/2014.
 * Helper to work with lifecycle of object
 */
public class TaskExecutorHelper implements RequestExecutor, TaskExecutor {
    private SpiceManager localSpiceManager;
    private SpiceManager remoteSpiceManager;
    private Context context;

    private boolean checkManagersState(Object request) {
        if (!remoteSpiceManager.isStarted()) {
            Ln.w(request.getClass().getName() + " is requested after onPause");
            return false;
        }
        return true;
    }

    public void onCreate(Context context) {
        this.context = context;
        SpiceManagerProvider spiceManagerProvider = ((SpiceManagerProvider) context.getApplicationContext());
        localSpiceManager = spiceManagerProvider.createLocalSpiceManager();
        remoteSpiceManager = spiceManagerProvider.createRemoteSpiceManager();
    }

    public void onResume() {
        localSpiceManager.start(context);
    }

    @Override
    public <T> void executeRequest(RemoteRequest<T> request, RequestListener<T> requestListener) {
        if (checkManagersState(request)) {
            remoteSpiceManager.execute(request, requestListener);
        }
    }

    @Override
    public <T> void executeRequest(RequestWrapper<T> requestWrapper) {
        if (checkManagersState(requestWrapper)) {
            requestWrapper.execute(remoteSpiceManager);
        }
    }

    @Override
    public <T> void executeTask(Task<T> task, RequestListener<T> requestListener) {
        if (checkManagersState(task)) {
            localSpiceManager.execute(task, requestListener);
        }
    }

    @Override
    public void executeTask(LocalTask task) {
        executeTask(task, Task.<Void>createMockListener());
    }

    public void onPause() {
        localSpiceManager.shouldStop();
    }

    public void onDestroy() {
        context = null;
        localSpiceManager = null;
        remoteSpiceManager = null;
    }
}
