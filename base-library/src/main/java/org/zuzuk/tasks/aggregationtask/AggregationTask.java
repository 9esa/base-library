package org.zuzuk.tasks.aggregationtask;

import org.zuzuk.tasks.base.Task;

/**
 * Created by Gavriil Sitnikov on 18/09/2014.
 * An object that can detect executing several Task objects and observe them as one long task.
 * All Task objects should be created in one thread (prefer UI)
 */
public abstract interface AggregationTask {

    /**
     * Returns is task still needs something to load on current stage
     * It is executing in non-UI worker thread.
     */
    boolean isLoadingNeeded(AggregationTaskStageState currentTaskStageState);

    /**
     * Returns is task loaded all needed info at current stage
     * It is executing in non-UI worker thread.
     */
    boolean isLoaded(AggregationTaskStageState currentTaskStageState);

    /**
     * Loading data at current stage
     * It is executing in main UI thread.
     */
    void load(AggregationTaskStageState currentTaskStageState);

    /**
     * Calls when loading of some stage have started
     * It is executing in main UI thread.
     */
    void onLoadingStarted( AggregationTaskStageState currentTaskStageState);

    /**
     * Calls when loading of some stage have completed successfully
     * It is executing in main UI thread.
     */
    void onLoaded(AggregationTaskStageState currentTaskStageState);

    /**
     * Calls when loading of some stage have failed with exceptions
     * It is executing in main UI thread.
     */
    void onFailed(AggregationTaskStageState currentTaskStageState);

    void processTask(Task task, AggregationTaskStageState currentTaskStageState);
}
