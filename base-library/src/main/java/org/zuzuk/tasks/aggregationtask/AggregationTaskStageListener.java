package org.zuzuk.tasks.aggregationtask;

/**
 * Created by Gavriil Sitnikov on 09/02/2015.
 * Listener only for stage events
 */
public interface AggregationTaskStageListener {

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

}
