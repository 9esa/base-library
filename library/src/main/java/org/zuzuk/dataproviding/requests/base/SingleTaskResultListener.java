package org.zuzuk.dataproviding.requests.base;

/**
 * Created by Gavriil Sitnikov on 07/09/2014.
 * Single task result listener. Use it only if you sure, that there will be no any tasks after that
 */
public abstract class SingleTaskResultListener<T> implements ResultListener<T> {
    private final TaskResultController taskResultController;

    public SingleTaskResultListener(TaskResultController taskResultController) {
        this.taskResultController = taskResultController;
    }

    @Override
    public void onSuccess(T response) {
        onTaskSuccess(response);
        taskResultController.fireOnSuccess();
    }

    @Override
    public void onFailure(Exception ex) {
        onTaskFailure(ex);
        taskResultController.fireOnFailure(ex);
    }

    /* Raises when task is successfully completed */
    public abstract void onTaskSuccess(T response);

    /* Raises when task is failed */
    public abstract void onTaskFailure(Exception ex);
}
