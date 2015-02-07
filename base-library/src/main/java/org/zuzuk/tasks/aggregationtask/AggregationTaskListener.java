package org.zuzuk.tasks.aggregationtask;

import java.util.List;

/**
 * Created by Gavriil Sitnikov on 06/02/2015.
 * Listener for aggregation task events
 */
public interface AggregationTaskListener {

    /* Calls when loading of some stage have started */
    void onLoadingStarted(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState);

    /* Calls when loading of some stage have completed successfully */
    void onLoadingFinished(AggregationTaskStage currentTaskStage, AggregationTaskStageState currentTaskStageState);
}
