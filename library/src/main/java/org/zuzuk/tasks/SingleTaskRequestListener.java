package org.zuzuk.tasks;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * Single task request result listener.
 * Use it only if you sure, that there will be no any tasks after that
 */
public abstract class SingleTaskRequestListener<T> implements RequestListener<T> {
    private final TaskResultController taskResultController;

    public SingleTaskRequestListener(TaskResultController taskResultController) {
        this.taskResultController = taskResultController;
    }

    @Override
    public void onRequestSuccess(T response) {
        onTaskSuccess(response);
        taskResultController.fireOnSuccess();
    }

    @Override
    public void onRequestFailure(SpiceException ex) {
        onTaskFailure(ex);
        taskResultController.fireOnFailure(ex);
    }

    /* Raises when task is successfully completed */
    public abstract void onTaskSuccess(T response);

    /* Raises when task is failed */
    public abstract void onTaskFailure(Exception ex);
}
