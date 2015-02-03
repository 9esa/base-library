package org.zuzuk.tasks.aggregationtask;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 18/09/2014.
 * An object that can detect executing several Task objects and observe them as one long task.
 * All Task objects should be created in one thread (prefer UI)
 */
public abstract interface AggregationTask {

    /* Returns is task still needs something to load on current stage */
    boolean isLoadingNeeded(AggregationTaskStage currentTaskStage);

    /* Returns is task loaded all needed info at current stage */
    boolean isLoaded(AggregationTaskStage currentTaskStage);

    /* Loading data at current stage */
    void load(boolean isInBackground, AggregationTaskStage currentTaskStage);

    /* Calls when loading of some stage have started */
    void onLoadingStarted(boolean isInBackground, AggregationTaskStage currentTaskStage);

    /* Calls when loading of some stage have completed successfully */
    void onLoaded(boolean isInBackground, AggregationTaskStage currentTaskStage);

    /* Calls when loading of some stage have failed with exceptions */
    void onFailed(boolean isInBackground, AggregationTaskStage currentTaskStage, List<Exception> exceptions);
}
